/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.wss.impl.transform;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamReader;


/**
 *
 * @author K.Venugopal@sun.com
 */
public class EnvelopedSignatureFilter implements StreamFilter{
    
    private static final String _SIGNATURE = "Signature";
    private static final String _NAMESPACE_URI ="http://www.w3.org/2000/09/xmldsig#";
    
    private boolean _skipSignatureElement = false;
    private boolean _skipDone= false;
    /** Creates a new instance of EnvelopedSignatureTransformImpl */
    public EnvelopedSignatureFilter(){
        
    }
    
    public boolean accept(XMLStreamReader reader) {
        if(_skipDone){
            return false;
        }else if(!_skipSignatureElement){
            if(reader.getEventType() == XMLStreamReader.START_ELEMENT ){
                if(_SIGNATURE.equals(reader.getLocalName()) && _NAMESPACE_URI.equals(reader.getNamespaceURI()) ){
                    this._skipSignatureElement = true;
                    return true;
                }
            }
        }else{
            if(reader.getEventType() == XMLStreamReader.END_ELEMENT){
                if(_SIGNATURE.equals(reader.getLocalName()) && _NAMESPACE_URI.equals(reader.getNamespaceURI()) ){
                    this._skipSignatureElement = false;
                    this._skipDone = true;
                    return true;
                }
            }
            return true;
        }
        return false;
    }
    
}
