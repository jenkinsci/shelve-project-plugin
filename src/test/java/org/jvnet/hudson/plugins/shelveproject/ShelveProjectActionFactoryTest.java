package org.jvnet.hudson.plugins.shelveproject;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.Action;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by Pierre Beitz
 * on 14/07/2018.
 */
@WithJenkins
class ShelveProjectActionFactoryTest {

    @Test
    void checkApiContract(JenkinsRule jenkinsRule) {
        Collection<? extends Action> actions = new ShelveProjectActionFactory().createFor(new FreeStyleProject((ItemGroup) null, "my-job"));
        assertNotNull(actions, "Factory contract says methods should return a null object");
    }

    @Test
    void theFactoryShouldReturnASingleShelveProjectActionForAFreestyleJob(JenkinsRule jenkinsRule) {
        Collection<? extends Action> actions = new ShelveProjectActionFactory().createFor(new FreeStyleProject((ItemGroup) null, "my-job"));
        assertEquals(1, actions.size(), "Only one action expected");
        assertInstanceOf(ShelveProjectAction.class, actions.toArray(new Object[0])[0], "Action should be of type " + ShelveProjectAction.class);
    }

    @Issue("JENKINS-26432")
    @Test
    void theFactoryShouldReturnASingleShelveProjectActionForAPipelineJob(JenkinsRule jenkinsRule) {
        Collection<? extends Action> actions = new ShelveProjectActionFactory().createFor(new WorkflowJob(null, "my-pipeline"));
        assertEquals(1, actions.size(), "Only one action expected");
        assertInstanceOf(ShelveProjectAction.class, actions.toArray(new Object[0])[0], "Action should be of type " + ShelveProjectAction.class);
    }

    @Issue("JENKINS-27734")
    @Test
    void theFactoryShouldReturnASingleShelveProjectActionForAFolder(JenkinsRule jenkinsRule) {
        Collection<? extends Action> actions = new ShelveProjectActionFactory().createFor(new Folder(null, "my-folder"));
        assertEquals(1, actions.size(), "Only one action expected");
        assertInstanceOf(ShelveProjectAction.class, actions.toArray(new Object[0])[0], "Action should be of type " + ShelveProjectAction.class);
    }
}
