package org.jvnet.hudson.plugins.shelveproject;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.verb.POST;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jvnet.hudson.plugins.shelveproject.ShelveProjectExecutable.ARCHIVE_FILE_EXTENSION;

@ExportedBean(defaultVisibility = 999)
@Extension
public class ShelvedProjectsAction implements RootAction {
    private final static Logger LOGGER = Logger.getLogger(ShelvedProjectsAction.class.getName());
    static final String SHELVED_PROJECTS_DIRECTORY = "shelvedProjects";
    private static final String ACTION_ICON_PATH = "/plugin/shelve-project-plugin/icons/shelve-project-icon.png";
    private static final Permission UNSHELVE_PERMISSION = Jenkins.ADMINISTER;

    @Override
    public String getIconFileName() {
        return getUnshelveIconPath();
    }

    private static String getUnshelveIconPath() {
        // keeping as the existing, ie you need the administrative priviledge to unshelve
        // otherwise you could leak projects (see documentation and JENKINS-55462).
        return Jenkins.getInstance().hasPermission(UNSHELVE_PERMISSION) ? ACTION_ICON_PATH : null;
    }

    public String getDisplayName() {
        return "Shelved Projects";
    }

    public String getUrlName() {
        return "/" + SHELVED_PROJECTS_DIRECTORY;
    }

    @Exported
    public List<ShelvedProject> getShelvedProjects() {
        Jenkins.getInstance().checkPermission(UNSHELVE_PERMISSION);
        Path rootPath = Jenkins.getInstance().getRootDir().toPath();
        List<ShelvedProject> projects = new LinkedList<>();
        try {
            Path shelvedProjectRoot = rootPath.resolve(ShelvedProjectsAction.SHELVED_PROJECTS_DIRECTORY);
            if (!Files.exists(shelvedProjectRoot)) {
                Files.createDirectory(shelvedProjectRoot);
            }
            PathMatcher legacyMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.zip");
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*." + ARCHIVE_FILE_EXTENSION);
            Files.walkFileTree(shelvedProjectRoot, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    super.visitFile(file, attrs);
                    if (legacyMatcher.matches(file)) {
                        projects.add(getLegacyShelvedProjectFromArchive(file.toFile()));
                    } else if (matcher.matches(file)) {
                        projects.add(getShelvedProjectFromArchive(file.toFile()));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not get the list of shelved projects", e);
        } finally {
            sortProjectsAlphabetically(projects);
        }
        return projects;
    }

    private void sortProjectsAlphabetically(final List<ShelvedProject> projects) {
        final Collator collator = Collator.getInstance();

        Collections.sort(projects, new Comparator<ShelvedProject>() {
            public int compare(ShelvedProject project1, ShelvedProject project2) {
                return collator.compare(project1.getProjectName(), project2.getProjectName());
            }
        });
    }

    private ShelvedProject getShelvedProjectFromArchive(File archive) {
        return ShelvedProject.createFromTar(archive);
    }

    private ShelvedProject getLegacyShelvedProjectFromArchive(File archive) {
        ShelvedProject shelvedProject = new ShelvedProject();
        shelvedProject.setProjectName(StringUtils.substringBeforeLast(archive.getName(), "-"));
        shelvedProject.setTimestamp(Long.parseLong(
                StringUtils.substringBefore(StringUtils.substringAfterLast(archive.getName(), "-"), ".")));
        shelvedProject.setArchive(archive);
        shelvedProject.setFormatedDate(formatDate(shelvedProject.getTimestamp()));
        return shelvedProject;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @POST
    public HttpResponse doManageShelvedProject(StaplerRequest request,
                                               StaplerResponse response)
            throws IOException, ServletException {
        if (request.hasParameter("unshelve"))
            return unshelveProject(request);
        else if (request.hasParameter("delete"))
            return deleteProject(request);
        return createRedirectToShelvedProjectsPage();
    }

    private HttpResponse unshelveProject(StaplerRequest request) {
        Jenkins.getInstance().checkPermission(UNSHELVE_PERMISSION);

        final String[] archives = request.getParameterValues("archives");
        if (archives == null) {
            return createRedirectToShelvedProjectsPage();
        }

        LOGGER.info("Unshelving archived projects.");
        // Unshelving the project could take some time, so add it as a task
        Jenkins.getInstance().getQueue().schedule(new UnshelveProjectTask(archives), 0);

        return createRedirectToMainPage();
    }

    private HttpResponse deleteProject(StaplerRequest request) {
        Jenkins.getInstance().checkPermission(UNSHELVE_PERMISSION);

        final String[] archives = request.getParameterValues("archives");
        if (archives == null) {
            return createRedirectToShelvedProjectsPage();
        }

        LOGGER.info("Deleting archived projects.");
        // Deleting the project could take some time, so add it as a task
        Jenkins.getInstance().getQueue().schedule(new DeleteProjectTask(archives), 0);

        return createRedirectToShelvedProjectsPage();
    }

    // TODO this will need some cleaning, outside of the scope of this dev
    public static String formatDate(long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        return simpleDateFormat.format(new Date(timestamp));
    }

    private HttpResponse createRedirectToShelvedProjectsPage() {
        return new HttpRedirect(Jenkins.getInstance().getRootUrl() + this.getUrlName());
    }

    private HttpRedirect createRedirectToMainPage() {
        return new HttpRedirect(Jenkins.getInstance().getRootUrl());
    }
}
