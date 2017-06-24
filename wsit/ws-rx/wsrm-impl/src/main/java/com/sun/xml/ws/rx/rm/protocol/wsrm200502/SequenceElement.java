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

package com.sun.xml.ws.rx.rm.protocol.wsrm200502;

import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;

import javax.xml.namespace.QName;
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
 * SequenceElement is based on a JAXB Schema Compiler generated class that serializes
 * and deserialized the <code>SequenceType</code> defined in the WS-RM schema.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SequenceType")
@XmlRootElement(name = "Sequence", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
public class SequenceElement {

    @XmlElement(name = "Identifier", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected Identifier identifier;
    @XmlElement(name = "MessageNumber", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected Long messageNumber;
    @XmlElement(name = "LastMessage", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected LastMessage lastMessage;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    public SequenceElement() {
    }

    public String getLocalPart() {
        return "Sequence";
    }

    /**
     * Mutator for the Id property.  Maps to the Identifier property in the underlying
     * JAXB class.
     * 
     * @param id The new value.
     */
    public void setId(String idString) {
        Identifier newId = new Identifier();
        newId.setValue(idString);
        setIdentifier(newId);
    }

    /**
     * Accessor for the Id property.  Maps to the Identifier property in the underlying
     * JAXB class
     * @return The sequence id
     */
    public String getId() {
        return getIdentifier().getValue();
    }

    /**
     * Mutator for the Last property that maps to the LastMessage property in the
     * underlying JAXB class
     *
     * @param last The value of the property.
     */
    public void setLast(boolean last) {
        if (last) {
            setLastMessage(new LastMessage());
        } else {
            setLastMessage(null);
        }
    }

    /**
     * Accessor for the Last property that maps to the LastMessage property in the
     * underlying JAXB class
     *
     * @return The value of the property.
     */
    public boolean getLast() {
        return getLastMessage() != null;
    }

    /**
     * Gets the value of the identifier property.
     * 
     * @return The property value
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value The new value.
     */
    public void setIdentifier(Identifier value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the messageNumber property.
     * 
     * @return The value of the property.
     *     
     */
    public Long getMessageNumber() {
        return messageNumber;
    }

    /**
     * Sets the value of the messageNumber property.
     * 
     * @param value The new value.
     *     
     */
    public void setMessageNumber(Long value) {
        this.messageNumber = value;
    }

    /**
     * Accessor for the Number property which maps to the MessageNumber property in
     * the underlying JAXB class.
     * 
     * @return The Message number.
     */
    public long getNumber() {
        return getMessageNumber();
    }
    
    /**
     * Gets the value of the lastMessage property.
     * 
     * @return The value of the property
     *          non-null indicates that a Last child will be serialized on
     *          the Sequence element.
     *     
     */
    public LastMessage getLastMessage() {
        return lastMessage;
    }

    /**
     * Sets the value of the lastMessage property.
     * 
     * @param value The new value.  Either null or a member
     * of the placeholder inner LastMessage class.
     *  
     *     
     */
    public void setLastMessage(LastMessage value) {
        this.lastMessage = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * @return The value of the property.
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
     * @return The map of attributes.
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    /**
     * <p>Java class for anonymous complex type.  That acts as a
     * placeholder in the <code>lastMessage</code> field.
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class LastMessage {
    }

    @Override
    public String toString() {
        return LocalizationMessages.WSRM_4005_SEQUENCE_TOSTRING_STRING(getId(), getNumber(), getLast() ? "true" : "false");
    }
}

