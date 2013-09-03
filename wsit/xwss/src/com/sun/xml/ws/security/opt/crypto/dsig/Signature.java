/* 
 * Signature.java 
 * 
 * Created on January 24, 2006, 3:59 PM 
 */ 


/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.xml.ws.security.opt.crypto.dsig;

import com.sun.xml.ws.security.opt.impl.util.JAXBUtil; 
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo; 
import com.sun.xml.wss.logging.LogDomainConstants;
import java.security.InvalidKeyException; 
import java.security.Key; 
import java.security.SignatureException; 
import java.util.ArrayList; 
import java.util.Arrays; 
import java.util.List; 
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext; 
import javax.xml.bind.annotation.XmlRootElement; 
import javax.xml.bind.annotation.XmlTransient; 
import javax.xml.crypto.KeySelector; 
import javax.xml.crypto.KeySelectorException; 
import javax.xml.crypto.KeySelectorResult; 
import javax.xml.crypto.MarshalException; 
import javax.xml.crypto.dsig.XMLSignContext; 
import javax.xml.crypto.dsig.XMLSignatureException; 
import javax.xml.crypto.dsig.XMLValidateContext;  
import org.jvnet.staxex.XMLStreamReaderEx; 
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec; 
import javax.xml.crypto.dsig.spec.HMACParameterSpec; 
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;

/** 
 * 
 * @author Abhijit Das 
 * @author K.Venugopal@sun.com 
 */



