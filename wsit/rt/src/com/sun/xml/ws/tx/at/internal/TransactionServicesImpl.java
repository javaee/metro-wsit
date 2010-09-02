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
package com.sun.xml.ws.tx.at.internal;

import com.sun.xml.ws.tx.at.WSATConstants;
import com.sun.xml.ws.tx.at.runtime.TransactionServices;
import com.sun.xml.ws.tx.at.WSATException;
import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.tx.at.common.TransactionImportManager;
import com.sun.xml.ws.tx.at.common.TransactionManagerImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.*;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;

public class TransactionServicesImpl implements TransactionServices {
    private static TransactionServices INSTANCE;
    static List<Xid> importedXids = new ArrayList<Xid>(); //todo leak cleanup in rollback and commit


    public static TransactionServices getInstance() {
        if(INSTANCE==null) INSTANCE = new TransactionServicesImpl();
        return INSTANCE;
    }

    public byte[] getGlobalTransactionId() {
        return new byte[]{'a'};
    }


  public byte[] enlistResource(XAResource resource, Xid xid)
          throws WSATException {
    WSATGatewayRM wsatgw = WSATGatewayRM.getInstance();
    if (wsatgw == null) throw new WSATException("WS-AT gateway not deployed.");
    Transaction transaction = WSATHelper.getInstance().getFromXidToTransactionMap(xid);
    try {
       return wsatgw.registerWSATResource(xid, resource, transaction);
    } catch (IllegalStateException e) {
      throw new WSATException(e);
    } catch (RollbackException e) {
      throw new WSATException(e);
    } catch (SystemException e) {
      throw new WSATException(e);
    }
  }
    public void registerSynchronization(Synchronization synchronization, Xid xid) throws WSATException {
        log("regsync");
    }

    public int getExpires() {
        return 30000;
    }

    public Xid importTransaction(int timeout, byte[] tId) throws WSATException {
        final XidImpl xidImpl = new XidImpl(tId);
        if(importedXids.contains(xidImpl)) return xidImpl;
        TransactionImportManager.getInstance().recreate(xidImpl, timeout);
        importedXids.add(xidImpl);
        //todo getterminator and infect - then getTx and return txid (temporarily use hashcode)
        return new XidImpl(1, new byte[]{'a'}, new byte[]{'a'});
    }

    public String prepare(byte[] tId) throws WSATException {
        log("prepare");
        final XidImpl xidImpl = new XidImpl(tId);
        int vote;
        try {
            vote = TransactionImportManager.getInstance().getXATerminator().prepare(xidImpl);
        } catch (XAException ex) {
            Logger.getLogger(TransactionServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new WSATException(ex);
        }
        return vote==XAResource.XA_OK?WSATConstants.PREPARED:WSATConstants.READONLY;
    }

    public void commit(byte[] tId) throws WSATException {
        log("commit");
        final XidImpl xidImpl = new XidImpl(tId);
        try {
            TransactionImportManager.getInstance().getXATerminator().commit(xidImpl, false);
        } catch (XAException ex) {
            Logger.getLogger(TransactionServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new WSATException(ex);
        }
    }

    public void rollback(byte[] tId) throws WSATException {
        log("rollback");
        final XidImpl xidImpl = new XidImpl(tId);
        try {
            TransactionImportManager.getInstance().getXATerminator().rollback(xidImpl);
        } catch (XAException ex) {
            Logger.getLogger(TransactionServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new WSATException(ex);
        }
    }

    public void replayCompletion(String tId, XAResource xaResource) throws WSATException {
        log("replayCompleiton");
    }

    public EndpointReference getParentReference(Xid xid) {
      log("getParentReference");
      if (xid == null) throw new IllegalArgumentException("No subordinate transaction " + xid);
   //todo readd this and either set it on thread for getResource call below or get the frc off it directly somehow
   //   Transaction tx = (Transaction) getTM().getTransaction(xid);
   //   if (tx == null) throw new IllegalArgumentException("No subordinate transaction " + xid);
      ForeignRecoveryContext foreignRecoveryContext =
              (ForeignRecoveryContext)TransactionManagerImpl.getInstance().getResource(
                    WSATConstants.TXPROP_WSAT_FOREIGN_RECOVERY_CONTEXT);
      //          (ForeignRecoveryContext) tx.getProperty(WSATConstants.TXPROP_WSAT_FOREIGN_RECOVERY_CONTEXT);
      if (foreignRecoveryContext == null)
          throw new AssertionError("No recovery context associated with transaction " + xid);
      return foreignRecoveryContext.getEndpointReference();
    }

    private void log(String msg){
     //   System.out.println("txservicesimpl:"+msg);
    }

}
