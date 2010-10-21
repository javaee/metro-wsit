/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

/*
 $Id: Retailer.java,v 1.10 2010-10-21 15:33:33 snajper Exp $
*/

package pricequote.retailer.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import pricequote.wholesaler.client.WholesalerClient;

/**
 * @author Arun Gupta
 */
@WebService(endpointInterface = "pricequote.retailer.server.RetailerPortType", wsdlLocation = "WEB-INF/wsdl/retailer.wsdl")
public class Retailer implements RetailerPortType {
    @Resource
    WebServiceContext wsc;

    private static final Logger logger = Logger.getAnonymousLogger();
    private static final Level level = Level.INFO;
    private static final String WQS_NAMESPACE_URI = "http://example.org/wholesaler";

    public Quote getPrice(int pid) {
        logger.log(level, "Retailer.getListPrice invoked");

        ServletContext sc = (ServletContext)wsc.getMessageContext().get(MessageContext.SERVLET_CONTEXT);

        logger.log(level, "Configuring WSIT client ...");
        WholesalerClient sunClient = new WholesalerClient(getSunEndpointAddress(sc), new QName(WQS_NAMESPACE_URI, getSunServiceName(sc)));
        logger.log(level, "Invoking WSIT's Wholesaler ...");
        pricequote.wholesaler.client.Quote sunQuote = sunClient.getQuote(pid);
        float sunPrice = sunQuote.getPrice();
        logger.log(level, "Sun's wholesaler response received.");
        sunClient.close();

        logger.log(level, "Configuring WSIT#2 client ...");
        WholesalerClient msClient = new WholesalerClient(getMSEndpointAddress(sc), new QName(WQS_NAMESPACE_URI, getMSServiceName(sc)));
        logger.log(level, "Invoking WSIT#2's Wholesaler ...");
        pricequote.wholesaler.client.Quote msQuote = msClient.getQuote(pid);
        float msPrice = msQuote.getPrice();
        logger.log(level, "WSIT#2's wholesaler response received.");
        msClient.close();

        pricequote.wholesaler.client.Quote quote = sunPrice <= msPrice ? sunQuote : msQuote;
        logger.log(level, "Got a better price from \"{0}\" Wholesaler ...",
                   (sunPrice <= msPrice ? "WSIT" : "WSIT#2"));

        // TODO: Calculate the price to be returned back
        // TODO: based upon user's identity and gross margin
        // TODO: For now, the best price from wholesaler is
        // TODO: returned to the consumer

        Quote response = new Quote();
        response.setPrice(quote.getPrice());
        response.setPhoto(quote.getPhoto());

        logger.log(level, "Returning the response.");

        return response;
    }

    private String getSunEndpointAddress(ServletContext sc) {
        String endpoint = sc.getInitParameter("wqs.wsit.endpoint");
        if (endpoint == null || endpoint.equals(""))
            endpoint = "http://localhost:8080/pricequote/wholesaler?wsdl";

        return endpoint;
    }

    private String getSunServiceName(ServletContext sc) {
        String serviceName = sc.getInitParameter("wqs.wsit.serviceName");
        if (serviceName == null || serviceName.equals(""))
            serviceName = "WholesalerQuoteService";

        return serviceName;
    }

    private String getMSEndpointAddress(ServletContext sc) {
        String endpoint = sc.getInitParameter("wqs.wcf.endpoint");
        if (endpoint == null || endpoint.equals("")) {
            //endpoint = "http://131.107.72.15/Wholesaler/WholesalerService.svc?wsdl";
            endpoint = "http://localhost:8080/pricequote-wcf/wholesaler?wsdl";
        }

        return endpoint;
    }

    private String getMSServiceName(ServletContext sc) {
        String serviceName = sc.getInitParameter("wqs.wcf.serviceName");
        if (serviceName == null || serviceName.equals("")) {
            //serviceName = "WholesalerService";
            serviceName = "WholesaleService";
        }

        return serviceName;
    }
}
