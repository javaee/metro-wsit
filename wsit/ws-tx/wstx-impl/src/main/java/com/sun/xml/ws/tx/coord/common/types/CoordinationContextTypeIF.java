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

package com.sun.xml.ws.tx.coord.common.types;

import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.EndpointReference;


/**
 * <p>Java class for CoordinationContextType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CoordinationContextType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Identifier">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anyURI">
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://docs.oasis-open.org/ws-tx/wscoor/2006/06}Expires" minOccurs="0"/>
 *         &lt;element name="CoordinationType" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="RegistrationService" type="{http://www.w3.org/2005/08/addressing}EndpointReferenceType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface CoordinationContextTypeIF<T extends EndpointReference,E,I,C> {

    /**
     * Gets the value of the identifier property.
     * 
     * @return
     *     possible object is
     *     {@link BaseIdentifier }
     *     
     */
    public abstract BaseIdentifier<I> getIdentifier();

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseIdentifier }
     *     
     */
    public abstract void setIdentifier(BaseIdentifier<I> value);

    /**
     * Gets the value of the expires property.
     * 
     * @return
     *     possible object is
     *     {@link BaseExpires }
     *     
     */
    public abstract BaseExpires<E> getExpires();

    /**
     * Sets the value of the expires property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseExpires }
     *     
     */
    public abstract void setExpires(BaseExpires<E> value);

    /**
     * Gets the value of the coordinationType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public abstract String getCoordinationType();

    /**
     * Sets the value of the coordinationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public abstract void setCoordinationType(String value);

    /**
     * Gets the value of the registrationService property.
     * 
     * @return
     *     possible object is
     *     {@link W3CEndpointReference }
     *     
     */
    public abstract T  getRegistrationService();

    /**
     * Sets the value of the registrationService property.
     * 
     * @param value
     *     allowed object is
     *     {@link W3CEndpointReference }
     *     
     */
    public abstract void setRegistrationService(T value);

    public abstract Map<QName, String> getOtherAttributes();

    public abstract C getDelegate();


}
