package org.jvnet.hudson.plugins.shelveproject;

import hudson.FilePath;
import hudson.model.Hudson;
import hudson.model.Queue;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnshelveProjectExecutable
    implements Queue.Executable
{
    private final static Logger LOGGER = Logger.getLogger( UnshelveProjectExecutable.class.getName() );

    private final String[] shelvedProjectDirs;

    private final Queue.Task parentTask;

    public UnshelveProjectExecutable( Queue.Task parentTask, String[] shelvedProjectDirs )
    {
        this.parentTask = parentTask;
        this.shelvedProjectDirs = shelvedProjectDirs;
    }

    public Queue.Task getParent()
    {
        return parentTask;
    }

    public void run()
    {
        for (String shelvedProjectDirString : shelvedProjectDirs)
        {
            final File shelvedProjectDir = new File(shelvedProjectDirString);
            LOGGER.info( "Unshelving project [" + shelvedProjectDir + "]." );
            try
            {
                new FilePath( shelvedProjectDir ).unzip(
                    new FilePath( new File( Hudson.getInstance().getRootDir(), "jobs" ) ) );
                shelvedProjectDir.delete();
                Hudson.getInstance().reload();
            }
            catch ( Exception e )
            {
                LOGGER.log( Level.SEVERE, "Could not unarchive project archive [" + shelvedProjectDir + "].", e );
            }
        }
    }

    public long getEstimatedDuration()
    {
        return -1; // impossible to estimate duration
    }

    @Override
    public String toString()
    {
        return "Unshelving Project";
    }
}