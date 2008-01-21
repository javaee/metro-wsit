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
package com.sun.xml.ws.rm.policy.assertion;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.ComplexAssertion;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import java.util.Collection;
import javax.xml.namespace.QName;

/**
 * <ms:RmFlowControl>
 *   <ms:MaxReceiveBufferSize>value</ms:MaxReceiveBufferSize>
 * </ms:RmFlowControl>
 */
/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class RmFlowControlAssertion extends ComplexAssertion {

    public static final QName NAME = new QName(Constants.microsoftVersion, "RmFlowControl");
    private static final RmLogger LOGGER = RmLogger.getLogger(RmFlowControlAssertion.class);
    private static final QName BUFFER_SIZE_ASSERTION_QNAME = new QName(Constants.microsoftVersion, "MaxReceiveBufferSize");
    private static RmAssertionInstantiator instantiator = new RmAssertionInstantiator() {

        public PolicyAssertion newInstance(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) throws AssertionCreationException {
            return new RmFlowControlAssertion(data, assertionParameters, nestedAlternative);
        }
    };

    public static RmAssertionInstantiator getInstantiator() {
        return instantiator;
    }
    private final long maxBufferSize;

    private RmFlowControlAssertion(AssertionData data, Collection<? extends PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) throws AssertionCreationException {
        super(data, assertionParameters, nestedAlternative);

        long _maxBufferSize = Configuration.UNSPECIFIED; // default

        if (nestedAlternative != null) {
            for (PolicyAssertion assertion : nestedAlternative) {
                if (BUFFER_SIZE_ASSERTION_QNAME.equals(assertion.getName())) {
                    _maxBufferSize = evaluateBufferSize(_maxBufferSize == Configuration.UNSPECIFIED, assertion.getValue(), data);
                }
            }
        }
        maxBufferSize = (_maxBufferSize == Configuration.UNSPECIFIED) ? Configuration.DEFAULT_DESTINATION_BUFFER_QUOTA : _maxBufferSize;
    }

    public long getMaximumBufferSize() {
        return maxBufferSize;
    }

    private long evaluateBufferSize(boolean successCondition, String valueOnSuccess, AssertionData data) throws AssertionCreationException {
        if (successCondition) {
            return Long.parseLong(valueOnSuccess);
        } else {
            // TODO L10N
            throw LOGGER.logSevereException(new AssertionCreationException(data, "Inconsistent RM policy: Multiple flow control buffer sizes specified."));
        }
    }
}
