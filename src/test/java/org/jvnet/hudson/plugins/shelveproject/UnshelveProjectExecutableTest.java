package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.FreeStyleProject;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Created by Pierre Beitz
 * on 10/07/2018.
 */
@WithJenkins
class UnshelveProjectExecutableTest {

    @Issue("JENKINS-43434")
    @Test
    void testUnshelvingIsNotDoneAtRoot(JenkinsRule jenkinsRule) throws IOException {
        String projectName = "ProjectWithWorkspace";
        String myFolderName = "myFolder";

        MockFolder myFolder = jenkinsRule.createFolder(myFolderName);
        FreeStyleProject project = myFolder.createProject(FreeStyleProject.class, projectName);

        ShelveProjectExecutable a = new ShelveProjectExecutable(null, project);
        a.run();

        File shelvedProjectsDir = new File(Jenkins.get().getRootDir(), ShelvedProjectsAction.SHELVED_PROJECTS_DIRECTORY);

        DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);
        String[] archives = fileExplorerVisitor.getArchives();

        UnshelveProjectExecutable unShelveProjectExecutable = new UnshelveProjectExecutable(null, new String[]{archives[0]});
        unShelveProjectExecutable.run();
        Path jobConfigPath = Paths.get(Jenkins.get().getRootDir().getPath(), "jobs", myFolderName, "jobs", projectName, "config.xml");
        assertTrue(Files.exists(jobConfigPath), "Couldn't find the job configuration in the folder");
    }

    @Issue("JENKINS-43434")
    @Test
    void testUnshelvingAFolderWithItsContent(JenkinsRule jenkinsRule) throws IOException {
        String projectName = "ProjectWithWorkspace";
        String myFolderName = "myFolder";

        MockFolder myFolder = jenkinsRule.createFolder(myFolderName);
        myFolder.createProject(FreeStyleProject.class, projectName);

        ShelveProjectExecutable exe = new ShelveProjectExecutable(null, myFolder);
        exe.run();

        File shelvedProjectsDir = new File(Jenkins.get().getRootDir(), ShelvedProjectsAction.SHELVED_PROJECTS_DIRECTORY);

        DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);
        String[] archives = fileExplorerVisitor.getArchives();

        UnshelveProjectExecutable unShelveProjectExecutable = new UnshelveProjectExecutable(null, new String[]{archives[0]});
        unShelveProjectExecutable.run();
        Path jobConfigPath = Paths.get(Jenkins.get().getRootDir().getPath(), "jobs", myFolderName, "jobs", projectName, "config.xml");
        assertTrue(Files.exists(jobConfigPath), "Couldn't find the job configuration in the folder");
    }
}
