
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

package com.sun.xml.wss.impl.transform;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.crypto.Data;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

/*
 * author K.Venugopal@sun.com
 */

public class STRTransformImpl {
    private static Logger logger = Logger.getLogger(LogDomainConstants.IMPL_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_SIGNATURE_DOMAIN_BUNDLE);
    
    protected static Data transform(Data data,XMLCryptoContext context,java.io.OutputStream outputStream)throws javax.xml.crypto.dsig.TransformException{
        try{
            Set nodeSet = getNodeSet(data);
            if(outputStream == null){
                ByteArrayOutputStream bs =   new ByteArrayOutputStream();
                new Canonicalizer20010315ExclOmitComments().engineCanonicalizeXPathNodeSet(nodeSet, "",bs,context);
                OctetStreamData osd =  new OctetStreamData(new ByteArrayInputStream(bs.toByteArray()));
                bs.close();
                return osd;
            }else{
                new Canonicalizer20010315ExclOmitComments().engineCanonicalizeXPathNodeSet(nodeSet, "",outputStream,context);
            }
            return null;
        }catch(Exception ex){
            logger.log(Level.SEVERE,"WSS1322.str_transform",ex);
        }
        return null;
    }
    
    private static Set getNodeSet(Data data)throws javax.xml.crypto.dsig.TransformException{
        HashSet nodeSet = new HashSet();
        if(data instanceof NodeSetData){
            Iterator it = ((NodeSetData)data).iterator();
            while(it.hasNext()){
                Object node = it.next();
                if(MessageConstants.debug){
                    logger.log(Level.FINEST,"Data is "+node);
                }
                nodeSet.add(node);
            }
        }else if(data instanceof OctetStreamData ){
            try{
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
                //new com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl();
                factory.setNamespaceAware(true);
                Document doc = factory.newDocumentBuilder().parse(((OctetStreamData)data).getOctetStream());
                toNodeSet(doc,nodeSet);
            }catch(Exception ex){
                logger.log(Level.SEVERE,"WSS1322.str_transform",ex);
                throw new javax.xml.crypto.dsig.TransformException(ex.getMessage());
                
            }
        }
        return nodeSet;
    }
    
    static final void toNodeSet(final Node rootNode,final Set result){
        //handle EKSHA1 under DKT
        if (rootNode == null) return;
        switch (rootNode.getNodeType()) {
            case Node.ELEMENT_NODE:
                result.add(rootNode);
                Element el=(Element)rootNode;
                if (el.hasAttributes()) {
                    NamedNodeMap nl = ((Element)rootNode).getAttributes();
                    for (int i=0;i<nl.getLength();i++) {
                        result.add(nl.item(i));
                    }
                }
                //no return keep working
            case Node.DOCUMENT_NODE:
                for (Node r=rootNode.getFirstChild();r!=null;r=r.getNextSibling()){
                    if (r.getNodeType()==Node.TEXT_NODE) {
                        result.add(r);
                        while ((r!=null) && (r.getNodeType()==Node.TEXT_NODE)) {
                            r=r.getNextSibling();
                        }
                        if (r==null)
                            return;
                    }
                    toNodeSet(r,result);
                }
                return;
            case Node.COMMENT_NODE:
                return;
            case Node.DOCUMENT_TYPE_NODE:
                return;
            default:
                result.add(rootNode);
        }
        return;
    }
}