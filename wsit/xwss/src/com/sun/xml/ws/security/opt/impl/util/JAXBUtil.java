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
 * JAXBUtil.java
 *
 * Created on July 20, 2006, 3:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.opt.impl.util;

import com.sun.xml.ws.api.SOAPVersion;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class JAXBUtil {
    public static final WSSNamespacePrefixMapper prefixMapper11 = new WSSNamespacePrefixMapper();
    public static final WSSNamespacePrefixMapper prefixMapper12 = new WSSNamespacePrefixMapper(true);
    private static ThreadLocal<WeakReference<JAXBContext>> jc = new ThreadLocal<WeakReference<JAXBContext>>();
    private static  JAXBContext jaxbContext;
    static {
        try {
            //JAXB might access private class members by reflection so 
            //make it JAXBContext privileged
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    jaxbContext = JAXBContext.newInstance(
                            "com.sun.xml.ws.security.opt.crypto.dsig:com.sun.xml.ws.security.opt.crypto.dsig.keyinfo:com.sun.xml.security.core.dsig:com.sun.xml.security.core.xenc:" +
                            "com.sun.xml.ws.security.opt.impl.keyinfo:com.sun.xml.ws.security.opt.impl.reference:" +
                            "com.sun.xml.ws.security.secext10:com.sun.xml.ws.security.wsu10:com.sun.xml.ws.security.secext11:" +
                            "com.sun.xml.ws.security.secconv.impl.bindings:" +
                            "com.sun.xml.ws.security.secconv.impl.wssx.bindings:com.sun.xml.security.core.ai");
                    return null;
                }
            });
        }catch (Exception je) {
            throw new WebServiceException(je);
        }
    }
    
    public static JAXBContext getJAXBContext(){
        return jaxbContext;
    }
    
    
    public static Marshaller createMarshaller(SOAPVersion soapVersion)throws JAXBException {
        try{
            Marshaller marshaller = jaxbContext.createMarshaller();
            if(SOAPVersion.SOAP_11 == soapVersion){
                marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper11);
            }else{
                marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper12);
            }
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT,true);
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", false);
            return marshaller;
        }catch(javax.xml.bind.PropertyException pe){
            throw new JAXBException("Error occurred while setting security marshaller properties",pe);
        }
       
    }
      public static void setSEIJAXBContext(JAXBContext context){
        jc.set(new WeakReference<JAXBContext>(context));
    }

    public static JAXBContext getSEIJAXBContext(){
        return jc.get().get();
    }
    
}
