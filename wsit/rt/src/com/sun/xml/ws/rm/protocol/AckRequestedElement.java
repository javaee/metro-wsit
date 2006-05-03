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
 * AckRequestedElement.java
 *
 * @author Mike Grogan
 * Created on October 23, 2005, 9:03 AM
 *
 */

package com.sun.xml.ws.rm.protocol;
import com.sun.xml.ws.rm.RMBuilder;



import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.namespace.QName;
import java.math.BigInteger;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AckRequested", namespace="http://schemas.xmlsoap.org/ws/2005/02/rm")
public class AckRequestedElement  {


    @XmlElement(name = "Identifier", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected Identifier identifier;
    @XmlElement(name = "MaxMessageNumberUsed", namespace = "http://schemas.xmlsoap.org/ws/2005/02/rm")
    protected BigInteger maxMessageNumberUsed;

    public AckRequestedElement(){
        
    }
    
    public QName getQName() {
        return  RMBuilder.getConstants().getAckRequestedQName();
    }

    public String getLocalPart(){
        return new String ("AckRequested");
    }

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
    
    public String toString() {
        String ret = "AckRequestedElement:\n";
        ret += "\tid = " + getId() + "\n";
        ret += "\tmaxMessageNumber = " + getMaxMessageNumber() + "\n";
        return ret;
        
    }



}

