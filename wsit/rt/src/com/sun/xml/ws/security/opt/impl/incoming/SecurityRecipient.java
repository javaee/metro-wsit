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
package com.sun.xml.ws.security.opt.impl.incoming;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.encoding.TagInfoset;
import com.sun.xml.ws.message.stream.StreamMessage;
import com.sun.xml.ws.protocol.soap.VersionMismatchException;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.TokenValidator;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.incoming.processor.SecurityHeaderProcessor;
import com.sun.xml.ws.security.opt.impl.incoming.processor.SecurityTokenProcessor;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.PolicyResolver;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.Target;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import com.sun.xml.stream.buffer.XMLStreamBufferMark;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.opt.impl.attachment.AttachmentImpl;
import com.sun.xml.ws.security.opt.impl.attachment.AttachmentSetImpl;
import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import com.sun.xml.wss.BasicSecurityProfile;
import java.util.Map;
import javax.xml.ws.WebServiceException;
import com.sun.xml.wss.logging.impl.opt.LogStringsMessages;
import static com.sun.xml.wss.BasicSecurityProfile.*;
import com.sun.xml.ws.security.opt.impl.util.VerifiedMessageXMLStreamReader;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.policy.PolicyAlternatives;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.spi.PolicyVerifier;
import com.sun.xml.wss.impl.policy.verifier.PolicyVerifierFactory;

/**
 *
 * @author K.Venugopal@sun.com
 */
