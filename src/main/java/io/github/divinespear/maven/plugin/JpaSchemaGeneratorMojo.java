/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.divinespear.maven.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.SmartPersistenceUnitInfo;

/**
 * Generate database schema or DDL scripts.
 * 
 * @author divinespear
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class JpaSchemaGeneratorMojo
        extends AbstractMojo {

    private final Log log = this.getLog();

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojo;

    @Parameter(defaultValue = "${plugin}", readonly = true)
    private PluginDescriptor plugin;

    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    @Component
    private ArtifactResolver resolver;

    /**
     * skip schema generation
     */
    @Parameter(property = "jpa-schema.generate.skip", required = true, defaultValue = "false")
    private boolean skip = false;

    public boolean isSkip() {
        return skip;
    }

    /**
     * generate as formatted
     */
    @Parameter(property = "jpa-schema.generate.format", required = true, defaultValue = "false")
    private boolean format = false;

    public boolean isFormat() {
        return format;
    }

    /**
     * scan test classes
     */
    @Parameter(property = "jpa-schema.generate.scan-test-classes", required = true, defaultValue = "false")
    private boolean scanTestClasses = false;

    public boolean isScanTestClasses() {
        return scanTestClasses;
    }

    /**
     * location of {@code persistence.xml} file
     * <p>
     * Note for Hibernate <b>DOES NOT SUPPORT custom location.</b> ({@link SchemaExport} support it, but JPA 2.1 schema
     * generator does NOT.)
     */
    @Parameter(required = true, defaultValue = PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML_DEFAULT)
    private String persistenceXml = PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML_DEFAULT;

    public String getPersistenceXml() {
        return persistenceXml;
    }

    /**
     * unit name of {@code persistence.xml}
     */
    @Parameter(required = true, defaultValue = "default")
    private String persistenceUnitName = "default";

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    /**
     * schema generation action for database
     * <p>
     * support value is {@code none}, {@code create}, {@code drop}, {@code drop-and-create}, or
     * {@code create-or-extend-tables}.
     * <p>
     * {@code create-or-extend-tables} only support for EclipseLink with database target.
     */
    @Parameter(required = true, defaultValue = PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION)
    private String databaseAction = PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION;

    public String getDatabaseAction() {
        return databaseAction;
    }

    /**
     * schema generation action for script
     * <p>
     * support value is {@code none}, {@code create}, {@code drop}, or {@code drop-and-create}.
     */
    @Parameter(required = true, defaultValue = PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION)
    private String scriptAction = PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION;

    public String getScriptAction() {
        return scriptAction;
    }

    /**
     * output directory for generated ddl scripts
     * <p>
     * REQUIRED for {@link #scriptAction} is one of {@code create}, {@code drop}, or
     * {@code drop-and-create}.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-schema")
    private File outputDirectory;

    public File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * generated create script name
     * <p>
     * REQUIRED for {@link #scriptAction} is one of {@code create}, or {@code drop-and-create}.
     */
    @Parameter(defaultValue = "create.sql")
    private String createOutputFileName = "create.sql";

    public String getCreateOutputFileName() {
        return createOutputFileName;
    }

    public File getCreateOutputFile() {
        return this.outputDirectory == null ? null : new File(this.outputDirectory, this.createOutputFileName);
    }

    /**
     * generated drop script name
     * <p>
     * REQUIRED for {@link #scriptAction} is one of {@code drop}, or {@code drop-and-create}.
     */
    @Parameter(defaultValue = "drop.sql")
    private String dropOutputFileName = "drop.sql";

    public String getDropOutputFileName() {
        return dropOutputFileName;
    }

    public File getDropOutputFile() {
        return this.outputDirectory == null ? null : new File(this.outputDirectory, this.dropOutputFileName);
    }

    /**
     * specifies whether the creation of database artifacts is to occur on the basis of the object/relational mapping
     * metadata, DDL script, or a combination of the two.
     * <p>
     * support value is {@code metadata}, {@code script}, {@code metadata-then-script}, or
     * {@code script-then-metadata}.
     */
    @Parameter(defaultValue = PersistenceUnitProperties.SCHEMA_GENERATION_METADATA_SOURCE)
    private String createSourceMode = PersistenceUnitProperties.SCHEMA_GENERATION_METADATA_SOURCE;

    public String getCreateSourceMode() {
        return createSourceMode;
    }

    /**
     * create source file path.
     * <p>
     * REQUIRED for {@link #createSourceMode} is one of {@code script}, {@code metadata-then-script}, or
     * {@code script-then-metadata}.
     */
    @Parameter
    private File createSourceFile;

    public File getCreateSourceFile() {
        return createSourceFile;
    }

    /**
     * specifies whether the dropping of database artifacts is to occur on the basis of the object/relational mapping
     * metadata, DDL script, or a combination of the two.
     * <p>
     * support value is {@code metadata}, {@code script}, {@code metadata-then-script}, or
     * {@code script-then-metadata}.
     */
    @Parameter(defaultValue = PersistenceUnitProperties.SCHEMA_GENERATION_METADATA_SOURCE)
    private String dropSourceMode = PersistenceUnitProperties.SCHEMA_GENERATION_METADATA_SOURCE;

    public String getDropSourceMode() {
        return dropSourceMode;
    }

    /**
     * drop source file path.
     * <p>
     * REQUIRED for {@link #dropSourceMode} is one of {@code script}, {@code metadata-then-script}, or
     * {@code script-then-metadata}.
     */
    @Parameter
    private File dropSourceFile;

    public File getDropSourceFile() {
        return dropSourceFile;
    }

    /**
     * jdbc driver class name
     * <p>
     * default is declared class name in persistence xml.
     * <p>
     * and Remember, <strike><a href="http://callofduty.wikia.com/wiki/No_Russian" target="_blank">No
     * Russian</a></strike> you MUST configure jdbc driver as plugin's dependency.
     */
    @Parameter
    private String jdbcDriver;

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    /**
     * jdbc connection url
     * <p>
     * default is declared connection url in persistence xml.
     */
    @Parameter
    private String jdbcUrl;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * jdbc connection username
     * <p>
     * default is declared username in persistence xml.
     */
    @Parameter
    private String jdbcUser;

    public String getJdbcUser() {
        return jdbcUser;
    }

    /**
     * jdbc connection password
     * <p>
     * default is declared password in persistence xml.
     * <p>
     * If your account has no password (especially local file-base, like Apache Derby, H2, etc...), it can be omitted.
     */
    @Parameter
    private String jdbcPassword;

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    /**
     * database product name for emulate database connection. this should useful for script-only action.
     * <ul>
     * <li>specified if scripts are to be generated by the persistence provider and a connection to the target database
     * is not supplied.</li>
     * <li>The value of this property should be the value returned for the target database by
     * {@link DatabaseMetaData#getDatabaseProductName()}</li>
     * </ul>
     */
    @Parameter
    private String databaseProductName;

    public String getDatabaseProductName() {
        return databaseProductName;
    }

    /**
     * database major version for emulate database connection. this should useful for script-only action.
     * <ul>
     * <li>specified if sufficient database version information is not included from
     * {@link DatabaseMetaData#getDatabaseProductName()}</li>
     * <li>The value of this property should be the value returned for the target database by
     * {@link DatabaseMetaData#getDatabaseMajorVersion()}</li>
     * </ul>
     */
    @Parameter
    private Integer databaseMajorVersion;

    public Integer getDatabaseMajorVersion() {
        return databaseMajorVersion;
    }

    /**
     * database minor version for emulate database connection. this should useful for script-only action.
     * <ul>
     * <li>specified if sufficient database version information is not included from
     * {@link DatabaseMetaData#getDatabaseProductName()}</li>
     * <li>The value of this property should be the value returned for the target database by
     * {@link DatabaseMetaData#getDatabaseMinorVersion()}</li>
     * </ul>
     */
    @Parameter
    private Integer databaseMinorVersion;

    public Integer getDatabaseMinorVersion() {
        return databaseMinorVersion;
    }

    /**
     * line separator for generated schema file.
     * <p>
     * support value is one of {@code CRLF} (windows default), {@code LF} (*nix, max osx), and {@code CR}
     * (classic mac), in case-insensitive.
     * <p>
     * default value is system property {@code line.separator}. if JVM cannot detect {@code line.separator},
     * then use {@code LF} by <a href="http://git-scm.com/book/en/Customizing-Git-Git-Configuration">git
     * {@code core.autocrlf} handling</a>.
     */
    @Parameter
    private String lineSeparator = System.getProperty("line.separator", "\n");

    private static final Map<String, String> LINE_SEPARATOR_MAP = new HashMap<>();

    static {
        LINE_SEPARATOR_MAP.put("CR", "\r");
        LINE_SEPARATOR_MAP.put("LF", "\n");
        LINE_SEPARATOR_MAP.put("CRLF", "\r\n");
    }

    public String getLineSeparator() {
        String actual = StringUtils.isEmpty(lineSeparator) ? null : LINE_SEPARATOR_MAP.get(lineSeparator.toUpperCase());
        return actual == null ? System.getProperty("line.separator", "\n") : actual;
    }

    /**
     * JPA vendor specific properties.
     */
    @Parameter
    private Map<String, String> properties = new HashMap<>();

    public Map<String, String> getProperties() {
        return properties;
    }

    public enum Vendor {
        eclipselink,
        hibernate,
        // datanucleus,
    }

    /**
     * JPA vendor name or class name of vendor's {@link PersistenceProvider} implemention.
     * <p>
     * vendor name is one of
     * <ul>
     * <li>{@code eclipselink}</li>
     * <li>{@code hibernate}</li>
     * </ul>
     * <p>
     * <b>REQUIRED for project without {@code persistence.xml}</b>
     */
    @Parameter
    private Vendor vendor;

    private static final Map<Vendor, Class<? extends PersistenceProvider>> PROVIDER_MAP = new HashMap<>();

    static {
        PROVIDER_MAP.put(Vendor.eclipselink, org.eclipse.persistence.jpa.PersistenceProvider.class);
        PROVIDER_MAP.put(Vendor.hibernate, org.hibernate.jpa.HibernatePersistenceProvider.class);
        // PROVIDER_MAP.put(Vendor.datanucleus, org.datanucleus.api.jpa.PersistenceProviderImpl.class);
    }

    public Vendor getVendor() {
        return vendor;
    }

    public Class<? extends PersistenceProvider> getProviderClass() {
        return PROVIDER_MAP.get(vendor);
    }

    /**
     * list of package name for scan entity classes
     * <p>
     * <b>REQUIRED for project without {@code persistence.xml}</b>
     */
    @Parameter
    private List<String> packageToScan = new ArrayList<>();

    public List<String> getPackageToScan() {
        return packageToScan;
    }

    private static final URL[] EMPTY_URLS = new URL[0];

    private ClassLoader getProjectClassLoader() throws MojoExecutionException {
        try {
            // compiled classes
            List<String> classfiles = this.project.getCompileClasspathElements();
            if (this.scanTestClasses) {
                classfiles.addAll(this.project.getTestClasspathElements());
            }
            // classpath to url
            List<URL> classURLs = new ArrayList<>(classfiles.size());
            for (String classfile : classfiles) {
                classURLs.add(new File(classfile).toURI().toURL());
            }

            // dependency artifacts to url
            ArtifactResolutionRequest sharedreq = new ArtifactResolutionRequest().setResolveRoot(true)
                                                                                 .setResolveTransitively(true)
                                                                                 .setLocalRepository(this.session.getLocalRepository())
                                                                                 .setRemoteRepositories(this.project.getRemoteArtifactRepositories());

            ArtifactRepository repository = this.session.getLocalRepository();
            Set<Artifact> artifacts = this.project.getDependencyArtifacts();
            for (Artifact artifact : artifacts) {
                if (!Artifact.SCOPE_TEST.equalsIgnoreCase(artifact.getScope())) {
                    ArtifactResolutionRequest request = new ArtifactResolutionRequest(sharedreq).setArtifact(artifact);
                    ArtifactResolutionResult result = this.resolver.resolve(request);
                    if (result.isSuccess()) {
                        File file = repository.find(artifact).getFile();
                        if (file != null) {
                            classURLs.add(file.toURI().toURL());
                        }
                    }
                }
            }

            for (URL url : classURLs) {
                this.log.info("  * classpath: " + url);
            }

            return new URLClassLoader(classURLs.toArray(EMPTY_URLS), this.getClass().getClassLoader());
        } catch (Exception e) {
            this.log.error(e);
            throw new MojoExecutionException("Error while creating classloader", e);
        }
    }

    private void generate() throws Exception {
        Map<String, Object> map = JpaSchemaGeneratorUtils.buildProperties(this);
        if (getVendor() == null) {
            // with persistence.xml
            Persistence.generateSchema(this.persistenceUnitName, map);
        } else {
            PersistenceProvider provider = getProviderClass().newInstance();
            List<String> packages = getPackageToScan();
            if (packages.isEmpty()) {
                throw new IllegalArgumentException("packageToScan is required on xml-less mode.");
            }

            DefaultPersistenceUnitManager manager = new DefaultPersistenceUnitManager();
            manager.setDefaultPersistenceUnitName(getPersistenceUnitName());
            manager.setPackagesToScan(packages.toArray(new String[packages.size()]));
            manager.afterPropertiesSet();

            SmartPersistenceUnitInfo info = (SmartPersistenceUnitInfo) manager.obtainDefaultPersistenceUnitInfo();
            info.setPersistenceProviderPackageName(provider.getClass().getName());
            info.getProperties().putAll(map);

            Path persistenceXml = null;
            /* @formatter:off */
            /*
            if (Vendor.datanucleus.equals(getVendor())) {
                // datanucleus must need persistence.xml
                Path path = Paths.get(project.getBuild().getOutputDirectory(), "META-INF");
                persistenceXml = Files.createTempFile(path, "persistence-", ".xml");
                try (BufferedWriter writer = Files.newBufferedWriter(persistenceXml, StandardCharsets.UTF_8)) {
                    PrintWriter out = new PrintWriter(writer);
                    out.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
                    out.println("<persistence version=\"2.1\"");
                    out.println("    xmlns=\"http://xmlns.jcp.org/xml/ns/persistence\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
                    out.println("    xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/persistence http://www.oracle.com/webfolder/technetwork/jsc/xml/ns/persistence/persistence_2_1.xsd\">");
                    out.printf("    <persistence-unit name=\"%s\" transaction-type=\"RESOURCE_LOCAL\">\n",
                               info.getPersistenceUnitName());
                    out.println("        <provider>org.datanucleus.api.jpa.PersistenceProviderImpl</provider>");
                    out.println("        <exclude-unlisted-classes>false</exclude-unlisted-classes>");
                    out.println("    </persistence-unit>");
                    out.println("</persistence>");
                }
                map.put(PropertyNames.PROPERTY_PERSISTENCE_XML_FILENAME, persistenceXml.toAbsolutePath().toString());

                // datanucleus does not support execution order...
                map.remove(PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_SOURCE);
                map.remove(PersistenceUnitProperties.SCHEMA_GENERATION_DROP_SOURCE);
            }
            */
            /* @formatter:on */

            try {
                provider.generateSchema(info, map);
            } finally {
                if (persistenceXml != null) {
                    Files.delete(persistenceXml);
                }
            }
        }
    }

    private static final Pattern CREATE_DROP_PATTERN = Pattern.compile("((?:create|drop|alter)\\s+(?:table|view|sequence))",
                                                                       Pattern.CASE_INSENSITIVE);

    private void postProcess() throws IOException {
        final String linesep = this.getLineSeparator();

        List<File> files = Arrays.asList(this.getCreateOutputFile(), this.getDropOutputFile());
        for (File file : files) {
            // check file exists
            if (file == null || !file.exists()) {
                continue;
            }
            File tempFile = File.createTempFile("script", null, this.getOutputDirectory());
            // read/write with eol
            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 PrintWriter writer = new PrintWriter(tempFile)) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    line = CREATE_DROP_PATTERN.matcher(line).replaceAll(";$1");
                    for (String s : line.split(";")) {
                        if (StringUtils.isBlank(s)) {
                            continue;
                        }
                        s = s.trim();
                        writer.print((this.isFormat() ? format(s) : s).replaceAll("\r\n", linesep));
                        writer.print(";");
                        writer.print(linesep);
                        writer.print(this.isFormat() ? linesep : "");
                    }
                }
                writer.flush();
            } finally {
                file.delete();
                tempFile.renameTo(file);
            }
        }

    }

    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("(?i)^create(\\s+\\S+)?\\s+(?:table|view)"),
            PATTERN_CREATE_INDEX = Pattern.compile("(?i)^create(\\s+\\S+)?\\s+index"),
            PATTERN_ALTER_TABLE = Pattern.compile("(?i)^alter\\s+table");

    String format(String s) {
        final String linesep = this.getLineSeparator();

        s = s.replaceAll("^([^(]+\\()", "$1\r\n\t")
             .replaceAll("\\)[^()]*$", "\r\n$0")
             .replaceAll("((?:[^(),\\s]+|\\S\\([^)]+\\)[^),]*),)\\s*", "$1\r\n\t");
        StringBuilder builder = new StringBuilder();
        boolean completed = true;
        if (PATTERN_CREATE_TABLE.matcher(s).find()) {
            for (String it : s.split("\r\n")) {
                if (it.matches("^\\S.*$")) {
                    if (!completed) {
                        builder.append(linesep);
                        completed = true;
                    }
                    builder.append(it).append(linesep);
                } else if (completed) {
                    if (it.matches("^\\s*[^(]+(?:[^(),\\s]+|\\S\\([^)]+\\)[^),]*),\\s*$")) {
                        builder.append(it).append(linesep);
                    } else {
                        builder.append(it);
                        completed = false;
                    }
                } else {
                    builder.append(it.trim());
                    if (it.matches("[^)]+\\).*$")) {
                        builder.append(linesep);
                        completed = true;
                    }
                }
            }
        } else if (PATTERN_CREATE_INDEX.matcher(s).find()) {
            for (String it : s.replaceAll("(?i)^(create(\\s+\\S+)?\\s+index\\s+\\S+)\\s*", "$1\r\n\t").split("\r\n")) {
                if (builder.length() == 0) {
                    builder.append(it).append(linesep);
                } else if (completed) {
                    if (it.matches("^\\s*[^(]+(?:[^(),\\s]+|\\S\\([^)]+\\)[^),]*),\\s*$")) {
                        builder.append(it).append(linesep);
                    } else {
                        builder.append(it);
                        completed = false;
                    }
                } else {
                    builder.append(it.trim());
                    if (it.matches("[^)]+\\).*$")) {
                        builder.append(linesep);
                        completed = true;
                    }
                }
            }
            String tmp = builder.toString();
            builder.setLength(0);
            builder.append(tmp.replaceAll("(?i)(asc|desc)\\s*(on)", "$2"));
        } else if (PATTERN_ALTER_TABLE.matcher(s).find()) {
            for (String it : s.replaceAll("(?i)^(alter\\s+table\\s+\\S+)\\s*", "$1\r\n\t")
                              .replaceAll("(?i)\\)\\s*(references)", ")\r\n\t$1").split("\r\n")) {
                if (builder.length() == 0) {
                    builder.append(it).append(linesep);
                } else if (completed) {
                    if (it.matches("^\\s*[^(]+(?:[^(),\\s]+|\\S\\([^)]+\\)[^),]*),\\s*$")) {
                        builder.append(it).append(linesep);
                    } else {
                        builder.append(it);
                        completed = false;
                    }
                } else {
                    builder.append(it.trim());
                    if (it.matches("[^)]+\\).*$")) {
                        builder.append(linesep);
                        completed = true;
                    }
                }
            }
        } else {
            builder.append(s.trim()).append(linesep);
        }
        return builder.toString().trim();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.skip) {
            log.info("schema generation is skipped.");
            return;
        }

        if (this.outputDirectory != null && !this.outputDirectory.exists()) {
            this.outputDirectory.mkdirs();
        }

        final ClassLoader classLoader = this.getProjectClassLoader();
        // driver load hack
        // http://stackoverflow.com/questions/288828/how-to-use-a-jdbc-driver-from-an-arbitrary-location
        if (StringUtils.isNotBlank(this.jdbcDriver)) {
            try {
                Driver driver = (Driver) classLoader.loadClass(this.jdbcDriver).newInstance();
                DriverManager.registerDriver(driver);
            } catch (Exception e) {
                throw new MojoExecutionException("Dependency for driver-class " + this.jdbcDriver + " is missing!", e);
            }
        }

        // generate schema
        Thread thread = Thread.currentThread();
        ClassLoader currentClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(classLoader);
            this.generate();
        } catch (Exception e) {
            throw new MojoExecutionException("Error while running", e);
        } finally {
            thread.setContextClassLoader(currentClassLoader);
        }

        // post-process
        try {
            this.postProcess();
        } catch (IOException e) {
            throw new MojoExecutionException("Error while post-processing script file", e);
        }
    }
}
