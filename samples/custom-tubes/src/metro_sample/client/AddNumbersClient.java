/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package metro_sample.client;

import java.io.IOException;

public class AddNumbersClient {

    private static final int[] NUMBER_1 = new int[]{10, 20, 30};
    private static final int[] NUMBER_2 = new int[]{20, 40, 60};
    private static final int[] EXPECTED_RESULT = new int[]{30, 60, 90};

    private final String name;
    private final AddNumbersPortType wsProxy;

    private AddNumbersClient(String clientName, AddNumbersService service) {
        name = clientName;
        wsProxy = service.getAddNumbersPort();
    }

    private void testAddNumbers(int n1, int n2, int expectedResult) {
        System.out.printf("[ %s ]: Adding numbers %d + %d\n", name, n1, n2);
        try {
            int result = wsProxy.addNumbers(n1, n2);
            if (result == expectedResult) {
                System.out.printf("[ %s ]: Result as expected: %d\n", name, result);
            } else {                
                System.out.printf("[ %s ]: Unexpected result: %d    Expected: %d\n", name, result, expectedResult);
            }
        } catch (Exception ex) {
            System.err.printf("[ %s ]: Exception occured:\n", name);
            ex.printStackTrace(System.err);
        }
        System.out.printf("\n\n");
    }

    private void releaseWsProxy() {
        try {
            System.out.printf("[ %s ]: Closing WS proxy...", name);
            ((java.io.Closeable) wsProxy).close();
            System.out.println("DONE.");
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {
        System.out.printf("Custom tubes sample application\n");
        System.out.printf("===============================\n\n");

        AddNumbersService service = new AddNumbersService();

        AddNumbersClient client = new AddNumbersClient("Client", service);
        
        for (int i = 0; i < EXPECTED_RESULT.length; i++) {
            client.testAddNumbers(NUMBER_1[i], NUMBER_2[i], EXPECTED_RESULT[i]);
        }

        client.releaseWsProxy();
    }
}
