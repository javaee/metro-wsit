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

package com.sun.xml.ws.api.config.management;

import java.util.HashMap;
import junit.framework.TestCase;

/**
 *
 * @author Fabian Ritzmann
 */
public class NamedParametersTest extends TestCase {
    
    public NamedParametersTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of get method, of class NamedParameters.
     */
    public void testGetSame() {
        String name = "name";
        Object expResult = new Object();
        NamedParameters instance = new NamedParameters();
        instance.put(name, expResult);
        Object result = instance.get(name);
        assertSame(expResult, result);
    }

    public void testPutAll() {
        String name = "name";
        Object expResult = new Object();
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(name, expResult);
        NamedParameters instance = new NamedParameters();
        instance.putAll(map);
        Object result = instance.get(name);
        assertSame(expResult, result);
    }

    /**
     * Test of toString method, of class NamedParameters.
     */
    public void testToStringEmpty() {
        NamedParameters instance = new NamedParameters();
        String result = instance.toString();
        assertNotNull(result);
    }

    /**
     * Test of toString method, of class NamedParameters.
     */
    public void testToString() {
        NamedParameters instance = new NamedParameters();
        instance.put("name1", new Object());
        instance.put("name2", new Object());
        String result = instance.toString();
        assertNotNull(result);
    }

}