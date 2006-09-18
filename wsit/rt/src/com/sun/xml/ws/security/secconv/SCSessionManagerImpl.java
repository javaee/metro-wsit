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

/*
 * SCSessionManagerImpl.java
 *
 * Created on February 1, 2006, 1:55 PM
 *
 */

package com.sun.xml.ws.security.secconv;

import com.sun.xml.ws.security.IssuedTokenContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.secconv.logging.LogDomainConstants;

/**
 *
 * @author Jiandong Guo
 */
class SCSessionManagerImpl implements SCSessionManager{
    
    Map sessions;
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.WSSC_IMPL_DOMAIN,
            LogDomainConstants.WSSC_IMPL_DOMAIN_BUNDLE);
    
    protected SCSessionManagerImpl() {
        sessions = new HashMap();
    }
    
    public Iterator getSecurityContextIds(){
        return sessions.keySet().iterator();
    }
    
    public IssuedTokenContext getSecurityContext(String secCtxId){
        IssuedTokenContext itcon = (IssuedTokenContext)sessions.get(secCtxId);
        if (itcon == null) {
            log.log(Level.FINE,
                    "WSSC1001.null.itCtx.for.session",
                    new Object[] {secCtxId});
        }
        return itcon;
    }
    
    public void addSecurityContext(IssuedTokenContext context, String secCtxId){
        try {
            sessions.put(secCtxId, context);
        } catch (NullPointerException npe) {
            log.log(Level.INFO,
                    "WSSC1002.null.sessionCtxId.askey");
        }
    }
}
