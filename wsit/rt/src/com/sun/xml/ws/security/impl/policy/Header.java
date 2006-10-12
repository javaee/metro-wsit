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

package com.sun.xml.ws.security.impl.policy;


import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Collection;

import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class Header extends PolicyAssertion implements com.sun.xml.ws.security.policy.Header{
    
    String name ="";
    String uri = "";
    int hashCode = 0;
    /**
     * Creates a new instance of Header
     */
    @Deprecated public Header(String localName , String uri) {
        Map<QName,String> attrs = this.getAttributes();
        attrs.put(NAME,localName);
        attrs.put(URI,uri);
    }
    
    public Header(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative)  throws PolicyException {
        super(name,nestedAssertions,nestedAlternative);
        
        
        String tmp = this.getAttributeValue(NAME);
        if(tmp != null){
            this.name = tmp;
        }
        
        this.uri = this.getAttributeValue(URI);
        
        if(uri == null || uri.length() == 0){
            throw new PolicyException("Namespace attribute is required under Header element ");
        }
        
    }
    
    public boolean equals(Object object){
        if(object instanceof Header){
            Header header = (Header)object;
            if(header.getLocalName() != null && header.getLocalName().equals(getLocalName())){
                if(header.getURI().equals(getURI())){
                    return true;
                }
            }
        }
        return false;
    }
    
    public int hashCode(){
        if(hashCode ==0){
            if(uri!=null){
                hashCode =uri.hashCode();
            }
            if(name !=null){
                hashCode =hashCode+name.hashCode();
            }
        }
        return hashCode;
    }
    
//    public QName getName(){
//        return Constants.HEADER_QNAME;
//    }
    
    public String getLocalName() {
        return name;
    }
    
    public String getURI() {
        return uri;
    }
}
