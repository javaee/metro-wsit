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
 * SAXC14nCanonicalzerImpl.java
 *
 * Created on August 20, 2005, 5:10 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.c14n;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SAXC14nCanonicalizerImpl extends BaseCanonicalizer  implements ContentHandler {
    
    NamespaceSupport nsContext = new NamespaceSupport ();
    //boolean  firstElement  =   true;
    
    
    
    /** Creates a new instance of SAXC14nCanonicalzerImpl */
    public SAXC14nCanonicalizerImpl () {
        //_attrResult = new TreeSet (new AttrSorter (false));
        _attrResult = new ArrayList();
        for(int i=0;i<4;i++){
            _attrs.add (new Attribute ());
        }
    }
    
    public NamespaceSupport getNSContext (){
        return  nsContext;
    }
    /**
     * Receive notification of a parser warning.
     *
     * <p>The default implementation does nothing.  Application writers
     * may override this method in a subclass to take specific actions
     * for each warning, such as inserting the message in a log file or
     * printing it to the console.</p>
     *
     * @param e The warning information encoded as an exception.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ErrorHandler#warning
     * @see org.xml.sax.SAXParseException
     */
    public void warning (org.xml.sax.SAXParseException e) throws SAXException {
        
        
    }
    
    /**
     * Receive notification of a recoverable parser error.
     *
     * <p>The default implementation does nothing.  Application writers
     * may override this method in a subclass to take specific actions
     * for each error, such as inserting the message in a log file or
     * printing it to the console.</p>
     *
     * @param e The warning information encoded as an exception.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ErrorHandler#warning
     * @see org.xml.sax.SAXParseException
     */
    public void error (org.xml.sax.SAXParseException e) throws SAXException {
        
        
    }
    
    /**
     * Report a fatal XML parsing error.
     *
     * <p>The default implementation throws a SAXParseException.
     * Application writers may override this method in a subclass if
     * they need to take specific actions for each fatal error (such as
     * collecting all of the errors into a single report): in any case,
     * the application must stop all regular processing when this
     * method is invoked, since the document is no longer reliable, and
     * the parser may no longer report parsing events.</p>
     *
     * @param e The error information encoded as an exception.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ErrorHandler#fatalError
     * @see org.xml.sax.SAXParseException
     */
    public void fatalError (org.xml.sax.SAXParseException e) throws SAXException {
        
        
    }
    
    public void comment (char[] ch, int start, int length) throws SAXException {
        
    }
    
    /**
     * Receive notification of character data inside an element.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method to take specific actions for each chunk of character data
     * (such as adding the data to a node or buffer, or printing it to
     * a file).</p>
     *
     * @param ch The characters.
     * @param start The start position in the character array.
     * @param length The number of characters to use from the
     *               character array.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#characters
     */
    public void characters (char[] ch, int start, int length) throws SAXException {
        try {
            outputTextToWriter (ch,start,length,_stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method to take specific actions for each chunk of ignorable
     * whitespace (such as adding data to a node or buffer, or printing
     * it to a file).</p>
     *
     * @param ch The whitespace characters.
     * @param start The start position in the character array.
     * @param length The number of characters to use from the
     *               character array.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#ignorableWhitespace
     */
    public void ignorableWhitespace (char[] ch, int start, int length) throws SAXException {
        
    }
    
    public void endEntity (String name) throws SAXException {
        
    }
    
    public void startEntity (String name) throws SAXException {
        
    }
    
    /**
     * Receive notification of the end of a Namespace mapping.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end of
     * each prefix mapping.</p>
     *
     * @param prefix The Namespace prefix being declared.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#endPrefixMapping
     */
    public void endPrefixMapping (String prefix) throws SAXException {
        
    }
    
    /**
     * Receive a Locator object for document events.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass if they wish to store the locator for use
     * with other document events.</p>
     *
     * @param locator A locator for all SAX document events.
     * @see org.xml.sax.ContentHandler#setDocumentLocator
     * @see org.xml.sax.Locator
     */
    public void setDocumentLocator (Locator locator) {
        
    }
    
    /**
     * Receive notification of a skipped entity.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions for each
     * processing instruction, such as setting status variables or
     * invoking other methods.</p>
     *
     * @param name The name of the skipped entity.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#processingInstruction
     */
    public void skippedEntity (String name) throws SAXException {
    }
    
    /**
     * Receive notification of an unparsed entity declaration.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to keep track of the unparsed entities
     * declared in a document.</p>
     *
     * @param name The entity name.
     * @param publicId The entity public identifier, or null if not
     *                 available.
     * @param systemId The entity system identifier.
     * @param notationName The name of the associated notation.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    public void unparsedEntityDecl (String name, String publicId, String systemId, String notationName) throws SAXException {
        
    }
    
    /**
     * Receive notification of the start of a Namespace mapping.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the start of
     * each Namespace prefix scope (such as storing the prefix mapping).</p>
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI mapped to the prefix.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     */
    public void startPrefixMapping (String prefix, String uri) throws SAXException {
        
        String dURI = nsContext.getURI (prefix);
        boolean add = false;
        if(dURI == null || !uri.equals (dURI)){
            add = true;
        }
        
        if(add && !_ncContextState[_depth]){
            nsContext.pushContext ();
            
            _ncContextState[_depth]=true;
        }
        if(add){
            if(prefix.length () == 0){
                _defURI = uri;
            }else{
                nsContext.declarePrefix (prefix,uri);
                AttributeNS attrNS = getAttributeNS ();
                attrNS.setPrefix (prefix);
                attrNS.setUri (uri);
                _nsResult.add (attrNS);
            }
        }
    }
    
    public void reset (){
        super.reset ();
        nsContext.reset ();
    }
    
    /**
     * Receive notification of the start of an element.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the start of
     * each element (such as allocating a new tree node or writing
     * output to a file).</p>
     *
     * @param uri The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param qName The qualified name (with prefix), or the
     *        empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *        there are no attributes, it shall be an empty
     *        Attributes object.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            
            _depth ++;
            resize();
            _ncContextState[_depth]=false;
            _stream .write ('<');
            if(qName.length () >0){
                writeStringToUtf8 (qName,_stream);
            }else {
                writeStringToUtf8 (localName,_stream);
            }
            if(attributes.getLength () >0 || _nsResult.size () >0){
                handleAttributes (attributes);
            }
            _stream.write ('>');
            
            _attrNSPos =0;
            _attrPos =0;
            
            _defURI = null;
            
            _nsResult.clear ();
            _attrResult.clear ();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Receive notification of the beginning of the document.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the beginning
     * of a document (such as allocating the root node of a tree or
     * creating an output file).</p>
     *
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startDocument
     */
    public void startDocument () throws SAXException {
        
        //super.startDocument ();
    }
    
    public void startDTD (String name, String publicId, String systemId) throws SAXException {
        
        //  super.startDTD (name, publicId, systemId);
    }
    
    public void startCDATA () throws SAXException {
        
        //super.startCDATA ();
    }
    
    /**
     * Tells the parser to resolve the systemId against the baseURI
     * and read the entity text from that resulting absolute URI.
     * Note that because the older
     * {@link DefaultHandler#resolveEntity DefaultHandler.resolveEntity()},
     * method is overridden to call this one, this method may sometimes
     * be invoked with null <em>name</em> and <em>baseURI</em>, and
     * with the <em>systemId</em> already absolutized.
     */
    public org.xml.sax.InputSource resolveEntity (String name, String publicId, String baseURI, String systemId) throws SAXException, java.io.IOException {
        throw new UnsupportedOperationException ("Not yet implemented");
    }
    
    /**
     * Invokes
     * {@link EntityResolver2#resolveEntity EntityResolver2.resolveEntity()}
     * with null entity name and base URI.
     * You only need to override that method to use this class.
     */
    public org.xml.sax.InputSource resolveEntity (String publicId, String systemId) throws SAXException, java.io.IOException {
        return null;
    }
    
    public void internalEntityDecl (String name, String value) throws SAXException {
        throw new UnsupportedOperationException ("Not yet implemented");
        
    }
    
    /**
     * Tells the parser that if no external subset has been declared
     * in the document text, none should be used.
     */
    public org.xml.sax.InputSource getExternalSubset (String name, String baseURI) throws SAXException, java.io.IOException {
        throw new UnsupportedOperationException ("Not yet implemented");
    }
    
    public void externalEntityDecl (String name, String publicId, String systemId) throws SAXException {
    }
    
    public void endDTD () throws SAXException {
    }
    
    public void endCDATA () throws SAXException {
    }
    
    public void elementDecl (String name, String model) throws SAXException {
    }
    
    public void attributeDecl (String eName, String aName, String type, String mode, String value) throws SAXException {
        
    }
    
    /**
     * Receive notification of the end of the document.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end
     * of a document (such as finalising a tree or closing an output
     * file).</p>
     *
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#endDocument
     */
    public void endDocument () throws SAXException {
        
    }
    
    /**
     * Receive notification of the end of an element.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end of
     * each element (such as finalising a tree node or writing
     * output to a file).</p>
     *
     * @param uri The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param qName The qualified name (with prefix), or the
     *        empty string if qualified names are not available.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    public void endElement (String uri, String localName, String qName) throws SAXException {
        _depth --;
        if(_ncContextState[_depth]){
            nsContext.popContext ();
            _ncContextState[_depth]=false;        }
        try{
            _stream.write (_END_TAG);
            if(qName.length () >0){
                writeStringToUtf8 (qName,_stream);
            }else {
                writeStringToUtf8 (localName,_stream);
            }
            _stream.write ('>');
        }catch(IOException io){
            throw new RuntimeException(io);
        }
    }
    
    /**
     * Receive notification of a notation declaration.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass if they wish to keep track of the notations
     * declared in a document.</p>
     *
     * @param name The notation name.
     * @param publicId The notation public identifier, or null if not
     *                 available.
     * @param systemId The notation system identifier.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.DTDHandler#notationDecl
     */
    public void notationDecl (String name, String publicId, String systemId) throws SAXException {
    }
    
    /**
     * Receive notification of a processing instruction.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions for each
     * processing instruction, such as setting status variables or
     * invoking other methods.</p>
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if
     *             none is supplied.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#processingInstruction
     */
    public void processingInstruction (String target, String data) throws SAXException {
    }
    
    //TODO:: Optimize
    private void handleAttributes (Attributes attributes) {
        int length = attributes.getLength ();
        String localName = null;
        boolean contextPushed= false;
        try{
            
            for(int i=0; i<length;i++){
                Attribute attr = getAttribute ();
                attr.setPosition (i);
                attr.setAttributes (attributes);
                _attrResult.add (attr);
            }
            
            if(_defURI != null){
                outputAttrToWriter ("xmlns",_defURI,_stream);
            }
            Iterator itr =  _nsResult.iterator ();
            writeAttributesNS (itr);
            BaseCanonicalizer.sort(_attrResult);
            writeAttributes (attributes,_attrResult.iterator ());
            _nsResult.clear ();
            _attrResult.clear ();
        }catch(IOException io){
            throw new RuntimeException(io);
        }
    }
    protected Attribute getAttribute (){
        if(_attrPos < _attrs.size () ){
            return  (Attribute)_attrs.get (_attrPos++);
        }else{
            for(int i=0;i<4;i++){
                _attrs.add (new Attribute ());
            }
            return (Attribute)_attrs.get (_attrPos++);
        }
    }
    
    
}
