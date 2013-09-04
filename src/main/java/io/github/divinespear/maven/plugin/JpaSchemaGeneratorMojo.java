package io.github.divinespear.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Generate database schema or DDL scripts.
 * 
 * @author divinespear
 */
@Mojo(name = "generate",
      defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class JpaSchemaGeneratorMojo
        extends AbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        // TODO Auto-generated method stub
    }

}
