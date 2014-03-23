
package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Queue.Task;
import hudson.tasks.Shell;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.File;
import java.util.ArrayList;

/**
 * @author christian.galsterer
 */
public class DeleteProjectExecutableTest extends HudsonTestCase {
    private Task parentTask;


    public void testProjectZipIsDeleted() throws Exception {

        String projectname = "ProjectWithWorkspace";
        String file;
        ArrayList<String> files = new ArrayList<String>();

        FreeStyleProject project = createFreeStyleProject(projectname);
        project.getBuildersList().add(new Shell("echo hello"));

        FreeStyleBuild b = project.scheduleBuild2(0).get();

        assertTrue("Workspace should exist by now",
                b.getWorkspace().exists());

        File shelvedProjectsDir = new File(Hudson.getInstance().getRootDir(), "shelvedProjects");
        shelvedProjectsDir.mkdirs();

        ShelveProjectExecutable a = new ShelveProjectExecutable(parentTask, project);
        a.run();

        // Read through target directory and find that the zip has been created.
        File[] listOfFiles = shelvedProjectsDir.listFiles();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                file = listOfFile.getName();
                files.add(file);
                if (file.startsWith(projectname) && (file.endsWith(".zip"))) {
                    assertTrue("Found project .zip file in shelvedProjects", true);
                } else {
                    fail("Did not find project .zip file in shelvedProjects");
                }
            }
        }

        DeleteProjectExecutable deleteProjectExecutable = new DeleteProjectExecutable(parentTask, files.toArray(new String[files.size()]));
        deleteProjectExecutable.run();

        // Read through target directory and find that the zip has been deleted.
        listOfFiles = shelvedProjectsDir.listFiles();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                file = listOfFile.getName();
                if (files.contains(file)) {
                    fail("Found project .zip file in shelvedProjects");
                }
            }
        }
    }
}
