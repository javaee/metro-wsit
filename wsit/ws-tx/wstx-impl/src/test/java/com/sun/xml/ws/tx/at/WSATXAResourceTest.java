/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.tx.at;

import junit.framework.TestCase;
import org.w3c.dom.*;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.sun.xml.ws.tx.at.internal.XidStub;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.tx.coord.common.EndpointReferenceBuilder;
import com.sun.xml.ws.util.DOMUtil;

/**
 *
 * @author paulparkinson
 */
public class WSATXAResourceTest extends TestCase {

    public void testEquality() throws Exception {
        Node[] node0 = new Node[]{createElement("testsame")};
        Node[] node1 = new Node[]{createElement("testsame")};
        Node[] node2 = new Node[]{createElement("test2 is different from test3")};
        Node[] node3 = new Node[]{createElement("test3")};
        Xid xid0 = new XidStub(true);
        Xid xid1 = new XidStub(true);
        Xid xid2 = new XidStub(false);
        Xid xid3 = new XidStub(false);
        String address0 = "testaddress";
        String address1 = "testaddress1";
        MemberSubmissionEndpointReference epr0_0 = EndpointReferenceBuilder.MemberSubmission().address(address0).referenceParameter(node0).build();
        MemberSubmissionEndpointReference epr0_1 = EndpointReferenceBuilder.MemberSubmission().address(address0).referenceParameter(node1).build();
        MemberSubmissionEndpointReference epr0_2 = EndpointReferenceBuilder.MemberSubmission().address(address0).referenceParameter(node2).build();
        MemberSubmissionEndpointReference epr0_3= EndpointReferenceBuilder.MemberSubmission().address(address0).referenceParameter(node3).build();
        MemberSubmissionEndpointReference epr1_1 = EndpointReferenceBuilder.MemberSubmission().address(address1).referenceParameter(node1).build();
        //test equal
        WSATXAResource testWSATXAResource = new WSATXAResource(epr0_0, xid0);
        WSATXAResource testWSATXAResource2 = new WSATXAResource(epr0_1, xid1);
        assertTrue("WSATResources", testWSATXAResource.equals(testWSATXAResource2));
        //test address not equal
        testWSATXAResource2 = new WSATXAResource(epr1_1, xid1);
        assertFalse("WSATResources", testWSATXAResource.equals(testWSATXAResource2));
        //test equal again
        testWSATXAResource2 = new WSATXAResource(epr0_1, xid1);
        assertTrue("WSATResources", testWSATXAResource.equals(testWSATXAResource2));
        //test xid not equal
        testWSATXAResource = new WSATXAResource(epr0_0, xid2);
        testWSATXAResource2 = new WSATXAResource(epr0_1, xid3);
        assertFalse("WSATResources", testWSATXAResource.equals(testWSATXAResource2));
        //test equal again
        testWSATXAResource2 = new WSATXAResource(epr0_1, xid1);
        assertTrue("WSATResources", testWSATXAResource.equals(testWSATXAResource2));
        //test node/ref-param not equal
        testWSATXAResource = new WSATXAResource(epr0_2, xid0);
        testWSATXAResource2 = new WSATXAResource(epr0_3, xid1);
        assertFalse("WSATResources", testWSATXAResource.equals(testWSATXAResource2));
    }

    public void testPrepare() throws Exception {
        Node[] node0 = new Node[]{createElement("test")};
        Xid xid0 = new XidStub(true);
        String address0 = "testaddress";
        MemberSubmissionEndpointReference epr0_0 = EndpointReferenceBuilder.MemberSubmission().address(address0).referenceParameter(node0).build();
        WSATXAResource testWSATXAResource = new WSATXAResource(epr0_0, xid0) {
            WSATHelper getWSATHelper() {
                return new WSATHelperStub();
            }

            int getWaitForReplyTimeout() {
                return 1;
            }
        };
        //first test scenario where reply is received before wait...
        testWSATXAResource.setStatus(WSATConstants.READONLY);
        assertEquals("prepare return", XAResource.XA_RDONLY, testWSATXAResource.prepare(xid0));
        testWSATXAResource.setStatus(WSATConstants.PREPARED);
        assertEquals("prepare return", XAResource.XA_OK, testWSATXAResource.prepare(xid0));
        testWSATXAResource.setStatus(WSATConstants.ABORTED);
        try {
            testWSATXAResource.prepare(xid0);
            fail("should have thrown xaex due to aborted status");
        } catch (XAException xaex) {
            assertEquals("xaerrorcode from aborted vote", XAException.XA_RBROLLBACK, xaex.errorCode);
        }
        testWSATXAResource.setStatus(WSATXAResource.ACTIVE);
        try {
            testWSATXAResource.prepare(xid0);
            fail("should have thrown xaex due to unknown status");
        } catch (XAException xaex) {
            assertEquals("xaerrorcode from unknown response", XAException.XAER_RMFAIL, xaex.errorCode);
        }
    }

