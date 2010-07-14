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
    protected final TransactionManagerImpl txnMgr;

    /**
     * next pipe in the chain
     */
    protected final Pipe next;
    
    public TxBasePipe(Pipe next) {
        this.next = next;
        txnMgr = TransactionManagerImpl.getInstance();
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
