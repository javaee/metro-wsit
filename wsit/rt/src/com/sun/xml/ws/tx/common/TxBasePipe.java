/*
 * TxBasePipe.java
 *
 * Created on March 16, 2007, 7:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.tx.common;

import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import java.util.Iterator;
import javax.xml.ws.WebServiceException;
import static com.sun.xml.ws.tx.common.Constants.*;
import static com.sun.xml.ws.tx.common.ATAssertion.*;

/**
 *
 * @author jf39279
 */
abstract public class TxBasePipe implements Pipe {
    
    static protected TransactionManagerImpl tm = TransactionManagerImpl.getInstance();

    /**
     * next pipe in the chain
     */
    protected final Pipe next;
    
    public TxBasePipe(Pipe next) {
        this.next = next;
    }

    /**
     * Invoked before the last copy of the pipeline is about to be discarded,
     * to give Pipes a chance to clean up any resources.
     */
    public void preDestroy() {
        next.preDestroy();
    }

    /**
     * Representation of all WS-AT policy assertions: ATAssertion and ATAlwaysCapability.
     */
    static public class OperationATPolicy {
        public ATAssertion atAssertion = ATAssertion.NOT_ALLOWED;
        public boolean ATAlwaysCapability = false;
    }

    protected OperationATPolicy getOperationATPolicy(PolicyMap pmap, 
            WSDLPort wsdlModel, 
            WSDLBoundOperation wsdlBoundOp) throws WebServiceException 
    {
        OperationATPolicy opat = new OperationATPolicy();
        try {
            if (pmap != null) {
                PolicyMapKey opKey = pmap.
                        createWsdlOperationScopeKey(
                        wsdlModel.getOwner().getName(), // service
                        wsdlModel.getName(), // port
                        wsdlBoundOp.getName() // operation
                        );
                Policy effectivePolicy =
                        pmap.getOperationEffectivePolicy(opKey);
                if (effectivePolicy != null) {
                    Iterator<AssertionSet> assertionIter = effectivePolicy.iterator();
                    AssertionSet assertionSet;
                    while (assertionIter.hasNext()) {
                        assertionSet = assertionIter.next();
                        for (PolicyAssertion pa : assertionSet) {
                            
                            // Check for 2004 WS-Atomic Transaction Policy Assertions
                            if (pa.getName().equals(AT_ASSERTION)) {
                                opat.atAssertion = (pa.isOptional() ? ALLOWED : MANDATORY);
                                
                                // begin patch for wsit issue 419
                                if (opat.atAssertion == MANDATORY) {
                                    String optionalBoolValue = pa.getAttributeValue(WSP2002_OPTIONAL);
                                    if (optionalBoolValue != null && Boolean.valueOf(optionalBoolValue)) {
                                        opat.atAssertion = ATAssertion.ALLOWED;
                                    }
                                }
                                // end patch
                            } else if (pa.getName().equals(AT_ALWAYS_CAPABILITY)) {
                                opat.ATAlwaysCapability = true;
                            }
                            
                            // TODO: To implement OASIS WS-Atomic Transaction,
                            // check for OASIS WS-Atomic Transaction Policy Assertion ATAssertion here
                        }
                    }
                }
            }
        } catch (PolicyException pe) {
            throw new WebServiceException(pe.getMessage(), pe);
        }
        return opat;
    }
}
