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

/*
 * SequenceConfig.java
 *
 * @author Mike Grogan
 * Created on October 16, 2005, 1:23 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.rm.RMConstants;
import com.sun.xml.ws.rm.RMBuilder;
import com.sun.xml.ws.rm.RMException;

import javax.xml.ws.addressing.AddressingConstants;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.policy.*;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.policy.jaxws.WSDLPolicyMapWrapper;
import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Iterator;

/**
 * SequenceConfig stores settings read from a WS-RM Policy Assertion in a Web Service's
 * metadata and is used enable/disable and configure reliable messaging on bindings
 * to which the policy assertion is attached.  (Placeholder for now pending WS-Policy
 * implementation)
 */
public class SequenceConfig {

    /**
     * AcksTo URI for the sequence
     */
    public String acksTo;

    /**
     * For OutboundSequences, determines whether destination guarantees ordered delivery.
     */
    public boolean ordered;

    /**
     * Number of milliseconds after which destination may terminate sequence.
     */
    public long inactivityTimeout;


    /**
     * Indicates whether flow control is enabled.
     */
    public boolean flowControl;
    
    /**
     * Number of messages that destination will buffer pending delivery.
     */
    public int bufferSize;

    /**
     * The SOAPVersion which will be passed on to the protocol elements
     * populated from the Pipe
     */
    public SOAPVersion soapVersion;
    
    
    private static RMConstants constants = 
            new RMConstants();


    /**
     * Constructor initializes with default values.
     */
    public SequenceConfig() {
        
        //Use anonymous URI for acksTo.  Its value depends on 
        //WS-Addressing version being used
        RMConstants constants = RMBuilder.getConstants();
        AddressingConstants addressingConstants = 
                      constants.getAddressingBuilder().newAddressingConstants();
        acksTo = addressingConstants.getAnonymousURI();
        
        ordered = false;
        inactivityTimeout = 600000;
        flowControl = true;
        bufferSize = 32;
        soapVersion = SOAPVersion.SOAP_12;
    }
    
    public SequenceConfig(WSDLPort port) {
        
        this();
        if (port != null) {
            WSDLBoundPortType binding = port.getBinding();
            WSDLModel model = binding.getOwner();
            PolicyMap policyMap = model.getExtension(WSDLPolicyMapWrapper.class)
                                            .getPolicyMap();
            try {
                init(port, policyMap);
            } catch (RMException e) {
                throw new WebServiceException(e);
            }
        } 
    }
    
    /**
     * Accessor for the <code>acksTo</code> property.
     *
     * @return The value of the property.
     */
    public String getAcksTo() {
        return acksTo;
    }

    /**
     * Mutator for the <code>AcksTo</code> property.
     *
     * @param acksTo The new value of the property.
     */
    public void setAcksTo(String acksTo) {
        this.acksTo = acksTo;
    }

    /**
     * Accessor for <code>ordered</code> property.
     *
     * @return The value of the property.
     */
    public boolean getOrdered() {
        return ordered;
    }

    /**
     * Mutator for the <code>ordered</code> property.
     *
     * @param ordered The new value of the property.
     */
    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    /**
     * Accessor for <code>inactivityTimeout</code> property.
     *
     * @return The value of the property.
     */
    public long getInactivityTimeout() {
        return inactivityTimeout;
    }

    /**
     * Mutator for the <code>inactivityTimeout</code> property.
     *
     * @param inactivityTimeout The new value of the property.
     */
    public void setInactivityTimeout(long inactivityTimeout) {
        this.inactivityTimeout = inactivityTimeout;
    }

    /**
     * Accessor for <code>bufferSize</code> property.
     *
     * @return The value of the property.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Mutator for the <code>bufferSize</code> property.
     *
     * @param bufferSize The new value of the property.
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Returns the SOAPVersion obtained from the WSBinding
     * Defaulting to SOAP_12
     *
     * @return SOAPVersion should not be null
     */
    public SOAPVersion getSoapVersion() {
        if (soapVersion != null)
            return soapVersion;
        //TODO check if this defaulting is ok
        else return SOAPVersion.SOAP_12;
    }

    public void setSoapVersion(SOAPVersion soapVersion) {
        this.soapVersion = soapVersion;
    }
    
    /**
     * Accessor for flow control field
     *
     * @return The value of the field.
     */
    public boolean getFlowControl() {
        return flowControl;
    }
    
    /**
     * Mutator for the flow control field
     *
     * @param The new value
     */
    public void setFlowControl(boolean use) {
        flowControl = use;
    }
    
    public void init(WSDLPort port, PolicyMap policyMap) throws RMException {
       
        try {
            
            if (policyMap != null) {
                PolicyMapKey endpointScopeKey = 
                        policyMap.
                            createWsdlEndpointScopeKey(port.getOwner().getName(), 
                                                       port.getName());

                if (endpointScopeKey != null) {
                    
                    AssertionSet policyAssertionSet = null;
                    
                    Policy policy = policyMap.getEndpointEffectivePolicy(endpointScopeKey);
                    if (policy != null) {
                        for (AssertionSet set: policy) {
                            policyAssertionSet = set;
                            break;
                        }
                    }
                    
                    if (policyAssertionSet != null) {
                        
                        PolicyAssertion rmAssertion = null;
                        PolicyAssertion flowAssertion = null;
                        
                        for (PolicyAssertion assertion : policyAssertionSet) {
                 
                            if (assertion.getName()
                                             .equals(constants.getRMAssertionQName())) {
                                rmAssertion = assertion;
                            } else if (assertion.getName()
                                            .equals(constants.getRMFlowControlQName())) {
                                flowAssertion = assertion;
                            } else if (assertion.getName()
                                            .equals(constants.getOrderedQName())) {
                                ordered = true;
                            } else {
                                //TODO handle error condition here
                            }
                        }
                        
                        if (rmAssertion != null) {
                           handleRMAssertion(rmAssertion);
                        } else {
                            throw new RMException("RM cannot be enabled as RM Assertion is missing");
                        }

                        if (flowAssertion != null)
                           handleFlowAssertion(flowAssertion);
                    }
                }
            }
            
        } catch (PolicyException e) {
            e.printStackTrace();
        }
    }
    
    private void handleRMAssertion(PolicyAssertion rmAssertion) {
        
        Iterator<PolicyAssertion> it = rmAssertion.getNestedAssertionsIterator();
        
        while (it != null && it.hasNext()) {
            PolicyAssertion assertion = it.next();
            if (assertion.getName().equals(constants.getInactivityTimeoutQName())) {
                
                String num = assertion.getAttributeValue(new QName("", "Milliseconds"));
               
                if (num != null) {
                    inactivityTimeout = Long.parseLong(num);
                }
            } else if (assertion.getName().equals(constants.getAcknowledgementIntervalQName())) {
                //don't have a member variable for it.  Do we need it?
                 
            }
        }
    }
    
    private void handleFlowAssertion(PolicyAssertion flowAssertion) {
       
        flowControl = true;
        
        Iterator<PolicyAssertion> it = flowAssertion.getNestedAssertionsIterator();
        while (it != null && it.hasNext()) { 
             PolicyAssertion assertion = it.next();
            if (assertion.getName().equals(constants.getMaxReceiveBufferSizeQName())) {
                bufferSize = Integer.parseInt(assertion.getValue());            
                break;
            }
       }
    }
        
}
