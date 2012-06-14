/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 1995-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.xml.wss.impl.transform;

import com.sun.xml.wss.logging.LogDomainConstants;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException;
import com.sun.org.apache.xml.internal.security.c14n.helper.C14nHelper;
import com.sun.org.apache.xml.internal.security.transforms.params.InclusiveNamespaces;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import java.util.logging.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.parsers.DocumentBuilderFactory;

import com.sun.org.apache.xml.internal.security.c14n.helper.AttrCompare;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import com.sun.xml.wss.WSITXMLFactory;
import com.sun.xml.wss.impl.MessageConstants;
import javax.xml.crypto.Data;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMURIReference;
import org.w3c.dom.Comment;
import org.w3c.dom.ProcessingInstruction;

/**
 * Implements &quot; <A
 * HREF="http://www.w3.org/TR/2002/REC-xml-exc-c14n-20020718/">Exclusive XML
 * Canonicalization, Version 1.0 </A>&quot; <BR />
 * Credits: During restructuring of the Canonicalizer framework, Ren??
 * Kollmorgen from Software AG submitted an implementation of ExclC14n which
 * fitted into the old architecture and which based heavily on my old (and slow)
 * implementation of "Canonical XML". A big "thank you" to Ren?? for this.
 * <BR />
 * <i>THIS </i> implementation is a complete rewrite of the algorithm.
 *
 * @author Christian Geuer-Pollmann <geuerp@apache.org>
 * @author K.Venugopal@sun.com
 * @version $Revision: 1.2 $
 * @see <a href="http://www.w3.org/TR/2002/REC-xml-exc-c14n-20020718/ Exclusive#">
 *          XML Canonicalization, Version 1.0</a>
 */
public class Canonicalizer20010315ExclOmitComments {
    private static Logger logger = Logger.getLogger(LogDomainConstants.IMPL_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_SIGNATURE_DOMAIN_BUNDLE);
    private static final byte[] _END_PI = {'?','>'};
    private static final byte[] _BEGIN_PI = {'<','?'};
    private static final byte[] _END_COMM = {'-','-','>'};
    private static final byte[] _BEGIN_COMM = {'<','!','-','-'};
    private static final byte[] __XA_ = {'&','#','x','A',';'};
    private static final byte[] __X9_ = {'&','#','x','9',';'};
    private static final byte[] _QUOT_ = {'&','q','u','o','t',';'};
    private static final byte[] __XD_ = {'&','#','x','D',';'};
    private static final byte[] _GT_ = {'&','g','t',';'};
    private static final byte[] _LT_ = {'&','l','t',';'};
    private static final byte[] _END_TAG = {'<','/'};
    private static final byte[] _AMP_ = {'&','a','m','p',';'};
    final static AttrCompare COMPARE= new AttrCompare();
    final static String XML="xml";
    final static String XMLNS="xmlns";
    final static byte[] equalsStr= {'=','\"'};
    static final int NODE_BEFORE_DOCUMENT_ELEMENT = -1;
    static final int NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT = 0;
    static final int NODE_AFTER_DOCUMENT_ELEMENT = 1;
    //The null xmlns definiton.
    protected static final Attr nullNode;
    
    static {
        try {
            nullNode=WSITXMLFactory.createDocumentBuilderFactory(WSITXMLFactory.DISABLE_SECURE_PROCESSING).
                    newDocumentBuilder().newDocument().createAttributeNS(Constants.NamespaceSpecNS,XMLNS);
            nullNode.setValue("");
        } catch (Exception e) {
            throw new RuntimeException("Unable to create nullNode"/*,*/+e);
        }
    }
    XMLCryptoContext cryptoContext = null;
    boolean _includeComments;
    boolean reset = false;
    Set _xpathNodeSet = null;
    /**
     * The node to be skiped/excluded from the DOM tree
     * in subtree canonicalizations.
     */
    Node _excludeNode =null;
    //OutputStream _writer = new ByteArrayOutputStream();//null;
    OutputStream _writer = null;
    /**
     * This Set contains the names (Strings like "xmlns" or "xmlns:foo") of
     * the inclusive namespaces.
     */
    TreeSet _inclusiveNSSet = null;
    Set tokenSet = new HashSet();
    static final String XMLNS_URI=Constants.NamespaceSpecNS;
    /**
     * Constructor Canonicalizer20010315ExclOmitComments
     *
     */
    public Canonicalizer20010315ExclOmitComments() {
        this._includeComments = false;
    }
    
