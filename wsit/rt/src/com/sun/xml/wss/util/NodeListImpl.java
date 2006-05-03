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
 * NodeListImpl.java
 *
 * Created on March 31, 2006, 8:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.util;

import org.w3c.dom.Node;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.NodeList;

/**
 *
 * @author ashutosh.shahi@sun.com
 */
public class NodeListImpl implements NodeList{
    
    private List<Node> nodes;
    
    /**
     * Creates a new instance of NodeListImpl
     */
    public NodeListImpl() {
        nodes = new ArrayList<Node>();
    }
    
    /**
     * get the size of the nodeList
     */
    public int getLength(){
        return nodes.size();
    }
    
    /**
     * get the ith item from NodeList
     */
    public Node item(int i){
        return nodes.get(i);
    }
    
    /**
     * add node to the end of NodeList
     */
    public void add(Node node){
        nodes.add(node);
    }
    
    public void merge(NodeList nodeList){
        for(int i = 0; i < nodeList.getLength(); i++){
            nodes.add(nodeList.item(i));
        }
    }
    
}
