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

package com.sun.xml.ws.security.trust.impl.ic;

import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.impl.WSTrustContractImpl;
import com.sun.xml.wss.WSITXMLFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Jiandong Guo
 */
public class ICContractImpl extends WSTrustContractImpl{
    @Override
    protected void handleExtension(BaseSTSRequest request, BaseSTSResponse response, IssuedTokenContext context) throws WSTrustException{
        @SuppressWarnings("unchecked") final Map<QName, List<String>> claimedAttributes = (Map<QName, List<String>>) context.getOtherProperties().get(IssuedTokenContext.CLAIMED_ATTRUBUTES);
        handleDisplayToken((RequestSecurityToken)request, (RequestSecurityTokenResponse)response, claimedAttributes);
    }
    
    private void handleDisplayToken(RequestSecurityToken rst, RequestSecurityTokenResponse rstr, Map<QName, List<String>> claimedAttrs)throws WSTrustException{
        List<Object> list = rst.getExtensionElements();
        boolean displayToken = false;
        for (int i =0; i < list.size(); i++){
            Object ele = list.get(i);
            if ((ele instanceof Element)){
                String localName = ((Element)ele).getLocalName();
                if ("RequestDisplayToken".equals(localName)){
                    displayToken = true;
                    break;
                }
            }
        }
        if (displayToken){
            // Create RequestedDisplayToken
            try {
                final DocumentBuilderFactory dbf = WSITXMLFactory.createDocumentBuilderFactory(WSITXMLFactory.DISABLE_SECURE_PROCESSING);
                dbf.setNamespaceAware(true);
                final DocumentBuilder builder = dbf.newDocumentBuilder();
                Document doc = builder.newDocument();
                Element rdt = doc.createElementNS("http://schemas.xmlsoap.org/ws/2005/05/identity", "RequestedDisplayToken");
                rdt.setAttribute("xmlns", "http://schemas.xmlsoap.org/ws/2005/05/identity");
                Element dt = doc.createElementNS("http://schemas.xmlsoap.org/ws/2005/05/identity", "DisplayToken");
                dt.setAttribute("xml:lang", "en-us");
                rdt.appendChild(dt);
                final Set<Map.Entry<QName, List<String>>> entries = claimedAttrs.entrySet();
                for(Map.Entry<QName, List<String>> entry : entries){
                    final QName attrKey = entry.getKey();
                    final List<String> values = entry.getValue();
                    if (values != null && values.size() > 0){
                        if (!STSAttributeProvider.NAME_IDENTIFIER.equals(attrKey.getLocalPart())){
                            Element dc = doc.createElementNS("http://schemas.xmlsoap.org/ws/2005/05/identity", "DisplayClaim");
                            dc.setAttribute("xmlns", "http://schemas.xmlsoap.org/ws/2005/05/identity");
                            String uri = attrKey.getNamespaceURI()+"/" + attrKey.getLocalPart();
                            dc.setAttribute("Uri", uri);
                            dt.appendChild(dc);
                            Element dtg = doc.createElementNS("http://schemas.xmlsoap.org/ws/2005/05/identity", "DisplayTag");
                            dtg.appendChild(doc.createTextNode(attrKey.getLocalPart()));
                            dc.appendChild(dtg);
                            
                            String displayValue = values.get(0);
                            Element dtv = doc.createElementNS("http://schemas.xmlsoap.org/ws/2005/05/identity", "DisplayValue");
                           // if ("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/privatepersonalidentifier".equals(uri)){
                              //  displayValue = WSTrustUtils.createFriendlyPPID(displayValue);
                           // }
                            dtv.appendChild(doc.createTextNode(displayValue));
                            dc.appendChild(dtv);
                        }
                    }
                }
                rstr.getAny().add(rdt);
            }catch (Exception ex){
                throw new WSTrustException(ex.getMessage(), ex);
            }   
        }
    }
}
