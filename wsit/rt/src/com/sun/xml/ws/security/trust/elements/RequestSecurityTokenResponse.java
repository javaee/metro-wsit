/*
 * $Id: RequestSecurityTokenResponse.java,v 1.6.20.2 2010-07-14 14:00:19 m_potociar Exp $
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

package com.sun.xml.ws.security.trust.elements;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import com.sun.xml.ws.api.security.trust.Status;

/**
 * @author Kumar Jayanti
 */
public interface RequestSecurityTokenResponse extends WSTrustElementBase, BaseSTSResponse {
    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link Object }
     * 
     * 
     */
    List<Object> getAny();

    /**
     * Gets the value of the context property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    String getContext();

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    Map<QName, String> getOtherAttributes();

  
    /**
     * set a SignChallengeResponse
     */
    void setSignChallengeResponse(SignChallengeResponse challenge);
    
    /**
     * get SignChallengeResponse element if any, null otherwise
     */
    SignChallengeResponse getSignChallengeResponse();
    
    /**
     * set an Authenticator
     */
    void setAuthenticator(Authenticator authenticator);

    /**
     * get Authenticator if set, null otherwise
     */
    Authenticator getAuthenticator();

    /**
     * Set the requestedProofToken on the security token response
     *
     * @param proofToken
     */
    void setRequestedProofToken(RequestedProofToken proofToken);

    /**
     * Get the requestedProofToken
     *
     * @return RequestedProofToken, null if none present
     */
    RequestedProofToken getRequestedProofToken();

    /**
     * Set the requestedSecurityToken on the security token response
     *
     * @param securityToken
     */
    void setRequestedSecurityToken(RequestedSecurityToken securityToken);

    /**
     * Get the requested Security Token
     *
     * @return RequestedSecurityToken
     */
    RequestedSecurityToken getRequestedSecurityToken();

    /**
     * Set the requestedAttachedReference on the security token response
     * @param reference
     */
    void setRequestedAttachedReference(RequestedAttachedReference reference);

    /**
     * Get the requestedAttachedReference.
     *
     * @return RequestedAttachedReference, null if none present
     */
    RequestedAttachedReference getRequestedAttachedReference();

    /**
     * Set the requestedUnattachedReference on the security token response
     * @param reference
     */
    void setRequestedUnattachedReference(RequestedUnattachedReference reference);

    /**
     * Get the requestedUnattachedReference.
     *
     * @return RequestedUnattachedReference, null if none present
     */
    RequestedUnattachedReference getRequestedUnattachedReference();
    
    void setRequestedTokenCancelled(RequestedTokenCancelled rtc);
    
    RequestedTokenCancelled getRequestedTokenCancelled();
    
    Status getStatus();
    
    void setStatus(Status status);
}

