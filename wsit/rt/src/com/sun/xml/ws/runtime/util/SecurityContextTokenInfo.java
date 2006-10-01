package com.sun.xml.ws.runtime.util;

import java.util.Date;
import java.util.List;

/**
 * The </code>SecurityContextTokenInfo</code> class is represents security parameters
 * which will be saved in the <code>Session</code> object so that whenever the endpoint
 * crashes the security negotiations can be resumed from its original  state and no new negotiations need to be done.
 * @author Jiandong Guo
 */
public abstract class SecurityContextTokenInfo {


    public abstract String getIdentifier();

    public abstract String getExternalId();

    public abstract byte[] getSecret();

    public abstract byte[] getInstanceSecret(String instance);

    public abstract void addInstance(String instance, byte[] key);

    public abstract Date getCreationTime();

    public abstract Date getExpirationTime();

    public abstract List getInstanceKeys();

}
