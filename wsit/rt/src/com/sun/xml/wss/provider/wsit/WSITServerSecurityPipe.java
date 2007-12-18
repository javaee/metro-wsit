/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * WSITServerSecurityPipe.java
 *
 * Created on November 6, 2006, 6:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import java.util.Map;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.xml.ws.WebServiceException;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.provider.wsit.logging.LogDomainConstants;
import com.sun.xml.wss.provider.wsit.logging.LogStringsMessages;

/**
 *
 * @author kumar jayanti
 */
public class WSITServerSecurityPipe implements Pipe {
    
    private static final Logger log =
        Logger.getLogger(
        LogDomainConstants.WSIT_PVD_DOMAIN,
        LogDomainConstants.WSIT_PVD_DOMAIN_BUNDLE);
    
    Map properties = null;
    Pipe nextPipe = null;
    
    ServerAuthConfig serverAuthConfig = null;
    ServerAuthContext serverAuthContext = null;
    
    public WSITServerSecurityPipe(WSITServerSecurityPipe that) {
        this.properties = that.properties;
        this.serverAuthConfig = that.serverAuthConfig;
        this.serverAuthContext = that.serverAuthContext;
    }
    
    /** Creates a new instance of WSITServerSecurityPipe */
    @SuppressWarnings("unchecked")
    public WSITServerSecurityPipe(Map properties, Pipe nextPipe) {
        this.properties = properties;
        this.nextPipe = nextPipe;
        properties.put("NEXT_PIPE", nextPipe);
        properties.put("WSIT_GENERIC_CALLBACK_HANDLER", "true");
        
        //TODO: Load the Class by reflection.
        //Hack for now
        AuthConfigProvider provider = new WSITAuthConfigProvider(properties, null);
        try {
            serverAuthConfig = provider.getServerAuthConfig("SOAP", null, null);
            //initialize the serverAuthContext
            serverAuthContext = serverAuthConfig.getAuthContext(null, null, properties);
        } catch (AuthException e) {
            log.log(Level.SEVERE, 
                    LogStringsMessages.WSITPVD_0047_ERROR_CREATING_NEW_INSTANCE_WSIT_SERVER_SEC_PIPE(), e);
            throw new RuntimeException(
                    LogStringsMessages.WSITPVD_0047_ERROR_CREATING_NEW_INSTANCE_WSIT_SERVER_SEC_PIPE(), e);                        
        }
    }

    @SuppressWarnings("unchecked")
    public Packet process(Packet packet) {
        MessageInfo messageInfo = new PacketMessageInfo();
        
        try {
            messageInfo.getMap().put("REQ_PACKET", packet);
            AuthStatus status = serverAuthContext.validateRequest(messageInfo, null,null);

            if (status == status.SEND_SUCCESS || status == status.SEND_FAILURE || status == status.FAILURE) {
                return (Packet)messageInfo.getMap().get("RES_PACKET");
            }

            Packet retPacket = nextPipe.process((Packet)messageInfo.getRequestMessage());
            messageInfo.getMap().put("RES_PACKET",retPacket);

            if (retPacket.getMessage() == null) {
                return retPacket;
            }
            //TODO: check auth status here as well
            serverAuthContext.secureResponse(messageInfo, null);
        } catch (AuthException e) {
            log.log(Level.SEVERE, 
                    LogStringsMessages.WSITPVD_0039_ERROR_PROCESSING_INCOMING_PACKET(), e);
            throw new WebServiceException(
                    LogStringsMessages.WSITPVD_0039_ERROR_PROCESSING_INCOMING_PACKET(), e);            
        }
        return (Packet)messageInfo.getMap().get("RES_PACKET");
    }

    public void preDestroy() {
        try {
            serverAuthContext.cleanSubject(null, null);
        } catch (AuthException e) {
            log.log(Level.SEVERE, 
                    LogStringsMessages.WSITPVD_0040_ERROR_CLEAN_SUBJECT(), e);
            throw new RuntimeException(
                    LogStringsMessages.WSITPVD_0040_ERROR_CLEAN_SUBJECT(), e);            
        }
       if (nextPipe != null) {
           nextPipe.preDestroy();
       }
    }

    public Pipe copy(PipeCloner cloner) {
        Pipe clonedNextPipe = cloner.copy(nextPipe);
        Pipe copied = new WSITServerSecurityPipe(this);
        ((WSITServerSecurityPipe)copied).setNextPipe(clonedNextPipe);
        cloner.add(this, copied);
        return copied;    
    }
    
    public void setNextPipe(Pipe next) {
        this.nextPipe = next;
    }
    
   
}
