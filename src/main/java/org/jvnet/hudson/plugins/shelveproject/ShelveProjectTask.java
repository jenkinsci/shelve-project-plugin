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

public class ShelveProjectTask
    implements Queue.FlyweightTask, Queue.TransientTask
{
    private final BuildableItem item;

    public ShelveProjectTask( BuildableItem project )
    {
        this.item = project;
    }

    public Label getAssignedLabel()
    {
        return null;
    }

    public Node getLastBuiltOn() {
        return item.getLastBuiltOn();
    }

    public boolean isBuildBlocked()
    {
        return item.isBuildBlocked();
    }

    public String getWhyBlocked()
    {
        return item.getWhyBlocked();
    }

    public CauseOfBlockage getCauseOfBlockage() {
        return item.getCauseOfBlockage();
    }

    public String getName()
    {
        return "Shelving " + item.getName();
    }

    public String getFullDisplayName()
    {
        return getName();
    }

    public long getEstimatedDuration()
    {
        return -1;
    }

    public Queue.Executable createExecutable()
        throws IOException
    {
        return new ShelveProjectExecutable( this, item);
    }

    public Queue.Task getOwnerTask()
    {
        return this;
    }

    public Object getSameNodeConstraint()
    {
        return null;
    }

    public void checkAbortPermission()
    {
        item.checkAbortPermission();
    }

    public boolean hasAbortPermission()
    {
        return item.hasAbortPermission();
    }

    public String getUrl()
    {
        return item.getUrl();
    }

    public boolean isConcurrentBuild()
    {
        return false;
    }

    public Collection<? extends SubTask> getSubTasks()
    {
        final List<SubTask> subTasks = new LinkedList<SubTask>();
        subTasks.add(this);
        return subTasks;
    }

    public ResourceList getResourceList()
    {
        return item.getResourceList();
    }

    public String getDisplayName()
    {
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