    /**
     * Method engineCanonicalizeXPathNodeSet
     * @inheritDoc
     * @param xpathNodeSet
     * @param inclusiveNamespaces
     * @throws CanonicalizationException
     */
    public void engineCanonicalizeXPathNodeSet(Set xpathNodeSet,
            String inclusiveNamespaces,java.io.OutputStream stream,XMLCryptoContext context) throws CanonicalizationException,URIReferenceException {
        
        try {
            _writer = stream;
            this._inclusiveNSSet = (TreeSet)InclusiveNamespaces
                    .prefixStr2Set(inclusiveNamespaces);
            if (xpathNodeSet.size() == 0) {
                //return new byte[0];
                return;
            }
            this._xpathNodeSet = xpathNodeSet;
            cryptoContext = context;
            try {
                Node rootNodeOfC14n = XMLUtils.getOwnerDocument(this._xpathNodeSet);
                this.canonicalizeXPathNodeSet(rootNodeOfC14n,new  NameSpaceSymbTable());
                //this._writer.close();
               /* if (this._writer instanceof ByteArrayOutputStream) {
                    byte [] sol=((ByteArrayOutputStream)this._writer).toByteArray();
                    if (reset) {
                        ((ByteArrayOutputStream)this._writer).reset();
                    }
                    return sol;
                }
                return null;
                */
                return;
            } catch (UnsupportedEncodingException ex) {
                throw new CanonicalizationException("empty", ex);
            } catch (IOException ex) {
                throw new CanonicalizationException("empty", ex);
            }
        } finally {
            this._inclusiveNSSet = null;
        }
    }
    
    
    
    /**
     * @inheritDoc
     * @param E
     * @throws CanonicalizationException
     */
    @SuppressWarnings("unchecked")
    final Iterator handleAttributes(Element E, NameSpaceSymbTable ns,boolean isOutputElement)
    throws CanonicalizationException {
        // result will contain the attrs which have to be outputted
        SortedSet result = new TreeSet(COMPARE);
        NamedNodeMap attrs = null;
        int attrsLength = 0;
        if (E.hasAttributes()) {
            attrs = E.getAttributes();
            attrsLength = attrs.getLength();
        }
        
        //The prefix visibly utilized(in the attribute or in the name) in the element
        Set visiblyUtilized =null;
        //It's the output selected.
        //boolean isOutputElement = this._xpathNodeSet.contains(E);
        if (isOutputElement) {
            visiblyUtilized =  (Set) this._inclusiveNSSet.clone();
        }
        
        for (int i = 0; i < attrsLength; i++) {
            Attr N = (Attr) attrs.item(i);
            String NName=N.getLocalName();
            String NNodeValue=N.getNodeValue();
            /*if ( !this._xpathNodeSet.contains(N) && !this.tokenSet.contains(N))  {
                //The node is not in the nodeset(if there is a nodeset)
                continue;
            }*/
            
            if (!XMLNS_URI.equals(N.getNamespaceURI())) {
                //Not a namespace definition.
                if (isOutputElement) {
                    //The Element is output element, add his prefix(if used) to visibyUtilized
                    String prefix = N.getPrefix();
                    if ((prefix != null) && (!prefix.equals(XML) && !prefix.equals(XMLNS)) ){
                        visiblyUtilized.add(prefix);
                    }
                    //Add to the result.
                    result.add(N);
                }
                continue;
            }
            
            
            if (ns.addMapping(NName, NNodeValue,N)) {
                //New definiton check if it is relative
                if (C14nHelper.namespaceIsRelative(NNodeValue)) {
                    Object exArgs[] = {E.getTagName(), NName,
                            N.getNodeValue()};
                            throw new CanonicalizationException(
                                    "c14n.Canonicalizer.RelativeNamespace", exArgs);
                }
            }
        }
        
        if (isOutputElement) {
            //The element is visible, handle the xmlns definition
            Attr xmlns = E.getAttributeNodeNS(XMLNS_URI, XMLNS);
            if ((xmlns!=null) &&  (!this._xpathNodeSet.contains(xmlns))) {
                //There is a definition but the xmlns is not selected by the xpath.
                //then xmlns=""
                ns.addMapping(XMLNS,"",nullNode);
            }
            
            if (E.getNamespaceURI() != null) {
                String prefix = E.getPrefix();
                if ((prefix == null) || (prefix.length() == 0)) {
                    visiblyUtilized.add(XMLNS);
                } else {
                    visiblyUtilized.add( prefix);
                }
            } else {
                visiblyUtilized.add(XMLNS);
            }
            //This can be optimezed by I don't have time
            //visiblyUtilized.addAll(this._inclusiveNSSet);
            Iterator it=visiblyUtilized.iterator();
            while (it.hasNext()) {
                String s=(String)it.next();
                Attr key=ns.getMapping(s);
                if (key==null) {
                    continue;
                }
                result.add(key);
            }
        } else /*if (_circunvented)*/ {
            Iterator it=this._inclusiveNSSet.iterator();
            while (it.hasNext()) {
                String s=(String)it.next();
                Attr key=ns.getMappingWithoutRendered(s);
                if (key==null) {
                    continue;
                }
                result.add(key);
            }
        }
        
        return result.iterator();
    }
    
    
    
