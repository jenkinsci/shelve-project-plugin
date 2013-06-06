
package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Hudson;
import org.apache.commons.io.FileUtils;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.FreeStyleBuild;
import org.jvnet.hudson.test.HudsonTestCase;

import hudson.security.*;
import hudson.tasks.BuildTrigger;
import hudson.tasks.Shell;
import hudson.scm.NullSCM;
import hudson.Launcher;
import hudson.FilePath;
import hudson.Functions;
import hudson.Util;
import hudson.model.Queue;
import hudson.tasks.ArtifactArchiver;
import hudson.util.StreamTaskListener;
import hudson.util.OneShotEvent;
import java.io.IOException;

import jenkins.model.Jenkins;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;

import org.jvnet.hudson.test.Bug;
//import org.jvnet.hudson.test.MemoryAssert;
import org.jvnet.hudson.test.recipes.PresetData;
import org.jvnet.hudson.test.recipes.PresetData.DataSet;

import hudson.model.Queue.Task;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;
//import org.jvnet.hudson.test.MockFolder;

/**
 *
 * @author ben.patterson
 */
public class ShelveProjectExecutableTest extends HudsonTestCase {
   private Queue.Task parentTask;
   


    public void testProjectZipIsCreated() throws Exception {
        
        String projectname = "ProjectWithWorkspace";
        String files;
        
        
        FreeStyleProject project = createFreeStyleProject(projectname);
        project.getBuildersList().add(new Shell("echo hello"));
        
        FreeStyleBuild b = project.scheduleBuild2(0).get();

        assertTrue("Workspace should exist by now",
                b.getWorkspace().exists());
        
        File shelvedProjectsDir = new File( Hudson.getInstance().getRootDir(), "shelvedProjects" );
        shelvedProjectsDir.mkdirs();
        
        ShelveProjectExecutable a = new ShelveProjectExecutable (parentTask,project);
        a.run();

        // Read through target directory and find that the zip has been created.
        File[] listOfFiles = shelvedProjectsDir.listFiles(); 
        for (int i = 0; i < listOfFiles.length; i++) 
            {
                if (listOfFiles[i].isFile()) 
                {
                files = listOfFiles[i].getName();
                if (files.startsWith(projectname) && (files.endsWith(".zip"))) 
                    {
                    assertTrue("Found project .zip file in shelvedProjects", true);
                    } else {
                    fail("Did not find project .zip file in shelvedProjects");
                        }
                }
            };                
    }
    
    
}
