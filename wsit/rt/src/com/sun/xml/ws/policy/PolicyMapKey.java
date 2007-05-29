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

package com.sun.xml.ws.policy;

import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import javax.xml.namespace.QName;

/**
 * This class provides implementation of PolicyMapKey interface to be used in connection with {@link PolicyMap}.
 * Instances of the class are created by a call to one of {@link PolicyMap} <code>createWsdl<emph>XXX</emph>PolicyMapKey(...)</code>
 * methods.
 * <p/>
 * The class wraps scope information and adds a package setter method to allow injection of actual equality comparator/tester. This injection
 * is made within a <code>get...</code> call on {@link PolicyMap}, before the actual scope map search is performed.
 * 
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final public class PolicyMapKey  {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyMapKey.class);
    
    QName service;
    QName port;
    QName operation;
    QName faultMessage;
//    QName inputMessage;
//    QName outputMessage;
    
    private PolicyMapKeyHandler handler;
    
    PolicyMapKey(final QName service, final QName port, final QName operation) {
        this.service = service;
        this.port = port;
        this.operation = operation;
        
//        this.inputMessage = inputMessage;
//        this.outputMessage = outputMessage;
    }
    
    PolicyMapKey(final QName service, final QName port, final QName operation, final QName faultMessage) {
        this.service = service;
        this.port = port;
        this.operation = operation;
        
//        this.inputMessage = inputMessage;
//        this.outputMessage = outputMessage;
        
        this.faultMessage = faultMessage;
    }
    
    PolicyMapKey(final PolicyMapKey that) {
        this.service = that.service;
        this.port = that.port;
        this.operation = that.operation;
        this.faultMessage = that.faultMessage;

//        this.inputMessage = that.inputMessage;
//        this.outputMessage = that.outputMessage;

        this.handler = that.handler;
    }
    
    void setHandler(final PolicyMapKeyHandler handler) {
        this.handler = handler;
    }
    
    public boolean equals(final Object that) {
        if (this == that) {
            return true; // we are lucky here => no special handling is required
        }
        
        if (that == null) {
            return false;
        }
            
        if (handler == null) {
            throw LOGGER.logSevereException(new IllegalStateException(LocalizationMessages.WSP_0046_POLICY_MAP_KEY_HANDLER_NOT_SET()));
        }
        
        if (that instanceof PolicyMapKey) {
            return handler.areEqual(this, (PolicyMapKey) that);
        } else {
            return false;
        }
    }

    public int hashCode() {
        if (handler == null) {
            throw LOGGER.logSevereException(new IllegalStateException(LocalizationMessages.WSP_0046_POLICY_MAP_KEY_HANDLER_NOT_SET()));
        }

        return handler.generateHashCode(this);
    }    
    
    public String toString() {
        final StringBuffer result = new StringBuffer("WsdlPolicyMapKey(");
        result.append(this.service).append(", ").append(port).append(", ").append(operation).append(", ").append(faultMessage);
        return result.append(")").toString();
    }
}
