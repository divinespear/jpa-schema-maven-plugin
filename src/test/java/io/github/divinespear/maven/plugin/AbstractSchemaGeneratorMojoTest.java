package io.github.divinespear.maven.plugin;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

abstract class AbstractSchemaGeneratorMojoTest
        extends AbstractMojoTestCase {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String POM_FILENAME = "pom.xml";

    protected File getPomFile(String path) {
        return this.getPomFile(path, POM_FILENAME);
    }

    protected File getPomFile(String path,
                              String pomFileName) {
        return new File(new File(getBasedir(), path), pomFileName);
    }

    protected void compileJpaModelSources(File pomfile) throws MavenInvocationException {
        Properties properties = new Properties();
        properties.setProperty("plugin.version", System.getProperty("plugin.version"));

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomfile);
        request.setGoals(Collections.singletonList("compile"));
        request.setProperties(properties);

        Invoker invoker = new DefaultInvoker();
        invoker.execute(request);
    }

    protected JpaSchemaGeneratorMojo getGenerateMojo(File pomfile) throws Exception {
        return (JpaSchemaGeneratorMojo) lookupMojo("generate", pomfile);
    }

    protected JpaSchemaGeneratorMojo executeSchemaGeneration(File pomfile) throws Exception {
        String parent = pomfile.getParent().toString();
        // create mojo
        JpaSchemaGeneratorMojo mojo = getGenerateMojo(pomfile);
        assertThat(mojo, notNullValue(JpaSchemaGeneratorMojo.class));
        // configure project mock
        MavenProject projectMock = mock(MavenProject.class);
        doReturn(Arrays.asList(parent + "/target/classes")).when(projectMock)
                                                           .getCompileClasspathElements();
        setVariableValueToObject(mojo, "project", projectMock);
        // configure project session
        setVariableValueToObject(mojo, "session", newMavenSession(projectMock));
        // execute
        mojo.execute();

        return mojo;
    }

    protected String readFileAsText(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        FileReader reader = new FileReader(file);
        try {
            char[] buf = new char[4096];
            while (reader.ready()) {
                int len = reader.read(buf);
                builder.append(buf, 0, len);
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }

}
