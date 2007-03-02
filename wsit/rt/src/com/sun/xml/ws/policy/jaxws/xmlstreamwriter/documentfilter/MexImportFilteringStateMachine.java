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
package com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter;

import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.Invocation;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import static com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter.InvocationProcessingState.*;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class MexImportFilteringStateMachine implements FilteringStateMachine {
    private enum StateMachineMode {
        INACTIVE,
        BUFFERING,
        FILTERING
    }
    
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(MexImportFilteringStateMachine.class);
    
    private static final String MEX_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/09/mex";
    private static final String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
    private static final QName WSDL_IMPORT_ELEMENT = new QName(WSDL_NAMESPACE, "import");
    private static final QName IMPORT_NAMESPACE_ATTIBUTE = new QName(WSDL_NAMESPACE, "namespace");
    
    private int depth; // indicates the depth in which we are currently nested in the element that should be filtered out
    private StateMachineMode currentMode = StateMachineMode.INACTIVE; // indicates that current mode of the filtering state machine
    
    /** Creates a new instance of MexImportFilteringStateMachine */
    public MexImportFilteringStateMachine() {
        // nothing to initialize
    }
    
    public InvocationProcessingState getState(final Invocation invocation, final XMLStreamWriter writer) {
        LOGGER.entering(invocation);
        InvocationProcessingState resultingState = NO_STATE_CHANGE;
        try {
            switch (invocation.getMethodType()) {
                case WRITE_START_ELEMENT:
                    if (currentMode == StateMachineMode.INACTIVE) {
                        if (startBuffering(invocation, writer)) {
                            resultingState = START_BUFFERING;
                            currentMode = StateMachineMode.BUFFERING;
                        }
                    } else {
                        depth++;
                    }
                    break;
                case WRITE_END_ELEMENT:
                    if (currentMode != StateMachineMode.INACTIVE) {
                        if (depth == 0) {
                            resultingState = (currentMode == StateMachineMode.BUFFERING) ? STOP_BUFFERING : STOP_FILTERING;
                            currentMode = StateMachineMode.INACTIVE;
                        } else {
                            depth--;
                        }
                    }
                    break;
                case WRITE_ATTRIBUTE:
                    if (currentMode == StateMachineMode.BUFFERING && startFiltering(invocation, writer)) {
                        resultingState = START_FILTERING;
                        currentMode = StateMachineMode.FILTERING;
                    }
                    break;
                case CLOSE:
                    switch (currentMode) {
                        case BUFFERING:
                            resultingState = STOP_BUFFERING; break;
                        case FILTERING:
                            resultingState = STOP_FILTERING; break;
                    }
                    currentMode = StateMachineMode.INACTIVE;
                    break;
                default:
                    break;
            }
            
            return resultingState;
            
        } finally {
            LOGGER.exiting(resultingState);
        }
    }
    
    private boolean startFiltering(final Invocation invocation, final XMLStreamWriter writer) {
        final XmlFilteringUtils.AttributeInfo attributeInfo = XmlFilteringUtils.getAttributeNameToWrite(invocation, XmlFilteringUtils.getDefaultNamespaceURI(writer));
        return IMPORT_NAMESPACE_ATTIBUTE.equals(attributeInfo.getName()) && MEX_NAMESPACE.equals(attributeInfo.getValue());
    }
    
    private boolean startBuffering(final Invocation invocation, final XMLStreamWriter writer) {
        final QName elementName = XmlFilteringUtils.getElementNameToWrite(invocation, XmlFilteringUtils.getDefaultNamespaceURI(writer));
        return WSDL_IMPORT_ELEMENT.equals(elementName);
    }
}
