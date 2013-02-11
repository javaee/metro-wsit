/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.rx.mc.policy.spi_impl;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator.Fitness;
import com.sun.xml.ws.rx.mc.policy.McAssertionNamespace;
import com.sun.xml.ws.rx.mc.policy.wsmc200702.MakeConnectionSupportedAssertion;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class McAssertionValidator implements PolicyAssertionValidator {

    private static final ArrayList<QName> SERVER_SIDE_ASSERTIONS = new ArrayList<QName>(1);
    private static final ArrayList<QName> CLIENT_SIDE_ASSERTIONS = new ArrayList<QName>(1);

    private static final List<String> SUPPORTED_DOMAINS = Collections.unmodifiableList(McAssertionNamespace.namespacesList());

    static {
        SERVER_SIDE_ASSERTIONS.add(MakeConnectionSupportedAssertion.NAME);

        // CLIENT_SIDE_ASSERTIONS.add();
        CLIENT_SIDE_ASSERTIONS.addAll(SERVER_SIDE_ASSERTIONS);
    }

    public McAssertionValidator() {
    }

    public Fitness validateClientSide(PolicyAssertion assertion) {
        return CLIENT_SIDE_ASSERTIONS.contains(assertion.getName()) ? Fitness.SUPPORTED : Fitness.UNKNOWN;
    }

    public Fitness validateServerSide(PolicyAssertion assertion) {
        QName assertionName = assertion.getName();
        if (SERVER_SIDE_ASSERTIONS.contains(assertionName)) {
            return Fitness.SUPPORTED;
        } else if (CLIENT_SIDE_ASSERTIONS.contains(assertionName)) {
            return Fitness.UNSUPPORTED;
        } else {
            return Fitness.UNKNOWN;
        }
    }

    public String[] declareSupportedDomains() {
        return SUPPORTED_DOMAINS.toArray(new String[SUPPORTED_DOMAINS.size()]);
    }
}
