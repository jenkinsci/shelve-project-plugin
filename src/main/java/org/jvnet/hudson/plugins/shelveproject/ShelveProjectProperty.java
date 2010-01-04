package org.jvnet.hudson.plugins.shelveproject;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.TopLevelItem;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Necessary so {@link ShelveProjectAction} can be added to the project,
 * which in turn displays a link on the project page.
 *
 * @author Ash Lux
 */
public class ShelveProjectProperty
    extends JobProperty<AbstractProject<?, ?>>
{
    @Override
    public Action getJobAction( AbstractProject<?, ?> job )
    {
        return new ShelveProjectAction( job );
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Extension
    public static final class DescriptorImpl
        extends JobPropertyDescriptor
    {
        public boolean isApplicable( Class<? extends Job> jobType )
        {
            return TopLevelItem.class.isAssignableFrom( jobType );
        }

        public String getDisplayName()
        {
            return null;
        }

        public ShelveProjectProperty newInstance( StaplerRequest req, JSONObject formData )
            throws FormException
        {
            return new ShelveProjectProperty();
        }
    }

}
