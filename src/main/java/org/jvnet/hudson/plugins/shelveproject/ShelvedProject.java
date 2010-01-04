package org.jvnet.hudson.plugins.shelveproject;

import java.io.File;

public class ShelvedProject
{
    private String projectName;

    private File archive;

    private long timestamp;

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName( String projectName )
    {
        this.projectName = projectName;
    }

    public File getArchive()
    {
        return archive;
    }

    public void setArchive( File archive )
    {
        this.archive = archive;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp( long timestamp )
    {
        this.timestamp = timestamp;
    }

}
