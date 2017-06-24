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

/*
 $Id: WholesalerClient.java,v 1.11 2010-10-21 15:33:34 snajper Exp $
*/

package pricequote.wholesaler.client;

import java.io.Closeable;
import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Arun Gupta
 */
public class WholesalerClient {
    @Resource
    WebServiceContext wsc;

    WholesalerPortType port = null;
    public static void main(String[] args) {
        String endpoint = "http://localhost:8080/pricequote/wholesaler?wsdl";
        String serviceName = "WholesalerQuoteService";

        String wqs = System.getProperty("wqs");
        if (wqs != null && wqs.equals("ms")) {
            System.out.println("Setting Wholesale Quote Service #2 endpoints");
            String ep = System.getProperty("wqs.endpoint");
            if (ep != null && !ep.equals(""))
                endpoint = ep;
            String sn = System.getProperty("wqs.serviceName");
            if (sn != null && !sn.equals(""))
                serviceName = sn;
        }

        System.out.println("Using endpoints ...");
        System.out.println("endpoint: " + endpoint);
        System.out.println("serviceName: " + serviceName);
        System.out.println(new WholesalerClient(endpoint, new QName("http://example.org/wholesaler", serviceName)).getQuote(10).getPrice());
    }

    public WholesalerClient(String endpoint, QName name) {
        try {
            WholesalerQuoteService service = new WholesalerQuoteService(new URL(endpoint), name);
            port = service.getWholesalerPort();
        } catch (MalformedURLException e) {
            throw new WebServiceException(e);
        }
    }

    public Quote getQuote(int pid) {
        return port.getQuote(pid);
    }

    public void close() {
        if (port != null) {
            ((Closeable) port).close();
            port = null;
        }
    }
}
