
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

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.crypto.Data;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMURIReference;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jcp.xml.dsig.internal.dom.ApacheData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
        HashSet nodeSet = nodeSet = new HashSet();
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
                DocumentBuilderFactory factory =  new com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl();
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

