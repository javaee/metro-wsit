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
 $Id: Wholesaler.java,v 1.1 2006-08-04 19:29:57 arungupta Exp $

 Copyright (c) 2006 Sun Microsystems, Inc.
 All rights reserved.
*/

package pricequote.wholesaler.server;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Arun Gupta
 */
@WebService(endpointInterface = "pricequote.wholesaler.server.WholesalerPortType", wsdlLocation = "WEB-INF/wsdl/wholesaler.wsdl")
public class Wholesaler implements WholesalerPortType {

    @Resource
    WebServiceContext context;

    public Quote getQuote(int i) {
        Quote response = new Quote();
        response.setPrice(PRICES[i % 4]);

        ServletContext servletContext = (ServletContext)context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
        if (servletContext != null) {

            String carName = "/images/" + carname(i) + ".jpg";

            System.out.println("Car name is: " + carName);
            InputStream is = servletContext.getResourceAsStream(carName);
            try {
                BufferedImage bi = ImageIO.read(is);
                response.setPhoto(bi);
                System.out.println(getClass().getName() + ": Added the photo");
            } catch (IOException e) {
                throw new WebServiceException(e);
            }
        }

        return response;
    }

    private String carname(int pid) {
        switch (pid % 4) {
            case 1:
                return "AM-Vantage-2k6";
            case 2:
                return "BMW-M3-2k6";
            case 3:
                return "MB-SLR-2k6";
            case 0:
            default:
                return "Porsche-911-2k6";
        }
    }

    private static final float[] PRICES = {
        (float)71834.95,
        (float)83450.00,
        (float)75640.00,
        (float)90990.99
    };
}
