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

package com.sun.xml.ws.mex.server;

import javax.xml.namespace.QName;
import junit.framework.TestCase;

/**
 *
 * @author Fabian Ritzmann
 */
public class MEXAddressResolverTest extends TestCase {
    
    public MEXAddressResolverTest(String testName) {
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
     * Test of getAddressFor method, of class MEXAddressResolver.
     */
    public void testGetAddressForNull() {
        QName serviceName = null;
        QName portName = null;
        String address = null;
        try {
            MEXAddressResolver instance = new MEXAddressResolver(serviceName, portName, address);
            fail("Expected a NullPointerException");
        } catch (NullPointerException e) {
            // This exception is expected
        }
    }

    /**
     * Test of getAddressFor method, of class MEXAddressResolver.
     */
    public void testGetAddressForNullAddress() {
        QName serviceName = new QName("namespace", "service");
        QName portName = new QName("namespace", "port");
        String address = null;
        MEXAddressResolver instance = new MEXAddressResolver(serviceName, portName, address);
        String result = instance.getAddressFor(serviceName, portName.getLocalPart());
        assertNull(result);
    }

    /**
     * Test of getAddressFor method, of class MEXAddressResolver.
     */
    public void testGetAddressForAddress() {
        QName serviceName = new QName("namespace", "service");
        QName portName = new QName("namespace", "port");
        String address = "http://myaddress/";
        MEXAddressResolver instance = new MEXAddressResolver(serviceName, portName, address);
        String expResult = address;
        String result = instance.getAddressFor(serviceName, portName.getLocalPart());
        assertEquals(expResult, result);
    }

    /**
     * Test of getAddressFor method, of class MEXAddressResolver.
     */
    public void testGetAddressForNullAddressNull() {
        QName serviceName = new QName("namespace", "service");
        QName portName = new QName("namespace", "port");
        String address = null;
        MEXAddressResolver instance = new MEXAddressResolver(serviceName, portName, address);
        String result = instance.getAddressFor(serviceName, portName.getLocalPart(), null);
        assertNull(result);
    }

    /**
     * Test of getAddressFor method, of class MEXAddressResolver.
     */
    public void testGetAddressForNoUrlAddressNull() {
        QName serviceName = new QName("namespace", "service");
        QName portName = new QName("namespace", "port");
        String address = "myaddress";
        MEXAddressResolver instance = new MEXAddressResolver(serviceName, portName, address);
        String expResult = address;
        String result = instance.getAddressFor(serviceName, portName.getLocalPart(), null);
        assertEquals(expResult, result);
    }

    /**
     * Test of getAddressFor method, of class MEXAddressResolver.
     */
    public void testGetAddressForUrlAddressNull() {
        QName serviceName = new QName("namespace", "service");
        QName portName = new QName("namespace", "port");
        String address = "http://myaddress/";
        MEXAddressResolver instance = new MEXAddressResolver(serviceName, portName, address);
        String expResult = address;
        String result = instance.getAddressFor(serviceName, portName.getLocalPart(), null);
        assertEquals(expResult, result);
    }

    /**
     * Test of getAddressFor method, of class MEXAddressResolver.
     */
    public void testGetAddressForUrlNameNotEqual() {
        QName serviceName = new QName("namespace", "service");
        QName port1 = new QName("namespace", "port1");
        String port2 = "port2";
        String address1 = "http://myaddress/";
        String address2 = "http://myaddress2/";
        MEXAddressResolver instance = new MEXAddressResolver(serviceName, port1, address1);
        String result = instance.getAddressFor(serviceName, port2, address2);
        assertNull(result);
    }

    /**
     * Test of getAddressFor method, of class MEXAddressResolver.
     */
    public void testGetAddressForUrlSameAddress() {
        QName serviceName = new QName("namespace", "service");
        QName port = new QName("namespace", "port");
        String address = "http://myaddress/";
        MEXAddressResolver instance = new MEXAddressResolver(serviceName, port, address);
        String expResult = address;
        String result = instance.getAddressFor(serviceName, port.getLocalPart(), address);
        assertEquals(expResult, result);
    }

    /**
     * Test of getAddressFor method, of class MEXAddressResolver.
     */
    public void testGetAddressForUrlHttpsToHttp() {
        QName serviceName = new QName("namespace", "service");
        QName port = new QName("namespace", "port");
        String address1 = "https://myaddress/";
        String address2 = "http://myaddress/";
        MEXAddressResolver instance = new MEXAddressResolver(serviceName, port, address1);
        String expResult = address2;
        String result = instance.getAddressFor(serviceName, port.getLocalPart(), address2);
        assertEquals(expResult, result);
    }

    /**
     * Test of getAddressFor method, of class MEXAddressResolver.
     */
    public void testGetAddressForUrlFtpToHttp() {
        QName serviceName = new QName("namespace", "service");
        QName port = new QName("namespace", "port");
        String address1 = "ftp://myaddress/";
        String address2 = "http://myaddress/";
        MEXAddressResolver instance = new MEXAddressResolver(serviceName, port, address1);
        String expResult = address1;
        String result = instance.getAddressFor(serviceName, port.getLocalPart(), address2);
        assertEquals(expResult, result);
    }

}
