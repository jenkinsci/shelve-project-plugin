package org.jvnet.hudson.plugins.shelveproject;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import hudson.model.Queue;
import jenkins.model.Jenkins;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jvnet.hudson.plugins.shelveproject.ShelveProjectExecutable.*;

/**
 * A {@link Queue.Executable} that will take care of unshelving projects.
 * <p>
 * Selected projects will be copied back under $JENKINS_HOME/jobs and the model will be reloaded.
 */
public class UnshelveProjectExecutable implements Queue.Executable {
  private final static Logger LOGGER = Logger.getLogger(UnshelveProjectExecutable.class.getName());

  private final String[] shelvedProjectArchiveNames;

  private final Queue.Task parentTask;

  /**
   * Creates a new {@link UnshelveProjectExecutable}
   *
   * @param parentTask                 The task from which the executable was created. Most likely {@link UnshelveProjectTask}
   * @param shelvedProjectArchiveNames The list of shelve archives to treat
   */
  public UnshelveProjectExecutable(Queue.Task parentTask, String[] shelvedProjectArchiveNames) {
    this.parentTask = parentTask;
    this.shelvedProjectArchiveNames = shelvedProjectArchiveNames != null ?
            Arrays.copyOf(shelvedProjectArchiveNames, shelvedProjectArchiveNames.length) : null;
  }

  @NonNull
  public Queue.Task getParent() {
    return parentTask;
  }

  public void run() {
    for (String shelvedProjectArchiveName : shelvedProjectArchiveNames) {
      final File shelvedProjectArchive = DeleteProjectExecutable.getArchiveFile(shelvedProjectArchiveName);
      LOGGER.info("Unshelving project [" + shelvedProjectArchiveName + "].");
      boolean correctlyExploded;
      try {
        if (ARCHIVE_FILE_EXTENSION.equals(FilenameUtils.getExtension(shelvedProjectArchiveName))) {
          correctlyExploded = explode(shelvedProjectArchive);
          if (correctlyExploded) {
            Files.delete(ShelvedProject.getMetadataFileFromArchive(shelvedProjectArchive));
          }
        } else {
          legacyExplode(shelvedProjectArchive);
          correctlyExploded = true;
        }
        if (correctlyExploded) {
          Files.delete(shelvedProjectArchive.toPath());
        } else {
          LOGGER.log(Level.INFO, "Skipping deletion of the backup at " + shelvedProjectArchiveName);
        }
        Jenkins.get().reload();
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Could not unarchive project archive [" + shelvedProjectArchiveName + "].", e);
      }
    }
  }

  private boolean explode(File shelvedProjectArchive) throws IOException, InterruptedException {
    File rootDir = Jenkins.get().getRootDir();
    Properties metadata = ShelvedProject.loadMetadata(shelvedProjectArchive);
    String projectPathProperty = metadata.getProperty(PROJECT_PATH_PROPERTY);
    Path projectPath = rootDir.toPath().resolve("jobs").resolve(projectPathProperty);
    boolean isCompressedArchive = Boolean.parseBoolean(metadata.getProperty(ARCHIVE_COMPRESSION, "false"));
    if (Files.exists(projectPath)) {
      LOGGER.log(Level.INFO, "A project exist for the given path " + projectPathProperty + "...skipping");
      return false;
    }
    new FilePath(shelvedProjectArchive).untar(
            new FilePath(new File(rootDir, "jobs")), isCompressedArchive ? FilePath.TarCompression.GZIP : FilePath.TarCompression.NONE);
    return true;
  }

  private void legacyExplode(File shelvedProjectArchive) throws IOException, InterruptedException {
    new FilePath(shelvedProjectArchive).unzip(
            new FilePath(new File(Jenkins.get().getRootDir(), "jobs")));
  }


  public long getEstimatedDuration() {
    return -1; // impossible to estimate duration
  }

  @Override
  public String toString() {
    return "Unshelving Project";
  }
}