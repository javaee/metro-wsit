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
 $Id: WholesalerClient.java,v 1.3 2006-08-05 00:29:36 arungupta Exp $
*/

package pricequote.wholesaler.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.security.trust.WSTrustConstants;

/**
 * @author Arun Gupta
 */
public class WholesalerClient {
    @Resource
    WebServiceContext wsc;

    WholesalerPortType port = null;
    private static final String STS_ENDPOINT = "sts.Endpoint";
    private static final String STS_WSDL_LOCATION = "sts.WsdlLocation";
    private static final String STS_NAMESPACE = "namespace";
    private static final String STS_SERVICE_NAME = "sts.ServiceName";
    private static final String STS_PORT_NAME = "sts.PortName";

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
            configureSTS((BindingProvider)port);
        } catch (MalformedURLException e) {
            throw new WebServiceException(e);
        }
    }

    private static final void configureSTS(BindingProvider port) {
        Map<String,String> map = configureMicrosoftSTS();

        try {
            port.getRequestContext().put(WSTrustConstants.PROPERTY_SERVICE_END_POINT, new URL(map.get(STS_ENDPOINT)));
            port.getRequestContext().put(WSTrustConstants.PROPERTY_URL, new URL(map.get(STS_WSDL_LOCATION)));
            port.getRequestContext().put(WSTrustConstants.PROPERTY_SERVICE_NAME, new QName(map.get(STS_NAMESPACE), map.get(STS_SERVICE_NAME)));
            port.getRequestContext().put(WSTrustConstants.PROPERTY_PORT_NAME, new QName(map.get(STS_NAMESPACE), map.get(STS_PORT_NAME)));
        } catch (MalformedURLException e) {
            throw new WebServiceException(e);
        }
    }

    private static final Map<String,String> configureMicrosoftSTS() {
        Map<String,String> map = new HashMap<String,String>();

        map.put(STS_ENDPOINT, "http://localhost:8080/pricequote-wcf/sts");
        map.put(STS_WSDL_LOCATION, "http://localhost:8080/pricequote-wcf/sts?wsdl");
        map.put(STS_NAMESPACE, "http://tempuri.org/");
        map.put(STS_SERVICE_NAME, "SecurityTokenService");
        map.put(STS_PORT_NAME, "WSHttpBinding_ISecurityTokenService");

        return map;
    }

    private static final Map<String,String> configureSunSTS() {
        Map<String,String> map = new HashMap<String,String>();

        map.put(STS_ENDPOINT, "http://localhost:8080/pricequote/sts");
        map.put(STS_WSDL_LOCATION, "http://localhost:8080/pricequote/sts?wsdl");
        map.put(STS_NAMESPACE, "http://tempuri.org/");
        map.put(STS_SERVICE_NAME, "SecurityTokenServiceSun");
        map.put(STS_PORT_NAME, "CustomBinding_ISecurityTokenService");

        return map;
    }

    public Quote getQuote(int pid) {
        return port.getQuote(pid);
    }

    public WholesalerPortType getBindingProvider() {
        return port;
    }
}
