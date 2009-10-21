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
package com.sun.xml.ws.rx.policy.assertion.net.rm200502;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.ComplexAssertion;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.rx.policy.assertion.AssertionInstantiator;
import com.sun.xml.ws.rx.policy.assertion.AssertionNamespace;
import com.sun.xml.ws.rx.policy.assertion.RmConfigurator;
import com.sun.xml.ws.rx.rm.ReliableMessagingFeatureBuilder;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import java.util.Collection;
import javax.xml.namespace.QName;

/**
 * <ms:RmFlowControl>
 *   <ms:MaxReceiveBufferSize>value</ms:MaxReceiveBufferSize>
 * </ms:RmFlowControl>
 */
/**
 * Defines maximum server-side buffer size in ordered delivery scenario.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class RmFlowControlAssertion extends ComplexAssertion implements RmConfigurator {

    public static final QName NAME = AssertionNamespace.MICROSOFT_200502.getQName("RmFlowControl");
    //    
    private static final Logger LOGGER = Logger.getLogger(RmFlowControlAssertion.class);
    private static final QName BUFFER_SIZE_ASSERTION_QNAME = AssertionNamespace.MICROSOFT_200502.getQName("MaxReceiveBufferSize");
    private static final long DEFAULT_DESTINATION_BUFFER_QUOTA = 32;
    //
    private static AssertionInstantiator instantiator = new AssertionInstantiator() {

        public PolicyAssertion newInstance(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) throws AssertionCreationException {
            return new RmFlowControlAssertion(data, assertionParameters, nestedAlternative);
        }
    };

    public static AssertionInstantiator getInstantiator() {
        return instantiator;
    }
    private final long maxBufferSize;

    private RmFlowControlAssertion(AssertionData data, Collection<? extends PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) throws AssertionCreationException {
        super(data, assertionParameters, nestedAlternative);

        long _maxBufferSize = DEFAULT_DESTINATION_BUFFER_QUOTA; // default
        boolean bufferSizeSet = false;
        if (assertionParameters != null) {
            for (PolicyAssertion assertion : assertionParameters) {
                if (BUFFER_SIZE_ASSERTION_QNAME.equals(assertion.getName())) {
                    if (bufferSizeSet) {
                        throw LOGGER.logSevereException(new AssertionCreationException(data, LocalizationMessages.WSRM_1006_MULTIPLE_BUFFER_SIZES_IN_POLICY()));
                    } else {
                        _maxBufferSize = Long.parseLong(assertion.getValue());
                    }
                }
            }
        }
        maxBufferSize = _maxBufferSize;
    }

    public long getMaximumBufferSize() {
        return maxBufferSize;
    }

    public ReliableMessagingFeatureBuilder update(ReliableMessagingFeatureBuilder builder) {
        return builder.destinationBufferQuota(maxBufferSize);
    }

    public boolean isCompatibleWith(RmVersion version) {
        return RmVersion.WSRM200502 == version || RmVersion.WSRM200702 == version;
    }
}
