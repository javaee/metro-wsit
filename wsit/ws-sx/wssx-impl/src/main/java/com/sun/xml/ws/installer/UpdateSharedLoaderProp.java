/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.installer;

/*
 * updateSharedLoaderProp.java
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
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
    
    @Override
    public void execute() {
        if (tomcatLib == null) {
            // Default to shared/lib
            tomcatLib = new String("${catalina.home}/shared/lib");
        }
        if (catalinaProps == null) {
            throw new BuildException("No catalinaProps set!");
        }
        //log("tomcatLib = " + tomcatLib + " catalinaProps = " + catalinaProps, Project.MSG_WARN);

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
