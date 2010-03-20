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


package com.sun.xml.xwss;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.net.URLDecoder;
import javax.xml.XMLConstants;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.wss.impl.MessageConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class XOPProcessor {
    private static String XOP_NS = "http://www.w3.org/2004/08/xop/include";
    private static String XOP_INCLUDE = "Include";

  
    private static final List<Node> xopNodes = new ArrayList<Node>();

    private static void getXOPIncludeNode(Node node){
        NodeList nl = node.getChildNodes();
        for(int i=0; i < nl.getLength(); i++){
            Node n = nl.item(i);
            //why its null?
            if(n==null || (n.getLocalName() == null) || (n.getNamespaceURI() == null))
                continue;
            if(n.getLocalName().equals(XOP_INCLUDE) && n.getNamespaceURI().equals(XOP_NS)){
                xopNodes.add(n);
            }
            getXOPIncludeNode(n);
        }
    }

    public static void unmarshal(SOAPMessage soapMsg) throws SOAPException {
        try{
            //System.out.println("++++++++++++++++++++Before unMarshal of XOP +++++++++++++++++++++++++++");
           // soapMsg.writeTo(System.out);
        }catch(Exception ex){ex.printStackTrace();}

        getXOPIncludeNode(soapMsg.getSOAPPart().getEnvelope());

        for (Node n : xopNodes){
            replaceXOPContent((SOAPElement)n,soapMsg);
        }
        //NodeList bodyList = soapMsg.getSOAPPart().getElementsByTagNameNS(XOP_NS,XOP_INCLUDE);
//        for (int i =0;i< bodyList.getLength();i++){
//            replaceXOPContent((SOAPElement)bodyList.item(i),soapMsg);
//        }
        
        
      /*  if(secHeader != null){
       
            NodeList xopElement = secHeader.getElementsByTagNameNS(XOP_NS,XOP_INCLUDE);
            for (int i =0;i< xopElement.getLength(); i++){
                SOAPElement xe = (SOAPElement)xopElement.item(i);
                replaceXOPContent(xe,soapMsg);
            }
        }
        NodeList bodyList = soapMsg.getSOAPBody().getElementsByTagNameNS(MessageConstants.XENC_NS,MessageConstants.ENCRYPTED_DATA_LNAME);
        for (int i =0;i< bodyList.getLength();i++){
            SOAPElement xe = (SOAPElement)bodyList.item(i);
            NodeList xeList = xe.getElementsByTagNameNS(XOP_NS,XOP_INCLUDE);
            for (int j =0;j< xeList.getLength();j++){
                replaceXOPContent((SOAPElement)xeList.item(j),soapMsg);
            }
        }
       */
        // soapMsg.getAttachment()
        try{
            //System.out.println("++++++++++++++++++++After unMarshal of XOP +++++++++++++++++++++++++++");
         //   soapMsg.writeTo(System.out);
        }catch(Exception ex){ex.printStackTrace();}
    }
    
    private static void replaceXOPContent(SOAPElement xe,SOAPMessage soapMsg) throws SOAPException{
        String ref = xe.getAttribute("href");
        if(ref == null)
            return;//error

        AttachmentPart ap = getXOPAttachment(soapMsg, ref);
        if(ap == null){
            //TODO: throw exception, unresolved xop reference
            return;
        }
//        AttachmentPart ap =soapMsg.getAttachment(xe);
        String data = getXOPContent(ap);
        SOAPElement xeParent = xe.getParentElement();
        Text text = xeParent.getOwnerDocument().createTextNode(data);
        xeParent.replaceChild(text,xe);
        //TODO :: Remove the Attachment.
    }

    private static AttachmentPart getXOPAttachment(SOAPMessage sm, String ref){
        ref = decodeCid(ref);
        Iterator iter = sm.getAttachments();
        while(iter.hasNext()){
            AttachmentPart ap = (AttachmentPart)iter.next();
            if(ap.getContentId().equals(ref)){
                return ap;
            }
        }
        return null;
    }

    private static String decodeCid(String cid){
        if(cid.startsWith("cid:"))
            cid = cid.substring(4, cid.length());
        try {
            return "<"+URLDecoder.decode(cid, "UTF-8")+">";
        } catch (UnsupportedEncodingException e) {
            //TODO: throw exception
            return null;
        }
    }

    private static String getXOPContent(AttachmentPart ap ) throws SOAPException{
        byte [] data = ap.getRawContentBytes();
        try {
            String str = new String(data, "utf-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return Base64.encode(data);
    }
    
    
    public static void marshal(SOAPMessage soapMsg) throws SOAPException {
        NodeList bodyList = soapMsg.getSOAPPart().getElementsByTagNameNS(MessageConstants.XENC_NS,MessageConstants.ENCRYPTED_DATA_LNAME);
      
        for (int i=0;i< bodyList.getLength();i++){
            SOAPElement xe = (SOAPElement)bodyList.item(i);
            replaceBase64Data(xe,soapMsg);
        }
        
        NodeList secTokenList = soapMsg.getSOAPPart().getElementsByTagNameNS(MessageConstants.WSSE_NS,MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME);
        for (int i =0;i< secTokenList.getLength();i++){
            SOAPElement xe = (SOAPElement)secTokenList.item(i);
            replaceBase64Data(xe,soapMsg);
        }
        System.out.println("DONE replacing BinaryData");
        try{
         //   soapMsg.writeTo(System.out);
        }catch(Exception ex){ex.printStackTrace();}
    }
    
    private static void replaceBase64Data(SOAPElement sOAPElement, SOAPMessage sOAPMessage) {
        long  id = new Random().nextLong();
        Node childNode = sOAPElement.getFirstChild();
        if(childNode.getNodeType() == Node.TEXT_NODE){
            AttachmentPart ap = sOAPMessage.createAttachmentPart();
            String b64Data  = childNode.getTextContent();
            byte[] decodedData;
            try {
                decodedData = Base64.decode(b64Data);
                ap.setDataHandler(new MTOMDataHandler(new XOPDataSource(decodedData)));
                sOAPMessage.addAttachmentPart(ap);
                String cid = Long.toString(id);
                ap.setContentId("<"+cid+">");
                ap.setMimeHeader("Content-transfer-encoding", "binary");
                Document doc = sOAPElement.getOwnerDocument();
                Element xe = doc.createElementNS("http://www.w3.org/2004/08/xop/include","xop:Include");
                xe.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,"xmlns:xop","http://www.w3.org/2004/08/xop/include");
                xe.setAttribute("href","cid:"+cid);
                sOAPElement.replaceChild(xe,childNode);
            } catch (Base64DecodingException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    private static class XOPDataSource implements javax.activation.DataSource {
        byte[] binaryData;
        
        XOPDataSource(byte[] data) {
            binaryData = data;
        }
        
        public String getContentType() {
            return "application/octect-stream";
        }
        
        public InputStream getInputStream() throws java.io.IOException {
            return new ByteArrayInputStream(binaryData);
        }
        
        public String getName() {
            return "MTOM DataSource";
        }
        
        public OutputStream getOutputStream() throws java.io.IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(binaryData, 0, binaryData.length);
            return baos;
        }
    }
    
    private static class MTOMDataHandler extends javax.activation.DataHandler {
        MTOMDataHandler(javax.activation.DataSource ds) {
            super(ds);
        }
        
        public void writeTo(OutputStream os) throws java.io.IOException {
            ((ByteArrayOutputStream) getDataSource().getOutputStream()).writeTo(os);
        }
    }
}
