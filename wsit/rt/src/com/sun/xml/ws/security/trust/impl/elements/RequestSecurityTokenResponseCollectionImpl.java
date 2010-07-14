/*
 * $Id: RequestSecurityTokenResponseCollectionImpl.java,v 1.7.22.2 2010-07-14 14:01:13 m_potociar Exp $
 */

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
            addRequestSecurityTokenResponse(new RequestSecurityTokenResponseImpl(list.get(i)));
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
