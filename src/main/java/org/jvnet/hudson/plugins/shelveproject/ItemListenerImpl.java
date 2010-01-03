package org.jvnet.hudson.plugins.shelveproject;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ash Lux
 */
@Extension
public class ItemListenerImpl
    extends ItemListener
{
    private static final Logger LOGGER = Logger.getLogger( ItemListenerImpl.class.getName() );

    @Override
    public void onLoaded()
    {
        for ( AbstractProject<?, ?> project : Hudson.getInstance().getAllItems( AbstractProject.class ) )
        {
            addShelveProjectProperty( project );
        }
    }

    @Override
    public void onCreated( Item item )
    {
        if ( item instanceof AbstractProject )
        {
            AbstractProject project = (AbstractProject) item;
            addShelveProjectProperty( project );
        }
    }

    private void addShelveProjectProperty( AbstractProject<?, ?> project )
    {
        try
        {
            if ( project.getProperty( ShelveProjectProperty.class ) == null )
            {
                project.addProperty( new ShelveProjectProperty() );
            }
        }
        catch ( IOException e )
        {
            LOGGER.log( Level.SEVERE, "Failed to persist " + project, e );
        }
    }
}
