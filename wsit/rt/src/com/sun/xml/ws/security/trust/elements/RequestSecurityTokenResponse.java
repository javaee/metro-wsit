/*
 * $Id: RequestSecurityTokenResponse.java,v 1.1 2006-05-03 22:57:19 arungupta Exp $
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

package com.sun.xml.ws.security.trust.elements;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * @author Kumar Jayanti
 */
public interface RequestSecurityTokenResponse extends WSTrustElementBase {
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
}

