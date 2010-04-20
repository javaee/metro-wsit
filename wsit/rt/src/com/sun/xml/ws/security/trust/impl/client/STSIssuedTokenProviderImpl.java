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

package com.sun.xml.ws.security.trust.impl.client;

import com.sun.xml.ws.api.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.TrustPlugin;
import com.sun.xml.ws.security.trust.WSTrustFactory;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.client.IssuedTokenProvider;
import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;
import com.sun.xml.wss.SubjectAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import javax.security.auth.Subject;

import com.sun.xml.ws.security.trust.logging.LogDomainConstants;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jiandong Guo
 */
public class STSIssuedTokenProviderImpl implements IssuedTokenProvider {

     private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    public void issue(IssuedTokenContext ctx)throws WSTrustException{
        STSIssuedTokenConfiguration config = (STSIssuedTokenConfiguration)ctx.getSecurityPolicy().get(0);
        ctx.setTokenIssuer(config.getSTSEndpoint());
        boolean shareToken = "true".equals(config.getOtherOptions().get(STSIssuedTokenConfiguration.SHARE_TOKEN));
        boolean renewExpiredToken = "true".equals(config.getOtherOptions().get(STSIssuedTokenConfiguration.RENEW_EXPIRED_TOKEN));
        getIssuedTokenContext(shareToken, renewExpiredToken, ctx);
    } 
    
    public void cancel(IssuedTokenContext ctx)throws WSTrustException{
        
    }
    
    public void renew(IssuedTokenContext ctx)throws WSTrustException{
        
    }
    
    public void validate(IssuedTokenContext ctx)throws WSTrustException{
        TrustPlugin tp = WSTrustFactory.newTrustPlugin();
        tp.processValidate(ctx);
    }

    private void updateContext(IssuedTokenContext cached, IssuedTokenContext ctx) {
        ctx.setUnAttachedSecurityTokenReference(cached.getUnAttachedSecurityTokenReference());
        ctx.setSecurityToken(cached.getSecurityToken());
        ctx.setRequestorCertificate(cached.getRequestorCertificate());
        ctx.setProofKeyPair(cached.getProofKeyPair());
        ctx.setProofKey(cached.getProofKey());
        ctx.setExpirationTime(cached.getExpirationTime());
        ctx.setCreationTime(cached.getCreationTime());
        ctx.setAttachedSecurityTokenReference(cached.getAttachedSecurityTokenReference());
    }

    private void getIssuedTokenContext(boolean shareToken, boolean renewExpiredToken, IssuedTokenContext ctx)throws WSTrustException {
        Subject subject = SubjectAccessor.getRequesterSubject();
        if (shareToken && subject != null){
            Set pcs = subject.getPrivateCredentials(IssuedTokenContext.class);
            for (Object obj : pcs){
                IssuedTokenContext cached = (IssuedTokenContext)obj;

                // Check if the token is expired
                Calendar c = new GregorianCalendar();
                long offset = c.get(Calendar.ZONE_OFFSET);
                if (c.getTimeZone().inDaylightTime(c.getTime())) {
                    offset += c.getTimeZone().getDSTSavings();
                }
                long beforeTime = c.getTimeInMillis();
                long currentTime = beforeTime - offset;
                c.setTimeInMillis(currentTime);
                Date currentTimeInDateFormat = c.getTime();
                if(cached.getExpirationTime() != null && currentTimeInDateFormat.after(cached.getExpirationTime())){
                    // Remove the expired context
                    subject.getPrivateCredentials().remove(cached);

                    //if renewExpiredToke="true" is not set
                    if (!renewExpiredToken){
                        log.log(Level.SEVERE,
                        LogStringsMessages.WST_0046_TOKEN_EXPIRED(cached.getCreationTime(), cached.getExpirationTime(), currentTimeInDateFormat));
                        throw new WSTrustException(LogStringsMessages.WST_0046_TOKEN_EXPIRED(cached.getCreationTime(), cached.getExpirationTime(), currentTimeInDateFormat));
                    }
                } else if (cached.getTokenIssuer().equals(ctx.getTokenIssuer())){
                    updateContext(cached, ctx);
                    return;
                }
            }
        }
        
        TrustPlugin tp = WSTrustFactory.newTrustPlugin();
        tp.process(ctx);
        if (shareToken){
            if (subject == null){
                 subject = new Subject();
            }
            subject.getPrivateCredentials().add(ctx);
            SubjectAccessor.setRequesterSubject(subject);
        }
    }
}
