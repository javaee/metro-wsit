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
 $Id: WholesalerClient.java,v 1.5 2006-12-13 23:32:40 arungupta Exp $
*/

package pricequote.wholesaler.client;

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

    public WholesalerPortType getBindingProvider() {
        return port;
    }
}
