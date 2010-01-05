package org.jvnet.hudson.plugins.shelveproject;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.RootAction;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
        return "edit-delete.gif";
    }

    public String getDisplayName()
    {
        return "Shelved Projects";
    }

    public String getUrlName()
    {
        return "shelvedProjects";
    }

    @SuppressWarnings({"unchecked"})
    @Exported
    public List<ShelvedProject> getShelvedProjects()
    {
        final File shelvedProjectsDir = new File( Hudson.getInstance().getRootDir(), "shelvedProjects" );
        shelvedProjectsDir.mkdirs();

        final Collection<File> shelvedProjectsArchives =
            FileUtils.listFiles( shelvedProjectsDir, new String[]{"zip"}, false );

        List<ShelvedProject> projects = new LinkedList<ShelvedProject>();
        for ( File archive : shelvedProjectsArchives )
        {
            projects.add( getShelvedProjectFromArchive( archive ) );
        }
        return projects;
    }

    private ShelvedProject getShelvedProjectFromArchive( File archive )
    {
        ShelvedProject shelvedProject = new ShelvedProject();
        shelvedProject.setProjectName( StringUtils.substringBefore( archive.getName(), "-" ) );
        shelvedProject.setTimestamp( Long.valueOf( StringUtils.substringBetween( archive.getName(), "-", "." ) ) );
        shelvedProject.setArchive( archive );
        return shelvedProject;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public HttpResponse doUnshelveProject( @QueryParameter(required = true) String project, StaplerRequest request,
                                           StaplerResponse response )
        throws IOException, ServletException
    {
        LOGGER.info( "Unshelving archived project [" + project + "]." );
        // Unshelving the project could take some time, so add it as a task
        Hudson.getInstance().getQueue().schedule( new UnshelveProjectTask( new File( project ) ), 0 );

        return createRedirectToMainPage();
    }

    private HttpRedirect createRedirectToMainPage()
    {
        return new HttpRedirect( Hudson.getInstance().getRootUrl() );
    }
}
