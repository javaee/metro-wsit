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
    }
    
    /**
     * The method is used to connect the policy map mutator instance to the map it should mutate.
     *
     * @param map the policy map instance that will be mutable by this mutator.
     * @throws IllegalStateException in case this mutator object is already connected to a policy map.
     */
    void connect(final PolicyMap map) {
        if (isConnected()) {
            LOGGER.severe("connect", LocalizationMessages.POLICY_MAP_MUTATOR_ALREADY_CONNECTED());
            throw new IllegalStateException(LocalizationMessages.POLICY_MAP_MUTATOR_ALREADY_CONNECTED());
        }
        
        this.map = map;
    }
    
    /**
     * Can be used to retrieve the policy map currently connected to this mutator. Will return {@code null} if not connected.
     *
     * @returns policy map currently connected to this mutator. May return {@code null} if the mutator is not connected.
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
     * @returns {@code true} if the mutator instance is connected to a policy map, otherwise returns {@code false}.
     */
    public boolean isConnected() {
        return this.map != null;
    }
}
