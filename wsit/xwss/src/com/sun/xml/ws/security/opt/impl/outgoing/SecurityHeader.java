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

package com.sun.xml.ws.security.opt.impl.outgoing;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.enc.JAXBEncryptedData;
import com.sun.xml.ws.security.opt.impl.enc.JAXBEncryptedKey;
import com.sun.xml.ws.security.opt.impl.message.GSHeaderElement;
import com.sun.xml.ws.security.opt.impl.tokens.Timestamp;
import com.sun.xml.wss.impl.MessageConstants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SecurityHeader {
    public static final int LAYOUT_LAX = 0;
    public static final int LAYOUT_STRICT = 1;
    public static final int LAYOUT_LAX_TS_FIRST = 2;
    public static final int LAYOUT_LAX_TS_LAST = 3;
    
    protected ArrayList<SecurityHeaderElement> secHeaderContent = new ArrayList<SecurityHeaderElement>();
    protected int headerLayout = LAYOUT_STRICT;
    protected String soapVersion = MessageConstants.SOAP_1_1_NS;
    private boolean debug = false;
    private boolean mustUnderstandValue = true;
    /**
     * Default constructor
     * uses Lax Message Layout and SOAP 1.1 version
     */
    public SecurityHeader(){
        
    }
    
    public SecurityHeader(int layout, String soapVersion, boolean muValue){
        this.headerLayout = layout;
        this.soapVersion = soapVersion;
        this.mustUnderstandValue = muValue;
    }
    
    public int getHeaderLayout(){
        return this.headerLayout;
    }
    
    public void setHeaderLayout(int headerLayout){
        this.headerLayout = headerLayout;
    }
    
    public String getSOAPVersion(){
        return this.soapVersion;
    }
    
    public void setSOAPVersion(String soapVersion){
        this.soapVersion = soapVersion;
    }
    
    public SecurityHeaderElement getChildElement(String localName,String uri){
        for(SecurityHeaderElement she : secHeaderContent){
            if(localName.equals(she.getLocalPart()) && uri.equals(she.getNamespaceURI())){
                return she;
            }
        }
        return null;
    }
    
    public Iterator getHeaders(final String localName,final String uri){
        return new Iterator() {
            int idx = 0;
            Object next;
            public boolean hasNext() {
                if(next==null)
                    fetch();
                return next!=null;
            }
            
            public Object next() {
                if(next==null) {
                    fetch();
                    if(next==null){
                        throw new NoSuchElementException();
                    }
                }
                
                Object r = next;
                next = null;
                return r;
            }
            
            private void fetch() {
                while(idx<secHeaderContent.size()) {
                    SecurityHeaderElement she = secHeaderContent.get(idx++);
                    if((uri == null && localName.equals(she.getLocalPart())) ||
                            (localName.equals(she.getLocalPart() )&& uri.equals(she.getNamespaceURI()))){
                        next = she;
                        break;
                    }
                }
            }
            
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    
    public SecurityHeaderElement getChildElement(String id){
        for(SecurityHeaderElement she: secHeaderContent){
            if(id.equals(she.getId()))
                return she;
        }
        return null;
    }
    
    public void add(SecurityHeaderElement header){
        prepend(header);
    }
    
    public boolean replace(SecurityHeaderElement replaceThis, SecurityHeaderElement withThis){
        int index = secHeaderContent.indexOf(replaceThis);
        if(index != -1){
            secHeaderContent.set(index,withThis);
            return true;
        }
        return false;
    }
    
    public void prepend(SecurityHeaderElement element){
        secHeaderContent.add(0,element);
    }
    
    public void append(SecurityHeaderElement element){
        secHeaderContent.add(element);
    }
    
    /**
     * Gets the namespace URI of this header element.
     *
     * @return
     *      this string must be interned.
     */
    public @NotNull String getNamespaceURI(){
        return MessageConstants.WSSE_NS;
    }
    
    /**
     * Gets the local name of this header element.
     *
     * @return
     *      this string must be interned.
     */
    public @NotNull String getLocalPart(){
        return "Security";
    }
    
    /**
     * Gets the attribute value on the header element.
     *
     * @param nsUri
     *      The namespace URI of the attribute. Can be empty.
     * @param localName
     *      The local name of the attribute.
     *
     * @return
     *      if the attribute is found, return the whitespace normalized value.
     *      (meaning no leading/trailing space, no consequtive whitespaces in-between.)
     *      Otherwise null. Note that the XML parsers are responsible for
     *      whitespace-normalizing attributes, so {@link Header} implementation
     *      doesn't have to do anything.
     */
    public @Nullable String getAttribute(@NotNull String nsUri, @NotNull String localName){
        throw new UnsupportedOperationException();
    }
    
    /**
     * Gets the attribute value on the header element.
     *
     * <p>
     * This is a convenience method that calls into {@link #getAttribute(String, String)}
     *
     * @param name
     *      Never null.
     *
     * @see #getAttribute(String, String)
     */
    public @Nullable String getAttribute(@NotNull QName name){
        throw new UnsupportedOperationException();
    }
    
    
    /**
     * Writes out the header.
     *
     * @throws XMLStreamException
     *      if the operation fails for some reason. This leaves the
     *      writer to an undefined state.
     */
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException{
        orderHeaders();
        if(secHeaderContent.size() >0){
            streamWriter.writeStartElement(MessageConstants.WSSE_PREFIX,"Security",MessageConstants.WSSE_NS);
            writeMustunderstand(streamWriter);
            for( SecurityHeaderElement el : secHeaderContent){
                ((SecurityElementWriter)el).writeTo(streamWriter);
            }
            
            streamWriter.writeEndElement();
        }
    }
    
    /**
     * Writes out the header to the given SOAPMessage.
     *
     * <p>
     * Sometimes a {@link Message} needs to produce itself
     * as {@link SOAPMessage}, in which case each header needs
     * to turn itself into a header.
     *
     * @throws SOAPException
     *      if the operation fails for some reason. This leaves the
     *      writer to an undefined state.
     */
    public void writeTo(SOAPMessage saaj) throws SOAPException{
        throw new UnsupportedOperationException();
    }
    
    /**
     * Writes out the header as SAX events.
     *
     * <p>
     * Sometimes a {@link Message} needs to produce SAX events,
     * and this method is necessary for headers to participate to it.
     *
     * <p>
     * A header is responsible for producing the SAX events for its part,
     * including <tt>startPrefixMapping</tt> and <tt>endPrefixMapping</tt>,
     * but not startDocument/endDocument.
     *
     * <p>
     * Note that SAX contract requires that any error that does NOT originate
     * from {@link ContentHandler} (meaning any parsing error and etc) must
     * be first reported to {@link ErrorHandler}. If the SAX event production
     * cannot be continued and the processing needs to abort, the code may
     * then throw the same {@link SAXParseException} reported to {@link ErrorHandler}.
     *
     * @param contentHandler
     *      The {@link ContentHandler} that receives SAX events.
     *
     * @param errorHandler
     *      The {@link ErrorHandler} that receives parsing errors.
     */
    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException{
        throw new UnsupportedOperationException();
    }

    private void writeMustunderstand(XMLStreamWriter writer) throws XMLStreamException{
        if(mustUnderstandValue){
            if(soapVersion == MessageConstants.SOAP_1_1_NS){
                writer.writeAttribute("S",MessageConstants.SOAP_1_1_NS,MessageConstants.MUST_UNDERSTAND,"1");
            }else if(soapVersion == MessageConstants.SOAP_1_2_NS){
                writer.writeAttribute("S",MessageConstants.SOAP_1_2_NS,MessageConstants.MUST_UNDERSTAND,"true");
            }
        } else{
            if(soapVersion == MessageConstants.SOAP_1_1_NS){
                writer.writeAttribute("S",MessageConstants.SOAP_1_1_NS,MessageConstants.MUST_UNDERSTAND,"0");
            }else if(soapVersion == MessageConstants.SOAP_1_2_NS){
                writer.writeAttribute("S",MessageConstants.SOAP_1_2_NS,MessageConstants.MUST_UNDERSTAND,"false");
            }
        }
    }
    
    private void orderHeaders(){
        if(headerLayout == LAYOUT_LAX_TS_LAST){
            laxTimestampLast();
        }else if(headerLayout == LAYOUT_LAX_TS_FIRST){
            laxTimestampFirst();
        }else if(headerLayout ==LAYOUT_STRICT){
            strict();
        }else{
            strict();
        }
    }
    
    private void laxTimestampLast(){
        strict();
        SecurityHeaderElement timestamp = this.secHeaderContent.get(0);
        if(timestamp != null && (timestamp instanceof Timestamp )){
            this.secHeaderContent.remove(0);
            this.secHeaderContent.add(timestamp);
        }
    }
    
    
    private void laxTimestampFirst(){
        strict();
    }
    
    private void print(ArrayList<SecurityHeaderElement> list){
        if(!debug){
            return;
        }
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
        for(int j=0;j<list.size();j++){
            System.out.println(list.get(j));
        }
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
    }
    
    private void strict(){
        ArrayList<SecurityHeaderElement> primaryElementList  = new ArrayList<SecurityHeaderElement> ();
        ArrayList<SecurityHeaderElement> topElementList  = new ArrayList<SecurityHeaderElement> ();
        int len = secHeaderContent.size();
        print(secHeaderContent);
        
        SecurityHeaderElement timeStamp = null;
        for(int i=0;i<len;i++){
            SecurityHeaderElement she = secHeaderContent.get(i);
            if(she.getLocalPart() == MessageConstants.TIMESTAMP_LNAME){
                timeStamp = she;
                continue;
            }
            if(isTopLevelElement(she)){
                topElementList.add(she);
            }else{
                primaryElementList.add(0,she);
            }
        }
        
        print(topElementList);
       // topElementList = orderList(topElementList);
        
        print(primaryElementList);
        primaryElementList = orderList(primaryElementList);
        
        ArrayList<SecurityHeaderElement> tmpList =   new ArrayList<SecurityHeaderElement> ();
        for(int i=0;i<primaryElementList.size();i++){
            SecurityHeaderElement she = primaryElementList.get(i);
            if(she.getLocalPart() == MessageConstants.XENC_REFERENCE_LIST_LNAME ||
                    she.getLocalPart() == MessageConstants.ENCRYPTEDKEY_LNAME){
                int tLen = topElementList.size();
                for(int j=tLen-1;j>=0;j--){
                    SecurityHeaderElement tk = topElementList.get(j);
                    if(she.refersToSecHdrWithId(tk.getId())){
                        topElementList.add(j+1,she);
                        //topElementList.add(j,she);
                        tmpList.add(she);
                        break;
                    }
                }
            }
        }
        primaryElementList.removeAll(tmpList);

        topElementList = orderList(topElementList);
        
        secHeaderContent.clear();
        for(int i=topElementList.size()-1;i>=0;i--){
            secHeaderContent.add(topElementList.get(i));
        }
        
        for(int i=primaryElementList.size()-1;i>=0;i--){
            secHeaderContent.add(primaryElementList.get(i));
        }
        
        if(timeStamp != null){
            secHeaderContent.add(0,timeStamp);
        }
    }
    
    private ArrayList<SecurityHeaderElement> orderList(ArrayList<SecurityHeaderElement> list){
        ArrayList<SecurityHeaderElement> tmp = new ArrayList<SecurityHeaderElement>();
        for(int i=0;i<list.size();i++){
            SecurityHeaderElement securityElementOne = list.get(i);
            
            int wLen = tmp.size();
            int index = 0;
            if(wLen == 0){
                tmp.add(securityElementOne);
                continue;
            }
            
            int setIndex = -1;
            for(int j=0;j<wLen;j++){
                SecurityHeaderElement securityElementTwo = tmp.get(j);
                if(securityElementOne.refersToSecHdrWithId(securityElementTwo.getId())){
                    if(securityElementTwo instanceof JAXBEncryptedData){
                        if(securityElementOne instanceof JAXBEncryptedKey || securityElementOne.getLocalPart() == MessageConstants.XENC_REFERENCE_LIST_LNAME){
                            setIndex = j+1;
                        }else{
                            setIndex = j;
                        }
                    } else{
                        setIndex = j;
                    }
                }else if(securityElementTwo instanceof JAXBEncryptedData  && refersToEncryptedElement(securityElementOne,securityElementTwo)){
                    setIndex = j;
                }else if(securityElementTwo.refersToSecHdrWithId(securityElementOne.getId())){
                    if(securityElementTwo instanceof JAXBEncryptedKey && securityElementOne instanceof JAXBEncryptedData){
                        setIndex = j;
                    }else{
                        setIndex = j+1;
                    }
                    
                }
            }
            if(tmp.contains(securityElementOne)){
                continue;
            }
            if(setIndex == -1){
                tmp.add(securityElementOne);
            }else{
                tmp.add(setIndex,securityElementOne);
            }
            print(tmp);
        }
        return tmp;
    }
    
    private boolean refersToEncryptedElement(SecurityHeaderElement securityElementOne,SecurityHeaderElement securityElementTwo){
        if(securityElementOne.refersToSecHdrWithId(((JAXBEncryptedData)securityElementTwo).getEncryptedId())){
            return true;
        }
        return false;
    }
    private void movePrevHeader(SecurityHeaderElement toBeMoved, int index){
        int prevIndex = secHeaderContent.indexOf(toBeMoved);
        SecurityHeaderElement prev = (SecurityHeaderElement)secHeaderContent.get(prevIndex-1);
        String prevId = prev.getId();
        secHeaderContent.remove(toBeMoved);
        secHeaderContent.add(index, toBeMoved);
        if(toBeMoved.refersToSecHdrWithId(prevId)){
            movePrevHeader(prev, index);
        }
    }
    //move tokens to top.
    private boolean isTopLevelElement(SecurityHeaderElement she) {
        String localPart = she.getLocalPart();
        //String uri = she.getNamespaceURI();
        if(localPart.equals(MessageConstants.ENCRYPTED_DATA_LNAME)){
            if(she instanceof GSHeaderElement){
                return true;//Issued token encrypted.
            }
            localPart = ((JAXBEncryptedData)she).getEncryptedLocalName();
            
        }
        if(localPart == MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME){
            return true;
        }
        if(localPart == MessageConstants.SECURITY_CONTEXT_TOKEN_LNAME){
            return true;
        }
        if(localPart == MessageConstants.ENCRYPTEDKEY_LNAME){
            if (((JAXBEncryptedKey)she).hasReferenceList()) {
            return false;
            }
            return true;
        }
        if(localPart == MessageConstants.DERIVEDKEY_TOKEN_LNAME ){
            return true;
        }
        if(localPart == MessageConstants.SIGNATURE_CONFIRMATION_LNAME){
            return true;
        }
        
        if(localPart == MessageConstants.TIMESTAMP_LNAME){
            return true;
        }
        if(localPart.equals( MessageConstants.SAML_ASSERTION_LNAME)){
            return true;
        }
        if(localPart == MessageConstants.USERNAME_TOKEN_LNAME){
            return true;
        }
        return false;
    }
}