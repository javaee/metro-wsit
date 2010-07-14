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
 * @version $Revision: 1.3.22.2 $
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
