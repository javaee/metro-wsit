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

package com.sun.xml.ws.security.opt.impl.util;



import java.util.ArrayList;

import java.util.Iterator;

import  com.sun.xml.wss.impl.MessageConstants;

/**

 *

 * @author K.Venugopal@sun.com

 */

public class NamespaceContextEx implements org.jvnet.staxex.NamespaceContextEx {

    private boolean addedWSSNS = false;
    private boolean samlNS = false;
    private boolean dsNS = false;
    private boolean encNS = false;
    private boolean scNS = false;
    private boolean exc14NS = false;
    private boolean addedWSS11NS = false;
    private ArrayList<org.jvnet.staxex.NamespaceContextEx.Binding> list = new ArrayList<org.jvnet.staxex.NamespaceContextEx.Binding>();
    private boolean addedXSDNS = false;
    /** Creates a new instance of NamespaceContextEx */

    public NamespaceContextEx() {
        this.add("S",MessageConstants.SOAP_1_1_NS );
        addDefaultNSDecl();
    }
    
    
    public NamespaceContextEx(boolean soap12Version) {
        if(soap12Version){
            this.add("S",MessageConstants.SOAP_1_2_NS );//SOAP 12
        }else{
            this.add("S",MessageConstants.SOAP_1_1_NS );
        }    
        addDefaultNSDecl();
    }
    
    private void addDefaultNSDecl(){
        
    }
    
    public void addWSSNS(){
        if(!addedWSSNS){
            this.add(MessageConstants.WSSE_PREFIX, MessageConstants.WSSE_NS);
            this.add(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS);
            addedWSSNS = true;
        }
    }
    
    public void addWSS11NS(){
         if(!addedWSS11NS){
            this.add(MessageConstants.WSSE11_PREFIX, MessageConstants.WSSE11_NS);            
            addedWSS11NS = true;
        }
    }
    
    public void addXSDNS(){
        if(!addedXSDNS){
            this.add("xs", MessageConstants.XSD_NS);
            addedXSDNS = true;
        }
    }
    
    public void addSignatureNS(){
        addWSSNS();
        if(!dsNS){
            this.add(MessageConstants.DSIG_PREFIX, MessageConstants.DSIG_NS);
            dsNS = true;
        }
    }
    
    public void addEncryptionNS(){
        addWSSNS();
        if(!encNS){
            this.add(MessageConstants.XENC_PREFIX, MessageConstants.XENC_NS);
            encNS = true;
        }
    }
    
    public void addSAMLNS(){
        if(!samlNS){
            this.add(MessageConstants.SAML_PREFIX, MessageConstants.SAML_v1_0_NS);
            samlNS = true;
        }
    }
    
    public void addSCNS(){
        if(!scNS){
            this.add(MessageConstants.WSSC_PREFIX, MessageConstants.WSSC_NS);
            scNS = true;
        }
    }
    
    public void addExc14NS(){
        if(!exc14NS){
            this.add("exc14n", MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
            exc14NS = true;
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
                if( ++index < list.size() && move()){
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
        }
        
        public String getPrefix() {
            return prefix;
        }
        
        public String getNamespaceURI() {
            return uri;
        }
        
    }
    
}
