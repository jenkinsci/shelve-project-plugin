package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.ResourceList;
import hudson.model.queue.CauseOfBlockage;

import java.io.File;
import java.io.IOException;

public class UnshelveProjectTask
    implements Queue.FlyweightTask, Queue.TransientTask
{
    private final File shelvedProjectDir;

    public UnshelveProjectTask( File shelvedProjectDir )
    {
        this.shelvedProjectDir = shelvedProjectDir;
    }

    public Label getAssignedLabel()
    {
        return null;
    }

    public Node getLastBuiltOn()
    {
        return null;
    }

    public boolean isBuildBlocked()
    {
        return false;
    }

    public String getWhyBlocked()
    {
        return null;
    }

    public CauseOfBlockage getCauseOfBlockage() {
        return null;
    }

    public String getName()
    {
        return "Unshelving Project";
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
        return new UnshelveProjectExecutable( this, shelvedProjectDir );
    }

    public void checkAbortPermission()
    {
    }

    public boolean hasAbortPermission()
    {
        return false;
    }

    public String getUrl()
    {
        return null;
    }

    public ResourceList getResourceList()
    {
        return new ResourceList();
    }

    public String getDisplayName()
    {
        return getName();
    }
}