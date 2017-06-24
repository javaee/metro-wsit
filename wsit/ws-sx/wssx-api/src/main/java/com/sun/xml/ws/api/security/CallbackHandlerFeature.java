/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
