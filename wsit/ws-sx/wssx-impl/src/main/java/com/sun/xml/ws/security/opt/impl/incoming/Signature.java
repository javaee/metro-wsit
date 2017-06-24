/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.security.opt.impl.incoming;

import org.apache.xml.security.exceptions.Base64DecodingException;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.ws.encoding.TagInfoset;
import com.sun.xml.ws.security.opt.api.NamespaceContextInfo;
import com.sun.xml.ws.security.opt.api.PolicyBuilder;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.crypto.dsig.SignatureValue;
import com.sun.xml.ws.security.opt.crypto.dsig.SignedInfo;
import com.sun.xml.ws.security.opt.crypto.dsig.internal.DigesterOutputStream;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBValidateContext;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.incoming.processor.KeyInfoProcessor;
import com.sun.xml.ws.security.opt.impl.incoming.processor.SignedInfoProcessor;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.ws.security.opt.impl.util.XMLStreamReaderFactory;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.crypto.KeySelector.Purpose;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignatureException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.XMLStreamReaderEx;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;

import java.security.MessageDigest;

import com.sun.xml.ws.security.opt.impl.incoming.processor.StreamingPayLoadDigester;
import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import java.security.NoSuchAlgorithmException;
import javax.xml.crypto.dsig.Transform;
import com.sun.xml.ws.security.opt.impl.dsig.ExcC14NParameterSpec;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import java.util.logging.Logger;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
/**
 * <element name="Signature" type="ds:SignatureType"/>
 * <complexType name="SignatureType">
 * <sequence>
 * <element ref="ds:SignedInfo"/>
 * <element ref="ds:SignatureValue"/>
 * <element ref="ds:KeyInfo" minOccurs="0"/>
 * <element ref="ds:Object" minOccurs="0" maxOccurs="unbounded"/>
 * </sequence>
 * <attribute name="Id" type="ID" use="optional"/>
 * </complexType>
 *
 */


/**
 *
 * @author K.Venugopal@sun.com
 */
public class Signature implements SecurityHeaderElement,NamespaceContextInfo, SecurityElementWriter, PolicyBuilder{
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    
    public static final int SIGNEDINFO_EVENT = 1;
    public static final int SIGNATUREVALUE_EVENT = 2;
    public static final int KEYINFO_EVENT = 3;
    public static final int OBJECT_EVENT = 4;
    
    public static final String SIGNED_INFO = "SignedInfo";
    public static final String SIGNATURE_VALUE = "SignatureValue";
    public static final String KEYINFO = "KeyInfo";
    public static final String OBJECT = "Object";
    
    private SignaturePolicy signPolicy = null;
    
    private HashMap<String,String> currentParentNS = new HashMap<String,String>();
    private JAXBFilterProcessingContext context;
    private StreamReaderBufferCreator creator = null;
    private SecurityContext securityContext = null;
    private String id ="";
    private SignedInfoProcessor sip = null;
    private XMLStreamWriter canonWriter = null;
    private com.sun.xml.ws.security.opt.crypto.dsig.Signature sig = null;
    private JAXBValidateContext jvc = new JAXBValidateContext();
    private SignedInfo si = null;
    private boolean validationStatus = false;
    private MutableXMLStreamBuffer buffer = null;
    private boolean cacheSignature = false;
    private boolean storeSigConfirmValue = true;
    
    //private JAXBSignatureFactory signatureFactory = null;
    
    /** Creates a new instance of SignatureProcessor */
    
    public Signature(JAXBFilterProcessingContext jpc,Map<String,String> namespaceList,StreamReaderBufferCreator sbc) {
        this.currentParentNS.putAll(namespaceList);
        this.creator = sbc;
        this.securityContext = jpc.getSecurityContext();
        this.context = jpc;
        cacheSignature = true;
        signPolicy = new SignaturePolicy();
        signPolicy.setFeatureBinding(new SignaturePolicy.FeatureBinding());
        //signatureFactory = JAXBSignatureFactory.newInstance();
    }
    
