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
package com.sun.xml.ws.tx.coordinator;

import com.sun.xml.ws.tx.common.Constants;

import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import java.util.Map;

/**
 * This interface hides the differences between the 2004/10 member submission and then 2006/03
 * OASIS versions of CoordinationContext
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.1 $
 * @since 1.0
 */
public interface CoordinationContextInterface {
    /**
     * Gets the value of the identifier property.
     *
     * @return the identifier value
     */
    String getIdentifier();

    /**
     * Sets the value of the identifier property.
     *
     * @param identifier the identifier value
     */
    void setIdentifier(String identifier);

    /**
     * Gets the value of the expires property in milliseconds.
     *
     * @return the value of the expires property in milliseconds
     */
    long getExpires();

    /**
     * Sets the value of the expires property in milliseconds.
     * <p/>
     * The expires value can not be negative.  Calling setExpires(0L)
     * unsets the underlying Expires element in the CoordinationContext.
     *
     * @param expires the expires value in milliseconds
     */
    void setExpires(long expires);

    /**
     * Gets the value of the coordinationType property.
     *
     * @return the value of the coordinationType
     */
    String getCoordinationType();

    /**
     * Sets the value of the coordinationType property.
     *
     * @param coordinationType either {@link Constants#WSAT_2004_PROTOCOL} or
     *                         {@link Constants#WSAT_OASIS_NSURI}
     */
    void setCoordinationType(String coordinationType);

    /**
     * Gets the value of the registrationService property.
     *
     * @return the EndpointReference of the registration service
     */
    EndpointReference getRegistrationService();

    /**
     * Sets the value of the registrationService property.
     *
     * @param registrationService the EndpointReference of the registration service
     */
    void setRegistrationService(EndpointReference registrationService);

    /**
     * Return the underlying JAXB generated coordination context type.
     * <p/>
     * This will either be a (@link com.sun.xml.ws.tx.webservice.member.coord.CoordinationContext} or a
     * OASIS CoordinationContext.
     * <p/>
     *
     * @return Return the underlying JAXB generated coordination context type
     */
    Object getValue();

    /**
     * Gets the underlying map that contains attributes that aren't bound to
     * any typed property.
     * <p/>
     * the map is keyed by the name of the attribute and
     * the value is the string value of the attribute.
     * <p/>
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     *
     * @return always non-null
     */
    public Map<QName, String> getOtherAttributes();

    /**
     * Set the root registration service EPR for this coordination context
     *
     * @param rootRegistrationService
     */
    public void setRootCoordinatorRegistrationService(EndpointReference rootRegistrationService);

    /**
     * Get the root registration service EPR
     *
     * @return the root registration service epr
     */
    public EndpointReference getRootRegistrationService();

}
