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

/*
 * Attribute.java
 *
 * Created on August 21, 2005, 4:49 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.c14n;

import org.xml.sax.Attributes;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class Attribute   {
    int position = 0;
    Attributes attributes = null;
    
    /** Creates a new instance of Attribute */
    public Attribute () {
    }
    
    public void setPosition (int pos){
        this.position = pos;
    }
    public void setAttributes (Attributes attrs){
        this.attributes = attrs;
    }
    public String getLocalName (){
        return attributes.getLocalName (position);
    }
    
    public String getNamespaceURI(){
        return attributes.getURI (position);
    }
    
    public String getValue (){
        return attributes.getValue (position);
    }
    public int getPosition(){
        return position;
    }
}
