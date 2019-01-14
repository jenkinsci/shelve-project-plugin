package org.jvnet.hudson.plugins.shelveproject;

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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Pierre Beitz
 * on 2019-03-10.
 */
public class ShelveProjectActionTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setUp() {
        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        jenkinsRule.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy().
                grant(Jenkins.ADMINISTER).everywhere().to("admin").
                grant(Jenkins.READ, Item.DELETE).everywhere().to("developer").
                grant(Jenkins.READ, Item.CREATE).everywhere().to("creator").
                grant(Jenkins.READ).everywhere().to("reader"));
    }

    @Issue("JENKINS-55462")
    @Test
    public void testShelveIconShouldBeVisibleForAdmin() {
        ACL.as(User.get("admin"));
        assertNotNull("Shelve icon should be visible", new ShelveProjectAction(null).getIconFileName());
    }

    @Issue("JENKINS-55462")
    @Test
    public void testShelveIconShouldBeVisibleForUserWithDeleteRights() {
        ACL.as(User.get("developer"));
        assertNotNull("Shelve icon should be visible", new ShelveProjectAction(null).getIconFileName());
    }

    @Issue("JENKINS-55462")
    @Test
    public void testShelveIconShouldNotBeVisibleForOtherUsers() {
        ACL.as(User.get("creator"));
        assertNull("Shelve icon should not be visible", new ShelveProjectAction(null).getIconFileName());
        ACL.as(User.get("reader"));
        assertNull("Shelve icon should not be visible", new ShelveProjectAction(null).getIconFileName());
    }
}
