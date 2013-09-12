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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Persistence;

import org.apache.commons.lang.NullArgumentException;
import org.eclipse.persistence.config.PersistenceUnitProperties;

class EclipseLinkProviderImpl
        extends BaseProviderImpl {

    public EclipseLinkProviderImpl() {
        super("eclipselink");
    }

    @Override
    protected void doExecute(JpaSchemaGeneratorMojo mojo) {
        boolean useDB = !PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION.equalsIgnoreCase(mojo.getDatabaseAction());
        boolean useScript = !PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION.equalsIgnoreCase(mojo.getScriptAction());

        Map<String, String> map = new HashMap<String, String>();
        // persistence.xml
        map.put(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, mojo.getPersistenceXml());
        // mode
        map.put(PersistenceUnitProperties.SCHEMA_GENERATION_DATABASE_ACTION, mojo.getDatabaseAction().toLowerCase());
        map.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_ACTION, mojo.getScriptAction().toLowerCase());
        // output files
        // database emulation options
        if (useScript) {
            if (mojo.getOutputDirectory() == null) {
                throw new NullArgumentException("outputDirectory is required for script generation.");
            }
            final File c = new File(mojo.getOutputDirectory(), mojo.getCreateOutputFileName());
            final File d = new File(mojo.getOutputDirectory(), mojo.getDropOutputFileName());
            map.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET, c.toURI().toString());
            map.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_DROP_TARGET, d.toURI().toString());

            map.put(PersistenceUnitProperties.SCHEMA_DATABASE_PRODUCT_NAME, mojo.getDatabaseProductName());
            map.put(PersistenceUnitProperties.SCHEMA_DATABASE_MAJOR_VERSION, mojo.getDatabaseMajorVersion());
            map.put(PersistenceUnitProperties.SCHEMA_DATABASE_MINOR_VERSION, mojo.getDatabaseMinorVersion());
        }
        // database options
        if (useDB) {
            map.put(PersistenceUnitProperties.JDBC_DRIVER, mojo.getJdbcDriver());
            map.put(PersistenceUnitProperties.JDBC_URL, mojo.getJdbcUrl());
            map.put(PersistenceUnitProperties.JDBC_USER, mojo.getJdbcUser());
            map.put(PersistenceUnitProperties.JDBC_PASSWORD, mojo.getJdbcPassword());
        }
        // source selection
        map.put(PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_SOURCE, mojo.getCreateSourceMode());
        if (mojo.getCreateSourceFile() == null) {
            if (!PersistenceUnitProperties.SCHEMA_GENERATION_METADATA_SOURCE.equals(mojo.getCreateSourceMode())) {
                throw new IllegalArgumentException("create source file is required for mode "
                                                   + mojo.getCreateSourceMode());
            }
        } else {
            map.put(PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_SCRIPT_SOURCE,
                    mojo.getCreateSourceFile().toURI().toString());
        }
        map.put(PersistenceUnitProperties.SCHEMA_GENERATION_DROP_SOURCE, mojo.getDropSourceMode());
        if (mojo.getDropSourceFile() == null) {
            if (!PersistenceUnitProperties.SCHEMA_GENERATION_METADATA_SOURCE.equals(mojo.getDropSourceMode())) {
                throw new IllegalArgumentException("drop source file is required for mode "
                                                   + mojo.getDropSourceMode());
            }
        } else {
            map.put(PersistenceUnitProperties.SCHEMA_GENERATION_DROP_SCRIPT_SOURCE,
                    mojo.getCreateSourceFile().toURI().toString());
        }

        Persistence.generateSchema(mojo.getPersistenceUnitName(), this.removeNullValuesFromMap(map));
    }
}
