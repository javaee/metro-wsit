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
package player;

import com.sun.xml.ws.developer.StreamingDataHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        StreamingDataHandler sdh = null;
        InputStream is = null;
        try {
            System.out.println("Creating and configuring stream service reference... ");
            provider.AudioStreamerService service = new provider.AudioStreamerService();
            provider.AudioStreamer port = service.getAudioStreamerPort();
            System.out.println("DONE. Press <Enter> to start audio streaming...");

            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();

            System.out.println("Geting data handler... ");
            sdh = (StreamingDataHandler) port.getWavStream();
            System.out.println("DONE");

            System.out.print("Opening data stream... ");
            is = sdh.readOnce();
            System.out.println("DONE");

            System.out.println("Starting audio player thread... ");
            sun.audio.AudioPlayer.player.start(is);
            System.out.print("Audio player thread started, waiting for it to finish... ");
            sun.audio.AudioPlayer.player.join();
            System.out.println("DONE");
        } finally {
            System.out.println("Closing data streams... ");
            is.close();
            sdh.close();
            System.out.println("DONE");
        }
    }
}