    /**
     * Checks whether a Comment or ProcessingInstruction is before or after the
     * document element. This is needed for prepending or appending "\n"s.
     *
     * @param currentNode comment or pi to check
     * @return NODE_BEFORE_DOCUMENT_ELEMENT, NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT or NODE_AFTER_DOCUMENT_ELEMENT
     * @see #NODE_BEFORE_DOCUMENT_ELEMENT
     * @see #NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT
     * @see #NODE_AFTER_DOCUMENT_ELEMENT
     */
    final static int getPositionRelativeToDocumentElement(Node currentNode) {
        
        if ((currentNode == null) ||
                (currentNode.getParentNode().getNodeType() != Node.DOCUMENT_NODE) ) {
            return NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT;
        }
        Element documentElement = currentNode.getOwnerDocument().getDocumentElement();
        if ( (documentElement == null)  || (documentElement == currentNode) ){
            return NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT;
        }
        
        for (Node x = currentNode; x != null; x = x.getNextSibling()) {
            if (x == documentElement) {
                return NODE_BEFORE_DOCUMENT_ELEMENT;
            }
        }
        
        return NODE_AFTER_DOCUMENT_ELEMENT;
    }
    
    
    /**
     * Canoicalizes all the nodes included in the currentNode and contained in the
     * _xpathNodeSet field.
     *
     * @param currentNode
     * @param ns
     * @throws CanonicalizationException
     * @throws IOException
     */
    final void canonicalizeXPathNodeSet(Node currentNode, NameSpaceSymbTable ns )
    throws CanonicalizationException, IOException ,URIReferenceException{
        boolean currentNodeIsVisible = this._xpathNodeSet.contains(currentNode);
        
        switch (currentNode.getNodeType()) {
            
            case Node.DOCUMENT_TYPE_NODE :
            default :
                break;
                
            case Node.ENTITY_NODE :
            case Node.NOTATION_NODE :
            case Node.DOCUMENT_FRAGMENT_NODE :
            case Node.ATTRIBUTE_NODE :
                throw new CanonicalizationException("empty");
            case Node.DOCUMENT_NODE :
                for (Node currentChild = currentNode.getFirstChild();
                currentChild != null;
                currentChild = currentChild.getNextSibling()) {
                    canonicalizeXPathNodeSet(currentChild,ns);
                }
                break;
                
            case Node.COMMENT_NODE :
                if (currentNodeIsVisible && this._includeComments) {
                    outputCommentToWriter((Comment) currentNode, this._writer);
                }
                break;
                
            case Node.PROCESSING_INSTRUCTION_NODE :
                if (currentNodeIsVisible) {
                    outputPItoWriter((ProcessingInstruction) currentNode, this._writer);
                }
                break;
                
            case Node.TEXT_NODE : {
                if (currentNodeIsVisible && currentNode.getParentNode().getLocalName().equals(MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME)
                && currentNode.getParentNode().getNamespaceURI().equals(MessageConstants.WSSE_NS)) {
                    outputTextToWriter(currentNode.getNodeValue(), this._writer,true);
                }
                break;
            }
            
            case Node.CDATA_SECTION_NODE :
                if (currentNodeIsVisible) {
                    outputTextToWriter(currentNode.getNodeValue(), this._writer);
                    
                    for (Node nextSibling = currentNode.getNextSibling();
                    (nextSibling != null)
                    && ((nextSibling.getNodeType() == Node.TEXT_NODE)
                    || (nextSibling.getNodeType()
                    == Node.CDATA_SECTION_NODE));
                    nextSibling = nextSibling.getNextSibling()) {
               /* The XPath data model allows to select only the first of a
                * sequence of mixed text and CDATA nodes. But we must output
                * them all, so we must search:
                *
                * @see http://nagoya.apache.org/bugzilla/show_bug.cgi?id=6329
                */
                        outputTextToWriter(nextSibling.getNodeValue(), this._writer);
                    }
                }
                break;
                
            case Node.ELEMENT_NODE :
                String localName = currentNode.getLocalName();
                Element currentElement = null;
                if(currentNodeIsVisible && localName != null && localName.equals("SecurityTokenReference")){
                    String namespaceURI = currentNode.getNamespaceURI();
                    currentElement = (Element)currentNode;
                    if(namespaceURI != null && namespaceURI.equals(MessageConstants.WSSE_NS)){
                        currentElement =(Element) deReference(currentNode,cryptoContext);
                        STRTransformImpl.toNodeSet(currentElement, this.tokenSet);
                        removeNodes(currentNode, this._xpathNodeSet);
                        //currentNode = currentElement;
                        printBinaryToken(currentElement, ns);
                                //new NameSpaceSymbTable());
                        this.tokenSet.clear();
                        break;
                    }
                }
                currentElement =(Element) currentNode;
                
                
                OutputStream writer=this._writer;
                String tagName=currentElement.getTagName();
                if (currentNodeIsVisible) {
                    //This is an outputNode.
                    ns.outputNodePush();
                    writer.write('<');
                    writeStringToUtf8(tagName,writer);
                } else {
                    //Not an outputNode.
                    ns.push();
                }
                
                // we output all Attrs which are available
                Iterator attrs = handleAttributes(currentElement,ns,currentNodeIsVisible);
                while (attrs.hasNext()) {
                    Attr attr = (Attr) attrs.next();
                    outputAttrToWriter(attr.getNodeName(), attr.getNodeValue(), writer);
                }
                
                if (currentNodeIsVisible) {
                    writer.write('>');
                }
                
                // traversal
                for (Node currentChild = currentNode.getFirstChild();
                currentChild != null;
                currentChild = currentChild.getNextSibling()) {
                    canonicalizeXPathNodeSet(currentChild,ns);
                }
                
                if (currentNodeIsVisible) {
                    writer.write(_END_TAG);
                    writeStringToUtf8(tagName,writer);
                    //this._writer.write(currentElement.getTagName().getBytes("UTF8"));
                    writer.write('>');
                    ns.outputNodePop();
                } else {
                    ns.pop();
                }
                break;
        }
    }
    
