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


/*

 * DemoClient.java

 */



package rmdemo.client;
import com.sun.xml.ws.Closeable;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This is a demo client to demonstrate the Session support
 * with WS RM implementation.
 * @author Mike Grogan
 */

public class DemoClient {

    public static void main(String[] args) throws Exception {
        RMDemoService service = new RMDemoService();
        RMDemo port = service.getRMDemoPort();

        BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(
                                       System.in));

        System.out.println("Running the RMDemo....\n"+
                "Enter as many strings as you like... \n" +
                "then press <Enter> to see the result and terminate this client\n");

        while (true) {

            String str = reader.readLine();
            if (!str.equals("")) {
                port.addString(str);

            } else {
                System.out.println(port.getResult());
                ((Closeable)port).close();
                System.exit(0);

            }

        }



    }

}

