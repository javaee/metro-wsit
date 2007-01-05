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

/*
 * SequenceAcknowledgementElement.java
 *
 * @author Mike Grogan
 * Created on October 23, 2005, 9:51 AM
 *
 */

package com.sun.xml.ws.rm.protocol;

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
@XmlRootElement(name = "SequenceAcknowledgement",namespace="http://schemas.xmlsoap.org/ws/2005/02/rm")
public class SequenceAcknowledgementElement   {


    @XmlElement(name = "Identifier", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected Identifier identifier;

    @XmlElement(name = "AcknowledgementRange", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected List<SequenceAcknowledgementElement.AcknowledgementRange> acknowledgementRange;

    @XmlElement(name = "Nack", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected List<BigInteger> nack;

    @XmlElement(name="BufferRemaining", namespace="http://schemas.microsoft.com/ws/2006/05/rm")
    public Integer bufferRemaining;

    @XmlAnyElement(lax = true)
    protected List<Object> any;

    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();


    public SequenceAcknowledgementElement(){

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


    public String getLocalPart(){
        return new String ( "SequenceAcknowledgement");
    }

    public void setId(String id) {
        Identifier identifier = new Identifier();
        identifier.setValue(id);
        setIdentifier(identifier);
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
            throw new IllegalArgumentException(Messages.BOTH_ACKS_AND_NACKS_MESSAGE.format());
        }
        //check validity of indices
        if (lower > upper) {
            throw new IllegalArgumentException(Messages.UPPERBOUND_LESSTHAN_LOWERBOUND_MESSAGE.format());
        }

        //TODO Further validity checking
        SequenceAcknowledgementElement.AcknowledgementRange range
                = new SequenceAcknowledgementElement.AcknowledgementRange();
        range.setLower(BigInteger.valueOf(lower));
        range.setUpper(BigInteger.valueOf(upper));
        getAcknowledgementRange().add(range);

    }

    public void addNack(long index) {
        if (acknowledgementRange != null) {
            throw new IllegalArgumentException(Messages.BOTH_ACKS_AND_NACKS_MESSAGE.format());
        }

        getNack().add(BigInteger.valueOf(index));
    }
    
    public String toString() {
       
        String ret = Messages.SEQUENCE_ACKNOWLEDGEMENT_TOSTRING_STRING.format(getId(), getBufferRemaining());
        List<AcknowledgementRange> ranges = getAcknowledgementRange();
        if (ranges != null) {
            for (AcknowledgementRange range: ranges) {
                ret += "\t\t" + range.toString() + "\n";
            }
        }
        return ret;
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
        
        public String toString() {
            return "AcknowledgementRange (" + lower.intValue() + "," + 
                    upper.intValue() + ")";
        }
    }
}



