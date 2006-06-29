/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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

/**
 * This Ant task concats manifest information from multiple jar files.
 *
 * @author Arun Gupta
 */
public final class ConcatManifest extends Taskdef {
    private File tempdir;
    private File manifest;
    private FileSet fileset;

    public File getTempdir() {
        return tempdir;
    }

    public void setTempdir(File tempdir) {
        this.tempdir = tempdir;
    }

    public void addConfiguredFileset(FileSet fileset) {
        this.fileset = fileset;
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
