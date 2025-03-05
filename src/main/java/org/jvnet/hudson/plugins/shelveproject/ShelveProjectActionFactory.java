package org.jvnet.hudson.plugins.shelveproject;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Item;
import jenkins.model.TransientActionFactory;

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

  @NonNull
  @Override
  public Collection<? extends Action> createFor(@NonNull Item target) {
    return Collections.singleton(new ShelveProjectAction(target));
  }
}
