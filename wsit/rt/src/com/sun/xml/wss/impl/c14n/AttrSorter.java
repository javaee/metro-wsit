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
 * AttrSorter.java
 *
 * Created on August 21, 2005, 4:38 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.c14n;



/**
 *
 * @author K.Venugopal@sun.com
 */
public class AttrSorter implements java.util.Comparator{
    
    boolean namespaceSort = false;
    
    /** Creates a new instance of AttrSorter */
    public AttrSorter (boolean namespaceSort) {
        this.namespaceSort = namespaceSort;
    }
    
    
    public int compare (Object o1, Object o2) {
        if(namespaceSort){
            return sortNamespaces (o1,o2);
        }else{
            return sortAttributes (o1,o2);
        }
    }
    
    //double check;
    protected int sortAttributes (Object object, Object object0) {
        Attribute attr = (Attribute)object;
        Attribute attr0 = (Attribute)object0;
        String uri = attr.getNamespaceURI ();
        String uri0 = attr0.getNamespaceURI ();
        int result = uri.compareTo (uri0);
        if(result == 0){
            String lN = attr.getLocalName ();
            String lN0 = attr0.getLocalName ();
            result = lN.compareTo (lN0);
        }
        return result;
    }
    
    //double check;
    protected int sortNamespaces (Object object, Object object0) {
        AttributeNS attr = (AttributeNS)object;
        AttributeNS attr0 = (AttributeNS)object0;
        //assume namespace processing is on.
        String lN = attr.getPrefix ();
        String lN0 = attr0.getPrefix ();     
        return lN.compareTo (lN0);
    }
    
}
