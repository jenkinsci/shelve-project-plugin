package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"ResultOfMethodCallIgnored"})
@WithJenkins
class ShelvedProjectsActionTest {

    private ShelvedProjectsAction shelvedProjectsAction;
    private File shelvedProjectsDir;


    @BeforeEach
    void setUp(JenkinsRule jenkinsRule) {
        shelvedProjectsAction = new ShelvedProjectsAction();
        shelvedProjectsDir = new File(Hudson.get().getRootDir(), "shelvedProjects");
        shelvedProjectsDir.mkdirs();
        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        jenkinsRule.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy().
                grant(Jenkins.ADMINISTER).everywhere().to("admin").
                grant(Jenkins.READ, Item.DELETE).everywhere().to("developer").
                grant(Jenkins.READ, Item.CREATE).everywhere().to("creator").
                grant(Jenkins.READ).everywhere().to("reader"));
        ACL.as2(User.get("admin", true, new HashMap<>()).impersonate2());
    }

    @AfterEach
    void tearDown() {
        shelvedProjectsDir.delete();
    }

    @Test
    void testGetShelvedProjects_shouldReturnEmptyListWhenNoArchivedProjectsFound() {
        assertTrue(shelvedProjectsAction.getShelvedProjects().isEmpty(),
                "No archived projects should have been found.");
    }

    @Test
    void testGetShelvedProjects_shouldReturnShelvedProject() throws IOException {
        FileUtils.touch(new File(shelvedProjectsDir, "blackMesaProject-1262634114828.zip"));

        List<ShelvedProject> shelvedProjects = shelvedProjectsAction.getShelvedProjects();

        assertEquals(1, shelvedProjects.size(), "Should have found one archived projects.");
        assertEquals("blackMesaProject",
                shelvedProjects.get(0).getProjectName(),
                "Should have found one archived projects.");
        assertEquals(1262634114828L,
                shelvedProjects.get(0).getTimestamp(),
                "Should have found one archived projects.");
        assertEquals("blackMesaProject-1262634114828.zip",
                shelvedProjects.get(0).getArchive().getName(),
                "Should have found one archived projects.");
        assertNotNull(shelvedProjects.get(0).getFormatedDate(), "Should have set formatted date.");
    }

    @Test
    void testGetShelvedProjects_shouldReturnMultipleArchivedProjects()
            throws IOException {
        FileUtils.touch(new File(shelvedProjectsDir, "appretureScience-1262634014828.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "blackMesaProject-1262634114828.zip"));

        List<ShelvedProject> shelvedProjects = shelvedProjectsAction.getShelvedProjects();

        assertEquals(2, shelvedProjects.size(), "Should have found two archived projects.");
    }

    @Test
    void testGetShelvedProjects_shouldHandleProjectNamesWithHyphens()
            throws IOException {
        FileUtils.touch(new File(shelvedProjectsDir, "appreture-science-1262634014828.zip"));

        List<ShelvedProject> shelvedProjects = shelvedProjectsAction.getShelvedProjects();

        assertEquals(1, shelvedProjects.size(), "Should have found two archived projects.");
        assertEquals("appreture-science",
                shelvedProjects.get(0).getProjectName(),
                "Should have correctly gotten project name, even one with hypens.");
        assertEquals(1262634014828L,
                shelvedProjects.get(0).getTimestamp(),
                "Should have correctly gotten timestamp, even when project name has hypens.");
    }

    @Test
    void testGetShelvedProjects_shouldSortProjectsByName()
            throws IOException {
        FileUtils.touch(new File(shelvedProjectsDir, "bbb-1111111111111.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "~aa-1111111111111.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "!aa-1111111111111.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "YYY-1111111111111.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "zzz-1111111111111.zip"));
        FileUtils.touch(new File(shelvedProjectsDir, "aaa-1111111111111.zip"));

        List<ShelvedProject> shelvedProjects = shelvedProjectsAction.getShelvedProjects();

        assertEquals("!aa", shelvedProjects.get(0).getProjectName(), "Project list should have been sorted alphabetically.");
        assertEquals("~aa", shelvedProjects.get(1).getProjectName(), "Project list should have been sorted alphabetically.");
        assertEquals("aaa", shelvedProjects.get(2).getProjectName(), "Project list should have been sorted alphabetically.");
        assertEquals("bbb", shelvedProjects.get(3).getProjectName(), "Project list should have been sorted alphabetically.");
        assertEquals("YYY", shelvedProjects.get(4).getProjectName(), "Project list should have been sorted alphabetically.");
        assertEquals("zzz", shelvedProjects.get(5).getProjectName(), "Project list should have been sorted alphabetically.");
    }

    @Issue("JENKINS-55462")
    @Test
    void testGetShelvedProjectsWithNoAdminRightsShouldThrow() throws Exception {
        try (ACLContext ignored = ACL.as2(User.get("reader", true, new HashMap<>()).impersonate2())) {
            FileUtils.touch(new File(shelvedProjectsDir, "one-project.zip"));
            assertThrows(hudson.security.AccessDeniedException3.class, () ->
                    shelvedProjectsAction.getShelvedProjects());
        }
    }

    @Issue("JENKINS-55462")
    @Test
    void testGetShelvedProjectsWithOnlyCreateRightsShouldThrow() throws Exception {
        try (ACLContext ignored = ACL.as2(User.get("creator", true, new HashMap<>()).impersonate2())) {
            FileUtils.touch(new File(shelvedProjectsDir, "one-project.zip"));
            assertThrows(hudson.security.AccessDeniedException3.class, () ->
                    shelvedProjectsAction.getShelvedProjects());
        }
    }


    @Issue("JENKINS-55462")
    @Test
    void testUnShelveIconShouldBeVisibleForAdmin() {
        try (ACLContext ignored = ACL.as2(User.get("admin", true, new HashMap<>()).impersonate2())) {
            assertNotNull(new ShelvedProjectsAction().getIconFileName(), "Shelve icon should be visible");
        }
    }

    @Issue("JENKINS-55462")
    @Test
    void testShelveIconShouldNotBeVisibleForOtherUsers() {
        try (ACLContext ignored = ACL.as2(User.get("developer", true, new HashMap<>()).impersonate2())) {
            assertNull(new ShelvedProjectsAction().getIconFileName(), "Shelve icon should not be visible");
        }
        try (ACLContext ignored = ACL.as2(User.get("creator", true, new HashMap<>()).impersonate2())) {
            assertNull(new ShelvedProjectsAction().getIconFileName(), "Shelve icon should not be visible");
        }
        try (ACLContext ignored = ACL.as2(User.get("reader", true, new HashMap<>()).impersonate2())) {
            assertNull(new ShelvedProjectsAction().getIconFileName(), "Shelve icon should not be visible");
        }
    }
}
