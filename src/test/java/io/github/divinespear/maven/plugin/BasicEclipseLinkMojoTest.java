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
import static org.hamcrest.Matchers.anyOf;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BasicEclipseLinkMojoTest
        extends AbstractSchemaGeneratorMojoTest {

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

}
