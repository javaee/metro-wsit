/*
 * $Id: SubjectAccessor.java,v 1.9 2009/07/02 06:46:45 kumarjayanti Exp $
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

package com.sun.xml.wss;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;

import javax.security.auth.Subject;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.server.ServletEndpointContext;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Class that can be used on the ServerSide by the SEI implementation methods, Callback Handlers
 * and Standalone SAAJ Applications using XWSS.
 */
public class SubjectAccessor {
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    
    private static ThreadLocal<Subject> wssThreadCtx = new ThreadLocal<Subject>();
    
    /**
     *@return the Requester's Subject if one is available, null otherwise.
     * The subject is populated with credentials from the incoming secure message.
     * Note: the context supplied should either be a ServletEndpointContext or a
     * com.sun.xml.wss.ProcessingContext or javax.xml.ws.handler.MessageContext or
     * javax.xml.ws.WebServiceContext
     */
    public static  Subject getRequesterSubject(Object context) throws XWSSecurityException {
        
        if (context instanceof ProcessingContext) {
            return (Subject)((ProcessingContext)context).getExtraneousProperty(MessageConstants.AUTH_SUBJECT);
        }  else if (context instanceof javax.xml.ws.handler.MessageContext) {
            
            javax.xml.ws.handler.MessageContext msgContext = (javax.xml.ws.handler.MessageContext)context;
            
            Subject subject =(Subject)msgContext.get(MessageConstants.AUTH_SUBJECT);
            return subject;
            
        } else if ( context instanceof javax.xml.ws.WebServiceContext) {
            try {
                 
                    javax.xml.ws.WebServiceContext wsCtx = (javax.xml.ws.WebServiceContext) context;
                    javax.xml.ws.handler.MessageContext msgContext = wsCtx.getMessageContext();
                    if (msgContext != null) {
                        Subject subject =(Subject)msgContext.get(MessageConstants.AUTH_SUBJECT);
                        return subject;
                    } else {
                        return null;
                    }
                
            } catch (NoClassDefFoundError ncde) {
                log.log(Level.SEVERE,
                        "WSS0761.context.not.instanceof.servletendpointcontext", ncde);
                throw new XWSSecurityException(ncde);
            } catch (Exception ex) {
                log.log(Level.SEVERE,
                        "WSS0761.context.not.instanceof.servletendpointcontext", ex);
                throw new XWSSecurityException(ex);
            }
        } else if (context instanceof ServletEndpointContext) {
            
            MessageContext msgContext = ((ServletEndpointContext)context).getMessageContext();
            if (msgContext != null) {
                Subject subject =(Subject)msgContext.getProperty(MessageConstants.AUTH_SUBJECT);
                return subject;
                
            } else {
                return null;
            }
            
        }
        return null;
    }
    
    /**
     *@return the Requester's Subject if one is available, null otherwise.The subject
     * is populated with credentials from the incoming secure message.
     * This method should be used only with synchronous Request-Response Message
     * Exchange Patterns.
     */
    public static Subject getRequesterSubject(){
        return wssThreadCtx.get();
        
    }
    
    /*
     * set the Requester's Subject into the context
     * @param sub the Requesters Subject
     * This method should be used only with synchronous Request-Response Message
     * Exchange Patterns.
     */
    public static void setRequesterSubject(Subject sub){
        wssThreadCtx.set(sub);
    }
}
