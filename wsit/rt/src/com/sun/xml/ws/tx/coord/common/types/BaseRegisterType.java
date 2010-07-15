/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.tx.coord.common.types;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import java.util.List;
import java.util.Map;


/**
 * <p>Java class for RegisterType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegisterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ProtocolIdentifier" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="ParticipantProtocolService" type="{http://www.w3.org/2005/08/addressing}EndpointReferenceType"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public abstract class BaseRegisterType<T extends EndpointReference,K> {

    protected K delegate;

    protected BaseRegisterType(K delegate) {
        this.delegate = delegate;
    }

    public K getDelegate() {
        return delegate;
    }

    /**
     * Gets the value of the protocolIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public abstract String getProtocolIdentifier();

    /**
     * Sets the value of the protocolIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public abstract void setProtocolIdentifier(String value);

    /**
     * Gets the value of the participantProtocolService property.
     * 
     * @return
     *     possible object is
     *     {@link EndpointReference }
     *     
     */
    public abstract T getParticipantProtocolService();

    /**
     * Sets the value of the participantProtocolService property.
     * 
     * @param value
     *     allowed object is
     *     {@link EndpointReference }
     *     
     */
    public abstract void setParticipantProtocolService(T value);

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
    public abstract List<Object> getAny();

    public abstract Map<QName, String> getOtherAttributes();

    public abstract boolean isDurable();
    public abstract boolean isVolatile();

}
