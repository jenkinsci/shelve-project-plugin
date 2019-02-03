package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Action;
import hudson.model.BuildableItem;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

public class ShelveProjectAction
        implements Action {
    private final static Logger LOGGER = Logger.getLogger(ShelveProjectAction.class.getName());

    private BuildableItem item;

    private boolean isShelvingProject;

    public ShelveProjectAction(BuildableItem item) {
        this.item = item;
        this.isShelvingProject = false;
    }

    public String getIconFileName() {
        if (Jenkins.getInstance().hasPermission(Permission.DELETE)) {
            return "/plugin/shelve-project-plugin/icons/shelve-project-icon.png";
        } else {
            return null;
        }
    }

    public String getDisplayName() {
        return "Shelve Project";
    }

    public String getUrlName() {
        return "shelve";
    }

    public BuildableItem getItem() {
        return item;
    }

    public boolean isShelvingProject() {
        return isShelvingProject;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public HttpResponse doShelveProject()
            throws IOException, ServletException {
        Jenkins.getInstance().checkPermission(Permission.DELETE);
        if (!isShelvingProject()) {
            LOGGER.info("Shelving project [" + getItem().getName() + "].");
            // Shelving the project could take some time, so add it as a task
            Jenkins.getInstance().getQueue().schedule(new ShelveProjectTask(item), 0);
        }

        return createRedirectToMainPage();
    }

    private HttpRedirect createRedirectToMainPage() {
        return new HttpRedirect(Jenkins.getInstance().getRootUrl());
    }
}
