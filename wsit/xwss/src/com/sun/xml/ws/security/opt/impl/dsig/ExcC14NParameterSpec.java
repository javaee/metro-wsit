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

package com.sun.xml.ws.security.opt.impl.dsig;

import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

/**
 *
 * @author ashutoshshahi
 */
public class ExcC14NParameterSpec implements AlgorithmParameterSpec, 
        C14NMethodParameterSpec, TransformParameterSpec{

    private List preList;

    /**
     * Indicates the default namespace ("#default").
     */
    public static final String DEFAULT = "#default";

    /**
     * Creates a <code>ExcC14NParameterSpec</code> with an empty prefix 
     * list.
     */
    public ExcC14NParameterSpec() {
	preList = new ArrayList();
    }

    /**
     * Creates a <code>ExcC14NParameterSpec</code> with the specified list
     * of prefixes. The list is copied to protect against subsequent 
     * modification.
     *
     * @param prefixList the inclusive namespace prefix list. Each entry in
     *    the list is a <code>String</code> that represents a namespace prefix.
     * @throws NullPointerException if <code>prefixList</code> is 
     *    <code>null</code>
     * @throws ClassCastException if any of the entries in the list are not
     *    of type <code>String</code>
     */
    public ExcC14NParameterSpec(List prefixList) {
	if (prefixList == null) {
	    throw new NullPointerException("prefixList cannot be null");
	}
	this.preList = new ArrayList(prefixList);
        for (int i = 0, size = preList.size(); i < size; i++) {
            if (!(preList.get(i) instanceof String)) {
		throw new ClassCastException("not a String");
	    }
	}
    }

    /**
     * Returns the inclusive namespace prefix list. Each entry in the list
     * is a <code>String</code> that represents a namespace prefix.
     *
     * @return the inclusive namespace prefix list (may be empty but never
     *    <code>null</code>)
     */
    public List getPrefixList() {
	return preList;
    }
}
