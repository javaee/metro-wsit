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
package com.sun.xml.ws.rm.v200502;

import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.protocol.AbstractAckRequested;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AckRequested", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
public class AckRequestedElement extends AbstractAckRequested {

    @XmlElement(name = "Identifier", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected Identifier identifier;
    @XmlElement(name = "MaxMessageNumberUsed", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected BigInteger maxMessageNumberUsed;

    public AckRequestedElement() {

    }

    /*public QName getQName() {
    return  RMBuilder.getConstants().getAckRequestedQName();
    }*/
    //Introduce accessors using simple types rather than BigInteger and
    //Identifier
    public void setId(String id) {
        Identifier idType = new Identifier();
        idType.setValue(id);
        setIdentifier(idType);
    }

    public String getId() {
        return getIdentifier().getValue();
    }

    public void setMaxMessageNumber(long l) {
        setMaxMessageNumberUsed(BigInteger.valueOf(l));
    }

    public long getMaxMessageNumber() {

        BigInteger big;
        if (null == (big = getMaxMessageNumberUsed())) {
            return 0;
        }
        return big.longValue();
    }

    /**
     * Gets the value of the Identifier property.
     * 
     * @return The value of the property
     *     
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value The new value.
     *     
     */
    public void setIdentifier(Identifier value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the maxMessageNumberUsed property.
     * 
     * @return The value of the property.
     *     
     */
    public BigInteger getMaxMessageNumberUsed() {
        return maxMessageNumberUsed;
    }

    /**
     * Sets the value of the maxMessageNumberUsed property.
     * 
     * @param value The new value.
     */
    public void setMaxMessageNumberUsed(BigInteger value) {
        this.maxMessageNumberUsed = value;
    }

    @Override
    public String toString() {
        return LocalizationMessages.WSRM_4000_ACKREQUESTED_TOSTRING_STRING(getId(), getMaxMessageNumber());
    }
}

