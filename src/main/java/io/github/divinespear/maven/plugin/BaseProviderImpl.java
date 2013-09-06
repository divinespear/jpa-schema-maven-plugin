package io.github.divinespear.maven.plugin;

import java.util.Map;

import org.codehaus.plexus.util.StringUtils;

abstract class BaseProviderImpl
        implements SchemaGeneratorProvider {

    private final String providerName;

    public BaseProviderImpl(String providerName) {
        this.providerName = providerName;
    }

    protected void putIfValueAvailable(Map<String, String> map,
                                       String key,
                                       String value) {
        if (!StringUtils.isBlank(value)) {
            map.put(key, value);
        }
    }

    @Override
    public String providerName() {
        return this.providerName;
    }

    @Override
    public void execute(ClassLoader classLoader,
                        JpaSchemaGeneratorMojo mojo) {
        Thread thread = Thread.currentThread();
        synchronized (thread) {
            ClassLoader original = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader(classLoader);
                this.doExecute(mojo);
            } finally {
                thread.setContextClassLoader(original);
            }
        }
    }

    abstract protected void doExecute(JpaSchemaGeneratorMojo mojo);
}
