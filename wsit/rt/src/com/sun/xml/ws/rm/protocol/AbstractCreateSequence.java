package com.sun.xml.ws.rm.protocol;

import com.sun.xml.ws.rm.v200502.Expires;
import com.sun.xml.ws.rm.v200502.OfferType;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.util.List;
import java.util.Map;

/**
 * This is the base class for the implementations of <code> CreateSequence </code> based on the
 * two versions of the RM specification
 *
 * @author Bhakti Mehta
 * @author Mike Grogan
 *
 */
public abstract class AbstractCreateSequence {


    /*protected W3CEndpointReference acksTo;
    protected List<Object> any = new ArrayList<Object>();

    protected AbstractExpires expires;

    protected AbstractOfferType offer;


    protected SecurityTokenReferenceType securityTokenReference;


    private Map<QName, String> otherAttributes = new HashMap<QName, String>();*/

    /**
     * Gets the value of the acksTo property.
     *
     * @return
     *     possible object is
     *     {@link com.sun.xml.ws.api.addressing.WSEndpointReference }
     *
     */

    protected abstract W3CEndpointReference getAcksTo() ;


    /**
     * Sets the value of the acksTo property.
     *
     * @param value
     *     allowed object is
     *     {@link javax.xml.ws.EndpointReference }
     *
     */

    public abstract void setAcksTo(W3CEndpointReference value) ;

//    /**
//     * Gets the value of the expires property.
//     *
//     * @return
//     *     possible object is
//     *     {@link com.sun.xml.ws.rm.v200502.Expires }   or
//     *     {@link com.sun.xml.ws.rm.v200702.Expires }
//     *
//     */
//    protected abstract AbstractExpires getExpires() ;

 /*   *//**
     * Sets the value of the expires property.
     *
     * @param value
     *     allowed object is
     *     {@link Expires }
     *
     *//*
    protected abstract void setExpires(AbstractExpires value) ;*/

   /* *//**
     * Gets the value of the offer property.
     *
     * @return
     *     possible object is
     *     {@link com.sun.xml.ws.rm.v200502.OfferType }
     *     {@link com.sun.xml.ws.rm.v200702.OfferType }
     *
     *//*
    protected abstract AbstractOfferType getOffer() ;

    *//**
     * Sets the value of the offer property.
     *
     * @param value
     *     allowed object is
     *     {@link OfferType }
     *
     *//*
    public abstract void setOffer(AbstractOfferType value);*/

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
     * {@link org.w3c.dom.Element }
     *
     *
     */
    protected abstract List<Object> getAny() ;

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
    protected abstract Map<QName, String> getOtherAttributes() ;

    protected abstract SecurityTokenReferenceType getSecurityTokenReference() ;

    public abstract void setSecurityTokenReference(SecurityTokenReferenceType s) ;

     

}





