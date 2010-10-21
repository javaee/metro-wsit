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

package com.sun.xml.ws.security.trust;

import com.sun.xml.ws.security.trust.impl.WSTrustVersion10;
import com.sun.xml.ws.security.trust.impl.wssx.WSTrustVersion13;

/**
 *
 * @author Jiandong
 */
public abstract class WSTrustVersion {

    public static final WSTrustVersion WS_TRUST_10 = new WSTrustVersion10();

    public static final WSTrustVersion WS_TRUST_13 = new WSTrustVersion13();

    public static final String WS_TRUST_10_NS_URI = "http://schemas.xmlsoap.org/ws/2005/02/trust";
    public static final String WS_TRUST_13_NS_URI = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
    public static WSTrustVersion getInstance(String nsURI){
        if (nsURI.equals(WS_TRUST_13.getNamespaceURI())){
            return WS_TRUST_13;
        }
        
        return WS_TRUST_10;
    }
    
    public abstract String getNamespaceURI();

    public abstract String getIssueRequestTypeURI();

    public abstract String getRenewRequestTypeURI();

    public abstract String getCancelRequestTypeURI();

    public abstract String getValidateRequestTypeURI();     
    
    public abstract String getValidateStatuesTokenType();

    public abstract String getKeyExchangeRequestTypeURI();
    
    public abstract String getPublicKeyTypeURI();        

    public abstract String getSymmetricKeyTypeURI();

    public abstract String getBearerKeyTypeURI();

    public abstract String getIssueRequestAction();

    public abstract String getIssueResponseAction();

    public abstract String getIssueFinalResoponseAction();

    public abstract String getRenewRequestAction();

    public abstract String getRenewResponseAction();

    public abstract String getRenewFinalResoponseAction();

    public abstract String getCancelRequestAction();

    public abstract String getCancelResponseAction();

    public abstract String getCancelFinalResoponseAction();
    
    public abstract String getValidateRequestAction();

    public abstract String getValidateResponseAction();

    public abstract String getValidateFinalResoponseAction();
    
    public String getFinalResponseAction(String reqAction){ 
        if (reqAction.equals(getIssueRequestAction())){
            return getIssueFinalResoponseAction();
        }
        
        if (reqAction.equals(getRenewRequestAction())){
            return getRenewFinalResoponseAction();
        }
        
        if (reqAction.equals(getCancelRequestAction())){
            return getCancelFinalResoponseAction();
        }
        
        if (reqAction.equals(getValidateRequestAction())){
            return getValidateFinalResoponseAction();
        }
        
        return null;
    }

    public abstract String getCKPSHA1algorithmURI();
    
    public abstract String getCKHASHalgorithmURI();

    public abstract String getAsymmetricKeyBinarySecretTypeURI();

    public abstract String getNonceBinarySecretTypeURI();
    
    public abstract String getValidStatusCodeURI();
    
    public abstract String getInvalidStatusCodeURI();
    
}
