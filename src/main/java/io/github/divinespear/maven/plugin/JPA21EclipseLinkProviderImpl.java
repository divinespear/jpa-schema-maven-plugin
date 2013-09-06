package io.github.divinespear.maven.plugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;

class JPA21EclipseLinkProviderImpl
        extends BaseProviderImpl {

    public JPA21EclipseLinkProviderImpl() {
        super("eclipselink_2.1");
    }

    private static final String
            TARGET_DATABASE = "database",
            TARGET_SCRIPT = "script",
            TARGET_BOTH = "both";
    private static final List<String>
            TARGET_DATABASE_LIST = Arrays.asList(TARGET_DATABASE, TARGET_BOTH),
            TARGET_SCRIPT_LIST = Arrays.asList(TARGET_SCRIPT, TARGET_BOTH);

    @Override
    protected void doExecute(JpaSchemaGeneratorMojo mojo) {
        final String target = mojo.getTarget().toLowerCase();
        String databaseMode = PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION;
        if (TARGET_DATABASE_LIST.contains(target)) {
            databaseMode = mojo.getMode();
        }
        String scriptMode = PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION;
        if (TARGET_SCRIPT_LIST.contains(target)) {
            scriptMode = mojo.getMode();
        }

        Map<String, String> map = new HashMap<String, String>();
        if (!PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML_DEFAULT.equals(mojo.getPersistenceXml())) {
            this.putIfValueAvailable(map, PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML,
                                     mojo.getPersistenceXml());
        }
        this.putIfValueAvailable(map, PersistenceUnitProperties.SCHEMA_GENERATION_DATABASE_ACTION, databaseMode);
        this.putIfValueAvailable(map, PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_ACTION, scriptMode);
        if (!PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION.equals(scriptMode)) {
            this.putIfValueAvailable(map, PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET,
                                     new File(mojo.getOutputDirectory(), mojo.getCreateOutputFileName()).toString());
            this.putIfValueAvailable(map, PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_DROP_TARGET,
                                     new File(mojo.getOutputDirectory(), mojo.getDropOutputFileName()).toString());
        }

        Persistence.generateSchema(mojo.getPersistenceUnitName(), map);
    }
}
