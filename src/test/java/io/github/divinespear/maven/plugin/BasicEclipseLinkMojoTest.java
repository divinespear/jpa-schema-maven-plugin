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

        final String expectCreate = readResourceAsString("/unit/eclipselink-simple-script-test/expected-create.txt");
        assertThat(this.readFileAsString(createScriptFile), is(expectCreate));

        File dropScriptFile = mojo.getDropOutputFile();
        assertThat("drop script should be generated.", dropScriptFile.exists(), is(true));

        final String expectDrop = readResourceAsString("/unit/eclipselink-simple-script-test/expected-drop.txt");
        assertThat(this.readFileAsString(dropScriptFile), is(expectDrop));
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

        final String expectCreate = "CREATE TABLE KEY_VALUE_STORE (" + LINE_SEPARATOR
                                    + "\tSTORED_KEY VARCHAR(128) NOT NULL," + LINE_SEPARATOR
                                    + "\tCREATED_AT TIMESTAMP," + LINE_SEPARATOR
                                    + "\tSTORED_VALUE VARCHAR(32768)," + LINE_SEPARATOR
                                    + "\tPRIMARY KEY (STORED_KEY)" + LINE_SEPARATOR
                                    + ");" + LINE_SEPARATOR
                                    + "CREATE TABLE MANY_COLUMN_TABLE (" + LINE_SEPARATOR
                                    + "\tID BIGINT NOT NULL," + LINE_SEPARATOR
                                    + "\tCOLUMN00 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN01 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN02 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN03 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN04 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN05 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN06 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN07 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN08 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN09 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN10 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN11 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN12 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN13 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN14 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN15 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN16 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN17 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN18 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN19 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN20 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN21 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN22 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN23 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN24 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN25 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN26 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN27 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN28 VARCHAR," + LINE_SEPARATOR
                                    + "\tCOLUMN29 VARCHAR," + LINE_SEPARATOR
                                    + "\tPRIMARY KEY (ID)" + LINE_SEPARATOR
                                    + ");" + LINE_SEPARATOR
                                    + "CREATE SEQUENCE SEQ_GEN_SEQUENCE INCREMENT BY 50 START WITH 50;"
                                    + LINE_SEPARATOR;
        assertThat(this.readFileAsString(createScriptFile), is(expectCreate));

        File dropScriptFile = mojo.getDropOutputFile();
        assertThat("drop script should be generated.", dropScriptFile.exists(), is(true));

        final String expectDrop = "DROP TABLE KEY_VALUE_STORE;"
                                  + LINE_SEPARATOR
                                  + "DROP TABLE MANY_COLUMN_TABLE;"
                                  + LINE_SEPARATOR
                                  + "DROP SEQUENCE SEQ_GEN_SEQUENCE;"
                                  + LINE_SEPARATOR;
        assertThat(this.readFileAsString(dropScriptFile), is(expectDrop));
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
