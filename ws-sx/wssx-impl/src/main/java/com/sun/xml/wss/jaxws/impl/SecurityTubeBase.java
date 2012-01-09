/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.xml.ws.api.addressing.*;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.policy.ModelUnmarshaller;
import com.sun.xml.ws.security.message.stream.LazyStreamBasedMessage;
import com.sun.xml.ws.security.impl.policyconv.XWSSPolicyGenerator;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.policy.CertStoreConfig;
import com.sun.xml.ws.security.policy.KerberosConfig;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.policy.ModelTranslator;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.security.impl.policyconv.SCTokenWrapper;
import com.sun.xml.ws.security.impl.policyconv.SecurityPolicyHolder;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicyMerger;
import com.sun.xml.ws.security.policy.AsymmetricBinding;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.SymmetricBinding;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.rx.mc.api.McProtocolVersion;
import com.sun.xml.ws.rx.rm.api.RmProtocolVersion;
import com.sun.xml.ws.security.opt.impl.util.CertificateRetriever;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.KeyStore;
import com.sun.xml.ws.security.policy.TrustStore;
import com.sun.xml.ws.security.policy.CallbackHandlerConfiguration;
import com.sun.xml.ws.security.policy.Validator;
import com.sun.xml.ws.security.policy.ValidatorConfiguration;
import com.sun.xml.ws.security.policy.WSSAssertion;
import com.sun.xml.ws.security.secconv.WSSCVersion;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.ProcessingContextImpl;
import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.impl.NewSecurityRecipient;
import com.sun.xml.wss.impl.PolicyViolationException;
import com.sun.xml.wss.XWSSConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.misc.DefaultCallbackHandler;
import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.policy.spi.PolicyVerifier;
import com.sun.xml.wss.jaxws.impl.logging.LogDomainConstants;
import com.sun.xml.wss.jaxws.impl.logging.LogStringsMessages;
import com.sun.xml.wss.provider.wsit.PolicyAlternativeHolder;
import static com.sun.xml.wss.jaxws.impl.Constants.SC_ASSERTION;
import static com.sun.xml.wss.jaxws.impl.Constants.bsOperationName;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Set;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *  @author Vbkumar.Jayanti@Sun.COM, shyam.rao@sun.com
 */
public abstract class SecurityTubeBase extends AbstractFilterTubeImpl {

