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

package com.sun.xml.ws.rx.rm.protocol.wsrm200702;

import com.sun.xml.ws.rx.rm.protocol.CreateSequenceResponseData;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.IncompleteSequenceBehavior;
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
import javax.xml.namespace.QName;

/**
 * <p>Java class for CreateSequenceResponseElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateSequenceResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://docs.oasis-open.org/ws-rx/wsrm/200702}Identifier"/>
 *         &lt;element ref="{http://docs.oasis-open.org/ws-rx/wsrm/200702}Expires" minOccurs="0"/>
 *         &lt;element name="IncompleteSequenceBehavior" type="{http://docs.oasis-open.org/ws-rx/wsrm/200702}IncompleteSequenceBehaviorType" minOccurs="0"/>
 *         &lt;element name="Accept" type="{http://docs.oasis-open.org/ws-rx/wsrm/200702}AcceptType" minOccurs="0"/>
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
@XmlType(name = "CreateSequenceResponseType", propOrder = {
"identifier",
"expires",
"incompleteSequenceBehavior",
"accept",
"any"
})
@XmlRootElement(name = "CreateSequenceResponse", namespace = "http://docs.oasis-open.org/ws-rx/wsrm/200702")
public class CreateSequenceResponseElement {

    @XmlElement(name = "Identifier", required = true)
    protected Identifier identifier;
    @XmlElement(name = "Expires")
    protected Expires expires;
    @XmlElement(name = "IncompleteSequenceBehavior")
    protected IncompleteSequenceBehaviorType incompleteSequenceBehavior;
    @XmlElement(name = "Accept")
    protected AcceptType accept;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    public CreateSequenceResponseElement() {
    }

    public CreateSequenceResponseElement(CreateSequenceResponseData data) {
        this();

        identifier = new Identifier(data.getSequenceId());

        if (data.getIncompleteSequenceBehavior() != IncompleteSequenceBehavior.getDefault()) {
            incompleteSequenceBehavior = IncompleteSequenceBehaviorType.fromISB(data.getIncompleteSequenceBehavior());
        }

        if (!data.doesNotExpire()) {
            expires = new Expires(data.getDuration());
        }
        if (data.getAcceptedSequenceAcksTo() != null) {
            accept = new AcceptType();
            accept.setAcksTo(data.getAcceptedSequenceAcksTo());
        }
    }

    public CreateSequenceResponseData.Builder toDataBuilder() {
        CreateSequenceResponseData.Builder dataBuilder = CreateSequenceResponseData.getBuilder(identifier.getValue());

        if (expires != null && expires.getDuration() != Sequence.NO_EXPIRY) {
            dataBuilder.duration(expires.getDuration());
        }

        if (accept != null) {
            dataBuilder.acceptedSequenceAcksTo(accept.getAcksTo());
        }

        if (incompleteSequenceBehavior != null) {
            dataBuilder.incompleteSequenceBehavior(incompleteSequenceBehavior.translate());
        }

        return dataBuilder;        
    }

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
     * Gets the value of the incompleteSequenceBehavior property.
     * 
     * @return
     *     possible object is
     *     {@link IncompleteSequenceBehaviorType }
     *     
     */
    public IncompleteSequenceBehaviorType getIncompleteSequenceBehavior() {
        return incompleteSequenceBehavior;
    }

    /**
     * Sets the value of the incompleteSequenceBehavior property.
     * 
     * @param value
     *     allowed object is
     *     {@link IncompleteSequenceBehaviorType }
     *     
     */
    public void setIncompleteSequenceBehavior(IncompleteSequenceBehaviorType value) {
        this.incompleteSequenceBehavior = value;
    }

    /**
     * Gets the value of the accept property.
     * 
     * @return
     *     possible object is
     *     {@link AcceptType }
     *     
     */
    public AcceptType getAccept() {
        return accept;
    }

    /**
     * Sets the value of the accept property.
     * 
     * @param value
     *     allowed object is
     *     {@link AcceptType }
     *     
     */
    public void setAccept(AcceptType value) {
        this.accept = value;
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
}
