/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.wss.jaxws.impl.TubeConfiguration;
import com.sun.xml.wss.provider.wsit.logging.LogDomainConstants;
import com.sun.xml.wss.provider.wsit.logging.LogStringsMessages;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.config.ClientAuthContext;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.WebServiceException;

/**
 *
 */
public class ClientSecurityTube extends AbstractFilterTubeImpl implements SecureConversationInitiator {

    private static final String WSIT_CLIENT_AUTH_CONTEXT="com.sun.xml.wss.provider.wsit.WSITClientAuthContext";
    protected PipeHelper helper;
    
    private AuthStatus status = AuthStatus.SEND_SUCCESS;
    private ClientAuthContext cAC = null;
    private Subject clientSubject = null;
    private PacketMessageInfo pmInfo = null;
    protected X509Certificate serverCert = null;
   
    protected static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSIT_PVD_DOMAIN,
            LogDomainConstants.WSIT_PVD_DOMAIN_BUNDLE);

   
    
    public ClientSecurityTube(TubeConfiguration config, Tube nextTube) {
        super(nextTube);  
    }
    
    public ClientSecurityTube(Map<Object, Object> props, Tube next) {

        super(next);
        props.put(PipeConstants.SECURITY_PIPE, this);

        WSDLPort wsdlModel = (WSDLPort) props.get(PipeConstants.WSDL_MODEL);
        if (wsdlModel != null) {
            props.put(PipeConstants.WSDL_SERVICE,
                    wsdlModel.getOwner().getName());
        }
        this.helper = new PipeHelper(PipeConstants.SOAP_LAYER, props, null);      
    }
     
    protected ClientSecurityTube(ClientSecurityTube that, TubeCloner cloner) {
        super(that, cloner);
        this.helper = that.helper;
        this.serverCert = that.serverCert;
    }

    @Override
    public AbstractTubeImpl copy(TubeCloner cloner) {
         return new ClientSecurityTube(this, cloner);
    }
    
    @Override
    public void preDestroy() {
        //Give the AuthContext a chance to cleanup 
        //create a dummy request packet
        try {
            Packet request = new Packet();
            PacketMessageInfo info = new PacketMapMessageInfo(request, new Packet());
            Subject subj = getClientSubject(request);
            ClientAuthContext cAC = helper.getClientAuthContext(info, subj);
            if (cAC != null && WSIT_CLIENT_AUTH_CONTEXT.equals(cAC.getClass().getName())) {
                cAC.cleanSubject(info, subj);
            }
        } catch (Exception ex) {
        //ignore exceptions
        }
        helper.disable();
    }    
    
    @Override
    public NextAction processRequest(Packet packet) {
        try {
            packet = processClientRequest(packet);
            //store the subject
            this.clientSubject = (Subject)packet.invocationProperties.get(PipeConstants.CLIENT_SUBJECT);
        } catch (Throwable t) {
            if (!(t instanceof WebServiceException)) {
                t = new WebServiceException(t);
            }
            return doThrow(t);
        }
        if (status == AuthStatus.FAILURE) {
            return doReturnWith(packet);
        }
        return doInvoke(super.next, packet);
    }
    
    @Override
    public NextAction processResponse(Packet ret) {
        try {
            //set the subject
            ret.invocationProperties.put(PipeConstants.CLIENT_SUBJECT, clientSubject);
            ret = processClientResponse(ret);
        } catch (Throwable t) {
            if (!(t instanceof WebServiceException)) {
                t = new WebServiceException(t);
            }
            return doThrow(t);
        }
        return doReturnWith(ret);
    }
    
    @Override
    public NextAction processException(Throwable t) {
        if (!(t instanceof WebServiceException)) {
            t = new WebServiceException(t);
        }
        return doThrow(t);
    }

    

    @SuppressWarnings("unchecked")
    private Packet processClientRequest(Packet request) {
       /*
	 * XXX should there be code like the following?
	 if(isHttpBinding) {
	     return next.process(request);
	 }
        */
	PacketMessageInfo info= new PacketMapMessageInfo(request,new Packet());
        info.getMap().put(javax.xml.ws.Endpoint.WSDL_SERVICE,
            helper.getProperty(PipeConstants.WSDL_SERVICE));
	clientSubject = getClientSubject(request);
	cAC = null;
	try {
	    cAC = helper.getClientAuthContext(info,clientSubject);
	    if (cAC != null) {
		// proceed to process message sescurity
		status = cAC.secureRequest(info, clientSubject);  
	    }
	} catch(Exception e) {
	    log.log(Level.SEVERE,LogStringsMessages.WSITPVD_0058_ERROR_SECURE_REQUEST(), e);
	    throw new WebServiceException(
		  "Cannot secure request",e);
	} 

	Packet response = null;
	if (status == AuthStatus.FAILURE) {
	    if (log.isLoggable(Level.FINE)) {
		log.log(Level.FINE,"ws.status_secure_request", status);
	    }
	    response = info.getResponsePacket();
	}  else {
            response = info.getRequestPacket();
        }
	// may return a security fault even if the MEP was one-way
        pmInfo = info;
        return response;
    }

    private Packet processClientResponse(Packet response) {
        // check for response
	Message m = response.getMessage();
	if (m != null) {
	    if (cAC != null) {
		AuthStatus authstatus = AuthStatus.SUCCESS;
		pmInfo.setResponsePacket(response);
		try {
		    authstatus = cAC.validateResponse(pmInfo,clientSubject,null);
		} catch (Exception e) {
		    throw new WebServiceException
			 ("Cannot validate response for {0}",e);
		}
		if (authstatus == AuthStatus.SEND_CONTINUE) {
		    response = processSecureRequest(pmInfo, cAC, clientSubject);
		} else {
		    response = pmInfo.getResponsePacket();
		} 
	    }
	}

	return response;
    }

    private Packet processSecureRequest(PacketMessageInfo info, 
	ClientAuthContext cAC, Subject clientSubject) 
	throws WebServiceException {
	// send the request
	//Packet response = next.process(info.getRequestPacket());
         Fiber fiber = Fiber.current().owner.createFiber(); 
         Packet response = fiber.runSync(next, info.getRequestPacket());
        
	// check for response
	Message m = response.getMessage();
	if (m != null) {
	    if (cAC != null) {
		AuthStatus status = AuthStatus.SUCCESS;
		info.setResponsePacket(response);
		try {
		    status = cAC.validateResponse(info,clientSubject,null);
		} catch (Exception e) {
		    throw new WebServiceException
			 ("Cannot validate response for {0}",e);
		}
		if (status == AuthStatus.SEND_CONTINUE) {
		    response = processSecureRequest(info, cAC, clientSubject);
		} else {
		    response = info.getResponsePacket();
		} 
	    }
	}
	return response;
    }

    
    public JAXBElement startSecureConversation(Packet packet) throws WSSecureConversationException {
        PacketMessageInfo info = new PacketMapMessageInfo(packet,new Packet());
	JAXBElement token = null;

	try {
	    // gets the subject from the packet (puts one there if not found)
	    Subject clientSubject = getClientSubject(packet);
	    // put MessageInfo in properties map, since MessageInfo 
	    // is not passed to getAuthContext, key idicates function
	    HashMap<Object, Object> map = new HashMap<Object, Object>();
	    map.put(PipeConstants.SECURITY_TOKEN,info);
	    helper.getSessionToken(map,info,clientSubject);
	    // helper returns token in map of msgInfo, using same key
	    Object o = info.getMap().get(PipeConstants.SECURITY_TOKEN);
	    if (o != null && o instanceof JAXBElement) {
		token = (JAXBElement) o;
	    }
	} catch(Exception e) {
	    if (e instanceof WSSecureConversationException) {
		throw (WSSecureConversationException) e;
	    } else {
		throw new WSSecureConversationException
		    ("Secure Conversation failure: ", e);
	    }
	} 
	return token;
    }
   
    @SuppressWarnings("unchecked")
    private Subject getClientSubject(Packet p) {

	Subject s = null;
	if (p != null) {
	    s = (Subject) 
		p.invocationProperties.get(PipeConstants.CLIENT_SUBJECT);
	}
	if (s == null) {
	    s = helper.getClientSubject();
            if (p != null) {
	        p.invocationProperties.put(PipeConstants.CLIENT_SUBJECT,s);
            }
	}
	return s;
    }
    
}
