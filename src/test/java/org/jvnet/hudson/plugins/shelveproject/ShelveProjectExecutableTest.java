
package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Hudson;
import hudson.model.FreeStyleProject;
import hudson.model.FreeStyleBuild;
import jenkins.model.Jenkins;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;

import hudson.tasks.Shell;
import hudson.model.Queue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.jvnet.hudson.plugins.shelveproject.ShelveProjectExecutable.ARCHIVE_COMPRESSION;
import static org.jvnet.hudson.plugins.shelveproject.ShelvedProjectsAction.*;

/**
 * @author ben.patterson
 */
public class ShelveProjectExecutableTest {
  private Queue.Task parentTask;

  @Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  @Test
  public void testProjectTarIsCreated() throws Exception {

    String projectname = "ProjectWithWorkspace";

    FreeStyleProject project = jenkinsRule.createFreeStyleProject(projectname);
    project.getBuildersList().add(new Shell("echo hello"));

    FreeStyleBuild b = project.scheduleBuild2(0).get();

    assertTrue("Workspace should exist by now",
            b.getWorkspace().exists());

    File shelvedProjectsDir = new File(Hudson.getInstance().getRootDir(), "shelvedProjects");
    shelvedProjectsDir.mkdirs();

    ShelveProjectExecutable a = new ShelveProjectExecutable(parentTask, project);
    a.run();

    DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
    Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

    assertEquals("Not the expected number of archive archives", 1, fileExplorerVisitor.getArchiveFileCount());
    assertEquals("Not the expected number of metadata archives", 1, fileExplorerVisitor.getMetadataFileCount());

  }

  @Issue("JENKINS-43434")
  @Test
  public void testProjectInFolderContainsTheCorrectMetadata() throws IOException {
    String projectName = "ProjectWithWorkspace";

    MockFolder myFolder = jenkinsRule.createFolder("myFolder");
    FreeStyleProject project = myFolder.createProject(FreeStyleProject.class, projectName);

    ShelveProjectExecutable a = new ShelveProjectExecutable(null, project);
    a.run();

    File shelvedProjectsDir = new File(Jenkins.getInstance().getRootDir(), SHELVED_PROJECTS_DIRECTORY);

    DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
    Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

    assertEquals("Not the expected number of archive archives", 1, fileExplorerVisitor.getArchiveFileCount());
    assertEquals("Not the expected number of metadata archives", 1, fileExplorerVisitor.getMetadataFileCount());

    Path metadataPath = fileExplorerVisitor.getMetadata().get(0);
    Properties properties = ShelvedProject.loadMetadata(metadataPath.toFile());

    assertEquals("Not the expected path", "ProjectWithWorkspace", properties.get(ShelveProjectExecutable.PROJECT_NAME_PROPERTY));
    assertEquals("Not the expected path", "myFolder/ProjectWithWorkspace", properties.get(ShelveProjectExecutable.PROJECT_FULL_NAME_PROPERTY));
    assertEquals("Not the expected path", Paths.get("myFolder/jobs/ProjectWithWorkspace").toString(), properties.get(ShelveProjectExecutable.PROJECT_PATH_PROPERTY));
  }

  @Issue("JENKINS-26432")
  @Test
  public void testPipelineProjectTarIsCreated() throws IOException, ExecutionException, InterruptedException {
    WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "my-pipeline");

    WorkflowRun run = project.scheduleBuild2(0).get();

    File shelvedProjectsDir = new File(Jenkins.getInstance().getRootDir(), SHELVED_PROJECTS_DIRECTORY);
    shelvedProjectsDir.mkdirs();

    ShelveProjectExecutable executable = new ShelveProjectExecutable(null, project);
    executable.run();

    DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
    Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

    assertEquals("Not the expected number of archive archives", 1, fileExplorerVisitor.getArchiveFileCount());
    assertEquals("Not the expected number of metadata archives", 1, fileExplorerVisitor.getMetadataFileCount());

  }

  @Issue("JENKINS-27734")
  @Test
  public void testFolderProjectTarIsCreated() throws IOException {
    MockFolder folder = jenkinsRule.createFolder("my-folder");

    File shelvedProjectsDir = new File(Jenkins.getInstance().getRootDir(), SHELVED_PROJECTS_DIRECTORY);
    shelvedProjectsDir.mkdirs();

    ShelveProjectExecutable executable = new ShelveProjectExecutable(null, folder);
    executable.run();

    DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
    Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

    assertEquals("Not the expected number of archive archives", 1, fileExplorerVisitor.getArchiveFileCount());
    assertEquals("Not the expected number of metadata archives", 1, fileExplorerVisitor.getMetadataFileCount());
  }

  @Issue("JENKINS-20922")
  @Test
  public void ifTheExecutableIsNotRunningTheTimeStampReturnsNA() throws IOException {
    WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "my-pipeline");
    ShelveProjectExecutable executable = new ShelveProjectExecutable(null, project);
    assertEquals("Not the expected duration", "N/A", executable.getTimestampString());
  }

  @Issue("JENKINS-55244")
  @Test
  public void aShelvedProjectShouldBeCompressed() throws IOException {
    WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "my-pipeline");
    File shelvedProjectsDir = new File(Jenkins.getInstance().getRootDir(), SHELVED_PROJECTS_DIRECTORY);
    shelvedProjectsDir.mkdirs();

    ShelveProjectExecutable executable = new ShelveProjectExecutable(null, project);
    executable.run();

    DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
    Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

    String archivePath = fileExplorerVisitor.getArchives()[0];
    try(InputStream is = new FileInputStream(shelvedProjectsDir.toPath().resolve(archivePath).toFile())) {
      byte[] fileAsBit = new byte[2];
      is.read(fileAsBit);
      assertEquals("Not the expected first byte for a gzip file", 0x1f, fileAsBit[0]);
      assertEquals("Not the expected second byte for a gzip file", (byte) 0x8b, fileAsBit[1]);
    }
  }

  @Issue("JENKINS-55244")
  @Test
  public void aShelvedProjectMetadatafileShouldIndicateItsCompressed() throws IOException {
    WorkflowJob project = jenkinsRule.createProject(WorkflowJob.class, "my-pipeline");
    File shelvedProjectsDir = new File(Jenkins.getInstance().getRootDir(), SHELVED_PROJECTS_DIRECTORY);
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
    Assert.assertTrue("metada  file should indicate the archive is compressed", Boolean.parseBoolean(archiveCompressionProperty));
  }

  @Issue("JENKINS-52781")
  @Test
  public void symlinksAreNotShelved() throws IOException, ExecutionException, InterruptedException {
    FreeStyleProject project = jenkinsRule.createFreeStyleProject("project");
    // we need to run one build to make sure the lastSuccessful link gets created
    project.scheduleBuild2(0).get();

    File shelvedProjectsDir = new File(Jenkins.getInstance().getRootDir(), "shelvedProjects");
    shelvedProjectsDir.mkdirs();

    ShelveProjectExecutable exe = new ShelveProjectExecutable(null, project);
    exe.run();

    DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
    Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

    String[] archives = fileExplorerVisitor.getArchives();

    assertEquals("Shelving should have created one archive", 1, archives.length);

    try (TarArchiveInputStream archive = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(new File(shelvedProjectsDir, archives[0]))))) {
      TarArchiveEntry entry;
      String fileName;
      while ((entry = archive.getNextTarEntry()) != null) {
        fileName = entry.getName();
        if (fileName.contains("lastSuccessful")) {
          Assert.fail("Found a lastSuccessful symlink, it should have been filtered out");
        }
      }
    }
  }
}
