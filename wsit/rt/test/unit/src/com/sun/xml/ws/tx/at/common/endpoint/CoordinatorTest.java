/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.tx.at.common.endpoint;

import junit.framework.TestCase;
import com.sun.xml.ws.tx.internal.XidStub;
import com.sun.xml.ws.tx.at.*;
import com.sun.xml.ws.tx.at.common.WSATVersion;
import com.sun.xml.ws.tx.at.common.WSATVersionStub;

import javax.transaction.xa.Xid;
import javax.xml.ws.WebServiceContext;

/**
 *
 * @author paulparkinson
 */
public class CoordinatorTest extends TestCase {

   public void testAll() throws Exception {
      WebServiceContext context = null;
      final Xid m_xid = new XidStub(true);
      final WSATXAResourceStub wsatXAResourceStub = WSATXAResourceTest.createStubWSATXAResourceForXid(m_xid);
      WSATVersion version = new WSATVersionStub();
      final WSATHelperStub m_wsatHelper = new WSATHelperStub(wsatXAResourceStub);
      Coordinator coordinator =  new EmulatedCoordinator(context, version, m_xid, wsatXAResourceStub, true);
      WSATXAResource xaRes = (WSATXAResource)m_wsatHelper.m_durableParticipantXAResourceMap.get(m_xid);
      assertNull("m_wsatHelper.m_durableParticipantXAResourceMap.get(m_xid)", xaRes);
      m_wsatHelper.m_durableParticipantXAResourceMap.put(m_xid, wsatXAResourceStub);
      xaRes = (WSATXAResource)m_wsatHelper.m_durableParticipantXAResourceMap.get(m_xid);
      assertNotNull("m_wsatHelper.m_durableParticipantXAResourceMap.get(m_xid)", xaRes);
      Object paramaters = null;
      coordinator.abortedOperation(paramaters);
      xaRes = (WSATXAResource)m_wsatHelper.m_durableParticipantXAResourceMap.get(m_xid);
      assertNotNull("m_wsatHelper.m_durableParticipantXAResourceMap.get(m_xid)", xaRes);
      assertEquals("xares returned and xaresstub state", ((WSATXAResourceStub)xaRes).m_status, WSATXAResource.ABORTED);
      coordinator.committedOperation(paramaters);
      assertEquals("xares returned and xaresstub state", ((WSATXAResourceStub)xaRes).m_status, WSATXAResource.COMMITTED);
      coordinator.preparedOperation(paramaters);
      assertEquals("xares returned and xaresstub state", ((WSATXAResourceStub)xaRes).m_status, WSATXAResource.PREPARED);
      coordinator.readOnlyOperation(paramaters);
      assertEquals("xares returned and xaresstubstate", ((WSATXAResourceStub)xaRes).m_status, WSATXAResource.READONLY);
      coordinator.replayOperation(paramaters);
   }
}

