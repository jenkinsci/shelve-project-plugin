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

    private final File shelvedProjectDir;

    private final Queue.Task parentTask;

    public UnshelveProjectExecutable( Queue.Task parentTask, File shelvedProjectDir )
    {
        this.parentTask = parentTask;
        this.shelvedProjectDir = shelvedProjectDir;
    }

    public Queue.Task getParent()
    {
        return parentTask;
    }

    public void run()
    {
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

    @Override
    public String toString()
    {
        return "Unshelving Project";
    }
}