package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Action;
import hudson.model.Item;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.verb.POST;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

public class ShelveProjectAction implements Action {
  private final static Logger LOGGER = Logger.getLogger(ShelveProjectAction.class.getName());
  private static final Permission SHELVE_PERMISSION = Item.DELETE;
  private Item item;

  private boolean isShelvingProject;
  private static final String ACTION_ICON_PATH = "/plugin/shelve-project-plugin/icons/shelve-project-icon.png";

  public ShelveProjectAction(Item item) {
    this.item = item;
    this.isShelvingProject = false;
  }

  @Override
  public String getIconFileName() {
    return getShelveIconPath();
  }

  private static String getShelveIconPath() {
    return Jenkins.getInstance().hasPermission(SHELVE_PERMISSION) ? ACTION_ICON_PATH : null;
  }

  public String getDisplayName() {
    return "Shelve Project";
  }

  public String getUrlName() {
    return "shelve";
  }

  public Item getItem() {
    return item;
  }

  public boolean isShelvingProject() {
    return isShelvingProject;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @POST
  public HttpResponse doShelveProject()
          throws IOException, ServletException {
    Jenkins.getInstance().checkPermission(Item.DELETE);
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
