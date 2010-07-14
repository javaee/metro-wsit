/*
 * $Id: DynamicPolicyContext.java,v 1.3.2.2 2010-07-14 14:06:58 m_potociar Exp $
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
    @SuppressWarnings("unchecked")
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
