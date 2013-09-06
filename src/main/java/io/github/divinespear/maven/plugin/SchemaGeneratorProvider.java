package io.github.divinespear.maven.plugin;

interface SchemaGeneratorProvider {

    String providerName();

    void execute(ClassLoader classLoader,
                 JpaSchemaGeneratorMojo mojo);
}
