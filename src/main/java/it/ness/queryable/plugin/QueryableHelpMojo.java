package it.ness.queryable.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
* Queryable is maven plugin for filter defs.
* @goal help
* @execute lifecycle="queryable" phase="process-sources"
*/
public class QueryableHelpMojo extends AbstractMojo
{
    public void execute() throws MojoExecutionException
    {
        getLog().info( "help" );
    }
}