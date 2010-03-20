/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.xml.ws.security.opt.impl.util;



import javax.xml.stream.StreamFilter;
import org.xml.sax.InputSource;
import javax.xml.ws.WebServiceException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

/**
 * <p>A factory to create XML and FI parsers.</p>
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class XMLStreamReaderFactory {
    
    /**
     * StAX input factory shared by all threads.
     */
    static final XMLInputFactory xmlInputFactory;
    
    /**
     * FI stream reader for each thread.
     */
    static final ThreadLocal fiStreamReader = new ThreadLocal();
    
    /**
     * Zephyr's stream reader for each thread.
     */
    static final ThreadLocal<XMLStreamReader> xmlStreamReader = new ThreadLocal<XMLStreamReader>();
    
    static {
        // Use StAX pluggability layer to get factory instance
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        
        try {
            // Turn OFF internal factory caching in Zephyr -- not thread safe
            xmlInputFactory.setProperty("reuse-instance", Boolean.FALSE);
        } catch (IllegalArgumentException e) {
            // falls through
        }
    }
    
    // -- XML ------------------------------------------------------------
    
    /**
     * Returns a fresh StAX parser created from an InputSource. Use this
     * method when concurrent instances are needed within a single thread.
     *
     * TODO: Reject DTDs?
     */
    public static XMLStreamReader createFreshXMLStreamReader(InputSource source,
            boolean rejectDTDs) {
        try {
            synchronized (xmlInputFactory) {
                // Char stream available?
                if (source.getCharacterStream() != null) {
                    return xmlInputFactory.createXMLStreamReader(
                            source.getSystemId(), source.getCharacterStream());
                }
                
                // Byte stream available?
                if (source.getByteStream() != null) {
                    return xmlInputFactory.createXMLStreamReader(
                            source.getSystemId(), source.getByteStream());
                }
                
                // Otherwise, open URI
                return xmlInputFactory.createXMLStreamReader(source.getSystemId(),
                        new URL(source.getSystemId()).openStream());
            }
        } catch (Exception e) {
            throw new WebServiceException("stax.cantCreate",e);
        }
    }
    
    /**
     * This factory method would be used for example when caller wants to close the stream.
     */
    public static XMLStreamReader createFreshXMLStreamReader(String systemId, InputStream stream) {
        try {
            synchronized (xmlInputFactory) {
                // Otherwise, open URI
                return xmlInputFactory.createXMLStreamReader(systemId,
                        stream);
            }
        } catch (Exception e) {
            throw new WebServiceException("stax.cantCreate",e);
        }
    }
    
    /**
     * This factory method would be used for example when caller wants to close the stream.
     */
    public static XMLStreamReader createFreshXMLStreamReader(String systemId, Reader reader) {
        try {
            synchronized (xmlInputFactory) {
                // Otherwise, open URI
                return xmlInputFactory.createXMLStreamReader(systemId,
                        reader);
            }
        } catch (Exception e) {
            throw new WebServiceException("stax.cantCreate",e);
        }
    }
    
    
    public static XMLStreamReader createFilteredXMLStreamReader(XMLStreamReader reader,StreamFilter filter){
        try {
            synchronized (xmlInputFactory) {
                // Otherwise, open URI
                return xmlInputFactory.createFilteredReader(reader,filter) ;
            }
        } catch (Exception e) {
            throw new WebServiceException("stax.cantCreate",e);
        }
    }
    
   
    
}
