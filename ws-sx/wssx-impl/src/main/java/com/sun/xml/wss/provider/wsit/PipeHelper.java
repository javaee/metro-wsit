/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.wss.provider.wsit;
import com.sun.xml.ws.security.spi.SecurityContext;
import com.sun.xml.ws.api.server.WSEndpoint;

import java.security.AccessController;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ClientAuthContext;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.Module;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PipeHelper extends ConfigHelper {
//    private static AuditManager auditManager =
//            AuditManagerFactory.getAuditManagerInstance();
//
//    protected static final LocalStringManagerImpl localStrings = 
//        new LocalStringManagerImpl(PipeConstants.class);
    private SEIModel seiModel;
    private SOAPVersion soapVersion;
    private static final String SECURITY_CONTEXT_PROP="META-INF/services/com.sun.xml.ws.security.spi.SecurityContext";
    private Class secCntxt = null;
    private SecurityContext context = null;
    
    public PipeHelper(String layer, Map<Object, Object> map, CallbackHandler cbh) {
        init(layer, getAppCtxt(map), map, cbh);

	this.seiModel = (SEIModel) map.get(PipeConstants.SEI_MODEL);
        WSBinding binding = (WSBinding)map.get(PipeConstants.BINDING);
        if (binding == null) {
            WSEndpoint endPoint = (WSEndpoint)map.get(PipeConstants.ENDPOINT);
            if (endPoint != null) {
                binding = endPoint.getBinding();
            }
        }
        this.soapVersion = (binding != null) ? binding.getSOAPVersion(): SOAPVersion.SOAP_11;
        
         URL url = loadFromClasspath(SECURITY_CONTEXT_PROP);
        if (url != null) {
            InputStream is = null;
            try {
                is = url.openStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int val = is.read();
                while (val != -1) {
                    os.write(val);
                    val = is.read();
                }
                String className = os.toString();
                secCntxt = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
                 if (secCntxt != null) {
                     context = (SecurityContext) secCntxt.newInstance();
                 }
            } catch (Exception e) {
                throw new WebServiceException(e);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    Logger.getLogger(PipeHelper.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        }
   }

    @Override
    public ClientAuthContext getClientAuthContext(MessageInfo info, Subject s) 
    throws AuthException {
	ClientAuthConfig c = (ClientAuthConfig)getAuthConfig(false);
	if (c != null) {
            addModel(info, map);
	    return c.getAuthContext(c.getAuthContextID(info),s,map);
	}
	return null;
    }

    @Override
    public ServerAuthContext getServerAuthContext(MessageInfo info, Subject s) 
    throws AuthException {
	ServerAuthConfig c = (ServerAuthConfig)getAuthConfig(true);
	if (c != null) {
            addModel(info, map);
	    return c.getAuthContext(c.getAuthContextID(info),s,map);
	}
	return null;
    }

     public static URL loadFromClasspath(final String configFileName) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            return ClassLoader.getSystemResource(configFileName);
        } else {
            return loader.getResource(configFileName);
        }
    }
     
    public Subject getClientSubject() {

        Subject s = null;
        if (context != null) {
            s = context.getSubject();
        }

        if (s == null) {
            s = Subject.getSubject(AccessController.getContext());
        }
        if (s == null) {
            s = new Subject();
        }
        return s;
    }

    public void getSessionToken(Map<Object, Object> m, 
				MessageInfo info, 
				Subject s) throws AuthException {
	ClientAuthConfig c = (ClientAuthConfig) getAuthConfig(false);    
	if (c != null) {
	    m.putAll(map);
            addModel(info, map);
	    c.getAuthContext(c.getAuthContextID(info),s,m);
	}
	return;
    }

   	
    public Object getModelName() { 
 	WSDLPort wsdlModel = (WSDLPort) getProperty(PipeConstants.WSDL_MODEL);
 	return (wsdlModel == null ? "unknown" : wsdlModel.getName());
    }
  
    // always returns response with embedded fault
    //public static Packet makeFaultResponse(Packet response, Throwable t) {
    public Packet makeFaultResponse(Packet response, Throwable t) {
	// wrap throwable in WebServiceException, if necessary
	if (!(t instanceof WebServiceException)) {
	    t = (Throwable) new WebServiceException(t);
	}
 	if (response == null) {
 	    response = new Packet();
  	} 
	// try to create fault in provided response packet, if an exception
	// is thrown, create new packet, and create fault in it.
	try {
	    return response.createResponse(Messages.create(t, this.soapVersion));
	} catch (Exception e) {
	    response = new Packet();
	}
 	return response.createResponse(Messages.create(t, this.soapVersion));
    }
    
    public boolean isTwoWay(boolean twoWayIsDefault, Packet request) { 
 	boolean twoWay = twoWayIsDefault;
 	Message m = request.getMessage();
 	if (m != null) {
	    WSDLPort wsdlModel =
		(WSDLPort) getProperty(PipeConstants.WSDL_MODEL);
	    if (wsdlModel != null) {
		twoWay = (m.isOneWay(wsdlModel) ? false : true);
	    }
	}
 	return twoWay;
    }
 
    // returns empty response if request is determined to be one-way
    public Packet getFaultResponse(Packet request, Packet response, 
	Throwable t) {
	boolean twoWay = true;
	try {
	    twoWay = isTwoWay(true,request);
	} catch (Exception e) {
	    // exception is consumed, and twoWay is assumed
 	} 
	if (twoWay) {
	    return makeFaultResponse(response,t);
 	} else {
	    return new Packet();
	}
    }
 
    @Override
    public void disable() {
	listenerWrapper.disableWithRefCount();
    }
    
    
    private static String getAppCtxt(Map map) {
        String rvalue = null;
        WSEndpoint wse = 
            (WSEndpoint) map.get(PipeConstants.ENDPOINT);
        Container container = (Container)map.get(PipeConstants.CONTAINER);
        // endpoint
        if (wse != null) {
            if (container != null) {
                Module module = container.getSPI(Module.class);
                if (module != null) {
                    List<BoundEndpoint> beList = module.getBoundEndpoints();
                    for (BoundEndpoint be : beList) {
                        WSEndpoint wsep = be.getEndpoint();
                        if (wse.getPortName().equals(wsep.getPortName())) {
                            rvalue = be.getAddress().toASCIIString();
                        }
                    }
                }
            }
            //fallback to default
            if (rvalue == null) {
                rvalue = wse.getPortName().toString();
            }
        } else {
             // client reference
            WSService service = (WSService)map.get(PipeConstants.SERVICE);
            if (service != null) {
                rvalue = service.getServiceName().toString();
            } 
            
        }
        return rvalue;
    }

    @SuppressWarnings("unchecked")
    private static void addModel(MessageInfo info, Map<Object, Object> map) {
        Object model = map.get(PipeConstants.WSDL_MODEL);
        if (model != null) {
            info.getMap().put(PipeConstants.WSDL_MODEL,model);
        }
    }
     private static String getServerName(WSEndpoint wse) {
         //TODO: FIXME
        return "localhost";
    }
     
    private static String getEndpointURI(WSEndpoint wse) {
        return wse.getPort().getAddress().getURI().toASCIIString();
    }
    
     public void authorize(Packet request) {

        // SecurityContext constructor should set initiator to
        // unathenticated if Subject is null or empty
        Subject s = (Subject) request.invocationProperties.get(PipeConstants.CLIENT_SUBJECT);
        if (s == null) {
            s = Subject.getSubject(AccessController.getContext());
        }
        //TODO: actual container authorization checks to go here
        if (context != null) {
            context.setSubject(s);
        }
    }


}
