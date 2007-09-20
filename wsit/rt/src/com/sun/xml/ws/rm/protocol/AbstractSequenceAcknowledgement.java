package com.sun.xml.ws.rm.protocol;



import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * This is the base class for the implementations of <code> SequenceAcknowledgementElement </code> based on the
 * two versions of the RM specification
 *
 * @author Bhakti Mehta
 * @author Mike Grogan
 */
public abstract class AbstractSequenceAcknowledgement {

     /**
     *  Gets the value of the nack property.
     *
     * @return The value of the property, which is a list of BigIntegers
     *
     *
     */
    protected abstract List<BigInteger> getNack() ;

    /**
     * Gets the value of the any property representing extensibility elements
     *
     * @return The list of elements.
     *
     */
    protected abstract List<Object> getAny() ;

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     *
     * @return The value of the property
     */
    protected abstract Map<QName, String> getOtherAttributes();


    /**
     * Sets the Identifier
     * @param id
     */
    public abstract void setId(String id) ;

    /**
     * Gets the identifier associated with the Sequence
     * @return     String
     */
    protected abstract String getId() ;

    /**
     * Gets the BufferRemaining value
     * @return   int
     */
    protected abstract int getBufferRemaining();

    /**
     * Sets the BufferRemaining value
     * @return void
     */

    public abstract void setBufferRemaining(int value) ;

    public  abstract void addAckRange(long lower, long upper);

    public abstract void addNack(long index);

  
}
