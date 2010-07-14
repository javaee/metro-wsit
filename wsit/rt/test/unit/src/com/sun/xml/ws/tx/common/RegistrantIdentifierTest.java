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

package com.sun.xml.ws.tx.common;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import junit.framework.TestCase;

/**
 *
 * @author Ryan.Shoemaker@Sun.COM
 */
public class RegistrantIdentifierTest extends TestCase {
    
    /** Creates a new instance of RegistrantIdentifierTest */
    public RegistrantIdentifierTest(String name) {
        super(name);
    }

    private String idValue = "242";
    private RegistrantIdentifier id = new RegistrantIdentifier(idValue);
    
    public void testValue() throws Exception {
        assertEquals(idValue, id.getValue());
    }
    
    public void testName() throws Exception {
        assertEquals(new QName(Constants.WSCOOR_SUN_URI, RegistrantIdentifier.REGISTRANT_ID), 
                     id.getName());
    }
    
    public void testSetValue() throws Exception {
        id.setValue("808");
        assertEquals("808", id.getValue());
        id.setValue(idValue);
    }
}
