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

package com.sun.xml.ws.policy.sourcemodel;

import java.util.Collection;
import com.sun.xml.ws.policy.PolicyException;

/**
 * Abstract class defines interface for policy model marshaller implementations that are specific to underlying
 * persistence layer.
 *
 * @author Marek Potociar
 */
public abstract class PolicyModelMarshaller {
    private static final PolicyModelMarshaller defaultXmlMarshaller = new XmlPolicyModelMarshaller(false);
    private static final PolicyModelMarshaller invisibleAssertionXmlMarshaller = new XmlPolicyModelMarshaller(true);
    
    /**
     * Default constructor to ensure we have a common model marshaller base, but only our API classes implemented in this 
     * package will be able to extend this abstract class. This is to restrict attempts of extending the class from 
     * a client code.
     */
    PolicyModelMarshaller() {  
        // nothing to instantiate
    }
    
    /**
     * Marshalls the policy source model using provided storage reference
     *
     * @param model policy source model to be marshalled
     * @param storage reference to underlying storage that should be used for model marshalling
     * @throws PolicyException If marshalling failed
     */
    public abstract void marshal(PolicySourceModel model, Object storage) throws PolicyException;

    /**
     * Marshalls the collection of policy source models using provided storage reference
     *
     * @param models collection of policy source models to be marshalled
     * @param storage reference to underlying storage that should be used for model marshalling
     * @throws PolicyException If marshalling failed
     */
    public abstract void marshal(Collection<PolicySourceModel> models, Object storage) throws PolicyException;

    /**
     * Factory methods that returns a marshaller instance based on input parameter.
     *
     * @param marshallInvisible boolean parameter indicating whether the marshaller 
     *        returned by this method does marshall private assertions or not.
     * 
     * @return policy model marshaller that either marshalls private assertions or not
     *         based on the input argument.
     */
    public static PolicyModelMarshaller getXmlMarshaller(final boolean marshallInvisible) {
        return (marshallInvisible) ? invisibleAssertionXmlMarshaller : defaultXmlMarshaller;
    }
}
