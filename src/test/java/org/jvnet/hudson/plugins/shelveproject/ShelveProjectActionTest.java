package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Pierre Beitz
 * on 2019-03-10.
 */
public class ShelveProjectActionTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    private FreeStyleProject project = null;

    @Before
    public void setUp() throws IOException{
        project = jenkinsRule.createFreeStyleProject();
        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        jenkinsRule.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy().
                grant(Jenkins.ADMINISTER).onItems(project).to("admin").
                grant(Jenkins.READ, Item.DELETE).onItems(project).to("developer").
                grant(Jenkins.READ, Item.CREATE).onItems(project).to("creator").
                grant(Jenkins.READ).onItems(project).to("reader"));
    }

    @Issue("JENKINS-55462")
    @Test
    public void testShelveIconShouldBeVisibleForAdmin() {
        ACL.as(User.get("admin"));
        assertNotNull("Shelve icon should be visible", new ShelveProjectAction(project).getIconFileName());
    }

    @Issue({"JENKINS-55462", "JENKINS-66382"})
    @Test
    public void testShelveIconShouldBeVisibleForUserWithDeleteRights()  {
        ACL.as(User.get("developer"));
        assertNotNull("Shelve icon should be visible", new ShelveProjectAction(project).getIconFileName());
    }

    @Issue({"JENKINS-55462", "JENKINS-66382"})
    @Test
    public void testShelveIconShouldNotBeVisibleForOtherUsers() {
        ACL.as(User.get("creator"));
        assertNull("Shelve icon should not be visible", new ShelveProjectAction(project).getIconFileName());
        ACL.as(User.get("reader"));
        assertNull("Shelve icon should not be visible", new ShelveProjectAction(project).getIconFileName());
    }
}
