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

import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.assembler.PipeConfiguration;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.impl.policyconv.SCTokenWrapper;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.secconv.WSSCConstants;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.wss.impl.PolicyResolver;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyHolder;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.impl.MessageConstants;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import org.w3c.dom.Node;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;
import java.util.HashMap;
import com.sun.xml.wss.impl.misc.PolicyAttributes;
import org.w3c.dom.NodeList;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.api.addressing.*;

import static com.sun.xml.wss.jaxws.impl.Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS;
import static com.sun.xml.wss.jaxws.impl.Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS;
import static com.sun.xml.wss.jaxws.impl.Constants.RM_CREATE_SEQ;
import static com.sun.xml.wss.jaxws.impl.Constants.RM_CREATE_SEQ_RESP;
import static com.sun.xml.wss.jaxws.impl.Constants.RM_SEQ_ACK;
import static com.sun.xml.wss.jaxws.impl.Constants.RM_TERMINATE_SEQ;
import static com.sun.xml.wss.jaxws.impl.Constants.RM_LAST_MESSAGE;
import static com.sun.xml.wss.jaxws.impl.Constants.SC_ASSERTION;
import static com.sun.xml.wss.jaxws.impl.Constants.rstSCTURI;
import static com.sun.xml.wss.jaxws.impl.Constants.rstrSCTURI;
import static com.sun.xml.wss.jaxws.impl.Constants.JAXWS_21_MESSAGE;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */

public class PolicyResolverImpl implements PolicyResolver{
    
    private WSDLBoundOperation cachedOperation = null;
    private HashMap<WSDLBoundOperation,SecurityPolicyHolder> inMessagePolicyMap = null;
    private HashMap<String,SecurityPolicyHolder> inProtocolPM = null;
    
    //private PolicyAttributes pa = null;
    private AddressingVersion addVer = null;
    private PipeConfiguration pipeConfig = null;
    private boolean isClient = false;
    private boolean isSCMessage = false;
    //private boolean isTrustOrSCMessage = false;
    private String action =  "";
    /**
     * Creates a new instance of OperationResolverImpl
     */
    
    public PolicyResolverImpl(HashMap<WSDLBoundOperation,SecurityPolicyHolder> inMessagePolicyMap,HashMap<String,SecurityPolicyHolder> ip ,WSDLBoundOperation cachedOperation,PipeConfiguration pipeConfig,AddressingVersion addVer,boolean isClient) {
        this.inMessagePolicyMap = inMessagePolicyMap;
        this.inProtocolPM = ip;
        this.cachedOperation = cachedOperation;
        this.pipeConfig = pipeConfig;
        this.addVer = addVer;
        this.isClient = isClient;
    }
    
    public MessagePolicy resolvePolicy(ProcessingContext ctx){
        Message msg = (Message)ctx.getExtraneousProperty(JAXWS_21_MESSAGE);
        Packet packet = null;
        MessagePolicy mp = null;
        SOAPMessage soapMsg = null;
        if(msg == null){
            if(ctx instanceof JAXBFilterProcessingContext){
                msg = ((JAXBFilterProcessingContext)ctx).getJAXWSMessage();
            } else{
                soapMsg = ctx.getSOAPMessage();
                msg = Messages.create(soapMsg);
            }
            ctx.setExtraneousProperty(JAXWS_21_MESSAGE,msg);
        }
        action = getAction(msg);
        if (isRMMessage()) {
            SecurityPolicyHolder holder = inProtocolPM.get("RM");
            return holder.getMessagePolicy();
            
        }
        
        if(isSCCancel()){
            SecurityPolicyHolder holder = inProtocolPM.get("SC");
            return holder.getMessagePolicy();
        }
        isSCMessage = isSCMessage();
        if (isSCMessage ) {
            Token scToken = (Token)getInBoundSCP();
            return getInboundXWSBootstrapPolicy(scToken);
        }
        
        if (msg.isFault()) {
            if(soapMsg == null){
                try {
                    soapMsg = msg.readAsSOAPMessage();
                } catch (SOAPException ex) {
                    //ex.printStackTrace();
                }
            }
            mp = getInboundFaultPolicy(soapMsg);
        }  else{
            mp =  getInboundXWSSecurityPolicy(msg);
        }
        
        if(mp == null){
            return new MessagePolicy();
        }
        return mp;
    }
    
    
    protected PolicyAssertion getInBoundSCP(){
        
        SecurityPolicyHolder sph = null;
        Collection coll = inMessagePolicyMap.values();
        Iterator itr = coll.iterator();
        
        while(itr.hasNext()){
            SecurityPolicyHolder ph = (SecurityPolicyHolder) itr.next();
            if(ph != null){
                sph = ph;
                break;
            }
        }
        if(sph == null){
            return null;
        }
        List<PolicyAssertion> policies = sph.getSecureConversationTokens();
        if(!policies.isEmpty()) {
            return (PolicyAssertion)policies.get(0);
        }
        return null;
    }
    
