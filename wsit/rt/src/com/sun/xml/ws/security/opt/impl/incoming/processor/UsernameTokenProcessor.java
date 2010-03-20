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

package com.sun.xml.ws.security.opt.impl.incoming.processor;

import com.sun.xml.wss.impl.MessageConstants;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */

public class UsernameTokenProcessor implements StreamFilter{
    
    String username = null;
    String password = null;
    String passwordDigest = null;
    String passwordType = null;
    String nonce = null;
    String created = null;
    String   Iterations ;
    String Salt;
    String currentElement = "";
    
    private static String USERNAME = "Username".intern();
    private static String PASSWORD = "Password".intern();
    private static String NONCE = "Nonce".intern();
    private static String CREATED = "Created".intern();
    private static String SALT = "Salt".intern();
    private static String ITERATIONS = "Iterations".intern();
    
    /** Creates a new instance of UsernameTokenProcessor */
    public UsernameTokenProcessor() {
    }
    /**
     * parses the UsernameToken and sets the members of the class
     * @param reader XMLStreamReader
     * @return boolean
     */
    public boolean accept(XMLStreamReader reader) {
        
        if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
            
            if("Username".equals(reader.getLocalName())){
                currentElement = USERNAME;
            } else if("Password".equals(reader.getLocalName())){
                currentElement = PASSWORD;
                passwordType = reader.getAttributeValue(null, "Type");
            } else if("Nonce".equals(reader.getLocalName())){
                currentElement = NONCE;
            } else if("Created".equals(reader.getLocalName())){
                currentElement = CREATED;
            }else if("Salt".equals(reader.getLocalName())){
                currentElement = SALT;
            }else if("Iterations".equals(reader.getLocalName())){
                currentElement = ITERATIONS;
            }
        }
        
        if(reader.getEventType() == XMLStreamReader.CHARACTERS){
            if(currentElement == USERNAME){
                username = reader.getText();
                currentElement = "";
            } else if(currentElement == PASSWORD){
                if(MessageConstants.PASSWORD_DIGEST_NS.equals(passwordType)){
                    passwordDigest = reader.getText();
                } else{
                    password = reader.getText();
                }
                currentElement = "";
            } else if(currentElement == NONCE){
                nonce = reader.getText();
                currentElement = "";
            } else if(currentElement == CREATED){
                created = reader.getText();
                currentElement = "";
            }else if (currentElement == SALT){
                 Salt = reader.getText();
                 currentElement = "";
            }else if (currentElement == ITERATIONS){
                 Iterations = reader.getText();
                 currentElement = "";
            }
        }
        return true;
    }
    
    public String getUsername(){
        return username;
    }
    
    public String getPassword(){
        return password;        
    }
    
    public String getPasswordDigest(){
        return passwordDigest;
    }
    
    public String getPasswordType(){
        return passwordType;
    }
    
    public String getNonce(){
        return nonce;
    }
    
    public String getCreated(){
        return created;
    }
    /**
     * returns the 16 byte salt for creating password derived keys
     * @return Salt String
     */
    public String getSalt(){
        return Salt;
    }
    /**
     *
     * @return Iterations String
     */
    public String getIterations(){
        return Iterations;
    }
}

