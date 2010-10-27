/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.tx.at.internal;

import junit.framework.TestCase;

/**
 * User: paulparkinson
 */
public class WSATGatewayRMTest extends TestCase {
    public WSATGatewayRMTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    
    public void testInitStore() throws Exception {
        WSATGatewayRM testWSATGatewayRM = new WSATGatewayRM("unittestserver");
        testWSATGatewayRM.initStore("testlocation");
    }


    public void xtestRecoverPendingBranches() throws Exception {
        WSATGatewayRM testWSATGatewayRM = new WSATGatewayRM("unittestserver");
        testWSATGatewayRM.recoverPendingBranches();
    }
    
    /**
    public void testRegister() throws Exception {
        // need to stub  for registerResourceWithTM in create call here...
        WSATGatewayRM.setTM(new TestTransactionManager());
        WSATGatewayRM wsatGatewayRM = WSATGatewayRM.create("serverName", new TestPersistentStore());
        Xid xid =  new TestXid(true);
        String address = "testaddress";
        Node[] node0 = new Node[]{createElement("test")};
        MemberSubmissionEndpointReference epr0_0 =
                EndpointReferenceBuilder.MemberSubmission().address(address).referenceParameter(node0).build();
        WSATXAResource wsatXAResource = new WSATXAResource(epr0_0, xid);
        TransactionStub transactionStub = new TransactionStub();
        WSATGatewayRM.setTx(transactionStub);
        assertEquals("transactionStub.enlistedNamedResources.size()", transactionStub.enlistedNamedResources.size(), 0);
        assertEquals("transactionStub.enlistedResources.size()", transactionStub.enlistedResources.size(), 0);
        byte[] branchqual = wsatGatewayRM.registerWSATResource(xid, wsatXAResource);
        assertEquals("transactionStub.enlistedNamedResources.size()", transactionStub.enlistedNamedResources.size(), 1);
        assertEquals("transactionStub.enlistedResources.size()", transactionStub.enlistedResources.size(), 1);
    }
     * */
    
}
