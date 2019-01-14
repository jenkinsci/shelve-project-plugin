package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.After;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;


import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class ShelvedProjectsActionTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private ShelvedProjectsAction shelvedProjectsAction;
    private File shelvedProjectsDir;


    @Before
    public void setUp() {
        shelvedProjectsAction = new ShelvedProjectsAction();
        shelvedProjectsDir = new File(Hudson.getInstance().getRootDir(), "shelvedProjects");
        shelvedProjectsDir.mkdirs();
        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        jenkinsRule.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy().
                grant(Jenkins.ADMINISTER).everywhere().to("admin").
                grant(Jenkins.READ, Item.DELETE).everywhere().to("developer").
                grant(Jenkins.READ, Item.CREATE).everywhere().to("creator").
                grant(Jenkins.READ).everywhere().to("reader"));
        ACL.as(User.get("admin"));
    }

    @After
    public void tearDown() {
        shelvedProjectsDir.delete();
    }

    @Test
    public void testGetShelvedProjects_shouldReturnEmptyListWhenNoArchivedProjectsFound() {
        assertTrue("No archived projects should have been found.",
                shelvedProjectsAction.getShelvedProjects().isEmpty());
    }

    @Test
    public void testGetShelvedProjects_shouldReturnShelvedProject() throws IOException {
        FileUtils.touch(new File(shelvedProjectsDir, "blackMesaProject-1262634114828.zip"));

        List<ShelvedProject> shelvedProjects = shelvedProjectsAction.getShelvedProjects();

        assertEquals("Should have found one archived projects.", 1, shelvedProjects.size());
        assertEquals("Should have found one archived projects.", "blackMesaProject",
                shelvedProjects.get(0).getProjectName());
        assertEquals("Should have found one archived projects.", 1262634114828L,
                shelvedProjects.get(0).getTimestamp());
        assertEquals("Should have found one archived projects.", "blackMesaProject-1262634114828.zip",
                shelvedProjects.get(0).getArchive().getName());
        assertNotNull("Should have set formatted date.", shelvedProjects.get(0).getFormatedDate());
    }

    @Test
    public void testGetShelvedProjects_shouldReturnMultipleArchivedProjects()
            throws IOException {
        FileUtils.touch(new File(shelvedProjectsDir, "appretureScience-1262634014828.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "blackMesaProject-1262634114828.zip"));

        List<ShelvedProject> shelvedProjects = shelvedProjectsAction.getShelvedProjects();

        assertEquals("Should have found two archived projects.", 2, shelvedProjects.size());
    }

    @Test
    public void testGetShelvedProjects_shouldHandleProjectNamesWithHyphens()
            throws IOException {
        FileUtils.touch(new File(shelvedProjectsDir, "appreture-science-1262634014828.zip"));

        List<ShelvedProject> shelvedProjects = shelvedProjectsAction.getShelvedProjects();

        assertEquals("Should have found two archived projects.", 1, shelvedProjects.size());
        assertEquals("Should have correctly gotten project name, even one with hypens.", "appreture-science",
                shelvedProjects.get(0).getProjectName());
        assertEquals("Should have correctly gotten timestamp, even when project name has hypens.", 1262634014828L,
                shelvedProjects.get(0).getTimestamp());
    }

    @Test
    public void testGetShelvedProjects_shouldSortProjectsByName()
            throws IOException {
        FileUtils.touch(new File(shelvedProjectsDir, "bbb-1111111111111.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "~aa-1111111111111.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "!aa-1111111111111.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "YYY-1111111111111.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "zzz-1111111111111.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "aaa-1111111111111.zip"));

        List<ShelvedProject> shelvedProjects = shelvedProjectsAction.getShelvedProjects();

        assertEquals("Project list should have been sorted alphabetically.",
                "!aa", shelvedProjects.get(0).getProjectName());
        assertEquals("Project list should have been sorted alphabetically.",
                "~aa", shelvedProjects.get(1).getProjectName());
        assertEquals("Project list should have been sorted alphabetically.",
                "aaa", shelvedProjects.get(2).getProjectName());
        assertEquals("Project list should have been sorted alphabetically.",
                "bbb", shelvedProjects.get(3).getProjectName());
        assertEquals("Project list should have been sorted alphabetically.",
                "YYY", shelvedProjects.get(4).getProjectName());
        assertEquals("Project list should have been sorted alphabetically.",
                "zzz", shelvedProjects.get(5).getProjectName());
    }

    @Issue("JENKINS-55462")
    @Test(expected = hudson.security.AccessDeniedException2.class)
    public void testGetShelvedProjectsWithNoAdminRightsShouldThrow() throws IOException {
        ACL.as(User.get("reader"));
        FileUtils.touch(new File(shelvedProjectsDir, "one-project.zip"));
        shelvedProjectsAction.getShelvedProjects();
    }

    @Issue("JENKINS-55462")
    @Test(expected = hudson.security.AccessDeniedException2.class)
    public void testGetShelvedProjectsWithOnlyCreateRightsShouldThrow() throws IOException {
        ACL.as(User.get("creator"));
        FileUtils.touch(new File(shelvedProjectsDir, "one-project.zip"));
        shelvedProjectsAction.getShelvedProjects();
    }


    @Issue("JENKINS-55462")
    @Test
    public void testUnShelveIconShouldBeVisibleForAdmin() {
        ACL.as(User.get("admin"));
        assertNotNull("Shelve icon should be visible", new ShelvedProjectsAction().getIconFileName());
    }

    @Issue("JENKINS-55462")
    @Test
    public void testShelveIconShouldNotBeVisibleForOtherUsers() {
        ACL.as(User.get("developer"));
        assertNull("Shelve icon should not be visible", new ShelvedProjectsAction().getIconFileName());
        ACL.as(User.get("creator"));
        assertNull("Shelve icon should not be visible", new ShelvedProjectsAction().getIconFileName());
        ACL.as(User.get("reader"));
        assertNull("Shelve icon should not be visible", new ShelvedProjectsAction().getIconFileName());
    }
}
