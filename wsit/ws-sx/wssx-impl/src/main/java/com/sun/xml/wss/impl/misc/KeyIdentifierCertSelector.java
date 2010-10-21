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

/*
 * KeyIdentifierCertSelector.java
 *
 * Created on February 26, 2007, 5:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.misc;

import java.security.cert.CertSelector;
import java.security.cert.Certificate;

import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import com.sun.xml.wss.XWSSecurityException;

/**
 *
 * @author kumar jayanti
 */
public class KeyIdentifierCertSelector implements CertSelector {
    
    private final byte[] keyId;
    /** Creates a new instance of KeyIdentifierCertSelector */
    public KeyIdentifierCertSelector(byte[] keyIdValue) {
        this.keyId = keyIdValue;
    }

    public boolean match(Certificate cert) {
        if (cert instanceof X509Certificate) {
            byte[] keyIdtoMatch = null;
            try {
                keyIdtoMatch =
                    X509SubjectKeyIdentifier.getSubjectKeyIdentifier((X509Certificate)cert);
            }catch (XWSSecurityException ex) {
                //ignore since not all certs in Certstore may have SKID
            }
            if (Arrays.equals(keyIdtoMatch, keyId)) {
                return true;
            }  
        }
        return false;
    }
    
    public Object clone() {
        return new KeyIdentifierCertSelector(this.keyId);
    }
    
}
