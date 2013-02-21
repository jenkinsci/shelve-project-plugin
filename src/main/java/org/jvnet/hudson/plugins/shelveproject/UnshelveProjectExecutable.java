package org.jvnet.hudson.plugins.shelveproject;

import hudson.FilePath;
import hudson.model.Hudson;
import hudson.model.Queue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import java.net.*;

public class UnshelveProjectExecutable
    implements Queue.Executable
{
    private final static Logger LOGGER = Logger.getLogger( UnshelveProjectExecutable.class.getName() );

    private final String[] shelvedProjectArchiveNames;

    private final Queue.Task parentTask;

    public UnshelveProjectExecutable( Queue.Task parentTask, String[] shelvedProjectArchiveNames )
    {
        this.parentTask = parentTask;
        this.shelvedProjectArchiveNames = shelvedProjectArchiveNames;
    }

    public Queue.Task getParent()
    {
        return parentTask;
    }

    public void run()
    {
        for (String shelvedProjectArchiveName : shelvedProjectArchiveNames)
        {
            final File shelvedProjectArchive = getArchiveFile(shelvedProjectArchiveName);
            LOGGER.info( "Unshelving project [" + shelvedProjectArchiveName + "]." );
			
            try
            {
				if ( shelvedProjectArchiveName.toLowerCase().matches("(.+)\\.zip") )
				{
					new FilePath( shelvedProjectArchive ).unzip(
						new FilePath( new File( Hudson.getInstance().getRootDir(), "jobs" ) ) );
				}
				else 
				{
					new FilePath( shelvedProjectArchive ).untar(
						new FilePath( new File( Hudson.getInstance().getRootDir(), "jobs" ) ), FilePath.TarCompression.GZIP );
				}
                shelvedProjectArchive.delete();
				
				//Reload only the project that changed
				String projectFolderName = shelvedProjectArchiveName.substring(0, shelvedProjectArchiveName.length()-4);
				int indexHyphen = projectFolderName.lastIndexOf("-");
				projectFolderName = projectFolderName.substring(0, indexHyphen);
				String fileData = new String(loadData(Hudson.getInstance().getRootDir() + "\\jobs\\" + projectFolderName + "\\config.xml"));
				String newConfigFileName = Hudson.getInstance().getRootDir() + "\\jobs\\" + projectFolderName + "\\unshelveConfig.txt";
				writeToFile(fileData, newConfigFileName);
				
				FileInputStream fis = new FileInputStream(newConfigFileName);
				Hudson.getInstance().createProjectFromXML(projectFolderName, fis);
				fis.close();
				File configFile = new File(newConfigFileName);
				configFile.delete();
            }
            catch ( Exception e )
            {
                LOGGER.log( Level.SEVERE, "Could not unarchive project archive [" + shelvedProjectArchiveName + "].", e );
            }
        }
    }

    private File getArchiveFile(String shelvedProjectArchiveName) {
        // JENKINS-8759 - The archive name comes from the html form, so take a bit extra care for security reasons by
        // only accessing the archive if it exists in the directory of shevled projects.
        File shelvedProjectsDirectory = new File( Hudson.getInstance().getRootDir(), "shelvedProjects" );
        Collection<File> files = FileUtils.listFiles(shelvedProjectsDirectory, null, false);
        for (File file : files)
        {
            if (StringUtils.equals(file.getName(), shelvedProjectArchiveName))
            {
                return file;
            }
        }
        return null; // Project was already unshelved?
    }

    public long getEstimatedDuration()
    {
        return -1; // impossible to estimate duration
    }
	
	public byte[] loadData(String fileName) {
		byte[] returnVal = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
			returnVal = new byte[fis.available()];
			fis.read(returnVal);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		return returnVal;
    }
	
	boolean writeToFile(String data, String fileName) {
        try {
			java.io.FileOutputStream fos = new java.io.FileOutputStream(fileName);
               fos.write(data.getBytes());
	        fos.close();
			return true;
        } catch (Exception e) {
			LOGGER.info("*** Could not write to file " + fileName + " ***");
			e.printStackTrace(System.out);
			return false;
        }
	}

    @Override
    public String toString()
    {
        return "Unshelving Project";
    }
}
