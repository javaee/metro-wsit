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

package com.sun.xml.xwss;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubelineAssembler;
import com.sun.xml.ws.api.pipe.TubelineAssemblerFactory;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.ws.api.server.Container;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 *
 * 
 */
public class XWSSTubelineAssemblerFactory extends TubelineAssemblerFactory {

    private static final String SERVLET_CONTEXT_CLASSNAME = "javax.servlet.ServletContext";   
    private static final String addrVersionClass = "com.sun.xml.ws.api.addressing.AddressingVersion";
    private static final boolean disable;

    static  {
       disable = Boolean.getBoolean("DISABLE_XWSS_SECURITY");
    }
    private static class XWSSTubelineAssembler implements TubelineAssembler {
        
        private final BindingID bindingId;

        XWSSTubelineAssembler(final BindingID bindingId) {
            this.bindingId = bindingId;
        }

        @NotNull
        public Tube createClient(@NotNull ClientTubeAssemblerContext context) {
            
            Tube p = context.createTransportTube();
            if (isSecurityConfigPresent(context)) {
                p = initializeXWSSClientTube(
                        context.getWsdlModel(), context.getService(), context.getBinding(), p);
            }
            
            p = context.createClientMUTube(p);
            p = context.createHandlerTube(p);
            // check for WS-Addressing
            if (isAddressingEnabled(context.getWsdlModel(), context.getBinding())) {
                p = context.createWsaTube(p);
            }
            
            return p;
        }

        @NotNull
        public Tube createServer(@NotNull ServerTubeAssemblerContext context) {
            Tube p = context.getTerminalTube();
            p = context.createHandlerTube(p);
            p = context.createServerMUTube(p);
            p = context.createMonitoringTube(p);
            
            // check for WS-Addressing
            if (isAddressingEnabled( context.getWsdlModel(), context.getEndpoint().getBinding())) {
                p = context.createWsaTube(p);
            }
            //look for XWSS 2.0 Style Security
            if (isSecurityConfigPresent(context)) {
                p = initializeXWSSServerTube(context.getEndpoint(), context.getWsdlModel(), p);
            }
            
            return p;
        }
        
         private boolean isAddressingEnabled(WSDLPort port, WSBinding binding) {
            //JAXWS 2.0 does not have AddressingVersion
            Class<?> clazz = null;
            try {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(addrVersionClass);
            } catch (ClassNotFoundException ex) {
                return false;
            }
            if (clazz != null) {
                try {
                    Method meth = clazz.getMethod("isEnabled", WSBinding.class);
                    Object result = meth.invoke(null, binding);
                    if (result instanceof Boolean) {
                        boolean ret = ((Boolean)result).booleanValue();
                        return ret;
                    }
                } catch (IllegalAccessException ex) {
                    throw new WebServiceException(ex);
                } catch (IllegalArgumentException ex) {
                    throw new WebServiceException(ex);
                } catch (InvocationTargetException ex) {
                    throw new WebServiceException(ex);
                } catch (NoSuchMethodException ex) {
                    throw new WebServiceException(ex);
                } catch (SecurityException ex) {
                    throw new WebServiceException(ex);
                }
            }
//            if (com.sun.xml.ws.api.addressing.AddressingVersion.isEnabled(binding)) {
//                return true;
//            }
            return false;
        }

        private static boolean isSecurityConfigPresent(ClientTubeAssemblerContext context) {
            
            //look for XWSS 2.0 style config file in META-INF classpath
            String configUrl = "META-INF/client_security_config.xml";
            URL url = SecurityUtil.loadFromClasspath(configUrl);
            if (url != null) {
                return true;
            }
            //returning true by default for now, because the Client Side Security Config is
            //only accessible as a Runtime Property on BindingProvider.RequestContext
            //With Metro 2.0 provide a way of disabling the default rule above and one would need to
            //set System Property DISABLE_XWSS_SECURITY to disable the client pipeline.
            if (disable) {
                return false;
            }
            return true;
        }
        
        private static boolean isSecurityConfigPresent(ServerTubeAssemblerContext context) {
            
            QName serviceQName = context.getEndpoint().getServiceName();
            //TODO: not sure which of the two above will give the service name as specified in DD
            String serviceLocalName = serviceQName.getLocalPart();
            Container container = context.getEndpoint().getContainer();
            
            Object ctxt = null;
            if (container != null) {
                try {
                    final Class<?> contextClass = Class.forName(SERVLET_CONTEXT_CLASSNAME);
                    ctxt = container.getSPI(contextClass);
                } catch (ClassNotFoundException e) {
                    //log here at FINE Level : that the ServletContext was not found
                }
            }
            String serverName = "server";
            if (ctxt != null) {
                String serverConfig = "/WEB-INF/" + serverName + "_" + "security_config.xml";
                URL url =  SecurityUtil.loadFromContext(serverConfig, ctxt);
                
                if (url == null) {
                    serverConfig = "/WEB-INF/" + serviceLocalName + "_" + "security_config.xml";
                    url = SecurityUtil.loadFromContext(serverConfig, ctxt);
                }
                
                if (url != null) {
                    return true;
                }
            } else {
                //this could be an EJB or JDK6 endpoint
                //so let us try to locate the config from META-INF classpath
                String serverConfig = "META-INF/" + serverName + "_" + "security_config.xml";
                URL url = SecurityUtil.loadFromClasspath(serverConfig);
                if (url == null) {
                    serverConfig = "META-INF/" + serviceLocalName + "_" + "security_config.xml";
                    url = SecurityUtil.loadFromClasspath(serverConfig);
                }
                
                if (url != null) {
                    return true;
                }
            }
            return false;
        }
        
        private static Tube initializeXWSSClientTube(WSDLPort prt, WSService svc, WSBinding bnd, Tube nextP) {
            Tube ret = new XWSSClientTube(prt,svc, bnd, nextP);
            return ret;
        }
        
        private static Tube initializeXWSSServerTube(WSEndpoint epoint, WSDLPort prt, Tube nextP) {
            Tube ret = new XWSSServerTube(epoint, prt, nextP);
            return ret;
        }
    }
    
    @Override
    public TubelineAssembler doCreate(BindingID bindingId) {
        return new XWSSTubelineAssembler(bindingId);
    }

}
