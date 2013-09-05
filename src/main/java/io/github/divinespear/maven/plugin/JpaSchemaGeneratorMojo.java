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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Generate database schema or DDL scripts.
 * 
 * @author divinespear
 */
@Mojo(name = "generate",
      defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class JpaSchemaGeneratorMojo
        extends AbstractMojo {

    /**
     * JPA version
     * <p>
     * support value is <code>2.0</code> or <code>2.1</code>.
     * <p>
     * Note for JPA 2.1, version of implementation must be:
     * <ul>
     * <li>EclipseLink: 2.5.0 or newer</li>
     * <li>Hibernate: 4.3.0 or newer</li>
     * </ul>
     */
    @Parameter(defaultValue = "2.1")
    private String jpaVersion;

    /**
     * JPA implementation
     * <p>
     * support value is <code>eclipselink</code> or <code>hibernate</code>, as case-insensitive.
     */
    @Parameter(defaultValue = "eclipselink")
    private String implementation;

    /**
     * location of <code>persistence.xml</code> file
     */
    @Parameter(defaultValue = "META-INF/persistence.xml")
    private String persistenceXml;

    /**
     * unit name of <code>persistence.xml</code>
     */
    @Parameter(defaultValue = "default")
    private String persistenceUnitName;

    /**
     * schema generation target
     * <p>
     * support value is <code>database</code>, <code>script</code>, or <code>both</code>.
     */
    @Parameter(defaultValue = "both")
    private String target;

    /**
     * schema generation mode
     * <p>
     * support value is <code>create</code>, <code>drop</code>, or <code>drop-and-create</code>.
     */
    @Parameter(defaultValue = "drop-and-create")
    private String mode;

    /**
     * output directory for generated ddl scripts
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-schema")
    private File outputDirectory;

    /**
     * generated create script name
     */
    @Parameter(defaultValue = "create.sql")
    private String createOutputFileName;

    /**
     * generated drop script name
     */
    @Parameter(defaultValue = "drop.sql")
    private String dropOutputFileName;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // TODO Auto-generated method stub
    }

}
