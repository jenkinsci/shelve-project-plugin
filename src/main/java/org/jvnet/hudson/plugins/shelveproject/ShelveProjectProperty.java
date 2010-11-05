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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
    public Collection<? extends Action> getJobActions(AbstractProject<?, ?> job)
    {
        final List<Action> actions = new LinkedList<Action>();
        actions.add(new ShelveProjectAction( job ));
        return actions;
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
