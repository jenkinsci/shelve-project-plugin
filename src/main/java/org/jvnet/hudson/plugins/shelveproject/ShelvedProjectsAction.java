package org.jvnet.hudson.plugins.shelveproject;

import hudson.Extension;
import hudson.model.Failure;
import hudson.model.Hudson;
import hudson.model.RootAction;
import hudson.security.Permission;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@ExportedBean(defaultVisibility = 999)
@Extension
public class ShelvedProjectsAction
    implements RootAction
{
    final static Logger LOGGER = Logger.getLogger( ShelvedProjectsAction.class.getName() );

    public String getIconFileName()
    {
        if ( Hudson.getInstance().hasPermission( Permission.CREATE ) )
        {
            return "/plugin/shelve-project-plugin/icons/shelve-project-icon.png";
        }
        else
        {
            return null;
        }
    }

    public String getDisplayName()
    {
        return "Shelved Projects";
    }

    public String getUrlName()
    {
        return "/shelvedProjects";
    }

    @SuppressWarnings({"unchecked"})
    @Exported
    public List<ShelvedProject> getShelvedProjects()
    {
        Hudson.getInstance().checkPermission( Permission.CREATE );

        final File shelvedProjectsDir = new File( Hudson.getInstance().getRootDir(), "shelvedProjects" );
        shelvedProjectsDir.mkdirs();

        final Collection<File> shelvedProjectsArchives =
            FileUtils.listFiles( shelvedProjectsDir, new String[]{"tgz","zip"}, false );

        List<ShelvedProject> projects = new LinkedList<ShelvedProject>();
        for ( File archive : shelvedProjectsArchives )
        {
            projects.add( getShelvedProjectFromArchive( archive ) );
        }

        sortProjectsAlphabetically(projects);

        return projects;
    }

    private void sortProjectsAlphabetically(final List<ShelvedProject> projects)
    {
        final Collator collator = Collator.getInstance();

        Collections.sort(projects, new Comparator<ShelvedProject>()
        {
            public int compare(ShelvedProject project1, ShelvedProject project2)
            {
                return collator.compare(project1.getProjectName(), project2.getProjectName());
            }
        });
    }

    private ShelvedProject getShelvedProjectFromArchive( File archive )
    {
        ShelvedProject shelvedProject = new ShelvedProject();
        shelvedProject.setProjectName( StringUtils.substringBeforeLast( archive.getName(), "-" ) );
        shelvedProject.setTimestamp( Long.valueOf(
            StringUtils.substringBefore( StringUtils.substringAfterLast( archive.getName(), "-" ), "." ) ) );
        shelvedProject.setArchive( archive );
        shelvedProject.setFormatedDate( formatDate( shelvedProject.getTimestamp() ) );
        return shelvedProject;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public HttpResponse doUnshelveProject( StaplerRequest request,
                                           StaplerResponse response )
        throws IOException, ServletException
    {
        Hudson.getInstance().checkPermission( Permission.CREATE );

        final String[] archives = request.getParameterValues("archives");
        if (archives == null)
        {
            return createRedirectToShelvedProjectsPage();
        }

        LOGGER.info("Unshelving archived projects.");
        // Unshelving the project could take some time, so add it as a task
        Hudson.getInstance().getQueue().schedule( new UnshelveProjectTask( archives ), 0 );

        return createRedirectToMainPage();
    }

    public String formatDate( long timestamp )
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss Z" );
        return simpleDateFormat.format( new Date( timestamp ) );
    }

    private HttpResponse createRedirectToShelvedProjectsPage()
    {
        return new HttpRedirect( Hudson.getInstance().getRootUrl() + this.getUrlName() );
    }

    private HttpRedirect createRedirectToMainPage()
    {
        return new HttpRedirect( Hudson.getInstance().getRootUrl() );
    }
}
