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
package io.github.divinespear.maven.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {

  private Constants() {
  }

  /* jpa common */
  public static final String JAVAX_SCHEMA_GENERATION_NONE_ACTION = "none";
  public static final String JAVAX_SCHEMA_GENERATION_DATABASE_ACTION = "javax.persistence.schema-generation.database.action";
  public static final String JAVAX_SCHEMA_GENERATION_SCRIPTS_ACTION = "javax.persistence.schema-generation.scripts.action";
  public static final String JAVAX_SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET = "javax.persistence.schema-generation.scripts.create-target";
  public static final String JAVAX_SCHEMA_GENERATION_SCRIPTS_DROP_TARGET = "javax.persistence.schema-generation.scripts.drop-target";
  public static final String JAVAX_SCHEMA_DATABASE_PRODUCT_NAME = "javax.persistence.database-product-name";
  public static final String JAVAX_SCHEMA_DATABASE_MAJOR_VERSION = "javax.persistence.database-major-version";
  public static final String JAVAX_SCHEMA_DATABASE_MINOR_VERSION = "javax.persistence.database-minor-version";
  public static final String JAVAX_JDBC_DRIVER = "javax.persistence.jdbc.driver";
  public static final String JAVAX_JDBC_URL = "javax.persistence.jdbc.url";
  public static final String JAVAX_JDBC_USER = "javax.persistence.jdbc.user";
  public static final String JAVAX_JDBC_PASSWORD = "javax.persistence.jdbc.password";
  public static final String JAVAX_SCHEMA_GENERATION_METADATA_SOURCE = "metadata";
  public static final String JAVAX_SCHEMA_GENERATION_CREATE_SOURCE = "javax.persistence.schema-generation.create-source";
  public static final String JAVAX_SCHEMA_GENERATION_DROP_SOURCE = "javax.persistence.schema-generation.drop-source";
  public static final String JAVAX_SCHEMA_GENERATION_CREATE_SCRIPT_SOURCE = "javax.persistence.schema-generation.create-script-source";
  public static final String JAVAX_SCHEMA_GENERATION_DROP_SCRIPT_SOURCE = "javax.persistence.schema-generation.drop-script-source";
  public static final String JAVAX_SCHEMA_GEN_CONNECTION = "javax.persistence.schema-generation-connection";
  public static final String JAVAX_VALIDATION_MODE = "javax.persistence.validation.mode";
  public static final String JAVAX_TRANSACTION_TYPE = "javax.persistence.transactionType";
  public static final String JAVAX_JTA_DATASOURCE = "javax.persistence.jtaDataSource";
  public static final String JAVAX_NON_JTA_DATASOURCE = "javax.persistence.nonJtaDataSource";

  /* eclipse specific */
  public static final String ECLIPSELINK_PERSISTENCE_XML = "eclipselink.persistencexml";
  public static final String ECLIPSELINK_WEAVING = "eclipselink.weaving";

  /* hibernate specific */
  public static final String HIBERNATE_AUTODETECTION = "hibernate.archive.autodetection";
  public static final String HIBERNATE_DIALECT = "hibernate.dialect";

  /* values */
  public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "default";
  public static final String JAVAX_TRANSACTION_TYPE_RESOURCE_LOCAL = "RESOURCE_LOCAL";

  /* predefined providers */
  public static final Map<String, String> PERSISTENCE_PROVIDER_MAP;

  /* line separator */
  public static final Map<String, String> LINE_SEPARATOR_MAP;

  static {
    final Map<String, String> providers = new HashMap<>();
    providers.put("eclipselink", "org.eclipse.persistence.jpa.PersistenceProvider");
    providers.put("hibernate", "org.hibernate.jpa.HibernatePersistenceProvider");
    providers.put("hibernate+spring", "org.springframework.orm.jpa.vendor.SpringHibernateJpaPersistenceProvider");
    PERSISTENCE_PROVIDER_MAP = Collections.unmodifiableMap(providers);

    final Map<String, String> separators = new HashMap<>();
    separators.put("CRLF", "\\r\\n");
    separators.put("LF", "\\n");
    separators.put("CR", "\\r");
    LINE_SEPARATOR_MAP = Collections.unmodifiableMap(separators);
  }
}

