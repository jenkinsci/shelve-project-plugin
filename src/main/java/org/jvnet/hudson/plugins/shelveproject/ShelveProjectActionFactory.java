package org.jvnet.hudson.plugins.shelveproject;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Item;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Pierre Beitz
 * on 14/07/2018.
 */
@Extension
public class ShelveProjectActionFactory extends TransientActionFactory<Item> {

  @Override
  public Class<Item> type() {
    return Item.class;
  }

  @Nonnull
  @Override
  public Collection<? extends Action> createFor(@Nonnull Item target) {
    return Collections.singleton(new ShelveProjectAction(target));
  }
}
