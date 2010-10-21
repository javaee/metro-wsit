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

/*
 * TestUtil.java
 *
 * Created on April 7, 2006, 12:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.util;

import javax.xml.soap.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author ashutosh.shahi@sun.com
 */
public class TestUtil {
    
    /** Creates a new instance of TestUtil */
    public TestUtil() {
    }
    @SuppressWarnings("unchecked")
    public static void saveMimeHeaders(SOAPMessage msg, String fileName)
    throws IOException {
                                                                                                                                                 
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
                                                                                                                                                 
        Hashtable hashTable = new Hashtable();
        MimeHeaders mimeHeaders = msg.getMimeHeaders();
        Iterator iterator = mimeHeaders.getAllHeaders();
                                                                                                                                                 
        while(iterator.hasNext()) {
           
            MimeHeader mimeHeader = (MimeHeader) iterator.next();
            
            hashTable.put(mimeHeader.getName(), mimeHeader.getValue());
        }
                                                                                                                                                 
        oos.writeObject(hashTable);
        oos.flush();
        oos.close();
                                                                                                                                                 
        fos.flush();
        fos.close();
    }

    public  static SOAPMessage constructMessage(String mimeHdrsFile, String msgFile)
    throws Exception {
        SOAPMessage message;
                                                                                                                                                 
        MimeHeaders mimeHeaders = new MimeHeaders();
        FileInputStream fis = new FileInputStream(msgFile);
                                                                                                                                                 
        ObjectInputStream ois = new ObjectInputStream(
        new FileInputStream(mimeHdrsFile));
        Hashtable hashTable = (Hashtable) ois.readObject();
        ois.close();
                                                                                                                                                 
        if(hashTable.isEmpty()) {
          //  System.out.println("MimeHeaders Hashtable is empty");
        } else {
            for(int i=0; i < hashTable.size(); i++) {
                Enumeration keys = hashTable.keys();
                Enumeration values = hashTable.elements();
                while (keys.hasMoreElements() && values.hasMoreElements()) {
                    String name = (String) keys.nextElement();
                    String value = (String) values.nextElement();
                    mimeHeaders.addHeader(name, value);
                }
            }
        }
                                                                                                                                                 
        MessageFactory messageFactory = MessageFactory.newInstance();
        message = messageFactory.createMessage(mimeHeaders, fis);
                                                                                                                                                 
        message.saveChanges();
                                                                                                                                                 
        return message;
    }
    
}
