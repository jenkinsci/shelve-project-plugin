package org.jvnet.hudson.plugins.shelveproject;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by Pierre Beitz
 * on 14/07/2018.
 */
@Extension
public class ShelveProjectActionFactory extends TransientProjectActionFactory {

    @Override
    public Collection<? extends Action> createFor(AbstractProject target) {
        return Collections.singleton(new ShelveProjectAction(target));
    }
}
