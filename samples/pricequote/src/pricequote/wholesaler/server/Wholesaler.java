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
 $Id: Wholesaler.java,v 1.7 2010-10-21 14:28:47 snajper Exp $
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
