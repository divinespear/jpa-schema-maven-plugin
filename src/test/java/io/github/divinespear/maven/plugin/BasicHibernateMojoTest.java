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

public class BasicHibernateMojoTest
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
