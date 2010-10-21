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

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.security.trust.WSTrustVersion;

/**
 *
 * @author Jiandong
 */
public class WSTrustVersion10 extends WSTrustVersion{

    private String nsURI;

    public WSTrustVersion10(){
           nsURI =  "http://schemas.xmlsoap.org/ws/2005/02/trust";
    }
    public String getNamespaceURI(){
        return nsURI;
    }

    public  String getIssueRequestTypeURI(){
        return nsURI + "/Issue";
    }

    public  String getRenewRequestTypeURI(){
        return nsURI + "/Renew";
    }

    public  String getCancelRequestTypeURI(){
        return nsURI +"/Cancel";
    }

    public  String getValidateRequestTypeURI(){
        return nsURI +"/Validate";
    }
    
    public String getValidateStatuesTokenType(){
        return nsURI+"/RSTR/Status";
    }
    
    public String getKeyExchangeRequestTypeURI(){
        return nsURI +"/KET";
    }    
    
    public  String getPublicKeyTypeURI(){
        return nsURI +"/PublicKey";
    }

    public  String getSymmetricKeyTypeURI(){
        return nsURI +"/SymmetricKey";
    }

    public  String getBearerKeyTypeURI(){
        return "http://schemas.xmlsoap.org/ws/2005/05/identity/NoProofKey";
    }

    public  String getIssueRequestAction(){
        return nsURI + "/RST/Issue";
    }

    public  String getIssueResponseAction(){
        return nsURI + "/RSTR/Issue";
    }

    public  String getIssueFinalResoponseAction(){
        return nsURI + "/RSTR/Issue";
    }

    public  String getRenewRequestAction(){
        return nsURI + "/RST/Renew";
    }

    public  String getRenewResponseAction(){
        return nsURI + "/RSTR/Renew";
    }

    public  String getRenewFinalResoponseAction(){
        return nsURI + "/RSTR/Renew";
    }
    public  String getCancelRequestAction(){
        return nsURI + "/RST/Cancel";
    }

    public  String getCancelResponseAction(){
        return nsURI + "/RSTR/Cancel";
    }

    public  String getCancelFinalResoponseAction(){
        return nsURI + "/RSTR/Cancel";
    }
    
    public  String getValidateRequestAction(){
        return nsURI + "/RST/Validate";
    }

    public  String getValidateResponseAction(){
        return nsURI + "/RSTR/Validate";
    }

    public  String getValidateFinalResoponseAction(){
        return nsURI + "/RSTR/Validate";
    }

    public  String getCKPSHA1algorithmURI(){
        return nsURI + "/CK/PSHA1";
    }
    
    public  String getCKHASHalgorithmURI(){
        return nsURI + "/CK/HASH";
    }

    public  String getAsymmetricKeyBinarySecretTypeURI(){
        return nsURI + "/AsymmetricKey";
    }

    public  String getNonceBinarySecretTypeURI(){
        return nsURI + "/Nonce";
    }
    
    public String getValidStatusCodeURI(){
        return nsURI + "/status/valid";
    }
    
    public String getInvalidStatusCodeURI(){
        return nsURI + "/status/invalid";
    }
}
