package com.sun.xml.ws.installer;

/*
 * updateSharedLoaderProp.java
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Replace;

public class UpdateSharedLoaderProp extends Task {
    
    String tomcatLib;
    public void settomcatLib(String tomcatLib) {
        this.tomcatLib = tomcatLib;
    }
    
    String catalinaProps;
    public void setCatalinaProps(String catalinaProps) {
        this.catalinaProps = catalinaProps;
    }
    
    public void execute() {
        if (tomcatLib == null) {
            // Default to shared/lib
            tomcatLib = new String("${catalina.home}/shared/lib");
        }
        if (catalinaProps == null) {
            throw new BuildException("No catalinaProps set!");
        }
        //log("tomcatLib = " + tomcatLib + " catalinaProps = " + catalinaProps, Project.MSG_WARN);

        // Remove earlier instances of Metro/WSIT path modifications
        CleanSharedLoaderProp cleanerTask = new CleanSharedLoaderProp();
        cleanerTask.setCatalinaProps(catalinaProps);
        cleanerTask.setProject(this.getProject());
        cleanerTask.execute();

        //
        final String jarWildcard = new String("/*.jar");
        final String metroJars = new String(tomcatLib + jarWildcard);
        // Read properties file.
        FileInputStream propsFileStream = null;
        Properties properties = new Properties();
        try {
            propsFileStream = new FileInputStream(catalinaProps);
            if (propsFileStream != null) {
                properties.load(propsFileStream);
                propsFileStream.close();
            }
        } catch (IOException e) {
            throw new BuildException("Missing or inaccessible " + catalinaProps + " file");
        }
        
        String sharedLoader = properties.getProperty("shared.loader");
        String newSharedLoader = null;
        if (sharedLoader == null || sharedLoader.length() == 0) {
            newSharedLoader = metroJars;
        }
        else if (sharedLoader.contains(metroJars)) {
            // already has what is needed
            return;
        }
        else {
            // has values but not shared/lib/*.jars
            newSharedLoader = new String(metroJars + "," + sharedLoader);
        }

        Replace replace = new Replace();
        File propsFile = new File(catalinaProps);
        replace.setProject(this.getProject());
        replace.setFile(propsFile);
        replace.setToken("shared.loader=" + sharedLoader);
        replace.setValue("shared.loader=" + newSharedLoader);
        try {
            replace.execute();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
