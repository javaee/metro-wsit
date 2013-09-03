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

import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.wss.BasicSecurityProfile;
import javax.xml.namespace.QName;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class TimestampProcessor implements StreamFilter{
    
    private String created = null;
    private String expires = null;
    private String currentElement = "";
    private JAXBFilterProcessingContext context = null;
    private static String EXPIRES = "Expires".intern();
    private static String CREATED = "Created".intern();
    /** Creates a new instance of TimestampProcessor */
    public TimestampProcessor(JAXBFilterProcessingContext ctx) {
        this.context = ctx;
    }
    
    public boolean accept(XMLStreamReader reader) {
        if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
            if("Created".equals(reader.getLocalName())){
                currentElement = CREATED;
                if(context.isBSP() && created != null){
                    BasicSecurityProfile.log_bsp_3203();
                }
                if(context.isBSP() && hasValueType(reader)){
                    BasicSecurityProfile.log_bsp_3225();
                }
                
            } else if("Expires".equals(reader.getLocalName())){
                if(context.isBSP() && expires != null){
                    BasicSecurityProfile.log_bsp_3224();
                }
                if(context.isBSP() && created == null){
                    BasicSecurityProfile.log_bsp_3221();
                }
                
                if(context.isBSP() && hasValueType(reader)){
                    BasicSecurityProfile.log_bsp_3226();
                }
                currentElement = EXPIRES;
            }else{
                //throw Unsupportedexception                
//                if(context.isBSP() && ! "Timestamp".equals(reader.getLocalName())){
//                    BasicSecurityProfile.log_bsp_3222(reader.getLocalName());
//                }
            }
        }
        
        if(reader.getEventType() == XMLStreamReader.CHARACTERS){
            if(currentElement == CREATED){
                created = reader.getText();
                currentElement = "";
            }else if(currentElement == EXPIRES){
                expires = reader.getText();
                currentElement = "";
            }
        }
        return true;
    }
    
    public String getCreated(){
        return created;
    }
    
    public String getExpires(){
        return expires;
    }
    
    
    private boolean hasValueType(XMLStreamReader reader){
        for(int i=0;i<reader.getAttributeCount();i++){
            QName name = reader.getAttributeName(i);
            if(name != null && "ValueType".equals(name.getLocalPart())){
                return true;
            }
        }
        return false;
    }
}
