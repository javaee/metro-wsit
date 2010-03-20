/*
 * Reference.java
 *
 * Created on January 24, 2006, 2:43 PM
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

import com.sun.org.apache.xml.internal.security.utils.UnsyncBufferedOutputStream;
import com.sun.xml.security.core.dsig.TransformsType;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.crypto.Data;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLValidateContext;
import org.jcp.xml.dsig.internal.DigesterOutputStream;

/**
 *
 * @author Abhijit Das
 * @author K.Venugopal@sun.com
 */
@XmlRootElement(name="Reference",namespace = "http://www.w3.org/2000/09/xmldsig#")
public class Reference extends com.sun.xml.security.core.dsig.ReferenceType implements javax.xml.crypto.dsig.Reference {
    @XmlTransient private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
    LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    @XmlTransient private Data _appliedTransformData;
    //@XmlTransient private boolean _digested = false;
    @XmlTransient private MessageDigest _md;
    
    @XmlTransient private boolean _validated;
    @XmlTransient private boolean _validationStatus;
    @XmlTransient private byte [] _calcDigestValue;
    /** Creates a new instance of Reference */
    public Reference() {
    }
    
    public byte[] getCalculatedDigestValue() {
        return _calcDigestValue;
    }
    
    public boolean validate(XMLValidateContext xMLValidateContext) throws XMLSignatureException {
        if (xMLValidateContext == null) {
            throw new NullPointerException("validateContext cannot be null");
        }
        if (_validated) {
            return _validationStatus;
        }
        Data data = dereference(xMLValidateContext);
        _calcDigestValue = transform(data, xMLValidateContext);
        if(logger.isLoggable(Level.FINEST)){
            logger.log(Level.FINEST,"Calculated digest value is: "+new String(_calcDigestValue));
        }
        
        if(logger.isLoggable(Level.FINEST)){
            logger.log(Level.FINEST," Expected digest value is: "+new String(digestValue));
        }
        
        _validationStatus = Arrays.equals(digestValue, _calcDigestValue);
        _validated = true;
        return _validationStatus;
    }
    
    public void digest(XMLCryptoContext signContext)throws XMLSignatureException {
        if(this.getDigestValue() == null){
            Data data = null;
            if (_appliedTransformData == null) {
                data = dereference(signContext);
            } else {
                data = _appliedTransformData;
            }
            byte [] digest = transform(data, signContext);
            this.setDigestValue(digest);
        }
        // insert digestValue into DigestValue element
        //String encodedDV = Base64.encode(digestValue);
        
    }
    
    
    public DigesterOutputStream getDigestOutputStream() throws XMLSignatureException{
        DigesterOutputStream dos;
        try {
            String algo = StreamUtil.convertDigestAlgorithm(this.getDigestMethod().getAlgorithm());
            if(logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE, "Digest Algorithm is "+ this.getDigestMethod().getAlgorithm());
                logger.log(Level.FINE, "Mapped Digest Algorithm is "+ algo);
            }
            _md = MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException nsae) {
            throw new XMLSignatureException(nsae);
        }
        dos = new DigesterOutputStream(_md);
        return dos;
    }
    
    private byte[] transform(Data dereferencedData,
            XMLCryptoContext context) throws XMLSignatureException {
        
        if (_md == null) {
            try {
                String algo = StreamUtil.convertDigestAlgorithm(this.getDigestMethod().getAlgorithm());
                if(logger.isLoggable(Level.FINE)){
                    logger.log(Level.FINE, "Digest Algorithm is "+ this.getDigestMethod().getAlgorithm());
                    logger.log(Level.FINE, "Mapped Digest Algorithm is "+ algo);
                }
                _md = MessageDigest.getInstance(algo);
                
            } catch (NoSuchAlgorithmException nsae) {
                logger.log(Level.SEVERE,LogStringsMessages.WSS_1760_DIGEST_INIT_ERROR(),nsae);
                throw new XMLSignatureException(nsae);
            }
        }
        _md.reset();
        DigesterOutputStream dos;
        
        //Boolean cache = (Boolean)context.getProperty("javax.xml.crypto.dsig.cacheReference");
        
        dos = new DigesterOutputStream(_md);
        OutputStream os = new UnsyncBufferedOutputStream(dos);
        Data data = dereferencedData;
        if ( transforms != null ) {
            List<Transform> transformList = ((TransformsType)transforms).getTransform();
            if ( transformList != null ) {
                for (int i = 0, size = transformList.size(); i < size; i++) {
                    Transform transform = (Transform) transformList.get(i);
                    try {
                        if (i < size - 1) {
                            data = transform.transform(data, context);
                        } else {
                            data = transform.transform(data, context, os);
                        }
                    } catch (TransformException te) {
                        logger.log(Level.SEVERE,LogStringsMessages.WSS_1759_TRANSFORM_ERROR(te.getMessage()),te);
                        throw new XMLSignatureException(te);
                    }
                }
            }
        }
        
        try {
            os.flush();
            dos.flush();
        } catch (IOException ex) {
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1761_TRANSFORM_IO_ERROR(),ex);
            throw new XMLSignatureException(ex);
        }
        
        
        return dos.getDigestValue();
    }
    
    private Data dereference(XMLCryptoContext context)
            throws XMLSignatureException {
        Data data = null;
        
        // use user-specified URIDereferencer if specified; otherwise use deflt
        URIDereferencer deref = context.getURIDereferencer();
        
        try {
            data = deref.dereference(this, context);
        } catch (URIReferenceException ure) {
            throw new XMLSignatureException(ure);
        }
        return data;
    }
    
    public Data getDereferencedData() {
        return _appliedTransformData;
    }
    
    public InputStream getDigestInputStream() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    public boolean isFeatureSupported(String string) {
        //TODO
        return false;
    }
    
    public DigestMethod getDigestMethod() {
        return digestMethod;
        
    }
    
    public List getTransforms() {
        return transforms.getTransform();
    }
    
}
