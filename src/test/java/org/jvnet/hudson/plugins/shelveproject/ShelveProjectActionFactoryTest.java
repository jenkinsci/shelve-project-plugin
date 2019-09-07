package org.jvnet.hudson.plugins.shelveproject;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.Action;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Pierre Beitz
 * on 14/07/2018.
 */
public class ShelveProjectActionFactoryTest {

  @Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  @Test
  public void checkApiContract() {
    Collection<? extends Action> actions = new ShelveProjectActionFactory().createFor(new FreeStyleProject((ItemGroup) null, "my-job"));
    assertNotNull("Factory contract says methods should return a null object", actions);
  }

  @Test
  public void theFactoryShouldReturnASingleShelveProjectActionForAFreestyleJob() {
    Collection<? extends Action> actions = new ShelveProjectActionFactory().createFor(new FreeStyleProject((ItemGroup) null, "my-job"));
    assertEquals("Only one action expected", 1, actions.size());
    assertTrue("Action should be of type " + ShelveProjectAction.class, actions.toArray(new Object[0])[0] instanceof ShelveProjectAction);
  }

  @Issue("JENKINS-26432")
  @Test
  public void theFactoryShouldReturnASingleShelveProjectActionForAPipelineJob() {
    Collection<? extends Action> actions = new ShelveProjectActionFactory().createFor(new WorkflowJob(null, "my-pipeline"));
    assertEquals("Only one action expected", 1, actions.size());
    assertTrue("Action should be of type " + ShelveProjectAction.class, actions.toArray(new Object[0])[0] instanceof ShelveProjectAction);
  }

  @Issue("JENKINS-27734")
  @Test
  public void theFactoryShouldReturnASingleShelveProjectActionForAFolder() {
    Collection<? extends Action> actions = new ShelveProjectActionFactory().createFor(new Folder(null, "my-folder"));
    assertEquals("Only one action expected", 1, actions.size());
    assertTrue("Action should be of type " + ShelveProjectAction.class, actions.toArray(new Object[0])[0] instanceof ShelveProjectAction);
  }
}
