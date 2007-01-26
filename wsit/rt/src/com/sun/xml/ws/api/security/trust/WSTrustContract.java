
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.api.security.trust;

import com.sun.xml.ws.api.security.trust.config.STSConfiguration;
import com.sun.xml.ws.security.IssuedTokenContext;

import java.util.Map;


/**
 * <html>
 *  <head>
 *    
 *  </head>
 *  <body>
 *    The Contract (SPI) to be used by an STS to handle an Incoming WS-Trust request and 
 *    send the corresponding response.
 *  </body>
 * </html>
 */
public interface WSTrustContract<K, V> {

    public void init(STSConfiguration config);
    
    /** Issue a Token */
    public V issue(K rst, IssuedTokenContext context) throws WSTrustException;

    /** Renew a Token */
    public V renew(K rst, IssuedTokenContext context)
            throws WSTrustException;

    /** Cancel a Token */
    public V cancel(K rst, IssuedTokenContext context, Map map)
            throws WSTrustException;

    /** Validate a Token */
    public V validate(K request, IssuedTokenContext context)
            throws WSTrustException;

    /** 
     * handle an unsolicited RSTR like in the case of 
     * Client Initiated Secure Conversation.
     */
    public void handleUnsolicited(V rstr, IssuedTokenContext context)
           throws WSTrustException;
}
