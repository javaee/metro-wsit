package com.oracle.webservices.api.rm;

/**
 * {@code RMRetryException} is given to {@code com.oracle.webservices.api.disi.ClientResponseTransport#fail}
 * to signal that the RMS retry sending the message again.
 *
 * <p>This results in the RMS causing the message to be given to
 * {@code com.oracle.webservices.api.disi.ClientRequestTransport#request}
 * again.<p/>
 *
 * <p>Note: a retry will not occur is max retries, timeouts, etc., are exceeded.</p>
 */
public class RMRetryException
    extends Exception
{
}

// End of file.
