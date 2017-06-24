/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

    private static enum Composition {
        /*
        Lady Gaga - Just Dance (Dance)
        Lily Allen - The Fear (Alternative)
        Britney Spears - Womanizer (Pop Rock)
        Metallica - ... (Rock Ballad)
        Carlos Santana & Buddy Guy at Montreux Jazz Festival (Electric Jazz)
        J.S.Bach - Air (Orchestral Suite)
        Manowar - Kings Of Metal (Heavy Metal)
         */

        DANCE(1, "Lady Gaga", "Just Dance", "Dance", "dance"),
        ALTERNATIVE(2, "Lily Allen", "The Fear", "Alternative Rock", "alternative"),
        POP_ROCK(3, "Britney Spears", "Womanizer", "Pop", "pop"),
        ROCK_BALLAD(4, "Metallica", "Turn The Page", "Rock Ballad", "ballad"),
        HEAVY_METAL(5, "Manowar", "Kings Of Metal", "Heavy Metal", "metal"),
        JAZZ(6, "Carlos Santana & Buddy Guy", "Montreux Jazz Festival", "Jazz", "jazz"),
        ORCHESTRAL(7, "J.S.Bach", "Air", "Classical", "classical");

        private static Composition getById(int chosenId) {
            for (Composition c : values()) {
                if (c.id == chosenId) {
                    return c;
                }
            }

            return null;
        }
        //
        final int id;
        final String artist;
        final String songTitle;
        final String genre;
        final String fileName;

        private Composition(int id, String artist, String songTitle, String genre, String fileName) {
            this.id = id;
            this.artist = artist;
            this.songTitle = songTitle;
            this.genre = genre;
            this.fileName = fileName;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        StreamingDataHandler sdh = null;
        InputStream is = null;
        try {
            System.out.println("Choose genre to start audio streaming:");

            Composition chosen;
            do {
                for (Composition c : Composition.values()) {
                    System.out.println(String.format("%d. %s", c.id, c.genre));
                }

                int chosenId = new Scanner(System.in).nextInt();
                chosen = Composition.getById(chosenId);

                if (chosen == null) {
                    System.out.println("\n\nIncorrect choice. Please select a proper number in range:");
                }
            } while (chosen == null);

            System.out.println(String.format("\n\nLoading %s - %s :\n\n", chosen.artist, chosen.songTitle));

            System.out.println("Creating and configuring stream service reference... ");
            provider.AudioStreamerService service = new provider.AudioStreamerService();
            provider.AudioStreamer port = service.getAudioStreamerPort();

            System.out.println("DONE\nGeting data handler... ");
            sdh = (StreamingDataHandler) port.getWavStream(chosen.fileName);
            System.out.println("DONE\nOpening data stream... ");
            is = sdh.readOnce();
            System.out.println("DONE\nStarting audio player thread... ");
            sun.audio.AudioPlayer.player.start(is);
            System.out.println("Audio player thread started, waiting for it to finish... ");
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
