package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.FreeStyleProject;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static org.junit.Assert.assertTrue;


/**
 * Created by Pierre Beitz
 * on 10/07/2018.
 */
public class UnshelveProjectExecutableTest {

  @Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  @Issue("JENKINS-43434")
  @Test
  public void testUnshelvingIsNotDoneAtRoot() throws IOException {
    String projectName = "ProjectWithWorkspace";
    String myFolderName = "myFolder";

    MockFolder myFolder = jenkinsRule.createFolder(myFolderName);
    FreeStyleProject project = myFolder.createProject(FreeStyleProject.class, projectName);

    ShelveProjectExecutable a = new ShelveProjectExecutable(null, project);
    a.run();

    File shelvedProjectsDir = new File(Jenkins.getInstance().getRootDir(), ShelvedProjectsAction.SHELVED_PROJECTS_DIRECTORY);

    DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
    Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);
    String[] archives = fileExplorerVisitor.getArchives();

    UnshelveProjectExecutable unShelveProjectExecutable = new UnshelveProjectExecutable(null, new String[]{archives[0]});
    unShelveProjectExecutable.run();
    Path jobConfigPath = Paths.get(Jenkins.getInstance().getRootDir().getPath(), "jobs", myFolderName, "jobs", projectName, "config.xml");
    assertTrue("Couldn't find the job configuration in the folder", Files.exists(jobConfigPath));
  }

  @Issue("JENKINS-43434")
  @Test
  public void testUnshelvingAFolderWithItsContent() throws IOException {
    String projectName = "ProjectWithWorkspace";
    String myFolderName = "myFolder";

    MockFolder myFolder = jenkinsRule.createFolder(myFolderName);
    FreeStyleProject project = myFolder.createProject(FreeStyleProject.class, projectName);

    ShelveProjectExecutable exe = new ShelveProjectExecutable(null, myFolder);
    exe.run();

    File shelvedProjectsDir = new File(Jenkins.getInstance().getRootDir(), ShelvedProjectsAction.SHELVED_PROJECTS_DIRECTORY);

    DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
    Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);
    String[] archives = fileExplorerVisitor.getArchives();

    UnshelveProjectExecutable unShelveProjectExecutable = new UnshelveProjectExecutable(null, new String[]{archives[0]});
    unShelveProjectExecutable.run();
    Path jobConfigPath = Paths.get(Jenkins.getInstance().getRootDir().getPath(), "jobs", myFolderName, "jobs", projectName, "config.xml");
    assertTrue("Couldn't find the job configuration in the folder", Files.exists(jobConfigPath));
  }
}
