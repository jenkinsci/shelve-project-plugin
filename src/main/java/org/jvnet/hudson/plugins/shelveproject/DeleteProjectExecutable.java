package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Hudson;
import hudson.model.Queue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteProjectExecutable
    implements Queue.Executable
{
    private final static Logger LOGGER = Logger.getLogger( DeleteProjectExecutable.class.getName() );

    private final String[] shelvedProjectArchiveNames;

    private final Queue.Task parentTask;

    public DeleteProjectExecutable(Queue.Task parentTask, String[] shelvedProjectArchiveNames)
    {
        this.parentTask = parentTask;
        this.shelvedProjectArchiveNames = shelvedProjectArchiveNames;
    }

    public Queue.Task getParent()
    {
        return parentTask;
    }

    public void run()
    {
        for (String shelvedProjectArchiveName : shelvedProjectArchiveNames)
        {
            final File shelvedProjectArchive = getArchiveFile(shelvedProjectArchiveName);
            LOGGER.info( "Deleting project [" + shelvedProjectArchiveName + "]." );
            try
            {
                shelvedProjectArchive.delete();
            }
            catch ( Exception e )
            {
                LOGGER.log( Level.SEVERE, "Could not delete project archive [" + shelvedProjectArchiveName + "].", e );
            }
        }
    }

    private File getArchiveFile(String shelvedProjectArchiveName) {
        // JENKINS-8759 - The archive name comes from the html form, so take a bit extra care for security reasons by
        // only accessing the archive if it exists in the directory of shevled projects.
        File shelvedProjectsDirectory = new File( Hudson.getInstance().getRootDir(), "shelvedProjects" );
        Collection<File> files = FileUtils.listFiles(shelvedProjectsDirectory, null, false);
        for (File file : files)
        {
            if (StringUtils.equals(file.getName(), shelvedProjectArchiveName))
            {
                return file;
            }
        }
        return null; // Project was already unshelved?
    }

    public long getEstimatedDuration()
    {
        return -1; // impossible to estimate duration
    }

    @Override
    public String toString()
    {
        return "Deleting Project";
    }
}