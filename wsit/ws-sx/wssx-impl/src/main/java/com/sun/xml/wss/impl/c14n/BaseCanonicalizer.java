/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.wss.impl.c14n;

import com.sun.xml.wss.impl.misc.UnsyncByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import javax.xml.XMLConstants;
import org.xml.sax.Attributes;

/**
 *
 * @author Apache
 * @author K.Venugopal@sun.com
 * //TODO:
 * Refactor code ..
 */
public abstract class BaseCanonicalizer {
    //extends DefaultHandler2 {
    static final byte[] _END_PI = {'?','>'};
    static final byte[] _BEGIN_PI = {'<','?'};
    static final byte[] _END_COMM = {'-','-','>'};
    static final byte[] _BEGIN_COMM = {'<','!','-','-'};
    static final byte[] __XA_ = {'&','#','x','A',';'};
    static final byte[] __X9_ = {'&','#','x','9',';'};
    static final byte[] _QUOT_ = {'&','q','u','o','t',';'};
    static final byte[] __XD_ = {'&','#','x','D',';'};
    static final byte[] _GT_ = {'&','g','t',';'};
    static final byte[] _LT_ = {'&','l','t',';'};
    static final byte[] _END_TAG = {'<','/'};
    static final byte[] _AMP_ = {'&','a','m','p',';'};
    final static String XML="xml";
    final static String XMLNS="xmlns";
    final static byte[] equalsStr= {'=','\"'};
    static final int NODE_BEFORE_DOCUMENT_ELEMENT = -1;
    static final int NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT = 0;
    static final int NODE_AFTER_DOCUMENT_ELEMENT = 1;
    protected ArrayList _attrs = new ArrayList();
    protected ArrayList _nsAttrs = new ArrayList();
    int _attrPos = 0;
    int _attrNSPos = 0;
    protected List _attrResult = null;
    //protected SortedSet _nsResult = new TreeSet(new AttrSorter(true));
    protected List _nsResult = new ArrayList();
    String _defURI = null;
    OutputStream _stream = null;
    boolean [] _ncContextState = new boolean [20];
    StringBuffer _attrName = new StringBuffer();
    int _depth = 0;
    protected static final int initalCacheSize = 4;
    boolean _parentNamespacesAdded = false;
    //List _parentNamespaces = null;
    String _elementPrefix = "";
    private static boolean debug = false;
    /** Creates a new instance of BaseCanonicalizer */
    public BaseCanonicalizer() {
    }
    
    public void reset(){
        _nsResult.clear();
        _attrResult.clear();
        _attrPos =0;
        _depth =0;
        _parentNamespacesAdded = false;
        
    }
    
    public void setStream(OutputStream os){
        this._stream = os;
    }
    
    public OutputStream getOutputStream(){
        return this._stream;
    }
    
    protected final void resize(){
        if(_depth >= _ncContextState.length ){
            boolean []tmp = new boolean[_ncContextState.length+20];
            System.arraycopy(_ncContextState,0,tmp,0,_ncContextState.length);
            _ncContextState = tmp;
        }
    }

    @SuppressWarnings("unchecked")
    public void addParentNamespaces(List nsDecls){
        if(!_parentNamespacesAdded){
            //_parentNamespaces = nsDecls;
            _nsResult.addAll(nsDecls);
            _parentNamespacesAdded = true;
        }
    }
    
    @SuppressWarnings("unchecked")
    protected AttributeNS getAttributeNS(){
        if(_attrNSPos < _nsAttrs.size() ){
            return  (AttributeNS)_nsAttrs.get(_attrNSPos++);
        }else{
            for(int i=0;i<initalCacheSize;i++){
                _nsAttrs.add(new AttributeNS());
            }
            return (AttributeNS)_nsAttrs.get(_attrNSPos++);
        }
    }
    
    
    protected void writeAttributes(Attributes attributes , Iterator itr) throws IOException {
        while(itr.hasNext()){
            Attribute attr = (Attribute) itr.next();
            int pos = attr.getPosition();
            
            outputAttrToWriter(attributes.getQName(pos),attributes.getValue(pos),_stream);
        }
        _attrResult.iterator();
    }
    
