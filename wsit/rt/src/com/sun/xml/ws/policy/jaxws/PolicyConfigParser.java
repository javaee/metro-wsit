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

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.xml.sax.SAXException;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import com.sun.xml.ws.wsdl.parser.XMLEntityResolver.Parser;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;

/**
 * Reads a policy configuration file and returns the WSDL model generated from it.
 */
public final class PolicyConfigParser {

    private static final PolicyLogger logger = PolicyLogger.getLogger(PolicyConfigParser.class);
    private static final String CONFIG_FILE_NAME="wsit.xml";
    private static final String SERVLET_CONTEXT_CLASSNAME = "javax.servlet.ServletContext";
    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    /**
     * Reads a WSDL file from META-INF or WEB-INF/wsit.xml, parses it
     * and returns a WSDLModel.
     *
     * @param container May hold the servlet context if run inside a web container
     * @return A WSDLModel populated from the WSDL file
     */
    public static WSDLModel parse(Container container) throws PolicyException {
        logger.entering("parse", container);
        WSDLModel model = null;
        try {
            UrlBufferStruct urlBuffer = null;
            Object context = null;
            try {
                Class<?> contextClass = Class.forName(SERVLET_CONTEXT_CLASSNAME);
                if (null!=container) {
                    context = container.getSPI(contextClass);
                }
            } catch (ClassNotFoundException e) {
                logger.fine("parse", "Did not find class " + SERVLET_CONTEXT_CLASSNAME + ". We are apparently not running in a container.");
            }
            logger.finest("parse", "context = " + context);
            if (context != null) {
                urlBuffer = loadFromContext(context);
            }
            else {
                // We are not running inside a web container, load file from META-INF
                urlBuffer = loadFromClasspath(CONFIG_FILE_NAME);
            }
            if (urlBuffer != null) {
                model = parse(urlBuffer.getUrl(), urlBuffer.getBuffer());
            }
            return model;
        } finally {
            logger.exiting("parse", model);
        }
    }
    
    
    /**
     * Reads WSDL from an XMLStreamBuffer, parses it
     * and returns a WSDLModel.
     *
     * @param systemId The URL to the file that is being parsed. May not be null.
     * @param buffer The XMLStreamBuffer from which WSDL is parsed. May not be null.
     * @return A WSDLModel populated from the WSDL input
     */
    public static WSDLModel parse(URL systemId, XMLStreamBuffer buffer) throws PolicyException {
        try {
            if (buffer == null) {
                throw new PolicyException(Messages.BUFFER_NOT_EXIST.format(systemId));
            }
            WSDLModel model = null;
            SDDocumentSource doc = SDDocumentSource.create(systemId, buffer);
            Parser parser =  new Parser(doc);
            model = RuntimeWSDLParser.parse(parser, new PolicyConfigResolver(),
                new WSDLParserExtension[] { new PolicyWSDLParserExtension() } );
            return model;
        } catch (XMLStreamException ex) {
            throw new PolicyException(Messages.WSDL_IMPORT_FAILED.format(), ex);
        } catch (IOException ex) {
            throw new PolicyException(Messages.WSDL_IMPORT_FAILED.format(), ex);
        } catch (SAXException ex) {
            throw new PolicyException(Messages.WSDL_IMPORT_FAILED.format(), ex);
        }
    }
    

    private static UrlBufferStruct loadFromClasspath(String filename)
        throws PolicyException {
        
        URL inputUrl = null;
        XMLStreamReader reader = null;
        InputStream input = null;
        
        try {

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                inputUrl = ClassLoader.getSystemResource(filename);
            }
            else {
                inputUrl = cl.getResource(filename);
            }
            XMLStreamBuffer buffer = null;
            if (inputUrl != null) {
                input = inputUrl.openStream();
                reader = xmlInputFactory.createXMLStreamReader(input);
                buffer = XMLStreamBuffer.createNewBufferFromXMLStreamReader(reader);
            }
            if (buffer == null) {
                return null;
            }
            else {
                return new UrlBufferStruct(inputUrl, buffer);
            }
        } catch (XMLStreamException e) {
            throw new PolicyException(Messages.READER_CREATE_FAILED.format(inputUrl), e);
        } catch (XMLStreamBufferException e) {
            throw new PolicyException(Messages.BUFFER_CREATE_FAILED.format(inputUrl, reader), e);
        } catch (IOException e) {
            throw new PolicyException(Messages.URL_OPEN_FAILED.format(inputUrl), e);
        }
    }

    
    private static UrlBufferStruct loadFromContext(Object context)
        throws PolicyException {
        
        URL inputUrl = null;
        InputStream input = null;
        XMLStreamReader reader = null;
        
        try {
            XMLStreamBuffer buffer = null;
            Method getResource = context.getClass().getMethod("getResource", String.class);
            inputUrl = (URL) getResource.invoke(context, "/WEB-INF/" + CONFIG_FILE_NAME);
            if (inputUrl != null) {
                input = inputUrl.openStream();
                reader = xmlInputFactory.createXMLStreamReader(input);
                buffer = XMLStreamBuffer.createNewBufferFromXMLStreamReader(reader);
            }
            if (buffer == null) {
                throw new PolicyException(Messages.FAILED_LOAD_CONTEXT.format(inputUrl, context));
            }
            return new UrlBufferStruct(inputUrl, buffer);
        } catch (NoSuchMethodException e) {
            throw new PolicyException(Messages.GET_RESOURCE_INVOCATION_FAILED.format(), e);
        } catch (IllegalAccessException e) {
            throw new PolicyException(Messages.GET_RESOURCE_INVOCATION_FAILED.format(), e);
        } catch (InvocationTargetException e) {
            throw new PolicyException(Messages.GET_RESOURCE_INVOCATION_FAILED.format(), e);
        } catch (IOException e) {
            throw new PolicyException(Messages.GET_RESOURCE_INVOCATION_FAILED.format(), e);
        } catch (XMLStreamException e) {
            throw new PolicyException(Messages.READER_CREATE_FAILED.format(inputUrl), e);
        } catch (XMLStreamBufferException e) {
            throw new PolicyException(Messages.BUFFER_CREATE_FAILED.format(inputUrl, reader), e);
        }
    }

    /**
     * Used to return an XMLStreamBuffer and the URL from which the buffer was built.
     */
    private static class UrlBufferStruct {
        private XMLStreamBuffer buffer;
        private URL url;
        
        UrlBufferStruct(URL url, XMLStreamBuffer buffer) {
            this.buffer = buffer;
            this.url = url;
        }
        
        public XMLStreamBuffer getBuffer() {
            return this.buffer;
        }
        
        public URL getUrl() {
            return this.url;
        }
    }
}
