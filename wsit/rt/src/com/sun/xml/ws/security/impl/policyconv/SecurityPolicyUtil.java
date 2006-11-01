/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.wss.impl.MessageConstants;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SecurityPolicyUtil {
    
    /** Creates a new instance of SecurityPolicyUtil */
    public SecurityPolicyUtil() {
    }
    
    public static boolean isSignedPartsEmpty(SignedParts sp){
        if(!sp.hasBody()){
            if(!sp.getHeaders().hasNext()){
                return true;
            }
        }
        return false;
    }
    
    public static boolean isEncryptedPartsEmpty(EncryptedParts ep){
        if(!ep.hasBody()){
            if(!ep.getTargets().hasNext()){
                return true;
            }
        }
        return false;
    }
    
    public static String convertToXWSSConstants(String type){
        if(type.contains(Token.REQUIRE_THUMBPRINT_REFERENCE)){
            return MessageConstants.THUMB_PRINT_TYPE;
        }else if(type.contains(Token.REQUIRE_KEY_IDENTIFIER_REFERENCE)){
            return MessageConstants.KEY_INDETIFIER_TYPE;
        }else if(type.contains(Token.REQUIRE_ISSUER_SERIAL_REFERENCE)){
            return MessageConstants.X509_ISSUER_TYPE ;
        }
        throw new UnsupportedOperationException(type+"  is not supported");
    }
}
