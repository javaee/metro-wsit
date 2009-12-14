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

package com.sun.xml.ws.rx.rm.policy.metro200603;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.SimpleAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.rx.policy.AssertionInstantiator;
import com.sun.xml.ws.rx.rm.policy.RmAssertionNamespace;
import com.sun.xml.ws.rx.rm.policy.RmConfigurator;
import com.sun.xml.ws.rx.rm.ReliableMessagingFeature.DeliveryAssurance;
import com.sun.xml.ws.rx.rm.ReliableMessagingFeatureBuilder;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import java.util.Collection;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * <sunc:AllowDuplicates />
 */
/**
 * Proprietary assertion that works with WS-RM v1.0 (WSRM200502) and enables 
 * "At Least Once" message delivery:
 * <p />
 * Each message is to be delivered at least once, or else an error MUST be raised 
 * by the RM Source and/or RM Destination. The requirement on an RM Source is that 
 * it SHOULD retry transmission of every message sent by the Application Source 
 * until it receives an acknowledgement from the RM Destination. The requirement 
 * on the RM Destination is that it SHOULD retry the transfer to the Application 
 * Destination of any message that it accepts from the RM Source, until that message 
 * has been successfully delivered. There is no requirement for the RM Destination 
 * to apply duplicate message filtering.
 * <p />
 * NOTE: this assertion has currently no effect, we treat the case the same way as 
 * "Exactly Once" delivery mode.
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class AllowDuplicatesAssertion extends SimpleAssertion implements RmConfigurator {
    public static final QName NAME = RmAssertionNamespace.METRO_200603.getQName("AllowDuplicates");
    
    private static AssertionInstantiator instantiator = new AssertionInstantiator() {
        public PolicyAssertion newInstance(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) {
            return new AllowDuplicatesAssertion(data, assertionParameters);
        }
    };
    
    public static AssertionInstantiator getInstantiator() {
        return instantiator;
    }
    
    public AllowDuplicatesAssertion(AssertionData data, Collection<? extends PolicyAssertion> assertionParameters) {
        super(data, assertionParameters);
    }

    public ReliableMessagingFeatureBuilder update(ReliableMessagingFeatureBuilder builder) {
        if (builder.getVersion() != RmVersion.WSRM200502) {
            throw new WebServiceException(LocalizationMessages.WSRM_1001_ASSERTION_NOT_COMPATIBLE_WITH_RM_VERSION(NAME, builder.getVersion()));
        }

        return builder.deliveryAssurance(DeliveryAssurance.AT_LEAST_ONCE);
    }
    
    public boolean isCompatibleWith(RmVersion version) {
        return RmVersion.WSRM200502 == version;
    }
}
