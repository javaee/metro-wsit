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

import com.sun.xml.ws.policy.spi.PolicySelector;
import java.util.ArrayList;
import javax.xml.namespace.QName;

/**
 * Used by EffectiveAlternativeSelectorTest
 *
 * @author japod
 */
public class DummyPolicySelector extends PolicySelector{
    
    public static boolean supported;
    
    /** Creates a new instance of DummyPolicySelector */
    public DummyPolicySelector() {
        super(new ArrayList<QName>());
    }
    
    public void setSupported(boolean supported) {
        this.supported = supported;
    }
    
    public boolean test(PolicyAssertion assertion) {
        return supported;
    }
    
    public boolean isSupported(String namespace) {
        return supported;
    }
}
