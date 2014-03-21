package org.jvnet.hudson.plugins.shelveproject;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

public class ItemListenerImplTest
{
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    ItemListenerImpl itemListener = new ItemListenerImpl();

    @Test
    public void testOnLoaded_shouldDoNothingWhenNoProjectsInHudson()
    {

        itemListener.onLoaded();

        // no exception
    }

   @Test
    public void testOnLoaded_shouldAddShelveProjectActionToAbstractProject()
        throws IOException
    {

        jenkinsRule.createFreeStyleProject("Mickey Mouse Project");

        itemListener.onLoaded();

        final AbstractProject project = ( (AbstractProject) Hudson.getInstance().getItem( "Mickey Mouse Project" ) );
        assertEquals( "Project should have had an action added.", 1, project.getActions().size() );
        assertEquals( "Project should have had a ShelveProjectAction added.", 1,
                      project.getActions( ShelveProjectAction.class ).size() );
    }

    @Test
    public void testOnLoaded_shouldAddShelveProjectActionToAllAbstractProjects()
        throws IOException
    {
        Hudson.getInstance().getItems().add( jenkinsRule.createFreeStyleProject( "Mickey Mouse Project" ) );
        Hudson.getInstance().getItems().add( jenkinsRule.createFreeStyleProject( "Donald Duck Project" ) );

        itemListener.onLoaded();

        final AbstractProject project1 = ( (AbstractProject) Hudson.getInstance().getItem( "Mickey Mouse Project" ) );
        assertEquals( "Project should have had an action added.", 1, project1.getActions().size() );
        assertEquals( "Project should have had a ShelveProjectAction added.", 1,
                      project1.getActions( ShelveProjectAction.class ).size() );

        final AbstractProject project2 = ( (AbstractProject) Hudson.getInstance().getItem( "Donald Duck Project" ) );
        assertEquals( "Project should have had an action added.", 1, project2.getActions().size() );
        assertEquals( "Project should have had a ShelveProjectAction added.", 1,
                      project2.getActions( ShelveProjectAction.class ).size() );
    }

    @Test
    public void testOnLoaded_shouldNotAddShelveProjectActionForProjectsAlreadyWithThisAction()
        throws IOException
    {
        Hudson.getInstance().getItems().add( jenkinsRule.createFreeStyleProject( "Mickey Mouse Project" ) );

        itemListener.onLoaded();
        itemListener.onLoaded();

        final AbstractProject project1 = ( (AbstractProject) Hudson.getInstance().getItem( "Mickey Mouse Project" ) );
        assertEquals( "Project should have been added just once.", 1, project1.getActions().size() );
        assertEquals( "Project should have had a ShelveProjectAction added.", 1,
                      project1.getActions( ShelveProjectAction.class ).size() );
    }

    @Test
    public void testOnCreate_shouldAddShelveProjectActionForNewProjects()
        throws IOException
    {
        FreeStyleProject freeStyleProject = jenkinsRule.createFreeStyleProject( "Mickey Mouse Project" );

        itemListener.onCreated( freeStyleProject );

        assertEquals( "Project should have been added just once.", 1, freeStyleProject.getActions().size() );
        assertEquals( "Project should have had a ShelveProjectAction added.", 1,
                      freeStyleProject.getActions( ShelveProjectAction.class ).size() );
    }

    @Test
    public void testOnUpdate_shouldAddShelveProjectActionForUpdatedProjects()
        throws IOException
    {

        FreeStyleProject freeStyleProject = jenkinsRule.createFreeStyleProject( "Goofy Project" );

        itemListener.onUpdated( freeStyleProject );

        assertEquals( "Project should have been added just once.", 1, freeStyleProject.getActions().size() );
        assertEquals( "Project should have had a ShelveProjectAction added.", 1,
                      freeStyleProject.getActions( ShelveProjectAction.class ).size() );
    }
}