    protected void writeAttributesNS(Iterator itr) throws IOException {
        while(itr.hasNext()){
            AttributeNS attr = (AttributeNS) itr.next();
            String prefix = attr.getPrefix();
            if(prefix.length() != 0){
                _attrName.setLength(0);
                _attrName.append("xmlns:");
                _attrName.append(prefix);
                prefix = _attrName.toString();
            }else{
                prefix = "xmlns";
            }
            
            outputAttrToWriter(prefix,attr.getUri(),_stream);
        }
    }
    
    
    
    void outputTextToWriter(char [] text , int start, int length, final OutputStream writer) throws IOException {
        
        byte []toWrite = null;
        for (int i = start; i < start+length; i++) {
            char c = text[i];
            
            switch (c) {
                
                case '&' :
                    if(Arrays.equals(toWrite, __XD_))
                        writer.write(toWrite);
                    toWrite=_AMP_;
                    //writer.write(_AMP_);
                    break;
                    
                case '<' :
                    if(Arrays.equals(toWrite, __XD_))
                        writer.write(toWrite);
                    toWrite=_LT_;
                    //writer.write(_LT_);
                    break;
                    
                case '>' :
                    if(Arrays.equals(toWrite, __XD_))
                        writer.write(toWrite);
                    toWrite=_GT_;
                    //writer.write(_GT_);
                    break;
                    
                case 0xD :
                    if(Arrays.equals(toWrite, __XD_))
                        writer.write(toWrite);
                    toWrite=__XD_;
                    //writer.write(__XD_);
                    break;
                    
                case 0xA :
                    toWrite = null;
                    writeCharToUtf8(c,writer);
                    break;
                    
                default :
                    if(Arrays.equals(toWrite, __XD_)){
                       writer.write(toWrite);
                       toWrite = null;
                    }
                    writeCharToUtf8(c,writer);
                    continue;
            }
            if(toWrite != null && !Arrays.equals(toWrite, __XD_))
                writer.write(toWrite);
        }
    }
    
    final static void outputAttrToWriter(final String name, final String value, final OutputStream writer) throws IOException {
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
    
    final static void outputAttrToWriter( String prefix,final String localName,  final String value, final OutputStream writer) throws IOException {
        writer.write(' ');
        if(localName.length() != 0){
            writeStringToUtf8(prefix,writer);
            writeStringToUtf8(":",writer);
            writeStringToUtf8(localName,writer);
        }else{
            writeStringToUtf8(prefix,writer);
        }
        
        
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
        }
        int bias;
        int write;
        if (c > 0x07FF) {
            ch=(char)(c>>>12);
            write=0xE0;
            if (ch>0) {
                write |= ( ch & 0x0F);
            }
            out.write(write);
            write=0x80;
            bias=0x3F;
        } else {
            write=0xC0;
            bias=0x1F;
        }
        ch=(char)(c>>>6);
        if (ch>0) {
            write|= (ch & bias);
        }
        out.write(write);
        out.write(0x80 | ((c) & 0x3F));
        
    }
    
    final static void writeStringToUtf8(final String str,final OutputStream out) throws IOException{
        final int length=str.length();
        int i=0;
        char c;
        while (i<length) {
            c=str.charAt(i++);
            if (/*(c >= 0x0001) &&*/ (c <= 0x007F)) {
                out.write(c);
                continue;
            }
            char ch;
            int bias;
            int write;
            if (c > 0x07FF) {
                ch=(char)(c>>>12);
                write=0xE0;
                if (ch>0) {
                    write |= ( ch & 0x0F);
                }
                out.write(write);
                write=0x80;
                bias=0x3F;
            } else {
                write=0xC0;
                bias=0x1F;
            }
            ch=(char)(c>>>6);
            if (ch>0) {
                write|= (ch & bias);
            }
            out.write(write);
            out.write(0x80 | ((c) & 0x3F));
            continue;
            
        }
        
    }
    
    
    /**
     * Outputs a PI to the internal Writer.
     *
     * @param currentPI
     * @param writer where to write the things
     * @throws IOException
     */
    
