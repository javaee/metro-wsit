/*
 * $Id: RequestSecurityTokenResponseCollectionImpl.java,v 1.1 2007-08-23 12:40:56 shyam_rao Exp $
 */

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

package com.sun.xml.ws.security.trust.impl.wssx.elements;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestSecurityTokenResponseCollectionType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.ObjectFactory;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestSecurityTokenResponseType;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;

/**
 * @author Manveen Kaur.
 */
public class RequestSecurityTokenResponseCollectionImpl extends RequestSecurityTokenResponseCollectionType
        implements RequestSecurityTokenResponseCollection {
    
    protected List<RequestSecurityTokenResponse> requestSecurityTokenResponse;
    
    public RequestSecurityTokenResponseCollectionImpl() {
        // empty ctor
    }

    public RequestSecurityTokenResponseCollectionImpl(RequestSecurityTokenResponse rstr) {
        addRequestSecurityTokenResponse(rstr);        
    }
    public RequestSecurityTokenResponseCollectionImpl(URI tokenType, URI context, RequestedSecurityToken token, AppliesTo scopes,
            RequestedAttachedReference attached, RequestedUnattachedReference unattached, RequestedProofToken proofToken, Entropy entropy, Lifetime lt) {
        RequestSecurityTokenResponse rstr = new RequestSecurityTokenResponseImpl(tokenType, context, token, scopes,
                attached, unattached, proofToken, entropy, lt, null);
        addRequestSecurityTokenResponse(rstr);
        
    }
    
    public RequestSecurityTokenResponseCollectionImpl(RequestSecurityTokenResponseCollectionType rstrcType)
    throws Exception {
        List<Object> list = rstrcType.getRequestSecurityTokenResponse();
        System.out.println("******* response size *****" + list.size());
        for (int i = 0; i < list.size(); i++) {

            RequestSecurityTokenResponseType rst = null;
            Object object = list.get(i);
            if (object instanceof JAXBElement){
                JAXBElement obj = (JAXBElement)object;

                String local = obj.getName().getLocalPart();
                if (local.equalsIgnoreCase("RequestSecurityTokenResponse")) {
                    rst = (RequestSecurityTokenResponseType)obj.getValue();
                }
            } else{
                rst = (RequestSecurityTokenResponseType)object;
            }
            addRequestSecurityTokenResponse(new RequestSecurityTokenResponseImpl(rst));
        }
    }
    
    public List<RequestSecurityTokenResponse> getRequestSecurityTokenResponses() {
        if (requestSecurityTokenResponse == null) {
            requestSecurityTokenResponse = new ArrayList<RequestSecurityTokenResponse>();
        }
        return this.requestSecurityTokenResponse;
    }
    
    public void addRequestSecurityTokenResponse(RequestSecurityTokenResponse rstr){
        System.out.println("******* rstr added in rstrc*****");
         getRequestSecurityTokenResponses().add(rstr);
         
        JAXBElement<RequestSecurityTokenResponseType> rstrEl =
                (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
         getRequestSecurityTokenResponse().add(rstrEl);
    }
    
}
