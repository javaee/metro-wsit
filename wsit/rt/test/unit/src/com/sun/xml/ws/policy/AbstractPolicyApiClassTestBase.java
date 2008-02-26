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

package com.sun.xml.ws.policy;

import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public abstract class AbstractPolicyApiClassTestBase extends TestCase {
    
    /**
     * Creates a new instance of AbstractPolicyApiClassTestBase
     */
    public AbstractPolicyApiClassTestBase(String name) {
        super(name);
    }
    
    protected abstract Object[][] getEqualInstanceRows() throws Exception;
    
    public final void testEqualsWithNull() throws Exception{
        for (Object[] instanceRow : getEqualInstanceRows()) {
            for (Object instance : instanceRow) {
                assertFalse("Instance of class '" + instance.getClass().getName() + "' must not return true when comparing for equality with 'null' value.", instance.equals(null));
            }
        }
    }
    
    public final void testEqualsOnManyEqualPolicies() throws Exception {
        Object[][] testbed = getEqualInstanceRows();
        String className = testbed[0][0].getClass().getName();
        int index = 0;
        for (Object[] instanceRow : testbed) {
            for (int i = 0; i < instanceRow.length; i++) {                
                assertEquals("'" + index + "' array of equal '" + className + "' instances comparison failed on comparing instance '" + i + "' with itself", instanceRow[i], instanceRow[i]);
                for (int j = i + 1; j < instanceRow.length; j++) {
//                    System.out.println( instanceRow[i].toString() + "\n");
//                    System.out.println( instanceRow[j].toString() + "\n");
                    assertEquals("'" + index + "' row of equal '" + className + "' instances comparison failed on comparing instance '" + i + "' to instance '" + j + "'", instanceRow[i], instanceRow[j]);
                    assertEquals("'" + index + "' row of equal '" + className + "' instances comparison failed on comparing instance '" + j + "' to instance '" + i + "'", instanceRow[j], instanceRow[i]);
                }
            }
            index++;
        }
    }    
}
