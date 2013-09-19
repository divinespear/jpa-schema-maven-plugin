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


License
-----------------------

Source Copyright © 2013 Sin-young "Divinespear" Kang. Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses).
