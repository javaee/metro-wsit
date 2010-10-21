/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.rx.rm.protocol.wsrm200702;

import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;

import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://docs.oasis-open.org/ws-rx/wsrm/200702}Identifier"/>
 *         &lt;choice>
 *           &lt;sequence>
 *             &lt;choice>
 *               &lt;element name="AcknowledgementRange" maxOccurs="unbounded">
 *                 &lt;complexType>
 *                   &lt;complexContent>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                       &lt;sequence>
 *                       &lt;/sequence>
 *                       &lt;attribute name="Upper" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
 *                       &lt;attribute name="Lower" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
 *                     &lt;/restriction>
 *                   &lt;/complexContent>
 *                 &lt;/complexType>
 *               &lt;/element>
 *               &lt;element name="None">
 *                 &lt;complexType>
 *                   &lt;complexContent>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                       &lt;sequence>
 *                       &lt;/sequence>
 *                     &lt;/restriction>
 *                   &lt;/complexContent>
 *                 &lt;/complexType>
 *               &lt;/element>
 *             &lt;/choice>
 *             &lt;element name="Final" minOccurs="0">
 *               &lt;complexType>
 *                 &lt;complexContent>
 *                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                     &lt;sequence>
 *                     &lt;/sequence>
 *                   &lt;/restriction>
 *                 &lt;/complexContent>
 *               &lt;/complexType>
 *             &lt;/element>
 *           &lt;/sequence>
 *           &lt;element name="Nack" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" maxOccurs="unbounded"/>
 *         &lt;/choice>
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
@XmlType(name = "", propOrder = {
"identifier",
"acknowledgementRange",
"none",
"_final",
"bufferRemaining",
"nack",
"any"
})
@XmlRootElement(name = "SequenceAcknowledgement", namespace = "http://docs.oasis-open.org/ws-rx/wsrm/200702")
public class SequenceAcknowledgementElement {

    @XmlElement(name = "Identifier", required = true, namespace = "http://docs.oasis-open.org/ws-rx/wsrm/200702")
    protected Identifier identifier;
    @XmlElement(name = "AcknowledgementRange", namespace = "http://docs.oasis-open.org/ws-rx/wsrm/200702")
    protected List<SequenceAcknowledgementElement.AcknowledgementRange> acknowledgementRange;
    @XmlElement(name = "None", namespace = "http://docs.oasis-open.org/ws-rx/wsrm/200702")
    protected SequenceAcknowledgementElement.None none;
    @XmlElement(name = "Final", namespace = "http://docs.oasis-open.org/ws-rx/wsrm/200702")
    protected SequenceAcknowledgementElement.Final _final;
    @XmlElement(name = "BufferRemaining", namespace = "http://schemas.microsoft.com/ws/2006/05/rm")
    protected Integer bufferRemaining;
    @XmlElement(name = "Nack", namespace = "http://docs.oasis-open.org/ws-rx/wsrm/200702")
    @XmlSchemaType(name = "unsignedLong")
    protected List<BigInteger> nack;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the identifier property.
     * 
     * @return
     *     possible object is
     *     {@link Identifier }
     *     
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link Identifier }
     *     
     */
    public void setIdentifier(Identifier value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the acknowledgementRange property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the acknowledgementRange property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAcknowledgementRange().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SequenceAcknowledgement.AcknowledgementRange }
     * 
     * 
     */
    public List<SequenceAcknowledgementElement.AcknowledgementRange> getAcknowledgementRange() {
        if (acknowledgementRange == null) {
            acknowledgementRange = new ArrayList<SequenceAcknowledgementElement.AcknowledgementRange>();
        }
        return this.acknowledgementRange;
    }

    /**
     * Gets the value of the none property.
     * 
     * @return
     *     possible object is
     *     {@link SequenceAcknowledgementElement.None }
     *     
     */
    public SequenceAcknowledgementElement.None getNone() {
        return none;
    }

    /**
     * Sets the value of the none property.
     * 
     * @param value
     *     allowed object is
     *     {@link SequenceAcknowledgementElement.None }
     *     
     */
    public void setNone(SequenceAcknowledgementElement.None value) {
        this.none = value;
    }

    /**
     * Gets the value of the final property.
     * 
     * @return
     *     possible object is
     *     {@link SequenceAcknowledgement.Final }
     *     
     */
    public SequenceAcknowledgementElement.Final getFinal() {
        return _final;
    }

    /**
     * Sets the value of the final property.
     * 
     * @param value
     *     allowed object is
     *     {@link SequenceAcknowledgementElement.Final }
     *     
     */
    public void setFinal(SequenceAcknowledgementElement.Final value) {
        this._final = value;
    }

    /**
     * Gets the value of the nack property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nack property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNack().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BigInteger }
     * 
     * 
     */
    public List<BigInteger> getNack() {
        if (nack == null) {
            nack = new ArrayList<BigInteger>();
        }
        return this.nack;
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

    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *       &lt;/sequence>
     *       &lt;attribute name="Upper" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
     *       &lt;attribute name="Lower" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedLong" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class AcknowledgementRange {

        @XmlAttribute(name = "Upper", required = true)
        @XmlSchemaType(name = "unsignedLong")
        protected BigInteger upper;
        @XmlAttribute(name = "Lower", required = true)
        @XmlSchemaType(name = "unsignedLong")
        protected BigInteger lower;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        /**
         * Gets the value of the upper property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getUpper() {
            return upper;
        }

        /**
         * Sets the value of the upper property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setUpper(BigInteger value) {
            this.upper = value;
        }

        /**
         * Gets the value of the lower property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getLower() {
            return lower;
        }

        /**
         * Sets the value of the lower property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setLower(BigInteger value) {
            this.lower = value;
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
    }

    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Final {
    }

    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class None {
    }

    public void setId(String idString) {
        com.sun.xml.ws.rx.rm.protocol.wsrm200702.Identifier newId = new Identifier();
        newId.setValue(idString);
        setIdentifier(newId);
    }

    public String getId() {
        return getIdentifier().getValue();
    }

    public int getBufferRemaining() {
        if (bufferRemaining == null) {
            return -1;
        }
        return bufferRemaining;
    }

    public void setBufferRemaining(int value) {
        bufferRemaining = value;
    }

    public void addAckRange(long lower, long upper) {
        if (nack != null) {
            throw new IllegalArgumentException(LocalizationMessages.WSRM_4002_BOTH_ACKS_AND_NACKS_MESSAGE());
        }
        //check validity of indices
        if (lower > upper) {
            throw new IllegalArgumentException(LocalizationMessages.WSRM_4003_UPPERBOUND_LESSTHAN_LOWERBOUND_MESSAGE());
        }

        //TODO Further validity checking
        SequenceAcknowledgementElement.AcknowledgementRange range = new SequenceAcknowledgementElement.AcknowledgementRange();
        range.setLower(BigInteger.valueOf(lower));
        range.setUpper(BigInteger.valueOf(upper));
        getAcknowledgementRange().add(range);
    }

    public void addNack(long index) {
        if (acknowledgementRange != null) {
            throw new IllegalArgumentException(LocalizationMessages.WSRM_4002_BOTH_ACKS_AND_NACKS_MESSAGE());
        }

        getNack().add(BigInteger.valueOf(index));
    }
}
