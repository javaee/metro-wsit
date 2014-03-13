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

package com.sun.xml.ws.rx.rm.protocol.wsrm200502;

import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstraction of the <code>SequenceAcknowledgement</code> WS-RM protocol element.
 * Based on a JAXB schema compiler generated class that has properties for each 
 * child element of the SequenceAcknowledgement element.  A property used to
 * serialize/ deserialize the <code>BufferRemaining</code> extensibility element is added.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
"identifier",
"acknowledgementRange",
"bufferRemaining",
"nack",
"any"
})
@XmlRootElement(name = "SequenceAcknowledgement", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
public class SequenceAcknowledgementElement {

    @XmlElement(name = "Identifier", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected Identifier identifier;
    @XmlElement(name = "AcknowledgementRange", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected List<SequenceAcknowledgementElement.AcknowledgementRange> acknowledgementRange;
    @XmlElement(name = "Nack", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected List<BigInteger> nack;
    @XmlElement(name = "BufferRemaining", namespace = "http://schemas.microsoft.com/ws/2006/05/rm")
    protected Integer bufferRemaining;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    public SequenceAcknowledgementElement() {

    }

    /**
     * Gets the value of the identifier property. 
     * @return The value of the property.
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value The new value of the property.
     */
    public void setIdentifier(Identifier value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the acknowledgementRange property.
     * 
     * @return The value of the property, which is a list of AcknowledgementRange
     *  objects
     */
    public List<SequenceAcknowledgementElement.AcknowledgementRange> getAcknowledgementRange() {
        if (acknowledgementRange == null) {
            acknowledgementRange = new ArrayList<SequenceAcknowledgementElement.AcknowledgementRange>();
        }
        return this.acknowledgementRange;
    }

    /**
     *  Gets the value of the nack property.
     * 
     * @return The value of the property, which is a list of BigIntegers
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
     * Gets the value of the any property representing extensibility elements
     *  
     * @return The list of elements.
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
     * @return The value of the property
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public void setId(String idString) {
        Identifier newId = new Identifier();
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

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(LocalizationMessages.WSRM_4004_SEQUENCE_ACKNOWLEDGEMENT_TOSTRING_STRING(getId(), getBufferRemaining()));
        List<AcknowledgementRange> ranges = getAcknowledgementRange();
        if (ranges != null) {
            for (AcknowledgementRange range : ranges) {
                buffer.append("\t\t").append(range.toString()).append('\n');
            }
        }
        return buffer.toString();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class AcknowledgementRange {

        @XmlAttribute(name = "Lower", required = true)
        protected BigInteger lower;
        @XmlAttribute(name = "Upper", required = true)
        protected BigInteger upper;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

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

        @Override
        public String toString() {
            return "AcknowledgementRange (" + lower.intValue() + "," + upper.intValue() + ")";
        }
    }
}



