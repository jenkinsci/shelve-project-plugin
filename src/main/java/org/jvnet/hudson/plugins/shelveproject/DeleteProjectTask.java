package org.jvnet.hudson.plugins.shelveproject;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Queue;
import hudson.model.ResourceList;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.SubTask;
import hudson.security.ACL;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.springframework.security.core.Authentication;

/**
 * Represents a lightweight task that will take care of Deleting shelved archives.
 * Creates a {@link DeleteProjectExecutable} in charge of the actual deletion
 */
public class DeleteProjectTask implements Queue.FlyweightTask, Queue.TransientTask {
    private final String[] shelvedProjectArchiveNames;

    /**
     * Creates a {@link DeleteProjectTask}
     *
     * @param shelvedProjectArchiveNames The list of shelve archives to delete
     */
    public DeleteProjectTask(String[] shelvedProjectArchiveNames) {
        this.shelvedProjectArchiveNames = shelvedProjectArchiveNames != null ?
                Arrays.copyOf(shelvedProjectArchiveNames, shelvedProjectArchiveNames.length) : null;
    }

    public CauseOfBlockage getCauseOfBlockage() {
        return null;
    }

    public String getName() {
        return "Deleting Project";
    }

    public String getFullDisplayName() {
        return getName();
    }

    public Queue.Executable createExecutable() {
        return new DeleteProjectExecutable(this, shelvedProjectArchiveNames);
    }

    @NonNull
    public Queue.Task getOwnerTask() {
        return this;
    }

    public void checkAbortPermission() {
    }

    public boolean hasAbortPermission() {
        return false;
    }

    public String getUrl() {
        return null;
    }

    public Collection<? extends SubTask> getSubTasks() {
        final List<SubTask> subTasks = new LinkedList<>();
        subTasks.add(this);
        return subTasks;
    }

    public ResourceList getResourceList() {
        return new ResourceList();
    }

    public String getDisplayName() {
        return getName();
    }

    @NonNull
    public Authentication getDefaultAuthentication2() {
        return ACL.SYSTEM2;
    }

    @NonNull
    @Override
    public Authentication getDefaultAuthentication2(Queue.Item item) {
        return getDefaultAuthentication2();
    }

}