@XmlRootElement(name="Signature",namespace = "http://www.w3.org/2000/09/xmldsig#") 
public class Signature extends com.sun.xml.security.core.dsig.SignatureType implements javax.xml.crypto.dsig.XMLSignature { 
    @XmlTransient private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);

    @XmlTransient private XMLStreamReaderEx _streamSI = null; 
    @XmlTransient private String type = null; 
    @XmlTransient private List<XMLObject> objects = null; 
    @XmlTransient private SignatureProcessor _sp; 
    @XmlTransient private Key verificationKey = null; 
    @XmlTransient private byte []  signedInfoBytes = null;

    /** 
     * Creates a new instance of Signature 
     */

    public Signature() { 
    }

    public void setSignedInfo(XMLStreamReaderEx streamReader) { 
        this._streamSI = streamReader; 
    }

    
    public void setSignedInfo(byte [] si){ 
        this.signedInfoBytes =  si; 
    }

    public void setVerificationKey(Key key){ 
        this.verificationKey = key; 
    } 
    

    public Key getVerificationKey(){ 
        return verificationKey;

    }

    public boolean validate(XMLValidateContext xMLValidateContext) throws XMLSignatureException {

        SignatureMethod sm; 
        if (xMLValidateContext == null) { 
            throw new NullPointerException("validateContext cannot be null"); 
        } 
        

        //List allReferences = new ArrayList(signedInfo.getReferences());

        
        SignedInfo si = getSignedInfo(); 
        List refList = si.getReferences(); 
        for (int i = 0, size = refList.size(); i < size; i++) { 
            Reference ref = (Reference) refList.get(i); 
            byte[] originalDigest = ref.getDigestValue(); 
            ref.digest(xMLValidateContext); 
            byte[] calculatedDigest = ref.getDigestValue(); 
            if ( ! Arrays.equals(originalDigest, calculatedDigest) ) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1713_SIGNATURE_VERIFICATION_EXCEPTION("Signature digest values mismatch"));
                throw new XMLSignatureException(LogStringsMessages.WSS_1713_SIGNATURE_VERIFICATION_EXCEPTION("Signature digest values mismatch"));

            } 
        } 
        si.setSignedInfo(_streamSI);

        // si.setCanonicalizedSI(signedInfoBytes);
        

        KeySelectorResult keySelectoResult = null; 
        try { 
            sm = si.getSignatureMethod(); 
            if(verificationKey == null){ 
                keySelectoResult = xMLValidateContext.getKeySelector().select(getKeyInfo(),KeySelector.Purpose.VERIFY,sm,xMLValidateContext); 
                verificationKey = keySelectoResult.getKey(); 
            } 
            if (verificationKey == null) { 
                throw new XMLSignatureException("The KeySelector"+ xMLValidateContext.getKeySelector()+ " did not " + 
                          "find the key used for signature verification"); 
            }

            if(_sp == null){ 
                _sp = new SignatureProcessor(); 
            }

            try { 
                String signatureAlgo = sm.getAlgorithm(); 
                if ( signatureAlgo.equals(SignatureMethod.RSA_SHA1)) { 
                    return _sp.verifyRSASignature(verificationKey,si,getSignatureValue().getValue()); 
                } else if ( signatureAlgo.equals(SignatureMethod.DSA_SHA1)) { 
                    return _sp.verifyDSASignature(verificationKey,si,getSignatureValue().getValue()); 
                } else if ( signatureAlgo.equals(SignatureMethod.HMAC_SHA1)) { 
                    SignatureMethodParameterSpec params = (SignatureMethodParameterSpec)sm.getParameterSpec();

                    int outputLength = -1;

                    if (params != null) { 
                        if (!(params instanceof HMACParameterSpec)) { 
                            throw new XMLSignatureException ("SignatureMethodParameterSpec must be of type HMACParameterSpec");

                        } 
                        outputLength = ((HMACParameterSpec) params).getOutputLength(); 
                    } 
                    return _sp.verifyHMACSignature(verificationKey,si,getSignatureValue().getValue(), outputLength); 
                } else { 
                    throw new XMLSignatureException("Unsupported signature algorithm found"); 
                } 
            } catch (InvalidKeyException ex) { 
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1713_SIGNATURE_VERIFICATION_EXCEPTION(ex));
                throw new XMLSignatureException(LogStringsMessages.WSS_1713_SIGNATURE_VERIFICATION_EXCEPTION(ex)); 
            } catch (SignatureException ex) { 
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1713_SIGNATURE_VERIFICATION_EXCEPTION("Signature digest values mismatch"));
                throw new XMLSignatureException(LogStringsMessages.WSS_1713_SIGNATURE_VERIFICATION_EXCEPTION(ex)); 
            } 
        } catch (KeySelectorException kse) { 
            throw new XMLSignatureException("Cannot find verification key", kse); 
        } 
        //return false; 
    } 

    public List getObjects() { 
        return null; 
    }

    public void sign(XMLSignContext xMLSignContext) throws MarshalException, XMLSignatureException { 
        SignatureMethod sm; 
        if (xMLSignContext == null) { 
            throw new NullPointerException("signContext cannot be null"); 
        } 
        
        //List allReferences = new ArrayList(signedInfo.getReferences()); 
        SignedInfo si = getSignedInfo(); 
        List refList = si.getReferences(); 
        for (int i = 0, size = refList.size(); i < size; i++) { 
            Reference ref = (Reference) refList.get(i); 
            ref.digest(xMLSignContext); 
        }


        Key signingKey = null; 
        KeySelectorResult keySelectoResult = null; 
        try { 
            sm = si.getSignatureMethod(); 
            keySelectoResult = xMLSignContext.getKeySelector().select(getKeyInfo(),KeySelector.Purpose.SIGN,sm,xMLSignContext); 
            signingKey = keySelectoResult.getKey(); 
            if (signingKey == null) { 
                throw new XMLSignatureException("The KeySelector"+ xMLSignContext.getKeySelector()+ " did not " + 
                          "find the key used for signing"); 
            } 
        } catch (KeySelectorException kse) { 
            throw new XMLSignatureException("Cannot find signing key", kse); 
        }

        if(_sp == null){ 
            try { 
                JAXBContext jc = JAXBUtil.getJAXBContext(); 
                _sp = new SignatureProcessor(); 
                _sp.setJAXBContext(jc); 
                _sp.setCryptoContext(xMLSignContext); 
            } catch (Exception ex) { 
                throw new XMLSignatureException(ex); 
            } 
        }

        String signatureAlgo = sm.getAlgorithm(); 
        //SignatureValue sv=getSignatureValue(); 
        
        if(signatureAlgo == SignatureMethod.RSA_SHA1){ 
            try { 
                com.sun.xml.ws.security.opt.crypto.dsig.SignatureValue sigValue = new com.sun.xml.ws.security.opt.crypto.dsig.SignatureValue(); 
                sigValue.setValue(_sp.performRSASign(signingKey,signedInfo)); 
                setSignatureValue(sigValue); 
                //((SignatureValueType)getSignatureValue()).setValue(_sp.performRSASign(signingKey,signedInfo)); 
                //((SignatureValueType)sv).setValue(_sp.performRSASign(signingKey,signedInfo)); 
            } catch (InvalidKeyException ex) { 
                throw new XMLSignatureException(ex); 
            } 
        } else if(signatureAlgo == SignatureMethod.DSA_SHA1){ 
            try { 
                com.sun.xml.ws.security.opt.crypto.dsig.SignatureValue sigValue = new com.sun.xml.ws.security.opt.crypto.dsig.SignatureValue(); 
                sigValue.setValue(_sp.performDSASign(signingKey,signedInfo)); 
                setSignatureValue(sigValue); 
                
                //((SignatureValueType)sv).setValue(_sp.performDSASign(signingKey,signedInfo));

            } catch (InvalidKeyException ex) { 
                throw new XMLSignatureException(ex); 
            }

        } else if ( signatureAlgo.equals(SignatureMethod.HMAC_SHA1)) { 
            SignatureMethodParameterSpec params = (SignatureMethodParameterSpec)sm.getParameterSpec(); 
            int outputLength = -1; 
            if (params != null) { 
                if (!(params instanceof HMACParameterSpec)) { 
                    throw new XMLSignatureException 
                              ("SignatureMethodParameterSpec must be of type HMACParameterSpec"); 
                } 
                outputLength = ((HMACParameterSpec) params).getOutputLength(); 
            }

            try{ 
                com.sun.xml.ws.security.opt.crypto.dsig.SignatureValue sigValue = new com.sun.xml.ws.security.opt.crypto.dsig.SignatureValue(); 
                sigValue.setValue(_sp.performHMACSign(signingKey,signedInfo, outputLength)); 
                setSignatureValue(sigValue); 
            } catch (InvalidKeyException ex) { 
                throw new XMLSignatureException(ex); 
            } 
        } else { 
            throw new XMLSignatureException("Unsupported signature algorithm found"); 
        } 
    }

    public KeySelectorResult getKeySelectorResult() { 
        return null; 
    }

    

    public boolean isFeatureSupported(String string) { 
        return false; 
    } 
    
    public SignatureValue getSignatureValue() {
        return signatureValue;
    }


    public SignedInfo getSignedInfo() {
        return this.signedInfo; 
    }


    public KeyInfo getKeyInfo() { 
        return keyInfo; 
    }

    
    public void setObjects(List<XMLObject> objects) { 
        this.objects = objects; 
    }


    public String getType() { 
        return type; 
    }


    public void setType(String type) { 
        this.type = type; 
    }


}

