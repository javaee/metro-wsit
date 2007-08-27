package com.sun.xml.ws.rm.protocol;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Aug 23, 2007
 * Time: 3:37:23 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractAcceptType {

    /**
         * Gets the value of the acksTo property.
         *
         * @return
         *     possible object is
         *     {@link com.sun.xml.ws.api.addressing.WSEndpointReference }
         *
         */
        protected abstract W3CEndpointReference getAcksTo();

        /**
         * Sets the value of the acksTo property.
         *
         * @param value
         *     allowed object is
         *     {@link com.sun.xml.ws.api.addressing.WSEndpointReference }
         *
         */
        public abstract void setAcksTo(W3CEndpointReference value);

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
        public abstract Map<QName, String> getOtherAttributes() ;

}
