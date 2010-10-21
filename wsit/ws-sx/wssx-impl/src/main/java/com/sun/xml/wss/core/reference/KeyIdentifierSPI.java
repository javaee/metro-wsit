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

package com.sun.xml.wss.core.reference;

import java.security.cert.X509Certificate;

public abstract class KeyIdentifierSPI {
    
    public static final String vmVendor = System.getProperty("java.vendor.url");
    public static final String sunVmVendor = "http://java.sun.com/";
    public static final String ibmVmVendor = "http://www.ibm.com/";
    public static final boolean isSunVM = sunVmVendor.equals(vmVendor) ? true: false;
    public static final boolean isIBMVM = ibmVmVendor.equals(vmVendor) ? true : false;
    
    private static final String sunKeyIdentifierSPIClass = "com.sun.wsit.security.SunKeyIdentifierSPI";
    private static final String ibmKeyIdentifierSPIClass = "com.sun.wsit.security.IBMKeyIdentifierSPI";
    private static final String sunKeyIdentifierImplClass="sun.security.x509.KeyIdentifier";
    private static final String ibmKeyIdentifierImplClass="com.ibm.security.x509.KeyIdentifier";
    
    protected static final String SUBJECT_KEY_IDENTIFIER_OID = "2.5.29.14";
    
    private static final KeyIdentifierSPI instance;
    
    static  {
    
       if (isSunVM) {
           instance = loadClass(sunKeyIdentifierSPIClass);
       } else if (isIBMVM) {
           instance = loadClass(ibmKeyIdentifierSPIClass);
       } else {
            if (testClassExist(sunKeyIdentifierImplClass)) {
               instance = loadClass(sunKeyIdentifierSPIClass);
           } else if (testClassExist(ibmKeyIdentifierImplClass)) {
               instance = loadClass(ibmKeyIdentifierSPIClass);    
           } else {
               throw new UnsupportedOperationException("KeyIdentifierSPI Error : No known implementation for VM: " + vmVendor);
           }
       }  
    }
            
            
    /** Creates a new instance of KeyIdentifierSPI */
    protected KeyIdentifierSPI() {
    }

    /**
     *Return the JRE vendor specific implementation of this SPI
     */
    public static KeyIdentifierSPI getInstance() {
        return instance;
    }

    private static boolean testClassExist(String className) {
        try {
            Class spiClass=null;
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                spiClass = classLoader.loadClass(className);
            }
            if (spiClass == null) {
                spiClass = Class.forName(className);
            }
            return (spiClass != null) ? true : false;
        } catch (ClassNotFoundException x) {
            return false;
        } catch (Exception x) {
            return false;
        }
    }
    
    private static KeyIdentifierSPI loadClass(String className) {
        try {
            Class spiClass=null;
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                spiClass = classLoader.loadClass(className);
            }
            if (spiClass == null) {
                spiClass = Class.forName(className);
            }
            return (KeyIdentifierSPI)spiClass.newInstance();
        } catch (ClassNotFoundException x) {
            throw new RuntimeException(
                    "The KeyIdentifierSPI class: " + className + " specified was not found", x);
        } catch (Exception x) {
            throw new RuntimeException(
                    "The KeyIdentifierSPI class: " + className + " could not be instantiated ", x);
        }
    }
      
    public abstract byte[] getSubjectKeyIdentifier(X509Certificate cert) 
       throws KeyIdentifierSPIException;
    
    protected static final class KeyIdentifierSPIException extends Exception {
        
        public KeyIdentifierSPIException(Exception ex) {
            this.initCause(ex);
        }
        
    }
    
}
