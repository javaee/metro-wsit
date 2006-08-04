/*
 The contents of this file are subject to the terms
 of the Common Development and Distribution License
 (the "License").  You may not use this file except
 in compliance with the License.
 
 You can obtain a copy of the license at
 https://jwsdp.dev.java.net/CDDLv1.0.html
 See the License for the specific language governing
 permissions and limitations under the License.
 
 When distributing Covered Code, include this CDDL
 HEADER in each file and include the License file at
 https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 add the following below this CDDL HEADER, with the
 fields enclosed by brackets "[]" replaced with your
 own identifying information: Portions Copyright [yyyy]
 [name of copyright owner]
*/
/*
 $Id: Retailer.java,v 1.1 2006-08-04 19:29:55 arungupta Exp $

 Copyright (c) 2006 Sun Microsystems, Inc.
 All rights reserved.
*/

package pricequote.retailer.server;

import pricequote.wholesaler.client.WholesalerClient;
import com.sun.xml.ws.rm.jaxws.runtime.client.ClientSession;

import javax.jws.WebService;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.namespace.QName;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        logger.log(level, "Configuring Sun client ...");
        WholesalerClient sunClient = new WholesalerClient(getSunEndpointAddress(sc), new QName(WQS_NAMESPACE_URI, getSunServiceName(sc)));
        logger.log(level, "Invoking Sun's Wholesaler ...");
        pricequote.wholesaler.client.Quote sunQuote = sunClient.getQuote(pid);
        float sunPrice = sunQuote.getPrice();
        ClientSession session = ClientSession.getSession((BindingProvider)sunClient.getBindingProvider());
        if (session != null)
            session.close();
        logger.log(level, "Sun's wholesaler response received.");

        logger.log(level, "Configuring Microsoft client ...");
        WholesalerClient msClient = new WholesalerClient(getMSEndpointAddress(sc), new QName(WQS_NAMESPACE_URI, getMSServiceName(sc)));
        logger.log(level, "Invoking Microsoft's Wholesaler ...");
        pricequote.wholesaler.client.Quote msQuote = msClient.getQuote(pid);
        session = ClientSession.getSession((BindingProvider)msClient.getBindingProvider());
        if (session != null)
            session.close();
        float msPrice = msQuote.getPrice();
        logger.log(level, "Microsoft's wholesaler response received.");

        pricequote.wholesaler.client.Quote quote = sunPrice <= msPrice ? sunQuote : msQuote;
//        pricequote.wholesaler.client.Quote quote = sunQuote;

        logger.log(level, "Got a better price from \"{0}\" Wholesaler ...",
                   (sunPrice <= msPrice ? "Sun" : "Microsoft"));

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
            endpoint = "http://localhost:8080/pricequote-wcf/wholesaler?wsdl";
        }

        return endpoint;
    }

    private String getMSServiceName(ServletContext sc) {
        String serviceName = sc.getInitParameter("wqs.wcf.serviceName");
        if (serviceName == null || serviceName.equals("")) {
            serviceName = "WholesaleService";
        }

        return serviceName;
    }

}
