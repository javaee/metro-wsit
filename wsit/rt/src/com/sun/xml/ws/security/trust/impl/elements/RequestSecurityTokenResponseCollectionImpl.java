/*
 * $Id: RequestSecurityTokenResponseCollectionImpl.java,v 1.4 2007-01-26 05:57:46 jdg6688 Exp $
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

package com.sun.xml.ws.security.trust.impl.elements;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenResponseCollectionType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenResponseType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Manveen Kaur.
 */
public class RequestSecurityTokenResponseCollectionImpl extends RequestSecurityTokenResponseCollectionType
        implements RequestSecurityTokenResponseCollection {
    
    protected List<RequestSecurityTokenResponse> requestSecurityTokenResponses;
    
    public RequestSecurityTokenResponseCollectionImpl() {
        // empty ctor
    }
    
    public RequestSecurityTokenResponseCollectionImpl(URI tokenType, URI context, RequestedSecurityToken token, AppliesTo scopes,
            RequestedAttachedReference attached, RequestedUnattachedReference unattached, RequestedProofToken proofToken, Entropy entropy, Lifetime lt) {
        final RequestSecurityTokenResponse rstr = new RequestSecurityTokenResponseImpl(tokenType, context, token, scopes,
                attached, unattached, proofToken, entropy, lt, null);
        addRequestSecurityTokenResponse(rstr);
        
    }
    
    public RequestSecurityTokenResponseCollectionImpl(RequestSecurityTokenResponseCollectionType rstrcType)
    throws URISyntaxException,WSTrustException{
        final List<RequestSecurityTokenResponseType> list = rstrcType.getRequestSecurityTokenResponse();
        for (int i = 0; i < list.size(); i++) {
            addRequestSecurityTokenResponse(new RequestSecurityTokenResponseImpl((RequestSecurityTokenResponseType)list.get(i)));
        }
    }
    
    public List<RequestSecurityTokenResponse> getRequestSecurityTokenResponses() {
        if (requestSecurityTokenResponses == null) {
            requestSecurityTokenResponses = new ArrayList<RequestSecurityTokenResponse>();
        }
        return this.requestSecurityTokenResponses;
    }
    
    public final void addRequestSecurityTokenResponse(final RequestSecurityTokenResponse rstr){
         getRequestSecurityTokenResponses().add(rstr);
         
        //JAXBElement<RequestSecurityTokenResponseType> rstrEl =
               // (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
         getRequestSecurityTokenResponse().add((RequestSecurityTokenResponseType)rstr);
    }
}
