//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2005.09.05 at 03:09:41 PM IST 
//


package com.sun.xml.wss.saml.internal.saml11.jaxb10;


/**
 * Java content class for AttributeType complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/space/combination/jwsdp1.6_tc/jaxb/bin/oasis-sstc-saml-schema-assertion-1.1.xsd line 190)
 * <p>
 * <pre>
 * &lt;complexType name="AttributeType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:1.0:assertion}AttributeDesignatorType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:1.0:assertion}AttributeValue" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface AttributeType
    extends com.sun.xml.wss.saml.internal.saml11.jaxb10.AttributeDesignatorType
{


    /**
     * Gets the value of the AttributeValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the AttributeValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link com.sun.xml.wss.saml.internal.saml11.jaxb10.AttributeValue}
     * {@link com.sun.xml.wss.saml.internal.saml11.jaxb10.AnyType}
     * 
     */
    java.util.List getAttributeValue();

}
