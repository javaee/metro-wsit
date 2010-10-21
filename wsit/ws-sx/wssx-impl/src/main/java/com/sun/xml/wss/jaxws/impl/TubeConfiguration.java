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

package com.sun.xml.wss.jaxws.impl;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubelineAssembler;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.policy.PolicyMap;

import javax.xml.ws.Dispatch;

/**
 * Entry point to the various configuration information
 * necessary for constructing {@link Tube}s.
 *
 * <p>
 * This object is created by a {@link TubelineAssembler} and
 * passed as a constructor parameter to most pipes,
 * so that they can access configuration information.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TubeConfiguration {
    private final PolicyMap policy;
    private final WSDLPort wsdlPort;

    TubeConfiguration(PolicyMap policy, WSDLPort wsdlPort) {
        this.policy = policy;
        this.wsdlPort = wsdlPort;
    }

    /**
     * Gets the {@link PolicyMap} that represents
     * the policy information applicable to the current pipeline.
     *
     * @return always non-null same object.
     */
    public PolicyMap getPolicyMap() {
        return policy;
    }

    /**
     * Gets the {@link WSDLPort} that represents
     * the WSDL information about the port for which
     * a pipeline is created.
     *
     * <p>
     * This model is present only when the client
     * provided WSDL to the JAX-WS runtime in some means
     * (such as indirectly through SEI or through {@link Dispatch}.)
     *
     * <p>
     * JAX-WS allows modes of operations where no WSDL
     * is available for the current pipeline, and in which
     * case this model is not present.
     *
     * @return null if this model is not present.
     *         If non-null, it's always the same object.
     */
    public WSDLPort getWSDLPort() {
        return wsdlPort;
    }

    /**
     * Gets the applicable {@link WSBinding} for this pipeline.
     *
     * @return always non-null.
     */
    public abstract WSBinding getBinding();
}
