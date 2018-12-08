package org.jvnet.hudson.plugins.shelveproject;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.BuildableItem;
import hudson.model.Executor;
import hudson.model.ItemGroup;
import hudson.model.Queue;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShelveProjectExecutable
        implements Queue.Executable {
    private final static Logger LOGGER = Logger.getLogger(ShelveProjectExecutable.class.getName());

    static final String METADATA_FILE_EXTENSION = "properties";
    static final String ARCHIVE_FILE_EXTENSION = "tar";

    static final String PROJECT_PATH_PROPERTY = "project.path";
    static final String PROJECT_FULL_NAME_PROPERTY = "project.fullname";
    static final String PROJECT_NAME_PROPERTY = "project.name";
    static final String ARCHIVE_TIME_PROPERTY = "archive.time";

    private final BuildableItem item;

    private final Queue.Task parentTask;

    public ShelveProjectExecutable(Queue.Task parentTask, BuildableItem item) {
        this.parentTask = parentTask;
        this.item = item;
    }

    public Queue.Task getParent() {
        return parentTask;
    }

    public void run() {
        if (archiveProject()) {
            deleteProject();
        }
    }

    public long getEstimatedDuration() {
        return -1; // impossible to estimate duration
    }

    public String getTimestampString() {
        Executor executor = Executor.of(this);
        if(executor != null) {
            return executor.getTimestampString();
        }
        return "N/A";
    }

    private boolean archiveProject() {

        wipeoutWorkspace();

        LOGGER.info("Creating archive for project [" + item.getName() + "].");
        try {
            Path projectRootPath = item.getRootDir().toPath();
            List<String> regexp = createListOfFoldersToBackup();
            regexp.add(relativizeToJenkinsJobsDirectory(projectRootPath) + "/**/*");
            long archiveTime = System.currentTimeMillis();
            String backupBaseName = item.getName() + "-" + archiveTime;
            Path rootPath = Jenkins.getInstance().getRootDir().toPath();
            Path shelvedProjectRoot = rootPath.resolve(ShelvedProjectsAction.SHELVED_PROJECTS_DIRECTORY);
            if (!Files.exists(shelvedProjectRoot)) {
                Files.createDirectory(shelvedProjectRoot);
            }
            buildMetadataFile(shelvedProjectRoot, backupBaseName, archiveTime);
            // use a tar because of https://github.com/jenkinsci/jenkins/pull/2639 when using ant includes.
            // and this will also fix https://issues.jenkins-ci.org/browse/JENKINS-10986.
            // keeping the filename formatted as before, if external scripts depend on it they won't be broken
            Path archivePath = Files.createFile(shelvedProjectRoot.resolve(backupBaseName + "." + ARCHIVE_FILE_EXTENSION));
            FilePath destinationPath = new FilePath(archivePath.toFile());
            tar(new FilePath(getJenkinsJobsDirectory()), destinationPath, String.join(",", regexp));
            return true;
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Could not archive project [" + item.getName() + "].", e);
            return false;
        }
    }

    private void buildMetadataFile(Path shelvedProjectRoot, String backupBaseName, long archiveTime) throws IOException {
        Path archivePath = Files.createFile(shelvedProjectRoot.resolve(backupBaseName + "." + METADATA_FILE_EXTENSION));
        Path projectRootPath = item.getRootDir().toPath();
        try (BufferedWriter writer = Files.newBufferedWriter(archivePath, Charset.forName("UTF-8"))) {
            addNewProperty(writer, PROJECT_PATH_PROPERTY, escapeForPropertiesFile(relativizeToJenkinsJobsDirectory(projectRootPath)));
            addNewProperty(writer, PROJECT_NAME_PROPERTY, item.getName());
            addNewProperty(writer, ARCHIVE_TIME_PROPERTY, Long.toString(archiveTime));
            addNewProperty(writer, PROJECT_FULL_NAME_PROPERTY, item.getFullName());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not write metadata for project [" + item.getName() + "].", e);
            throw e;
        }
    }

    private static String escapeForPropertiesFile(@Nonnull  String path) {
        // Windows is using \ while it's an escape character in properties files
        return path.replaceAll("\\\\", java.util.regex.Matcher.quoteReplacement("\\\\"));
    }

    private static void addNewProperty(BufferedWriter writer, String key, String value) throws IOException {
        writer.write(key + "=" + value);
        writer.newLine();
    }

    // could probably be integrated in FilePath
    private static void tar(FilePath origin, FilePath dst, String glob) throws IOException, InterruptedException {
        try (OutputStream os = dst.write()) {
            origin.tar(os, glob);
        }
    }

    private List<String> createListOfFoldersToBackup() {
        List<String> regexp = new ArrayList<>();
        // TODO: test Folder plugin is here
        ItemGroup parent = item.getParent();
        // technically not using Folder plugin code, but in practice, the Folder plugin code should be there for this
        // situation to occur.
        while (parent instanceof AbstractFolder) {
            LOGGER.log(Level.INFO, "Archiving parent folder: " + parent.getFullName());
            Path absoluteFolderConfigPath = parent.getRootDir().toPath().resolve("config.xml");
            regexp.add(relativizeToJenkinsJobsDirectory(absoluteFolderConfigPath));
            parent = ((AbstractFolder) parent).getParent();
        }
        return regexp;
    }

    private String relativizeToJenkinsJobsDirectory(Path path) {
        return getJenkinsJobsDirectory().toPath().relativize(path).toString();
    }

    private File getJenkinsJobsDirectory() {
        return new File(Jenkins.getInstance().getRootDir(), "jobs");
    }

    private void wipeoutWorkspace() {
        LOGGER.info("Wiping out workspace for project [" + item.getName() + "].");
        try {
            if(item instanceof AbstractProject) {
                ((AbstractProject) item).doDoWipeOutWorkspace();
            }
            // there is no API to do this in the case of Pipelines: https://issues.jenkins-ci.org/browse/JENKINS-26138
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not wipeout workspace [" + item.getName() + "].", e);
        }
    }

    private void deleteProject() {
        LOGGER.info("Deleting project [" + item.getName() + "].");
        try {
           item.delete();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not delete project [" + item.getName() + "].", e);
        }
    }

    @Override
    public String toString() {
        return "Shelving " + item.getName();
    }
}
