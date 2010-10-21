/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * CanonicalizationMethod.java
 *
 * Created on January 24, 2006, 2:25 PM
 */

package com.sun.xml.ws.security.opt.crypto.dsig;

import com.sun.xml.security.core.dsig.CanonicalizationMethodType;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.crypto.Data;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;
import java.util.logging.Level;
/**
 *
 * @author Abhijit Das
 * @author K.Venugopal@sun.com
 */
@XmlRootElement(name="CanonicalizationMethod",namespace = "http://www.w3.org/2000/09/xmldsig#")
public class CanonicalizationMethod extends CanonicalizationMethodType implements javax.xml.crypto.dsig.CanonicalizationMethod {
    @XmlTransient private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
    LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    
    @XmlTransient private Exc14nCanonicalizer _exc14nCanonicalizer = new Exc14nCanonicalizer();
    @XmlTransient private AlgorithmParameterSpec _algSpec = null;
    
    /** Creates a new instance of CanonicalizationMethod */
    public CanonicalizationMethod() {
    }
    
    public void setParameterSpec(AlgorithmParameterSpec algSpec) {
        this._algSpec = algSpec;
    }
    
    public AlgorithmParameterSpec getParameterSpec() {
        return _algSpec;
    }
    
    
    public boolean isFeatureSupported(String string) {
        //TODO:
        return false;
    }
    
    public Data transform(Data data, XMLCryptoContext xMLCryptoContext) throws TransformException {
        if(algorithm == CanonicalizationMethod.EXCLUSIVE){
            try {
                _exc14nCanonicalizer.init((TransformParameterSpec) _algSpec);
            } catch (InvalidAlgorithmParameterException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1758_TRANSFORM_INIT(),ex);
                throw new TransformException(ex);
            }
            _exc14nCanonicalizer.transform(data,xMLCryptoContext);
        }
        return null;
        
    }
    
    public Data transform(Data data, XMLCryptoContext xMLCryptoContext, OutputStream outputStream) throws TransformException {
        if(algorithm == CanonicalizationMethod.EXCLUSIVE){
            _exc14nCanonicalizer.transform(data,xMLCryptoContext,outputStream);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public void setContent(List content) {
        this.content = content;
    }
}
