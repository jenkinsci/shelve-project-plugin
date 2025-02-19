package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.Shell;
import jenkins.model.Jenkins;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.jvnet.hudson.plugins.shelveproject.ShelveProjectExecutable.ARCHIVE_COMPRESSION;
import static org.jvnet.hudson.plugins.shelveproject.ShelvedProjectsAction.SHELVED_PROJECTS_DIRECTORY;

/**
 * @author ben.patterson
 */
@WithJenkins
class ShelveProjectExecutableTest {

    @Test
    void testProjectTarIsCreated(JenkinsRule jenkinsRule) throws Exception {

        String projectname = "ProjectWithWorkspace";

        FreeStyleProject project = jenkinsRule.createFreeStyleProject(projectname);
        project.getBuildersList().add(new Shell("echo hello"));

        FreeStyleBuild b = project.scheduleBuild2(0).get();

        assertTrue(b.getWorkspace().exists(),
                "Workspace should exist by now");

        File shelvedProjectsDir = new File(Hudson.get().getRootDir(), "shelvedProjects");
        shelvedProjectsDir.mkdirs();

        ShelveProjectExecutable a = new ShelveProjectExecutable(null, project);
        a.run();

        DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

        assertEquals(1, fileExplorerVisitor.getArchiveFileCount(), "Not the expected number of archive archives");
        assertEquals(1, fileExplorerVisitor.getMetadataFileCount(), "Not the expected number of metadata archives");

    }

    @Issue("JENKINS-43434")
    @Test
    void testProjectInFolderContainsTheCorrectMetadata(JenkinsRule jenkinsRule) throws IOException {
        String projectName = "ProjectWithWorkspace";

        MockFolder myFolder = jenkinsRule.createFolder("myFolder");
        FreeStyleProject project = myFolder.createProject(FreeStyleProject.class, projectName);

        ShelveProjectExecutable a = new ShelveProjectExecutable(null, project);
        a.run();

        File shelvedProjectsDir = new File(Jenkins.get().getRootDir(), SHELVED_PROJECTS_DIRECTORY);

        DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

        assertEquals(1, fileExplorerVisitor.getArchiveFileCount(), "Not the expected number of archive archives");
        assertEquals(1, fileExplorerVisitor.getMetadataFileCount(), "Not the expected number of metadata archives");

        Path metadataPath = fileExplorerVisitor.getMetadata().get(0);
        Properties properties = ShelvedProject.loadMetadata(metadataPath.toFile());

        assertEquals("ProjectWithWorkspace", properties.get(ShelveProjectExecutable.PROJECT_NAME_PROPERTY), "Not the expected path");
        assertEquals("myFolder/ProjectWithWorkspace", properties.get(ShelveProjectExecutable.PROJECT_FULL_NAME_PROPERTY), "Not the expected path");
        assertEquals(Paths.get("myFolder/jobs/ProjectWithWorkspace").toString(), properties.get(ShelveProjectExecutable.PROJECT_PATH_PROPERTY), "Not the expected path");
    }

    @Issue("JENKINS-26432")
    @Test
    void testPipelineProjectTarIsCreated(JenkinsRule jenkinsRule) throws IOException, ExecutionException, InterruptedException {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "my-pipeline");

        project.scheduleBuild2(0).get();

        File shelvedProjectsDir = new File(Jenkins.get().getRootDir(), SHELVED_PROJECTS_DIRECTORY);
        shelvedProjectsDir.mkdirs();

        ShelveProjectExecutable executable = new ShelveProjectExecutable(null, project);
        executable.run();

        DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

