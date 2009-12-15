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
package com.sun.xml.ws.xmlfilter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import com.sun.istack.logging.Logger;

import static com.sun.xml.ws.xmlfilter.ProcessingStateChange.*;

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
    
    private static final Logger LOGGER = Logger.getLogger(MexImportFilteringStateMachine.class);
    
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
    
    public ProcessingStateChange getStateChange(final Invocation invocation, final XMLStreamWriter writer) {
        LOGGER.entering(invocation);
        ProcessingStateChange resultingState = NO_CHANGE;
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