    public Signature(JAXBFilterProcessingContext jpc,Map<String,String> namespaceList,StreamReaderBufferCreator sbc,boolean cacheSig) {
        this.currentParentNS.putAll(namespaceList);
        this.creator = sbc;
        this.securityContext = jpc.getSecurityContext();
        this.context = jpc;
        cacheSignature = cacheSig;
        signPolicy = new SignaturePolicy();
        signPolicy.setFeatureBinding(new SignaturePolicy.FeatureBinding());
    }
    @SuppressWarnings("unchecked")
    public void process(XMLStreamReader signature) throws XWSSecurityException{
        try {
            XMLStreamReader reader = null;
            //by default buffer now , enable buffering using policy
            buffer = new MutableXMLStreamBuffer();//resuse pick from pool
            buffer.createFromXMLStreamReader(signature);
            reader =  buffer.readAsXMLStreamReader();
            reader.next();
            byte [] signatureValue = null;
            TagInfoset signatureRoot = new TagInfoset(reader);
            for(int i=0; i< reader.getNamespaceCount();i++){
                String prefix = reader.getNamespacePrefix(i);
                String uri = reader.getNamespaceURI(i);
                if(prefix == null){
                    prefix = "";
                }
                currentParentNS.put(prefix,uri);
            }
            this.context.setCurrentBuffer(buffer);
            Key key = null;
            id = reader.getAttributeValue(null,"Id");
            if(id == null || id.length() == 0){
                id = "pvid"+context.generateID();// need an Id for policy verification.
                if(logger.isLoggable(Level.FINEST)){
                    logger.log(Level.FINEST, LogStringsMessages.WSS_1755_MISSINGID_INCOMING_SIGNATURE(id));
                }
            }
            // policy creation
            signPolicy.setUUID(id);
            
            if(StreamUtil.moveToNextElement(reader)){
                int refElement = getEventType(reader);
                while(reader.getEventType() != reader.END_DOCUMENT){
                    switch(refElement){
                    case SIGNEDINFO_EVENT :{
                        sip = new SignedInfoProcessor(signatureRoot,currentParentNS,reader,context, signPolicy,buffer);
                        si = (SignedInfo) sip.process();
                        canonWriter = sip.getCanonicalizer();
                        break;
                    }
                    case SIGNATUREVALUE_EVENT :{
                        if(canonWriter == null){
                            logger.log(Level.SEVERE, LogStringsMessages.WSS_1707_ERROR_PROCESSING_SIGNEDINFO(id));
                            throw new XWSSecurityException("Elements under Signature are not as per defined schema"+
                                    " or error must have occurred while processing SignedInfo for Signature with ID"+id);
                        }
                        StreamUtil.writeCurrentEvent(reader,canonWriter);
                        
                        if(reader instanceof XMLStreamReaderEx){
                            reader.next();
                            StringBuffer sb = null;
                            while(reader.getEventType() == reader.CHARACTERS && reader.getEventType() != reader.END_ELEMENT){
                                CharSequence charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
                                if(charSeq instanceof Base64Data){
                                    Base64Data bd = (Base64Data) ((XMLStreamReaderEx)reader).getPCDATA();
                                    signatureValue = bd.getExact();
                                    canonWriter.writeCharacters(Base64.encode(signatureValue));
                                } else{
                                    if(sb == null){
                                        sb = new StringBuffer();
                                    }
                                    for(int i=0;i<charSeq.length();i++){
                                        sb.append(charSeq.charAt(i));
                                    }
                                }
                                reader.next();
                            }
                            if(sb != null){
                                String tmp  = sb.toString();
                                canonWriter.writeCharacters(tmp);
                                try{
                                    signatureValue = Base64.decode(tmp);
                                }catch(Base64DecodingException dec){
                                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1708_BASE_64_DECODING_ERROR(id));
                                    throw new XWSSecurityException("Error occurred while decoding signatureValue for Signature with ID"+id);
                                }
                            }else{
                                reader.next();
                            }
                        }else{
                            String tmp = StreamUtil.getCV(reader);
                                    //reader.getElementText();
                            canonWriter.writeCharacters(tmp);
                            try{
                                signatureValue = Base64.decode(tmp);
                            }catch(Base64DecodingException dec){
                                logger.log(Level.SEVERE, LogStringsMessages.WSS_1708_BASE_64_DECODING_ERROR(id), dec);
                                throw new XWSSecurityException("Error occurred while decoding signatureValue for Signature with ID"+id);
                            }
                        }
                        //For SignatureConfirmation
                        List scList = (ArrayList)context.getExtraneousProperty("receivedSignValues");
                        if(scList != null && storeSigConfirmValue){
                            scList.add(signatureValue);
                        }
                        //End SignatureConfirmation specific code
                        break;
                    }
                    case KEYINFO_EVENT :{
                        if(canonWriter == null){
                            logger.log(Level.SEVERE, LogStringsMessages.WSS_1707_ERROR_PROCESSING_SIGNEDINFO(id));
                            throw new XWSSecurityException("Elements under Signature are not as per defined schema"+
                                    " or error must have occurred while processing SignedInfo for Signature with ID"+id);
                        }
                        // StreamUtil.writeCurrentEvent(reader,canonWriter);
                        securityContext.setInferredKB(null);
                        KeyInfoProcessor kip = new KeyInfoProcessor(context,Purpose.VERIFY);
                        key = kip.getKey(reader);
                        if (key instanceof PublicKey) {
                            X509Certificate cert = null;
                            try {
                                cert = context.getSecurityEnvironment().getCertificate(context.getExtraneousProperties(), (PublicKey) key, false);
                            } catch (XWSSecurityException ex) {
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.log(Level.FINE, "", ex);
                                }
                            }
                            if (cert != null && !context.isSamlSignatureKey()) {
                                context.getSecurityEnvironment().updateOtherPartySubject(DefaultSecurityEnvironmentImpl.getSubject(context), cert);
                            }
                        }
                        MLSPolicy inferredKB = securityContext.getInferredKB();
                        signPolicy.setKeyBinding(inferredKB);
                        securityContext.setInferredKB(null);
                        break;
                    }
                    default :{
                        logger.log(Level.SEVERE, LogStringsMessages.WSS_1709_UNRECOGNIZED_SIGNATURE_ELEMENT(reader.getName()));
                        throw new XWSSecurityException("Element name "+reader.getName()+" is not recognized under Signature");
                    }
                    }
                    
                    if(StreamUtil._break(reader,"Signature", MessageConstants.DSIG_NS)){
                        reader.next();
                        break;
                    }
                    
                    if(!StreamUtil.isStartElement(reader) && StreamUtil.moveToNextStartOREndElement(reader) && StreamUtil._break(reader,
                            "Signature", MessageConstants.DSIG_NS)){
                        reader.next();
                        break;
                    }else{
                        if(reader.getEventType() != XMLStreamReader.START_ELEMENT){
                            StreamUtil.moveToNextStartOREndElement(reader);
                            boolean isBreak = false;
                            while(reader.getEventType() == XMLStreamReader.END_ELEMENT){
                                if(StreamUtil._break(reader,"Signature", MessageConstants.XENC_NS)){
                                    isBreak = true;
                                    break;
                                }
                                StreamUtil.moveToNextStartOREndElement(reader);
                            }
                            if(isBreak)
                                break;
                            if(reader.getEventType() == XMLStreamReader.END_DOCUMENT ){
                                break;
                            }
                        }
                    }
                    refElement = getEventType(reader);
                }
            }
            
            
            sig = new com.sun.xml.ws.security.opt.crypto.dsig.Signature();
            SignatureValue sv = new SignatureValue();
            sv.setValue(signatureValue);
            sig.setSignatureValue(sv);
            
