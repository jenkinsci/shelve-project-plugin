package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.*;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.SubTask;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ShelveProjectTask
    implements Queue.FlyweightTask, Queue.TransientTask
{
    private final AbstractProject project;

    public ShelveProjectTask( AbstractProject project )
    {
        this.project = project;
    }

    public Label getAssignedLabel()
    {
        return null;
    }

    public Node getLastBuiltOn()
    {
        return project.getLastBuiltOn();
    }

    public boolean isBuildBlocked()
    {
        return project.isBuildBlocked();
    }

    public String getWhyBlocked()
    {
        return project.getWhyBlocked();
    }

    public CauseOfBlockage getCauseOfBlockage() {
        return project.getCauseOfBlockage();
    }

    public String getName()
    {
        return "Shelve " + project.getName();
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
        return new ShelveProjectExecutable( this, project );
    }

    public Queue.Task getOwnerTask()
    {
        return null;
    }

    public Object getSameNodeConstraint()
    {
        return null;
    }

    public void checkAbortPermission()
    {
        project.checkAbortPermission();
    }

    public boolean hasAbortPermission()
    {
        return project.hasAbortPermission();
    }

    public String getUrl()
    {
        return project.getUrl();
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
        return project.getResourceList();
    }

    public String getDisplayName()
    {
        return getName();
    }
}
