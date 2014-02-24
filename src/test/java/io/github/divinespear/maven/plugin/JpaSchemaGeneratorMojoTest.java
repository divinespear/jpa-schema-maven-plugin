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

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.anyOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JpaSchemaGeneratorMojoTest
        extends AbstractMojoTestCase {

    private static final String POM_FILENAME = "pom.xml";

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private File getPomFile(String path) {
        return this.getPomFile(path, POM_FILENAME);
    }

    private File getPomFile(String path,
                            String pomFileName) {
        return new File(new File(getBasedir(), path), pomFileName);
    }

    private void compileJpaModelSources(File pomfile) throws MavenInvocationException {
        Properties properties = new Properties();
        properties.setProperty("plugin.version", System.getProperty("plugin.version"));

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomfile);
        request.setGoals(Collections.singletonList("compile"));
        request.setProperties(properties);

        Invoker invoker = new DefaultInvoker();
        invoker.execute(request);
    }

    private JpaSchemaGeneratorMojo executeSchemaGeneration(File pomfile) throws Exception {
        String parent = pomfile.getParent().toString();
        // create mojo
        JpaSchemaGeneratorMojo mojo = (JpaSchemaGeneratorMojo) lookupMojo("generate", pomfile);
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

    private List<String> readFileAsList(File file) throws IOException {
        List<String> list = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } finally {
            reader.close();
        }
        return list;
    }

    private String readFileAsText(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\r\n");
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }

    /**
     * Simple schema generation test for script using EclipseLink
     * 
     * @throws Exception
     *             if any exception raises
     */
    @Test
    public void testGenerateScriptUsingEclipseLink() throws Exception {
        final File pomfile = this.getPomFile("target/test-classes/unit/eclipselink-simple-script-test");

        this.compileJpaModelSources(pomfile);
        JpaSchemaGeneratorMojo mojo = this.executeSchemaGeneration(pomfile);

        // file check
        List<String> lines = null;

        File createScriptFile = mojo.getCreateOutputFile();
        assertThat("create script should be generated.", createScriptFile.exists(), is(true));

        lines = this.readFileAsList(createScriptFile);
        assertThat(lines.size(), is(3));
        for (String line : lines) {
            assertThat(line, endsWith(";"));
        }

        File dropScriptFile = mojo.getDropOutputFile();
        assertThat("drop script should be generated.", dropScriptFile.exists(), is(true));

        lines = this.readFileAsList(dropScriptFile);
        assertThat(lines.size(), is(3));
        for (String line : lines) {
            assertThat(line, endsWith(";"));
        }
    }

    /**
     * Simple schema generation test for script using EclipseLink
     * 
     * @throws Exception
     *             if any exception raises
     */
    @Test
    public void testGenerateScriptUsingEclipseLinkFormatted() throws Exception {
        final File pomfile = this.getPomFile("target/test-classes/unit/eclipselink-formatted-script-test");

        this.compileJpaModelSources(pomfile);
        JpaSchemaGeneratorMojo mojo = this.executeSchemaGeneration(pomfile);

        // file check
        List<String> lines = null;

        File createScriptFile = mojo.getCreateOutputFile();
        assertThat("create script should be generated.", createScriptFile.exists(), is(true));

        final String expectCreate = "CREATE TABLE KEY_VALUE_STORE (\r\n"
                                    + "\tSTORED_KEY VARCHAR(128) NOT NULL,\r\n"
                                    + "\tCREATED_AT TIMESTAMP,\r\n"
                                    + "\tSTORED_VALUE VARCHAR(32768),\r\n"
                                    + "\tPRIMARY KEY (STORED_KEY)\r\n"
                                    + ");\r\n"
                                    + "CREATE TABLE MANY_COLUMN_TABLE (\r\n"
                                    + "\tID BIGINT NOT NULL,\r\n"
                                    + "\tCOLUMN00 VARCHAR,\r\n"
                                    + "\tCOLUMN01 VARCHAR,\r\n"
                                    + "\tCOLUMN02 VARCHAR,\r\n"
                                    + "\tCOLUMN03 VARCHAR,\r\n"
                                    + "\tCOLUMN04 VARCHAR,\r\n"
                                    + "\tCOLUMN05 VARCHAR,\r\n"
                                    + "\tCOLUMN06 VARCHAR,\r\n"
                                    + "\tCOLUMN07 VARCHAR,\r\n"
                                    + "\tCOLUMN08 VARCHAR,\r\n"
                                    + "\tCOLUMN09 VARCHAR,\r\n"
                                    + "\tCOLUMN10 VARCHAR,\r\n"
                                    + "\tCOLUMN11 VARCHAR,\r\n"
                                    + "\tCOLUMN12 VARCHAR,\r\n"
                                    + "\tCOLUMN13 VARCHAR,\r\n"
                                    + "\tCOLUMN14 VARCHAR,\r\n"
                                    + "\tCOLUMN15 VARCHAR,\r\n"
                                    + "\tCOLUMN16 VARCHAR,\r\n"
                                    + "\tCOLUMN17 VARCHAR,\r\n"
                                    + "\tCOLUMN18 VARCHAR,\r\n"
                                    + "\tCOLUMN19 VARCHAR,\r\n"
                                    + "\tCOLUMN20 VARCHAR,\r\n"
                                    + "\tCOLUMN21 VARCHAR,\r\n"
                                    + "\tCOLUMN22 VARCHAR,\r\n"
                                    + "\tCOLUMN23 VARCHAR,\r\n"
                                    + "\tCOLUMN24 VARCHAR,\r\n"
                                    + "\tCOLUMN25 VARCHAR,\r\n"
                                    + "\tCOLUMN26 VARCHAR,\r\n"
                                    + "\tCOLUMN27 VARCHAR,\r\n"
                                    + "\tCOLUMN28 VARCHAR,\r\n"
                                    + "\tCOLUMN29 VARCHAR,\r\n"
                                    + "\tPRIMARY KEY (ID)\r\n"
                                    + ");\r\n"
                                    + "CREATE SEQUENCE SEQ_GEN_SEQUENCE INCREMENT BY 50 START WITH 50;\r\n";
        assertThat(this.readFileAsText(createScriptFile), is(expectCreate));

        File dropScriptFile = mojo.getDropOutputFile();
        assertThat("drop script should be generated.", dropScriptFile.exists(), is(true));

        lines = this.readFileAsList(dropScriptFile);
        assertThat(lines.size(), is(3));
        for (String line : lines) {
            assertThat(line, endsWith(";"));
        }
    }

    /**
     * Simple schema generation test for database using EclipseLink
     * 
     * @throws Exception
     *             if any exception raises
     */
    @Test
    public void testGenerateDatabaseUsingEclipseLink() throws Exception {
        // delete database if exists
        File databaseFile = new File(getBasedir(),
                                     "target/test-classes/unit/eclipselink-simple-database-test/target/test.h2.db");
        if (databaseFile.exists()) {
            databaseFile.delete();
        }

        final File pomfile = this.getPomFile("target/test-classes/unit/eclipselink-simple-database-test");

        this.compileJpaModelSources(pomfile);
        JpaSchemaGeneratorMojo mojo = this.executeSchemaGeneration(pomfile);

        // database check
        Connection connection = DriverManager.getConnection(mojo.getJdbcUrl(),
                                                            mojo.getJdbcUser(),
                                                            mojo.getJdbcPassword());
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = null;
            try {
                resultSet = statement.executeQuery("SELECT * FROM KEY_VALUE_STORE");
                try {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    assertThat(metaData.getColumnCount(), is(3));
                    assertThat(metaData.getColumnName(1), anyOf(is("stored_key"), is("STORED_KEY")));
                    assertThat(metaData.getColumnName(2), anyOf(is("created_at"), is("CREATED_AT")));
                    assertThat(metaData.getColumnName(3), anyOf(is("stored_value"), is("STORED_VALUE")));
                } finally {
                    resultSet.close();
                }
                resultSet = statement.executeQuery("SELECT * FROM MANY_COLUMN_TABLE");
                try {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    assertThat(metaData.getColumnCount(), is(31));
                    assertThat(metaData.getColumnName(1), anyOf(is("id"), is("ID")));
                    assertThat(metaData.getColumnName(2), anyOf(is("column00"), is("COLUMN00")));
                } finally {
                    resultSet.close();
                }
            } finally {
                statement.close();
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Simple schema generation test for script using Hibernate
     * 
     * @throws Exception
     */
    @Test
    public void testGenerateScriptUsingHibernate() throws Exception {
        final File pomfile = this.getPomFile("target/test-classes/unit/hibernate-simple-script-test");

        this.compileJpaModelSources(pomfile);
        JpaSchemaGeneratorMojo mojo = this.executeSchemaGeneration(pomfile);

        // file check
        List<String> lines = null;

        File createScriptFile = mojo.getCreateOutputFile();
        assertThat("create script should be generated.", createScriptFile.exists(), is(true));

        lines = this.readFileAsList(createScriptFile);
        assertThat(lines.size(), is(3));
        for (String line : lines) {
            assertThat(line, endsWith(";"));
        }

        File dropScriptFile = mojo.getDropOutputFile();
        assertThat("drop script should be generated.", dropScriptFile.exists(), is(true));

        lines = this.readFileAsList(dropScriptFile);
        assertThat(lines.size(), is(3));
        for (String line : lines) {
            assertThat(line, endsWith(";"));
        }
    }

    /**
     * Simple schema generation test for script using Hibernate
     * 
     * @throws Exception
     */
    @Test
    public void testGenerateScriptUsingHibernateFormatted() throws Exception {
        final File pomfile = this.getPomFile("target/test-classes/unit/hibernate-formatted-script-test");

        this.compileJpaModelSources(pomfile);
        JpaSchemaGeneratorMojo mojo = this.executeSchemaGeneration(pomfile);

        // file check
        List<String> lines = null;

        File createScriptFile = mojo.getCreateOutputFile();
        assertThat("create script should be generated.", createScriptFile.exists(), is(true));

        lines = this.readFileAsList(createScriptFile);
        assertThat(lines.size(), is(41));
        for (String line : lines) {
            assertThat(line, endsWith(";"));
        }

        File dropScriptFile = mojo.getDropOutputFile();
        assertThat("drop script should be generated.", dropScriptFile.exists(), is(true));

        lines = this.readFileAsList(dropScriptFile);
        assertThat(lines.size(), is(3));
        for (String line : lines) {
            assertThat(line, endsWith(";"));
        }
    }

    /**
     * Simple schema generation test for database using Hibernate
     * 
     * @throws Exception
     *             if any exception raises
     */
    @Test
    public void testGenerateDatabaseUsingHibernate() throws Exception {
        // delete database if exists
        File databaseFile = new File(getBasedir(),
                                     "target/test-classes/unit/hibernate-simple-database-test/target/test.h2.db");
        if (databaseFile.exists()) {
            databaseFile.delete();
        }

        final File pomfile = this.getPomFile("target/test-classes/unit/hibernate-simple-database-test");

        this.compileJpaModelSources(pomfile);
        JpaSchemaGeneratorMojo mojo = this.executeSchemaGeneration(pomfile);

        // database check
        Connection connection = DriverManager.getConnection(mojo.getJdbcUrl(),
                                                            mojo.getJdbcUser(),
                                                            mojo.getJdbcPassword());
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = null;
            try {
                resultSet = statement.executeQuery("SELECT * FROM key_value_store");
                try {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    assertThat(metaData.getColumnCount(), is(3));
                    assertThat(metaData.getColumnName(1), anyOf(is("stored_key"), is("STORED_KEY")));
                    assertThat(metaData.getColumnName(2), anyOf(is("created_at"), is("CREATED_AT")));
                    assertThat(metaData.getColumnName(3), anyOf(is("stored_value"), is("STORED_VALUE")));
                } finally {
                    resultSet.close();
                }
                resultSet = statement.executeQuery("SELECT * FROM many_column_table");
                try {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    assertThat(metaData.getColumnCount(), is(31));
                    assertThat(metaData.getColumnName(1), anyOf(is("id"), is("ID")));
                    assertThat(metaData.getColumnName(2), anyOf(is("column00"), is("COLUMN00")));
                } finally {
                    resultSet.close();
                }
            } finally {
                statement.close();
            }
        } finally {
            connection.close();
        }
    }
}
