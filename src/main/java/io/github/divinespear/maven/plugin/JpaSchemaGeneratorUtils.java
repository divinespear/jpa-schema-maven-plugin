package io.github.divinespear.maven.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

import static org.hibernate.cfg.AvailableSettings.JAKARTA_HBM2DDL_CONNECTION;

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

final class JpaSchemaGeneratorUtils {

    private JpaSchemaGeneratorUtils() {
    }

    private static boolean isDatabaseTarget(JpaSchemaGeneratorMojo mojo) {
        return !PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION.equalsIgnoreCase(mojo.getDatabaseAction());
    }

    private static boolean isScriptTarget(JpaSchemaGeneratorMojo mojo) {
        return !PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION.equalsIgnoreCase(mojo.getScriptAction());
    }

    public static Map<String, Object> buildProperties(JpaSchemaGeneratorMojo mojo) {
        Map<String, Object> map = new HashMap<>();
        Map<String, String> properties = mojo.getProperties();

        /*
         * Common JPA options
         */
        // mode
        map.put(PersistenceUnitProperties.SCHEMA_GENERATION_DATABASE_ACTION, mojo.getDatabaseAction().toLowerCase());
        map.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_ACTION, mojo.getScriptAction().toLowerCase());
        // output files
        if (isScriptTarget(mojo)) {
            if (mojo.getOutputDirectory() == null) {
                throw new NullArgumentException("outputDirectory is required for script generation.");
            }
            map.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET,
                    mojo.getCreateOutputFile().toURI().toString());
            map.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_DROP_TARGET,
                    mojo.getDropOutputFile().toURI().toString());

        }
        // database emulation options
        map.put(PersistenceUnitProperties.SCHEMA_DATABASE_PRODUCT_NAME, mojo.getDatabaseProductName());
        map.put(PersistenceUnitProperties.SCHEMA_DATABASE_MAJOR_VERSION,
                mojo.getDatabaseMajorVersion() == null ? null : String.valueOf(mojo.getDatabaseMajorVersion()));
        map.put(PersistenceUnitProperties.SCHEMA_DATABASE_MINOR_VERSION,
                mojo.getDatabaseMinorVersion() == null ? null : String.valueOf(mojo.getDatabaseMinorVersion()));
        // database options
        map.put(PersistenceUnitProperties.JDBC_DRIVER, mojo.getJdbcDriver());
        map.put(PersistenceUnitProperties.JDBC_URL, mojo.getJdbcUrl());
        map.put(PersistenceUnitProperties.JDBC_USER, mojo.getJdbcUser());
        map.put(PersistenceUnitProperties.JDBC_PASSWORD, mojo.getJdbcPassword());
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
                throw new IllegalArgumentException("drop source file is required for mode " + mojo.getDropSourceMode());
            }
        } else {
            map.put(PersistenceUnitProperties.SCHEMA_GENERATION_DROP_SCRIPT_SOURCE,
                    mojo.getDropSourceFile().toURI().toString());
        }

        /*
         * EclipseLink specific
         */
        // persistence.xml
        map.put(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, mojo.getPersistenceXml());
        // disable weaving
        map.put(PersistenceUnitProperties.WEAVING, "false");

        /*
         * Hibernate specific
         */
        // auto-detect
        map.put(AvailableSettings.SCANNER_DISCOVERY, "class,hbm");
        // dialect (without jdbc connection)
        String dialect = properties.get(AvailableSettings.DIALECT);
        if (StringUtils.isEmpty(dialect) && StringUtils.isEmpty(mojo.getJdbcUrl())) {
            final String productName = mojo.getDatabaseProductName();
            final int minorVersion = mojo.getDatabaseMinorVersion() == null ? 0 : mojo.getDatabaseMinorVersion();
            final int majorVersion = mojo.getDatabaseMajorVersion() == null ? 0 : mojo.getDatabaseMajorVersion();
            DialectResolutionInfo info = new DialectResolutionInfo() {
                @Override
                public String getDriverName() {
                    return null;
                }

                @Override
                public int getDriverMinorVersion() {
                    return 0;
                }

                @Override
                public String getSQLKeywords() {
                    return "ABORT,DECIMAL,INTERVAL,PRESERVE,ALL,DECODE,INTO,PRIMARY,ALLOCATE,DEFAULT,LEADING,RESET,ANALYSE,DESC,LEFT,REUSE,ANALYZE,DISTINCT,LIKE,RIGHT,AND,DISTRIBUTE,LIMIT,ROWS,ANY,DO,LOAD,SELECT,AS,ELSE,LOCAL,SESSION_USER,ASC,END,LOCK,SETOF,BETWEEN,EXCEPT,MINUS,SHOW,BINARY,EXCLUDE,MOVE,SOME,BIT,EXISTS,NATURAL,TABLE,BOTH,EXPLAIN,NCHAR,THEN,CASE,EXPRESS,NEW,TIES,CAST,EXTEND,NOT,TIME,CHAR,EXTERNAL,NOTNULL,TIMESTAMP,CHARACTER,EXTRACT,NULL,TO,CHECK,FALSE,NULLS,TRAILING,CLUSTER,FIRST,NUMERIC,TRANSACTION,COALESCE,FLOAT,NVL,TRIGGER,COLLATE,FOLLOWING,NVL2,TRIM,COLLATION,FOR,OFF,TRUE,COLUMN,FOREIGN,OFFSET,UNBOUNDED,CONSTRAINT,FROM,OLD,UNION,COPY,FULL,ON,UNIQUE,CROSS,FUNCTION,ONLINE,USER,CURRENT,GENSTATS,ONLY,USING,CURRENT_CATALOG,GLOBAL,OR,VACUUM,CURRENT_DATE,GROUP,ORDER,VARCHAR,CURRENT_DB,HAVING,OTHERS,VERBOSE,CURRENT_SCHEMA,IDENTIFIER_CASE,OUT,VERSION,CURRENT_SID,ILIKE,OUTER,VIEW,CURRENT_TIME,IN,OVER,WHEN,CURRENT_TIMESTAMP,INDEX,OVERLAPS,WHERE,CURRENT_USER,INITIALLY,PARTITION,WITH,CURRENT_USERID,INNER,POSITION,WRITE,CURRENT_USEROID,INOUT,PRECEDING,RESET,DEALLOCATE,INTERSECT,PRECISION,REUSE,DEC";
                }

                @Override
                public int getDriverMajorVersion() {
                    return 0;
                }

                @Override
                public String getDatabaseName() {
                    return productName;
                }

                @Override
                public String getDatabaseVersion() {
                    return String.format("%d.%d", getDatabaseMajorVersion(), getDatabaseMinorVersion());
                }

                @Override
                public int getDatabaseMinorVersion() {
                    return minorVersion;
                }

                @Override
                public int getDatabaseMajorVersion() {
                    return majorVersion;
                }
            };
            Dialect detectedDialect = new StandardDialectResolver().resolveDialect(info);
            dialect = detectedDialect.getClass().getName();
        }
        if (dialect != null) {
            properties.remove(AvailableSettings.DIALECT);
            map.put(AvailableSettings.DIALECT, dialect);
        }

        if (!isDatabaseTarget(mojo) && StringUtils.isEmpty(mojo.getJdbcUrl())) {
            map.put(JAKARTA_HBM2DDL_CONNECTION,
                    new ConnectionMock(mojo.getDatabaseProductName(),
                                       mojo.getDatabaseMajorVersion(),
                                       mojo.getDatabaseMinorVersion()));
        }

        map.putAll(mojo.getProperties());

        /* force override JTA to RESOURCE_LOCAL */
        map.put(PersistenceUnitProperties.TRANSACTION_TYPE, "RESOURCE_LOCAL");
        map.put(PersistenceUnitProperties.JTA_DATASOURCE, null);
        map.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, null);
        map.put(PersistenceUnitProperties.VALIDATION_MODE, "NONE");

        // normalize - remove null
        List<String> keys = new ArrayList<>(map.keySet());
        for (String key : keys) {
            if (map.get(key) == null) {
                map.remove(key);
            }
        }

        return map;
    }
}
