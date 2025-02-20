package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.AbstractItem;
import hudson.model.Action;
import hudson.model.Item;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.verb.POST;

import java.util.logging.Logger;

public class ShelveProjectAction implements Action {
  private final static Logger LOGGER = Logger.getLogger(ShelveProjectAction.class.getName());
  private static final Permission SHELVE_PERMISSION = Item.DELETE;
  private Item item;

  private boolean isShelvingProject;

  public ShelveProjectAction(Item item) {
    this.item = item;
    this.isShelvingProject = false;
  }

  @Override
  public String getIconFileName() {
    return getShelveIconPath();
  }

  private static String getShelveIconPath() {
    return Jenkins.get().hasPermission(SHELVE_PERMISSION) ? "symbol-file-tray-stacked-outline plugin-ionicons-api" : null;
  }

  public String getDisplayName() {
    if (item instanceof AbstractItem a) {
      return "Shelve " + a.getPronoun();
    }
    return "Shelve Item";
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
  public HttpResponse doShelveProject() {
    Jenkins.get().checkPermission(Item.DELETE);
    if (!isShelvingProject()) {
      LOGGER.info("Shelving project [" + getItem().getFullName() + "].");
      // Shelving the project could take some time, so add it as a task
      Jenkins.get().getQueue().schedule(new ShelveProjectTask(item), 0);
    }

    return createRedirectToMainPage();
  }

  private HttpRedirect createRedirectToMainPage() {
    return new HttpRedirect(Jenkins.get().getRootUrl());
  }
}
