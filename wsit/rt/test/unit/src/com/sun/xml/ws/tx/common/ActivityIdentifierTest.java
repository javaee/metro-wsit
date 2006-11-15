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

package com.sun.xml.ws.tx.common;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import junit.framework.TestCase;

/**
 *
 * @author Ryan.Shoemaker@Sun.COM
 */
public class ActivityIdentifierTest extends TestCase {
    
    /** Creates a new instance of ActivityIdentifierTest */
    public ActivityIdentifierTest(String name) {
        super(name);
    }

    private String idValue = "242";
    private ActivityIdentifier id = new ActivityIdentifier(idValue);
    
    public void testValue() throws Exception {
        assertEquals(idValue, id.getValue());
    }
    
    public void testName() throws Exception {
        assertEquals(new QName(Constants.WSCOOR_SUN_URI, ActivityIdentifier.ACTIVITY_ID), 
                     id.getName());
    }
    
    public void testSetValue() throws Exception {
        id.setValue("808");
        assertEquals("808", id.getValue());
        id.setValue(idValue);
    }

    public void testSoapElement() throws Exception {
        SOAPElement e = id.getSOAPElement();
        assertEquals(new QName(Constants.WSCOOR_SUN_URI, ActivityIdentifier.ACTIVITY_ID), 
                     e.getElementQName());
        // TODO: more?  we don't really even marshal ID's any more so no need
    }
}
