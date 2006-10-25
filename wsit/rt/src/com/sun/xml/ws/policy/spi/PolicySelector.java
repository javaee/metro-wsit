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

package com.sun.xml.ws.policy.spi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.xml.namespace.QName;
import com.sun.xml.ws.policy.PolicyAssertion;

/**
 * A call-back interface to test if a policy assertion is valid and supported.
 */
public interface PolicySelector {
    
    public enum Fitness {
        UNKNOWN,
        SUPPORTED,
        UNSUPPORTED
    }
    
    
    /**
     * An implementation of this method must return Fitness.UNKNOWN if the given policy
     * assertion is not known, Fitness.SUPPORTED if it is supported and Fitness.UNSUPPORTED otherwise.
     *
     * @param assertion A policy asssertion. May contain nested policies and
     * assertions.
     * @return must not be null
     */
    public Fitness getFitness(PolicyAssertion assertion);
}
