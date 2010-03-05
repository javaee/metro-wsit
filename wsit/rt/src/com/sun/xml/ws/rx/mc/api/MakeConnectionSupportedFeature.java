/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rx.mc.api;

import com.sun.xml.ws.api.FeatureConstructor;
import javax.xml.ws.WebServiceFeature;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
@ManagedData
public class MakeConnectionSupportedFeature extends WebServiceFeature {

    public static final String ID = "com.sun.xml.ws.rm.MakeConnectionSupportedFeature";
    /**
     * Default response retrieval timeout value [milliseconds]
     */
    public static final long DEFAULT_RESPONSE_RETRIEVAL_TIMEOUT = 600000;
    /**
     * Default base interval between two subsequent MakeConnection requests [milliseconds]
     */
    public static final long DEFAULT_MAKE_CONNECTION_REQUEST_INTERVAL = 2000;

    private final long responseRetrievalTimeout;
    private final long mcRequestBaseInterval;

    /**
     * This constructor is here to satisfy JAX-WS specification requirements
     */
    public MakeConnectionSupportedFeature() {
        this(
                true,
                DEFAULT_MAKE_CONNECTION_REQUEST_INTERVAL,
                DEFAULT_RESPONSE_RETRIEVAL_TIMEOUT);
    }

    /**
     * This constructor is here to satisfy JAX-WS specification requirements
     */
    @FeatureConstructor({
        "enabled"
    })
    public MakeConnectionSupportedFeature(boolean enabled) {
        this(
                enabled,
                DEFAULT_MAKE_CONNECTION_REQUEST_INTERVAL,
                DEFAULT_RESPONSE_RETRIEVAL_TIMEOUT);
    }

    MakeConnectionSupportedFeature(
            boolean enabled,
            long mcRequestBaseInterval,
            long responseRetrievalTimeout) {

        super.enabled = enabled;

        this.mcRequestBaseInterval = mcRequestBaseInterval;
        this.responseRetrievalTimeout = responseRetrievalTimeout;
    }

    @Override
    @ManagedAttribute
    public String getID() {
        return ID;
    }

    /**
     * Specifies which WS-MC version protocol SOAP messages and SOAP message headers should
     * be used for communication between MC source and MC destination
     *
     * @return WS-MC protocol version currently configured for the feature.
     */
    public McProtocolVersion getProtocolVersion() {
        return McProtocolVersion.WSMC200702;
    }

    /**
     * Specifies a timeout for consecutive unsuccessfull response retrievals.
     *
     * @return currently configured timeout for consecutive unsuccessfull response 
     *         retrievals. If not set explicitly, the default value is specified by
     *         {@link #DEFAULT_RESPONSE_RETRIEVAL_TIMEOUT} constant.
     */
    public long getResponseRetrievalTimeout() {
        return responseRetrievalTimeout;
    }

    /**
     * Specifies a base interval between two consecutive MakeConnection requests
     *
     * @return currently configured base interval (in milliseconds) of time that
     *         must pass between two consecutive MakeConnection request messages.
     *         If not set explicitly, the default value is specified by
     *         {@link #DEFAULT_MAKE_CONNECTION_REQUEST_INTERVAL} constant.
     */
    public long getBaseMakeConnectionRequetsInterval() {
        return mcRequestBaseInterval;
    }
}