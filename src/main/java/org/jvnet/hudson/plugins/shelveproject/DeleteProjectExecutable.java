package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Queue;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jvnet.hudson.plugins.shelveproject.ShelveProjectExecutable.ARCHIVE_FILE_EXTENSION;

/**
 * A {@link Queue.Executable} that will take care of removing the provided shelvedProjects/
 * <p>
 * This executable will take care of deleting both the list of archive provided and also their
 * associated metadata file (if relevant)
 */
public class DeleteProjectExecutable implements Queue.Executable {
    private final static Logger LOGGER = Logger.getLogger(DeleteProjectExecutable.class.getName());

    private final String[] shelvedProjectArchiveNames;

    private final Queue.Task parentTask;

    /**
     * Creates a {@link DeleteProjectExecutable}.
     *
     * @param parentTask                 The task from which the executable was created. Most likely {@link DeleteProjectTask}
     * @param shelvedProjectArchiveNames The list of shelve archives to delete
     */
    public DeleteProjectExecutable(Queue.Task parentTask, String[] shelvedProjectArchiveNames) {
        this.parentTask = parentTask;
        this.shelvedProjectArchiveNames = shelvedProjectArchiveNames != null ?
                Arrays.copyOf(shelvedProjectArchiveNames, shelvedProjectArchiveNames.length) : null;
    }

    public Queue.Task getParent() {
        return parentTask;
    }

    public void run() {
        for (String shelvedProjectArchiveName : shelvedProjectArchiveNames) {
            final File shelvedProjectArchive = getArchiveFile(shelvedProjectArchiveName);
            LOGGER.info("Deleting project [" + shelvedProjectArchiveName + "].");
            try {
                if (ARCHIVE_FILE_EXTENSION.equals(FilenameUtils.getExtension(shelvedProjectArchiveName))) {
                    Files.delete(ShelvedProject.getMetadataFileFromArchive(shelvedProjectArchive));
                }
                Files.delete(shelvedProjectArchive.toPath());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not delete project archive [" + shelvedProjectArchiveName + "].", e);
            }
        }
    }

    static File getArchiveFile(String shelvedProjectArchiveName) {
        // JENKINS-8759 - The archive name comes from the html form, so take a bit extra care for security reasons by
        // only accessing the archive if it exists in the directory of shevled projects.
        File shelvedProjectsDirectory = new File(Jenkins.getInstance().getRootDir(), ShelvedProjectsAction.SHELVED_PROJECTS_DIRECTORY);
        Collection<File> files = FileUtils.listFiles(shelvedProjectsDirectory, null, false);
        for (File file : files) {
            if (StringUtils.equals(file.getName(), shelvedProjectArchiveName)) {
                return file;
            }
        }
        return null; // Project was already unshelved?
    }

    public long getEstimatedDuration() {
        return -1; // impossible to estimate duration
    }

    @Override
    public String toString() {
        return "Deleting Project";
    }
}