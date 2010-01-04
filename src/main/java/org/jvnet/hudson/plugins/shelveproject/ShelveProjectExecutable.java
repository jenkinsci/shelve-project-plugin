package org.jvnet.hudson.plugins.shelveproject;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Queue;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShelveProjectExecutable
    implements Queue.Executable
{
    private final static Logger LOGGER = Logger.getLogger( ShelveProjectExecutable.class.getName() );

    private final AbstractProject project;

    private final Queue.Task parentTask;

    public ShelveProjectExecutable( Queue.Task parentTask, AbstractProject project )
    {
        this.parentTask = parentTask;
        this.project = project;
    }

    public Queue.Task getParent()
    {
        return parentTask;
    }

    public void run()
    {
        if ( archiveProject() )
        {
            deleteProject();
        }
    }

    private boolean archiveProject()
    {
        LOGGER.info( "Creating archive for project [" + project.getName() + "]." );
        try
        {
            final File projectRoot = project.getRootDir();
            OutputStream outputStream1 = createOutputStream( Hudson.getInstance().getRootDir(), project.getName() );
            new FilePath( projectRoot ).zip( outputStream1, new FileFilter()
            {
                public boolean accept( File file )
                {
                    return true;
                }
            } );
            outputStream1.close();
            return true;
        }
        catch ( Exception e )
        {
            LOGGER.log( Level.SEVERE, "Could not archive project [" + project.getName() + "].", e );
            return false;
        }
    }

    private OutputStream createOutputStream( final File rootDir, final String projectName )
        throws FileNotFoundException
    {
        final File baseDir = new File( rootDir, "shelvedProjects" );
        baseDir.mkdirs();
        final File archive = new File( baseDir, projectName + "-" + System.currentTimeMillis() + ".zip" );
        return new FileOutputStream( archive );
    }

    private void deleteProject()
    {
        LOGGER.info( "Deleting project [" + project.getName() + "]." );
        try
        {
            project.delete();
        }
        catch ( Exception e )
        {
            LOGGER.log( Level.SEVERE, "Could not delete project [" + project.getName() + "].", e );
        }
    }

    @Override
    public String toString()
    {
        return "Shelving " + project.getName();
    }
}
