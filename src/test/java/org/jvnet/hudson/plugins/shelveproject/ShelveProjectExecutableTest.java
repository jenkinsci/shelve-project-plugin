
package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Hudson;
import hudson.model.FreeStyleProject;
import hudson.model.FreeStyleBuild;
import jenkins.model.Jenkins;
import org.junit.Rule;

import hudson.tasks.Shell;
import hudson.model.Queue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
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
        
        File shelvedProjectsDir = new File( Hudson.getInstance().getRootDir(), "shelvedProjects" );
        shelvedProjectsDir.mkdirs();
        
        ShelveProjectExecutable a = new ShelveProjectExecutable (parentTask,project);
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

        ShelveProjectExecutable a = new ShelveProjectExecutable (null,project);
        a.run();

        File shelvedProjectsDir = new File( Jenkins.getInstance().getRootDir(), ShelvedProjectsAction.SHELVED_PROJECTS_DIRECTORY);

        DeleteProjectExecutableTest.FileExplorerVisitor fileExplorerVisitor = new DeleteProjectExecutableTest.FileExplorerVisitor();
        Files.walkFileTree(shelvedProjectsDir.toPath(), fileExplorerVisitor);

        assertEquals("Not the expected number of archive archives", 1, fileExplorerVisitor.getArchiveFileCount());
        assertEquals("Not the expected number of metadata archives", 1, fileExplorerVisitor.getMetadataFileCount());

        Path metadataPath = fileExplorerVisitor.getMetadata().get(0);
        Properties properties = ShelvedProject.loadMetadata(metadataPath.toFile());

        assertEquals("Not the expected path", "ProjectWithWorkspace", properties.get(ShelveProjectExecutable.PROJECT_NAME_PROPERTY));
        assertEquals("Not the expected path", "myFolder/ProjectWithWorkspace", properties.get(ShelveProjectExecutable.PROJECT_FULL_NAME_PROPERTY));
        assertEquals("Not the expected path", "myFolder/jobs/ProjectWithWorkspace", properties.get(ShelveProjectExecutable.PROJECT_PATH_PROPERTY));

    }
}