public final class SecurityRecipient {

    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_DOMAIN,
            LogDomainConstants.IMPL_OPT_DOMAIN_BUNDLE);
    //TODO Move static block to SecurityPipeBase .
    

    static {
        com.sun.org.apache.xml.internal.security.Init.init();
    }
    private static final int TIMESTAMP_ELEMENT = 1;
    private static final int USERNAME_TOKEN_ELEMENT = 2;
    private static final int BINARYSECURITY_TOKEN_ELEMENT = 3;
    private static final int ENCRYPTED_DATA_ELEMENT = 4;
    private static final int ENCRYPTED_KEY_ELEMENT = 5;
    private static final int SIGNATURE_ELEMENT = 6;
    private static final int REFERENCE_LIST_ELEMENT = 7;
    private static final int DERIVED_KEY_ELEMENT = 8;
    private static final int SIGNATURE_CONFIRMATION_ELEMENT = 9;
    private static final int SECURITY_CONTEXT_TOKEN = 10;
    private static final int SAML_ASSERTION_ELEMENT = 11;
    private static final int ENCRYPTED_HEADER_ELEMENT = 12;
    private static final int STR_ELEMENT = 13;
    private static final String SOAP_ENVELOPE = "Envelope";
    private static final String SOAP_HEADER = "Header";
    private static final String SOAP_BODY = "Body";
    private final String SOAP_NAMESPACE_URI;
    private final SOAPVersion soapVersion;
    private XMLStreamReader message = null;
    private StreamReaderBufferCreator creator = null;
    private HashMap<String, String> parentNS = new HashMap<String, String>();
    private HashMap<String, String> parentNSOnEnvelope = new HashMap<String, String>();
    private HashMap<String, String> securityHeaderNS = new HashMap<String, String>();
    private HashMap<String, String> bodyENVNS = new HashMap<String, String>();
    private HashMap<String, String> envshNS = null;
    // for future use
    private XMLInputFactory staxIF = null;
    private JAXBFilterProcessingContext context = null;
    private HeaderList headers = null;
    private TagInfoset headerTag = null;
    private TagInfoset envelopeTag = null;
    private ArrayList processedHeaders = new ArrayList(2);
    private SecurityHeaderElement pendingElement = null;
    private ArrayList bufferedHeaders = new ArrayList();
    private SecurityContext securityContext = null;
    private TagInfoset bodyTag = null;
    private String bodyWsuId = "";
    private String payLoadWsuId = "";
    private String payLoadEncId = "";
    private HashMap<String, String> encIds = new HashMap<String, String>();
    private HashMap<String, QName> encQNames = new HashMap<String, QName>();
    private HashMap<String, String> edAlgos = new HashMap<String, String>();
    //used for bsp checks
    private BasicSecurityProfile bspContext = new BasicSecurityProfile();
    //private boolean isBSP = false;

    public SecurityRecipient(XMLStreamReader reader, SOAPVersion soapVersion) {

        this.message = reader;
        this.soapVersion = soapVersion;
        this.SOAP_NAMESPACE_URI = soapVersion.nsUri;
        securityContext = new SecurityContext();
    }

    public SecurityRecipient(XMLStreamReader reader, SOAPVersion soapVersion, AttachmentSet attachSet) {
        this(reader, soapVersion);
        securityContext.setAttachmentSet(attachSet);
    }

    public Message validateMessage(JAXBFilterProcessingContext ctx) throws XWSSecurityException {
        try {
            this.context = ctx;
            context.setSecurityContext(securityContext);
            // Move to soap:Envelope and verify

            //unconditionally set these since the policy is unknown
            context.setExtraneousProperty("EnableWSS11PolicyReceiver", "true");
            List scList = new ArrayList();
            context.setExtraneousProperty("receivedSignValues", scList);

            if (message.getEventType() != XMLStreamConstants.START_ELEMENT) {
                XMLStreamReaderUtil.nextElementContent(message);
            }
            XMLStreamReaderUtil.verifyReaderState(message, XMLStreamConstants.START_ELEMENT);
            if (SOAP_ENVELOPE.equals(message.getLocalName()) && !SOAP_NAMESPACE_URI.equals(message.getNamespaceURI())) {
                throw new VersionMismatchException(soapVersion, SOAP_NAMESPACE_URI, message.getNamespaceURI());
            }
            XMLStreamReaderUtil.verifyTag(message, SOAP_NAMESPACE_URI, SOAP_ENVELOPE);

            envelopeTag = new TagInfoset(message);
            for (int i = 0; i < message.getNamespaceCount(); i++) {
                parentNS.put(message.getNamespacePrefix(i), message.getNamespaceURI(i));
                parentNSOnEnvelope.put(message.getNamespacePrefix(i), message.getNamespaceURI(i));
            }
            // Move to next element
            XMLStreamReaderUtil.nextElementContent(message);
            XMLStreamReaderUtil.verifyReaderState(message, javax.xml.stream.XMLStreamConstants.START_ELEMENT);


            if (message.getLocalName().equals(SOAP_HEADER) && message.getNamespaceURI().equals(SOAP_NAMESPACE_URI)) {
                headerTag = new TagInfoset(message);

                // Collect namespaces on soap:Header
                for (int i = 0; i < message.getNamespaceCount(); i++) {
                    parentNS.put(message.getNamespacePrefix(i), message.getNamespaceURI(i));
                }
                // skip <soap:Header>
                XMLStreamReaderUtil.nextElementContent(message);

                // If SOAP header blocks are present (i.e. not <soap:Header/>)
                if (message.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    this.headers = new HeaderList();
                    // Cache SOAP header blocks
                    cacheHeaders(message, parentNS);

                }
                // Move to soap:Body
                XMLStreamReaderUtil.nextElementContent(message);
            }

            return createMessage();
        } catch (WssSoapFaultException ex) {
            Message msg = new StreamMessage(envelopeTag, headerTag, new AttachmentSetImpl(), headers, bodyTag, getEmptyBodyNoException(), soapVersion);
            ctx.setPVMessage(msg);
            throw ex;
        } catch (WebServiceException te) {
            Message msg = new StreamMessage(envelopeTag, headerTag, new AttachmentSetImpl(), headers, bodyTag, getEmptyBodyNoException(), soapVersion);
            ctx.setPVMessage(msg);
            throw te;
        } catch (XMLStreamException e) {
            // TODO need to throw more meaningful exception
            Message msg = new StreamMessage(envelopeTag, headerTag, new AttachmentSetImpl(), headers, bodyTag, getEmptyBodyNoException(), soapVersion);
            ctx.setPVMessage(msg);
            throw new WebServiceException(e);
        } catch (XWSSecurityException xe) {
            Message msg = new StreamMessage(envelopeTag, headerTag, new AttachmentSetImpl(), headers, bodyTag, getEmptyBodyNoException(), soapVersion);
            ctx.setPVMessage(msg);
            throw xe;
        } catch (XWSSecurityRuntimeException re) {
            Message msg = new StreamMessage(envelopeTag, headerTag, new AttachmentSetImpl(), headers, bodyTag, getEmptyBodyNoException(), soapVersion);
            ctx.setPVMessage(msg);
            throw re;
        } catch (Exception e) {
            Message msg = new StreamMessage(envelopeTag, headerTag, new AttachmentSetImpl(), headers, bodyTag, getEmptyBodyNoException(), soapVersion);
            ctx.setPVMessage(msg);
            throw new XWSSecurityRuntimeException(e);
        }
    }

    private void cacheHeaders(XMLStreamReader reader, Map<String, String> namespaces) throws XMLStreamException, XWSSecurityException {

        creator = new StreamReaderBufferCreator();
        MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
        creator.setXMLStreamBuffer(buffer);
        // Reader is positioned at the first header block
        while (reader.getEventType() == javax.xml.stream.XMLStreamConstants.START_ELEMENT) {
            Map<String, String> headerBlockNamespaces = namespaces;

            // Create Header
            if (MessageConstants.WSSE_SECURITY_LNAME.equals(reader.getLocalName()) &&
                    MessageConstants.WSSE_NS.equals(reader.getNamespaceURI())) {
                // Collect namespaces on SOAP header block
                if (reader.getNamespaceCount() > 0) {
                    headerBlockNamespaces = new HashMap<String, String>(namespaces);
                    for (int i = 0; i < reader.getNamespaceCount(); i++) {
                        headerBlockNamespaces.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
                    }
                }
                // Mark
                XMLStreamBuffer mark = new XMLStreamBufferMark(headerBlockNamespaces, creator);
                handleSecurityHeader();
            } else {
                try {
                    // Collect namespaces on SOAP header block
                    if (reader.getNamespaceCount() > 0) {
                        headerBlockNamespaces = new HashMap<String, String>(namespaces);
                        for (int i = 0; i < reader.getNamespaceCount(); i++) {
                            headerBlockNamespaces.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
                        }
                    }
                    // Mark
                    XMLStreamBuffer mark = new XMLStreamBufferMark(headerBlockNamespaces, creator);
                    GenericSecuredHeader gsh = new GenericSecuredHeader(reader, soapVersion, creator, (HashMap) namespaces, staxIF, context.getEncHeaderContent());
                    headers.add(gsh);

                } catch (XMLStreamBufferException ex) {
                    throw new XWSSecurityException("Error occurred while reading SOAP Header" + ex);
                }
            }
            while (reader.isWhiteSpace()) {
                reader.next();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleSecurityHeader() throws XWSSecurityException {
        // MutableXMLStreamBuffer buffer = new MutableXMLStreamBuffer();
        try {
            if (message.getEventType() == XMLStreamReader.START_ELEMENT) {
                if (MessageConstants.WSSE_SECURITY_LNAME.equals(message.getLocalName()) &&
                        MessageConstants.WSSE_NS.equals(message.getNamespaceURI())) {
                    for (int i = 0; i < message.getNamespaceCount(); i++) {
                        securityHeaderNS.put(message.getNamespacePrefix(i), message.getNamespaceURI(i));
                    }
                    StreamUtil.moveToNextStartOREndElement(message);
                }
            }

            HashMap<String, String> currentParentNS = new HashMap<String, String>();
            currentParentNS.putAll(parentNS);
            currentParentNS.putAll(securityHeaderNS);
            envshNS = currentParentNS;
            int eventType = getSecurityElementType();
            securityContext.setSOAPEnvelopeNSDecls(parentNSOnEnvelope);
            securityContext.setSecurityHdrNSDecls(securityHeaderNS);
            securityContext.setNonSecurityHeaders(headers);
            securityContext.setProcessedSecurityHeaders(processedHeaders);
            securityContext.setBufferedSecurityHeaders(bufferedHeaders);
            context.setSecurityContext(securityContext);
            while (message.getEventType() != message.END_DOCUMENT) {
                switch (eventType) {
                    case TIMESTAMP_ELEMENT: {
                        if (context.isBSP() && bspContext.isTimeStampFound()) {
                            log_bsp_3203();
                        }
                        bspContext.setTimeStampFound(true);
                        TimestampHeader timestamp = new TimestampHeader(message, creator, (HashMap) currentParentNS, context);
                        WSSPolicy policy = timestamp.getPolicy();
                        ((TokenValidator) timestamp).validate(context);
                        processedHeaders.add(timestamp);
                        context.getInferredSecurityPolicy().append(timestamp.getPolicy());
                        break;
                    }
                    case USERNAME_TOKEN_ELEMENT: {
                        UsernameTokenHeader ut = new UsernameTokenHeader(message, creator, (HashMap) currentParentNS, staxIF);
                        ut.validate(context);
                        context.getSecurityContext().getProcessedSecurityHeaders().add(ut);
                        context.getInferredSecurityPolicy().append(ut.getPolicy());
                        if (context.isTrustMessage() && !context.isClient()) {
                            IssuedTokenContext ctx = null;
                            if (context.getTrustContext() == null) {
                                ctx = new IssuedTokenContextImpl();
                                if (context.isSecure()) {
                                    ctx.setAuthnContextClass(MessageConstants.PASSWORD_PROTECTED_TRANSPORT_AUTHTYPE);
                                } else {
                                    ctx.setAuthnContextClass(MessageConstants.PASSWORD_AUTH_TYPE);
                                }
                                context.setTrustContext(ctx);
                            } else {
                                ctx = context.getTrustContext();
                                if (ctx.getAuthnContextClass() != null) {
                                    if (context.isSecure()) {
                                        ctx.setAuthnContextClass(MessageConstants.PASSWORD_PROTECTED_TRANSPORT_AUTHTYPE);
                                    } else {
                                        ctx.setAuthnContextClass(MessageConstants.PASSWORD_AUTH_TYPE);
                                    }
                                    context.setTrustContext(ctx);
                                }
                            }
                        }
                        break;
                    }
                    case BINARYSECURITY_TOKEN_ELEMENT: {
                        String valueType = message.getAttributeValue(null, MessageConstants.WSE_VALUE_TYPE);
                        if (MessageConstants.KERBEROS_V5_GSS_APREQ_1510.equals(valueType) ||
                                MessageConstants.KERBEROS_V5_GSS_APREQ.equals(valueType)) {
                            KerberosBinarySecurityToken kbst = new KerberosBinarySecurityToken(message, creator, (HashMap) currentParentNS, staxIF);
                            WSSPolicy policy = kbst.getPolicy();
                            ((TokenValidator) kbst).validate(context);
                            processedHeaders.add(kbst);
                            context.getInferredSecurityPolicy().append(kbst.getPolicy());
                            if (context.isTrustMessage() && !context.isClient()) {
                                IssuedTokenContext ctx = null;
                                if (context.getTrustContext() == null) {
                                    ctx = new IssuedTokenContextImpl();
                                    ctx.setAuthnContextClass(MessageConstants.KERBEROS_AUTH_TYPE);
                                    context.setTrustContext(ctx);
                                } else {
                                    ctx = context.getTrustContext();
                                    if (ctx.getAuthnContextClass() != null) {
                                        ctx.setAuthnContextClass(MessageConstants.KERBEROS_AUTH_TYPE);
                                        context.setTrustContext(ctx);
                                    }
                                }
                            }
                        } else if (MessageConstants.X509v3_NS.equals(valueType) ||
                                MessageConstants.X509v1_NS.equals(valueType) ||
                                valueType == null) /*null takes as X509 BST */ {
                            X509BinarySecurityToken bst = new X509BinarySecurityToken(message, creator, (HashMap) currentParentNS, staxIF);
                            WSSPolicy policy = bst.getPolicy();
                            ((TokenValidator) bst).validate(context);
                            processedHeaders.add(bst);
                            context.getInferredSecurityPolicy().append(bst.getPolicy());
                            if (context.isTrustMessage() && !context.isClient()) {
                                IssuedTokenContext ctx = null;
                                if (context.getTrustContext() == null) {
                                    ctx = new IssuedTokenContextImpl();
                                    ctx.setAuthnContextClass(MessageConstants.X509_AUTH_TYPE);
                                    context.setTrustContext(ctx);
                                } else {
                                    ctx = context.getTrustContext();
                                    if (ctx.getAuthnContextClass() != null) {
                                        ctx.setAuthnContextClass(MessageConstants.X509_AUTH_TYPE);
                                        context.setTrustContext(ctx);
                                    }
                                }
                            }
                        }

                        break;
                    }
                    case ENCRYPTED_KEY_ELEMENT: {
                        EncryptedKey ek = new EncryptedKey(message, context, (HashMap) currentParentNS);
                        ArrayList<String> list = (ArrayList) ek.getPendingReferenceList();
                        if (list != null) {
                            findAndReplaceED(list, ek);
                            if (ek.getPendingReferenceList().size() > 0) {
                                if (pendingElement == null) {
                                    pendingElement = ek;
                                }//else{
                                addSecurityHeader(ek);
                            //}
                            }
                        } else {
                            addSecurityHeader(ek);
                        }
                        if (ek.getPolicy() != null) {
                            context.getInferredSecurityPolicy().append(ek.getPolicy());
                        }
                        break;
                    }
                    case ENCRYPTED_DATA_ELEMENT: {
                        EncryptedData ed = new EncryptedData(message, context, currentParentNS);
                        handleEncryptedData(ed, currentParentNS);
                        break;
                    }

                    case ENCRYPTED_HEADER_ELEMENT: {
                        throw new XWSSecurityException("wsse11:EncryptedHeader not allowed inside SecurityHeader");
                    //break;
                    }

                    case REFERENCE_LIST_ELEMENT: {
                        ReferenceListHeader refList = new ReferenceListHeader(message, context);
                        if (pendingElement == null) {
                            pendingElement = refList;
                        } else {
                            addSecurityHeader(refList);
                        }

                        context.getInferredSecurityPolicy().append(refList.getPolicy());
                        break;
                    }
                    case SIGNATURE_ELEMENT: {
                        Signature sig = new Signature(context, currentParentNS, creator);
                        sig.process(message);
                        if (!sig.isValidated()) {
                            if (pendingElement == null) {
                                pendingElement = sig;
                            } else {
                                addSecurityHeader(sig);
                            }
                        } else {
                            if (!processedHeaders.contains(sig)) {
                                processedHeaders.add(sig);
                            }
                        }
                        context.getInferredSecurityPolicy().append(sig.getPolicy());
                        break;
                    }
                    case DERIVED_KEY_ELEMENT: {
                        DerivedKeyToken dkt = new DerivedKeyToken(message, context, (HashMap) currentParentNS);
                        processedHeaders.add(dkt);
                        break;
                    }
                    case SIGNATURE_CONFIRMATION_ELEMENT: {
                        SignatureConfirmation signConfirm = new SignatureConfirmation(message, creator, (HashMap) currentParentNS, staxIF);
                        WSSPolicy policy = signConfirm.getPolicy();
                        signConfirm.validate(context);
                        processedHeaders.add(signConfirm);
                        context.getInferredSecurityPolicy().append(signConfirm.getPolicy());
                        break;
                    }
                    case SECURITY_CONTEXT_TOKEN: {
                        SecurityContextToken sct = new SecurityContextToken(message, context, (HashMap) currentParentNS);
                        processedHeaders.add(sct);
                        break;
                    }
                    case SAML_ASSERTION_ELEMENT: {
                        SAMLAssertion samlAssertion = new SAMLAssertion(message, context, null, (HashMap) currentParentNS);
                        processedHeaders.add(samlAssertion);
                        if (samlAssertion.isHOK()) {
                            if (!samlAssertion.validateSignature()) {
                                logger.log(Level.SEVERE, LogStringsMessages.WSS_1614_SAML_SIGNATURE_INVALID());
                                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_AUTHENTICATION,
                                        LogStringsMessages.WSS_1614_SAML_SIGNATURE_INVALID(),
                                        new Exception(LogStringsMessages.WSS_1614_SAML_SIGNATURE_INVALID()));
                            }
                        }
                        samlAssertion.validate(context);
                        samlAssertion.getKey();
                        // Set in the extraneous property only if not already set
                        // workaround in the case where there are two HOK assertions in the request
                        if (context.getExtraneousProperty(MessageConstants.INCOMING_SAML_ASSERTION) == null && samlAssertion.isHOK()) {
                            context.getExtraneousProperties().put(MessageConstants.INCOMING_SAML_ASSERTION, samlAssertion);
                        }
                        if (context.isTrustMessage() && !context.isClient()) {
                            IssuedTokenContext ctx = null;
                            if (context.getTrustContext() == null) {
                                ctx = new IssuedTokenContextImpl();
                                ctx.setAuthnContextClass(MessageConstants.PREVIOUS_SESSION_AUTH_TYPE);
                                context.setTrustContext(ctx);
                            } else {
                                ctx = context.getTrustContext();
                                if (ctx.getAuthnContextClass() != null) {
                                    ctx.setAuthnContextClass(MessageConstants.PREVIOUS_SESSION_AUTH_TYPE);
                                    context.setTrustContext(ctx);
                                }
                            }
                        } else if (!context.isTrustMessage()) {
                            context.getInferredSecurityPolicy().append(samlAssertion.getPolicy());
                        }

                        break;
                    }
                    case STR_ELEMENT: {
                        SecurityTokenProcessor str = new SecurityTokenProcessor(context, null);
                        str.resolveReference(message);
                        break;
                    }
                    default: {
                        // Throw Exception if an unrecognized Security Header is present
                        if (message.getEventType() == message.START_ELEMENT &&
                                getSecurityElementType() == -1) {
                            logger.log(Level.SEVERE, LogStringsMessages.WSS_1613_UNRECOGNIZED_SECURITY_ELEMENT(message.getLocalName()));
                            throw new XWSSecurityException(LogStringsMessages.WSS_1613_UNRECOGNIZED_SECURITY_ELEMENT(message.getLocalName()));
                        }
                    }
                }
                if (StreamUtil._break(message, MessageConstants.WSSE_SECURITY_LNAME, MessageConstants.WSSE_NS)) {
                    break;
                }
                eventType = getSecurityElementType();
                if (eventType == -1 && !StreamUtil.isStartElement(message)) {
                    if (StreamUtil._break(message, MessageConstants.WSSE_SECURITY_LNAME, MessageConstants.WSSE_NS)) {
                        break;
                    } else {
                        message.next();
                    }
                }
            }
            message.next();
        } catch (XMLStreamException ex) {
            //ex.printStackTrace();
            logger.log(Level.FINE, "Error occurred while reading SOAP Headers", ex);
            throw new XWSSecurityException(ex);
        } catch (XMLStreamBufferException ex) {
            //  ex.printStackTrace();
            logger.log(Level.FINE, "Error occurred while reading SOAP Headers", ex);
            throw new XWSSecurityException(ex);
        }
    }

    private void handleEncryptedData(EncryptedData ed, HashMap<String, String> currentParentNS) throws XMLStreamException, XWSSecurityException {
        if (pendingElement != null && pendingElement instanceof EncryptedKey) {
            EncryptedKey ek = ((EncryptedKey) pendingElement);
            if (ek.getPendingReferenceList() != null && ek.getPendingReferenceList().contains(ed.getId())) {
                //for policy verification
                if (ek.getPolicy() != null) {
                    ek.getPolicy().setKeyBinding(ek.getInferredKB());
                }
                //

                if (!ed.hasCipherReference()) {
                    XMLStreamReader decryptedData = ed.getDecryptedData(ek.getKey(ed.getEncryptionAlgorithm()));
                    SecurityHeaderProcessor shp = new SecurityHeaderProcessor(context, currentParentNS, staxIF, creator);
                    if (decryptedData.getEventType() != XMLStreamReader.START_ELEMENT) {
                        StreamUtil.moveToNextElement(decryptedData);
                    }
                    SecurityHeaderElement she = shp.createHeader(decryptedData);
                    encIds.put(ed.getId(), she.getId());
                    edAlgos.put(ed.getId(), ed.getEncryptionAlgorithm());
                    ek.getPendingReferenceList().remove(ed.getId());
                    if (ek.getPendingReferenceList().isEmpty()) {
                        pendingElement = null;
                        bufferedHeaders.remove(ek);
                        addSecurityHeader(ek);
                    }
                    checkDecryptedData(she, ek.getPolicy());
                } else {
                    // Handle Encrypted Attachment here 
                    byte[] decryptedMimeData = ed.getDecryptedMimeData(ek.getKey(ed.getEncryptionAlgorithm()));
                    Attachment as = new AttachmentImpl(ed.getAttachmentContentId(), decryptedMimeData, ed.getAttachmentMimeType());
                    securityContext.getDecryptedAttachmentSet().add(as);
                    ek.getPendingReferenceList().remove(ed.getId());
                    if (ek.getPendingReferenceList().isEmpty()) {
                        pendingElement = null;
                        bufferedHeaders.remove(ek);
                        addSecurityHeader(ek);
                    }
                }
            } else {
                if (!lookInBufferedHeaders(ed, currentParentNS)) {
                    addSecurityHeader(ed);
                }
            }
        } else if (pendingElement != null && pendingElement instanceof ReferenceListHeader) {
            ReferenceListHeader refList = (ReferenceListHeader) pendingElement;
            if (refList.getPendingReferenceList().contains(ed.getId())) {
                //for policy verification
                refList.getPolicy().setKeyBinding(ed.getInferredKB());
                //

                if (!ed.hasCipherReference()) {
                    XMLStreamReader decryptedData = ed.getDecryptedData();
                    if (decryptedData.getEventType() != XMLStreamReader.START_ELEMENT) {
                        StreamUtil.moveToNextElement(decryptedData);
                    }
                    SecurityHeaderProcessor shp = new SecurityHeaderProcessor(context, currentParentNS, staxIF, creator);
                    SecurityHeaderElement she = shp.createHeader(decryptedData);
                    encIds.put(ed.getId(), she.getId());
                    edAlgos.put(ed.getId(), ed.getEncryptionAlgorithm());
                    refList.getPendingReferenceList().remove(ed.getId());
                    if (refList.getPendingReferenceList().isEmpty()) {
                        pendingElement = null;
                    }
                    checkDecryptedData(she, refList.getPolicy());
                } else {
                    // Handle Encrypted Attachment here 
                    byte[] decryptedMimeData = ed.getDecryptedMimeData();
                    Attachment as = new AttachmentImpl(ed.getAttachmentContentId(), decryptedMimeData, ed.getAttachmentMimeType());
                    securityContext.getDecryptedAttachmentSet().add(as);
                    refList.getPendingReferenceList().remove(ed.getId());
                    if (refList.getPendingReferenceList().isEmpty()) {
                        pendingElement = null;
                    }
                }
            } else {
                if (!lookInBufferedHeaders(ed, currentParentNS)) {
                    decryptStandaloneED(ed, currentParentNS, refList.getPolicy());
                }
            }
        } else {
            if (!lookInBufferedHeaders(ed, currentParentNS)) {
                decryptStandaloneED(ed, currentParentNS, null);
            }
        }
    }

    private boolean lookInBufferedHeaders(EncryptedData ed, HashMap<String, String> currentParentNS) throws XWSSecurityException, XMLStreamException {
        if (bufferedHeaders.size() > 0) {
            for (int i = 0; i < bufferedHeaders.size(); i++) {
                SecurityHeaderElement bufShe = (SecurityHeaderElement) bufferedHeaders.get(i);
                if (MessageConstants.ENCRYPTEDKEY_LNAME.equals(bufShe.getLocalPart()) &&
                        MessageConstants.XENC_NS.equals(bufShe.getNamespaceURI())) {
                    EncryptedKey ek = ((EncryptedKey) bufShe);
                    if (ek.getPendingReferenceList() != null && ek.getPendingReferenceList().contains(ed.getId())) {
                        //for policy verification
                        if (ek.getPolicy() != null) {
                            ek.getPolicy().setKeyBinding(ek.getInferredKB());
                        }
                        if (!ed.hasCipherReference()) {
                            XMLStreamReader decryptedData = ed.getDecryptedData(ek.getKey(ed.getEncryptionAlgorithm()));
                            SecurityHeaderProcessor shp = new SecurityHeaderProcessor(context, currentParentNS, staxIF, creator);
                            if (decryptedData.getEventType() != XMLStreamReader.START_ELEMENT) {
                                StreamUtil.moveToNextElement(decryptedData);
                            }
                            SecurityHeaderElement she = shp.createHeader(decryptedData);
                            encIds.put(ed.getId(), she.getId());
                            edAlgos.put(ed.getId(), ed.getEncryptionAlgorithm());
                            //addSecurityHeader(she);
                            ek.getPendingReferenceList().remove(ed.getId());
                            checkDecryptedData(she, ek.getPolicy());
                        } else {
                            // handle encrypted attachment here
                            byte[] decryptedMimeData = ed.getDecryptedMimeData(ek.getKey(ed.getEncryptionAlgorithm()));
                            Attachment as = new AttachmentImpl(ed.getAttachmentContentId(), decryptedMimeData, ed.getAttachmentMimeType());
                            securityContext.getDecryptedAttachmentSet().add(as);
                            ek.getPendingReferenceList().remove(ed.getId());
                        }
                        return true;
                    }

                } else if (MessageConstants.XENC_REFERENCE_LIST_LNAME.equals(bufShe.getLocalPart()) &&
                        MessageConstants.XENC_NS.equals(bufShe.getNamespaceURI())) {
                    ReferenceListHeader refList = (ReferenceListHeader) bufShe;
                    if (refList.getPendingReferenceList().contains(ed.getId())) {
                        //for policy verification
                        refList.getPolicy().setKeyBinding(ed.getInferredKB());
                        //

                        if (!ed.hasCipherReference()) {
                            XMLStreamReader decryptedData = ed.getDecryptedData();
                            if (decryptedData.getEventType() != XMLStreamReader.START_ELEMENT) {
                                StreamUtil.moveToNextElement(decryptedData);
                            }
                            SecurityHeaderProcessor shp = new SecurityHeaderProcessor(context, currentParentNS, staxIF, creator);
                            SecurityHeaderElement she = shp.createHeader(decryptedData);
                            encIds.put(ed.getId(), she.getId());
                            edAlgos.put(ed.getId(), ed.getEncryptionAlgorithm());
                            //addSecurityHeader(she);
                            refList.getPendingReferenceList().remove(ed.getId());
                            checkDecryptedData(she, refList.getPolicy());
                        } else {
                            // handle encrypted attachment here
                            byte[] decryptedMimeData = ed.getDecryptedMimeData();
                            Attachment as = new AttachmentImpl(ed.getAttachmentContentId(), decryptedMimeData, ed.getAttachmentMimeType());
                            securityContext.getDecryptedAttachmentSet().add(as);
                            refList.getPendingReferenceList().remove(ed.getId());
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void decryptStandaloneED(EncryptedData ed, HashMap<String, String> currentParentNS, WSSPolicy policy) throws XMLStreamException, XWSSecurityException {
        if (ed.getKey() == null) { //can't decrypt now
            bufferedHeaders.add(ed);
            return;
        }
        if (!ed.hasCipherReference()) {
            XMLStreamReader decryptedData = ed.getDecryptedData();
            if (decryptedData.getEventType() != XMLStreamReader.START_ELEMENT) {
                StreamUtil.moveToNextElement(decryptedData);
            }
            SecurityHeaderProcessor shp = new SecurityHeaderProcessor(context, currentParentNS, staxIF, creator);
            SecurityHeaderElement she = shp.createHeader(decryptedData);
            encIds.put(ed.getId(), she.getId());
            edAlgos.put(ed.getId(), ed.getEncryptionAlgorithm());
            checkDecryptedData(she, policy);
            if (!handleSAMLAssertion(she)) {
                addSecurityHeader(she);
            }
        } else {
            // handle encrypted attachment here
            byte[] decryptedMimeData = ed.getDecryptedMimeData();
            Attachment as = new AttachmentImpl(ed.getAttachmentContentId(), decryptedMimeData, ed.getAttachmentMimeType());
            securityContext.getDecryptedAttachmentSet().add(as);
        }
    }

    @SuppressWarnings("unchecked")
    private void checkDecryptedData(SecurityHeaderElement she, WSSPolicy pol) throws XWSSecurityException {
        if (MessageConstants.SIGNATURE_LNAME.equals(she.getLocalPart())) {
            if (PolicyTypeUtil.encryptionPolicy(pol)) {
                EncryptionPolicy ep = (EncryptionPolicy) pol;
                EncryptionPolicy.FeatureBinding fb =
                        (EncryptionPolicy.FeatureBinding) ep.getFeatureBinding();
                fb.encryptsSignature(true);
            }
            Signature sig = (Signature) she;
            if (!sig.getReferences().isEmpty() && isPending()) {
                if (pendingElement == null) {
                    pendingElement = she;
                } else {
                    bufferedHeaders.add(sig);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "static-access"})
    private Message createMessage() throws XWSSecurityException {
        // Verify that <soap:Body> is present
        XMLStreamReaderUtil.verifyTag(message, SOAP_NAMESPACE_URI, SOAP_BODY);
        bodyTag = new TagInfoset(message);
        bodyENVNS.putAll(parentNSOnEnvelope);
        for (int i = 0; i < message.getNamespaceCount(); i++) {
            bodyENVNS.put(message.getNamespacePrefix(i), message.getNamespaceURI(i));
        }
        bodyWsuId = StreamUtil.getWsuId(message);
        if (bodyWsuId == null) {
            bodyWsuId = "";
        }
        XMLStreamReaderUtil.nextElementContent(message);
        cachePayLoadId();
        if (pendingElement != null) {
            //String id = StreamUtil.getWsuId(message);
            try {
                if (pendingElement instanceof EncryptedKey && StreamUtil.isStartElement(message) && isEncryptedData()) {
                    XMLStreamReader reader = message;
                    EncryptedData ed = new EncryptedData(message, context, bodyENVNS);
                    payLoadWsuId = ed.getId();
                    handlePayLoadED(ed);
                    payLoadEncId = ed.getId();
                    XMLStreamReaderUtil.close(reader);
                    XMLStreamReaderFactory.recycle(reader);
                } else if (pendingElement instanceof Signature) {
                    Signature sig = (Signature) pendingElement;
                    handleSignature(sig);

                    processedHeaders.add(pendingElement);
                    pendingElement = null;
                } else if (pendingElement instanceof ReferenceListHeader) {
                    ReferenceListHeader refList = (ReferenceListHeader) pendingElement;
                    if (refList.getPendingReferenceList().contains(payLoadWsuId)) {
                        XMLStreamReader reader = message;
                        EncryptedData ed = new EncryptedData(message, context, bodyENVNS);
                        //for policy verification
                        refList.getPolicy().setKeyBinding(ed.getInferredKB());
                        payLoadWsuId = ed.getId();
                        handlePayLoadED(ed);
                        refList.getPendingReferenceList().remove(payLoadWsuId);
                        payLoadEncId = ed.getId();
                        cachePayLoadId();
                        XMLStreamReaderUtil.close(reader);
                        XMLStreamReaderFactory.recycle(reader);
                    }
                    if (refList.getPendingReferenceList().size() > 0) {
                        findAndReplaceED((ArrayList<String>) refList.getPendingReferenceList(), refList);
                    }
                    if (refList.getPendingReferenceList().isEmpty()) {
                        pendingElement = null;
                    } else {
                        String uri = refList.getPendingReferenceList().get(0);
                        throw new XWSSecurityException("Reference with ID " + uri + " was not found in the message");
                    }

                }
            } catch (XMLStreamException e) {
                // TODO need to throw more meaningful exception
                throw new WebServiceException(e);
            } catch (XWSSecurityException xse) {
                throw new WebServiceException(xse);
            }
        }

        ArrayList clonedBufferedHeaders = (ArrayList) bufferedHeaders.clone();
        if (clonedBufferedHeaders.size() > 0) {
            for (int i = 0; i < clonedBufferedHeaders.size(); i++) {
                SecurityHeaderElement she = (SecurityHeaderElement) clonedBufferedHeaders.get(i);
                processSecurityHeader(she);
            }
        }
        if (processedHeaders.size() > 0) {
            for (int i = 0; i < processedHeaders.size(); i++) {
                SecurityHeaderElement she = (SecurityHeaderElement) processedHeaders.get(i);
                processProcessedHeaders(she);
            }
        }

        try {
            if (message == null) {
                message = getEmptyBody();
            }
        } catch (XMLStreamException xse) {
            throw new XWSSecurityException(xse);
        }

        Message streamMsg = null;
        AttachmentSet as = securityContext.getDecryptedAttachmentSet();
        if (as == null || as.isEmpty()) {
            as = securityContext.getAttachmentSet();
        }        
        if (!context.getDisablePayloadBuffering() && (!context.isSecure() || "Fault".equals(message.getLocalName()))) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Buffering Payload from incomming message");
            }
            VerifiedMessageXMLStreamReader verifiedReader = new VerifiedMessageXMLStreamReader(message, bodyENVNS);
            streamMsg = new VerifiedStreamMessage(envelopeTag, headerTag, as, headers, bodyTag, verifiedReader, soapVersion, this.bodyENVNS);
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Not Buffering Payload from incomming message");
            }
            streamMsg = new StreamMessage(envelopeTag, headerTag, as, headers, bodyTag, message, soapVersion);
        }
        context.setMessage(streamMsg);
        boolean scCancel = false;
        /*if(streamMsg.isFault())
        return streamMsg;*/
        if (context.getAddressingVersion() != null) {
            String action = streamMsg.getHeaders().getAction(context.getAddressingVersion(), context.getSOAPVersion());

            if (MessageConstants.MEX_GET.equals(action)) {
                return streamMsg;
            }
            if (MessageConstants.CANCEL_SECURITY_CONTEXT_TOKEN_ACTION.equals(action) ||
                    MessageConstants.CANCEL_SECURITY_CONTEXT_TOKEN_RESPONSE_ACTION.equals(action)) {
                scCancel = true;
            }
        }
        SecurityPolicy msgPolicy =  context.getSecurityPolicy();

        //boolean isTrust = context.isTrustMessage();
        if (isEmpty(msgPolicy)) {
            PolicyResolver opResolver =
                    (PolicyResolver) context.getExtraneousProperty(context.OPERATION_RESOLVER);
            if (opResolver != null) {
                msgPolicy = opResolver.resolvePolicy(context);
            }
        }
        if (context.isSecure() && context.getInferredSecurityPolicy().isEmpty()) {
            if (isEmpty(msgPolicy) || context.isMissingTimestampAllowed()) {
                return streamMsg;
            } else {
                throw new XWSSecurityException("Security Requirements not met - No Security header in message");
            }
        }

        // for policy verification, replace target uris with qnames for signature and encryption targets
        try {
            MessagePolicy inferredMessagePolicy = context.getInferredSecurityPolicy();
            for (int i = 0; i < inferredMessagePolicy.size(); i++) {
                WSSPolicy wssPolicy = (WSSPolicy) inferredMessagePolicy.get(i);
                if (PolicyTypeUtil.signaturePolicy(wssPolicy)) {
                    SignaturePolicy.FeatureBinding fb = (SignaturePolicy.FeatureBinding) wssPolicy.getFeatureBinding();
                    ArrayList targets = fb.getTargetBindings();
                    // replace uri target types with qname target types
                    modifyTargets(targets);
                } else if (PolicyTypeUtil.encryptionPolicy(wssPolicy)) {
                    EncryptionPolicy.FeatureBinding fb = (EncryptionPolicy.FeatureBinding) wssPolicy.getFeatureBinding();
                    ArrayList targets = fb.getTargetBindings();
                    // replace uri target types with qname target types
                    modifyTargets(targets);
                }
            }

        } catch (Exception ex) {
            throw new XWSSecurityException(ex);
        }

        if (scCancel) {
            boolean securedBody = false;
            boolean allHeaders = false;
            try {
                MessagePolicy mp = context.getInferredSecurityPolicy();
                for (int i = 0; i < mp.size(); i++) {
                    WSSPolicy wp = (WSSPolicy) mp.get(i);
                    if (PolicyTypeUtil.encryptionPolicy(wp)) {
                        EncryptionPolicy ep = (EncryptionPolicy) wp;
                        ArrayList list = ((EncryptionPolicy.FeatureBinding) ep.getFeatureBinding()).getTargetBindings();
                        for (int ei = 0; ei < list.size(); ei++) {
                            EncryptionTarget et = (EncryptionTarget) list.get(ei);
                            if (et.getValue().equals(Target.BODY)) {
                                securedBody = true;
                            }
                        }
                    } else if (PolicyTypeUtil.signaturePolicy(wp)) {
                        SignaturePolicy sp = (SignaturePolicy) wp;
                        ArrayList list = ((SignaturePolicy.FeatureBinding) sp.getFeatureBinding()).getTargetBindings();
                        for (int ei = 0; ei < list.size(); ei++) {
                            SignatureTarget st = (SignatureTarget) list.get(ei);
                            //if(st.getValue() == Target.BODY){
                            if (st.getValue().equals(Target.BODY)) {
                                securedBody = true;
                            }
                        }
                        if (!allHeaders) {
                            allHeaders = areHeadersSecured(sp);
                        }
                    }
                }
            } catch (Exception ex) {
                throw new XWSSecurityException(ex);
            }

            if (!context.isSecure() && (!securedBody || !allHeaders)) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1602_SCCANCEL_SECURITY_UNCONFIGURED());
                throw new XWSSecurityException("Security Requirements not met");
            }
            return streamMsg;
        }

        if (context.getInferredSecurityPolicy() == null || context.getInferredSecurityPolicy().isEmpty()) {
            if (context.isClient() && MessageConstants.RSTR_CANCEL_ACTION.equals(context.getAction())) {
                return streamMsg;
            }
        }

        // for policy verification
        //TargetResolver targetResolver = new TargetResolverImpl(context);
        //MessagePolicyVerifier mpv = new MessagePolicyVerifier(context, targetResolver);
        PolicyVerifier mpv = PolicyVerifierFactory.createVerifier(msgPolicy, context);
        mpv.verifyPolicy(context.getInferredSecurityPolicy(), msgPolicy);
        
        return streamMsg;
    }

    private XMLStreamReader getEmptyBody() throws XMLStreamException {
        // create an empty body and create xmlstream reader out of it
        String emptyBody = "<S:Body xmlns:S=\"" + soapVersion.nsUri + "\"" + "></S:Body>";
        InputStream in = new ByteArrayInputStream(emptyBody.getBytes());
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader empBody = xif.createXMLStreamReader(in);
        empBody.next(); //next of start document
        empBody.next(); // next of Body, will point to end of body
        return empBody;
    }

    private XMLStreamReader getEmptyBodyNoException() {
        try {
            return getEmptyBody();
        } catch (XMLStreamException ex) {
            logger.log(Level.FINE,"",ex);
        }
        return null;
    }

    // replace uri target types with qname target types
    @SuppressWarnings("unchecked")
    private void modifyTargets(ArrayList targets) {
        for (int i = 0; i < targets.size(); i++) {
            Target target = (Target) targets.get(i);
            if (Target.TARGET_TYPE_VALUE_URI.equals(target.getType())) {
                findAndReplaceTargets(target);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean handleSAMLAssertion(SecurityHeaderElement she) throws XWSSecurityException {
        if (MessageConstants.SAML_ASSERTION_LNAME.equals(she.getLocalPart())) {
            SAMLAssertion samlAssertion = (SAMLAssertion) she;
            processedHeaders.add(samlAssertion);
            if (samlAssertion.isHOK()) {
                samlAssertion.validateSignature();
            }
            samlAssertion.validate(context);
            samlAssertion.getKey();
            context.getExtraneousProperties().put(MessageConstants.INCOMING_SAML_ASSERTION, samlAssertion);
            if (context.isTrustMessage() && !context.isClient()) {
                IssuedTokenContext ctx = null;
                if (context.getTrustContext() == null) {
                    ctx = new IssuedTokenContextImpl();
                    ctx.setAuthnContextClass(MessageConstants.PREVIOUS_SESSION_AUTH_TYPE);
                    context.setTrustContext(ctx);
                } else {
                    ctx = context.getTrustContext();
                    if (ctx.getAuthnContextClass() != null) {
                        ctx.setAuthnContextClass(MessageConstants.PREVIOUS_SESSION_AUTH_TYPE);
                        context.setTrustContext(ctx);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void findAndReplaceTargets(Target target) {
        String uri = target.getValue();
        int index = uri.indexOf("#");
        QName qname = null;
        if (index >= 0) {
            uri = uri.substring(index + 1);
        }
        if (target instanceof EncryptionTarget) {
            String temp = encIds.get(uri);
            String edAlgo = edAlgos.get(uri);
            ((EncryptionTarget) target).setDataEncryptionAlgorithm(edAlgo);
            if (temp != null) {
                uri = temp;
            } else {
                qname = encQNames.get(uri);
            }
        }
        // look if uri is body
        if (uri.equals(bodyWsuId)) {
            target.setType(Target.TARGET_TYPE_VALUE_QNAME);
            target.setValue("SOAP-BODY");
            target.setContentOnly(false);
            return;
        }
        if (uri.equals(payLoadWsuId) || uri.equals(payLoadEncId)) {
            target.setType(Target.TARGET_TYPE_VALUE_QNAME);
            target.setValue("SOAP-BODY");
            target.setContentOnly(true);
            return;
        }

        // look in non-security headers
        if (headers != null && headers.size() > 0) {
            Iterator<Header> listItr = headers.listIterator();
            while (listItr.hasNext()) {
                GenericSecuredHeader header = (GenericSecuredHeader) listItr.next();
                if (header.hasID(uri)) {
                    qname = new QName(header.getNamespaceURI(), header.getLocalPart());
                    target.setQName(qname);
                    target.setContentOnly(false);
                    return;
                } else if (qname != null) {
                    target.setQName(qname);
                    target.setContentOnly(false);
                    return;
                }
            }
        }
        // look in processed headers
        for (int j = 0; j < processedHeaders.size(); j++) {
            SecurityHeaderElement header = (SecurityHeaderElement) processedHeaders.get(j);
            if (uri.equals(header.getId())) {
                qname = new QName(header.getNamespaceURI(), header.getLocalPart());
                target.setQName(qname);
                target.setContentOnly(false);
                return;
            }
        }
        // look in buffered headers
        for (int j = 0; j < bufferedHeaders.size(); j++) {
            SecurityHeaderElement header = (SecurityHeaderElement) bufferedHeaders.get(j);
            if (uri.equals(header.getId())) {
                qname = new QName(header.getNamespaceURI(), header.getLocalPart());
                target.setQName(qname);
                target.setContentOnly(false);
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processProcessedHeaders(SecurityHeaderElement she) throws XWSSecurityException {
        if (she instanceof EncryptedData) {
            throw new XWSSecurityException("Error in Processing, EncryptedData inside procesesdHeaders, should never happen");
        } else if (she instanceof EncryptedKey) {
            EncryptedKey ek = (EncryptedKey) she;
            ArrayList<String> list = (ArrayList) ek.getPendingReferenceList();
            if (list != null && list.size() > 0) {
                throw new XWSSecurityException("Error in processing, ReferenceList inside EK should have been processed");
            }
        } else if (she instanceof ReferenceListHeader) {
            ReferenceListHeader refList = (ReferenceListHeader) she;
            if (refList.getPendingReferenceList() != null && refList.getPendingReferenceList().size() > 0) {
                throw new XWSSecurityException("Error in processing, references in ReferenceList not processed");
            }
        } else if (she instanceof Signature) {
            Signature sig = (Signature) she;
            if (sig.getReferences() != null && sig.getReferences().size() > 0) {
                throw new XWSSecurityException("Error in processing, references in Signature not processed");
            }
        }
    }

    private boolean isPending() throws XWSSecurityException {
        for (int i = 0; i < bufferedHeaders.size(); i++) {
            SecurityHeaderElement she = (SecurityHeaderElement) bufferedHeaders.get(i);
            if (isPrimary(she)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean isPrimary(SecurityHeaderElement she) {
        if (MessageConstants.SIGNATURE_LNAME.equals(she.getLocalPart())) {
            return true;
        } else if (MessageConstants.ENCRYPTEDKEY_LNAME.equals(she.getLocalPart())) {
            return true;
        } else if (MessageConstants.XENC_REFERENCE_LIST_LNAME.equals(she.getLocalPart())) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void processSecurityHeader(SecurityHeaderElement she) throws XWSSecurityException {
        if (she instanceof Signature) {
            handleSignature((Signature) she);
            processedHeaders.add(she);
        } else if (she instanceof EncryptedData) {
            EncryptedData ed = (EncryptedData) she;
            XMLStreamReader decryptedData;
            try {
                if (!ed.hasCipherReference()) {
                    decryptedData = ed.getDecryptedData();
                    SecurityHeaderProcessor shp = new SecurityHeaderProcessor(context, envshNS, staxIF, creator);
                    if (decryptedData.getEventType() != XMLStreamReader.START_ELEMENT) {
                        StreamUtil.moveToNextElement(decryptedData);
                    }
                    SecurityHeaderElement newHeader = shp.createHeader(decryptedData);
                    encIds.put(ed.getId(), newHeader.getId());
                    edAlgos.put(ed.getId(), ed.getEncryptionAlgorithm());
                    processSecurityHeader(newHeader);
                    processedHeaders.add(newHeader);
                } else {
                    // handle encrypted attachment here
                    byte[] decryptedMimeData = ed.getDecryptedMimeData();
                    Attachment as = new AttachmentImpl(ed.getAttachmentContentId(), decryptedMimeData, ed.getAttachmentMimeType());
                    securityContext.getDecryptedAttachmentSet().add(as);
                }
            } catch (XMLStreamException ex) {
                logger.log(Level.SEVERE,"",ex);
                throw new XWSSecurityException("Error occurred while decrypting EncryptedData with ID " + ed.getId(), ex);
            }
        } else if (she instanceof EncryptedKey) {
            EncryptedKey ek = (EncryptedKey) she;
            if (pendingElement == null) {
                pendingElement = ek;
            }
            addSecurityHeader(ek);
            ArrayList<String> list = (ArrayList) ek.getPendingReferenceList();
            if (list != null) {
                findAndReplaceED(list, ek);

                if (ek.getPendingReferenceList().size() > 0 && payLoadWsuId.length() > 0) {
                    if (ek.getPendingReferenceList().contains(payLoadWsuId)) {

                        EncryptedData ed;
                        try {
                            ed = new EncryptedData(message, context, bodyENVNS);
                            payLoadWsuId = ed.getId();
                            handlePayLoadED(ed);
                        } catch (XMLStreamException ex) {
                            logger.log(Level.SEVERE, "", ex);
                            throw new XWSSecurityException("Error occurred while parsing EncryptedData" + ex);
                        }
                        ek.getPendingReferenceList().remove(payLoadWsuId);
                    }
                }
                if (!ek.getPendingReferenceList().isEmpty()) {
                    throw new XWSSecurityException("Data  Reference under EncryptedKey with ID " + ek.getId() + " is not found");
                } else {
                    pendingElement = null;
                    bufferedHeaders.remove(ek);
                    addSecurityHeader(ek);
                }
            }
        } else if (she instanceof ReferenceListHeader) {
            ReferenceListHeader refList = (ReferenceListHeader) she;
            if (refList.getPendingReferenceList().contains(payLoadWsuId)) {
                try {
                    EncryptedData ed = new EncryptedData(message, context, bodyENVNS);

                    //for policy verification
                    refList.getPolicy().setKeyBinding(ed.getInferredKB());
                    //
                    payLoadWsuId = ed.getId();
                    handlePayLoadED(ed);
                    refList.getPendingReferenceList().remove(payLoadWsuId);
                    cachePayLoadId();
                    payLoadEncId = ed.getId();
                } catch (XMLStreamException ex) {
                    throw new XWSSecurityException("Error occurred while processing EncryptedData", ex);
                }
            }
            if (refList.getPendingReferenceList().size() > 0) {
                findAndReplaceED((ArrayList<String>) refList.getPendingReferenceList(), refList);
            }
            if (refList.getPendingReferenceList().size() > 0) {
                String uri = refList.getPendingReferenceList().get(0);
                throw new XWSSecurityException("Reference with ID " + uri + " was not found in the message");
            }
        } else {
            throw new XWSSecurityException("Need to support this header, please file a bug." + she);
        }
    }

    private void handleSignature(Signature sig) throws XWSSecurityException {
        com.sun.xml.ws.security.opt.crypto.dsig.Reference bodyRef = null;
        com.sun.xml.ws.security.opt.crypto.dsig.Reference payLoadRef = null;
        if (bodyWsuId.length() > 0) {
            bodyRef = sig.removeReferenceWithID("#" + bodyWsuId);
        }
        if (payLoadWsuId.length() > 0) {
            payLoadRef = sig.removeReferenceWithID("#" + payLoadWsuId);
        }
        if (bodyRef != null && payLoadRef != null) {
            throw new XWSSecurityException("Does not support signing of Body and PayLoad together");
        }

        boolean validated = false;
        try {
            validated = sig.validate();
        } catch (XWSSecurityException xe) {
            throw new WebServiceException(xe);
        }
        if (!validated) {
            ArrayList<com.sun.xml.ws.security.opt.crypto.dsig.Reference> refs = sig.getReferences();
            if (refs != null && refs.size() > 0) {
                throw new WebServiceException("Could not find Reference " + refs.get(0).getURI() + " under Signature with ID" + sig.getId());
            } else {
                throw new XWSSecurityException("Verification of Signature with ID  " + sig.getId() + " failed, possible cause : proper canonicalized" +
                        "signedinfo was not produced.");
            }
        } else {
            if (bodyRef != null) {
                message = sig.wrapWithDigester(bodyRef, message, bodyTag, parentNSOnEnvelope, false);
            } else if (payLoadRef != null) {
                message = sig.wrapWithDigester(payLoadRef, message, bodyTag, bodyENVNS, true);
            }
        }
    }

    private void handlePayLoadED(EncryptedData ed) throws XWSSecurityException, XMLStreamException {
        if (pendingElement != null && pendingElement instanceof EncryptedKey) {
            EncryptedKey ek = ((EncryptedKey) pendingElement);
            if (ek.getPendingReferenceList().contains(((SecurityHeaderElement) ed).getId())) {
                //for policy verification
                if (ek.getPolicy() != null) {
                    ek.getPolicy().setKeyBinding(ek.getInferredKB());
                }

                message = ed.getDecryptedData(ek.getKey(ed.getEncryptionAlgorithm()));
                payLoadEncId = payLoadWsuId;
                if (message != null && message.hasNext()) {
                    message.next();
                }
                if (message == null) {
                    message = getEmptyBody();
                }
                cachePayLoadId();
                ek.getPendingReferenceList().remove(((SecurityHeaderElement) ed).getId());
                findAndReplaceED((ArrayList<String>) ek.getPendingReferenceList(), ek);
                if (ek.getPendingReferenceList().isEmpty()) {
                    pendingElement = null;
                } else {
                    String uri = ek.getPendingReferenceList().get(0);
                    throw new XWSSecurityException("Could not find Reference " + uri + " under EncryptedKey with ID" + ek.getId());
                }
            }
        } else {
            message = ed.getDecryptedData();
            if (message != null && message.hasNext()) {
                message.next();
            }
            if (message == null) {
                message = getEmptyBody();
            }
        }
        edAlgos.put(ed.getId(), ed.getEncryptionAlgorithm());
    }

    private void moveToNextElement() throws XMLStreamException {
        message.next();
        while (message.getEventType() != XMLStreamReader.START_ELEMENT) {
            message.next();
        }
    }

    private boolean isTimeStamp() {
        if (MessageConstants.TIMESTAMP_LNAME.equals(message.getLocalName()) && MessageConstants.WSU_NS.equals(message.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    private boolean isBST() {
        if (MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME.equals(message.getLocalName()) && MessageConstants.WSSE_NS.equals(message.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    private boolean isUsernameToken() {
        if (MessageConstants.USERNAME_TOKEN_LNAME.equals(message.getLocalName()) && MessageConstants.WSSE_NS.equals(message.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    private boolean isSignature() {
        if (MessageConstants.SIGNATURE_LNAME.equals(message.getLocalName()) && MessageConstants.DSIG_NS.equals(message.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    private boolean isEncryptedKey() {
        if (MessageConstants.ENCRYPTEDKEY_LNAME.equals(message.getLocalName()) && MessageConstants.XENC_NS.equals(message.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    private boolean isReferenceList() {
        if (MessageConstants.XENC_REFERENCE_LIST_LNAME.equals(message.getLocalName()) && MessageConstants.XENC_NS.equals(message.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    private boolean isEncryptedData() {
        if (MessageConstants.ENCRYPTED_DATA_LNAME.equals(message.getLocalName()) && MessageConstants.XENC_NS.equals(message.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    private boolean isDerivedKey() {
        if (MessageConstants.DERIVEDKEY_TOKEN_LNAME.equals(message.getLocalName()) &&
                (MessageConstants.WSSC_NS.equals(message.getNamespaceURI()) || MessageConstants.WSSC_13NS.equals(message.getNamespaceURI()))) {
            return true;
        }
        return false;
    }

    private boolean isSignatureConfirmation() {
        if (MessageConstants.SIGNATURE_CONFIRMATION_LNAME.equals(message.getLocalName()) && MessageConstants.WSSE11_NS.equals(message.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    private boolean isSCT() {
        if (MessageConstants.SECURITY_CONTEXT_TOKEN_LNAME.equals(message.getLocalName()) &&
                (MessageConstants.WSSC_NS.equals(message.getNamespaceURI()) || MessageConstants.WSSC_13NS.equals(message.getNamespaceURI()))) {
            return true;
        }
        return false;
    }

    private boolean isEncryptedHeader() {
        if (MessageConstants.ENCRYPTED_HEADER_LNAME.equals(message.getLocalName()) && MessageConstants.WSSE11_NS.equals(message.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    private boolean isSTR() {
        if (MessageConstants.WSSE_SECURITY_TOKEN_REFERENCE_LNAME.equals(message.getLocalName()) && MessageConstants.WSSE_NS.equals(message.getNamespaceURI())) {
            return true;
        }
        return false;
    }

    private boolean isSAML() {
        if (MessageConstants.SAML_ASSERTION_LNAME.equals(message.getLocalName())) {
            String uri = message.getNamespaceURI();
            if (MessageConstants.SAML_v2_0_NS.equals(uri) || MessageConstants.SAML_v1_0_NS.equals(uri) || MessageConstants.SAML_v1_1_NS.equals(uri)) {
                return true;
            }
        }
        return false;
    }

    private int getSecurityElementType() {
        if (message.getEventType() == XMLStreamReader.START_ELEMENT) {
            if (isTimeStamp()) {
                return TIMESTAMP_ELEMENT;
            }

            if (isUsernameToken()) {
                return USERNAME_TOKEN_ELEMENT;
            }

            if (isBST()) {
                return BINARYSECURITY_TOKEN_ELEMENT;
            }

            if (isSignature()) {
                return SIGNATURE_ELEMENT;
            }

            if (isEncryptedKey()) {
                return ENCRYPTED_KEY_ELEMENT;
            }

            if (isEncryptedData()) {
                return ENCRYPTED_DATA_ELEMENT;
            }

            if (isEncryptedHeader()) {
                return ENCRYPTED_HEADER_ELEMENT;
            }

            if (isReferenceList()) {
                return REFERENCE_LIST_ELEMENT;
            }

            if (isSignatureConfirmation()) {
                return SIGNATURE_CONFIRMATION_ELEMENT;
            }

            if (isDerivedKey()) {
                return DERIVED_KEY_ELEMENT;
            }

            if (isSCT()) {
                return SECURITY_CONTEXT_TOKEN;
            }

            if (isSAML()) {
                return SAML_ASSERTION_ELEMENT;
            }

            if (isSTR()) {
                return STR_ELEMENT;
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private void findAndReplaceED(ArrayList<String> edList, SecurityHeaderElement ekOrRlh) throws XWSSecurityException {
        EncryptedKey ek = null;
        ReferenceListHeader rlh = null;
        if (ekOrRlh instanceof EncryptedKey) {
            ek = (EncryptedKey) ekOrRlh;
        } else if (ekOrRlh instanceof ReferenceListHeader) {
            rlh = (ReferenceListHeader) ekOrRlh;
        } else {
            // should never happen
            return;
        }
        ArrayList<String> refList = (ArrayList) edList.clone();

        for (int i = 0; i < refList.size(); i++) {
            String id = refList.get(i);
            boolean found = false;

            Iterator<Header> listItr = headers.listIterator();
            while (listItr.hasNext()) {
                GenericSecuredHeader header = (GenericSecuredHeader) listItr.next();
                String localPart = (header != null) ? (header.getLocalPart()) : null;
                boolean isEncHeader = "EncryptedHeader".equals(localPart);
                GenericSecuredHeader processedHeader = null;
                if (isEncHeader) {
                    processedHeader = processEncryptedSOAPHeader(header, ekOrRlh);
                }
                if (header.hasID(id) || id.equals(context.getEdIdforEh())) {
                    if (processedHeader == null) {
                        processedHeader = processEncryptedSOAPHeader(header, ekOrRlh);
                    }
                    edList.remove(id);
                    context.setEdIdforEh(null);
                    int index = headers.indexOf(header);
                    headers.set(index, processedHeader);
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            for (int j = 0; j < processedHeaders.size(); j++) {
                SecurityHeaderElement header = (SecurityHeaderElement) processedHeaders.get(j);
                if (id.equals(header.getId())) {
                    if (header instanceof EncryptedData) {
                        found = true;
                        throw new XWSSecurityException("EncryptedKey or ReferenceList must appear before EncryptedData element with ID" + header.getId());
                    }
                }
            }
            if (found) {
                continue;
            }
            for (int j = 0; j < bufferedHeaders.size(); j++) {
                SecurityHeaderElement header = (SecurityHeaderElement) bufferedHeaders.get(j);
                if (id.equals(header.getId())) {
                    if (header instanceof EncryptedData) {
                        EncryptedData ed = (EncryptedData) header;
                        if (!ed.hasCipherReference()) {
                            XMLStreamReader decryptedData = null;
                            try {
                                if (ek != null) {
                                    if (ek.getPolicy() != null) {
                                        //for policy verification
                                        ek.getPolicy().setKeyBinding(ek.getInferredKB());
                                    }
                                    decryptedData = ed.getDecryptedData(ek.getKey(ed.getEncryptionAlgorithm()));
                                } else if (rlh != null) {
                                    rlh.getPolicy().setKeyBinding(ed.getInferredKB());
                                    decryptedData = ed.getDecryptedData();
                                } else {
                                    throw new XWSSecurityException("Internal Error: Both EncryptedKey and ReferenceList are set to null");
                                }


                                SecurityHeaderProcessor shp = new SecurityHeaderProcessor(context, envshNS, staxIF, creator);
                                if (decryptedData.getEventType() != XMLStreamReader.START_ELEMENT) {
                                    StreamUtil.moveToNextElement(decryptedData);
                                }
                                SecurityHeaderElement she = shp.createHeader(decryptedData);
                                edList.remove(ed.getId());
                                encIds.put(ed.getId(), she.getId());
                                edAlgos.put(ed.getId(), ed.getEncryptionAlgorithm());
                                bufferedHeaders.set(i, she);
                            } catch (XMLStreamException ex) {
                                logger.log(Level.SEVERE,"", ex);
                                throw new XWSSecurityException("Error occurred while decrypting EncryptedData with ID " + ed.getId(), ex);
                            }
                        } else {
                            // handle encrypted attachment here
                            byte[] decryptedMimeData = null;
                            if (ek != null) {
                                decryptedMimeData = ed.getDecryptedMimeData(ek.getKey(ed.getEncryptionAlgorithm()));
                            } else if (rlh != null) {
                                decryptedMimeData = ed.getDecryptedMimeData();
                            } else {
                                throw new XWSSecurityException("Internal Error: Both EncryptedKey and ReferenceList are set to null");
                            }
                            Attachment as = new AttachmentImpl(ed.getAttachmentContentId(), decryptedMimeData, ed.getAttachmentMimeType());
                            securityContext.getDecryptedAttachmentSet().add(as);
                            edList.remove(ed.getId());
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private GenericSecuredHeader processEncryptedSOAPHeader(GenericSecuredHeader header,
            SecurityHeaderElement ekOrRlh) throws XWSSecurityException {
        EncryptedKey ek = null;
        ReferenceListHeader rlh = null;
        if (ekOrRlh instanceof EncryptedKey) {
            ek = (EncryptedKey) ekOrRlh;
        } else if (ekOrRlh instanceof ReferenceListHeader) {
            rlh = (ReferenceListHeader) ekOrRlh;
        }

        try {
            XMLStreamReader reader = header.readHeader();
            if (reader.getEventType() == XMLStreamReader.START_DOCUMENT) {
                reader.next();
            }
            if (reader.getEventType() != XMLStreamReader.START_ELEMENT) {
                StreamUtil.moveToNextElement(reader);
            }
            XMLStreamReader decryptedData = null;
            InputStream decryptedIS = null;
            EncryptedData ed = null;
            EncryptedHeader eh = null;
            boolean encContent = false;
            EncryptedContentHeaderParser encContentparser = null;
            if (MessageConstants.ENCRYPTED_DATA_LNAME.equals(reader.getLocalName()) && MessageConstants.XENC_NS.equals(reader.getNamespaceURI())) {
                ed = new EncryptedData(reader, context, parentNS);
            } else if (MessageConstants.ENCRYPTED_HEADER_LNAME.equals(reader.getLocalName()) && MessageConstants.WSSE11_NS.equals(reader.getNamespaceURI())) {
                eh = new EncryptedHeader(reader, context, parentNS);
                ed = eh.getEncryptedData();
            } else if (context.getEncHeaderContent()) {
                // the content of header is encrypted
                encContent = true;

                encContentparser = new EncryptedContentHeaderParser(reader, parentNS, context);
                ed = encContentparser.getEncryptedData();
            } else {
                throw new XWSSecurityException("Wrong Encrypted SOAP Header");
            }
            if (ed != null) {
                context.setEdIdforEh(ed.getId());
            }

            //for policy verification
            if (!encContent) {
                if (ek != null) {
                    if (ek.getPolicy() != null) {
                        ek.getPolicy().setKeyBinding(ek.getInferredKB());
                    }
                    decryptedData = ed.getDecryptedData(ek.getKey(ed.getEncryptionAlgorithm()));
                } else if (rlh != null) {
                    rlh.getPolicy().setKeyBinding(ed.getInferredKB());
                    decryptedData = ed.getDecryptedData();
                } else {
                    throw new XWSSecurityException("Internal Error: Both EncryptedKey and ReferenceList set to null");
                }
                //

                if (decryptedData.getEventType() == XMLStreamReader.START_DOCUMENT) {
                    decryptedData.next();
                }
                if (decryptedData.getEventType() != XMLStreamReader.START_ELEMENT) {
                    StreamUtil.moveToNextElement(decryptedData);
                }
            } else {
                if (ek != null) {
                    if (ek.getPolicy() != null) {
                        ek.getPolicy().setKeyBinding(ek.getInferredKB());
                    }
                    decryptedIS = ed.getCipherInputStream(ek.getKey(ed.getEncryptionAlgorithm()));
                } else if (rlh != null) {
                    rlh.getPolicy().setKeyBinding(ed.getInferredKB());
                    decryptedIS = ed.getCipherInputStream();
                }
            }

            GenericSecuredHeader gsh = null;
            if (!encContent) {
                Map<String, String> headerBlockNamespaces = parentNS;
                // Collect namespaces on SOAP header block
                if (decryptedData.getNamespaceCount() > 0) {
                    headerBlockNamespaces = new HashMap<String, String>(parentNS);
                    for (int k = 0; k < decryptedData.getNamespaceCount(); k++) {
                        headerBlockNamespaces.put(decryptedData.getNamespacePrefix(k), decryptedData.getNamespaceURI(k));
                    }
                }
                // Mark
                //XMLStreamBuffer mark = new XMLStreamBufferMark(headerBlockNamespaces, creator);
                gsh = new GenericSecuredHeader(decryptedData, soapVersion, creator, (HashMap) headerBlockNamespaces, staxIF, context.getEncHeaderContent());
            } else {

                XMLStreamReader decryptedHeader = encContentparser.getDecryptedElement(decryptedIS);

                if (decryptedHeader.getEventType() == XMLStreamReader.START_DOCUMENT) {
                    decryptedHeader.next();
                }
                if (decryptedHeader.getEventType() != XMLStreamReader.START_ELEMENT) {
                    StreamUtil.moveToNextElement(decryptedHeader);
                }
                Map<String, String> headerBlockNamespaces = parentNS;
                // Collect namespaces on SOAP header block
                if (decryptedHeader.getNamespaceCount() > 0) {
                    headerBlockNamespaces = new HashMap<String, String>(parentNS);
                    for (int k = 0; k < decryptedHeader.getNamespaceCount(); k++) {
                        String prefix = decryptedHeader.getNamespacePrefix(k);
                        if (prefix == null) {
                            prefix = "";
                        }
                        headerBlockNamespaces.put(prefix, decryptedHeader.getNamespaceURI(k));
                    }
                }
                gsh = new GenericSecuredHeader(decryptedHeader, soapVersion, creator, (HashMap) headerBlockNamespaces, staxIF, context.getEncHeaderContent());
            }
            QName gshQName = new QName(gsh.getNamespaceURI(), gsh.getLocalPart());
            if (eh != null) {
                encQNames.put(eh.getId(), gshQName);
                edAlgos.put(eh.getId(), ed.getEncryptionAlgorithm());
            } else {
                encQNames.put(ed.getId(), gshQName);
                edAlgos.put(ed.getId(), ed.getEncryptionAlgorithm());
            }

            return gsh;
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, "", ex);
            throw new XWSSecurityException("Error occurred while decrypting EncryptedData ", ex);
        } catch (XMLStreamBufferException ex) {
            logger.log(Level.SEVERE, "", ex);
            throw new XWSSecurityException("Error occurred while decrypting EncryptedData", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void addSecurityHeader(SecurityHeaderElement sh) {
        if (pendingElement == null || (sh instanceof TokenValidator)) {
            processedHeaders.add(sh);
        } else {
            bufferedHeaders.add(sh);
        }
    }

    private void cachePayLoadId() {
        if (message != null && message.getEventType() == XMLStreamReader.START_ELEMENT) {
            payLoadWsuId = StreamUtil.getWsuId(message);
            if (payLoadWsuId == null) {
                payLoadWsuId = StreamUtil.getId(message);
            }
            if (payLoadWsuId == null) {
                payLoadWsuId = "";
            }
        }
    }

    private boolean areHeadersSecured(SignaturePolicy sp) {
        ArrayList list = ((SignaturePolicy.FeatureBinding) sp.getFeatureBinding()).getTargetBindings();
        List headerList = headers;
        for (int hl = 0; hl < headerList.size(); hl++) {
            Header hdr = (Header) headerList.get(hl);
            String localName = hdr.getLocalPart();
            String uri = hdr.getNamespaceURI();
            boolean found = false;
            if (MessageConstants.ADDRESSING_W3C_NAMESPACE.equals(uri) || MessageConstants.ADDRESSING_MEMBER_SUBMISSION_NAMESPACE.equals(uri)) {
                for (int i = 0; i < list.size(); i++) {
                    SignatureTarget st = (SignatureTarget) list.get(i);
                    QName value = st.getQName();
                    if (value.getLocalPart().equals(localName) && value.getNamespaceURI().equals(uri)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return found;
                }
            }
        }
        return true;
    }

    private boolean isEmpty(SecurityPolicy msgPolicy) {
        if (msgPolicy == null) {
            return true;
        }
        //TODO: it will be best if SecurityPolicy interface had an isEmpty
        //will make that change after initial checkin for policy-alternatives
        if (msgPolicy instanceof MessagePolicy) {
            return (((MessagePolicy)msgPolicy).isEmpty());
        } else if (msgPolicy instanceof PolicyAlternatives) {
            PolicyAlternatives pol = (PolicyAlternatives)msgPolicy;
            return pol.isEmpty();
        }
        return false;
    }
}
