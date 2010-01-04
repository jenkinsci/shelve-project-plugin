package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.Hudson;
import org.apache.commons.io.FileUtils;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ShelvedProjectsActionTest
    extends HudsonTestCase
{
    private ShelvedProjectsAction shelvedProjectsAction;

    private File shelvedProjectsDir;

    public void setUp()
        throws Exception
    {
        super.setUp();

        shelvedProjectsAction = new ShelvedProjectsAction();

        shelvedProjectsDir = new File( Hudson.getInstance().getRootDir(), "shelvedProjects" );
        shelvedProjectsDir.mkdirs();
    }

    public void tearDown()
        throws Exception
    {
        super.tearDown();

        shelvedProjectsDir.delete();
    }

    public void testGetShelvedProjects_shouldReturnEmptyListWhenNoArchivedProjectsFound()
    {
        assertTrue( "No archived projects should have been found.",
                    shelvedProjectsAction.getShelvedProjects().isEmpty() );
    }

    public void testGetShelvedProjects_shouldReturnShelvedProject()
        throws IOException
    {
        FileUtils.touch( new File( shelvedProjectsDir, "blackMesaProject-1262634114828.zip" ) );

        List<ShelvedProject> shelvedProjects = shelvedProjectsAction.getShelvedProjects();

        assertEquals( "Should have found one archived projects.", 1, shelvedProjects.size() );
        assertEquals( "Should have found one archived projects.", "blackMesaProject",
                      shelvedProjects.get( 0 ).getProjectName() );
        assertEquals( "Should have found one archived projects.", 1262634114828L,
                      shelvedProjects.get( 0 ).getTimestamp() );
        assertEquals( "Should have found one archived projects.", "blackMesaProject-1262634114828.zip",
                      shelvedProjects.get( 0 ).getArchive().getName() );
    }

    public void testGetShelvedProjects_shouldReturnMultipleArchivedProjects()
        throws IOException
    {
        FileUtils.touch( new File( shelvedProjectsDir, "appretureScience-1262634014828.zip" ) );
        FileUtils.touch( new File( shelvedProjectsDir, "blackMesaProject-1262634114828.zip" ) );

        List<ShelvedProject> shelvedProjects = shelvedProjectsAction.getShelvedProjects();

        assertEquals( "Should have found two archived projects.", 2, shelvedProjects.size() );
    }
}
