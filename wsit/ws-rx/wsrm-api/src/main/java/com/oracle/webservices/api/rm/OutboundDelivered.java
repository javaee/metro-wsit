package com.oracle.webservices.api.rm;

import com.oracle.webservices.api.message.BasePropertySet;

/**
 * {@code OutboundDelivered} is created by a user of client-side (i.e., RMS) RM.
 *
 * <p>It is passed as a
 * {@link com.oracle.webservices.api.message.PropertySet} to
 * {@link com.oracle.webservices.api.disi.DispatcherRequest#request}.</p>
 *
 */
public abstract class OutboundDelivered
    extends BasePropertySet
{
    /**
     * Key for delivered property
     *
     * @see  #setDelivered
     */
    public static final String DELIVERED_PROPERTY = "com.oracle.webservices.api.rm.outbound.delivered.delivered";

    /**
     * <p>When the RMS receives an ACK from the RMD for the request message instance
     * that contains this {@code com.oracle.webserivces.api.message.Property},
     * then the RMS will call {@code #delivered(true)}.</p>
     *
     * <p>If max retries, timeouts or
     * {@code com.oracle.webservices.api.disi.ClientResponseTransport#fail} is called
     * with an non {@code RMRetryException} exception, then the RMS calls
     * {@code #delivered(false)}.
     *
     * @see #DELIVERED_PROPERTY
     */
    @Property(DELIVERED_PROPERTY)
    public abstract void setDelivered(boolean accept);


    /**
     * Key for message identity property
     *
     * @see  #getMessageIdentity
     */
    public static final String MESSAGE_IDENTITY_PROPERTY = "com.oracle.webservices.api.rm.outbound.delivered.message.identity";


    /**
     * @return The identity of the message.  Note: the return type is
     * {@code Object}.  The web services stack will only use that object to hash on.
     * The only thing that matters is that the implementor of the return {@code Object}'s
     * {@code hashCode} return a consistent "identity".
     *
     * @see #MESSAGE_IDENTITY_PROPERTY
     */
    @Property(MESSAGE_IDENTITY_PROPERTY)
    public abstract Object getMessageIdentity();


    ////////////////////////////////////////////////////
    //
    // PropertySet boilerplate
    //

    private static final PropertyMap model;

    static {
        model = parse(OutboundDelivered.class);
    }

    @Override
    protected PropertyMap getPropertyMap() {
        return model;
    }
}

// End of file.
