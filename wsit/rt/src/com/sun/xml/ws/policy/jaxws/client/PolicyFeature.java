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

package com.sun.xml.ws.policy.jaxws.client;

import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.policy.PolicyMap;
import javax.xml.ws.WebServiceFeature;

/**
 * Store a policy map on the endpoint.
 * 
 * This feature should be set on the binding. It does not make sense to set a
 * policy map per port because the map contains the policies for all ports in a
 * WSDL document.
 */
public class PolicyFeature extends WebServiceFeature {

    private static final String id = PolicyMap.class.getName();
    private final PolicyMap policyMap;
    private final WSDLModel wsdlModel;
    private final WSPortInfo portInfo;
    
    /**
     * Creates a new instance of PolicyFeature and sets this feature to enabled.
     *
     * @param map A PolicyMap
     * @param model The associated WSDL model
     * @param port The port
     */
    public PolicyFeature(PolicyMap map, WSDLModel model, WSPortInfo port) {
        this.policyMap = map;
        this.wsdlModel = model;
        this.portInfo = port;
        this.enabled = true;
    }

    /**
     * Returns the ID of this feature.
     *
     * @return The ID of this feature
     */
    public String getID() {
        return this.id;
    }

    /**
     * Returns the PolicyMap stored with this feature instance.
     *
     * @return The PolicyMap stored with this feature instance
     */
    public PolicyMap getPolicyMap() {
        return this.policyMap;
    }
    
    /**
     * Returns the WSDLModel stored with this feature instance.
     *
     * @return The WSDLModel stored with this feature instance
     */
    public WSDLModel getWsdlModel() {
        return this.wsdlModel;
    }

    /**
     * Returns the WSPortInfo stored with this feature instance.
     *
     * @return The WSPortInfo stored with this feature instance
     */
    public WSPortInfo getPortInfo() {
        return this.portInfo;
    }
}