    public void testRollback() throws Exception {
        Node[] node0 = new Node[]{createElement("test")};
        Xid xid0 = new XidStub(true);
        String address0 = "testaddress";
        MemberSubmissionEndpointReference epr0_0 = EndpointReferenceBuilder.MemberSubmission().address(address0).referenceParameter(node0).build();
        WSATXAResource testWSATXAResource = new WSATXAResource(epr0_0, xid0) {
            WSATHelper getWSATHelper() {
                return new WSATHelperStub();
            }

            int getWaitForReplyTimeout() {
                return 1;
            }
        };
        //first test scenario where reply is received before wait...
        testWSATXAResource.setStatus(WSATConstants.ABORTED);
        testWSATXAResource.rollback(xid0);
        testWSATXAResource.setStatus(WSATConstants.PREPARED);
        try {
            testWSATXAResource.rollback(xid0);
            fail("should have thrown xaex due to prepared status");
        } catch (XAException xaex) {
            assertEquals("xaerrorcode from aborted vote", XAException.XAER_RMFAIL, xaex.errorCode);
        }
        testWSATXAResource.setStatus(WSATXAResource.ACTIVE);
        try {
            testWSATXAResource.rollback(xid0);
            fail("should have thrown xaex due to unknown status");
        } catch (XAException xaex) {
            assertEquals("xaerrorcode from unknown response", XAException.XAER_RMFAIL, xaex.errorCode);
        }
        testWSATXAResource.setStatus(WSATXAResource.COMMITTED);
        try {
            testWSATXAResource.rollback(xid0);
            fail("should have thrown xaex due to committed status");
        } catch (XAException xaex) {
            assertEquals("xaerrorcode from unknown response", XAException.XAER_RMFAIL, xaex.errorCode); //todo revisit
        }
    }


    public void testCommit() throws Exception {
        Node[] node0 = new Node[]{createElement("test")};
        Xid xid0 = new XidStub(true);
        String address0 = "testaddress";
        MemberSubmissionEndpointReference epr0_0 = EndpointReferenceBuilder.MemberSubmission().address(address0).referenceParameter(node0).build();
        WSATXAResource wsatXAResourceTest = new WSATXAResource(epr0_0, xid0) {
            WSATHelper getWSATHelper() {
                return new WSATHelperStub();
            }

            int getWaitForReplyTimeout() {
                return 1;
            }
        };
        //first test scenario where reply is received before wait...
        wsatXAResourceTest.setStatus(WSATConstants.COMMITTED);
        wsatXAResourceTest.commit(xid0, false);
        wsatXAResourceTest.setStatus(WSATConstants.PREPARED);
        try {
            wsatXAResourceTest.commit(xid0, false);
            fail("should have thrown xaex due to prepared status");
        } catch (XAException xaex) {
            assertEquals("xaerrorcode from aborted vote", XAException.XAER_RMFAIL, xaex.errorCode);
        }
        wsatXAResourceTest.setStatus(WSATXAResource.ACTIVE);
        try {
            wsatXAResourceTest.commit(xid0, false);
            fail("should have thrown xaex due to unknown status");
        } catch (XAException xaex) {
            assertEquals("xaerrorcode from unknown response", XAException.XAER_PROTO, xaex.errorCode);
        }
        wsatXAResourceTest.setStatus(WSATXAResource.ABORTED);
        try {
            wsatXAResourceTest.commit(xid0, false);
            fail("should have thrown xaex due to committed status");
        } catch (XAException xaex) {
            assertEquals("xaerrorcode from unknown response", XAException.XAER_PROTO, xaex.errorCode); //todo revisit
        }
    }


    public static WSATXAResource createWSATXAResourceForXid(Xid xid) {
            return createWSATXAResourceForXid(xid, true);
    }

    public static WSATXAResourceStub createStubWSATXAResourceForXid(Xid xid) {
            return (WSATXAResourceStub)createWSATXAResourceForXid(xid, false);
    }

    private static Element createElement(String text) {
        Element element = DOMUtil.createDom().createElement("txID");
        element.setTextContent(text);
        return element;
    }

    /**
     *
     * @param xid
     * @param b actual impl/true or stub/false
     * @return
     */
    public static WSATXAResource createWSATXAResourceForXid(Xid xid, boolean b) {
        String address = "testaddress";
        Node[] node0 = new Node[]{createElement("test")};
        MemberSubmissionEndpointReference epr0_0 =
                EndpointReferenceBuilder.MemberSubmission().address(address).referenceParameter(node0).build();
        WSATXAResource wsatXAResource = b?new WSATXAResource(epr0_0, xid):new WSATXAResourceStub(epr0_0, xid);
        return wsatXAResource;
    }
}

