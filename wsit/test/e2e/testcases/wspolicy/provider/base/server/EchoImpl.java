/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package wspolicy.provider.base.server;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

@ServiceMode(value=Service.Mode.PAYLOAD)
@WebServiceProvider(wsdlLocation="WEB-INF/wsdl/EchoService.wsdl")
public class EchoImpl implements Provider<Source> {

    private static final String XSLT = "<?xml version=\"1.0\"?>"
                                     + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                                     + "  <xsl:output method=\"text\" omit-xml-declaration=\"yes\"/>"
                                     + "  <xsl:for-each select=\"echo\">"
                                     + "    <xsl:value-of select=\"arg0\"/>"
                                     + "  </xsl:for-each>"
                                     + "</xsl:stylesheet>";
    private final Transformer transformer;

    public EchoImpl() {
        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer(new StreamSource(new StringReader(XSLT)));
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(EchoImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new WebServiceException(ex);
        }
    }
    
    public Source invoke(Source request) {
        try {
            final StringWriter writer = new StringWriter();
            final Result output = new StreamResult(writer);
            transformer.transform(request, output);
            final String input = writer.toString();
            final String result = echo(input);
            final String body = "<ns:echoResponse xmlns:ns=\"http://server.wsdl.provider.wspolicy/\">"
                              + "  <return>" + result + "</return>"
                              + "</ns:echoResponse>";
            final Source response = new StreamSource(new ByteArrayInputStream(body.getBytes()));
            return response;
        } catch (TransformerException ex) {
            Logger.getLogger(EchoImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new WebServiceException(ex);
        }
    }

    private String echo(String yodel) {
        final StringBuffer holladrio = new StringBuffer();
        final int l = yodel.length();
        for (int i = 0; i <  l; i++) {
            holladrio.append(yodel.substring(i, l));
        }
        return holladrio.toString();
    }

}
