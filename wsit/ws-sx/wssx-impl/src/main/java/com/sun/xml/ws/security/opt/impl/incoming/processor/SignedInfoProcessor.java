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

package com.sun.xml.ws.security.opt.impl.incoming.processor;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.security.core.dsig.ReferenceType;
import com.sun.xml.security.core.dsig.TransformsType;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.encoding.TagInfoset;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.crypto.dsig.DigestMethod;
import com.sun.xml.ws.security.opt.crypto.dsig.Reference;
import com.sun.xml.ws.security.opt.crypto.dsig.SignatureMethod;
import com.sun.xml.ws.security.opt.crypto.dsig.SignedInfo;
import com.sun.xml.ws.security.opt.crypto.dsig.Transform;
import com.sun.xml.ws.security.opt.crypto.dsig.Transforms;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBSignatureFactory;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBStructure;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBValidateContext;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.incoming.GenericSecuredHeader;
import com.sun.xml.ws.security.opt.impl.incoming.SecurityContext;
import com.sun.xml.ws.security.opt.impl.incoming.URIResolver;
import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.ws.security.secext10.TransformationParametersType;
import com.sun.xml.wss.BasicSecurityProfile;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.misc.UnsyncByteArrayOutputStream;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.Target;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.bind.JAXBElement;
import javax.xml.crypto.Data;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.XMLSignatureException;
import com.sun.xml.ws.security.opt.impl.dsig.ExcC14NParameterSpec;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.XMLStreamReaderEx;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;

/**
 *  <element name="SignedInfo" type="ds:SignedInfoType"/>
 *  <complexType name="SignedInfoType">
 *      <sequence>
 *        <element ref="ds:CanonicalizationMethod"/>
 *        <element ref="ds:SignatureMethod"/>
 *        <element ref="ds:Reference" maxOccurs="unbounded"/>
 *      </sequence>
 *      <attribute name="Id" type="ID" use="optional"/>
 *  </complexType>
 */

/**
 *
 * @author K.Venugopal@sun.com
 */

public class SignedInfoProcessor {
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    
    public static final int CANONICALIZATION_METHOD_EVENT = 1;
    public static final int SIGNATURE_METHOD_EVENT = 2;
    public static final int REFERENCE_EVENT = 3;
    public static final int DIGEST_METHOD_EVENT = 4;
    public static final int DIGEST_VALUE_EVENT = 5;
    public static final int TRANSFORM_EVENT = 6;
    public static final int TRANSFORMS_EVENT = 7;
    
    StAXEXC14nCanonicalizerImpl exc14nFinal = null;
    public static final String CANONICALIZATION_METHOD = "CanonicalizationMethod";
    public static final String SIGNATURE_METHOD = "SignatureMethod";
    public static final String REFERENCE = "Reference";
    public static final String INCLUSIVENAMESPACES = "InclusiveNamespaces";
    public static final String EXC14N_NS = "http://www.w3.org/2001/10/xml-exc-c14n#";
    public static final String TRANSFORMS = "Transforms";
    public static final String TRANSFORM = "Transform";
    public static final String DIGEST_METHOD = "DigestMethod";
    public static final String DIGEST_VALUE = "DigestValue";
    private String canonAlgo = "";
    // future use
    private TagInfoset signatureRoot = null;
    //private XMLStreamWriter canonWriter = null;
    private String signatureMethod = "";
    private HashMap<String,String> currentNSDecls = new HashMap<String,String>();
    private UnsyncByteArrayOutputStream canonInfo = new UnsyncByteArrayOutputStream();
    private XMLStreamReader reader = null;
    private SecurityContext securityContext = null;
    private ArrayList<Reference> refList = null;
    private URIResolver resolver = null;
    private SignaturePolicy.FeatureBinding fb = null;
    private JAXBFilterProcessingContext pc = null;
    private JAXBSignatureFactory signatureFactory = null;
    MutableXMLStreamBuffer siBuffer = null;
    /** Creates a new instance of SignedInfoProcessor */
    public SignedInfoProcessor(TagInfoset signature,HashMap<String,String> parentNSDecls,
            XMLStreamReader reader,JAXBFilterProcessingContext pc, SignaturePolicy signPolicy,MutableXMLStreamBuffer buffer) {
        this.signatureRoot = signature;
        this.siBuffer = buffer;
        currentNSDecls.putAll(parentNSDecls);
        this.reader = reader;
        this.pc = pc;
        this.securityContext = pc.getSecurityContext();
        this.resolver = new URIResolver(pc);
        this.signatureFactory = JAXBSignatureFactory.newInstance();
        fb = (SignaturePolicy.FeatureBinding) signPolicy.getFeatureBinding();
    }
    
