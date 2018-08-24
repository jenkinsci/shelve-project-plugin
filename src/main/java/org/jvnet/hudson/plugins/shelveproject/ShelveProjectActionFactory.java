package org.jvnet.hudson.plugins.shelveproject;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.BuildableItem;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Pierre Beitz
 * on 14/07/2018.
 */
@Extension
public class ShelveProjectActionFactory extends TransientActionFactory<BuildableItem> {

    @Override
    public Class<BuildableItem> type() {
        return BuildableItem.class;
    }

    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull BuildableItem target) {
        return Collections.singleton(new ShelveProjectAction(target));
    }
}
