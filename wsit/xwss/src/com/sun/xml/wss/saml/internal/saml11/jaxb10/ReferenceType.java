//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2005.09.05 at 03:09:41 PM IST 
//


package com.sun.xml.wss.saml.internal.saml11.jaxb10;


/**
 * Java content class for ReferenceType complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at http://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd line 97)
 * <p>
 * <pre>
 * &lt;complexType name="ReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Transforms" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}DigestMethod"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}DigestValue"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="URI" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface ReferenceType {


    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getType();

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setType(java.lang.String value);

    /**
     * Gets the value of the digestMethod property.
     * 
     * @return
     *     possible object is
     *     {@link com.sun.xml.wss.saml.internal.saml11.jaxb10.DigestMethodType}
     *     {@link com.sun.xml.wss.saml.internal.saml11.jaxb10.DigestMethod}
     */
    com.sun.xml.wss.saml.internal.saml11.jaxb10.DigestMethodType getDigestMethod();

    /**
     * Sets the value of the digestMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.sun.xml.wss.saml.internal.saml11.jaxb10.DigestMethodType}
     *     {@link com.sun.xml.wss.saml.internal.saml11.jaxb10.DigestMethod}
     */
    void setDigestMethod(com.sun.xml.wss.saml.internal.saml11.jaxb10.DigestMethodType value);

    /**
     * Gets the value of the digestValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    byte[] getDigestValue();

    /**
     * Sets the value of the digestValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    void setDigestValue(byte[] value);

    /**
     * Gets the value of the uri property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getURI();

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setURI(java.lang.String value);

    /**
     * Gets the value of the transforms property.
     * 
     * @return
     *     possible object is
     *     {@link com.sun.xml.wss.saml.internal.saml11.jaxb10.TransformsType}
     *     {@link com.sun.xml.wss.saml.internal.saml11.jaxb10.Transforms}
     */
    com.sun.xml.wss.saml.internal.saml11.jaxb10.TransformsType getTransforms();

    /**
     * Sets the value of the transforms property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.sun.xml.wss.saml.internal.saml11.jaxb10.TransformsType}
     *     {@link com.sun.xml.wss.saml.internal.saml11.jaxb10.Transforms}
     */
    void setTransforms(com.sun.xml.wss.saml.internal.saml11.jaxb10.TransformsType value);

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getId();

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setId(java.lang.String value);

}
