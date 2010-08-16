/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.rx.mc.api.McProtocolVersion;
import com.sun.xml.ws.rx.rm.api.RmProtocolVersion;
import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyHolder;
import com.sun.xml.wss.impl.PolicyResolver;
import com.sun.xml.wss.jaxws.impl.PolicyResolverImpl;
import com.sun.xml.wss.jaxws.impl.TubeConfiguration;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author vbkumarjayanti
 */
public class PolicyResolverFactory {

    public static PolicyResolver createPolicyResolver(List<PolicyAlternativeHolder>
            alternatives, WSDLBoundOperation cachedOperation, TubeConfiguration tubeConfig, 
            AddressingVersion addVer, boolean isClient, RmProtocolVersion rmVer, McProtocolVersion mcVer) {
        if (alternatives.size() == 1) {
            return new PolicyResolverImpl(alternatives.get(0).inMessagePolicyMap,
                    alternatives.get(0).inProtocolPM, cachedOperation,tubeConfig,addVer, isClient, rmVer,mcVer);
        } else {
            return new AlternativesBasedPolicyResolver(alternatives,cachedOperation,tubeConfig,addVer, isClient, rmVer,mcVer);
        }
    }

    public static PolicyResolver createPolicyResolver(HashMap<WSDLBoundOperation, SecurityPolicyHolder> inMessagePolicyMap,
            HashMap<String, SecurityPolicyHolder> ip,
            WSDLBoundOperation cachedOperation, TubeConfiguration tubeConfig,
            AddressingVersion addVer, boolean isClient, RmProtocolVersion rmVer, McProtocolVersion mcVer) {

            return new PolicyResolverImpl(inMessagePolicyMap,ip ,
                    cachedOperation,tubeConfig,addVer, isClient, rmVer,mcVer);

    }

}
