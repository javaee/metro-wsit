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

import java.util.Collection;

/**
 * Abstract class defines interface for policy map unmarshaller implementations that are specific to underlying
 * persistence layer.
 *
 * @author Marek Potociar
 */
public abstract class PolicyMapUnmarshaller {
    
    /**
     * Default constructor to ensure we have a common map unmarshaller base, but only our API classes implemented in this
     * package will be able to extend this abstract class. This is to restrict attempts of extending the class from
     * a client code.
     */
    PolicyMapUnmarshaller() {
    }
    
    /**
     * Unmarshalls all discovered policy source models from provided storage reference. This operation is optional.
     *
     * @param policies collection of policies that are expected to be bound in the unmarshalled policy map.
     * @param storage reference to underlying storage that should be used for model unmarshalling
     * @return unmarshalled policy map. If no policy bindings are found, returns empty policy map.
     * @throws UnsupportedOperationException if the operation is not supported by implementation.
     */
    public abstract PolicyMap unmarshal(Collection<Policy> policies, Object storage) throws UnsupportedOperationException;
    
    /**
     * Unmarshalls all discovered policy source models from provided storage reference and adds them into provided
     * policy map. This operation is optional.
     *
     * @param map existing policy map that should be extended with newly unmarshalled policy bindings.
     * @param policies collection of policies that are expected to be bound in the unmarshalled policy map.
     * @param storage reference to underlying storage that should be used for model unmarshalling
     * @return unmarshalled policy map. If no policy bindings are found, returns empty policy map.
     * @throws UnsupportedOperationException if the operation is not supported by implementation.
     */
    public abstract PolicyMap unmarshalNewPolicyBindings(PolicyMap map, Collection<Policy> policies, Object storage) throws UnsupportedOperationException;
}
