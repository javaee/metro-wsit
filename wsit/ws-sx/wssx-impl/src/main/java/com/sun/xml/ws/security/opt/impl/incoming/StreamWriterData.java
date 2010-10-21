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

package com.sun.xml.ws.security.opt.impl.incoming;

import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.NamespaceContextEx;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class StreamWriterData implements com.sun.xml.ws.security.opt.crypto.StreamWriterData{
    
    private GenericSecuredHeader gsh = null;
    private SecurityHeaderElement she = null;
    private SWDNamespaceContextEx nce = new SWDNamespaceContextEx();
    private HashMap<String,String> nsDecls = null;
    private XMLStreamBuffer xmlBuffer = null;
    
    /** Creates a new instance of StreamWriterData */
    public StreamWriterData(GenericSecuredHeader gsh,HashMap<String,String> nsDecls) {
        this.gsh = (GenericSecuredHeader)gsh;
        this.nsDecls = nsDecls;
        addNSDecls();
    }
    
    public StreamWriterData(SecurityHeaderElement she,HashMap<String,String> nsDecls) {
        this.she = (SecurityHeaderElement)she;
        this.nsDecls = nsDecls;
        addNSDecls();
    }
    
    public StreamWriterData(XMLStreamBuffer buffer){
        this.xmlBuffer = buffer;
    }
    
    public Object getDereferencedObject(){
        if(she != null)
            return she;
        else
            return gsh;
    }
    
    private void addNSDecls(){
        Iterator<String> itr  = nsDecls.keySet().iterator();
        while(itr.hasNext()){
            String prefix = itr.next();
            String uri = nsDecls.get(prefix);
            nce.add(prefix,uri);
        }
    }
    
    public NamespaceContextEx getNamespaceContext() {
        return nce;
    }
    
    public void write(XMLStreamWriter writer) throws XMLStreamException {
        if(xmlBuffer != null){
            xmlBuffer.writeToXMLStreamWriter(writer);
        }else if(gsh != null){
            gsh.writeTo(writer);
        }else{
            ((SecurityElementWriter)she).writeTo(writer);
        }
    }
    
    static class SWDNamespaceContextEx implements org.jvnet.staxex.NamespaceContextEx {
        
        private ArrayList<org.jvnet.staxex.NamespaceContextEx.Binding> list = new ArrayList<org.jvnet.staxex.NamespaceContextEx.Binding>();
        /** Creates a new instance of NamespaceContextEx */
        public SWDNamespaceContextEx() {
        }
        
        public SWDNamespaceContextEx(boolean soap12Version) {
            if(soap12Version){
                this.add("S","http://www.w3.org/2003/05/soap-envelope" );//SOAP 12
            }else{
                this.add("S","http://schemas.xmlsoap.org/soap/envelope/" );
            }
        }
        
        public void add(String prefix,String uri){
            list.add(new BindingImpl(prefix,uri));
        }
        
        public Iterator<org.jvnet.staxex.NamespaceContextEx.Binding> iterator() {
            return list.iterator();
        }
        
        public String getNamespaceURI(String prefix) {
            for(org.jvnet.staxex.NamespaceContextEx.Binding binding : list){
                if(prefix.equals(binding.getPrefix())){
                    return binding.getNamespaceURI();
                }
            }
            return null;
        }
        
        public String getPrefix(String namespaceURI) {
            for(org.jvnet.staxex.NamespaceContextEx.Binding binding : list){
                if(namespaceURI.equals(binding.getNamespaceURI())){
                    return binding.getPrefix();
                }
            }
            return null;
        }
        
        public Iterator getPrefixes(final String namespaceURI) {
            return new Iterator(){
                int index = 0;
                public boolean hasNext(){
                    if(index++ < list.size() && move()){
                        return true;
                    }
                    return false;
                }
                
                public Object next(){
                    return list.get(index).getPrefix();
                }
                
                public void remove() {
                    throw new UnsupportedOperationException();
                }
                
                private boolean move(){
                    boolean found = false;
                    do{
                        if(namespaceURI.equals(list.get(index).getNamespaceURI())){
                            found = true;
                            break;
                        }else{
                            index++;
                        }
                    }while(index < list.size());
                    return found;
                }
            };
        }
        
        static class BindingImpl implements org.jvnet.staxex.NamespaceContextEx.Binding{
            private String prefix="";
            private String uri="";
            public BindingImpl(String prefix,String uri){
                this.prefix = prefix;
                this.uri = uri;
                if(this.prefix == null){
                    this.prefix = "";
                }
            }
            
            public String getPrefix() {
                return prefix;
            }
            
            public String getNamespaceURI() {
                return uri;
            }
        }
    }
    
}
