/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * KeyValue.java
 *
 * Created on January 24, 2006, 4:49 PM
 */

package com.sun.xml.ws.security.opt.crypto.dsig.keyinfo;

import java.math.BigInteger;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Abhijit Das
 */
@XmlRootElement(name="KeyValue", namespace = "http://www.w3.org/2000/09/xmldsig#")
public class KeyValue extends com.sun.xml.security.core.dsig.KeyValueType implements javax.xml.crypto.dsig.keyinfo.KeyValue {
    
    /** Creates a new instance of KeyValue */
    public KeyValue() {
    }
    
    public PublicKey getPublicKey() throws KeyException {
        PublicKey publicKey = null;
        for ( Object o : content) {
            if ( o instanceof DSAKeyValue) {
                DSAKeyValue dsaKeyValue = (DSAKeyValue) o;
                
                DSAPublicKeySpec spec = new DSAPublicKeySpec(
                        new BigInteger(dsaKeyValue.getY()),
                        new BigInteger(dsaKeyValue.getP()),
                        new BigInteger(dsaKeyValue.getQ()),
                        new BigInteger(dsaKeyValue.getG()) );
                try {
                    KeyFactory fac = KeyFactory.getInstance("DSA");
                    return fac.generatePublic(spec);
                } catch (Exception ex) {
                    throw new KeyException(ex);
                }
            } else if ( o instanceof RSAKeyValue) {
                RSAKeyValue rsaKayValue = (RSAKeyValue) o;
                
                RSAPublicKeySpec spec = new RSAPublicKeySpec(
                        new BigInteger(rsaKayValue.getModulus()),
                        new BigInteger(rsaKayValue.getExponent()));
                
                try {
                    KeyFactory fac = KeyFactory.getInstance("RSA");
                    return fac.generatePublic(spec);
                } catch (Exception ex) {
                    throw new KeyException(ex);
                }
            }
        }
        return null;
    }
    
    public boolean isFeatureSupported(String string) {
        return false;
    }
    
    public void setContent(List<Object> content ) {
        this.content = content;
    }
    
}
