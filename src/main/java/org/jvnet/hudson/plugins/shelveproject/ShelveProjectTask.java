package org.jvnet.hudson.plugins.shelveproject;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.*;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.SubTask;
import hudson.security.ACL;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.springframework.security.core.Authentication;

public class ShelveProjectTask implements Queue.FlyweightTask, Queue.TransientTask {
  private final Item item;

  public ShelveProjectTask(Item project) {
    this.item = project;
  }

  public CauseOfBlockage getCauseOfBlockage() {
    return item instanceof Queue.Task qt ? qt.getCauseOfBlockage() : null;
  }

  public String getName() {
    return "Shelving " + item.getFullName();
  }

  public String getFullDisplayName() {
    return getName();
  }

  public Queue.Executable createExecutable() {
    return new ShelveProjectExecutable(this, item);
  }

  @NonNull
  public Queue.Task getOwnerTask() {
    return this;
  }

  public void checkAbortPermission() {
    item.checkPermission(hudson.model.Item.CANCEL);
  }

  public boolean hasAbortPermission() {
    return item.hasPermission(hudson.model.Item.CANCEL);
  }

  public String getUrl() {
    return item.getUrl();
  }

  public Collection<? extends SubTask> getSubTasks() {
    final List<SubTask> subTasks = new LinkedList<>();
    subTasks.add(this);
    return subTasks;
  }

  public String getDisplayName() {
    return getName();
  }

  @NonNull
  @Override
  public Authentication getDefaultAuthentication2() {
    return ACL.SYSTEM2;
  }

  @NonNull
  @Override
  public Authentication getDefaultAuthentication2(Queue.Item item) {
    return getDefaultAuthentication2();
  }
}
