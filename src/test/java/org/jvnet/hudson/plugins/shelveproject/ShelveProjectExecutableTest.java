
package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Hudson;
import hudson.model.FreeStyleProject;
import hudson.model.FreeStyleBuild;
import org.jvnet.hudson.test.HudsonTestCase;

import hudson.tasks.Shell;
import hudson.model.Queue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

/**
 *
 * @author ben.patterson
 */
public class ShelveProjectExecutableTest extends HudsonTestCase {
   private Queue.Task parentTask;
   

    @Test
    public void testProjectTarIsCreated() throws Exception {
        
        String projectname = "ProjectWithWorkspace";

        FreeStyleProject project = createFreeStyleProject(projectname);
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
    
    
}
