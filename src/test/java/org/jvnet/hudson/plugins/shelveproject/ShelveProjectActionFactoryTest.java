package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Action;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Pierre Beitz
 * on 14/07/2018.
 */
public class ShelveProjectActionFactoryTest {

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
}
