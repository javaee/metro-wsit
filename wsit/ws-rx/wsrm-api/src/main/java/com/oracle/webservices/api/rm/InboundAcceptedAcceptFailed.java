package com.oracle.webservices.api.rm;

/**
 * {@code InboundAcceptedAcceptFailed} is thrown if the user calls {@code InboundAccepted#accepted(true)} but the RMD is
 *     not able to internally record the message as delivered
 *     (e.g., an atomic transaction fails to commit).
 */
public class InboundAcceptedAcceptFailed
    extends Exception
{
}

// End of file.
