package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.*;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.SubTask;
import hudson.security.ACL;
import org.acegisecurity.Authentication;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ShelveProjectTask implements Queue.FlyweightTask, Queue.TransientTask {
  private final Item item;

  public ShelveProjectTask(Item project) {
    this.item = project;
  }

  public Label getAssignedLabel() {
    return null;
  }

  public Node getLastBuiltOn() {
    return null;
  }

  public boolean isBuildBlocked() {
    return item instanceof Queue.Task && ((Queue.Task) item).isBuildBlocked();
  }

  @SuppressWarnings("deprecation")
  public String getWhyBlocked() {
    return item instanceof Queue.Task ? ((Queue.Task) item).getWhyBlocked() : "";
  }

  public CauseOfBlockage getCauseOfBlockage() {
    return item instanceof Queue.Task ? ((Queue.Task) item).getCauseOfBlockage() : null;
  }

  public String getName() {
    return "Shelving " + item.getName();
  }

  public String getFullDisplayName() {
    return getName();
  }

  public long getEstimatedDuration() {
    return -1;
  }

  public Queue.Executable createExecutable()
          throws IOException {
    return new ShelveProjectExecutable(this, item);
  }

  public Queue.Task getOwnerTask() {
    return this;
  }

  public Object getSameNodeConstraint() {
    return null;
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

  public boolean isConcurrentBuild() {
    return false;
  }

  public Collection<? extends SubTask> getSubTasks() {
    final List<SubTask> subTasks = new LinkedList<SubTask>();
    subTasks.add(this);
    return subTasks;
  }

  public ResourceList getResourceList() {
    return ResourceList.EMPTY;
  }

  public String getDisplayName() {
    return getName();
  }

  @Nonnull
  public Authentication getDefaultAuthentication() {
    return ACL.SYSTEM;
  }

  @Nonnull
  @Override
  public Authentication getDefaultAuthentication(Queue.Item item) {
    return getDefaultAuthentication();
  }
}
