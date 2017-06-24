/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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
 * $Id: RequestSecurityToken.java,v 1.2 2010-10-21 15:35:40 snajper Exp $
 */

package com.sun.xml.ws.security.trust.elements;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import java.net.URI;

import com.sun.xml.ws.api.security.trust.Claims;

/**
 * @author Kumar Jayanti
 */
public interface RequestSecurityToken extends WSTrustElementBase, BaseSTSRequest {
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
     */
    List<Object> getAny();

    /**
     * Gets the value of the context property.
     * 
     * 
     * @return {@link String }
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
     * 
     * @return always non-null
     */
    Map<QName, String> getOtherAttributes();

   /**
    * Get the type of request, specified as a URI.
    * The URI indicates the class of function that is requested.
    * @return {@link URI}
    */
    URI getRequestType();

   /**
     * Set the type of request, specified as a URI.
     * @param requestType {@link URI}
     */
    void setRequestType(URI requestType);


    /**
      * Set the desired claims settings for the requested token
      */
     void setClaims(Claims claims);

     /**
      * Get the desired claims settings for the token if specified, null otherwise
      */
     Claims getClaims();

     /**
      * Set the Participants Sharing the requested Token
      */
     void setParticipants(Participants participants);
     
     void setValidateTarget(ValidateTarget valigateTarget);
     
     void setRenewTarget(RenewTarget renewTarget);
     
     void setCancelTarget(CancelTarget cancelTarget);
     
     /**
      * Get the participants sharing the token if specified, null otherwise 
      */
     Participants getParticipants();
     
     RenewTarget getRenewTarget();
     
     CancelTarget getCancelTarget();
     
     ValidateTarget getValidateTarget();
     
     void setSecondaryParameters(SecondaryParameters sp);
     
     SecondaryParameters getSecondaryParameters(); 
     
     public List<Object> getExtensionElements();
     
     void setActAs(ActAs actAs);

     ActAs getActAs();

}

