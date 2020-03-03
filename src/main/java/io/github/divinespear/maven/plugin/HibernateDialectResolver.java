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

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;

public class HibernateDialectResolver {

  private HibernateDialectResolver() {
  }

  public static String resolve(String databaseName, Integer majorVersion, Integer minorVersion) {
    try {
      final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      // create connection mock
      final ConnectionMock mock = new ConnectionMock(databaseName, majorVersion, minorVersion);
      // create DialectResolutionInfo
      final Class<?> driTypeClass = classLoader.loadClass("org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo");
      final Class<?> driClass = classLoader.loadClass("org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter");
      final Object dri = driClass.getDeclaredConstructor(DatabaseMetaData.class).newInstance(mock.getMetaData());
      // get resolver
      final Class<?> resolverClass = classLoader.loadClass("org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver");
      final Object resolver = resolverClass.getDeclaredConstructor().newInstance();
      // resolve dialect
      final Method resolveMethod = resolverClass.getDeclaredMethod("resolveDialect", driTypeClass);
      final Object found = resolveMethod.invoke(resolver, dri);
      return found.getClass().getTypeName();
    } catch (ReflectiveOperationException e) {
      return null;
    }
  }
}
