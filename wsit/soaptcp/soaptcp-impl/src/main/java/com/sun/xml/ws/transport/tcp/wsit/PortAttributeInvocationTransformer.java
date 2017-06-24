/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.transport.tcp.wsit;

import com.sun.xml.ws.xmlfilter.Invocation;
import com.sun.xml.ws.xmlfilter.XmlStreamWriterMethodType;
import com.sun.xml.ws.xmlfilter.InvocationTransformer;
import com.sun.xml.ws.xmlfilter.XmlFilteringUtils;
import com.sun.xml.ws.xmlfilter.XmlFilteringUtils.AttributeInfo;

import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.server.WSTCPModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import javax.xml.stream.XMLStreamWriter;
import static com.sun.xml.ws.transport.tcp.wsit.TCPConstants.*;

/**
 * SOAP/TCP invocation transformer, which is responsible to insert SOAP/TCP 'port' 
 * attribute in a published WSDL
 * 
 * @author Alexey Stashok
 */
public class PortAttributeInvocationTransformer implements InvocationTransformer {
    private static final String RUNTIME_PORT_CHANGE_VALUE = "SET_BY_RUNTIME";

    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private Collection<Invocation> invocationWrapper = new ArrayList<Invocation>(4);
    
    private boolean isProcessingWSTCPAssertion;
    
    private volatile Invocation addPortAttributeInvocation;

    /**
     * Method transforms SOAP/TCP port attribute, otherwise returns the same invocation.
     * WARNING: due to perf. reasons, method reuses the same Collection instance.
     * So call transform next time only if previously returned Collection is not required
     * any more.
     * 
     * @param invocation
     * @return transformed invocations
     */
    public Collection<Invocation> transform(final Invocation invocation) {
        Invocation resultInvocation = invocation;
        switch (invocation.getMethodType()) {
            case WRITE_START_ELEMENT:
                if (!isProcessingWSTCPAssertion) {
                    isProcessingWSTCPAssertion = startBuffering(invocation);
                }
                break;
            case WRITE_END_ELEMENT:
                isProcessingWSTCPAssertion = false;
                break;
            case WRITE_ATTRIBUTE:
                if (isProcessingWSTCPAssertion && isReplacePortAttribute(invocation)) {
                    try {
                        initializeAddPortAttributeIfRequired();
                        if (addPortAttributeInvocation == null && 
                                WSTCPModule.getInstance().getPort() == -1) {
                            if (logger.isLoggable(Level.WARNING)) {
                                logger.log(Level.WARNING,
                                        MessagesMessages.WSTCP_1162_UNSUPPORTED_PORT_ATTRIBUTE());
                            }
                        }
                    } catch(Exception e) {
                        if (logger.isLoggable(Level.WARNING)) {
                            logger.log(Level.WARNING, 
                                    MessagesMessages.WSTCP_1161_ADD_PORT_ATTR_INIT_FAIL(), e);
                        }
                    }
                    
                    resultInvocation = addPortAttributeInvocation;
                }
                break;
            case CLOSE:
                isProcessingWSTCPAssertion = false;
                break;
            default:
                break;
            }

        invocationWrapper.clear();
        if (resultInvocation != null) {
            invocationWrapper.add(resultInvocation);
        }
        
        return invocationWrapper;
    }

    private void initializeAddPortAttributeIfRequired() throws Exception {
        int port;
        if (addPortAttributeInvocation == null && 
                (port = WSTCPModule.getInstance().getPort()) != -1) {
            synchronized(this) {
                if (addPortAttributeInvocation == null) {
                    addPortAttributeInvocation = Invocation.createInvocation(
                            XMLStreamWriter.class.getMethod(
                            XmlStreamWriterMethodType.WRITE_ATTRIBUTE.getMethodName(), 
                            String.class, String.class),
                            new Object[] {
                                TCPTRANSPORT_PORT_ATTRIBUTE.getLocalPart(),
                                Integer.toString(port)
                            });
                }
            }
        }
    }

    private boolean isReplacePortAttribute(Invocation invocation) {
        AttributeInfo attr = XmlFilteringUtils.getAttributeNameToWrite(invocation, "");
        if (TCPTRANSPORT_PORT_ATTRIBUTE.equals(attr.getName())) {
            if (RUNTIME_PORT_CHANGE_VALUE.equals(attr.getValue())) return true;
            
            
            String attrValue = attr.getValue();
            int portNumber = -1;
            if (attrValue != null) {
                try {
                    portNumber = Integer.parseInt(attrValue);
                } catch(NumberFormatException e) {
                }
            }
            
            if (portNumber > 0) return false;
            
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, MessagesMessages.WSTCP_1160_PORT_ATTR_INVALID_VALUE(attrValue));
            }
            
            return true;
        }
        
        return false;
    }
    
    private boolean startBuffering(final Invocation invocation) {
        final QName elementName = XmlFilteringUtils.getElementNameToWrite(invocation, "");
        return TCPTRANSPORT_POLICY_ASSERTION.equals(elementName);
    }    
}
