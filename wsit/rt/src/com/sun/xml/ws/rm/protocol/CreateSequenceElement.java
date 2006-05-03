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


package com.sun.xml.ws.rm.protocol;


import com.sun.xml.ws.security.impl.bindings.SecurityTokenReferenceType;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import javax.xml.ws.addressing.EndpointReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>Java class for CreateSequenceType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CreateSequenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.xmlsoap.org/ws/2005/02/rm}AcksTo"/>
 *         &lt;element ref="{http://schemas.xmlsoap.org/ws/2005/02/rm}Expires" minOccurs="0"/>
 *         &lt;element name="Offer" type="{http://schemas.xmlsoap.org/ws/2005/02/rm}OfferType" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateSequenceType", propOrder = {
        "any",
        "expires",
        "offer",
        "securityTokenReference"

        })
@XmlRootElement(name="CreateSequence",namespace="http://schemas.xmlsoap.org/ws/2005/02/rm")
public class CreateSequenceElement {

    @XmlAnyElement(lax = true)
    protected List<Object> any = new ArrayList();
    @XmlElement(name = "Expires", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected Expires expires;
    @XmlElement(name = "Offer", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected OfferType offer;
    @XmlElement(name="SecurityTokenReference", namespace="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd")
    private SecurityTokenReferenceType securityTokenReference;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the acksTo property.
     *
     * @return
     *     possible object is
     *     {@link EndpointReference }
     *
     */
    public EndpointReference getAcksTo() {
        for (int i = 0 ; i < any.size(); i++) {
            if (any.get(i) instanceof EndpointReference) {
                return (EndpointReference)any.get(i);
            }
        }
        return null;
    }

    /**
     * Sets the value of the acksTo property.
     *
     * @param value
     *     allowed object is
     *     {@link EndpointReference }
     *
     */
    public void setAcksTo(EndpointReference value) {
        this.any.add(value);
    }

    /**
     * Gets the value of the expires property.
     * 
     * @return
     *     possible object is
     *     {@link Expires }
     *     
     */
    public Expires getExpires() {
        return expires;
    }

    /**
     * Sets the value of the expires property.
     * 
     * @param value
     *     allowed object is
     *     {@link Expires }
     *     
     */
    public void setExpires(Expires value) {
        this.expires = value;
    }

    /**
     * Gets the value of the offer property.
     * 
     * @return
     *     possible object is
     *     {@link OfferType }
     *     
     */
    public OfferType getOffer() {
        return offer;
    }

    /**
     * Sets the value of the offer property.
     * 
     * @param value
     *     allowed object is
     *     {@link OfferType }
     *     
     */
    public void setOffer(OfferType value) {
        this.offer = value;
    }

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
     * {@link Object }
     * {@link Element }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

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
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public SecurityTokenReferenceType getSecurityTokenReference() {
        return securityTokenReference;
    }

    public void setSecurityTokenReference(SecurityTokenReferenceType securityTokenReference) {
        this.securityTokenReference = securityTokenReference;
    }

}
