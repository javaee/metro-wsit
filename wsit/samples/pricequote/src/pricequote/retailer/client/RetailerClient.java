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
 $Id: RetailerClient.java,v 1.1 2006-08-04 19:29:55 arungupta Exp $

 Copyright (c) 2006 Sun Microsystems, Inc.
 All rights reserved.
*/

package pricequote.retailer.client;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import pricequote.retailer.client.ImageServingServlet;

/**
 * @author Arun Gupta
 */
public class RetailerClient {
    private static final String ENDPOINT = "http://localhost:8080/pricequote/retailer";
    private static final String WSDL_LOCATION = ENDPOINT + "?wsdl";
    private static final QName SERVICE = new QName("http://example.org/retailer", "RetailerQuoteService");
    private static int pid = 10;
    private static String imageLocation;

    public static void main(String[] args) {
        String endpoint = System.getProperty("endpoint");
        String pid = System.getProperty("pid");
        Quote quote = getQuote(endpoint, pid);
        displayPhotoAndPrice(quote);
    }

    /**
     * Invokes the web service.
     */
    public static Quote getQuote(String endpoint, String spid) {
        System.out.println("Got endpoint address: " + endpoint);
        System.out.println("Got product id: " + spid);
        RetailerQuoteService service;
        try {
            service = new RetailerQuoteService(new URL(WSDL_LOCATION), SERVICE);
        } catch (MalformedURLException e) {
            throw new WebServiceException(e);
        }
        if (endpoint == null || endpoint.equals("")) {
            endpoint = ENDPOINT;
        }

        if (spid != null && spid != "")
            pid = Integer.valueOf(spid);

        RetailerPortType port = service.getRetailerPort();
        System.out.printf("Using endpoint address: %s\n", endpoint);
        ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

        Quote quote = port.getPrice(pid);
        return quote;
    }

    private static final void displayPhotoAndPrice(Quote quote) {
        if (quote.getPhoto() != null) {
            try {
                String carName = "car" + (pid%2==0 ? "0":"1") + ".jpg";
                File file = new File(carName);
                ImageIO.write((BufferedImage)quote.getPhoto(), "jpeg", file);
                System.out.println("Photo copied to " + carName);
                imageLocation = file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No photo received.");
        }
        System.out.println("Quoted price: " + quote.getPrice());
    }

    /**
     * Persists an image to a file and returns the URL.
     */
    public static String getPhoto(BufferedImage img) throws IOException {
        File file = new File(ImageServingServlet.TEMP_DIR,(iota++)+".jpg");
        ImageIO.write(img, "jpeg", file);
        return file.getName();
    }

    private static int iota = 1;
}
