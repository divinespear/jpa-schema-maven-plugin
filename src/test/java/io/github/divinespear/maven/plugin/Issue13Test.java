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
}