    private MessagePolicy getInboundXWSSecurityPolicy(Message msg) {
        MessagePolicy mp = null;
        
        
        
        //Review : Will this return operation name in all cases , doclit,rpclit, wrap / non wrap ?
        WSDLBoundOperation operation = null;
        if(cachedOperation != null){
            operation = cachedOperation;
        }else{
            operation = msg.getOperation(pipeConfig.getWSDLModel());
            if(operation == null)
                operation = getWSDLOpFromAction();
        }
        
        SecurityPolicyHolder sph = (SecurityPolicyHolder) inMessagePolicyMap.get(operation);
        //TODO: pass isTrustMessage Flag to this method later
        if (sph == null && (isTrustMessage() || isSCMessage)) {
            operation = getWSDLOpFromAction();
            sph = (SecurityPolicyHolder) inMessagePolicyMap.get(operation);
        }
        if(sph == null){
            return null;
        }
        
        mp = sph.getMessagePolicy();
        
        return mp;
    }
    
    
    private MessagePolicy getInboundFaultPolicy(SOAPMessage msg) {
        if(cachedOperation != null){
            WSDLOperation operation = cachedOperation.getOperation();
            try{
                SOAPBody body = msg.getSOAPBody();
                NodeList nodes = body.getElementsByTagName("detail");
                if(nodes.getLength() == 0){
                    nodes = body.getElementsByTagNameNS(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE,"Detail");
                }
                if(nodes.getLength() >0){
                    Node node = nodes.item(0);
                    Node faultNode = node.getFirstChild();
                    if(faultNode == null){
                        return new MessagePolicy();
                    }
                    String uri = faultNode.getNamespaceURI();
                    QName faultDetail = null;
                    if(uri != null && uri.length() >0){
                        faultDetail = new QName(faultNode.getNamespaceURI(),faultNode.getNodeName());
                    }else{
                        faultDetail = new QName(faultNode.getNodeName());
                    }
                    WSDLFault fault = operation.getFault(faultDetail);
                    SecurityPolicyHolder sph = inMessagePolicyMap.get(cachedOperation);
                    SecurityPolicyHolder faultPolicyHolder = sph.getFaultPolicy(fault);
                    MessagePolicy faultPolicy = (faultPolicyHolder == null) ? new MessagePolicy() : faultPolicyHolder.getMessagePolicy();
                    return faultPolicy;
                }
            }catch(SOAPException sx){
                //sx.printStackTrace();
                //log error
            }
        }
        return new MessagePolicy();
        
    }
    
    private boolean isTrustMessage(){
        if(WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION.equals(action) ||
                WSTrustConstants.REQUEST_SECURITY_TOKEN_RESPONSE_ISSUE_ACTION.equals(action)){
            return true;
        }
        return false;
        
    }
    
    private boolean isRMMessage(){
        if (RM_CREATE_SEQ.equals(action) || RM_CREATE_SEQ_RESP.equals(action)
        || RM_SEQ_ACK.equals(action) || RM_TERMINATE_SEQ.equals(action)
        || RM_LAST_MESSAGE.equals(action)) {
            return true;
        }
        return false;
    }
    
    private String getAction(Message msg){
        if(addVer != null){
            HeaderList hl = msg.getHeaders();
            String action =  hl.getAction(addVer, pipeConfig.getBinding().getSOAPVersion());
            return action;
        }
        return "";
        
    }
    
    private MessagePolicy getInboundXWSBootstrapPolicy(Token scAssertion) {
        return ((SCTokenWrapper)scAssertion).getMessagePolicy();
    }
    
    private boolean isSCMessage(){
        if (rstSCTURI.equals(action) || rstrSCTURI.equals(action)){
            return true;
        }
        return false;
    }
    
    private boolean isSCCancel(){
        
        if(WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION.equals(action) ||
                WSSCConstants.CANCEL_SECURITY_CONTEXT_TOKEN_ACTION .equals(action)) {
            return true;
        }
        return false;
    }
    
    private String getAction(WSDLOperation operation){
        if(!isClient){
            return operation.getInput().getAction();
        }else{
            return operation.getOutput().getAction();
        }
    }
    
    private WSDLBoundOperation getWSDLOpFromAction(){
        Set <WSDLBoundOperation>keys = inMessagePolicyMap.keySet();
        for(WSDLBoundOperation wbo : keys){
            WSDLOperation wo = wbo.getOperation();
            // WsaWSDLOperationExtension extensions = wo.getExtension(WsaWSDLOperationExtension.class);
            String confAction = getAction(wo);
            if(confAction != null && confAction.equals(action)){
                return wbo;
            }
        }
        return null;
    }
    
}

