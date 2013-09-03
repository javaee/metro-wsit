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

/*
 * ReflectionUtil.java
 *
 * Created on August 13, 2007, 2:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.misc;

import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

 /**
  * Reflection utilities wrapper
  */
public  class ReflectionUtil {
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /**
     * Reflectively invokes specified method on the specified target
     */
    public static <T> T invoke(final Object target, final String methodName,
            final Class<T> resultClass, final Object... parameters) throws XWSSecurityRuntimeException {
        Class[] parameterTypes;
        if (parameters != null && parameters.length > 0) {
            parameterTypes = new Class[parameters.length];
            int i = 0;
            for (Object parameter : parameters) {
                parameterTypes[i++] = parameter.getClass();
            }
        } else {
            parameterTypes = null;
        }
        
        return invoke(target, methodName, resultClass, parameters, parameterTypes);
    }
    
    /**
     * Reflectively invokes specified method on the specified target
     */
    public static <T> T invoke(final Object target, final String methodName, final Class<T> resultClass,
            final Object[] parameters, final Class[] parameterTypes) throws XWSSecurityRuntimeException {
        try {
            final Method method = target.getClass().getMethod(methodName, parameterTypes);
            final Object result = method.invoke(target, parameters);
            
            return resultClass.cast(result);
        } catch (IllegalArgumentException e) {
            log.log(Level.SEVERE, "WSS0810.method.invocation.failed" , e);
            throw e;
        } catch (InvocationTargetException e) {
            log.log(Level.SEVERE, "WSS0810.method.invocation.failed" , e);
            throw new XWSSecurityRuntimeException(e);
        } catch (IllegalAccessException e) {
            log.log(Level.SEVERE, "WSS0810.method.invocation.failed" , e);
            throw new XWSSecurityRuntimeException(e);
        } catch (SecurityException e) {
            log.log(Level.SEVERE, "WSS0810.method.invocation.failed" , e);
            throw e;
        } catch (NoSuchMethodException e) {
            log.log(Level.SEVERE, "WSS0810.method.invocation.failed" , e);
            throw new XWSSecurityRuntimeException(e);
        }
    }
    
    
}