    /**
     * Adds to ns the definitons from the parent elements of el
     * @param el
     * @param ns
     */
    @SuppressWarnings("unchecked")
    final static void getParentNameSpaces(Element el,NameSpaceSymbTable ns)  {
        List parents=new ArrayList();
        Node n1=el.getParentNode();
        if (!(n1 instanceof Element)) {
            return;
        }
        //Obtain all the parents of the elemnt
        Element parent=(Element) el.getParentNode();
        while (parent!=null) {
            parents.add(parent);
            Node n=parent.getParentNode();
            if (!(n instanceof Element )) {
                break;
            }
            parent=(Element)n;
        }
        //Visit them in reverse order.
        ListIterator it=parents.listIterator(parents.size());
        while (it.hasPrevious()) {
            Element ele=(Element)it.previous();
            if (!ele.hasAttributes()) {
                continue;
            }
            NamedNodeMap attrs = ele.getAttributes();
            int attrsLength = attrs.getLength();
            for (int i = 0; i < attrsLength; i++) {
                Attr N = (Attr) attrs.item(i);
                if (!Constants.NamespaceSpecNS.equals(N.getNamespaceURI())) {
                    //Not a namespace definition, ignore.
                    continue;
                }
                
                String NName=N.getLocalName();
                String NValue=N.getNodeValue();
                if (XML.equals(NName)
                && Constants.XML_LANG_SPACE_SpecNS.equals(NValue)) {
                    continue;
                }
                ns.addMapping(NName,NValue,N);
            }
        }
        Attr nsprefix;
        if (((nsprefix=ns.getMappingWithoutRendered("xmlns"))!=null)
        && "".equals(nsprefix.getValue())) {
            ns.addMappingAndRender("xmlns","",nullNode);
        }
    }
    /**
     * Outputs an Attribute to the internal Writer.
     *
     * The string value of the node is modified by replacing
     * <UL>
     * <LI>all ampersands (&) with <CODE>&amp;amp;</CODE></LI>
     * <LI>all open angle brackets (<) with <CODE>&amp;lt;</CODE></LI>
     * <LI>all quotation mark characters with <CODE>&amp;quot;</CODE></LI>
     * <LI>and the whitespace characters <CODE>#x9</CODE>, #xA, and #xD, with character
     * references. The character references are written in uppercase
     * hexadecimal with no leading zeroes (for example, <CODE>#xD</CODE> is represented
     * by the character reference <CODE>&amp;#xD;</CODE>)</LI>
     * </UL>
     *
     * @param name
     * @param value
     * @param writer
     * @throws IOException
     */
    static final void outputAttrToWriter(final String name, final String value, final OutputStream writer) throws IOException {
        writer.write(' ');
        writeStringToUtf8(name,writer);
        writer.write(equalsStr);
        byte  []toWrite;
        final int length = value.length();
        for (int i=0;i < length; i++) {
            char c = value.charAt(i);
            
            switch (c) {
                
                case '&' :
                    toWrite=_AMP_;
                    //writer.write(_AMP_);
                    break;
                    
                case '<' :
                    toWrite=_LT_;
                    //writer.write(_LT_);
                    break;
                    
                case '"' :
                    toWrite=_QUOT_;
                    //writer.write(_QUOT_);
                    break;
                    
                case 0x09 :    // '\t'
                    toWrite=__X9_;
                    //writer.write(__X9_);
                    break;
                    
                case 0x0A :    // '\n'
                    toWrite=__XA_;
                    //writer.write(__XA_);
                    break;
                    
                case 0x0D :    // '\r'
                    toWrite=__XD_;
                    //writer.write(__XD_);
                    break;
                    
                default :
                    writeCharToUtf8(c,writer);
                    //this._writer.write(c);
                    continue;
            }
            writer.write(toWrite);
        }
        
        writer.write('\"');
    }
    
