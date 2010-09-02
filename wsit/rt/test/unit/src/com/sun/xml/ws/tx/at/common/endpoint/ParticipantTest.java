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
import com.sun.xml.ws.tx.at.runtime.TransactionServices;
import com.sun.xml.ws.tx.at.WSATConstants;
import com.sun.xml.ws.tx.at.common.CoordinatorIF;
import com.sun.xml.ws.tx.at.common.WSATVersion;
import com.sun.xml.ws.tx.at.common.WSATVersionStub;

import javax.xml.ws.WebServiceContext;

/**
 *
 * @author paulparkinson
 */
public class ParticipantTest extends TestCase {

   public void testRollback() throws Exception {
      WebServiceContext context = null;
      WSATVersion version = new WSATVersionStub();
      EmulatedCoordinator testCoordinator = EmulatedCoordinator.createDefault();
       EmulatedTransactionServices testTransactionServices = new EmulatedTransactionServices();
      byte[] testTid = new byte[]{'a'};
      Participant participant =
              new TestParticipant(context, version, testCoordinator, testTransactionServices, testTid);
      assertEquals("abortedOperationCount before rollback call", 0, testCoordinator.abortedOperationCount);
      participant.rollback(null);
      assertEquals("abortedOperationCount after rollback call", 1, testCoordinator.abortedOperationCount);
   }

   public void testPreparedVote() throws Exception {
      EmulatedCoordinator testCoordinator = EmulatedCoordinator.createDefault();
      EmulatedTransactionServices testTransactionServices = new EmulatedTransactionServices();
      Participant participant = createTestParticipant(testCoordinator, testTransactionServices);
      assertEquals("preparedOperationCount before prepare call", 0, testCoordinator.preparedOperationCount);
      participant.prepare(null);
      assertEquals("preparedOperationCount after prepare call", 1, testCoordinator.preparedOperationCount);
   }

   public void testReadOnlyVote() throws Exception {
      EmulatedCoordinator testCoordinator = EmulatedCoordinator.createDefault();
      EmulatedTransactionServices testTransactionServices = new EmulatedTransactionServices();
      testTransactionServices.setPrepareVoteReturn(WSATConstants.READONLY);
      Participant participant = createTestParticipant(testCoordinator, testTransactionServices);
      assertEquals("preparedOperationCount before prepare call", 0, testCoordinator.readOnlyOperationCount);
      participant.prepare(null);
      assertEquals("preparedOperationCount after prepare call", 1, testCoordinator.readOnlyOperationCount);
   }

    private Participant createTestParticipant(EmulatedCoordinator testCoordinator, EmulatedTransactionServices testTransactionServices) {
        WebServiceContext context = null;
        WSATVersion version = new WSATVersionStub();
        byte[] testTid = new byte[]{'a'};
        Participant participant =
                new TestParticipant(context, version, testCoordinator, testTransactionServices, testTid);
        return participant;
    }

    public void testPrepareException() throws Exception {
      WebServiceContext context = null;
      WSATVersion version = new WSATVersionStub();
      EmulatedCoordinator testCoordinator = EmulatedCoordinator.createDefault();
      EmulatedTransactionServices testTransactionServices = new EmulatedTransactionServices();
      testTransactionServices.m_isPrepareException = true;
      byte[] testTid = new byte[]{'a'};
      Participant participant =
              new TestParticipant(context, version, testCoordinator, testTransactionServices, testTid);
      assertEquals("preparedOperationCount before prepare call", 0, testCoordinator.preparedOperationCount);
      assertEquals("abortedOperationCount before prepare call", 0, testCoordinator.abortedOperationCount);
      participant.prepare(null);
      assertEquals("preparedOperationCount after prepare exception call", 0, testCoordinator.preparedOperationCount);
      assertEquals("abortedOperationCount after prepare exceptioncall", 1, testCoordinator.abortedOperationCount);
   }

   public void testCommit() throws Exception {
      WebServiceContext context = null;
      WSATVersion version = new WSATVersionStub();
      EmulatedCoordinator testCoordinator = EmulatedCoordinator.createDefault();
      EmulatedTransactionServices testTransactionServices = new EmulatedTransactionServices();
      byte[] testTid = new byte[]{'a'};
      Participant participant =
              new TestParticipant(context, version, testCoordinator, testTransactionServices, testTid);
      assertEquals("committedOperationCount before commit call", 0, testCoordinator.committedOperationCount);
      participant.commit(null);
      assertEquals("committedOperationCount after commit call", 1, testCoordinator.committedOperationCount);
   }

   public void testCommitException() throws Exception {   //failure
      WebServiceContext context = null;
      WSATVersion version = new WSATVersionStub();
      EmulatedCoordinator testCoordinator = EmulatedCoordinator.createDefault();
      EmulatedTransactionServices testTransactionServices = new EmulatedTransactionServices();
      testTransactionServices.m_isCommitException = true;
      byte[] testTid = new byte[]{'a'};
      Participant participant =
              new TestParticipant(context, version, testCoordinator, testTransactionServices, testTid);
      assertEquals("committedOperationCount before commit call", 0, testCoordinator.committedOperationCount);
      participant.commit(null);
      assertEquals("committedOperationCount after commit exception call", 1, testCoordinator.committedOperationCount); //todo change as we interpret exception beyond nota
   }

    class TestParticipant extends Participant {
      CoordinatorIF m_coordinatorIF;
      TransactionServices m_transactionServices;
      byte[] m_tid;

       @Override
       boolean isInForeignContextMap() {
           return true;
       }

       public TestParticipant(WebServiceContext context, WSATVersion version,
                             CoordinatorIF coordinatorIF, TransactionServices transactionServices, byte[] tid) {
         super(context, version);
         m_coordinatorIF = coordinatorIF;
         m_transactionServices = transactionServices;
         m_tid = tid;
      }

      TransactionServices getTransactionaService() {
         return m_transactionServices;
      }

      byte[] getWSATTid() {
         return m_tid;
      }

      CoordinatorIF getCoordinatorPortType() {
         return m_coordinatorIF;
      }

       @Override
       CoordinatorIF getCoordinatorPortTypeForReplyTo() {
         return m_coordinatorIF;
       }

   }


}