    public XMLStreamWriter getCanonicalizer(){
        return exc14nFinal;
    }
    /**
     * processes different types the SignedInfo element of an XMLSignature
     * @return  SignedInfo
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public SignedInfo process() throws XWSSecurityException{
        try {            
            for(int i=0; i< reader.getNamespaceCount();i++){
                currentNSDecls.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
            }
            boolean referencesFound = false;
            if(StreamUtil.moveToNextElement(reader)){
                int refElement = getEventType(reader);
                while(reader.getEventType() != reader.END_DOCUMENT){
                    switch(refElement){
                    case CANONICALIZATION_METHOD_EVENT :{
                        readCanonicalizationMethod(reader);
                        fb.setCanonicalizationAlgorithm(canonAlgo);
                        break;
                    }
                    case SIGNATURE_METHOD_EVENT :{                       
                        signatureMethod = reader.getAttributeValue(null,"Algorithm");
                        break;
                    }
                    case REFERENCE_EVENT :{
                        referencesFound = true;
                        processReferences(reader);
                        break;
                    }
                    default :{
                        //no-op
                    }
                    }
                    if(StreamUtil._break(reader,"SignedInfo", MessageConstants.DSIG_NS)){                        
                        if(reader.hasNext())
                            reader.next();
                        break;
                    }else{
                        if(reader.getEventType() == XMLStreamReader.END_DOCUMENT ){
                            break;
                        }
                        reader.next();
                    }
                    if(StreamUtil._break(reader,"SignedInfo", MessageConstants.DSIG_NS)){                        
                        if(reader.hasNext())
                            reader.next();
                        break;
                    }
                    refElement = getEventType(reader);
                }
            }
            // One or more reference element must be present in Signature 
            if(!referencesFound){
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1725_REFERENCE_ELEMENT_NOTFOUND());
                throw new XWSSecurityException(LogStringsMessages.WSS_1725_REFERENCE_ELEMENT_NOTFOUND());
            }
            SignedInfo si = new SignedInfo();
            SignatureMethod sm =new SignatureMethod();
            sm.setAlgorithm(signatureMethod);
            si.setSignatureMethod(sm);
            si.setReference(getReferenceList());
            XMLStreamReader siReader = siBuffer.readAsXMLStreamReader();
            while(siReader.hasNext() ){
                if(siReader.getEventType() == siReader.START_ELEMENT){
                    if(siReader.getLocalName() == "SignedInfo".intern()){
                        break;
                    }                   
                }
                 siReader.next();
            }
            int counter =1;
            while(siReader.hasNext()){  
                StreamUtil.writeCurrentEvent(siReader,exc14nFinal);
                if(counter == 0){
                    break;
                }
                siReader.next();
                if(siReader.getEventType() == siReader.END_ELEMENT){
                    counter --;                    
                }else if(siReader.getEventType() == siReader.START_ELEMENT){
                    counter++;
                }                
            }
            si.setCanonicalizedSI(canonInfo.toByteArray());
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST, "Canonicalized Signed Info:" + new String(canonInfo.toByteArray()));
            }
            return si;
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1711_ERROR_VERIFYING_SIGNATURE(),ex);
            throw new XWSSecurityException(LogStringsMessages.WSS_1711_ERROR_VERIFYING_SIGNATURE() ,ex);
        }
        //return null;
    }
    /**
     *
     * @param reader  XMLStreamReader
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public void readCanonicalizationMethod(XMLStreamReader reader) throws XWSSecurityException{
        try{
            canonAlgo = reader.getAttributeValue(null,"Algorithm");
            if(canonAlgo != null && canonAlgo.length() ==0){
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1718_MISSING_CANON_ALGORITHM());
                throw new XWSSecurityException(LogStringsMessages.WSS_1718_MISSING_CANON_ALGORITHM());
            }            
            String [] prefixList = null;
            if(reader.hasNext()){
                reader.next();
                if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
                    if(reader.getLocalName() == INCLUSIVENAMESPACES && reader.getNamespaceURI() ==EXC14N_NS ){
                        String pl = reader.getAttributeValue(null,"PrefixList");
                        if(pl != null && pl.length() >0){
                            prefixList = pl.split(" ");
                        }
                    }
                }
            }
            
            if(MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS.equals(canonAlgo)){
                exc14nFinal = new StAXEXC14nCanonicalizerImpl();               
                if(prefixList != null && prefixList.length >0){
                    ArrayList<String> al = new ArrayList<String>(prefixList.length);
                    for(int i=0;i<prefixList.length ;i++){
                        al.add(prefixList[i]);
                    }
                    exc14nFinal.setInclusivePrefixList(al);
                }                
                Iterator<String> itr = currentNSDecls.keySet().iterator();
                exc14nFinal.setStream(canonInfo);
                
                while(itr.hasNext()){
                    String prefix = itr.next();
                    String uri = currentNSDecls.get(prefix);
                    exc14nFinal.writeNamespace(prefix,uri);
                }                             
            }
        }catch(XMLStreamException xse){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1711_ERROR_VERIFYING_SIGNATURE(),xse);
            throw new XWSSecurityException(LogStringsMessages.WSS_1711_ERROR_VERIFYING_SIGNATURE() ,xse);
        }
    }
    /**
     * processes references
     * @param reader XMLStreamReader
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    @SuppressWarnings("unchecked")
    private void processReferences(XMLStreamReader reader)throws XWSSecurityException{
        try{
            String dm = "";
            final String uri=reader.getAttributeValue(null,"URI");            
            String digestValue ="";
            Base64Data bd = null;
            ArrayList tList = null;
            while(reader.hasNext() && !StreamUtil._break(reader,"Reference",MessageConstants.DSIG_NS)){
                reader.next();
                int referenceEvent = getReferenceEventType(reader);
                switch(referenceEvent){
                case TRANSFORMS_EVENT :{
                    tList = processTransforms(reader,uri);
                    break;
                }
                case DIGEST_METHOD_EVENT :{                    
                    dm = reader.getAttributeValue(null,"Algorithm");
                    break;
                }
                case DIGEST_VALUE_EVENT :{                    
                    if(reader instanceof XMLStreamReaderEx){
                        reader.next();
                        CharSequence charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
                        String dv = null;
                        if(charSeq instanceof Base64Data){
                            bd = (Base64Data)charSeq;
                            dv = Base64.encode(bd.getExact());
                        } else{
                            dv = StreamUtil.getCV(reader);   
                        }
                        digestValue = dv;                        
                        //reader.next();
                    }else{
                        digestValue = StreamUtil.getCV(reader);                        
                    }
                    break;
                }
                default :{
                    //no - op
                }
                }
            }
            
            ReferenceType rt = new Reference();
            DigestMethod digestMethod = new DigestMethod();
            digestMethod.setAlgorithm(dm);
            rt.setDigestMethod(digestMethod);
            if(bd != null){
                rt.setDigestValue(bd.getExact());
            }else{
                try{
                    rt.setDigestValue(Base64.decode(digestValue));
                }catch(Base64DecodingException dec){
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1719_ERROR_DIGESTVAL_REFERENCE(uri),dec);
                    throw new XWSSecurityException(LogStringsMessages.WSS_1719_ERROR_DIGESTVAL_REFERENCE(uri));
                }
            }
            
            rt.setURI(uri);
            TransformsType transforms= new Transforms();
            transforms.setTransform(tList);
            rt.setTransforms((Transforms) transforms);
            
            // policy creation
            Target target = new Target(Target.TARGET_TYPE_VALUE_URI, uri);
            SignatureTarget signTarget = new SignatureTarget(target);
            signTarget.setDigestAlgorithm(dm);
            for(int i = 0; tList != null &&  i < tList.size(); i++){
                Transform tr = (Transform) tList.get(i);
                SignatureTarget.Transform sTr = new SignatureTarget.Transform(tr.getAlgorithm());
                signTarget.addTransform(sTr);
            }
            
            fb.addTargetBinding(signTarget);
            
            if(!processReference((Reference) rt)){
                ArrayList<Reference> refCache = getReferenceList();
                refCache.add((Reference)rt);
            }            
        }catch(XMLStreamException xe){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1711_ERROR_VERIFYING_SIGNATURE(),xe);
            throw new XWSSecurityException(LogStringsMessages.WSS_1711_ERROR_VERIFYING_SIGNATURE() ,xe);
        }
    }
    /**
     * processes the given reference
     * @param reference Reference
     * @return boolean
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public boolean processReference(Reference reference)throws XWSSecurityException{
        final String uri = reference.getURI();
        URIReference ref = new URIReference() {
            public String getType() {
                return "";
            }
            public String getURI() {
                return uri;
            }
        };
        Data data = null;
        try{
            data = resolver.dereference(ref,null);
        }catch(URIReferenceException ure){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1720_ERROR_URI_DEREF(uri),ure);
            throw new XWSSecurityException(LogStringsMessages.WSS_1720_ERROR_URI_DEREF(uri), ure);
        }
        if(data != null){
            JAXBValidateContext jvc =new JAXBValidateContext();
            jvc.setURIDereferencer(resolver);
            jvc.put(MessageConstants.WSS_PROCESSING_CONTEXT, pc);
            try{
                if(!reference.validate(jvc)){
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1721_REFERENCE_VALIDATION_FAILED(uri));
                    throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK, LogStringsMessages.WSS_1721_REFERENCE_VALIDATION_FAILED(uri), null);
                }
                return true;
            }catch(XMLSignatureException xse){
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1722_ERROR_REFERENCE_VALIDATION(uri),xse);
                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK,LogStringsMessages.WSS_1722_ERROR_REFERENCE_VALIDATION(uri),xse);
            }
        }
        return false;
    }
    
    public ArrayList<Reference> getReferenceList(){
        if(refList == null){
            refList = new ArrayList<Reference>(1);
        }
        return refList;
    }
    
    public int getReferenceEventType(XMLStreamReader reader){
        if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
            if(reader.getLocalName() == TRANSFORMS){
                return TRANSFORMS_EVENT;
            }
            if(reader.getLocalName() == DIGEST_METHOD){
                return DIGEST_METHOD_EVENT;
            }
            if(reader.getLocalName() == DIGEST_VALUE){
                return DIGEST_VALUE_EVENT;
            }
        }
        return -1;
    }
    
    private int getEventType(XMLStreamReader reader) {
        if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
            if(reader.getLocalName() == CANONICALIZATION_METHOD){
                return CANONICALIZATION_METHOD_EVENT;
            }
            if(reader.getLocalName() == SIGNATURE_METHOD){
                return SIGNATURE_METHOD_EVENT;
            }
            if(reader.getLocalName() == REFERENCE){
                return REFERENCE_EVENT;
            }
        }
        return -1;
    }
    /**
     * processes the transforms under a reference
     * @param reader XMLStreamReader
     * @param uri String
     * @return trList ArrayList
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    @SuppressWarnings("unchecked")
    private ArrayList processTransforms(XMLStreamReader reader,String uri)throws XWSSecurityException{
        try{
            ArrayList trList  = new ArrayList(1);
            while(reader.hasNext()){
                if(StreamUtil.isStartElement(reader) && reader.getLocalName() == TRANSFORM){
                    trList.add(processTransform(reader,uri));
                }
                reader.next();
                if(StreamUtil._break(reader,TRANSFORMS,MessageConstants.DSIG_NS)){                 
                    break;
                }                                
            }
            return trList;
        }catch(XMLStreamException xse){
            throw new XWSSecurityException(xse);
        }
    }
    /**
     * processes the transform identified by the algorithm attribute
     * @param reader XMLStreamReader
     * @param uri String
     * @return Transform
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private Transform processTransform(XMLStreamReader reader,String uri) throws XWSSecurityException{
        try{
            
            ExcC14NParameterSpec exc14nSpec = null;            
            String value = reader.getAttributeValue(null,"Algorithm");
            if(EXC14N_NS.equals(value)){
                exc14nSpec = readEXC14nTransform(reader);
                return (Transform) signatureFactory.newTransform(value,exc14nSpec);
            }else if(Transform.ENVELOPED.equals(value)){
                if(pc.isBSP()){
                    BasicSecurityProfile.log_bsp_3104();
                }
                return (Transform)signatureFactory.newTransform(value,exc14nSpec);
            }else if(MessageConstants.STR_TRANSFORM_URI.equals(value)){
                StreamUtil.moveToNextStartOREndElement(reader);
                if(reader.getLocalName() == MessageConstants.TRANSFORMATION_PARAMETERS){
                    Transform tr = (Transform) signatureFactory.newTransform(value,readSTRTransform(reader));
                    String id = "";
                    int index = uri.indexOf("#");
                    if( index >=0){
                        id = uri.substring(index+1);
                    }else{
                        id = uri;
                    }
                    tr.setReferenceId(id);
                    return tr;
                }
            } else if(MessageConstants.SWA11_ATTACHMENT_CONTENT_SIGNATURE_TRANSFORM.equals(value)){
                return (Transform)signatureFactory.newTransform(value,exc14nSpec);
            } else if(MessageConstants.SWA11_ATTACHMENT_COMPLETE_SIGNATURE_TRANSFORM.equals(value)){
                return (Transform)signatureFactory.newTransform(value,exc14nSpec);
            } else {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1723_UNSUPPORTED_TRANSFORM_ELEMENT(value));
                SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_UNSUPPORTED_ALGORITHM, LogStringsMessages.WSS_1723_UNSUPPORTED_TRANSFORM_ELEMENT(value), null);
            }
            return null;
        }catch(Exception xe){
            throw new XWSSecurityException("Transform error",xe);
        }
    }
    
    /**
     *
     * @param id String
     * @return Object
     */
    private Object getMessagePart(String id){
        HeaderList headers = securityContext.getNonSecurityHeaders();
        if(headers != null && headers.size() >0){
            Iterator<Header> listItr = headers.listIterator();
            boolean found = false;
            while(listItr.hasNext()){
                GenericSecuredHeader header = (GenericSecuredHeader)listItr.next();
                if(header.hasID(id)){
                    return header;
                }
            }
        }
        ArrayList pshList =  securityContext.getProcessedSecurityHeaders();
        for(int j=0; j< pshList.size() ; j++){
            SecurityHeaderElement  header = (SecurityHeaderElement) pshList.get(j);
            if(id.equals(header.getId())){
                return header;
            }
        }
        return null;
    }
    
