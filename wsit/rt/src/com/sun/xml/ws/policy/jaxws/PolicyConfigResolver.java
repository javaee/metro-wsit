/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.policy.jaxws;

import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver.Parser;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.streaming.TidyXMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Assumes that a given XML entity holds a valid URL and returns an
 * XMLEntityResolver.Parser for that URL. An XMLEntityResolver.Parser is
 * essentially a wrapper around an XMLStreamReader.
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
public class PolicyConfigResolver implements XMLEntityResolver {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyConfigResolver.class);
    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    /**
     * Assumes that a given XML entity holds a valid URL and returns an
     * XMLEntityResolver.Parser for that URL.
     *
     * @param publicId The public ID of the entity. This parameter is ignored.
     * @param systemId The system ID of the entity. Must be a valid URL.
     * @return A parser (i.e. an XMLStreamReader) for the systemId URL.
     * @throws XMLStreamException If the XMLStreamReader could not be created
     * @throws IOException If the URL was invalid or a connection to the URL
     * failed
     * @see javax.xml.stream.XMLStreamReader
     * @see com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver.Parser
     */
    public Parser resolveEntity(final String publicId, final String systemId)
        throws XMLStreamException, IOException {
        
        LOGGER.entering(publicId, systemId);
        Parser parser = null;

        try {
            // TODO: think about using alg from http://www.w3.org/International/O-URL-code.html
            final URL systemUrl = new URL(PolicyUtils.Rfc2396.unquote(systemId));
            final InputStream input = systemUrl.openStream();
            final XMLStreamReader reader = new TidyXMLStreamReader(xmlInputFactory.createXMLStreamReader(systemId, input), input);

            parser = new Parser(systemUrl, reader);
            return parser;
        } finally {
            LOGGER.exiting(parser);
        }
    }
}
