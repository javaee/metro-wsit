/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

/**
 * In order to internally use WebServiceFeatures instead of a policy map to configure
 * Metro functionality, we must be able to associate single operations and messages
 * with WebServiceFeatures. So far WebServiceFeatures can only be attached to
 * ports. The JAX-WS API provides a number of ways to set these features:
 * <ul>
 * <li>client-side: <code>create</code> method on <code>javax.xml.ws.Service</code>.
 * <li>client-side: Generated constructors on <code>javax.xml.ws.Service</code> implementation.
 * <li>client-side: <code>getPort</code> methods on <code>javax.xml.ws.Service</code>.
 * <li>client-side: <code>createDispatch</code> methods on <code>javax.xml.ws.Service</code>.
 * <li>client-side: <code>createServiceDelegate</code> methods on <code>javax.xml.ws.spi.Provider</code>.
 * <li>client-side: <code>getPort</code> methods on <code>javax.xml.ws.spi.Provider</code>.
 * <li>server-side: <code>create</code> methods on <code>javax.xml.ws.Endpoint</code>.
 * <li>server-side: <code>publish</code> methods on <code>javax.xml.ws.Endpoint</code>.
 * <li>server-side: <code>createEndpoint</code> methods on <code>javax.xml.ws.spi.Provider</code>.
 * <li>server-side: <code>createAndPublishEndpoint</code> methods on <code>javax.xml.ws.spi.Provider</code>.
 * </ul>
 *
 * <h2>WebServiceFeature</h2>
 * There are two ways to approach this. The simpler implementation is to build all the information we need
 * into <code>javax.xml.ws.WebServiceFeature</code>. The feature could e.g. contain maps that map
 * operations and messages to the settings in the feature:
 *
 * <pre> Map&lt;String operationName, boolean featureEnabled&gt; operationMap;
 * Map&lt;String operationName, boolean featureEnabled&gt; inboundMessageMap;
 * Map&lt;String operationName, boolean featureEnabled&gt; outboundMessageMap;
 * Map&lt;Tuple&lt;String operationName, String faultName&gt; faultMessage, boolean featureEnabled&gt; faultMessageMap;</pre>
 *
 * While we wait for the JAX-WS specification to add this interface, we would have
 * to provide a proprietary interface.
 *
 * <h2>Feature Handler</h2>
 * The above however seems awkward to use for developers. The more user-friendly implementation
 * would be to provide additional methods related to <code>javax.xml.ws.Service</code> and
 * {@link javax.xml.ws.Binding} to explicitly set WebServiceFeatures for specific
 * operations or messages.
 * <p>
 * Simply adding new methods to these interfaces would still lead to an overloaded
 * interface. Instead, a more straight-forward approach that is familiar to JAX-WS users
 * is to do something similar to JAX-WS handlers. JAX-WS allows to set handler chains
 * through simple methods on <code>javax.xml.ws.Binding</code> and {@link javax.xml.ws.Service}. We could provide similar
 * methods to add WebServiceFeatures that target specific operations and messages.
 * 
 * <h3>Client-side</h3>
 * Handlers may be set programmatically by the methods on {@link javax.xml.ws.Service}.
 * We can take the same approach for WebServiceFeatures:
 * <ul>
 * <li><code>javax.xml.ws.Service</code>: <code>public FeatureResolver getFeatureResolver()</code>
 * <li><code>javax.xml.ws.Service</code>: <code>public void setFeatureResolver(FeatureResolver featureResolver)</code>
 * </ul>
 * The {@link com.sun.xml.ws.api.metro.config.FeatureResolver} <code>getFeatureSet</code> methods may be called
 * multiple times. Once for the binding, once for the port, once for each operation and
 * once for each message of each operation. This allows to attach lists of features on a per-message
 * and per-operation basis. The {@link com.sun.xml.ws.api.metro.config.ElementInfo} parameter
 * is used to identify the attachment point at the time of each call.
 * <p>
 * Again, we would have to provide a proprietary interface while we wait for the JAX-WS
 * specification to catch up.
 *
 * <h3>Server-side</h3>
 * Handlers may be set programmatically on the endpoint through {@link javax.xml.ws.Binding}.
 * This class would be extended like this:
 * <ul>
 * <li><code>javax.xml.ws.Binding</code>: <code>public FeatureResolver getFeatureResolver()</code>
 * <li><code>javax.xml.ws.Binding</code>: <code>public void setFeatureResolver(FeatureResolver featureResolver)</code>
 * </ul>
 * The {@link com.sun.xml.ws.api.metro.config.FeatureResolver} is the same interface as on the client side.
 *
 * <h2>Internal Handling</h2>
 * Internally, WebServiceFeatures are passed through the system through {@link com.sun.xml.ws.api.WSBinding}.
 * This class will need to be extended to maintain lists for every attachment point.
 * The currently existing methods can be maintained and would operate on the port level
 * as they do now. We can use {@link com.sun.xml.ws.api.metro.config.ElementInfo} objects
 * to uniquely identify each attachment point.
 */
package com.sun.xml.ws.api.metro.config;