    /**
     * reads the STR transform and creates the XMLStructure
     * @param reader XMLStreamReader
     * @return XMLStructure
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private XMLStructure readSTRTransform(XMLStreamReader reader)throws XWSSecurityException{
        try{
            TransformationParametersType tp =
                    new com.sun.xml.ws.security.secext10.ObjectFactory().createTransformationParametersType();
            com.sun.xml.ws.security.opt.crypto.dsig.CanonicalizationMethod cm =
                    new com.sun.xml.ws.security.opt.crypto.dsig.CanonicalizationMethod();
            tp.getAny().add(cm);
            JAXBElement<TransformationParametersType> tpElement =
                    new com.sun.xml.ws.security.secext10.ObjectFactory().createTransformationParameters(tp);
            XMLStructure transformSpec = new JAXBStructure(tpElement);
            reader.next();
            if(StreamUtil.isStartElement(reader) && (reader.getLocalName() == MessageConstants.CANONICALIZATION_METHOD)){                
                String value = reader.getAttributeValue(null,"Algorithm");
                cm.setAlgorithm(value);
                StreamUtil.moveToNextStartOREndElement(reader);
            }
            return transformSpec;
        } catch(Exception ex){
            throw new XWSSecurityException(ex);
        }
    }
    /**
     * reads the exclusive canonicalization
     * @param reader XMLStreamReader
     * @return ExcC14NParameterSpec
     * @throws javax.xml.stream.XMLStreamException
     */
    @SuppressWarnings("unchecked")
    private ExcC14NParameterSpec readEXC14nTransform(XMLStreamReader reader) throws XMLStreamException{
        String prefixList = "";
        ExcC14NParameterSpec exc14nSpec = null;
        if(reader.hasNext()){
            reader.next();
            if(StreamUtil.isStartElement(reader) && reader.getLocalName() == INCLUSIVENAMESPACES){                
                prefixList = reader.getAttributeValue(null,"PrefixList");
                String [] pl = null;
                if(prefixList != null && prefixList.length() >0){
                    pl = prefixList.split(" ");
                }
                if(pl != null && pl.length >0){
                    ArrayList prefixs = new ArrayList();
                    for(int i=0;i< pl.length ;i++){
                        prefixs.add(pl[i]);
                    }
                    exc14nSpec = new ExcC14NParameterSpec(prefixs);
                }
                if(reader.hasNext()){
                    reader.next();                    
                }
            }
        }
        return exc14nSpec;
    }
}
