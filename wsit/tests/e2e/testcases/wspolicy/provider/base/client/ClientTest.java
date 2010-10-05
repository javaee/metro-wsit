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

package wspolicy.provider.base.client;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.rx.rm.runtime.RmRuntimeVersion;
import com.sun.xml.ws.rx.testing.PacketFilter;
import com.sun.xml.ws.rx.testing.PacketFilteringFeature;
import junit.framework.TestCase;

/**
 *
 * @author Fabian Ritzmann
 */
public class ClientTest extends TestCase {

    public static class TestFilter extends PacketFilter {

        private static volatile RmRuntimeVersion version;

        public Packet filterClientRequest(Packet request) throws Exception {
            if (version == null) {
                version = getRmVersion();
            }
            else {
                assertEquals(version, getRmVersion());
            }
            return request;
        }

        public Packet filterServerResponse(Packet response) throws Exception {
            if (version == null) {
                version = getRmVersion();
            }
            else {
                assertEquals(version, getRmVersion());
            }
            return response;
        }

        public static RmRuntimeVersion getVersion() {
            return version;
        }
    }

    public void testProvider() {
        PacketFilteringFeature filterFeature = new PacketFilteringFeature(TestFilter.class);
        EchoService echoService = new EchoService();
        Echo echo = echoService.getEchoPort(filterFeature);
        String result = echo.echo("Hello");
        assertEquals("Helloellolloloo", result);
        // Make sure that the message exchange actually used the policy configuration
        RmRuntimeVersion version = TestFilter.getVersion();
        assertEquals(RmRuntimeVersion.WSRM200702, version);
    }
}