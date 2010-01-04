package org.jvnet.hudson.plugins.shelveproject;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.RootAction;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
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
    public List<String> getShelvedProjects()
    {
        final File shelvedProjectsDir = new File( Hudson.getInstance().getRootDir(), "shelvedProjects" );
        final Collection<File> shelvedProjectsArchives =
            FileUtils.listFiles( shelvedProjectsDir, new String[]{"tar.gz"}, false );

        List<String> projects = new LinkedList<String>();
        for ( File archive : shelvedProjectsArchives )
        {
            projects.add( StringUtils.substringBefore( archive.getName(), "-" ) );
        }
        return projects;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public HttpResponse doUnshelveProject( @QueryParameter(required = true) String project )
        throws IOException, ServletException
    {
        // TODO: Multiple shelved projects w/ same name
        // TODO: Project already exists, (recreated?)
        LOGGER.info( "Unshelving project [" + project + "]." );

        return createRedirectToMainPage();
    }

    private HttpRedirect createRedirectToMainPage()
    {
        return new HttpRedirect( Hudson.getInstance().getRootUrl() );
    }
}
