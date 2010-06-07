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
package com.sun.xml.ws.rx.rm.protocol.wsrm200502;

import com.sun.xml.ws.rx.rm.protocol.CreateSequenceData;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;

import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
"acksTo",
"any",
"expires",
"offer",
"securityTokenReference"
})
@XmlRootElement(name = "CreateSequence", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
public class CreateSequenceElement {

    @XmlElement(name = "AcksTo", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected EndpointReference acksTo;
    @XmlAnyElement(lax = true)
    protected List<Object> any = new ArrayList<Object>();
    @XmlElement(name = "Expires", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected Expires expires;
    @XmlElement(name = "Offer", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected OfferType offer;
    @XmlElement(name = "SecurityTokenReference", namespace = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd")
    private SecurityTokenReferenceType securityTokenReference;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    public CreateSequenceElement() {
    }

    public CreateSequenceElement(CreateSequenceData data) {
        this();

        acksTo = data.getAcksToEpr();
        if (!data.doesNotExpire()) {
            expires = new Expires(data.getDuration());
        }

        if (data.getOfferedSequenceId() != null) {
            this.offer = new OfferType();
            offer.setId(data.getOfferedSequenceId());
            if (!data.offeredSequenceDoesNotExpire()) {
                offer.setExpires(new Expires(data.getOfferedSequenceExpiry()));
            }
        }
        if (data.getStrType() != null) {
            securityTokenReference = data.getStrType();
        }
    }

    public CreateSequenceData.Builder toDataBuilder() {
        final CreateSequenceData.Builder dataBuilder = CreateSequenceData.getBuilder(this.getAcksTo());
        dataBuilder.strType(securityTokenReference);

        if (expires != null) {
            dataBuilder.duration(expires.getDuration());
        }

        if (offer != null) {
            dataBuilder.offeredInboundSequenceId(offer.getId());
            if (offer.getExpires() != null) {
                dataBuilder.offeredSequenceExpiry(offer.getExpires().getDuration());
            }
        }

        return dataBuilder;
    }

    /**
     * Gets the value of the acksTo property.
     */
    public EndpointReference getAcksTo() {
        return acksTo;
    }

    /**
     * Sets the value of the acksTo property.
     */
    public void setAcksTo(EndpointReference value) {
        this.acksTo = value;
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
        this.expires = (Expires) value;
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
