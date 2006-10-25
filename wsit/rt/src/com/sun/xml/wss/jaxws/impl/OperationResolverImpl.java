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


package com.sun.xml.wss.jaxws.impl;



import com.sun.xml.wss.impl.OperationResolver;

import com.sun.xml.wss.impl.policy.mls.MessagePolicy;

import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;

import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;

import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyHolder;
import com.sun.xml.wss.ProcessingContext;

import com.sun.xml.wss.impl.MessageConstants;



import org.w3c.dom.Node;



import javax.xml.soap.SOAPMessage; 

import javax.xml.soap.SOAPException;



import java.util.HashMap;



/**

 *

 * @author Ashutosh.Shahi@sun.com

 */

public class OperationResolverImpl implements OperationResolver{

    

    private HashMap inMessagePolicyMap;

    private WSDLBoundPortType boundPortType;

    

    /**

     * Creates a new instance of OperationResolverImpl

     */

    public OperationResolverImpl(HashMap inMessagePolicyMap, WSDLBoundPortType boundPortType) {

        this.inMessagePolicyMap = inMessagePolicyMap;

        this.boundPortType = boundPortType;

    }

    

    public MessagePolicy resolveOperationPolicy(ProcessingContext ctx){

        //TODO: venu, make it more generic.
        SOAPMessage message = ctx.getSOAPMessage();
        String uri = null;

        String localName = null;

        try{

            Node opNode = message.getSOAPBody().getFirstChild();

            if(opNode != null){

                uri = opNode.getNamespaceURI();

                localName = opNode.getLocalName();

                if(localName.equals("Fault") && 

                        (uri.equals(MessageConstants.SOAP_1_1_NS) || uri.equals(MessageConstants.SOAP_1_2_NS)))

                    return null;

            } else

                return null;

        } catch(SOAPException se){

            

        }

        WSDLBoundOperation boundOpeartion = boundPortType.getOperation(uri, localName);

        SecurityPolicyHolder policyHolder = (SecurityPolicyHolder)inMessagePolicyMap.get(boundOpeartion);

        //TODO: this is a workaround for Protocol Messages

        if (policyHolder == null) {

            return null;

        }

        return policyHolder.getMessagePolicy();

    }

    

}

