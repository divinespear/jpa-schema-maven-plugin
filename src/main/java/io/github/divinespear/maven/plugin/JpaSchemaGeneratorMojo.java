package io.github.divinespear.maven.plugin;

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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * Generate database schema or DDL scripts.
 * 
 * @author divinespear
 */
@Mojo(name = "generate",
      defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class JpaSchemaGeneratorMojo
        extends AbstractMojo {

    private final Log log = this.getLog();

    @Component
    private MavenSession session;

    @Component
    private MavenProject project;

    @Component
    private MojoExecution mojo;

    // for Maven 3 only
    @Component
    private PluginDescriptor plugin;

    @Component
    private Settings settings;

    /**
     * skip schema generation
     */
    @Parameter(property = "jpa-schema.generate.skip", required = true, defaultValue = "false")
    private boolean skip = false;

    /**
     * scan test classes
     */
    @Parameter(property = "jpa-schema.generate.scan-test-classes", required = true, defaultValue = "false")
    private boolean scanTestClasses = false;

    /**
     * JPA implementation
     * <p>
     * support value is <code>eclipselink</code> or <code>hibernate</code>, as case-insensitive.
     */
    @Parameter(required = true, defaultValue = "eclipselink")
    private String implementation = "eclipselink";

    /**
     * location of <code>persistence.xml</code> file
     * <p>
     * Note for Hibernate: <b>current version (4.3.0.beta3) DOES NOT SUPPORT custom location.</b> so your configuration
     * will be ignored.
     */
    @Parameter(required = true, defaultValue = "META-INF/persistence.xml")
    private String persistenceXml = "META-INF/persistence.xml";

    public String getPersistenceXml() {
        return persistenceXml;
    }

    /**
     * unit name of <code>persistence.xml</code>
     */
    @Parameter(required = true, defaultValue = "default")
    private String persistenceUnitName = "default";

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    /**
     * schema generation action for database
     * <p>
     * support value is <code>none</code>, <code>create</code>, <code>drop</code>, <code>drop-and-create</code>, or
     * <code>create-or-extend-tables</code> (EclipseLink only).
     */
    @Parameter(required = true, defaultValue = "none")
    private String databaseAction = "none";

    public String getDatabaseAction() {
        return databaseAction;
    }

    /**
     * schema generation action for script
     * <p>
     * support value is <code>none</code>, <code>create</code>, <code>drop</code>, or <code>drop-and-create</code>.
     */
    @Parameter(required = true, defaultValue = "none")
    private String scriptAction = "none";

    public String getScriptAction() {
        return scriptAction;
    }

    /**
     * output directory for generated ddl scripts
     * <p>
     * REQUIRED for {@link #scriptAction} is one of <code>create</code>, <code>drop</code>, or
     * <code>drop-and-create</code>.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-schema")
    private File outputDirectory;

    public File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * generated create script name
     * <p>
     * REQUIRED for {@link #scriptAction} is one of <code>create</code>, or <code>drop-and-create</code>.
     */
    @Parameter(defaultValue = "create.sql")
    private String createOutputFileName;

    public String getCreateOutputFileName() {
        return createOutputFileName;
    }

    /**
     * generated drop script name
     * <p>
     * REQUIRED for {@link #scriptAction} is one of <code>drop</code>, or <code>drop-and-create</code>.
     */
    @Parameter(defaultValue = "drop.sql")
    private String dropOutputFileName;

    public String getDropOutputFileName() {
        return dropOutputFileName;
    }

    /**
     * specifies whether the creation of database artifacts is to occur on the basis of the object/relational mapping
     * metadata, DDL script, or a combination of the two.
     * <p>
     * support value is <code>metadata</code>, <code>script</code>, <code>metadata-then-script</code>, or
     * <code>script-then-metadata</code>.
     * 
     * @since JPA 2.1
     */
    @Parameter(defaultValue = "metadata")
    private String createSourceMode = "metadata";

    public String getCreateSourceMode() {
        return createSourceMode;
    }

    /**
     * create source file path.
     * <p>
     * REQUIRED for {@link #createSourceMode} is one of <code>script</code>, <code>metadata-then-script</code>, or
     * <code>script-then-metadata</code>.
     * 
     * @since JPA 2.1
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
     * support value is <code>metadata</code>, <code>script</code>, <code>metadata-then-script</code>, or
     * <code>script-then-metadata</code>.
     * 
     * @since JPA 2.1
     */
    @Parameter(defaultValue = "metadata")
    private String dropSourceMode = "metadata";

    public String getDropSourceMode() {
        return dropSourceMode;
    }

    /**
     * drop source file path.
     * <p>
     * REQUIRED for {@link #dropSourceMode} is one of <code>script</code>, <code>metadata-then-script</code>, or
     * <code>script-then-metadata</code>.
     * 
     * @since JPA 2.1
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
     * Note this is JPA 2.1 feature, you CANNOT use this parameter on JPA 2.0 mode. please use JDBC connection.
     * 
     * @since JPA 2.1
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
     * Note this is JPA 2.1 feature, you CANNOT use this parameter on JPA 2.0 mode. please use JDBC connection.
     * 
     * @since JPA 2.1
     */
    @Parameter
    private Integer databaseMajorVersion;

    public String getDatabaseMajorVersion() {
        return databaseMajorVersion == null ? null : String.valueOf(databaseMajorVersion);
    }

    /**
     * database minor version for emulate database connection. this should useful for script-only action.
     * <ul>
     * <li>specified if sufficient database version information is not included from
     * {@link DatabaseMetaData#getDatabaseProductName()}</li>
     * <li>The value of this property should be the value returned for the target database by
     * {@link DatabaseMetaData#getDatabaseMinorVersion()}</li>
     * </ul>
     * Note this is JPA 2.1 feature, you CANNOT use this parameter on JPA 2.0 mode. please use JDBC connection.
     * 
     * @since JPA 2.1
     */
    @Parameter
    private Integer databaseMinorVersion;

    public String getDatabaseMinorVersion() {
        return databaseMinorVersion == null ? null : String.valueOf(databaseMinorVersion);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.skip) {
            log.info("Generating schema is skipped.");
            return;
        }

        log.info("* JPA Implementation    : " + this.implementation);
        log.info("* Persistence XML       : " + this.persistenceXml);
        log.info("* Persistence Unit Name : " + this.persistenceUnitName);
        log.info("* Action for Database   : " + this.databaseAction);
        log.info("* Action for Script     : " + this.scriptAction);
        log.info("* Output Directory      : " + this.outputDirectory);
        log.info("  - Create Script Name  : " + this.createOutputFileName);
        log.info("  - Drop Script Name    : " + this.dropOutputFileName);
        log.info("");
        log.info("* Options");
        log.info("  - DatabaseProductName  : " + this.databaseProductName);
        log.info("  - DatabaseMajorVersion : " + this.databaseMajorVersion);
        log.info("  - databaseMinorVersion : " + this.databaseMinorVersion);
        log.info("  - JDBC Driver          : " + this.jdbcDriver);
        log.info("  - JDBC URL             : " + this.jdbcUrl);
        log.info("  - JDBC Username        : " + this.jdbcUser);
        log.info("  - JDBC Password        : " + this.jdbcPassword);

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

        final String providerId = this.implementation.toLowerCase().trim();
        final SchemaGeneratorProvider provider = PROVIDER_MAP.get(providerId);
        log.info("* Selected provider     : " + providerId + "(" + provider.getClass().toString() + ")");

        try {
            PROVIDER_MAP.get(providerId).execute(classLoader, this);
        } catch (Exception e) {
            throw new MojoExecutionException("Error while running", e);
        }
    }

    private ClassLoader getProjectClassLoader() throws MojoExecutionException {
        try {
            // compiled classes
            List<String> classfiles = this.project.getCompileClasspathElements();
            if (this.scanTestClasses) {
                classfiles.addAll(this.project.getTestClasspathElements());
            }
            // classpath to url
            List<URL> classURLs = new ArrayList<URL>(classfiles.size());
            log.debug("* Dependencies:");
            for (String classfile : classfiles) {
                log.debug("  - " + classfile);
                classURLs.add(new File(classfile).toURI().toURL());
            }
            return new URLClassLoader(classURLs.toArray(EMPTY_URLS), this.getClass().getClassLoader());
        } catch (Exception e) {
            throw new MojoExecutionException("Error while creating classloader", e);
        }
    }

    private static final URL[] EMPTY_URLS = new URL[0];
    private static final Map<String, SchemaGeneratorProvider> PROVIDER_MAP;
    static {
        Map<String, SchemaGeneratorProvider> map = new HashMap<String, SchemaGeneratorProvider>();
        try {
            registerProvider(map, EclipseLinkProviderImpl.class);
            PROVIDER_MAP = Collections.unmodifiableMap(map);
        } catch (Exception e) {
            throw new RuntimeException("exception while initialize", e);
        }
    }

    private static void registerProvider(Map<String, SchemaGeneratorProvider> map,
                                         Class<? extends SchemaGeneratorProvider> clazz) throws Exception {
        SchemaGeneratorProvider provider = clazz.newInstance();
        map.put(provider.providerName(), provider);
    }
}
