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
