
package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Queue.Task;
import hudson.tasks.Shell;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DeleteProjectExecutableTest {
    private Task parentTask;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    /**
     * Tests if a project is deleted.
     *
     * @throws Exception
     */
    @Test
    public void testProjectZipIsDeleted() throws Exception {

        String projectname = "ProjectWithWorkspace";
        String filename;
        ArrayList<String> files = new ArrayList<String>();

        FreeStyleProject project = jenkinsRule.createFreeStyleProject(projectname);
        project.getBuildersList().add(new Shell("echo hello"));

        FreeStyleBuild b = project.scheduleBuild2(0).get();

        assertTrue("Workspace should exist by now", b.getWorkspace().exists());

        File shelvedProjectsDir = new File(Hudson.getInstance().getRootDir(), "shelvedProjects");
        shelvedProjectsDir.mkdirs();

        ShelveProjectExecutable a = new ShelveProjectExecutable(parentTask, project);
        a.run();

        File[] listOfFiles = shelvedProjectsDir.listFiles();

        // Read through target directory and find that the zip has been created.
        for (File file : listOfFiles) {
            if (file.isFile()) {
                filename = file.getName();
                files.add(filename);
                if (filename.startsWith(projectname) && (filename.endsWith(".zip"))) {
                    assertTrue("Found project .zip file in shelvedProjects", true);
                } else {
                    fail("Did not find project .zip file in shelvedProjects");
                }
            }
        }

        DeleteProjectExecutable deleteProjectExecutable = new DeleteProjectExecutable(parentTask, files.toArray(new String[files.size()]));
        deleteProjectExecutable.run();

        // Read through target directory and find that the zip has been deleted.
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                filename = listOfFile.getName();
                if (files.contains(filename)) {
                    fail("Found project .zip file in shelvedProjects");
                }
            }
        }
    }

    /**
     * Tests if two projects can be unshelved and deleted in parallel
     *
     * @throws Exception
     */
    @Test
    public void testParallelProjectUnshelvingAndDeleting() throws Exception {
        String filename;

        String projectnameToUnshelve = "ProjectToUnshelveWithWorkspace";
        String projectnameToDelete = "ProjectToDeleteWithWorkspace";

        ArrayList<String> filenamesToUnshelve = new ArrayList<String>();
        ArrayList<String> filenamesToDelete = new ArrayList<String>();

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

        File shelvedProjectsDir = new File(Hudson.getInstance().getRootDir(), "shelvedProjects");
        shelvedProjectsDir.mkdirs();

        //Shelve project to be deleted later on
        ShelveProjectExecutable shelveProjectExecutable = new ShelveProjectExecutable(parentTask, projectToDelete);
        shelveProjectExecutable.run();
        //Shelve project to be unshelved later on
        shelveProjectExecutable = new ShelveProjectExecutable(parentTask, projectToUnshelve);
        shelveProjectExecutable.run();

        File[] listOfFiles = shelvedProjectsDir.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                filename = file.getName();
                // Check if project to delete later on is created
                if (filename.startsWith(projectnameToDelete) && (filename.endsWith(".zip"))) {
                    filenamesToDelete.add(filename);
                    assertTrue("Found projectToDelete .zip file in shelvedProjects", true);
                } else if (filename.startsWith(projectnameToUnshelve) && (filename.endsWith(".zip"))) {
                    filenamesToUnshelve.add(filename);
                    assertTrue("Found projectToUnshelve .zip file in shelvedProjects", true);
                } else {
                    fail("Unexpected file found in shelvedProjects");
                }
            }
        }

        UnshelveProjectExecutable unShelveProjectExecutable = new UnshelveProjectExecutable(parentTask,filenamesToUnshelve.toArray(new String[filenamesToUnshelve.size()]));
        DeleteProjectExecutable deleteProjectExecutable = new DeleteProjectExecutable(parentTask, filenamesToDelete.toArray(new String[filenamesToDelete.size()]));

        unShelveProjectExecutable.run();
        deleteProjectExecutable.run();

        listOfFiles = shelvedProjectsDir.listFiles();

        // Check if project to delete is deleted and project to unshelve is unshelved
        for (File file : listOfFiles) {
            if (file.isFile()) {
                filename = file.getName();
                if (filename.startsWith(projectnameToUnshelve) && (filename.endsWith(".zip"))) {
                    fail("Found projectToUnshelve .zip file in shelvedProjects");
                } else if (filenamesToDelete.contains(filename)) {
                    fail("Found projectToDelete .zip file in shelvedProjects");
                }
            }
        }
    }
}
