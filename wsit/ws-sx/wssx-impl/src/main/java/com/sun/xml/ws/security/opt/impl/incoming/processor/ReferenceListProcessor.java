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

import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.Target;
import java.util.ArrayList;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class ReferenceListProcessor {
    
    ArrayList<String> refList = null;
    //EncryptionPolicy encPolicy = null;
    EncryptionPolicy.FeatureBinding fb = null;
    /** Creates a new instance of ReferenceListProcessor */
    public ReferenceListProcessor(EncryptionPolicy encPolicy) {
        //this.encPolicy = encPolicy;
        fb = (EncryptionPolicy.FeatureBinding) encPolicy.getFeatureBinding();
    }
    /**
     * processes the ReferenceList and sets the refList member
     * @param reader XMLStreamReader
     * @throws javax.xml.stream.XMLStreamException
     */
    public void process(XMLStreamReader reader) throws XMLStreamException{
        refList = new ArrayList<String>(2);
        if(StreamUtil.moveToNextStartOREndElement(reader)){
            while(reader.getEventType() != reader.END_DOCUMENT){
                if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
                    if(reader.getLocalName() == "DataReference" && reader.getNamespaceURI() == MessageConstants.XENC_NS){
                        String uri = reader.getAttributeValue(null,"URI");
                        if(uri.startsWith("#")){
                            refList.add(uri.substring(1));
                        }else{
                            refList.add(uri);
                        }
                        // for policy creation
                        Target target = new Target(Target.TARGET_TYPE_VALUE_URI, uri);
                        EncryptionTarget encTarget = new EncryptionTarget(target);
                        fb.addTargetBinding(encTarget);
                    }
                }
                if(_exit(reader)){
                    break;
                }
                reader.next();
                
                if(_exit(reader)){
                    break;
                }
            }
        }
    }
    
    public ArrayList<String> getReferences(){
        return refList;
    }
    
    public boolean _exit(XMLStreamReader reader){
        if(reader.getEventType() == XMLStreamReader.END_ELEMENT){
            if(reader.getLocalName() == "ReferenceList" && reader.getNamespaceURI() == MessageConstants.XENC_NS){
               return true;
            }
        }
        return false;
    }
}