    final static void writeCharToUtf8(final char c,final OutputStream out) throws IOException{
        char ch;
        if (/*(c >= 0x0001) &&*/ (c <= 0x007F)) {
            out.write(c);
            return;
        } else if (c > 0x07FF) {
            ch=(char)(c>>>12);
            if (ch>0) {
                out.write(0xE0 | ( ch & 0x0F));
            } else {
                out.write(0xE0);
            }
            out.write(0x80 | ((c >>>  6) & 0x3F));
            out.write(0x80 | ((c) & 0x3F));
            return;
            
        } else {
            ch=(char)(c>>>6);
            if (ch>0) {
                out.write(0xC0 | (ch & 0x1F));
            } else {
                out.write(0xC0);
            }
            out.write(0x80 | ((c) & 0x3F));
            return;
        }
        
    }
    
    final static void writeStringToUtf8(final String str,final OutputStream out) throws IOException{
        final int length=str.length();
        int i=0;
        char ch,c;
        while (i<length) {
            c=str.charAt(i++);
            if (/*(c >= 0x0001) &&*/ (c <= 0x007F)) {
                out.write(c);
                continue;
            } else if (c > 0x07FF) {
                ch=(char)(c>>>12);
                if (ch>0) {
                    out.write(0xE0 | ( ch & 0x0F));
                } else {
                    out.write(0xE0);
                }
                out.write(0x80 | ((c >>>  6) & 0x3F));
                out.write(0x80 | ((c) & 0x3F));
                continue;
            } else {
                ch=(char)(c>>>6);
                if (ch>0) {
                    out.write(0xC0 | (ch & 0x1F));
                } else {
                    out.write(0xC0);
                }
                out.write(0x80 | ((c) & 0x3F));
                continue;
            }
        }
        
    }
    /**
     * Outputs a PI to the internal Writer.
     *
     * @param currentPI
     * @param writer TODO
     * @throws IOException
     */
    static final void outputPItoWriter(ProcessingInstruction currentPI, OutputStream writer) throws IOException {
        final int position = getPositionRelativeToDocumentElement(currentPI);
        
        if (position == NODE_AFTER_DOCUMENT_ELEMENT) {
            writer.write('\n');
        }
        writer.write(_BEGIN_PI);
        
        final String target = currentPI.getTarget();
        int length = target.length();
        
        for (int i = 0; i < length; i++) {
            char c=target.charAt(i);
            if (c==0x0D) {
                writer.write(__XD_);
            } else {
                writeCharToUtf8(c,writer);
            }
        }
        
        final String data = currentPI.getData();
        
        length = data.length();
        
        if (length > 0) {
            writer.write(' ');
            
            for (int i = 0; i < length; i++) {
                char c=data.charAt(i);
                if (c==0x0D) {
                    writer.write(__XD_);
                } else {
                    writeCharToUtf8(c,writer);
                }
            }
        }
        
        writer.write(_END_PI);
        if (position == NODE_BEFORE_DOCUMENT_ELEMENT) {
            writer.write('\n');
        }
    }
    
