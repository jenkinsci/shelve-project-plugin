package org.jvnet.hudson.plugins.shelveproject;

import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.apache.commons.io.FileUtils.touch;
import static org.junit.Assert.assertEquals;

/**
 * Created by Pierre Beitz
 * on 2019-06-22.
 */
// Extracted from ShelveProjectsActionTest as the setup is not the same
public class ShelvedProjectsActionWithSymbolicLinkTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();


    @Test
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @Issue("JENKINS-55382")
    public void listingTheShelveProjectsShouldWork() throws IOException {
        File shelvedProjectsCustomDir = new File(Jenkins.getInstance().getRootDir(), "shelvedProjectsCustom");
        File shelvedProjectsDir = new File(Jenkins.getInstance().getRootDir(), "shelvedProjects");
        shelvedProjectsCustomDir.mkdirs();

        Files.createSymbolicLink(shelvedProjectsDir.toPath(), shelvedProjectsCustomDir.toPath());

        touch(new File(shelvedProjectsDir, "project1-1262634014828.zip"));
        touch(new File(shelvedProjectsDir, "project2-1262634114828.zip"));

        List<ShelvedProject> shelvedProjects = new ShelvedProjectsAction().getShelvedProjects();
        assertEquals("Should have found two archived projects.", 2, shelvedProjects.size());
    }
}
