package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Shell;
import jenkins.model.Jenkins;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.jvnet.hudson.plugins.shelveproject.ShelveProjectExecutable.ARCHIVE_FILE_EXTENSION;
import static org.jvnet.hudson.plugins.shelveproject.ShelveProjectExecutable.METADATA_FILE_EXTENSION;

@WithJenkins
class DeleteProjectExecutableTest {

    /**
     * Tests if a project is deleted.
     *
     * @throws Exception
     */
    @Test
    void testProjectTarIsDeleted(JenkinsRule jenkinsRule) throws Exception {

        String projectname = "ProjectWithWorkspace";

        FreeStyleProject project = jenkinsRule.createFreeStyleProject(projectname);
        project.getBuildersList().add(new Shell("echo hello"));

        FreeStyleBuild b = project.scheduleBuild2(0).get();

        assertTrue(b.getWorkspace().exists(), "Workspace should exist by now");

        File shelvedProjectsDir = new File(Jenkins.get().getRootDir(), "shelvedProjects");
        shelvedProjectsDir.mkdirs();

        ShelveProjectExecutable a = new ShelveProjectExecutable(null, project);
        a.run();

        try (Stream<Path> files = Files.list(shelvedProjectsDir.toPath())) {
            assertEquals(2, files.count(), "Directory should contain two archives, the metadata and the archive");
        }
        FileExplorerVisitor fileExplorerVisitor = new FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);
        assertEquals(1, fileExplorerVisitor.getArchiveFileCount(), "Not the expected number of archive archives");
        assertEquals(1, fileExplorerVisitor.getMetadataFileCount(), "Not the expected number of metadata archives");

        DeleteProjectExecutable deleteProjectExecutable = new DeleteProjectExecutable(null, fileExplorerVisitor.getArchives());
        deleteProjectExecutable.run();

        try (Stream<Path> files = Files.list(shelvedProjectsDir.toPath())) {
            assertEquals(0, files.count(), "Directory should not contain anything");
        }
    }

    static class FileExplorerVisitor extends SimpleFileVisitor<Path> {
        private int metadataFileCount = 0;
        private int archiveFileCount = 0;
        private int unexpectedFileCount = 0;
        private final List<String> archives = new ArrayList<>();
        private final List<Path> metadata = new ArrayList<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String extension = FilenameUtils.getExtension(file.toString());
            if (ARCHIVE_FILE_EXTENSION.equals(extension)) {
                archiveFileCount++;
                archives.add(file.getFileName().toString());
            } else if (METADATA_FILE_EXTENSION.equals(extension)) {
                metadataFileCount++;
                metadata.add(file);
            } else {
                unexpectedFileCount++;
            }
            return super.visitFile(file, attrs);
        }

        int getArchiveFileCount() {
            return archiveFileCount;
        }

        int getMetadataFileCount() {
            return metadataFileCount;
        }

        String[] getArchives() {
            return archives.toArray(new String[0]);
        }

        int getUnexpectedFileCount() {
            return unexpectedFileCount;
        }

        List<Path> getMetadata() {
            return metadata;
        }
    }

    /**
     * Tests if two projects can be unshelved and deleted in parallel
     *
     * @throws Exception
     */
    @Test
    void testParallelProjectUnshelvingAndDeleting(JenkinsRule jenkinsRule) throws Exception {

        String projectnameToUnshelve = "ProjectToUnshelveWithWorkspace";
        String projectnameToDelete = "ProjectToDeleteWithWorkspace";

        // Project to unshelve
        FreeStyleProject projectToUnshelve = jenkinsRule.createFreeStyleProject(projectnameToUnshelve);
        projectToUnshelve.getBuildersList().add(new Shell("echo hello"));
        FreeStyleBuild unshelveBuild = projectToUnshelve.scheduleBuild2(0).get();
        assertTrue(unshelveBuild.getWorkspace().exists(), "Workspace should exist by now");

        //Project to delete
        FreeStyleProject projectToDelete = jenkinsRule.createFreeStyleProject(projectnameToDelete);
        projectToDelete.getBuildersList().add(new Shell("echo hello"));
        FreeStyleBuild deleteBuild = projectToDelete.scheduleBuild2(0).get();
        assertTrue(deleteBuild.getWorkspace().exists(), "Workspace should exist by now");

        File shelvedProjectsDir = new File(Jenkins.get().getRootDir(), "shelvedProjects");
        shelvedProjectsDir.mkdirs();

        //Shelve project to be deleted later on
        ShelveProjectExecutable shelveProjectExecutable = new ShelveProjectExecutable(null, projectToDelete);
        shelveProjectExecutable.run();
        //Shelve project to be unshelved later on
        shelveProjectExecutable = new ShelveProjectExecutable(null, projectToUnshelve);
        shelveProjectExecutable.run();

        FileExplorerVisitor fileExplorerVisitor = new FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

        assertEquals(2, fileExplorerVisitor.getArchiveFileCount(), "Not the expected number of archive archives");
        assertEquals(2, fileExplorerVisitor.getMetadataFileCount(), "Not the expected number of metadata archives");
        assertEquals(0, fileExplorerVisitor.getUnexpectedFileCount(), "There should be no unexpected files");

        String[] archives = fileExplorerVisitor.getArchives();

        UnshelveProjectExecutable unShelveProjectExecutable = new UnshelveProjectExecutable(null, new String[]{archives[0]});
        DeleteProjectExecutable deleteProjectExecutable = new DeleteProjectExecutable(null, new String[]{archives[1]});

        unShelveProjectExecutable.run();
        deleteProjectExecutable.run();

        try (Stream<Path> files = Files.list(shelvedProjectsDir.toPath())) {
            assertEquals(0, files.count(), "Directory should not contain anything");
        }
    }
}
