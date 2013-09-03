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
