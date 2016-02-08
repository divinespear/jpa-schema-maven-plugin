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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Issue13Test
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

    @Test
    public void testShouldDefaultLineSeparatorIsSameAsSystem() throws Exception {
        final File pomfile = this.getPomFile("target/test-classes/unit/issue-13", "pom-default.xml");
        final JpaSchemaGeneratorMojo mojo = this.getGenerateMojo(pomfile);

        assertThat(mojo.getLineSeparator(), is(LINE_SEPARATOR));
    }

    @Test
    public void testShouldLoadLineSeparator() throws Exception {
        final File pomfile = this.getPomFile("target/test-classes/unit/issue-13", "pom-crlf.xml");
        final JpaSchemaGeneratorMojo mojo = this.getGenerateMojo(pomfile);

        assertThat(mojo.getLineSeparator(), is("\r\n"));
    }

    /**
     * Simple schema generation test for script using EclipseLink
     * 
     * @throws Exception
     *             if any exception raises
     */
    @Test
    public void testShouldGenerateScriptWithLineSeparator() throws Exception {
        final File pomfile = this.getPomFile("target/test-classes/unit/issue-13", "pom-crlf.xml");

        this.compileJpaModelSources(pomfile);
        JpaSchemaGeneratorMojo mojo = this.executeSchemaGeneration(pomfile);

        File createScriptFile = mojo.getCreateOutputFile();
        assertThat("create script should be generated.", createScriptFile.exists(), is(true));

        final String expectCreate = "CREATE TABLE KEY_VALUE_STORE (STORED_KEY VARCHAR(128) NOT NULL, CREATED_AT TIMESTAMP, STORED_VALUE VARCHAR(32768), PRIMARY KEY (STORED_KEY));"
                                    + "\r\n"
                                    + "CREATE TABLE MANY_COLUMN_TABLE (ID BIGINT NOT NULL, COLUMN00 VARCHAR, COLUMN01 VARCHAR, COLUMN02 VARCHAR, COLUMN03 VARCHAR, COLUMN04 VARCHAR, COLUMN05 VARCHAR, COLUMN06 VARCHAR, COLUMN07 VARCHAR, COLUMN08 VARCHAR, COLUMN09 VARCHAR, COLUMN10 VARCHAR, COLUMN11 VARCHAR, COLUMN12 VARCHAR, COLUMN13 VARCHAR, COLUMN14 VARCHAR, COLUMN15 VARCHAR, COLUMN16 VARCHAR, COLUMN17 VARCHAR, COLUMN18 VARCHAR, COLUMN19 VARCHAR, COLUMN20 VARCHAR, COLUMN21 VARCHAR, COLUMN22 VARCHAR, COLUMN23 VARCHAR, COLUMN24 VARCHAR, COLUMN25 VARCHAR, COLUMN26 VARCHAR, COLUMN27 VARCHAR, COLUMN28 VARCHAR, COLUMN29 VARCHAR, PRIMARY KEY (ID));"
                                    + "\r\n"
                                    + "CREATE SEQUENCE SEQ_GEN_SEQUENCE INCREMENT BY 50 START WITH 50;"
                                    + "\r\n";
        assertThat(this.readFileAsString(createScriptFile), is(expectCreate));

        File dropScriptFile = mojo.getDropOutputFile();
        assertThat("drop script should be generated.", dropScriptFile.exists(), is(true));

        final String expectDrop = "DROP TABLE KEY_VALUE_STORE;" + "\r\n"
                                  + "DROP TABLE MANY_COLUMN_TABLE;" + "\r\n"
                                  + "DROP SEQUENCE SEQ_GEN_SEQUENCE;" + "\r\n";
        assertThat(this.readFileAsString(dropScriptFile), is(expectDrop));
    }
}
