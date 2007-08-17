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

/*
 * SequenceConfig.java
 *
 * @author Mike Grogan
 * Created on October 16, 2005, 1:23 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.rm.SequenceSettings;
import com.sun.xml.ws.policy.*;
import com.sun.xml.ws.policy.jaxws.WSDLPolicyMapWrapper;
import com.sun.xml.ws.rm.RMConstants;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.RMVersion;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.util.Iterator;

/**
 * SequenceConfig stores settings read from a WS-RM Policy Assertion in a Web Service's
 * metadata and is used enable/disable and configure reliable messaging on bindings
 * to which the policy assertion is attached.  (Placeholder for now pending WS-Policy
 * implementation)
 */
public class SequenceConfig extends SequenceSettings {

   
    /**
     * Constructor initializes with default values.
     */
    public SequenceConfig() {
        
        //Use anonymous URI for acksTo.  Its value depends on 
        //WS-Addressing version being used

        if (constants == null) {
            //hardcoding W3C as default
            constants = RMConstants.getRMConstants(AddressingVersion.W3C);
        }
        acksTo = constants.getAnonymousURI().toString();
        
        ordered = false;
        allowDuplicates = false;
        inactivityTimeout = 600000;
        flowControl = false;
        bufferSize = 32;
        soapVersion = SOAPVersion.SOAP_12;
             
        ackRequestInterval = 0;
        resendInterval = 0;
        
        closeTimeout = 0; //infinite
        sequenceSTR = false;
        sequenceTransportSecurity = false;
        
    }
    
    public SequenceConfig(WSDLPort port, WSBinding wsbinding) {
        
        this();
        if (port != null) {
            WSDLBoundPortType binding = port.getBinding();
            WSDLModel model = binding.getOwner();
            PolicyMap policyMap = model.getExtension(WSDLPolicyMapWrapper.class)
                                            .getPolicyMap();
            this.constants = RMConstants.getRMConstants(wsbinding.getAddressingVersion());
            try {
                init(port, policyMap);
            } catch (RMException e) {
                throw new WebServiceException(e);
            }
        } 
    }
    
    /**
     * Copies the members of a specified SequenceSettings
     */
    public SequenceConfig(SequenceSettings toCopy) {
        
        this.ackRequestInterval = toCopy.ackRequestInterval;
        this.acksTo = toCopy.acksTo;
        this.allowDuplicates = toCopy.allowDuplicates;
        this.bufferSize = toCopy.bufferSize;
        this.closeTimeout = toCopy.closeTimeout;
        this.constants = toCopy.constants;
        this.flowControl = toCopy.flowControl;
        this.inactivityTimeout = toCopy.inactivityTimeout;
        this.ordered = toCopy.ordered;
        this.resendInterval = toCopy.resendInterval;
        this.soapVersion = toCopy.soapVersion;
        this.sequenceSTR = toCopy.sequenceSTR;
        this.sequenceTransportSecurity = toCopy.sequenceTransportSecurity;
  
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
     * Accessor for the closeTimeout property.
     *
     * @return The value of the property.
     */
    public long getCloseTimeout() {
        return closeTimeout;
    }
    
    /**
     * Mutator for the closeTimeout property.
     *
     * @param The new value.
     */
    public void setCloseTimeout(long timeout) {
        closeTimeout = timeout;
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
    
     /**
     * Accessor for resendIterval field
     *
     * @return The value of the field.
     */
    public long getResendInterval() {
        return resendInterval;
    }
    
    /**
     * Mutator for the flow control field
     *
     * @param The new value
     */
    public void setResendInterval(long interval) {
        resendInterval = interval;
    }
    
     /**
     * Accessor for ackRequestIterval field
     *
     * @return The value of the field.
     */
    public long getAckRequestInterval() {
        return ackRequestInterval;
    }
    
    /**
     * Mutator for the ackRequestInterval field
     *
     * @param The new value
     */
    public void setAckRequestInterval(long interval) {
        ackRequestInterval = interval;
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
                            QName qname = assertion.getName();
                          
                            if (qname.equals(RMVersion.WSRM10.getRMPolicyAssertionQName())) {
                                rmVersion = RMVersion.WSRM10;
                                rmAssertion = assertion;
                            } else if (qname.equals(RMVersion.WSRM11.getRMPolicyAssertionQName())) {
                                rmVersion = RMVersion.WSRM11;
                                rmAssertion = assertion;
                            } else if (qname.equals(constants.getRMFlowControlQName())) {
                                flowAssertion = assertion;
                            } else if (qname.equals(constants.getOrderedQName())) {
                                ordered = true;
                            } else if (qname.equals (constants.getAllowDuplicatesQName())) {
                                allowDuplicates = true;
                            } else if (qname.equals(constants.getAckRequestIntervalQName())) {
                                String num = assertion.getAttributeValue(new QName("", "Milliseconds"));
                                if (num != null) {
                                    ackRequestInterval = Long.parseLong(num);
                                }
                            } else if (qname.equals(constants.getResendIntervalQName())) {
                                String num = assertion.getAttributeValue(new QName("", "Milliseconds"));
                                if (num != null) {
                                    resendInterval = Long.parseLong(num);
                                }
                            } else if (qname.equals(constants.getCloseTimeoutQName())) {
                                String num = assertion.getAttributeValue(new QName("", "Milliseconds"));
                                if (num != null) {
                                    closeTimeout = Long.parseLong(num);
                                }
                            }  else if (qname.equals(RMVersion.WSRM11.getSequenceSTRAssertionQName())) {
                                sequenceSTR = true;
                            } else if (qname.equals(RMVersion.WSRM11.getSequenceTransportSecurityAssertionQName())) {
                                sequenceTransportSecurity = true;
                            } else {
                                //TODO handle error condition here
                            }
                        }
                        
                        if (rmAssertion != null) {
                           handleRMAssertion(rmAssertion);
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
            if (assertion.getName().equals(rmVersion.getInactivityTimeoutAssertionQName())) {
                
                String num = assertion.getAttributeValue(new QName("", "Milliseconds"));
               
                if (num != null) {
                    inactivityTimeout = Long.parseLong(num);
                }
           //TODO - disregard other nested assertions for now.
           //possibly revisit this later
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

    /**
     *  Returns the RMConstants based on the AddressingVersion
     * @return RMConstants
     */
    public RMConstants getRMConstants() {
        return constants;
    }

    public RMConstants getConstants() {
        return constants;
    }

    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }


    public RMVersion getRMVersion(){
        return rmVersion;
    }
}
