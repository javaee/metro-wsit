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

package com.sun.wsit.security;

import java.io.IOException;
import java.security.cert.X509Certificate;
/**
 *
 * @author sk112103
 */
public class SunKeyIdentifierSPI extends com.sun.xml.wss.core.reference.KeyIdentifierSPI {
    
    /** Creates a new instance of SunKeyIdentifierSPI */
    public SunKeyIdentifierSPI() {
    }

    public byte[] getSubjectKeyIdentifier(X509Certificate cert) throws KeyIdentifierSPIException {
        byte[] subjectKeyIdentifier =
                cert.getExtensionValue(SUBJECT_KEY_IDENTIFIER_OID);
        if (subjectKeyIdentifier == null)
            return null;
        
        try {
            sun.security.x509.KeyIdentifier keyId = null;
            
            sun.security.util.DerValue derVal = new sun.security.util.DerValue(
                    new sun.security.util.DerInputStream(subjectKeyIdentifier).getOctetString());
            
            keyId = new sun.security.x509.KeyIdentifier(derVal.getOctetString());
            return keyId.getIdentifier();
        } catch (NoClassDefFoundError ncde) {
            // TODO X509 Token profile states that only the contents of the
            // OCTET STRING should be returned, excluding the "prefix"
            byte[] dest = new byte[subjectKeyIdentifier.length-4];
            System.arraycopy(
                    subjectKeyIdentifier, 4, dest, 0, subjectKeyIdentifier.length-4);
            return dest;
            
        } catch (IOException e) {
            //log exception
            throw new KeyIdentifierSPIException(e);
        }

    }
    
    
}
