/*
 * $Id: DynamicPolicyContext.java,v 1.1 2006-05-03 22:57:52 arungupta Exp $
 */

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

package com.sun.xml.wss.impl.policy;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Represents a SecurityPolicy identifier context resolved at runtime
 */
public abstract class DynamicPolicyContext {

    /* Represents extraneous properties */
    protected HashMap properties = new HashMap ();

    /**
     * get the named property
     * @param name property name
     * @return Object property value
     */
    protected Object getProperty (String name) {
        return properties.get(name);
    }

    /**
     * set the named property to value <code>value</code>.
     * @param name property name
     * @param value property value
     */
    protected void setProperty (String name, Object value) {
        properties.put (name, value);
    }

    /**
     * remove the named property
     * @param name property to be removed
     */
    protected void removeProperty (String name) {
        properties.remove (name);
    }

    /**
     * @param name property to be checked for presence
     * @return true if the property <code>name</code> is present.
     */
    protected boolean containsProperty (String name) {
        return properties.containsKey(name);
    }

    /**
     * @return Iterator over the property names
     */
    protected Iterator getPropertyNames () {
        return properties.keySet ().iterator();
    }

   /**
    * @return Any <code>StaticPolicyContext</code> associated with this context.
    */
    public abstract StaticPolicyContext getStaticPolicyContext ();
}