    protected static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSS_JAXWS_IMPL_DOMAIN,
            LogDomainConstants.WSS_JAXWS_IMPL_DOMAIN_BUNDLE);
    //protected Tube nextTube;
    // TODO: Optimized flag to be set based on some conditions (no SignedElements/EncryptedElements)
    protected boolean optimized = true;
    protected boolean transportOptimization = false;
    // Per-Proxy State for SecureConversation sessions
    // as well as IssuedTokenContext returned by invoking a Trust-Plugin
    // This map stores IssuedTokenContext against the Policy-Id
    protected Hashtable<String, IssuedTokenContext> issuedTokenContextMap = new Hashtable<String, IssuedTokenContext>();
    protected TubeConfiguration tubeConfig = null;
    //static JAXBContext used across the Tube
    protected static JAXBContext jaxbContext;
    protected WSSCVersion wsscVer;
    protected WSTrustVersion wsTrustVer;
    protected RmProtocolVersion rmVer = RmProtocolVersion.WSRM200502;
    protected McProtocolVersion mcVer = McProtocolVersion.WSMC200702;
    protected boolean disablePayloadBuffer = false;
    protected AlgorithmSuite bindingLevelAlgSuite = null;
    private final QName EPREnabled = new QName("http://schemas.sun.com/2006/03/wss/server", "EnableEPRIdentity");
    private final QName encSCServerCancel = new QName("http://schemas.sun.com/2006/03/wss/server", "EncSCCancel");
    private final QName encSCClientCancel = new QName("http://schemas.sun.com/2006/03/wss/client", "EncSCCancel");
    private final QName optServerSecurity = new QName("http://schemas.sun.com/2006/03/wss/server", "DisableStreamingSecurity");
    private final QName optClientSecurity = new QName("http://schemas.sun.com/2006/03/wss/client", "DisableStreamingSecurity");
    private final QName disableSPBuffering = new QName("http://schemas.sun.com/2006/03/wss/server", "DisablePayloadBuffering");
    private final QName disableCPBuffering = new QName("http://schemas.sun.com/2006/03/wss/client", "DisablePayloadBuffering");
    protected boolean disableIncPrefix = false;
    private final QName disableIncPrefixServer = new QName("http://schemas.sun.com/2006/03/wss/server", "DisableInclusivePrefixList");
    private final QName disableIncPrefixClient = new QName("http://schemas.sun.com/2006/03/wss/client", "DisableInclusivePrefixList");
    protected boolean encHeaderContent = false;
    private final QName encHeaderContentServer = new QName("http://schemas.sun.com/2006/03/wss/server", "EncryptHeaderContent");
    private final QName encHeaderContentClient = new QName("http://schemas.sun.com/2006/03/wss/client", "EncryptHeaderContent");
    private final QName bsp10Server = new QName("http://schemas.sun.com/2006/03/wss/server", "BSP10");
    private final QName bsp10Client = new QName("http://schemas.sun.com/2006/03/wss/client", "BSP10");
    protected boolean bsp10 = false;
    protected boolean allowMissingTimestamp = false;
    private final QName allowMissingTSServer = new QName("http://schemas.sun.com/2006/03/wss/server", "AllowMissingTimestamp");
    private final QName allowMissingTSClient = new QName("http://schemas.sun.com/2006/03/wss/client", "AllowMissingTimestamp");
    protected boolean securityMUValue = true;
    private final QName unsetSecurityMUValueServer = new QName("http://schemas.sun.com/2006/03/wss/server", "UnsetSecurityMUValue");
    private final QName unsetSecurityMUValueClient = new QName("http://schemas.sun.com/2006/03/wss/client", "UnsetSecurityMUValue");
    protected boolean encRMLifecycleMsg = false;
    private final QName encRMLifecycleMsgServer = new QName("http://schemas.sun.com/2006/03/wss/server", "EncryptRMLifecycleMessage");
    private final QName encRMLifecycleMsgClient = new QName("http://schemas.sun.com/2006/03/wss/client", "EncryptRMLifecycleMessage");
    protected static final ArrayList<String> securityPolicyNamespaces;
    protected static final List<PolicyAssertion> EMPTY_LIST = Collections.emptyList();
    // Security Environment reference initialized with a JAAS CallbackHandler
    protected SecurityEnvironment secEnv = null;
    // debug the Secure SOAP Messages (enable dumping)
    protected static final boolean debug = false;
    // SOAP version
    protected boolean isSOAP12 = false;
    protected SOAPVersion soapVersion = null;
    // SOAP Factory
    protected SOAPFactory soapFactory = null;
    protected PolicyMap wsPolicyMap = null;
    //public static final URI ISSUE_REQUEST_URI ;
    //public static final URI CANCEL_REQUEST_URI;
    protected Policy bpMSP = null;
    //milliseconds
    protected long timestampTimeOut = 0;
    protected int iterationsForPDK = 0;
    protected boolean isEPREnabled = false;
    protected boolean isCertValidityVerified = false;
    protected List<PolicyAlternativeHolder> policyAlternatives = new ArrayList<PolicyAlternativeHolder>();
    /**
     * Constants for RM Security Processing
     */
    protected WSDLBoundOperation cachedOperation = null;
    protected Policy wsitConfig = null;
    // store as instance variable
    protected Marshaller marshaller = null;
    protected Unmarshaller unmarshaller = null;
    // store operation resolver
    // protected OperationResolver opResolver = null;
    //store instance variable(s): Binding has IssuedToken/RM/SC Policy
    boolean hasIssuedTokens = false;
    boolean hasSecureConversation = false;
    boolean hasReliableMessaging = false;
    boolean hasMakeConnection = false;
    boolean hasKerberosToken = false;
    //boolean addressingEnabled = false;
    AddressingVersion addVer = null;
    // Security Policy version
    protected SecurityPolicyVersion spVersion = null;
    protected static final String WSDLPORT = "WSDLPort";
    protected static final String WSENDPOINT = "WSEndpoint";
    //flag used as temporary variable for each run
    //boolean isTrustOrSCMessage = false;
    protected X509Certificate serverCert = null;
    private boolean encryptCancelPayload = false;
    private Policy cancelMSP;
    protected boolean isCertValid;
    private AlgorithmSuite bootStrapAlgoSuite;


    static {
        try {
            //TODO: system property maynot be appropriate for server side.
            //debug = Boolean.valueOf(System.getProperty("DebugSecurity"));
            //ISSUE_REQUEST_URI = new URI(WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION);
            //CANCEL_REQUEST_URI = new URI(WSTrustConstants.CANCEL_REQUEST);
            //jaxbContext = WSTrustElementFactory.getContext();            
            securityPolicyNamespaces = new ArrayList<String>();
            securityPolicyNamespaces.add(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SecurityTubeBase(TubeConfiguration config, Tube nextTube) {
        super(nextTube);
        this.tubeConfig = config;
        soapVersion = tubeConfig.getBinding().getSOAPVersion();
        //addressingEnabled = (pipeConfig.getBinding().getAddressingVersion() == null) ?  false : true;
        isSOAP12 = (soapVersion == SOAPVersion.SOAP_12);
        wsPolicyMap = tubeConfig.getPolicyMap();
        soapFactory = tubeConfig.getBinding().getSOAPVersion().saajSoapFactory;
        //unmarshaller as instance variable of the pipe
        if (wsPolicyMap != null) {
            collectPolicies(policyAlternatives);
        }

        try {
            jaxbContext = WSTrustElementFactory.getContext(wsTrustVer);
            this.marshaller = jaxbContext.createMarshaller();
            this.unmarshaller = jaxbContext.createUnmarshaller();
        } catch (javax.xml.bind.JAXBException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSSTUBE_0001_PROBLEM_MAR_UNMAR(), ex);
            throw new RuntimeException(LogStringsMessages.WSSTUBE_0001_PROBLEM_MAR_UNMAR(), ex);
        }

        //unmarshaller = jaxbContext.createUnmarshaller();
        // check whether Service Port has RM
        hasReliableMessaging = isReliableMessagingEnabled(tubeConfig.getWSDLPort());
        hasMakeConnection = isMakeConnectionEnabled(tubeConfig.getWSDLPort());
    //   opResolver = new OperationResolverImpl(inMessagePolicyMap,pipeConfig.getWSDLModel().getBinding());

    }

    protected SecurityTubeBase(SecurityTubeBase that, TubeCloner cloner) {
        super(that, cloner);
        tubeConfig = that.tubeConfig;
        transportOptimization = that.transportOptimization;
        optimized = that.optimized;
        disableIncPrefix = that.disableIncPrefix;
        allowMissingTimestamp = that.allowMissingTimestamp;
        securityMUValue = that.securityMUValue;
        encHeaderContent = that.encHeaderContent;
        issuedTokenContextMap = that.issuedTokenContextMap;
        secEnv = that.secEnv;
        isSOAP12 = that.isSOAP12;
        soapVersion = that.soapVersion;
        this.spVersion = that.spVersion;
        this.soapFactory = that.soapFactory;
        this.addVer = that.addVer;
        this.wsTrustVer = that.wsTrustVer;
        this.wsscVer = that.wsscVer;
        this.rmVer = that.rmVer;
        this.mcVer = that.mcVer;
        this.encRMLifecycleMsg = that.encRMLifecycleMsg;
        wsPolicyMap = that.wsPolicyMap;
        this.policyAlternatives = that.policyAlternatives;
        bindingLevelAlgSuite = that.bindingLevelAlgSuite;
        this.hasIssuedTokens = that.hasIssuedTokens;
        this.hasKerberosToken = that.hasKerberosToken;
        this.hasSecureConversation = that.hasSecureConversation;
        this.hasReliableMessaging = that.hasReliableMessaging;
        this.hasMakeConnection = that.hasMakeConnection;
        //this.opResolver = that.opResolver;
        this.timestampTimeOut = that.timestampTimeOut;
        this.iterationsForPDK = that.iterationsForPDK;
        this.serverCert = that.serverCert;
        this.isCertValidityVerified = that.isCertValidityVerified;
        this.isCertValid = that.isCertValid;
        this.cancelMSP = that.cancelMSP;
        this.encryptCancelPayload = that.encryptCancelPayload;
        try {
            this.marshaller = WSTrustElementFactory.getContext(this.wsTrustVer).createMarshaller();
            this.unmarshaller = WSTrustElementFactory.getContext(this.wsTrustVer).createUnmarshaller();
        } catch (javax.xml.bind.JAXBException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSSTUBE_0001_PROBLEM_MAR_UNMAR(), ex);
            throw new RuntimeException("Problem creating JAXB Marshaller/Unmarshaller", ex);
        }
    }

    protected SOAPMessage secureOutboundMessage(SOAPMessage message, ProcessingContext ctx) {
        try {
            ctx.setSOAPMessage(message);
            SecurityAnnotator.secureMessage(ctx);
            return ctx.getSOAPMessage();
        } catch (WssSoapFaultException soapFaultException) {
            throw getSOAPFaultException(soapFaultException);
        } catch (XWSSecurityException xwse) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0024_ERROR_SECURING_OUTBOUND_MSG(), xwse);
            WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INTERNAL_SERVER_ERROR,
                    xwse.getMessage(), xwse);
            throw wsfe;
        }
    }

    protected RuntimeException generateInternalError(PolicyException ex) {
        SOAPFault fault;
        try {
            if (isSOAP12) {
                fault = soapFactory.createFault(ex.getMessage(), SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(MessageConstants.WSSE_INTERNAL_SERVER_ERROR);
            } else {
                fault = soapFactory.createFault(ex.getMessage(), MessageConstants.WSSE_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSSTUBE_0002_INTERNAL_SERVER_ERROR(), e);
            throw new RuntimeException(LogStringsMessages.WSSTUBE_0002_INTERNAL_SERVER_ERROR(), e);
        }
        return new SOAPFaultException(fault);
    }

    protected Message secureOutboundMessage(Message message, ProcessingContext ctx) {
        try {
            JAXBFilterProcessingContext context = (JAXBFilterProcessingContext) ctx;
            context.setSOAPVersion(soapVersion);
            context.setAllowMissingTimestamp(allowMissingTimestamp);
            context.setMustUnderstandValue(securityMUValue);
            context.setWSSAssertion(((MessagePolicy) ctx.getSecurityPolicy()).getWSSAssertion());
            context.setJAXWSMessage(message, soapVersion);
            context.isOneWayMessage(message.isOneWay(this.tubeConfig.getWSDLPort()));
            context.setDisableIncPrefix(disableIncPrefix);
            context.setEncHeaderContent(encHeaderContent);
            context.setBSP(bsp10);
            SecurityAnnotator.secureMessage(context);
            return context.getJAXWSMessage();
        } catch (XWSSecurityException xwse) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0024_ERROR_SECURING_OUTBOUND_MSG(), xwse);
            WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INTERNAL_SERVER_ERROR,
                    xwse.getMessage(), xwse);
            throw wsfe;
        }
    }

    protected SOAPMessage verifyInboundMessage(SOAPMessage message, ProcessingContext ctx)
            throws WssSoapFaultException, XWSSecurityException {
        try {
            ctx.setSOAPMessage(message);
            if (debug) {
                DumpFilter.process(ctx);
            }
            NewSecurityRecipient.validateMessage(ctx);
            return ctx.getSOAPMessage();
        } catch (WssSoapFaultException soapFaultException) {
            throw getSOAPFaultException(soapFaultException);
        } catch (XWSSecurityException xwse) {
            //TODO: MISSING-LOG
            WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INTERNAL_SERVER_ERROR,
                    xwse.getMessage(), xwse);
            throw getSOAPFaultException(wsfe);
        }
    }

    protected Message verifyInboundMessage(Message message, ProcessingContext ctx) throws XWSSecurityException {
        JAXBFilterProcessingContext context = (JAXBFilterProcessingContext) ctx;
        context.setDisablePayloadBuffering(disablePayloadBuffer);
        context.setDisableIncPrefix(disableIncPrefix);
        if (((MessagePolicy) ctx.getSecurityPolicy()) != null) {
            context.setWSSAssertion(((MessagePolicy) ctx.getSecurityPolicy()).getWSSAssertion());
        }
        context.setAllowMissingTimestamp(allowMissingTimestamp);
        context.setMustUnderstandValue(securityMUValue);
        context.setEncHeaderContent(encHeaderContent);
        context.setBSP(bsp10);
        //  context.setJAXWSMessage(message, soapVersion);
        if (debug) {
            try {
                ((LazyStreamBasedMessage) message).print();
            } catch (XMLStreamException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSSTUBE_0003_PROBLEM_PRINTING_MSG(), ex);
                throw new XWSSecurityException(LogStringsMessages.WSSTUBE_0003_PROBLEM_PRINTING_MSG(), ex);
            }
        }
        LazyStreamBasedMessage lazyStreamMessage = (LazyStreamBasedMessage) message;
        AttachmentSet attachSet = lazyStreamMessage.getAttachments();
        com.sun.xml.ws.security.opt.impl.incoming.SecurityRecipient recipient;
        if (attachSet == null || attachSet.isEmpty()) {
            recipient =
                    new com.sun.xml.ws.security.opt.impl.incoming.SecurityRecipient(lazyStreamMessage.readMessage(), soapVersion);
        } else {
            recipient = new com.sun.xml.ws.security.opt.impl.incoming.SecurityRecipient(lazyStreamMessage.readMessage(), soapVersion, attachSet);
        }

        return recipient.validateMessage(context);
    }

    protected List<PolicyAssertion> getIssuedTokenPoliciesFromBootstrapPolicy(
            Token scAssertion) {
        SCTokenWrapper token = (SCTokenWrapper) scAssertion;
        return token.getIssuedTokens();
    }

    protected List<PolicyAssertion> getKerberosTokenPoliciesFromBootstrapPolicy(Token scAssertion) {
        SCTokenWrapper token = (SCTokenWrapper) scAssertion;
        return token.getKerberosTokens();
    }

    protected MessagePolicy getOutgoingXWSSecurityPolicy(
            Packet packet, boolean isSCMessage) {


        if (isSCMessage) {
            Token scToken = (Token) packet.invocationProperties.get(SC_ASSERTION);
            return getOutgoingXWSBootstrapPolicy(scToken);
        }
        Message message = packet.getMessage();
        WSDLBoundOperation operation;
        if (isTrustMessage(packet)) {
            operation = getWSDLOpFromAction(packet, false);
        } else {
            operation = message.getOperation(tubeConfig.getWSDLPort());
        }

        //Review : Will this return operation name in all cases , doclit,rpclit, wrap / non wrap ?

        MessagePolicy mp = null;
        PolicyAlternativeHolder applicableAlternative =
                resolveAlternative(packet, isSCMessage);
        //if(operation == null){
        //Body could be encrypted. Security will have to infer the
        //policy from the message till the Body is decrypted.
        //    mp =  new MessagePolicy();
        //}
        if (applicableAlternative.getOutMessagePolicyMap() == null) {
            //empty message policy
            return new MessagePolicy();
        }
        SecurityPolicyHolder sph =
                (SecurityPolicyHolder) applicableAlternative.getOutMessagePolicyMap().get(operation);
        if (sph == null) {
            return new MessagePolicy();
        }
        mp = sph.getMessagePolicy();
        return mp;
    }

    protected WSDLBoundOperation getOperation(Message message) {
        if (cachedOperation == null) {
            cachedOperation = message.getOperation(tubeConfig.getWSDLPort());
        }
        return cachedOperation;
    }

    protected MessagePolicy getInboundXWSBootstrapPolicy(Token scAssertion) {
        return ((SCTokenWrapper) scAssertion).getMessagePolicy();
    }

    protected MessagePolicy getOutgoingXWSBootstrapPolicy(Token scAssertion) {
        return ((SCTokenWrapper) scAssertion).getMessagePolicy();
    }

    @SuppressWarnings("unchecked")
    protected ProcessingContext initializeInboundProcessingContext(
            Packet packet /*, boolean isSCMessage*/) {
        ProcessingContextImpl ctx;
        if (optimized) {
            ctx = new JAXBFilterProcessingContext(packet.invocationProperties);
            ((JAXBFilterProcessingContext) ctx).setAddressingVersion(addVer);
            ((JAXBFilterProcessingContext) ctx).setSOAPVersion(soapVersion);
            ((JAXBFilterProcessingContext) ctx).setSecure(packet.wasTransportSecure);
            ((JAXBFilterProcessingContext) ctx).setBSP(bsp10);

        } else {
            ctx = new ProcessingContextImpl(packet.invocationProperties);
        }
        if (isSCRenew(packet)) {
            ctx.isExpired(true);
        }

        String action = null;
        if (addVer != null) {
            action = getAction(packet);
            ctx.setAction(action);
        }
        // Set the SecurityPolicy version namespace in processingContext 
        ctx.setSecurityPolicyVersion(spVersion.namespaceUri);
        //ctx.setIssuedTokenContextMap(issuedTokenContextMap);
        ctx.setiterationsForPDK(this.iterationsForPDK);

        if ((action != null && (action.contains("/RST/SCT") || action.contains("/RSTR/SCT"))) && this.bootStrapAlgoSuite != null) {
            ctx.setAlgorithmSuite(getAlgoSuite(this.bootStrapAlgoSuite));
        } else {
            ctx.setAlgorithmSuite(getAlgoSuite(getBindingAlgorithmSuite(packet)));
        }
        //set the server certificate in the context ;
        if (serverCert != null) {
            if (isCertValidityVerified == false) {
                CertificateRetriever cr = new CertificateRetriever();
                isCertValid = cr.setServerCertInTheContext(ctx, secEnv, serverCert);
                cr = null;
                isCertValidityVerified = true;
            } else {
                if (isCertValid == true) {
                    ctx.getExtraneousProperties().put(XWSSConstants.SERVER_CERTIFICATE_PROPERTY, serverCert);
                }
            }
        }
        // setting a flag if issued tokens present
        ctx.hasIssuedToken(bindingHasIssuedTokenPolicy());
        ctx.setSecurityEnvironment(secEnv);
        ctx.isInboundMessage(true);
        if (isTrustMessage(packet)) {
            ctx.isTrustMessage(true);
        }
        if (tubeConfig.getWSDLPort() != null) {
            ctx.getExtraneousProperties().put(SecurityTubeBase.WSDLPORT, tubeConfig.getWSDLPort());
        }
        if (tubeConfig instanceof ServerTubeConfiguration) {
            ctx.getExtraneousProperties().put(SecurityTubeBase.WSENDPOINT, ((ServerTubeConfiguration) tubeConfig).getEndpoint());
        }
        return ctx;
    }

    protected boolean bindingHasIssuedTokenPolicy() {
        return hasIssuedTokens;
    }

    protected boolean bindingHasSecureConversationPolicy() {
        return hasSecureConversation;
    }

    protected boolean bindingHasRMPolicy() {
        return hasReliableMessaging;
    }

    protected boolean hasKerberosTokenPolicy() {
        return hasKerberosToken;
    }

    @SuppressWarnings("unchecked")
    protected ProcessingContext initializeOutgoingProcessingContext(
            Packet packet, boolean isSCMessage) {
        ProcessingContextImpl ctx;
        if (optimized) {
            ctx = new JAXBFilterProcessingContext(packet.invocationProperties);
            ((JAXBFilterProcessingContext) ctx).setAddressingVersion(addVer);
            ((JAXBFilterProcessingContext) ctx).setSOAPVersion(soapVersion);
            ((JAXBFilterProcessingContext) ctx).setBSP(bsp10);
        } else {
            ctx = new ProcessingContextImpl(packet.invocationProperties);
        }
        if (addVer != null) {
            ctx.setAction(getAction(packet));
        }
        // Set the SecurityPolicy version namespace in processingContext 
        ctx.setSecurityPolicyVersion(spVersion.namespaceUri);
        ctx.setTimestampTimeout(this.timestampTimeOut);
        ctx.setiterationsForPDK(this.iterationsForPDK);
        // set the policy, issued-token-map, and extraneous properties
        //ctx.setIssuedTokenContextMap(issuedTokenContextMap);
        ctx.setAlgorithmSuite(getAlgoSuite(getBindingAlgorithmSuite(packet)));
        //set the server certificate in the context ;
        if (serverCert != null) {
            if (isCertValidityVerified == false) {
                CertificateRetriever cr = new CertificateRetriever();
                isCertValid = cr.setServerCertInTheContext(ctx, secEnv, serverCert);
                cr = null;
                isCertValidityVerified = true;
            } else {
                if (isCertValid == true) {
                    ctx.getExtraneousProperties().put(XWSSConstants.SERVER_CERTIFICATE_PROPERTY, serverCert);
                }
            }
        }
        try {
            PolicyAlternativeHolder applicableAlternative =
                    resolveAlternative(packet, isSCMessage);
            MessagePolicy policy = null;
            if (isRMMessage(packet) || isMakeConnectionMessage(packet)) {
                SecurityPolicyHolder holder = applicableAlternative.getOutProtocolPM().get("RM");
                policy = holder.getMessagePolicy();
            } else if (isSCCancel(packet)) {
                SecurityPolicyHolder holder = applicableAlternative.getOutProtocolPM().get("SC-CANCEL");
                policy = holder.getMessagePolicy();
            } else if (isSCRenew(packet)) {
                policy = getOutgoingXWSSecurityPolicy(packet, isSCMessage);
                ctx.isExpired(true);
            } else {
                policy = getOutgoingXWSSecurityPolicy(packet, isSCMessage);
            }
            if (debug) {
                policy.dumpMessages(true);
            }
            if (policy.getAlgorithmSuite() != null) {
                //override the binding level suite
                ctx.setAlgorithmSuite(policy.getAlgorithmSuite());
            }
            ctx.setWSSAssertion(policy.getWSSAssertion());
            ctx.setSecurityPolicy(policy);
            ctx.setSecurityEnvironment(secEnv);
            ctx.isInboundMessage(false);
        } catch (XWSSecurityException e) {
            log.log(Level.SEVERE, LogStringsMessages.WSSTUBE_0006_PROBLEM_INIT_OUT_PROC_CONTEXT(), e);
            throw new RuntimeException(LogStringsMessages.WSSTUBE_0006_PROBLEM_INIT_OUT_PROC_CONTEXT(), e);
        }
        return ctx;
    }

    protected SOAPFault getSOAPFault(WssSoapFaultException sfe) {

        SOAPFault fault;
        try {
            if (isSOAP12) {
                fault = soapFactory.createFault(sfe.getFaultString(), SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(sfe.getFaultCode());
            } else {
                fault = soapFactory.createFault(sfe.getFaultString(), sfe.getFaultCode());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSSTUBE_0002_INTERNAL_SERVER_ERROR());
            throw new RuntimeException(LogStringsMessages.WSSTUBE_0002_INTERNAL_SERVER_ERROR(), e);
        }
        return fault;
    }

    protected SOAPFaultException getSOAPFaultException(WssSoapFaultException sfe) {

        SOAPFault fault;
        try {
            if (isSOAP12) {
                fault = soapFactory.createFault(sfe.getFaultString(), SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(sfe.getFaultCode());
            } else {
                fault = soapFactory.createFault(sfe.getFaultString(), sfe.getFaultCode());
            }
        } catch (SOAPException e) {
            log.log(Level.SEVERE, LogStringsMessages.WSSTUBE_0002_INTERNAL_SERVER_ERROR());
            throw new RuntimeException(LogStringsMessages.WSSTUBE_0002_INTERNAL_SERVER_ERROR(), e);
        }
        SOAPFaultException e = new SOAPFaultException(fault);
        e.initCause(sfe);
        return e;

    }

    protected SOAPFaultException getSOAPFaultException(XWSSecurityException xwse) {
        QName qname;
        if (xwse.getCause() instanceof PolicyViolationException) {
            qname = MessageConstants.WSSE_RECEIVER_POLICY_VIOLATION;
        } else {
            qname = MessageConstants.WSSE_FAILED_AUTHENTICATION;
        }

        WssSoapFaultException wsfe =
                SecurableSoapMessage.newSOAPFaultException(
                qname, xwse.getMessage(), xwse);
        //TODO: MISSING-LOG
        return getSOAPFaultException(wsfe);
    }

    /**
     * Summary from Section 4.2, WS-Security Policy spec( version 1.1 July 2005 ).
     * MessagePolicySubject : policy can be attached to
     *   1) wsdl:binding/wsdl:operation/wsdl:input, ./wsdl:output, or ./wsdl:fault
     *
     * OperationPolicySubject : policy can be attached to
     *   1)wsdl:binding/wsdl:operation
     *
     * EndpointPolicySubject : policy can be attached to
     *   1)wsdl:port
     *   2)wsdl:Binding
     */
    protected void collectPolicies(List<PolicyAlternativeHolder> alternatives) {
        try {
            if (wsPolicyMap == null) {
                return;
            }
            //To check: Is this sufficient, any edge cases I need to take care
            QName serviceName = tubeConfig.getWSDLPort().getOwner().getName();
            QName portName = tubeConfig.getWSDLPort().getName();
            //Review: will this take care of EndpointPolicySubject
            //PolicyMerger policyMerge = PolicyMerger.getMerger();
            PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(serviceName, portName);
            //createWsdlEndpointScopeKey(serviceName,portName);
            //Review:Will getEffectivePolicy return null or empty policy ?.
            Policy endpointPolicy = wsPolicyMap.getEndpointEffectivePolicy(endpointKey);

            //This will be used for setting credentials like  spVersion... etc for binding level policies
            setPolicyCredentials(endpointPolicy);

            //This will be used for setting credentials like  spVersion... etc for operation level policies
            for (WSDLBoundOperation operation : tubeConfig.getWSDLPort().getBinding().getBindingOperations()) {
                QName operationName = new QName(operation.getBoundPortType().getName().getNamespaceURI(), operation.getName().getLocalPart());
                PolicyMapKey operationKey = PolicyMap.createWsdlOperationScopeKey(serviceName, portName, operationName);
                Policy operationPolicy = wsPolicyMap.getOperationEffectivePolicy(operationKey);
                setPolicyCredentials(operationPolicy);
            }

            if (endpointPolicy == null) {
                ArrayList<Policy> policyList = new ArrayList<Policy>();
                PolicyAlternativeHolder ph = new PolicyAlternativeHolder(null, spVersion, bpMSP);
                alternatives.add(ph);
                collectOperationAndMessageLevelPolicies(wsPolicyMap, null, policyList, ph);
                return;
            }
            Iterator<AssertionSet> policiesIter = endpointPolicy.iterator();
            while (policiesIter.hasNext()) {
                ArrayList<Policy> policyList = new ArrayList<Policy>();
                AssertionSet ass = policiesIter.next();
                PolicyAlternativeHolder ph = new PolicyAlternativeHolder(ass, spVersion, bpMSP);
                alternatives.add(ph);

                Collection<AssertionSet> coll = new ArrayList<AssertionSet>();
                coll.add(ass);
                Policy singleAlternative = Policy.createPolicy(
                        endpointPolicy.getNamespaceVersion(), endpointPolicy.getName(), endpointPolicy.getId(), coll);

                buildProtocolPolicy(singleAlternative, ph);
                //if(endpointPolicy != null){
                policyList.add(singleAlternative);
                //}
                collectOperationAndMessageLevelPolicies(wsPolicyMap, singleAlternative, policyList, ph);
            }
        } catch (PolicyException pe) {
            throw generateInternalError(pe);
        }
    }

    //TODO:POLALT Alternatives only at BindingLevel for Now
    private void collectOperationAndMessageLevelPolicies(PolicyMap wsPolicyMap,
            Policy singleAlternative, ArrayList<Policy> policyList, PolicyAlternativeHolder ph) {
        if (wsPolicyMap == null) {
            return;
        }
        try {
            QName serviceName = tubeConfig.getWSDLPort().getOwner().getName();
            QName portName = tubeConfig.getWSDLPort().getName();

            PolicyMerger policyMerge = PolicyMerger.getMerger();
            for (WSDLBoundOperation operation : tubeConfig.getWSDLPort().getBinding().getBindingOperations()) {
                QName operationName = new QName(operation.getBoundPortType().getName().getNamespaceURI(),
                        operation.getName().getLocalPart());

                PolicyMapKey messageKey = PolicyMap.createWsdlMessageScopeKey(
                        serviceName, portName, operationName);
                PolicyMapKey operationKey = PolicyMap.createWsdlOperationScopeKey(serviceName, portName, operationName);

                //Review:Not sure if this is need and what is the
                //difference between operation and message level key.
                //securityPolicyNamespaces
                Policy operationPolicy = wsPolicyMap.getOperationEffectivePolicy(operationKey);
                if (operationPolicy != null) {
                    policyList.add(operationPolicy);
                } else {
                    //log fine message
                    //System.out.println("Operation Level Security policy is null");
                }

                Policy imPolicy = null;
                imPolicy = wsPolicyMap.getInputMessageEffectivePolicy(messageKey);
                if (imPolicy != null) {
                    policyList.add(imPolicy);
                }
                //input message effective policy to be used. Policy elements at various
                //scopes merged.
                Policy imEP = policyMerge.merge(policyList);
                SecurityPolicyHolder outPH = null;
                if (imEP != null) {
                    outPH = addOutgoingMP(operation, imEP, ph);
                }

                if (imPolicy != null) {
                    policyList.remove(imPolicy);
                }
                //one way
                SecurityPolicyHolder inPH = null;

                Policy omPolicy = null;
                omPolicy = wsPolicyMap.getOutputMessageEffectivePolicy(messageKey);
                if (omPolicy != null) {
                    policyList.add(omPolicy);
                }
                //ouput message effective policy to be used. Policy elements at various
                //scopes merged.

                Policy omEP = policyMerge.merge(policyList);
                if (omPolicy != null) {
                    policyList.remove(omPolicy);
                }
                if (omEP != null) {
                    inPH = addIncomingMP(operation, omEP, ph);
                }

                Iterator faults = operation.getOperation().getFaults().iterator();
                ArrayList<Policy> faultPL = new ArrayList<Policy>();
                if (singleAlternative != null) {
                    faultPL.add(singleAlternative);
                }
                if (operationPolicy != null) {
                    faultPL.add(operationPolicy);
                }
                while (faults.hasNext()) {
                    WSDLFault fault = (WSDLFault) faults.next();
                    PolicyMapKey fKey = null;
                    fKey = PolicyMap.createWsdlFaultMessageScopeKey(
                            serviceName, portName, operationName,
                            new QName(operationName.getNamespaceURI(), fault.getName()));
                    Policy fPolicy = wsPolicyMap.getFaultMessageEffectivePolicy(fKey);

                    if (fPolicy != null) {
                        faultPL.add(fPolicy);
                    } else {
                        //continue;
                    }
                    Policy ep = policyMerge.merge(faultPL);
                    if (inPH != null) {
                        addIncomingFaultPolicy(ep, inPH, fault);
                    }
                    if (outPH != null) {
                        addOutgoingFaultPolicy(ep, outPH, fault);
                    }
                    faultPL.remove(fPolicy);
                }
                if (operationPolicy != null) {
                    policyList.remove(operationPolicy);
                }
            }
        } catch (PolicyException pe) {
            throw generateInternalError(pe);
        }
    }
    //TODO:POLALT : should this method look over all alternatives

    protected List<PolicyAssertion> getInBoundSCP(Message message) {
        SecurityPolicyHolder sph = null;
        //TODO:encapsulate this explicit public member access p.x below
        for (PolicyAlternativeHolder p : policyAlternatives) {
            if (p.getInMessagePolicyMap() == null) {
                return Collections.emptyList();
            }

            Collection coll = p.getInMessagePolicyMap().values();
            Iterator itr = coll.iterator();

            while (itr.hasNext()) {
                SecurityPolicyHolder ph = (SecurityPolicyHolder) itr.next();
                if (ph != null) {
                    sph = ph;
                    break;
                }
            }
            if (sph == null) {
                return EMPTY_LIST;
            }
        }
        return sph.getSecureConversationTokens();
    }
    //TODO:POLALT : should this method look over all alternatives

    protected List<PolicyAssertion> getOutBoundSCP(
            Message message) {

        SecurityPolicyHolder sph = null;
        //TODO:encapsulate this explicit public member access p.x below
        for (PolicyAlternativeHolder p : policyAlternatives) {
            if (p.getOutMessagePolicyMap() == null) {
                return Collections.emptyList();
            }

            Collection coll = p.getOutMessagePolicyMap().values();
            Iterator itr = coll.iterator();

            while (itr.hasNext()) {
                SecurityPolicyHolder ph = (SecurityPolicyHolder) itr.next();
                if (ph != null) {
                    sph = ph;
                    break;
                }
            }
            if (sph == null) {
                return EMPTY_LIST;
            }
        }
        return sph.getSecureConversationTokens();

    }
    //TODO:POLALT : should this method look over all alternatives

    protected List<PolicyAssertion> getOutBoundKTP(Packet packet, boolean isSCMessage) {
        if (isSCMessage) {
            Token scToken = (Token) packet.invocationProperties.get(SC_ASSERTION);
            return ((SCTokenWrapper) scToken).getKerberosTokens();
        }
        SecurityPolicyHolder sph = null;
        //TODO:encapsulate this explicit public member access p.x below
        for (PolicyAlternativeHolder p : policyAlternatives) {
            if (p.getOutMessagePolicyMap() == null) {
                return Collections.emptyList();
            }
            Message message = packet.getMessage();

            Collection coll = p.getOutMessagePolicyMap().values();
            Iterator itr = coll.iterator();

            while (itr.hasNext()) {
                SecurityPolicyHolder ph = (SecurityPolicyHolder) itr.next();
                if (ph != null) {
                    sph = ph;
                    break;
                }
            }
            if (sph == null) {
                return EMPTY_LIST;
            }
        }
        return sph.getKerberosTokens();
    }
    //TODO:POLALT : should this method look over all alternatives

    protected List<PolicyAssertion> getSecureConversationPolicies(
            Message message, String scope) {
        SecurityPolicyHolder sph = null;
        //TODO:encapsulate this explicit public member access p.x below
        for (PolicyAlternativeHolder p : policyAlternatives) {
            if (p.getOutMessagePolicyMap() == null) {
                return Collections.emptyList();
            }

            Collection coll = p.getOutMessagePolicyMap().values();
            Iterator itr = coll.iterator();

            while (itr.hasNext()) {
                SecurityPolicyHolder ph = (SecurityPolicyHolder) itr.next();
                if (ph != null) {
                    sph = ph;
                    break;
                }
            }
            if (sph == null) {
                return EMPTY_LIST;
            }
        }
        return sph.getSecureConversationTokens();
    }

    //TODO :: Refactor
    protected ArrayList<PolicyAssertion> getTokens(Policy policy) {
        ArrayList<PolicyAssertion> tokenList = new ArrayList<PolicyAssertion>();
        for (AssertionSet assertionSet : policy) {
            for (PolicyAssertion assertion : assertionSet) {
                if (PolicyUtil.isAsymmetricBinding(assertion, spVersion)) {
                    AsymmetricBinding sb = (AsymmetricBinding) assertion;
                    Token iToken = sb.getInitiatorToken();
                    if (iToken != null) {
                        addToken(iToken, tokenList);
                    } else {
                        addToken(sb.getInitiatorSignatureToken(), tokenList);
                        addToken(sb.getInitiatorEncryptionToken(), tokenList);
                    }

                    Token rToken = sb.getRecipientToken();
                    if (rToken != null) {
                        addToken(rToken, tokenList);
                    } else {
                        addToken(sb.getRecipientSignatureToken(), tokenList);
                        addToken(sb.getRecipientEncryptionToken(), tokenList);
                    }
                } else if (PolicyUtil.isSymmetricBinding(assertion, spVersion)) {
                    SymmetricBinding sb = (SymmetricBinding) assertion;
                    Token token = sb.getProtectionToken();
                    if (token != null) {
                        addToken(token, tokenList);
                    } else {
                        addToken(sb.getEncryptionToken(), tokenList);
                        addToken(sb.getSignatureToken(), tokenList);
                    }
                } else if (PolicyUtil.isSupportingTokens(assertion, spVersion)) {
                    SupportingTokens st = (SupportingTokens) assertion;
                    Iterator itr = st.getTokens();
                    while (itr.hasNext()) {
                        addToken((Token) itr.next(), tokenList);
                    }
                }
            }
        }
        return tokenList;
    }

    private void addConfigAssertions(Policy policy, SecurityPolicyHolder sph) {
        //ArrayList<PolicyAssertion> tokenList = new ArrayList<PolicyAssertion>();
        for (AssertionSet assertionSet : policy) {
            for (PolicyAssertion assertion : assertionSet) {
                if (PolicyUtil.isConfigPolicyAssertion(assertion)) {
                    sph.addConfigAssertions(assertion);
                }
            }
        }
    }

    private void addToken(Token token, ArrayList<PolicyAssertion> list) {
        if (token == null) {
            return;
        }
        if (PolicyUtil.isSecureConversationToken((PolicyAssertion) token, spVersion) ||
                PolicyUtil.isIssuedToken((PolicyAssertion) token, spVersion) ||
                PolicyUtil.isKerberosToken((PolicyAssertion) token, spVersion)) {
            list.add((PolicyAssertion) token);
        }
    }

    protected PolicyMapKey getOperationKey(Message message) {
        WSDLBoundOperation operation = message.getOperation(tubeConfig.getWSDLPort());
        WSDLOperation wsdlOperation = operation.getOperation();
        QName serviceName = tubeConfig.getWSDLPort().getOwner().getName();
        QName portName = tubeConfig.getWSDLPort().getName();
        //WSDLInput input = wsdlOperation.getInput();
        //WSDLOutput output = wsdlOperation.getOutput();
        //QName inputMessageName = input.getMessage().getName();
        //QName outputMessageName = output.getMessage().getName();
        PolicyMapKey messageKey = PolicyMap.createWsdlMessageScopeKey(
                serviceName, portName, wsdlOperation.getName());
        return messageKey;

    }

    protected abstract SecurityPolicyHolder addOutgoingMP(WSDLBoundOperation operation, Policy policy, PolicyAlternativeHolder ph) throws PolicyException;

    protected abstract SecurityPolicyHolder addIncomingMP(WSDLBoundOperation operation, Policy policy, PolicyAlternativeHolder ph) throws PolicyException;

    protected AlgorithmSuite getBindingAlgorithmSuite(Packet packet) {
        return bindingLevelAlgSuite;
    }

    protected void cacheMessage(Packet packet) {
        // Not required, commeting
//        Message message = null;
//        if(!optimized){
//            try{
//                message = packet.getMessage();
//                message= Messages.create(message.readAsSOAPMessage());
//                packet.setMessage(message);
//            }catch(SOAPException se){
//                // internal error
//                log.log(Level.SEVERE, LogStringsMessages.WSSTUBE_0005_PROBLEM_PROC_SOAP_MESSAGE(), se);
//                throw new WebServiceException(LogStringsMessages.WSSTUBE_0005_PROBLEM_PROC_SOAP_MESSAGE(), se);
//            }
//        }
    }

    private boolean hasTargets(NestedPolicy policy) {
        AssertionSet as = policy.getAssertionSet();
        //Iterator<PolicyAssertion> paItr = as.iterator();
        boolean foundTargets = false;
        for (PolicyAssertion assertion : as) {
            if (PolicyUtil.isSignedParts(assertion, spVersion) || PolicyUtil.isEncryptParts(assertion, spVersion)) {
                foundTargets = true;
                break;
            }
        }
        return foundTargets;
    }

    protected Policy getEffectiveBootstrapPolicy(NestedPolicy bp) throws PolicyException {
        try {
            ArrayList<Policy> pl = new ArrayList<Policy>();
            pl.add(bp);
            Policy mbp = getMessageBootstrapPolicy();
            if (mbp != null) {
                pl.add(mbp);
            }

            PolicyMerger pm = PolicyMerger.getMerger();
            Policy ep = pm.merge(pl);
            return ep;
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSSTUBE_0007_PROBLEM_GETTING_EFF_BOOT_POLICY(), e);
            throw new PolicyException(LogStringsMessages.WSSTUBE_0007_PROBLEM_GETTING_EFF_BOOT_POLICY(), e);
        }

    }

    private Policy getMessageBootstrapPolicy() throws PolicyException, IOException {
        if (bpMSP == null) {
            String bootstrapMessagePolicy = "boot-msglevel-policy.xml";
            if (SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri.equals(spVersion.namespaceUri)) {
                bootstrapMessagePolicy = "boot-msglevel-policy-sx.xml";
            }
            PolicySourceModel model = unmarshalPolicy(
                    "com/sun/xml/ws/security/impl/policyconv/" + bootstrapMessagePolicy);
            bpMSP = ModelTranslator.getTranslator().translate(model);
        }
        return bpMSP;
    }

    private Policy getMessageLevelBSP() throws PolicyException {
        QName serviceName = tubeConfig.getWSDLPort().getOwner().getName();
        QName portName = tubeConfig.getWSDLPort().getName();
        PolicyMapKey operationKey = PolicyMap.createWsdlOperationScopeKey(serviceName, portName, bsOperationName);

        Policy operationLevelEP = wsPolicyMap.getOperationEffectivePolicy(operationKey);
        return operationLevelEP;
    }

    protected PolicySourceModel unmarshalPolicy(String resource) throws PolicyException, IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (is == null) {
            return null;
        }
        Reader reader = new InputStreamReader(is);
        PolicySourceModel model = ModelUnmarshaller.getUnmarshaller().unmarshalModel(reader);
        reader.close();
        return model;
    }

    protected final void cacheOperation(Message msg) {
        cachedOperation = msg.getOperation(tubeConfig.getWSDLPort());
    }

    protected final void resetCachedOperation() {
        cachedOperation = null;
    }

    protected boolean isSCMessage(Packet packet) {

        if (!bindingHasSecureConversationPolicy()) {
            return false;
        }

        if (!isAddressingEnabled()) {
            return false;
        }

        String action = getAction(packet);
        return wsscVer.getSCTRequestAction().equals(action) ||
                wsscVer.getSCTRenewRequestAction().equals(action);
    }

    protected boolean isSCCancel(Packet packet) {

        if (!bindingHasSecureConversationPolicy()) {
            return false;
        }

        if (!isAddressingEnabled()) {
            return false;
        }

        String action = getAction(packet);
        return wsscVer.getSCTCancelResponseAction().equals(action) ||
                wsscVer.getSCTCancelRequestAction().equals(action);
    }

    protected boolean isSCRenew(Packet packet) {

        if (!bindingHasSecureConversationPolicy()) {
            return false;
        }

        if (!isAddressingEnabled()) {
            return false;
        }

        String action = getAction(packet);
        return wsscVer.getSCTRenewResponseAction().equals(action) ||
                wsscVer.getSCTRenewRequestAction().equals(action);
    }

    protected boolean isAddressingEnabled() {
        return (addVer != null);
    }

    protected boolean isTrustMessage(Packet packet) {
        if (!isAddressingEnabled()) {
            return false;
        }
        String action = getAction(packet);

        // Issue 
        if (wsTrustVer.getIssueRequestAction().equals(action) ||
                wsTrustVer.getIssueFinalResoponseAction().equals(action)) {
            return true;
        }

        // Validate 
        return wsTrustVer.getValidateRequestAction().equals(action) ||
                wsTrustVer.getValidateFinalResoponseAction().equals(action);

    }

    protected boolean isRMMessage(Packet packet) {
        if (!isAddressingEnabled()) {
            return false;
        }
        if (!bindingHasRMPolicy()) {
            return false;
        }

        return rmVer.isProtocolAction(getAction(packet));
    }

    protected boolean isMakeConnectionMessage(Packet packet) {
        if (!this.hasMakeConnection) {
            return false;
        }
        return mcVer.isProtocolAction(getAction(packet));
    }

    protected String getAction(Packet packet) {
        // if ("true".equals(packet.invocationProperties.get(WSTrustConstants.IS_TRUST_MESSAGE))){
        //    return (String)packet.invocationProperties.get(WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION);
        //}

        HeaderList hl = packet.getMessage().getHeaders();
        //String action =  hl.getAction(tubeConfig.getBinding().getAddressingVersion(), tubeConfig.getBinding().getSOAPVersion());
        String action = hl.getAction(addVer, tubeConfig.getBinding().getSOAPVersion());
        return action;
    }

    protected WSDLBoundOperation getWSDLOpFromAction(Packet packet, boolean isIncomming) {
        String uriValue = getAction(packet);
        for (PolicyAlternativeHolder p : policyAlternatives) {
            Set<WSDLBoundOperation> keys = p.getOutMessagePolicyMap().keySet();
            for (WSDLBoundOperation wbo : keys) {
                WSDLOperation wo = wbo.getOperation();
                // WsaWSDLOperationExtension extensions = wo.getExtension(WsaWSDLOperationExtension.class);
                String action = getAction(wo, isIncomming);
                if (action != null && action.equals(uriValue)) {
                    return wbo;
                }
            }
        }
        return null;

    }

    protected void buildProtocolPolicy(Policy endpointPolicy, PolicyAlternativeHolder ph) throws PolicyException {
        if (endpointPolicy == null) {
            return;
        }
        try {
            RMPolicyResolver rr = new RMPolicyResolver(spVersion, rmVer, mcVer, encRMLifecycleMsg);
            Policy msgLevelPolicy = rr.getOperationLevelPolicy();
            PolicyMerger merger = PolicyMerger.getMerger();
            ArrayList<Policy> pList = new ArrayList<Policy>(2);
            pList.add(endpointPolicy);
            pList.add(msgLevelPolicy);
            Policy effectivePolicy = merger.merge(pList);
            addIncomingProtocolPolicy(effectivePolicy, "RM", ph);
            addOutgoingProtocolPolicy(effectivePolicy, "RM", ph);

            pList.remove(msgLevelPolicy);
            pList.add(getMessageBootstrapPolicy());
            PolicyMerger pm = PolicyMerger.getMerger();
            //add secure conversation policy.
            Policy ep = pm.merge(pList);
            addIncomingProtocolPolicy(ep, "SC", ph);
            addOutgoingProtocolPolicy(ep, "SC", ph);
            ArrayList<Policy> pList1 = new ArrayList<Policy>(2);
            pList1.add(endpointPolicy);
            pList1.add(getSCCancelPolicy(encryptCancelPayload));
            PolicyMerger pm1 = PolicyMerger.getMerger();
            //add secure conversation policy.
            Policy ep1 = pm1.merge(pList1);
            addIncomingProtocolPolicy(ep1, "SC-CANCEL", ph);
            addOutgoingProtocolPolicy(ep1, "SC-CANCEL", ph);
        } catch (IOException ie) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0008_PROBLEM_BUILDING_PROTOCOL_POLICY(), ie);
            throw new PolicyException(
                    LogStringsMessages.WSSTUBE_0008_PROBLEM_BUILDING_PROTOCOL_POLICY(), ie);
        }
    }

    protected SecurityPolicyHolder constructPolicyHolder(Policy effectivePolicy,
            boolean isServer, boolean isIncoming) throws PolicyException {
        return constructPolicyHolder(effectivePolicy, isServer, isIncoming, false);
    }

    protected SecurityPolicyHolder constructPolicyHolder(Policy effectivePolicy,
            boolean isServer, boolean isIncoming, boolean ignoreST) throws PolicyException {

        XWSSPolicyGenerator xwssPolicyGenerator = new XWSSPolicyGenerator(effectivePolicy, isServer, isIncoming, spVersion);
        xwssPolicyGenerator.process(ignoreST);
        this.bindingLevelAlgSuite = xwssPolicyGenerator.getBindingLevelAlgSuite();
        MessagePolicy messagePolicy = xwssPolicyGenerator.getXWSSPolicy();

        SecurityPolicyHolder sph = new SecurityPolicyHolder();
        sph.setMessagePolicy(messagePolicy);
        sph.setBindingLevelAlgSuite(xwssPolicyGenerator.getBindingLevelAlgSuite());
        sph.isIssuedTokenAsEncryptedSupportingToken(xwssPolicyGenerator.isIssuedTokenAsEncryptedSupportingToken());
        List<PolicyAssertion> tokenList = getTokens(effectivePolicy);
        addConfigAssertions(effectivePolicy, sph);

        for (PolicyAssertion token : tokenList) {
            if (PolicyUtil.isSecureConversationToken(token, spVersion)) {
                NestedPolicy bootstrapPolicy = ((SecureConversationToken) token).getBootstrapPolicy();
                Policy effectiveBP;
                if (hasTargets(bootstrapPolicy)) {
                    effectiveBP = bootstrapPolicy;
                } else {
                    effectiveBP = getEffectiveBootstrapPolicy(bootstrapPolicy);
                }
                xwssPolicyGenerator = new XWSSPolicyGenerator(effectiveBP, isServer, isIncoming, spVersion);
                xwssPolicyGenerator.process(ignoreST);
                MessagePolicy bmp = xwssPolicyGenerator.getXWSSPolicy();
                this.bootStrapAlgoSuite = xwssPolicyGenerator.getBindingLevelAlgSuite();

                if (isServer && isIncoming) {
                    EncryptionPolicy optionalPolicy =
                            new EncryptionPolicy();
                    EncryptionPolicy.FeatureBinding fb = (EncryptionPolicy.FeatureBinding) optionalPolicy.getFeatureBinding();
                    optionalPolicy.newX509CertificateKeyBinding();
                    EncryptionTarget target = new EncryptionTarget();
                    target.setQName(new QName(MessageConstants.SAML_v1_1_NS, MessageConstants.SAML_ASSERTION_LNAME));
                    target.setEnforce(false);
                    fb.addTargetBinding(target);
                /*
                try {
                bmp.prepend(optionalPolicy);
                } catch (PolicyGenerationException ex) {
                throw new PolicyException(ex);
                }*/
                }

                PolicyAssertion sct = new SCTokenWrapper(token, bmp);
                sph.addSecureConversationToken(sct);
                hasSecureConversation = true;

                // if the bootstrap has issued tokens then set hasIssuedTokens=true
                List<PolicyAssertion> iList =
                        this.getIssuedTokenPoliciesFromBootstrapPolicy((Token) sct);
                if (!iList.isEmpty()) {
                    hasIssuedTokens = true;
                }

                // if the bootstrap has kerberos tokens then set hasKerberosTokens=true
                List<PolicyAssertion> kList =
                        this.getKerberosTokenPoliciesFromBootstrapPolicy((Token) sct);
                if (!kList.isEmpty()) {
                    hasKerberosToken = true;
                }

            } else if (PolicyUtil.isIssuedToken(token, spVersion)) {
                sph.addIssuedToken(token);
                hasIssuedTokens = true;
            } else if (PolicyUtil.isKerberosToken(token, spVersion)) {
                sph.addKerberosToken(token);
                hasKerberosToken = true;
            }
        }
        return sph;
    }

    // return the callbackhandler if the xwssCallbackHandler was set
    // otherwise populate the props and return null.
    protected String populateConfigProperties(Set<PolicyAssertion> configAssertions, Properties props) {
        if (configAssertions == null) {
            return null;
        }
        for (PolicyAssertion as : configAssertions) {
            if ("KeyStore".equals(as.getName().getLocalPart())) {
                populateKeystoreProps(props, (KeyStore) as);
            } else if ("TrustStore".equals(as.getName().getLocalPart())) {
                populateTruststoreProps(props, (TrustStore) as);
            } else if ("CallbackHandlerConfiguration".equals(as.getName().getLocalPart())) {
                String ret = populateCallbackHandlerProps(props, (CallbackHandlerConfiguration) as);
                if (ret != null) {
                    return ret;
                }
            } else if ("ValidatorConfiguration".equals(as.getName().getLocalPart())) {
                populateValidatorProps(props, (ValidatorConfiguration) as);
            } else if ("CertStore".equals(as.getName().getLocalPart())) {
                populateCertStoreProps(props, (CertStoreConfig) as);
            } else if ("KerberosConfig".equals(as.getName().getLocalPart())) {
                populateKerberosProps(props, (KerberosConfig) as);
            }
        }
        return null;
    }

    private void populateKerberosProps(Properties props, KerberosConfig kerbConfig) {
        if (kerbConfig.getLoginModule() != null) {
            props.put(DefaultCallbackHandler.KRB5_LOGIN_MODULE, kerbConfig.getLoginModule());
        }
        if (kerbConfig.getServicePrincipal() != null) {
            props.put(DefaultCallbackHandler.KRB5_SERVICE_PRINCIPAL, kerbConfig.getServicePrincipal());
        }
        if (kerbConfig.getCredentialDelegation() != null) {
            props.put(DefaultCallbackHandler.KRB5_CREDENTIAL_DELEGATION, kerbConfig.getCredentialDelegation());
        }
    }

    private void populateKeystoreProps(Properties props, KeyStore store) {
        boolean foundLoginModule = false;
        if (store.getKeyStoreLoginModuleConfigName() != null) {
            props.put(DefaultCallbackHandler.JAAS_KEYSTORE_LOGIN_MODULE, store.getKeyStoreLoginModuleConfigName());
            foundLoginModule = true;
        }
        if (store.getKeyStoreCallbackHandler() != null) {
            props.put(DefaultCallbackHandler.KEYSTORE_CBH, store.getKeyStoreCallbackHandler());
            if (store.getAlias() != null) {
                props.put(DefaultCallbackHandler.MY_ALIAS, store.getAlias());
            }
            if (store.getAliasSelectorClassName() != null) {
                props.put(DefaultCallbackHandler.KEYSTORE_CERTSELECTOR, store.getAliasSelectorClassName());
            }
            return;
        }
        if (foundLoginModule) {
            return;//
        }

        if (store.getLocation() != null) {
            props.put(DefaultCallbackHandler.KEYSTORE_URL, store.getLocation());
        } else {
            //throw RuntimeException for now
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0014_KEYSTORE_URL_NULL_CONFIG_ASSERTION());
            throw new RuntimeException(LogStringsMessages.WSSTUBE_0014_KEYSTORE_URL_NULL_CONFIG_ASSERTION());
        }

        if (store.getType() != null) {
            props.put(DefaultCallbackHandler.KEYSTORE_TYPE, store.getType());
        } else {
            props.put(DefaultCallbackHandler.KEYSTORE_TYPE, "JKS");
        }

        if (store.getPassword() != null) {
            props.put(DefaultCallbackHandler.KEYSTORE_PASSWORD, new String(store.getPassword()));
        } else {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0015_KEYSTORE_PASSWORD_NULL_CONFIG_ASSERTION());
            throw new RuntimeException(LogStringsMessages.WSSTUBE_0015_KEYSTORE_PASSWORD_NULL_CONFIG_ASSERTION());
        }

        if (store.getAlias() != null) {
            props.put(DefaultCallbackHandler.MY_ALIAS, store.getAlias());
        } else {
            // use default alias
            //throw new RuntimeException("KeyStore Alias was obtained as NULL from ConfigAssertion");
        }

        if (store.getKeyPassword() != null) {
            props.put(DefaultCallbackHandler.KEY_PASSWORD, store.getKeyPassword());
        }

        if (store.getAliasSelectorClassName() != null) {
            props.put(DefaultCallbackHandler.KEYSTORE_CERTSELECTOR, store.getAliasSelectorClassName());
        }
    }

    private void populateTruststoreProps(Properties props, TrustStore store) {
        if (store.getTrustStoreCallbackHandler() != null) {
            props.put(DefaultCallbackHandler.TRUSTSTORE_CBH, store.getTrustStoreCallbackHandler());
            if (store.getPeerAlias() != null) {
                props.put(DefaultCallbackHandler.PEER_ENTITY_ALIAS, store.getPeerAlias());
            }
            if (store.getCertSelectorClassName() != null) {
                props.put(DefaultCallbackHandler.TRUSTSTORE_CERTSELECTOR, store.getCertSelectorClassName());
            }
            return;
        }
        if (store.getLocation() != null) {
            props.put(DefaultCallbackHandler.TRUSTSTORE_URL, store.getLocation());
        } else {
            //throw RuntimeException for now
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0016_TRUSTSTORE_URL_NULL_CONFIG_ASSERTION());
            throw new RuntimeException(LogStringsMessages.WSSTUBE_0016_TRUSTSTORE_URL_NULL_CONFIG_ASSERTION());
        }

        if (store.getType() != null) {
            props.put(DefaultCallbackHandler.TRUSTSTORE_TYPE, store.getType());
        } else {
            props.put(DefaultCallbackHandler.TRUSTSTORE_TYPE, "JKS");
        }

        if (store.getPassword() != null) {
            props.put(DefaultCallbackHandler.TRUSTSTORE_PASSWORD, new String(store.getPassword()));
        } else {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSTUBE_0017_TRUSTSTORE_PASSWORD_NULL_CONFIG_ASSERTION());
            throw new RuntimeException(LogStringsMessages.WSSTUBE_0017_TRUSTSTORE_PASSWORD_NULL_CONFIG_ASSERTION());
        }

        if (store.getPeerAlias() != null) {
            props.put(DefaultCallbackHandler.PEER_ENTITY_ALIAS, store.getPeerAlias());
        }

        if (store.getSTSAlias() != null) {
            props.put(DefaultCallbackHandler.STS_ALIAS, store.getSTSAlias());
        }

        if (store.getServiceAlias() != null) {
            props.put(DefaultCallbackHandler.SERVICE_ALIAS, store.getServiceAlias());
        }

        if (store.getCertSelectorClassName() != null) {
            props.put(DefaultCallbackHandler.TRUSTSTORE_CERTSELECTOR, store.getCertSelectorClassName());
        }
    }

    private String populateCallbackHandlerProps(Properties props, CallbackHandlerConfiguration conf) {
        //check if timestamp timeout has been set
        if (conf.getTimestampTimeout() != null) {
            //in milliseconds
            this.timestampTimeOut = Long.parseLong(conf.getTimestampTimeout()) * 1000;
        }
        if (conf.getUseXWSSCallbacks() != null) {
            props.put(DefaultCallbackHandler.USE_XWSS_CALLBACKS, conf.getUseXWSSCallbacks());
        }
        if (conf.getiterationsForPDK() != null) {
            this.iterationsForPDK = Integer.parseInt(conf.getiterationsForPDK());
        }
        Iterator it = conf.getCallbackHandlers();
        for (; it.hasNext();) {
            PolicyAssertion p = (PolicyAssertion) it.next();
            com.sun.xml.ws.security.impl.policy.CallbackHandler hd = (com.sun.xml.ws.security.impl.policy.CallbackHandler) p;
            String name = hd.getHandlerName();
            String ret = hd.getHandler();
            if ("xwssCallbackHandler".equals(name)) {
                if (ret != null && !"".equals(ret)) {
                    return ret;
                } else {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSSTUBE_0018_NULL_OR_EMPTY_XWSS_CALLBACK_HANDLER_CLASSNAME());
                    throw new RuntimeException(LogStringsMessages.WSSTUBE_0018_NULL_OR_EMPTY_XWSS_CALLBACK_HANDLER_CLASSNAME());
                }
            } else if ("usernameHandler".equals(name)) {
                if (ret != null && !"".equals(ret)) {
                    props.put(DefaultCallbackHandler.USERNAME_CBH, ret);
                } else {
                    QName qname = new QName("default");
                    String def = hd.getAttributeValue(qname);
                    if (def != null && !"".equals(def)) {
                        props.put(DefaultCallbackHandler.MY_USERNAME, def);
                    } else {
                        log.log(Level.SEVERE,
                                LogStringsMessages.WSSTUBE_0019_NULL_OR_EMPTY_USERNAME_HANDLER_CLASSNAME());
                        throw new RuntimeException(LogStringsMessages.WSSTUBE_0019_NULL_OR_EMPTY_USERNAME_HANDLER_CLASSNAME());
                    }
                }
            } else if ("passwordHandler".equals(name)) {
                if (ret != null && !"".equals(ret)) {
                    props.put(DefaultCallbackHandler.PASSWORD_CBH, ret);
                } else {
                    QName qname = new QName("default");
                    String def = hd.getAttributeValue(qname);
                    if (def != null && !"".equals(def)) {
                        props.put(DefaultCallbackHandler.MY_PASSWORD, def);
                    } else {
                        log.log(Level.SEVERE,
                                LogStringsMessages.WSSTUBE_0020_NULL_OR_EMPTY_PASSWORD_HANDLER_CLASSNAME());
                        throw new RuntimeException(LogStringsMessages.WSSTUBE_0020_NULL_OR_EMPTY_PASSWORD_HANDLER_CLASSNAME());
                    }
                }
            } else if ("samlHandler".equals(name)) {
                if (ret == null || "".equals(ret)) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSSTUBE_0021_NULL_OR_EMPTY_SAML_HANDLER_CLASSNAME());
                    throw new RuntimeException(LogStringsMessages.WSSTUBE_0021_NULL_OR_EMPTY_SAML_HANDLER_CLASSNAME());
                }
                props.put(DefaultCallbackHandler.SAML_CBH, ret);
            } else {
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSTUBE_0009_UNSUPPORTED_CALLBACK_TYPE_ENCOUNTERED(name));
                throw new RuntimeException(LogStringsMessages.WSSTUBE_0009_UNSUPPORTED_CALLBACK_TYPE_ENCOUNTERED(name));
            }
        }
        return null;
    }

    private void populateValidatorProps(Properties props, ValidatorConfiguration conf) {
        if (conf.getMaxClockSkew() != null) {
            props.put(DefaultCallbackHandler.MAX_CLOCK_SKEW_PROPERTY, conf.getMaxClockSkew());
        }

        if (conf.getTimestampFreshnessLimit() != null) {
            props.put(DefaultCallbackHandler.TIMESTAMP_FRESHNESS_LIMIT_PROPERTY, conf.getTimestampFreshnessLimit());
        }

        if (conf.getMaxNonceAge() != null) {
            props.put(DefaultCallbackHandler.MAX_NONCE_AGE_PROPERTY, conf.getMaxNonceAge());
        }
        if (conf.getRevocationEnabled() != null) {
            props.put(DefaultCallbackHandler.REVOCATION_ENABLED, conf.getRevocationEnabled());
        }

        Iterator it = conf.getValidators();
        for (; it.hasNext();) {
            PolicyAssertion p = (PolicyAssertion) it.next();
            Validator v = (Validator) p;
            String name = v.getValidatorName();
            String validator = v.getValidator();
            if (validator == null || "".equals(validator)) {
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSTUBE_0022_NULL_OR_EMPTY_VALIDATOR_CLASSNAME(name));
                throw new RuntimeException(LogStringsMessages.WSSTUBE_0022_NULL_OR_EMPTY_VALIDATOR_CLASSNAME(name));
            }

            if ("usernameValidator".equals(name)) {
                props.put(DefaultCallbackHandler.USERNAME_VALIDATOR, validator);
            } else if ("timestampValidator".equals(name)) {
                props.put(DefaultCallbackHandler.TIMESTAMP_VALIDATOR, validator);
            } else if ("certificateValidator".equals(name)) {
                props.put(DefaultCallbackHandler.CERTIFICATE_VALIDATOR, validator);
            } else if ("samlAssertionValidator".equals(name)) {
                props.put(DefaultCallbackHandler.SAML_VALIDATOR, validator);
            } else {
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSTUBE_0010_UNKNOWN_VALIDATOR_TYPE_CONFIG(name));
                throw new RuntimeException(LogStringsMessages.WSSTUBE_0010_UNKNOWN_VALIDATOR_TYPE_CONFIG(name));
            }
        }
    }

    private void populateCertStoreProps(Properties props, CertStoreConfig certStoreConfig) {
        if (certStoreConfig.getCallbackHandlerClassName() != null) {
            props.put(DefaultCallbackHandler.CERTSTORE_CBH, certStoreConfig.getCallbackHandlerClassName());
        }
        if (certStoreConfig.getCertSelectorClassName() != null) {
            props.put(DefaultCallbackHandler.CERTSTORE_CERTSELECTOR, certStoreConfig.getCertSelectorClassName());
        }
        if (certStoreConfig.getCRLSelectorClassName() != null) {
            props.put(DefaultCallbackHandler.CERTSTORE_CRLSELECTOR, certStoreConfig.getCRLSelectorClassName());
        }
    }

    protected Class loadClass(String classname) throws Exception {
        if (classname == null) {
            return null;
        }
        Class ret;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            try {
                ret = loader.loadClass(classname);
                return ret;
            } catch (ClassNotFoundException e) {
            }
        }
        // if context classloader didnt work, try this
        loader = this.getClass().getClassLoader();
        try {
            ret = loader.loadClass(classname);
            return ret;
        } catch (ClassNotFoundException e) {
            // ignore
        }
        log.log(Level.FINE,
                LogStringsMessages.WSSTUBE_0011_COULD_NOT_FIND_USER_CLASS(), classname);
        throw new XWSSecurityException("Error : could not find user class :" + classname);
    }

    protected com.sun.xml.wss.impl.AlgorithmSuite getAlgoSuite(AlgorithmSuite suite) {
        if (suite == null) {
            return null;
        }
        com.sun.xml.wss.impl.AlgorithmSuite als = new com.sun.xml.wss.impl.AlgorithmSuite(
                suite.getDigestAlgorithm(),
                suite.getEncryptionAlgorithm(),
                suite.getSymmetricKeyAlgorithm(),
                suite.getAsymmetricKeyAlgorithm());
        als.setSignatureAlgorithm(suite.getSignatureAlgorithm());
        return als;
    }

    protected com.sun.xml.wss.impl.WSSAssertion getWssAssertion(WSSAssertion asser) {
        com.sun.xml.wss.impl.WSSAssertion assertion = new com.sun.xml.wss.impl.WSSAssertion(
                asser.getRequiredProperties(),
                asser.getType());
        return assertion;
    }

    //TODO: Duplicate information copied from Tubeline Assembler
    private boolean isReliableMessagingEnabled(WSDLPort port) {
        if (port != null && port.getBinding() != null) {
            boolean enabled = port.getBinding().getFeatures().isEnabled(com.sun.xml.ws.rx.rm.api.ReliableMessagingFeature.class);
            return enabled;
        }
        return false;
    }

    private boolean isMakeConnectionEnabled(WSDLPort port) {
        if (port != null && port.getBinding() != null) {
            boolean enabled = port.getBinding().getFeatures().isEnabled(com.sun.xml.ws.rx.mc.api.MakeConnectionSupportedFeature.class);
            return enabled;
        }
        return false;
    }

    protected abstract void addIncomingFaultPolicy(Policy effectivePolicy, SecurityPolicyHolder sph, WSDLFault fault) throws PolicyException;

    protected abstract void addOutgoingFaultPolicy(Policy effectivePolicy, SecurityPolicyHolder sph, WSDLFault fault) throws PolicyException;

    protected abstract void addIncomingProtocolPolicy(Policy effectivePolicy, String protocol, PolicyAlternativeHolder ph) throws PolicyException;

    protected abstract void addOutgoingProtocolPolicy(Policy effectivePolicy, String protocol, PolicyAlternativeHolder ph) throws PolicyException;

    protected abstract String getAction(WSDLOperation operation, boolean isIncomming);

    private void setPolicyCredentials(Policy policy) {

        if (policy != null) {
            if (policy.contains(AddressingVersion.W3C.policyNsUri) || policy.contains("http://www.w3.org/2007/05/addressing/metadata")) {
                addVer = AddressingVersion.W3C;
            } else if (policy.contains(AddressingVersion.MEMBER.policyNsUri)) {
                addVer = AddressingVersion.MEMBER;
            }
            if (policy.contains(optServerSecurity) || policy.contains(optClientSecurity)) {
                optimized = false;
            }
            if (policy.contains(EPREnabled)) {
                isEPREnabled = true;
            }
            if (policy.contains(encSCServerCancel) || policy.contains(encSCClientCancel)) {
                this.encryptCancelPayload = true;
            }
            if (policy.contains(disableCPBuffering) || policy.contains(disableSPBuffering)) {
                disablePayloadBuffer = true;
            }
            if (policy.contains(disableIncPrefixServer) || policy.contains(disableIncPrefixClient)) {
                disableIncPrefix = true;
            }
            if (policy.contains(encHeaderContentServer) || policy.contains(encHeaderContentClient)) {
                encHeaderContent = true;
            }
            if (policy.contains(bsp10Client) || policy.contains(bsp10Server)) {
                bsp10 = true;
            }
            if (policy.contains(allowMissingTSClient) || policy.contains(allowMissingTSServer)) {
                allowMissingTimestamp = true;
            }
            if (policy.contains(unsetSecurityMUValueClient) || policy.contains(unsetSecurityMUValueServer)) {
                securityMUValue = false;
            }
            if (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri)) {
                spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
                wsscVer = WSSCVersion.WSSC_10;
                wsTrustVer = WSTrustVersion.WS_TRUST_10;
            } else if (policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri)) {
                spVersion = SecurityPolicyVersion.SECURITYPOLICY12NS;
                wsscVer = WSSCVersion.WSSC_13;
                wsTrustVer = WSTrustVersion.WS_TRUST_13;
            } else if (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri)) {
                spVersion = SecurityPolicyVersion.SECURITYPOLICY200512;
                wsscVer = WSSCVersion.WSSC_10;
                wsTrustVer = WSTrustVersion.WS_TRUST_10;
            }

            // For RM messages
            if (policy.contains(RmProtocolVersion.WSRM200702.protocolNamespaceUri) ||
                    policy.contains(RmProtocolVersion.WSRM200702.policyNamespaceUri)) {
                rmVer = RmProtocolVersion.WSRM200702;
            } else if (policy.contains(RmProtocolVersion.WSRM200502.protocolNamespaceUri) ||
                    policy.contains(RmProtocolVersion.WSRM200502.policyNamespaceUri)) {
                rmVer = RmProtocolVersion.WSRM200502;
            }
            if (policy.contains(this.encRMLifecycleMsgServer) || policy.contains(encRMLifecycleMsgClient)) {
                encRMLifecycleMsg = true;
            }
        }
    }

    private Policy getSCCancelPolicy(boolean encryptCancelPayload) throws PolicyException, IOException {
        if (cancelMSP == null) {

            String scCancelMessagePolicy = encryptCancelPayload ? "enc-sccancel-msglevel-policy.xml" : "sccancel-msglevel-policy.xml";
            if (SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri.equals(spVersion.namespaceUri)) {
                scCancelMessagePolicy = encryptCancelPayload ? "enc-sccancel-msglevel-policy-sx.xml" : "sccancel-msglevel-policy-sx.xml";
            }
            PolicySourceModel model = unmarshalPolicy(
                    "com/sun/xml/ws/security/impl/policyconv/" + scCancelMessagePolicy);
            cancelMSP = ModelTranslator.getTranslator().translate(model);
        }
        return cancelMSP;
    }

    protected PolicyAlternativeHolder resolveAlternative(Packet packet, boolean isSCMessage) {
        if (this.policyAlternatives.size() == 1) {
            return this.policyAlternatives.get(0);
        }

        String alternativeId = (String) packet.invocationProperties.get(PolicyVerifier.POLICY_ALTERNATIVE_ID);
        if (alternativeId != null) {
            for (PolicyAlternativeHolder p : this.policyAlternatives) {
                if (alternativeId.equals(p.getId())) {
                    return p;
                }
            }
        }
        //return arbitrarily
        if (!this.policyAlternatives.isEmpty()) {
            return this.policyAlternatives.get(0);
        } else {
            return null;
        }
    }
}
