/*
 * $Id: SubjectAccessor.java,v 1.2 2006-06-27 15:48:31 kumarjayanti Exp $
 */

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

package com.sun.xml.wss;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.server.ServletEndpointContext;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;



/**
 * Class that can be used on the ServerSide by the SEI implementation methods, Callback Handlers
 * and Standalone SAAJ Applications using XWSS.
 */
public class SubjectAccessor {
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    
    private static ThreadLocal wssThreadCtx = new ThreadLocal();
    
    /**
     *@return the Requester's Subject if one is available, null otherwise.
     * The subject is populated with credentials from the incoming secure message.
     * Note: the context supplied should either be a ServletEndpointContext or a
     * com.sun.xml.wss.ProcessingContext
     */
    public static  Subject getRequesterSubject(Object context) throws XWSSecurityException {
        
        if (context instanceof ProcessingContext) {
            return (Subject)((ProcessingContext)context).getExtraneousProperty(MessageConstants.AUTH_SUBJECT);
        } else if (context instanceof ServletEndpointContext) {
            
            MessageContext msgContext = ((ServletEndpointContext)context).getMessageContext();
            if (msgContext != null) {
                Subject subject =(Subject)msgContext.getProperty(MessageConstants.AUTH_SUBJECT);
                return subject;
                
            } else {
                return null;
            }
        } else {
            try {
                if ( context instanceof javax.xml.ws.WebServiceContext) {
                    /*
                    java.security.Principal principal =
                            ((javax.xml.ws.WebServiceContext)context).getUserPrincipal();
                    if ( principal != null ) {
                        Subject subject = new Subject();
                        subject.getPrincipals().add(principal);
                        return subject;
                    } else {
                        return null;
                    }*/
                    javax.xml.ws.WebServiceContext wsCtx = (javax.xml.ws.WebServiceContext) context;
                    javax.xml.ws.handler.MessageContext msgContext = wsCtx.getMessageContext();
                    if (msgContext != null) {
                        Subject subject =(Subject)msgContext.get(MessageConstants.AUTH_SUBJECT);
                        return subject;
                    } else {
                        return null;
                    }
                }
            } catch (NoClassDefFoundError ncde) {
                log.log(Level.SEVERE,
                        "WSS0761.context.not.instanceof.servletendpointcontext");
                throw new XWSSecurityException(
                        "'context' argument is not an instanceof ServletEndpointContext, WebServiceContext or com.sun.xml.wss.ProcessingContext");
            } catch (Exception ex) {
                log.log(Level.SEVERE,
                        "WSS0761.context.not.instanceof.servletendpointcontext");
                throw new XWSSecurityException(
                        "'context' argument is not an instanceof ServletEndpointContext, WebServiceContext or com.sun.xml.wss.ProcessingContext");
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
        return (Subject)wssThreadCtx.get();
        
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