            jvc.setURIDereferencer(new URIResolver(context));
            sig.setSignedInfo(si);
            sig.setVerificationKey(key);
            if(sip.getReferenceList().size() == 0){
                if(!sig.validate(jvc)){
                    validationStatus = true;
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1710_SIGNATURE_VERFICATION_FAILED(id));
                    throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK, LogStringsMessages.WSS_1710_SIGNATURE_VERFICATION_FAILED(id), null);
                }else{
                    validationStatus = true;
                }
            }
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1711_ERROR_VERIFYING_SIGNATURE(),ex);
            throw new XWSSecurityException(ex);
        } catch(XMLSignatureException xse){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1711_ERROR_VERIFYING_SIGNATURE(),xse);
            throw new XWSSecurityException(xse);
        }
    }
    
    public void process(XMLStreamReader signature, boolean storeSigConfirmValue) throws XWSSecurityException{
        this.storeSigConfirmValue = storeSigConfirmValue;
        process(signature);
        this.storeSigConfirmValue = true;
    }
    
    public boolean validate() throws XWSSecurityException{
        if(isReady()){
            try{
                boolean status = sig.validate(jvc);
                validationStatus = true;
                return status;
            }catch(XMLSignatureException ex){
                validationStatus = true;
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1713_SIGNATURE_VERIFICATION_EXCEPTION(ex.getMessage()), ex);
                throw new XWSSecurityException(ex);
            }
        }
        return false;
    }
    
    
    
    public com.sun.xml.ws.security.opt.crypto.dsig.Reference  removeReferenceWithID(String id){
        ArrayList<com.sun.xml.ws.security.opt.crypto.dsig.Reference> refList = sip.getReferenceList();
        com.sun.xml.ws.security.opt.crypto.dsig.Reference ref = null;
        for(int i=0;i<refList.size();i++){
            if(refList.get(i).getURI().equals(id)){
                ref =sip.getReferenceList().remove(i);
                break;
            }
        }
        return ref;
    }
    
    public ArrayList<com.sun.xml.ws.security.opt.crypto.dsig.Reference> getReferences(){
        return sip.getReferenceList();
    }
    
    public boolean isValidated(){
        return validationStatus;
    }
    @SuppressWarnings("unchecked")
    public boolean isReady() throws XWSSecurityException{
        if(sip.getReferenceList().size() == 0){
            return true;
        }else{
            ArrayList<com.sun.xml.ws.security.opt.crypto.dsig.Reference> refList =  (ArrayList) sip.getReferenceList().clone();
            for(int i=0;i<refList.size();i++){
                com.sun.xml.ws.security.opt.crypto.dsig.Reference ref = (com.sun.xml.ws.security.opt.crypto.dsig.Reference) refList.get(i);
                if(sip.processReference(ref)){
                    sip.getReferenceList().remove(ref);
                }
            }
        }
        if(sip.getReferenceList().size() == 0){
            return true;
        }
        return false;
    }
    
    public boolean verifyReferences(){
        throw new UnsupportedOperationException();
    }
    
    public boolean verifySignatureValue(){
        throw new UnsupportedOperationException();
    }
    
    private int getEventType(XMLStreamReader reader) {
        if(reader.getEventType()== XMLStreamReader.START_ELEMENT){
            if(reader.getLocalName() == SIGNED_INFO ){
                return SIGNEDINFO_EVENT;
            }
            if(reader.getLocalName() == SIGNATURE_VALUE){
                return SIGNATUREVALUE_EVENT;
            }
            if(reader.getLocalName() == KEYINFO){
                return KEYINFO_EVENT;
            }
            if(reader.getLocalName() == OBJECT){
                return OBJECT_EVENT;
            }
        }
        return -1;
    }
    
    private boolean _break(XMLStreamReader reader) throws XMLStreamException{
        if(reader.getEventType() == XMLStreamReader.END_ELEMENT){
            if(reader.getLocalName() == "Signature" && reader.getNamespaceURI() == MessageConstants.DSIG_NS){
                reader.next();
                return true;
            }
        }
        return false;
    }
    
    public boolean refersToSecHdrWithId(String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getNamespaceURI() {
        return MessageConstants.DSIG_NS;
    }
    
    public String getLocalPart() {
        return MessageConstants.SIGNATURE_LNAME;
    }
    
    public XMLStreamReader readHeader() throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(OutputStream os) {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
        if(buffer != null){
            buffer.writeToXMLStreamWriter(streamWriter);
        }else{
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1712_UNBUFFERED_SIGNATURE_ERROR());
            throw new XMLStreamException("Signature is not buffered , message not as per configured policy");
        }
    }
    
    
    public XMLStreamReader wrapWithDigester(Reference ref,XMLStreamReader message, String bodyPrologue, String bodyEpilogue, TagInfoset bodyTag,HashMap<String,String>parentNS,boolean payLoad)throws XWSSecurityException{
        
        MessageDigest  md = null;
        try {
            String algo = StreamUtil.convertDigestAlgorithm(ref.getDigestMethod().getAlgorithm());
            
            if(logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE, "Digest Algorithm is "+  ref.getDigestMethod().getAlgorithm());
                logger.log(Level.FINE, "Mapped Digest Algorithm is "+ algo);
            }
            md = MessageDigest.getInstance(algo);
            
        } catch (NoSuchAlgorithmException nsae) {
            throw new XWSSecurityException(nsae);
        }
        
        DigesterOutputStream dos;
        dos = new DigesterOutputStream(md);
        // OutputStream os = new UnsyncBufferedOutputStream(dos);
        StAXEXC14nCanonicalizerImpl canonicalizer = new StAXEXC14nCanonicalizerImpl();
        canonicalizer.setBodyPrologue(bodyPrologue);
        canonicalizer.setBodyEpilogue(bodyEpilogue);
        //TODO:share canonicalizers .
        canonicalizer.setStream(dos);
        if(logger.isLoggable(Level.FINEST)){
            canonicalizer.setStream(new ByteArrayOutputStream());
        }
        List trList = ref.getTransforms();
        if(trList.size() >1){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1714_UNSUPPORTED_TRANSFORM_ERROR());
            throw new XWSSecurityException("Only EXC14n Transform is supported");
        }
        Transform tr = (Transform) trList.get(0);
        
        ExcC14NParameterSpec spec = (ExcC14NParameterSpec)tr.getParameterSpec();
        if(spec != null){
            canonicalizer.setInclusivePrefixList(spec.getPrefixList());
        }
        if(parentNS != null && parentNS.size() >0){
            Iterator<Map.Entry<String, String>> itr = parentNS.entrySet().iterator();
            
            while(itr.hasNext()){
                Map.Entry<String, String> entry = itr.next();
                String prefix = entry.getKey();
                try {
                    String uri = entry.getValue();
                    canonicalizer.writeNamespace(prefix,uri);
                } catch (XMLStreamException ex) {
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1715_ERROR_CANONICALIZING_BODY(), ex);
                }
            }
        }
        try {
            if(!payLoad){
                bodyTag.writeStart(canonicalizer);

                // the space characters between soap:Body and payload element must be preserved for payload signature!
                canonicalizer.setBodyPrologueTime(true);
            }
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1715_ERROR_CANONICALIZING_BODY(), ex);
            throw new XWSSecurityException("Error occurred while canonicalizing BodyTag"+ex);
        }
        StreamingPayLoadDigester digester = new StreamingPayLoadDigester(ref,message,canonicalizer,payLoad);
        return XMLStreamReaderFactory.createFilteredXMLStreamReader(message,digester);
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public HashMap<String, String> getInscopeNSContext() {
        return currentParentNS;
    }
    
    public WSSPolicy getPolicy() {
        return signPolicy;
    }
}
