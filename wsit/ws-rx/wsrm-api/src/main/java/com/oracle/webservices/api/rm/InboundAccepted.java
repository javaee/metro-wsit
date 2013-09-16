package com.oracle.webservices.api.rm;

import com.oracle.webservices.api.message.BasePropertySet;

/**
 * {@code InboundAccepted} is created by the RMD.
 *
 * <p>It is passed as a
 * {@link com.oracle.webservices.api.message.PropertySet} to
 * {@link com.oracle.webservices.api.disi.ProviderRequest#request}.</p>
 *
 */
public abstract class InboundAccepted
    extends BasePropertySet
{
    /**
     * Key for accepted property
     *
     * @see  #accepted
     */
    public static final String ACCEPTED_PROPERTY = "com.oracle.webservices.api.rm.inbound.accepted.accepted";

    /**
     * <p>When the user determines that the message has been delivered to them then they call {@code #accepted(true)}.</p>
     *
     * <p>The RMD will <em>not</em> acknowledge the message to the RMS until {@code #accepted(true)} is called.</p>
     *
     * <p>If the user calls {@code #accepted(false)} then the RMD will not
     * acknowledge the delivery of this particular request.  Note: if the
     * RMS sends a retry, that is considered a new request and the
     * delivery/acceptance process starts anew.</p>
     *
     * <p>If the user calls {@code #accepted(false)} and an atomic
     * transaction is being used to handle the message, then that
     * transaction will be rolled back.</p>
     *
     * @throws {@link InboundAcceptedAcceptFailed}
     *     If the user calls {@code #accepted(true)} but the RMD is
     *     not able to internally record the message as delivered
     *     (e.g., an atomic transaction fails to commit) then this
     *     exception is thrown.
     *
     * @see #ACCEPTED_PROPERTY
     */
    @Property(ACCEPTED_PROPERTY)
    public abstract void accepted(boolean accept) throws InboundAcceptedAcceptFailed;

    ////////////////////////////////////////////////////
    //
    // PropertySet boilerplate
    //

    private static final PropertyMap model;

    static {
        model = parse(InboundAccepted.class);
    }

    @Override
    protected PropertyMap getPropertyMap() {
        return model;
    }
}

// End of file.
