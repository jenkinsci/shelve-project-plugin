package org.jvnet.hudson.plugins.shelveproject;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jvnet.hudson.plugins.shelveproject.ShelveProjectExecutable.*;

public class ShelvedProject {
    private final static Logger LOGGER = Logger.getLogger(ShelvedProject.class.getName());

    private String projectName;

    private File archive;

    private long timestamp;

    private String formatedDate;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public File getArchive() {
        return archive;
    }

    public void setArchive(File archive) {
        this.archive = archive;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormatedDate() {
        return formatedDate;
    }

    public void setFormatedDate(String formatedDate) {
        this.formatedDate = formatedDate;
    }

    static ShelvedProject createFromTar(File archive) {
        ShelvedProject shelvedProject = new ShelvedProject();
        try {
            Properties shelveProperties = loadMetadata(archive);
            shelvedProject.setTimestamp(Long.parseLong(shelveProperties.getProperty(ARCHIVE_TIME_PROPERTY)));
            shelvedProject.setArchive(archive);
            // TODO this will need some cleaning, outside of the scope of this dev
            shelvedProject.setFormatedDate(ShelvedProjectsAction.formatDate(shelvedProject.getTimestamp()));
            shelvedProject.setProjectName(shelveProperties.getProperty(PROJECT_FULL_NAME_PROPERTY));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load the archive properly for " + archive, e);
        }
        return shelvedProject;
    }

    static Properties loadMetadata(File archive) throws IOException {
        Path metadataPath = getMetadataFileFromArchive(archive);
        Properties shelveProperties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(metadataPath)) {
            shelveProperties.load(reader);

        }
        return shelveProperties;
    }

    static Path getMetadataFileFromArchive(File archive) {
        return Paths.get(FilenameUtils.getFullPath(archive.getAbsolutePath()),
                FilenameUtils.getBaseName(archive.getAbsolutePath()) + "." + METADATA_FILE_EXTENSION);
    }
}
