package org.jvnet.hudson.plugins.shelveproject;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Queue;
import jenkins.model.Jenkins;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public long getEstimatedDuration()
    {
        return -1; // impossible to estimate duration
    }

    private boolean archiveProject() {

        wipeoutWorkspace();

        LOGGER.info("Creating archive for project [" + project.getName() + "].");
        try {
            Path projectRootPath = project.getRootDir().toPath();
            FilePath sourcePath = new FilePath(projectRootPath.toFile());
            Path rootPath = Jenkins.getInstance().getRootDir().toPath();
            Path shelvedProjectRoot = rootPath.resolve(ShelvedProjectsAction.SHELVED_PROJECTS_DIRECTORY);
            if(!Files.exists(shelvedProjectRoot)) {
                Files.createDirectory(shelvedProjectRoot);
            }
            Path archivePath = Files.createFile(shelvedProjectRoot.resolve(project.getName() + "-" + System.currentTimeMillis() + ".zip"));
            FilePath destinationPath = new FilePath(archivePath.toFile());
            sourcePath.zip(destinationPath);
            return true;
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Could not archive project [" + project.getName() + "].", e);
            return false;
        }
    }

    private void wipeoutWorkspace()
    {
            LOGGER.info( "Wiping out workspace for project [" + project.getName() + "]." );
        try {
            project.doDoWipeOutWorkspace();
        } catch (Exception e) {
            LOGGER.log( Level.SEVERE, "Could not wipeout workspace [" + project.getName() + "].", e );
        }

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
