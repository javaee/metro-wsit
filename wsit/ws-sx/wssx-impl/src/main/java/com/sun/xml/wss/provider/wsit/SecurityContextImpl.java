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

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.security.spi.SecurityContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.security.auth.Subject;

/**
 *Provides a Default Implementation (tailored for GlassFish)
 * of the SecurityContext interface
 */
public class SecurityContextImpl implements SecurityContext {

    private static final String GF_SEC_CONTEXT="com.sun.enterprise.security.SecurityContext";
    private Class c = null;
    private Method getCurrent = null;
    private Method serverGenCred =null;
    private Method getSubject = null;
    private Constructor ctor = null;
    @SuppressWarnings("unchecked")
    public SecurityContextImpl() {
        try {
            Class[] params = new Class[]{};
            c = Class.forName(GF_SEC_CONTEXT, true, Thread.currentThread().getContextClassLoader());
            getCurrent = c.getMethod("getCurrent", params);
            serverGenCred = c.getMethod("didServerGenerateCredentials", params);
            getSubject = c.getMethod("getSubject", params);
            params = new Class[]{Subject.class};
            ctor = c.getConstructor(params);
        } catch (NoSuchMethodException ex) {
            //Logger.getLogger(SecurityContextImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            //Logger.getLogger(SecurityContextImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            //Logger.getLogger(SecurityContextImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public Subject getSubject() {
        Subject s = null;        
        Object[] args = new Object[]{};
        try {
            
            if(getCurrent == null || serverGenCred == null ||getSubject == null) {
                return null;
            }
            
            Object currentSC = getCurrent.invoke(null, args);
            if (currentSC == null) {
                return null;
            }
            Boolean didServerGenerateCredentials = (Boolean)serverGenCred.invoke(currentSC, args);
            if (!didServerGenerateCredentials.booleanValue()) {
                s = (Subject)getSubject.invoke(currentSC, args);
            }
            return s;
          
        } catch (IllegalAccessException ex) {
            return null;
        } catch (IllegalArgumentException ex) {
            return null;
        } catch (InvocationTargetException ex) {
            return null;
        } catch (SecurityException ex) {
            return null;
        }
    }
    
    public void setSubject(Subject subject) {
        //SecurityContext sC = new SecurityContext(s);
	//SecurityContext.setCurrent(sC);
        Class[] params = null;
        Object[] args = null;
        try {
            args = new Object[] {subject};
            if (ctor == null) {
                //TODO: log warning here
                return;
            }
            Object secContext = ctor.newInstance(args);
            params = new Class[]{secContext.getClass()};
            @SuppressWarnings("unchecked")
            Method setCurrent = c.getMethod("setCurrent", params);
            args = new Object[]{secContext};
            if (setCurrent == null) {
                //TODO: log warning here
                return;
            }
            setCurrent.invoke(null, args);
        } catch (InstantiationException ex) {
            //ignore
        } catch (IllegalAccessException ex) {
            //ignore
        } catch (IllegalArgumentException ex) {
            //ignore
        } catch (InvocationTargetException ex) {
            //ignore
        } catch (NoSuchMethodException ex) {
            //ignore
        } catch (SecurityException ex) {
            //ignore
        }
    }


}
