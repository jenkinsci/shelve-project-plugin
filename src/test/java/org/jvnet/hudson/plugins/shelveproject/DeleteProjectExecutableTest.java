
package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Queue.Task;
import hudson.tasks.Shell;
import jenkins.model.Jenkins;
import org.apache.commons.io.FilenameUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.jvnet.hudson.plugins.shelveproject.ShelveProjectExecutable.*;

public class DeleteProjectExecutableTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    /**
     * Tests if a project is deleted.
     *
     * @throws Exception
     */
    @Test
    public void testProjectTarIsDeleted() throws Exception {

        String projectname = "ProjectWithWorkspace";

        FreeStyleProject project = jenkinsRule.createFreeStyleProject(projectname);
        project.getBuildersList().add(new Shell("echo hello"));

        FreeStyleBuild b = project.scheduleBuild2(0).get();

        assertTrue("Workspace should exist by now", b.getWorkspace().exists());

        File shelvedProjectsDir = new File(Jenkins.getInstance().getRootDir(), "shelvedProjects");
        shelvedProjectsDir.mkdirs();

        ShelveProjectExecutable a = new ShelveProjectExecutable(null, project);
        a.run();

        try(Stream<Path> files= Files.list(shelvedProjectsDir.toPath())) {
            assertEquals("Directory should contain two archives, the metadata and the archive", 2, files.count());
        }
         FileExplorerVisitor fileExplorerVisitor = new FileExplorerVisitor();
         Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);
         assertEquals("Not the expected number of archive archives", 1, fileExplorerVisitor.getArchiveFileCount());
         assertEquals("Not the expected number of metadata archives", 1, fileExplorerVisitor.getMetadataFileCount());

         DeleteProjectExecutable deleteProjectExecutable = new DeleteProjectExecutable(null, fileExplorerVisitor.getArchives());
         deleteProjectExecutable.run();

        try(Stream<Path> files= Files.list(shelvedProjectsDir.toPath())) {
            assertEquals("Directory should not contain anything", 0, files.count());
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
    public void testParallelProjectUnshelvingAndDeleting() throws Exception {

        String projectnameToUnshelve = "ProjectToUnshelveWithWorkspace";
        String projectnameToDelete = "ProjectToDeleteWithWorkspace";

        // Project to unshelve
        FreeStyleProject projectToUnshelve = jenkinsRule.createFreeStyleProject(projectnameToUnshelve);
        projectToUnshelve.getBuildersList().add(new Shell("echo hello"));
        FreeStyleBuild unshelveBuild = projectToUnshelve.scheduleBuild2(0).get();
        assertTrue("Workspace should exist by now", unshelveBuild.getWorkspace().exists());

        //Project to delete
        FreeStyleProject projectToDelete = jenkinsRule.createFreeStyleProject(projectnameToDelete);
        projectToDelete.getBuildersList().add(new Shell("echo hello"));
        FreeStyleBuild deleteBuild = projectToDelete.scheduleBuild2(0).get();
        assertTrue("Workspace should exist by now", deleteBuild.getWorkspace().exists());

        File shelvedProjectsDir = new File(Jenkins.getInstance().getRootDir(), "shelvedProjects");
        shelvedProjectsDir.mkdirs();

        //Shelve project to be deleted later on
        ShelveProjectExecutable shelveProjectExecutable = new ShelveProjectExecutable(null, projectToDelete);
        shelveProjectExecutable.run();
        //Shelve project to be unshelved later on
        shelveProjectExecutable = new ShelveProjectExecutable(null, projectToUnshelve);
        shelveProjectExecutable.run();

        FileExplorerVisitor fileExplorerVisitor = new FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

        assertEquals("Not the expected number of archive archives", 2, fileExplorerVisitor.getArchiveFileCount());
        assertEquals("Not the expected number of metadata archives", 2, fileExplorerVisitor.getMetadataFileCount());
        assertEquals("There should be no unexpected files", 0, fileExplorerVisitor.getUnexpectedFileCount());

        String[] archives = fileExplorerVisitor.getArchives();

        UnshelveProjectExecutable unShelveProjectExecutable = new UnshelveProjectExecutable(null, new String[]{archives[0]});
        DeleteProjectExecutable deleteProjectExecutable = new DeleteProjectExecutable(null, new String[]{archives[1]});

        unShelveProjectExecutable.run();
        deleteProjectExecutable.run();

        try(Stream<Path> files= Files.list(shelvedProjectsDir.toPath())) {
            assertEquals("Directory should not contain anything", 0, files.count());
        }
    }
}
