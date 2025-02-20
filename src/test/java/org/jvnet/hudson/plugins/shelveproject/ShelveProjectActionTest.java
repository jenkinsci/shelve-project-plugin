package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import java.io.IOException;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by Pierre Beitz
 * on 2019-03-10.
 */
@WithJenkins
class ShelveProjectActionTest {

    private FreeStyleProject shelveMe;
    private FreeStyleProject keepMe;

    @BeforeEach
    void setUp(JenkinsRule jenkinsRule) throws IOException {
        shelveMe = jenkinsRule.createFreeStyleProject("shelveMe");
        keepMe = jenkinsRule.createFreeStyleProject("keepMe");
        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm());
        jenkinsRule.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy().
                grant(Jenkins.ADMINISTER).everywhere().to("admin").
                grant(Jenkins.READ, Item.DELETE).everywhere().to("developer").
                grant(Jenkins.READ, Item.CREATE).everywhere().to("creator").
                grant(Jenkins.READ).everywhere().to("reader").
                grant(Jenkins.READ).everywhere().to("deleter").
                grant(Item.DELETE).onPaths("shelveMe").to("deleter")
        );
    }

    @Issue(("JENKINS-66382"))
    @Test
    void testShelveIconShouldBeVisibleForUserWithDeleteOnProjectOnly() {
        try (ACLContext ignored = ACL.as2(User.get("deleter", true, new HashMap<>()).impersonate2())) {
            assertNotNull(new ShelveProjectAction(shelveMe).getIconFileName(), "Shelve icon should be visible");
        }
        try (ACLContext ignored = ACL.as2(User.get("deleter", true, new HashMap<>()).impersonate2())) {
            assertNull(new ShelveProjectAction(keepMe).getIconFileName(), "Shelve icon should not be visible");
        }
    }

    @Issue("JENKINS-55462")
    @Test
    void testShelveIconShouldBeVisibleForAdmin() {
        try (ACLContext ignored = ACL.as2(User.get("admin", true, new HashMap<>()).impersonate2())) {
            assertNotNull(new ShelveProjectAction(shelveMe).getIconFileName(), "Shelve icon should be visible");
        }
    }

    @Issue("JENKINS-55462")
    @Test
    void testShelveIconShouldBeVisibleForUserWithDeleteRights() {
        try (ACLContext ignored = ACL.as2(User.get("developer", true, new HashMap<>()).impersonate2())) {
            assertNotNull(new ShelveProjectAction(shelveMe).getIconFileName(), "Shelve icon should be visible");
        }
    }

    @Issue("JENKINS-55462")
    @Test
    void testShelveIconShouldNotBeVisibleForOtherUsers() {
        try (ACLContext ignored = ACL.as2(User.get("creator", true, new HashMap<>()).impersonate2())) {
            assertNull(new ShelveProjectAction(shelveMe).getIconFileName(), "Shelve icon should not be visible");
        }
        try (ACLContext ignored = ACL.as2(User.get("reader", true, new HashMap<>()).impersonate2())) {
            assertNull(new ShelveProjectAction(shelveMe).getIconFileName(), "Shelve icon should not be visible");
        }
    }
}
