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

        File createScriptFile = mojo.getCreateOutputFile();
        assertThat("create script should be generated.", createScriptFile.exists(), is(true));

        final String expectCreate = "CREATE TABLE KEY_VALUE_STORE (STORED_KEY VARCHAR(128) NOT NULL, CREATED_AT TIMESTAMP, STORED_VALUE VARCHAR(32768), PRIMARY KEY (STORED_KEY));\r\n"
                                    + "CREATE TABLE MANY_COLUMN_TABLE (ID BIGINT NOT NULL, COLUMN00 VARCHAR, COLUMN01 VARCHAR, COLUMN02 VARCHAR, COLUMN03 VARCHAR, COLUMN04 VARCHAR, COLUMN05 VARCHAR, COLUMN06 VARCHAR, COLUMN07 VARCHAR, COLUMN08 VARCHAR, COLUMN09 VARCHAR, COLUMN10 VARCHAR, COLUMN11 VARCHAR, COLUMN12 VARCHAR, COLUMN13 VARCHAR, COLUMN14 VARCHAR, COLUMN15 VARCHAR, COLUMN16 VARCHAR, COLUMN17 VARCHAR, COLUMN18 VARCHAR, COLUMN19 VARCHAR, COLUMN20 VARCHAR, COLUMN21 VARCHAR, COLUMN22 VARCHAR, COLUMN23 VARCHAR, COLUMN24 VARCHAR, COLUMN25 VARCHAR, COLUMN26 VARCHAR, COLUMN27 VARCHAR, COLUMN28 VARCHAR, COLUMN29 VARCHAR, PRIMARY KEY (ID));\r\n"
                                    + "CREATE SEQUENCE SEQ_GEN_SEQUENCE INCREMENT BY 50 START WITH 50;\r\n";
        assertThat(this.readFileAsText(createScriptFile), is(expectCreate));

        File dropScriptFile = mojo.getDropOutputFile();
        assertThat("drop script should be generated.", dropScriptFile.exists(), is(true));

        final String expectDrop = "DROP TABLE KEY_VALUE_STORE;\r\n"
                                  + "DROP TABLE MANY_COLUMN_TABLE;\r\n"
                                  + "DROP SEQUENCE SEQ_GEN_SEQUENCE;\r\n";
        assertThat(this.readFileAsText(dropScriptFile), is(expectDrop));
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

        final String expectDrop = "DROP TABLE KEY_VALUE_STORE;\r\n"
                                  + "DROP TABLE MANY_COLUMN_TABLE;\r\n"
                                  + "DROP SEQUENCE SEQ_GEN_SEQUENCE;\r\n";
        assertThat(this.readFileAsText(dropScriptFile), is(expectDrop));
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

        File createScriptFile = mojo.getCreateOutputFile();
        assertThat("create script should be generated.", createScriptFile.exists(), is(true));

        final String expectCreate = "create table key_value_store (stored_key varchar(128) not null, created_at timestamp, stored_value varchar(32768), primary key (stored_key));\r\n"
                                    + "create table many_column_table (id bigint not null, column00 varchar(255), column01 varchar(255), column02 varchar(255), column03 varchar(255), column04 varchar(255), column05 varchar(255), column06 varchar(255), column07 varchar(255), column08 varchar(255), column09 varchar(255), column10 varchar(255), column11 varchar(255), column12 varchar(255), column13 varchar(255), column14 varchar(255), column15 varchar(255), column16 varchar(255), column17 varchar(255), column18 varchar(255), column19 varchar(255), column20 varchar(255), column21 varchar(255), column22 varchar(255), column23 varchar(255), column24 varchar(255), column25 varchar(255), column26 varchar(255), column27 varchar(255), column28 varchar(255), column29 varchar(255), primary key (id));\r\n"
                                    + "create sequence hibernate_sequence;\r\n";
        assertThat(this.readFileAsText(createScriptFile), is(expectCreate));

        File dropScriptFile = mojo.getDropOutputFile();
        assertThat("drop script should be generated.", dropScriptFile.exists(), is(true));

        final String expectDrop = "drop table key_value_store if exists;\r\n"
                                  + "drop table many_column_table if exists;\r\n"
                                  + "drop sequence hibernate_sequence;\r\n";
        assertThat(this.readFileAsText(dropScriptFile), is(expectDrop));
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

        File createScriptFile = mojo.getCreateOutputFile();
        assertThat("create script should be generated.", createScriptFile.exists(), is(true));

        final String expectCreate = "create table key_value_store (\r\n"
                                    + "\tstored_key varchar(128) not null,\r\n"
                                    + "\tcreated_at timestamp,\r\n"
                                    + "\tstored_value varchar(32768),\r\n"
                                    + "\tprimary key (stored_key)\r\n"
                                    + ");\r\n"
                                    + "create table many_column_table (\r\n"
                                    + "\tid bigint not null,\r\n"
                                    + "\tcolumn00 varchar(255),\r\n"
                                    + "\tcolumn01 varchar(255),\r\n"
                                    + "\tcolumn02 varchar(255),\r\n"
                                    + "\tcolumn03 varchar(255),\r\n"
                                    + "\tcolumn04 varchar(255),\r\n"
                                    + "\tcolumn05 varchar(255),\r\n"
                                    + "\tcolumn06 varchar(255),\r\n"
                                    + "\tcolumn07 varchar(255),\r\n"
                                    + "\tcolumn08 varchar(255),\r\n"
                                    + "\tcolumn09 varchar(255),\r\n"
                                    + "\tcolumn10 varchar(255),\r\n"
                                    + "\tcolumn11 varchar(255),\r\n"
                                    + "\tcolumn12 varchar(255),\r\n"
                                    + "\tcolumn13 varchar(255),\r\n"
                                    + "\tcolumn14 varchar(255),\r\n"
                                    + "\tcolumn15 varchar(255),\r\n"
                                    + "\tcolumn16 varchar(255),\r\n"
                                    + "\tcolumn17 varchar(255),\r\n"
                                    + "\tcolumn18 varchar(255),\r\n"
                                    + "\tcolumn19 varchar(255),\r\n"
                                    + "\tcolumn20 varchar(255),\r\n"
                                    + "\tcolumn21 varchar(255),\r\n"
                                    + "\tcolumn22 varchar(255),\r\n"
                                    + "\tcolumn23 varchar(255),\r\n"
                                    + "\tcolumn24 varchar(255),\r\n"
                                    + "\tcolumn25 varchar(255),\r\n"
                                    + "\tcolumn26 varchar(255),\r\n"
                                    + "\tcolumn27 varchar(255),\r\n"
                                    + "\tcolumn28 varchar(255),\r\n"
                                    + "\tcolumn29 varchar(255),\r\n"
                                    + "\tprimary key (id)\r\n"
                                    + ");\r\n"
                                    + "create sequence hibernate_sequence;\r\n";
        assertThat(this.readFileAsText(createScriptFile), is(expectCreate));

        File dropScriptFile = mojo.getDropOutputFile();
        assertThat("drop script should be generated.", dropScriptFile.exists(), is(true));

        final String expectDrop = "drop table key_value_store if exists;\r\n"
                                  + "drop table many_column_table if exists;\r\n"
                                  + "drop sequence hibernate_sequence;\r\n";
        assertThat(this.readFileAsText(dropScriptFile), is(expectDrop));
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
