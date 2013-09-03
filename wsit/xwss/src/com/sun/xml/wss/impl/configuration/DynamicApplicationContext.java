/*
 * $Id: DynamicApplicationContext.java,v 1.4 2008/07/03 05:28:55 ofung Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.wss.impl.configuration;

import java.util.HashMap;

import com.sun.xml.wss.impl.policy.StaticPolicyContext;
import com.sun.xml.wss.impl.policy.DynamicPolicyContext;

/**
 * Represents a concrete SecurityPolicy identifier context resolved at runtime,
 * An XWS-Security <code>DynamicPolicyCallback</code> is passed an instance of
 * a <code>DynamicApplicationContext</code>. A callback Handler handling 
 * DynamicPolicyCallback can make use of information in this context
 * to dynamically determine the Security policy applicable for a request/response
 */
public class DynamicApplicationContext extends DynamicPolicyContext {

    private String messageIdentifier = "";
    private boolean inBoundMessage = false;

    private StaticPolicyContext context = null;

    /**
     * Create an empty DynamicApplicationContext
     */
    public DynamicApplicationContext () {}

    /**
     * Create a DynamicApplicationContext with an associated 
     * StaticPolicyContext <code>context</code>
     * @param context the associated StaticPolicyContext
     */
    public DynamicApplicationContext (StaticPolicyContext context) {
        this.context = context;
    }

    /**
     * Set the messageIdentifier for this Message associated with this context
     * @param messageIdentifier
     */
    public void setMessageIdentifier (String messageIdentifier) {
        this.messageIdentifier = messageIdentifier;
    }

    /**
     * @return messageIdentifier for the Message associated with this context
     */
    public String getMessageIdentifier () {
        return this.messageIdentifier;
    }

    /**
     * Set the Message direction (inbound/outbound) to which this context corresponds to
     * @param inBound flag indicating the direction of the message
     */
    public void inBoundMessage (boolean inBound) {
        this.inBoundMessage = inBound;
    }

    /**
     * @return true if the context is for an inbound message
     */
    public boolean inBoundMessage () {
        return this.inBoundMessage;
    }

    /**
     * set the associated StaticPolicyContext for this context
     * @param context StaticPolicyContext
     */
    public void setStaticPolicyContext (StaticPolicyContext context) {
        this.context = context;
    }

    /**
     * @return the associated StaticPolicContext if any, null otherwise
     */
    public StaticPolicyContext getStaticPolicyContext () {
        return context;
    }

    /**
     * @return HashMap of runtime properties in this context
     */
    public HashMap getRuntimeProperties () {
        return properties;
    }

    /**
     * equals operator
     * @param ctx DynamicApplicationContext with which to compare for equality
     * @return true if ctx is equal to this DynamicApplicationContext
     */
    public boolean equals (DynamicApplicationContext ctx) {
        boolean b1 =
               getStaticPolicyContext().equals (ctx.getStaticPolicyContext());
        if (!b1) return false;

        boolean b2 =
            (messageIdentifier.equalsIgnoreCase (ctx.getMessageIdentifier()) &&
                inBoundMessage == ctx.inBoundMessage);
        if (!b2) return false;

        return true;
    }
}
