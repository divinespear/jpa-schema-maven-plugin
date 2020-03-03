package io.github.divinespear.maven.plugin;

import java.util.*;

import org.apache.commons.lang.NullArgumentException;
import org.codehaus.plexus.util.StringUtils;

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

  private static boolean isNotNone(String value) {
    return !Constants.JAVAX_SCHEMA_GENERATION_NONE_ACTION.equalsIgnoreCase(value);
  }

  private static boolean isDatabaseTarget(JpaSchemaGeneratorMojo mojo) {
    return isNotNone(mojo.getDatabaseAction());
  }

  private static boolean isScriptTarget(JpaSchemaGeneratorMojo mojo) {
    return isNotNone(mojo.getScriptAction());
  }

  public static Map<String, Object> buildProperties(JpaSchemaGeneratorMojo mojo) {
    Map<String, Object> map = new HashMap<>();
    Map<String, String> properties = mojo.getProperties();

    /*
     * Common JPA options
     */
    // mode
    map.put(Constants.JAVAX_SCHEMA_GENERATION_DATABASE_ACTION, mojo.getDatabaseAction().toLowerCase());
    map.put(Constants.JAVAX_SCHEMA_GENERATION_SCRIPTS_ACTION, mojo.getScriptAction().toLowerCase());
    // output files
    if (isScriptTarget(mojo)) {
      if (mojo.getOutputDirectory() == null) {
        throw new NullArgumentException("outputDirectory is required for script generation.");
      }
      map.put(Constants.JAVAX_SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET, mojo.getCreateOutputFile().toURI().toString());
      map.put(Constants.JAVAX_SCHEMA_GENERATION_SCRIPTS_DROP_TARGET, mojo.getDropOutputFile().toURI().toString());
    }
    // database emulation options
    map.put(Constants.JAVAX_SCHEMA_DATABASE_PRODUCT_NAME, mojo.getDatabaseProductName());
    map.put(Constants.JAVAX_SCHEMA_DATABASE_MAJOR_VERSION,
            Optional.ofNullable(mojo.getDatabaseMajorVersion()).map(String::valueOf));
    map.put(Constants.JAVAX_SCHEMA_DATABASE_MINOR_VERSION,
            Optional.ofNullable(mojo.getDatabaseMinorVersion()).map(String::valueOf));
    // database options
    map.put(Constants.JAVAX_JDBC_DRIVER, mojo.getJdbcDriver());
    map.put(Constants.JAVAX_JDBC_URL, mojo.getJdbcUrl());
    map.put(Constants.JAVAX_JDBC_USER, mojo.getJdbcUser());
    map.put(Constants.JAVAX_JDBC_PASSWORD, mojo.getJdbcPassword());
    // source selection
    map.put(Constants.JAVAX_SCHEMA_GENERATION_CREATE_SOURCE, mojo.getCreateSourceMode());
    if (mojo.getCreateSourceFile() == null) {
      if (!Constants.JAVAX_SCHEMA_GENERATION_METADATA_SOURCE.equals(mojo.getCreateSourceMode())) {
        throw new IllegalArgumentException("create source file is required for mode " + mojo.getCreateSourceMode());
      }
    } else {
      map.put(Constants.JAVAX_SCHEMA_GENERATION_CREATE_SCRIPT_SOURCE, mojo.getCreateSourceFile().toURI().toString());
    }
    map.put(Constants.JAVAX_SCHEMA_GENERATION_DROP_SOURCE, mojo.getDropSourceMode());
    if (mojo.getDropSourceFile() == null) {
      if (!Constants.JAVAX_SCHEMA_GENERATION_METADATA_SOURCE.equals(mojo.getDropSourceMode())) {
        throw new IllegalArgumentException("drop source file is required for mode " + mojo.getDropSourceMode());
      }
    } else {
      map.put(Constants.JAVAX_SCHEMA_GENERATION_DROP_SCRIPT_SOURCE, mojo.getDropSourceFile().toURI().toString());
    }

    /*
     * EclipseLink specific
     */
    // persistence.xml
    map.put(Constants.ECLIPSELINK_PERSISTENCE_XML, mojo.getPersistenceXml());
    // disable weaving
    map.put(Constants.ECLIPSELINK_WEAVING, "false");

    /*
     * Hibernate specific
     */
    final String productName = mojo.getDatabaseProductName();
    final int minorVersion = Optional.ofNullable(mojo.getDatabaseMinorVersion()).orElse(0);
    final int majorVersion = Optional.ofNullable(mojo.getDatabaseMajorVersion()).orElse(0);
    // auto-detect
    map.put(Constants.HIBERNATE_AUTODETECTION, "class,hbm");
    // dialect (without jdbc connection)
    String dialect = properties.get(Constants.HIBERNATE_DIALECT);
    if (StringUtils.isEmpty(dialect) && StringUtils.isEmpty(mojo.getJdbcUrl())) {
      dialect = HibernateDialectResolver.resolve(productName, majorVersion, minorVersion);
    }
    if (dialect != null) {
      properties.remove(Constants.HIBERNATE_DIALECT);
      map.put(Constants.HIBERNATE_DIALECT, dialect);
    }

    if (!isDatabaseTarget(mojo) && StringUtils.isEmpty(mojo.getJdbcUrl())) {
      map.put(Constants.JAVAX_SCHEMA_GEN_CONNECTION, new ConnectionMock(productName, majorVersion, minorVersion));
    }

    map.putAll(mojo.getProperties());

    /* force override JTA to RESOURCE_LOCAL */
    map.put(Constants.JAVAX_TRANSACTION_TYPE, Constants.JAVAX_TRANSACTION_TYPE_RESOURCE_LOCAL);
    map.put(Constants.JAVAX_JTA_DATASOURCE, null);
    map.put(Constants.JAVAX_NON_JTA_DATASOURCE, null);
    map.put(Constants.JAVAX_VALIDATION_MODE, "NONE");

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
