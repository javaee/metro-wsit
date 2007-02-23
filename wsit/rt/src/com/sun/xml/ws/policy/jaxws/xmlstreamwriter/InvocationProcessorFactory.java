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
package com.sun.xml.ws.policy.jaxws.xmlstreamwriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * The interface provides API contract for {@link InvocationProcessor} factory 
 * implementations. Implementations of this interface may be passed into {@link EnhancedXmlStreamWriterProxy}
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public interface InvocationProcessorFactory {
    
    /**
     * Factory method creates {@link InvocationProcessor} instance that implements
     * additional {@link XMLStreamWriter} feature or enhancement.
     *
     * @param writer underlying {@link XMLStreamWriter} instance that should be enhanced 
     * with the new feature(s).
     *
     * @return newly created {@link InvocationProcessor} instance.
     * 
     * @throws XMLStreamException in case of any problems with creation of
     *         new {@link InvocationProcessor} instance.
     */
    InvocationProcessor createInvocationProcessor(XMLStreamWriter writer) throws XMLStreamException;
}
