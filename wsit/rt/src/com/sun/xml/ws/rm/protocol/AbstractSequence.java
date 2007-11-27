package com.sun.xml.ws.rm.protocol;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * This is the base class for the implementations of <code> SequenceElement </code> based on the
 * two versions of the RM specification
 *
 * @author Bhakti Mehta
 * @author Mike Grogan
 */
public abstract class AbstractSequence {

    public String getLocalPart() {
        return "Sequence";
    }

    /**
     * Mutator for the Id property.  Maps to the Identifier property in the underlying
     * JAXB class.
     *
     * @param id The new value.
     */
    public abstract void setId(String id);

    /**
     * Accessor for the Id property.  Maps to the Identifier property in the underlying
     * JAXB class
     * @return The sequence id
     */
    protected abstract String getId();

    /**
     * Mutator for the Number property which maps to the MessageNumber property in
     * the underlying JAXB class.
     *
     * @param l The Message number.
     */
    public void setNumber(int l) {
        setMessageNumber(l);
    }

    /**
     * Accessor for the Number property which maps to the MessageNumber property in
     * the underlying JAXB class.
     *
     * @return The Message number.
     */
    protected int getNumber() {
        return getMessageNumber();
    }

    /**
     * Gets the value of the messageNumber property.
     *
     * @return The value of the property.
     *
     */
    protected abstract Integer getMessageNumber();

    /**
     * Sets the value of the messageNumber property.
     *
     * @param value The new value.
     *
     */
    public abstract void setMessageNumber(Integer value);

    /**
     * Gets the value of the any property.
     *
     * @return The value of the property.
     *
     *
     */
    public abstract List<Object> getAny();

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     *
     * @return The map of attributes.
     */
    public abstract Map<QName, String> getOtherAttributes();
}
