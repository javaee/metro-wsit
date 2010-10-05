package com.sun.xml.ws.api.security;

import com.sun.istack.NotNull;

import javax.xml.ws.WebServiceFeature;
import javax.security.auth.callback.CallbackHandler;
import java.security.cert.CertStore;
import java.security.KeyStore;

import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

/**
 * {@link WebServiceFeature} that controls {@link CallbackHandler} used during security related processing
 * of Metro.
 *
 * <p>
 * This rather untyped, low-level and user-unfriendly {@link CallbackHandler} object controls many details of the security
 * processing at runtime, such as locating {@link CertStore} or {@link KeyStore}. While we'd like to provide
 * a higher level features for common configurations, this feature works as an catch-all escape hatch.
 *
 * <p>
 * See {@link com.sun.xml.wss.impl.misc.DefaultCallbackHandler#handle(javax.security.auth.callback.Callback[])}
 * implementation as an example of what callback {@link CallbackHandler} receives (note that this default
 * implementation class itself is not a committed part of Metro.)
 *
 * <p>
 * This feature allows you to pass in an instance of {@link CallbackHandler} unlike
 * {@code <sc:CallbackHandlerConfiguration>} assertion, which makes it convenient to pass in some state
 * from the calling application into {@link CallbackHandler}.
 *
 * @author Kohsuke Kawaguchi
 * @since Metro 1.5
 */
@ManagedData
public final class CallbackHandlerFeature extends WebServiceFeature {
    private final CallbackHandler handler;

    public CallbackHandlerFeature(@NotNull CallbackHandler handler) {
        if(handler==null)   throw new IllegalArgumentException();
        this.handler = handler;
    }

    @ManagedAttribute
    public String getID() {
        return CallbackHandlerFeature.class.getName();
    }

    /**
     * @return
     *      {@link CallbackHandler} set in the constructor. Never null. 
     */
    @ManagedAttribute
    public @NotNull CallbackHandler getHandler() {
        return handler;
    }
}