        assertEquals(1, fileExplorerVisitor.getArchiveFileCount(), "Not the expected number of archive archives");
        assertEquals(1, fileExplorerVisitor.getMetadataFileCount(), "Not the expected number of metadata archives");

    }

    @Issue("JENKINS-27734")
    @Test
    void testFolderProjectTarIsCreated(JenkinsRule jenkinsRule) throws IOException {
        MockFolder folder = jenkinsRule.createFolder("my-folder");

        File shelvedProjectsDir = new File(Jenkins.get().getRootDir(), SHELVED_PROJECTS_DIRECTORY);
        shelvedProjectsDir.mkdirs();

        ShelveProjectExecutable executable = new ShelveProjectExecutable(null, folder);
        executable.run();

        DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

        assertEquals(1, fileExplorerVisitor.getArchiveFileCount(), "Not the expected number of archive archives");
        assertEquals(1, fileExplorerVisitor.getMetadataFileCount(), "Not the expected number of metadata archives");
    }

    @Issue("JENKINS-20922")
    @Test
    void ifTheExecutableIsNotRunningTheTimeStampReturnsNA(JenkinsRule jenkinsRule) throws IOException {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "my-pipeline");
        ShelveProjectExecutable executable = new ShelveProjectExecutable(null, project);
        assertEquals("N/A", executable.getTimestampString(), "Not the expected duration");
    }

    @Issue("JENKINS-55244")
    @Test
    void aShelvedProjectShouldBeCompressed(JenkinsRule jenkinsRule) throws IOException {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "my-pipeline");
        File shelvedProjectsDir = new File(Jenkins.get().getRootDir(), SHELVED_PROJECTS_DIRECTORY);
        shelvedProjectsDir.mkdirs();

        ShelveProjectExecutable executable = new ShelveProjectExecutable(null, project);
        executable.run();

        DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

        String archivePath = fileExplorerVisitor.getArchives()[0];
        try (InputStream is = Files.newInputStream(shelvedProjectsDir.toPath().resolve(archivePath).toFile().toPath())) {
            byte[] fileAsBit = new byte[2];
            is.read(fileAsBit);
            assertEquals(0x1f, fileAsBit[0], "Not the expected first byte for a gzip file");
            assertEquals((byte) 0x8b, fileAsBit[1], "Not the expected second byte for a gzip file");
        }
    }

    @Issue("JENKINS-55244")
    @Test
    void aShelvedProjectMetadatafileShouldIndicateItsCompressed(JenkinsRule jenkinsRule) throws IOException {
        WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "my-pipeline");
        File shelvedProjectsDir = new File(Jenkins.get().getRootDir(), SHELVED_PROJECTS_DIRECTORY);
        shelvedProjectsDir.mkdirs();

        ShelveProjectExecutable executable = new ShelveProjectExecutable(null, project);
        executable.run();

        DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

        Path metadataPath = fileExplorerVisitor.getMetadata().get(0);
        Properties shelveProperties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(metadataPath)) {
            shelveProperties.load(reader);

        }
        String archiveCompressionProperty = shelveProperties.getProperty(ARCHIVE_COMPRESSION, "false");
        assertTrue(Boolean.parseBoolean(archiveCompressionProperty), "metada  file should indicate the archive is compressed");
    }

    @Issue("JENKINS-52781")
    @Test
    void symlinksAreNotShelved(JenkinsRule jenkinsRule) throws IOException, ExecutionException, InterruptedException {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("project");
        // we need to run one build to make sure the lastSuccessful link gets created
        project.scheduleBuild2(0).get();

        File shelvedProjectsDir = new File(Jenkins.get().getRootDir(), "shelvedProjects");
        shelvedProjectsDir.mkdirs();

        ShelveProjectExecutable exe = new ShelveProjectExecutable(null, project);
        exe.run();

        DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

        String[] archives = fileExplorerVisitor.getArchives();

        assertEquals(1, archives.length, "Shelving should have created one archive");

        try (TarArchiveInputStream archive = new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(new File(shelvedProjectsDir, archives[0]).toPath())))) {
            TarArchiveEntry entry;
            String fileName;
            while ((entry = archive.getNextTarEntry()) != null) {
                fileName = entry.getName();
                if (fileName.contains("lastSuccessful")) {
                    fail("Found a lastSuccessful symlink, it should have been filtered out");
                }
            }
        }
    }
}
