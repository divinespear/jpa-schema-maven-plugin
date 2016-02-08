# jpa-schema-maven-plugin

[![Build Status](https://secure.travis-ci.org/divinespear/jpa-schema-maven-plugin.png)](http://travis-ci.org/divinespear/jpa-schema-maven-plugin)

Maven plugin for generate schema or DDL scripts from JPA entities using [JPA 2.1](http://jcp.org/en/jsr/detail?id=338) schema generator.
for Gradle, see [Gradle Plugin](https://github.com/divinespear/jpa-schema-gradle-plugin).

Currently support [EclipseLink](http://www.eclipse.org/eclipselink) (Reference Implementation) and [Hibernate](http://hibernate.org).

## Before Announce...

READ MY LIP; **JPA DDL GENERATOR IS NOT SILVER BULLET**

Sometimes (*most times* exactly :P) JPA will generate weird scripts so you **SHOULD** modify them properly.


## Announce 0.2

Finally, I got some times, and 0.2 is here.

* Support generate without `persistence.xml` (like spring-data, spring-boot, ...) related [#14](//github.com/divinespear/jpa-schema-gradle-plugin/issues/14)
* Changed default version of implementations.
    * Eclipselink: `2.6.1`
    * Hibernate: `5.0.7.Final`
* Added `properties` property.
* Removed properties `namingStrategy` and `dialect` cause Hibernate 4.x to 5.x is cataclysm. please use `properties` instead.

On 0.2.x, plugin required

* [Java 1.7 or above](http://www.oracle.com/technetwork/java/javase/eol-135779.html), and
* Maven 3.3.x. (developed on 3.3.1)

Unfortunately, DataNucleus does not support on maven plugin... but [I will find a way. I always have.](http://www.imdb.com/title/tt0816692/)

## How-to Use
Define plugin at your maven `pom.xml` file like below.

	<project>
		...
		<build>
			<plugins>
				<plugin>
					<groupId>io.github.divinespear</groupId>
					<artifactId>jpa-schema-maven-plugin</artifactId>
					<version>0.1.12</version>
					<configuration>
						...
					</configuration>
					<executions>
						<!-- if you want auto-generate schema on lifecycle. default lifecycle is "process-classes". -->
						...
					</executions>
					<dependencies>
						<!-- JDBC driver here (if you don't defined in dependencies) -->
						...
					</dependencies>
				</plugin>
				...
			</plugins>
		</build>
		...
	</project>

To generate, use goal

	mvn jpa-schema:generate

or use default lifecycle

	mvn process-classes

To see configuration parameters, see [here](http://divinespear.github.io/jpa-schema-maven-plugin/generate-mojo.html).


## Database Product Names

It's about `databaseProductName` property. If not listed below, will work as basic standard SQL.

### for EclipseLink
`databaseMajorVersion` and `databaseMinorVersion` is not required.

* `Oracle12` = Oracle 12c
* `Oracle11` = Oracle 11g
* `Oracle10`: Oracle 10g
* `Oracle9`: Oracle 9i
* `Oracle`: Oracle with default compatibility
* `Microsoft SQL Server`
* `DB2`
* `MySQL`
* `PostgreSQL`
* `SQL Anywhere`
* `Sybase SQL Server`
* `Adaptive Server Enterprise` = Sybase
* `Pointbase`
* `Informix Dynamic Server`
* `Firebird`
* `ingres`
* `Apache Derby`
* `H2`
* `HSQL Database Engine`

### for Hibernate
Some products uses different dialect by `databaseMajorVersion` and/or `databaseMinorVersion`.
You can override using `hibernate.dialect` property.

* `CUBRID`
    * `org.hibernate.dialect.CUBRIDDialect` = all version
* `HSQL Database Engine`
    * `org.hibernate.dialect.HSQLDialect` = all version
* `H2`
    * `org.hibernate.dialect.H2Dialect` = all version
* `MySQL`
    * `org.hibernate.dialect.MySQL5Dialect` = 5.x
    * `org.hibernate.dialect.MySQLDialect` = 4.x or below
    * `org.hibernate.dialect.MySQLMyISAMDialect`
    * `org.hibernate.dialect.MySQLInnoDBDialect`
    * `org.hibernate.dialect.MySQL5InnoDBDialect`
    * `org.hibernate.dialect.MySQL57InnoDBDialect`
* `PostgreSQL`
    * `org.hibernate.dialect.PostgreSQL94Dialect` = 9.4 or above
    * `org.hibernate.dialect.PostgreSQL92Dialect` = 9.2 or above
    * `org.hibernate.dialect.PostgreSQL9Dialect` = 9.x
    * `org.hibernate.dialect.PostgreSQL82Dialect` = 8.2 or above
    * `org.hibernate.dialect.PostgreSQL81Dialect` = 8.1 or below
* `Apache Derby`
    * `org.hibernate.dialect.DerbyTenSevenDialect` = 10.7 or above
    * `org.hibernate.dialect.DerbyTenSixDialect` = 10.6
    * `org.hibernate.dialect.DerbyTenFiveDialect` = 10.5
    * `org.hibernate.dialect.DerbyDialect` = 10.4 or below
* `ingres`
    * `org.hibernate.dialect.Ingres10Dialect` = 10.x
    * `org.hibernate.dialect.Ingres9Dialect` = 9.2 or above
    * `org.hibernate.dialect.IngresDialect` = 9.1 or below
* `Microsoft SQL Server`
    * `org.hibernate.dialect.SQLServer2012Dialect` = 11.x
    * `org.hibernate.dialect.SQLServer2008Dialect` = 10.x
    * `org.hibernate.dialect.SQLServer2005Dialect` = 9.x
    * `org.hibernate.dialect.SQLServerDialect` = 8.x or below
* `Sybase SQL Server`
    * `org.hibernate.dialect.SybaseASE15Dialect` = all version
    * `org.hibernate.dialect.SybaseASE17Dialect`
* `Adaptive Server Enterprise` = Sybase
* `Adaptive Server Anywhere`
    * `org.hibernate.dialect.SybaseAnywhereDialect` = all version
* `Informix Dynamic Server`
    * `org.hibernate.dialect.InformixDialect` = all version
* ~~`DB2 UDB for AS/390`~~
    * `org.hibernate.dialect.DB2390Dialect`
* `DB2 UDB for AS/400`
    * `org.hibernate.dialect.DB2400Dialect` = all version
*  start with `DB2/`
    * `org.hibernate.dialect.DB2Dialect` = all version
* `Oracle`
    * `org.hibernate.dialect.Oracle12cDialect` = 12.x
    * `org.hibernate.dialect.Oracle10gDialect` = 11.x, 10.x
    * `org.hibernate.dialect.Oracle9iDialect` = 9.x
    * `org.hibernate.dialect.Oracle8iDialect` = 8.x or below
* `Firebird`
    * `org.hibernate.dialect.FirebirdDialect` = all version

## License

Source Copyright © 2013 Sin-young "Divinespear" Kang. Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses).
