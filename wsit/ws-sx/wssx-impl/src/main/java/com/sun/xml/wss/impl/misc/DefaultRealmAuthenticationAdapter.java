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

/*
 * DefaultRealmAuthenticationProvider.java
 *
 * Created on November 12, 2006, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.misc;


import com.sun.xml.wss.RealmAuthenticationAdapter;
import com.sun.xml.wss.WSITXMLFactory;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.HashMap;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.x500.X500Principal;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author kumar jayanti
 */
public class DefaultRealmAuthenticationAdapter extends RealmAuthenticationAdapter {
    
    private CallbackHandler gfCallbackHandler = null;
    private HashMap<String, String> tomcatUsersXML = null;
    
    private static DocumentBuilderFactory dbf = WSITXMLFactory.createDocumentBuilderFactory(WSITXMLFactory.DISABLE_SECURE_PROCESSING);
    
    private static String classname = "com.sun.enterprise.security.jmac.callback.ContainerCallbackHandler";
 
    /*
    static {
        dbf.setNamespaceAware(true);
    }*/
    
    /** Creates a new instance of DefaultRealmAuthenticationProvider */
    public DefaultRealmAuthenticationAdapter() {
        if (isGlassfish()) {
            gfCallbackHandler = this.loadGFHandler();
        } else if (isTomcat()) {
            populateTomcatUsersXML();     
        }
    }
    
    private boolean isGlassfish() {
        String val = System.getProperty("com.sun.aas.installRoot");
        if (val != null) {
            return true;
        }
        return false;
    }
    private boolean isTomcat() {
        String val = System.getProperty("catalina.home");
        String val1 = System.getProperty("com.sun.aas.installRoot");
        if ((val1 == null) && (val != null)) {
            return true;
        }
        return false;
    }
    
    private boolean authenticateFromTomcatUsersXML(
            final Subject callerSubject, final String username, final String password) 
            throws XWSSecurityException {
        if (tomcatUsersXML != null) {
            String pass = (String)tomcatUsersXML.get(username);
            if (pass == null) {
                return false;
            }
            if (pass.equals(password)) {
                //populate the subject
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    
                    public Object run() {
                        String x500Name = "CN=" + username;
                        Principal principal = new X500Principal(x500Name);
                        callerSubject.getPrincipals().add(principal);
//                        if (password != null) {
//                            callerSubject.getPrivateCredentials().add(password);
//                        }
                        return null; // nothing to return
                    }
                });
                return true;
            }else {
                return false;
            }
            
        } else {
            throw new XWSSecurityException(
                    "Internal Error: Username Authentication Failed: Could not Load/Locate tomcat-users.xml, Possible Cause is Application is Not Running on TOMCAT ?");       
        }
    }
    
    private boolean authenticateWithGFCBH(Subject callerSubject, String username, String password)throws XWSSecurityException {
        if (gfCallbackHandler != null) {
            char[] pwd = (password == null) ? null : password.toCharArray(); 
            PasswordValidationCallback pvCallback = 
                    new PasswordValidationCallback(callerSubject,username, pwd);
            Callback[] callbacks = new Callback[] { pvCallback };
            try {
               gfCallbackHandler.handle(callbacks);
            } catch (Exception e) {
               throw new XWSSecurityException(e);
            }

           // zero the password 
           if (pwd != null)
              pvCallback.clearPassword();

           boolean result = pvCallback.getResult();
           if (result) {
               //invoke the CallerPrincipalCallback
               CallerPrincipalCallback pCallback = new CallerPrincipalCallback(callerSubject, username);
               callbacks = new Callback[] { pCallback };
               try {
                   gfCallbackHandler.handle(callbacks);
               } catch (Exception e) {
                   throw new XWSSecurityException(e);
               }
               return result;
           } else {
               return result;
           }
        } else {
            throw new XWSSecurityException(
                    "Internal Error: Username Authentication Failed: Could not Locate/Load CallbackHandler: " + classname);       
        }
    }
    
    @Override
    public boolean authenticate(Subject callerSubject, String username, String password) throws XWSSecurityException {
        if (isGlassfish())  {
            return authenticateWithGFCBH(callerSubject, username, password);
            
        } else if (isTomcat()) {
            return authenticateFromTomcatUsersXML(callerSubject, username, password);
        }  else  {
            throw new XWSSecurityException("Error: Could not locate default username validator for the container");
        }
    }
    
    @Override
     public boolean authenticate(Subject callerSubject,  String username, String passwordDigest, String nonce, String created) throws XWSSecurityException {
        // this is the place from where i can go to GF JDBC Realm
         throw new XWSSecurityException("Not Yet Supported: Digest Authentication not yet supported in  DefaultRealmAuthenticationAdapter");
     }
       
    protected CallbackHandler loadGFHandler() {
        
        Class ret = null;
        try {
            
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                if (loader != null) {
                    ret = loader.loadClass(classname);
                }
            }catch(ClassNotFoundException e) {
              //  throw new RuntimeException(e);
            }
            
            if (ret == null) {
                // if context classloader didnt work, try this
                loader = this.getClass().getClassLoader();
                ret = loader.loadClass(classname);
            }
            
            if (ret != null) {
                CallbackHandler handler = (CallbackHandler)ret.newInstance();
                return handler;
            }
        } catch (ClassNotFoundException e) {
            // ignore
            //throw new RuntimeException(e);
            
        } catch(InstantiationException e) {
            //throw new RuntimeException(e);
        } catch(IllegalAccessException ex) {
            //throw new RuntimeException(ex);
        }
        //throw new RuntimeException("Internal Error: Could Not Load CallbackHandler Class: " + classname);
        return null;
    }

    private void populateTomcatUsersXML() {
        String catalinaHome = System.getProperty("catalina.home");
       
        String tomcat_user_xml = 
                catalinaHome + File.separator + "conf" + File.separator + "tomcat-users.xml";
       
        try {
            File tomcatUserXML = new File(tomcat_user_xml);
            if (!tomcatUserXML.exists()) {
                // this may not be a TOMCAT system although catalina.home is set.
                //TODO Issue a FINE LOG here
                return;
            }
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(tomcatUserXML);
            NodeList nl = doc.getElementsByTagName("user");
            tomcatUsersXML = new HashMap<String, String>();
            
            for (int i=0; i<nl.getLength(); i++) {
                Node n = nl.item(i);
                NamedNodeMap nmap = n.getAttributes();
                Node un = nmap.getNamedItem("username");
                if (un == null) {
                    //bundled tomcat uses "name instead of username"
                    un = nmap.getNamedItem("name");
                }
                Node pn = nmap.getNamedItem("password");
                tomcatUsersXML.put(un.getNodeValue(),pn.getNodeValue());
                if (MessageConstants.debug) {
                    System.out.println("Entered : U=" + un.getNodeValue() + ": P=" + pn.getNodeValue());
                }
            }
        }catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }catch (SAXException e) {
            throw new RuntimeException(e);
        }catch (IOException ie) {
            throw new RuntimeException(ie);
        }
    }
}
