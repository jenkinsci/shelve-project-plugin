package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.AbstractProject;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.ResourceList;

import java.io.IOException;

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
        return project.isBuilding();
    }

    public String getWhyBlocked()
    {
        return "Project [" + project.getName() + "] is building.";
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

    public ResourceList getResourceList()
    {
        return project.getResourceList();
    }

    public String getDisplayName()
    {
        return getName();
    }
}
