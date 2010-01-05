package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Hudson;
import hudson.security.Permission;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

public class ShelveProjectAction
    implements Action
{
    final static Logger LOGGER = Logger.getLogger( ShelveProjectAction.class.getName() );

    private AbstractProject project;

    private boolean isShelvingProject;

    public ShelveProjectAction( AbstractProject project )
    {
        this.project = project;
        this.isShelvingProject = false;
    }

    public String getIconFileName()
    {
        if ( Hudson.getInstance().hasPermission( Permission.DELETE ) )
        {
            return "edit-delete.gif";
        }
        else
        {
            return null;
        }
    }

    public String getDisplayName()
    {
        return "Shelve Project";
    }

    public String getUrlName()
    {
        return "shelve";
    }

    public AbstractProject getProject()
    {
        return project;
    }

    public boolean isShelvingProject()
    {
        return isShelvingProject;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public HttpResponse doShelveProject()
        throws IOException, ServletException
    {
        Hudson.getInstance().checkPermission( Permission.DELETE );
        if ( !isShelvingProject() )
        {
            LOGGER.info( "Shelving project [" + getProject().getName() + "]." );
            // Shelving the project could take some time, so add it as a task
            Hudson.getInstance().getQueue().schedule( new ShelveProjectTask( project ), 0 );
        }

        return createRedirectToMainPage();
    }

    private HttpRedirect createRedirectToMainPage()
    {
        return new HttpRedirect( Hudson.getInstance().getRootUrl() );
    }
}
