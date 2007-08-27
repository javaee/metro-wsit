package com.sun.xml.ws.rm.protocol;

import javax.xml.namespace.QName;
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
public abstract class AbstractCreateSequenceResponse {

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
     public abstract List<Object> getAny() ;

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
    public abstract Map<QName, String> getOtherAttributes();
    /**
     * Sets the value of the accept property.
     *
     * @param value
     *     allowed object is
     *     {@link com.sun.xml.ws.rm.v200702.AcceptType }
     *
     */
    public abstract void setAccept(AbstractAcceptType value) ;
}
