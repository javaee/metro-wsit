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

/**
 * The class serves as a base for specific policy map mutator implementations. It provides common methods that allow
 * concrete mutator implementations to connect and disconnect to/from a policy map instance.
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public abstract class PolicyMapMutator {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyMapMutator.class);
    
    private PolicyMap map = null;
    
    /**
     * Creates a new instance of PolicyMapMutator. This class cannot be extended from outside of this package.
     */
    PolicyMapMutator() {
        // nothing to instantiate
    }
    
    /**
     * The method is used to connect the policy map mutator instance to the map it should mutate.
     *
     * @param map the policy map instance that will be mutable by this mutator.
     * @throws IllegalStateException in case this mutator object is already connected to a policy map.
     */
    void connect(final PolicyMap map) {
        if (isConnected()) {
            throw LOGGER.logSevereException(new IllegalStateException(LocalizationMessages.WSP_0044_POLICY_MAP_MUTATOR_ALREADY_CONNECTED()));
        }
        
        this.map = map;
    }
    
    /**
     * Can be used to retrieve the policy map currently connected to this mutator. Will return {@code null} if not connected.
     *
     * @return policy map currently connected to this mutator. May return {@code null} if the mutator is not connected.
     *
     * @see #isConnected()
     * @see #disconnect()
     */
    public PolicyMap getMap() {
        return this.map;
    }
    
    /**
     * Disconnects the mutator from the policy map object it is connected to. Method must be called prior to connecting this
     * mutator instance to another policy map.
     * <p/>
     * This operation is irreversible: you cannot connect the mutator to the same policy map instance once you disconnect from it. 
     * Multiple consequent calls of this method will have no effect.
     */
    public void disconnect() {
        this.map = null;
    }    
        
    /**
     * This method provides connection status information of the policy map mutator instance.
     *
     * @return {@code true} if the mutator instance is connected to a policy map, otherwise returns {@code false}.
     */
    public boolean isConnected() {
        return this.map != null;
    }
}