    static final void outputPItoWriter(String target, String data,OutputStream writer) throws IOException {
        
        //Assume comments after document element only.
        //as this will be used to canonicalize body.
        
        writer.write('\n');
        
        writer.write(_BEGIN_PI);
        
        
        int length = target.length();
        
        for (int i = 0; i < length; i++) {
            char c=target.charAt(i);
            if (c==0x0D) {
                writer.write(__XD_);
            } else {
                writeCharToUtf8(c,writer);
            }
        }
        
        
        
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
        
    }
    
    /**
     * Method outputCommentToWriter
     *
     * @param currentComment
     * @param writer writer where to write the things
     * @throws IOException
     */
    
    static final void outputCommentToWriter(String data, OutputStream writer) throws IOException {
        //Assume comments after document element only.
        //as this will be used to canonicalize body.
        
        writer.write('\n');
        
        writer.write(_BEGIN_COMM);
        
        
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
        
    }
    
    
    
    void outputTextToWriter(String text, OutputStream writer) throws IOException{
        byte []toWrite = null;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            switch (c) {
                
                case '&' :
                    if(Arrays.equals(toWrite, __XD_))
                        writer.write(toWrite);
                    toWrite=_AMP_;
                    //writer.write(_AMP_);
                    break;
                    
                case '<' :
                    if(Arrays.equals(toWrite, __XD_))
                        writer.write(toWrite);
                    toWrite=_LT_;
                    //writer.write(_LT_);
                    break;
                    
                case '>' :
                    if(Arrays.equals(toWrite, __XD_))
                        writer.write(toWrite);
                    toWrite=_GT_;
                    //writer.write(_GT_);
                    break;
                    
                case 0xD :
                    if(Arrays.equals(toWrite, __XD_))
                        writer.write(toWrite);
                    toWrite=__XD_;
                    //writer.write(__XD_);
                    break;
                    
                case 0xA :
                    toWrite = null;
                    writeCharToUtf8(c,writer);
                    break;
                    
                default :
                    if(Arrays.equals(toWrite, __XD_)){
                       writer.write(toWrite);
                       toWrite = null;
                    }
                    writeCharToUtf8(c,writer);
                    continue;
            }
            if(toWrite != null && !Arrays.equals(toWrite, __XD_))
                writer.write(toWrite);
        }
    }
    
    
    
    /**
     * Method namespaceIsRelative
     *
     * @param namespaceValue
     * @return true if the given namespace is relative.
     */
    public static boolean namespaceIsRelative(String namespaceValue) {
        return !namespaceIsAbsolute(namespaceValue);
    }
    
    
    
    /**
     * Method namespaceIsAbsolute
     *
     * @param namespaceValue
     * @return true if the given namespace is absolute.
     */
    public static boolean namespaceIsAbsolute(String namespaceValue) {
        
        // assume empty namespaces are absolute
        if (namespaceValue.length() == 0) {
            return true;
        }
        return namespaceValue.indexOf(':')>0;
    }
    
    /*
     *
     * NamespaceContext implementation.
     *
     */
    @SuppressWarnings("unchecked")
    public static class NamespaceContextImpl implements javax.xml.namespace.NamespaceContext{
        
        AttributeNS nsDecl = new AttributeNS();
        HashMap prefixMappings = new HashMap();
        ArrayList clearDepth  = new ArrayList(10);
        
        int nsDepth;
        int resizeBy = 10;
        
        public NamespaceContextImpl(){
            //change this
            for(int i=0;i<10;i++){
                clearDepth.add(null);
            }
            
        }
        @SuppressWarnings("unchecked")
        public AttributeNS getNamespaceDeclaration(String prefix){
            Stack stack = (Stack)prefixMappings.get(prefix);
            if(stack == null || stack.empty() ){
                return null;
            }
            AttributeNS attrNS  = (AttributeNS)stack.peek();
            if(attrNS.isWritten()){
                if(debug){
                    System.out.println("depth "+nsDepth +" did not return prefix "+prefix);
                }
                return null;
            }
            if (attrNS != null && !attrNS.isWritten() && !isPrefixRedefined(stack, attrNS)) {
                 //now check if at output parents it was written
                int tmp = nsDepth -1;
                while (tmp >=0) {
                    UsedNSList uList = (UsedNSList)clearDepth.get(tmp);
                    if (uList != null && uList.getUsedPrefixList() != null &&
                            uList.getUsedPrefixList().contains(prefix)) {
                        attrNS = null;
                        break;
                    }
                    tmp--;
                }
            }
            UsedNSList uList = null;
            
            uList = (UsedNSList)clearDepth.get(nsDepth);
            if(uList == null){
                uList = new UsedNSList();
                clearDepth.set(nsDepth,uList);
            }
            if(debug){
                System.out.println("depth "+nsDepth +" return prefix "+prefix);
            }
            uList.getUsedPrefixList().add(prefix);
            return attrNS;
        }
        
        @SuppressWarnings("unchecked")
        public void declareNamespace(String prefix, String uri){
            Stack nsDecls = (Stack)prefixMappings.get(prefix);
            nsDecl.setPrefix(prefix);
            nsDecl.setUri(uri);
            if(nsDecls == null){
                nsDecls = new Stack();
                try {
                    nsDecls.add(nsDecl.clone());
                    prefixMappings.put(prefix,nsDecls);
                } catch (CloneNotSupportedException ex) {
                    throw new RuntimeException(ex);
                }
            }else if(!nsDecls.contains(nsDecl)){
                try {
                    nsDecls.add(nsDecl.clone());
                } catch (CloneNotSupportedException ex) {
                    throw new RuntimeException(ex);
                }
            } else if (nsDecls.contains(nsDecl) && "".equals(prefix)) {
                AttributeNS top = (AttributeNS)nsDecls.peek();
                if (!nsDecl.equals(top)) {
                    try {
                        nsDecls.add(nsDecl.clone());
                    } catch (CloneNotSupportedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }else{
                return;
            }
            
            
            UsedNSList uList = null;
            uList = (UsedNSList)clearDepth.get(nsDepth);
            if(uList == null){
                uList = new UsedNSList();
                clearDepth.set(nsDepth,uList);
            }
            ArrayList prefixList = uList.getPopList();
            prefixList.add(prefix);
        }

        @SuppressWarnings("unchecked")
        public void push(){
            nsDepth++;
            if(debug){
                System.out.println("--------------------Push depth----------------"+nsDepth);
            }
            if(nsDepth >=clearDepth.size()){
                clearDepth.ensureCapacity(clearDepth.size()+resizeBy);
                int len = clearDepth.size();
                for(int i=len;i< len+resizeBy;i++){
                    clearDepth.add(null);
                }
            }
        }
        
        public void pop(){
            if(nsDepth <=0){
                return;
            }
            UsedNSList ul = (UsedNSList)clearDepth.get(nsDepth);
            if(debug){
                System.out.println("---------------------pop depth----------------------"+nsDepth);
            }
            nsDepth--;
            if(ul == null ){
                return;
            }
            ArrayList pList  = ul.getPopList();
            for(int i=0;i<pList.size();i++){
                String prefix = (String)pList.get(i);
                Stack stack = (Stack)prefixMappings.get(prefix);
                if(debug){
                    System.out.println("clear prefix"+prefix);
                }
                if(!stack.isEmpty()){
                    stack.pop();
                }
            }
            
            ArrayList rList  = ul.getUsedPrefixList();
            for(int i=0;i<rList.size();i++){
                String prefix = (String)rList.get(i);
                if(pList.contains(prefix)){
                    continue;
                }
                Stack stack = (Stack)prefixMappings.get(prefix);
                if(debug){
                    System.out.println("reset written prefix"+prefix);
                }
                if(!stack.isEmpty()){
                    AttributeNS attrNS = (AttributeNS)stack.peek();
                    attrNS.setWritten(false);
                }
            }
            pList.clear();
            rList.clear();
        }
        
        public void reset(){
            nsDepth =0;
            for(int i=0;i<clearDepth.size();i++){
                UsedNSList ul = (UsedNSList)clearDepth.get(i);
                if(ul == null){
                    continue;
                }
                ul.clear();
            }
        }
        
        public String getNamespaceURI(String prefix) {
            Stack stack = (Stack)prefixMappings.get(prefix);
            if(stack == null || stack.empty() ){
                return XMLConstants.NULL_NS_URI;
            }
            AttributeNS attrNS  = (AttributeNS)stack.peek();
            return attrNS.getUri();
        }

        @SuppressWarnings("unchecked")
        public String getPrefix(String namespaceURI) {
            Set<String> keys = prefixMappings.keySet();
            
            Iterator<String> itr = keys.iterator();
            while(itr.hasNext()){
                String key = itr.next();
                Stack stack = (Stack)prefixMappings.get(key);
                if(stack == null || stack.empty() ){
                    continue;
                }
                AttributeNS attrNS  = (AttributeNS)stack.peek();
                if(namespaceURI.equals(attrNS.getUri())){
                    return key;
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public Iterator getPrefixes(String namespaceURI) {
            Set<String> keys = prefixMappings.keySet();
            ArrayList  list = new ArrayList();
            Iterator<String> itr = keys.iterator();
            while(itr.hasNext()){
                String key = itr.next();
                Stack stack = (Stack)prefixMappings.get(key);
                if(stack == null || stack.empty() ){
                    continue;
                }
                AttributeNS attrNS  = (AttributeNS)stack.peek();
                if(namespaceURI.equals(attrNS.getUri())){
                    list.add(key);
                }
            }
            
            return list.iterator();
            
        }

        private boolean isPrefixRedefined(Stack stack, AttributeNS attrNS) {
           Iterator it = stack.iterator();
           while (it.hasNext()) {
               AttributeNS aNS = (AttributeNS)it.next();
               if (!attrNS.getUri().equals(aNS.getUri())) {
                   return true;
               }
           }
           return false;
        }
    }
    
    static class UsedNSList {
        ArrayList usedPrefixList = new ArrayList();
        ArrayList popPrefixList = new ArrayList();
        
        public ArrayList getPopList(){
            return popPrefixList;
        }
        
        public ArrayList getUsedPrefixList(){
            return usedPrefixList;
        }
        
        public void clear(){
            usedPrefixList.clear();
            popPrefixList.clear();
        }
    }
    
    static class ElementName {
        //byte [] utf8Data = new UnsyncBufferedOutputStream(20);
        private UnsyncByteArrayOutputStream utf8Data = new UnsyncByteArrayOutputStream(20);
        
        
        public UnsyncByteArrayOutputStream getUtf8Data() {
            return utf8Data;
        }
        
        public void setUtf8Data(UnsyncByteArrayOutputStream utf8Data) {
            this.utf8Data = utf8Data;
        }
    }
    
    /*public static void sort(List list) {
        Object[] a = list.toArray();
        int size = a.length;
        for ( int iterator=0; iterator<size; iterator++) {
            for ( int iterator1=iterator+1; iterator1<size; iterator1++) {
                if ( ((Comparable)a[iterator1]).compareTo(a[iterator])<=0 ) {
                    swap(a,  iterator1, iterator);
                }
            }
        }
        //ListIterator<T> i = list.listIterator();
        ListIterator i = list.listIterator();
        for (int j=0; j<a.length; j++) {
            i.next();
            //i.set((T)a[j]);
            i.set(a[j]);
        }
    }*/

    @SuppressWarnings("unchecked")
    public static void sort(List list) {
        int size = list.size();
        for ( int iterator=0; iterator<size; iterator++) {
            for ( int iterator1=iterator+1; iterator1<size; iterator1++) {
                if ( ((Comparable)list.get(iterator1)).compareTo(list.get(iterator))>=0 ) {
                    swap(list,  iterator1, iterator);
                }
            }
        }
    }
    
    
    /*private static void swap(Object[] x, int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }*/
    @SuppressWarnings("unchecked")
    private static void swap(List x, int a, int b) {
        Object t = x.get(a);
        x.set(a, x.get(b));
        x.set(b, t);
    }
    
    
}