    /**
     * Method outputCommentToWriter
     *
     * @param currentComment
     * @param writer TODO
     * @throws IOException
     */
    static final void outputCommentToWriter(Comment currentComment, OutputStream writer) throws IOException {
        final int position = getPositionRelativeToDocumentElement(currentComment);
        if (position == NODE_AFTER_DOCUMENT_ELEMENT) {
            writer.write('\n');
        }
        writer.write(_BEGIN_COMM);
        
        final String data = currentComment.getData();
        final int length = data.length();
        
        for (int i = 0; i < length; i++) {
            char c=data.charAt(i);
            if (c==0x0D) {
                writer.write(__XD_);
            } else {
                writeCharToUtf8(c,writer);
            }
        }
        
        writer.write(_END_COMM);
        if (position == NODE_BEFORE_DOCUMENT_ELEMENT) {
            writer.write('\n');
        }
    }
    
    /**
     * Outputs a Text of CDATA section to the internal Writer.
     *
     * @param text
     * @param writer TODO
     * @throws IOException
     */
    static final void outputTextToWriter(final String text, final OutputStream writer) throws IOException {
        outputTextToWriter(text, writer,false);
    }
    
    /**
     * Outputs a Text of CDATA section to the internal Writer.
     *
     * @param text
     * @param writer TODO
     * @param skipWhiteSpace S	   ::=   	(#x20 | #x9 | #xD | #xA)+
     * @throws IOException
     */
    static final void outputTextToWriter(final String text, final OutputStream writer, boolean skipWhiteSpace) throws IOException {
        final int length = text.length();
        byte []toWrite;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            
            switch (c) {
                
                case '&' :
                    writer.write(_AMP_);
                    break;
                    
                case '<' :
                    writer.write(_LT_);
                    break;
                    
                case '>' :
                    writer.write(_GT_);
                    break;
                    
                case 0xD :
                    if(!skipWhiteSpace){
                        writer.write(__XD_);
                    }
                    break;
                case 0xA :
                    if(!skipWhiteSpace){
                        writeCharToUtf8(c,writer);
                    }
                    break;
                case 0x9 :
                    if(!skipWhiteSpace){
                        writeCharToUtf8(c,writer);
                    }
                    break;
                case 0x20 :
                    if(!skipWhiteSpace){
                        writeCharToUtf8(c,writer);
                    }
                    break;
                default :
                    writeCharToUtf8(c,writer);
                    continue;
            }
        }
    }
    
    
    /**
     * @return Returns the _includeComments.
     */
    final public boolean is_includeComments() {
        return _includeComments;
    }
    /**
     * @param comments The _includeComments to set.
     */
    final public void set_includeComments(boolean comments) {
        _includeComments = comments;
    }
    
    /**
     * @param _writer The _writer to set.
     */
    public void setWriter(OutputStream _writer) {
        this._writer = _writer;
    }
    
    protected static Node deReference(final Node node,XMLCryptoContext context)throws URIReferenceException {
        
         /*NodeList nodeList = ((Document)node).getElementsByTagNameNS(WSSE_EXT,"SecurityTokenReference");
        final Node domNode = nodeList.item(0);*/
        URIDereferencer dereferencer = context.getURIDereferencer();
        
        //Dereference SecurityTokenReference;
        DOMURIReference domReference = new DOMURIReference(){
            public Node getHere(){
                return node;
            }
            public String getURI(){
                return null;
            }
            public String getType(){
                return null;
            }
        };
        Data data = dereferencer.dereference(domReference, context);
        //Node parentNode = node.getParentNode();
        Iterator nodeIterator =  ((NodeSetData)data).iterator();
        if(nodeIterator.hasNext()){
            return (Node)nodeIterator.next();
        }else{
            throw new URIReferenceException("URI "+((Element)node).getAttribute("URI") + "not found");
        }
    }
    
    void removeNodes(Node rootNode,Set result){
        try{
            switch (rootNode.getNodeType()) {
                case Node.ELEMENT_NODE:
                    result.remove(rootNode);
                    //no return keep working
                case Node.DOCUMENT_NODE:
                    for (Node r=rootNode.getFirstChild();r!=null;r=r.getNextSibling()){
                        removeNodes(r,result);
                    }
                    return;
                default:
                    result.remove(rootNode);
            }
        }catch(Exception ex){
            //log
        }
        return;
    }
    
    final void printBinaryToken(Node currentNode, NameSpaceSymbTable ns )
    throws CanonicalizationException, IOException ,URIReferenceException{
        //handle EKSHA1 under DKT
        if (currentNode == null) return;
        
        boolean currentNodeIsVisible = this._xpathNodeSet.contains(currentNode);
        if(!currentNodeIsVisible){
            currentNodeIsVisible = this.tokenSet.contains(currentNode);
        }
        switch (currentNode.getNodeType()) {
            
            case Node.DOCUMENT_TYPE_NODE :
            default :
                break;
                
            case Node.ENTITY_NODE :
            case Node.NOTATION_NODE :
            case Node.DOCUMENT_FRAGMENT_NODE :
            case Node.ATTRIBUTE_NODE :
                throw new CanonicalizationException("empty");
            case Node.DOCUMENT_NODE :
                for (Node currentChild = currentNode.getFirstChild();
                currentChild != null;
                currentChild = currentChild.getNextSibling()) {
                    printBinaryToken(currentChild,ns);
                }
                break;
                
            case Node.COMMENT_NODE :
                if (currentNodeIsVisible && this._includeComments) {
                    outputCommentToWriter((Comment) currentNode, this._writer);
                }
                break;
                
            case Node.PROCESSING_INSTRUCTION_NODE :
                if (currentNodeIsVisible) {
                    outputPItoWriter((ProcessingInstruction) currentNode, this._writer);
                }
                break;
                
            case Node.TEXT_NODE : {
                if (currentNodeIsVisible && currentNode.getParentNode().getLocalName().equals(MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME)
                && currentNode.getParentNode().getNamespaceURI().equals(MessageConstants.WSSE_NS)) {
                    NamedNodeMap nmap = currentNode.getParentNode().getAttributes();
                    Node node = nmap.getNamedItemNS(MessageConstants.WSU_NS, "Id");
                    if(node == null){
                        //System.out.println("ID FOUND");
                        outputTextToWriter(currentNode.getNodeValue(), this._writer,true);
                    }else{
                        outputTextToWriter(currentNode.getNodeValue(), this._writer,false);
                    }
                }
                break;
            }
            
            case Node.CDATA_SECTION_NODE :
                if (currentNodeIsVisible) {
                    outputTextToWriter(currentNode.getNodeValue(), this._writer);
                    
                    for (Node nextSibling = currentNode.getNextSibling();
                    (nextSibling != null)
                    && ((nextSibling.getNodeType() == Node.TEXT_NODE)
                    || (nextSibling.getNodeType()
                    == Node.CDATA_SECTION_NODE));
                    nextSibling = nextSibling.getNextSibling()) {
               /* The XPath data model allows to select only the first of a
                * sequence of mixed text and CDATA nodes. But we must output
                * them all, so we must search:
                *
                * @see http://nagoya.apache.org/bugzilla/show_bug.cgi?id=6329
                */
                        outputTextToWriter(nextSibling.getNodeValue(), this._writer);
                    }
                }
                break;
                
            case Node.ELEMENT_NODE :
                String localName = currentNode.getLocalName();
                Element currentElement = null;
                currentElement = (Element)currentNode;
                
                OutputStream writer=this._writer;
                String tagName=currentElement.getTagName();
                if (currentNodeIsVisible) {
                    //This is an outputNode.
                    ns.outputNodePush();
                    writer.write('<');
                    writeStringToUtf8(tagName,writer);
                } else {
                    //Not an outputNode.
                    ns.push();
                }
                
                // we output all Attrs which are available
                Attr defNS = currentElement.getAttributeNodeNS(MessageConstants.NAMESPACES_NS, "xmlns");
                Iterator attrs = handleAttributes(currentElement,ns, currentNodeIsVisible);
                if ( defNS != null )
                    outputAttrToWriter(defNS.getNodeName(), defNS.getNodeValue(), writer);
                while (attrs.hasNext()) {
                    Attr attr = (Attr) attrs.next();
                    outputAttrToWriter(attr.getNodeName(), attr.getNodeValue(), writer);
                }
                
                if (currentNodeIsVisible) {
                    writer.write('>');
                }
                
                // traversal
                for (Node currentChild = currentNode.getFirstChild();
                currentChild != null;
                currentChild = currentChild.getNextSibling()) {
                    printBinaryToken(currentChild,ns);
                }
                
                if (currentNodeIsVisible) {
                    writer.write(_END_TAG);
                    writeStringToUtf8(tagName,writer);
                    //this._writer.write(currentElement.getTagName().getBytes("UTF8"));
                    writer.write('>');
                    ns.outputNodePop();
                } else {
                    ns.pop();
                }
                break;
        }
    }
}
