jpa-schema-maven-plugin
=======================

[![Build Status](https://secure.travis-ci.org/divinespear/jpa-schema-maven-plugin.png)](http://travis-ci.org/divinespear/jpa-schema-maven-plugin)

Maven plugin for generate schema or DDL scripts from JPA entities using [JPA 2.1](http://jcp.org/en/jsr/detail?id=338) schema generator.

Currently support [EclipseLink](http://www.eclipse.org/eclipselink) (Reference Implementation) and [Hibernate](http://hibernate.org).


How-to Use
-----------------------

Define plugin at your maven `pom.xml` file like below.

	<project>
		...
		<build>
			<plugins>
				<plugin>
					<groupId>io.github.divinespear</groupId>
					<artifactId>jpa-schema-maven-plugin</artifactId>
					<version>0.1.0</version>
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

To see configuration parameters, see [here](generate-mojo.html).


Database Product Names
--------------------------------

It's about `databaseProductName` property. If not listed below, will work as basic standard SQL.

### for EclipseLink
`databaseMajorVersion` and `databaseMinorVersion` is not required.

* `Oracle 12`: Oracle 12g
* `Oracle 11`: Oracle 11g
* `Oracle 10`: Oracle 10g
* `Oracle 9`: Oracle 9i
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
some products uses different dialect by `databaseMajorVersion` and/or `databaseMinorVersion`.

* `CUBRID`
* `HSQL Database Engine`
* `H2`
* `MySQL`: 5.0 or above, 4.x or below
* `PostgreSQL`: 9.x, 8.x (8.2 or above), 8.1 or below
* `Apache Derby`: 10.7 or above, 10.6, 10.5, 10.4 or below
* `ingres`: 10.x, 9.x (9.2 or above), 9.1 or below
* `Microsoft SQL Server`: 11.x, 10.x, 9.x, 8.x or below
* `Sybase SQL Server`
* `Adaptive Server Enterprise` = Sybase
* `Adaptive Server Anywhere` = Sybase Anywhere
* `Informix Dynamic Server`
* `DB2 UDB for AS/400`
*  start with `DB2/`
* `Oracle`: 11.x, 10.x, 9.x, 8.x
* `Firebird`


License
-----------------------

Source Copyright © 2013 Sin-young "Divinespear" Kang. Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses).
