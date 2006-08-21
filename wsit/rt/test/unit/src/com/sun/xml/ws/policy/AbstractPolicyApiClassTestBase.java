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
