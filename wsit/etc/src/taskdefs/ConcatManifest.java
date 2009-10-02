/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package taskdefs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Taskdef;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;

/**
 * This Ant task concats manifest information from multiple jar files.
 *
 * @author Arun Gupta
 * @author Fabian Ritzmann
 */
public final class ConcatManifest extends Taskdef {
    private File tempdir;
    private File manifest;
    private FileSet fileset;
    private PatternSet expandPatterns;

    public File getTempdir() {
        return tempdir;
    }

    public void setTempdir(File tempdir) {
        this.tempdir = tempdir;
    }

    public void addConfiguredFileset(FileSet fileset) {
        this.fileset = fileset;
    }

    public void addConfiguredPatternset(PatternSet patternSet) {
        this.expandPatterns = patternSet;
    }

    public File getManifest() {
        return manifest;
    }

    public void setManifest(File manifest) {
        this.manifest = manifest;
    }

    @Override
    public void execute() throws BuildException {
        if (!getManifest().isFile())
            throw new BuildException("manifest must be a file.");

        Expand expand = newExpand();
        if (!getTempdir().isDirectory())
            throw new BuildException("tempdir must be a directory.");
        expand.setDest(tempdir);
        if (this.expandPatterns != null) {
            expand.addPatternset(this.expandPatterns);
        }

        final File m = new File(tempdir + File.separator + "META-INF/MANIFEST.MF");
        Delete delete = newDelete();
        delete.setFile(m);

        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(getManifest(), true));
            DirectoryScanner ds = fileset.getDirectoryScanner(this.project);
            for (String file : ds.getIncludedFiles()) {
                delete.execute();
                expand.setSrc(new File(ds.getBasedir() + File.separator + file));
                expand.execute();
                writeManifest(bw, file, m);
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private void writeManifest(BufferedWriter bw, String file, File m) throws IOException {
        bw.write("\nName: " + file + "\n");
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(m));
        } catch (FileNotFoundException e) {
            log("No manifest found for " + file, Project.MSG_WARN);

            // ignore this exception and continue
            return;
        }
        String line = br.readLine();
        log("\nManifest for " + file, Project.MSG_DEBUG);
        while (line != null) {
            log(line, Project.MSG_DEBUG);
            bw.write(line + "\n");
            line = br.readLine();
        }
        bw.write("\n");
    }

    private Delete newDelete() {
        Delete delete = new Delete();
        delete.setProject(this.project);
        delete.setFailOnError(false);
        delete.setQuiet(true);

        return delete;
    }

    private Expand newExpand() {
        Expand expand = new Expand();
        expand.setProject(this.project);

        return expand;
    }
}
