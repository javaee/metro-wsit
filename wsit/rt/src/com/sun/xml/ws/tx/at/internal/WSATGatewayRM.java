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

import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.tx.at.WSATXAResource;
import com.sun.xml.ws.tx.at.common.TransactionManagerImpl;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;


/**
 *
 * @author paulparkinson
 */

/**
 * Gateway XAResource for managing outbound WS-AT transaction branches.
 */
public class WSATGatewayRM implements XAResource {
  private static WSATGatewayRM singleton;

  private String resourceRegistrationName; // JTA resource registration name
  private Map<Xid, BranchRecord> branches; // xid to Branch
  private List<Xid> pendingXids; // collection of Xids
  private final Object currentXidLock = new Object();
  private byte[] currentBQual; //todo existance of currentXid actually makes this unnecessary
  private Xid currentXid;

  static {
      create("server"); 
  }
  
  private WSATGatewayRM() {
  }

  private WSATGatewayRM(String serverName) {
    this.resourceRegistrationName = "RM_NAME_PREFIX" + serverName;
    this.branches = Collections.synchronizedMap(new HashMap<Xid, BranchRecord>());
    this.pendingXids = Collections.synchronizedList(new ArrayList<Xid>());
  }

  public static synchronized WSATGatewayRM getInstance() {
    return singleton;
  }

    /**
     * Called as part of WSATTransactionService start
     * @param serverName this server's name
     * @param store PersistentStore to be used
     * @return the WSATGatewayRM singleton that WSATTransactionService will call stop on during stop/shutdown
     * @throws SystemException if there is any issue while registerResourceWithTM
     * @throws PersistentStoreException if there is any issue with initStore or recoverPendingBranches
     */
  public static synchronized WSATGatewayRM create(String serverName)
  {
    if (singleton == null) {
      singleton = new WSATGatewayRM(serverName);
      singleton.initStore();
      singleton.recoverPendingBranches();
      singleton.registerResourceWithTM();
    }
    return singleton;
  }

    /**
     * Called for create of WSATGatewayRM
     * @throws PersistentStoreException persistentStoreException
     */
  private void initStore()  {
  }

    /**
     * Called for create of WSATGatewayRM
     * @throws PersistentStoreException persistentStoreException
     */
  private void recoverPendingBranches() {
 /**   if (WSATHelper.isDebugEnabled())
        debug("recoverPendingBranches()");
    PersistentStoreConnection.Cursor cursor = storeConn.createCursor(0);
    PersistentStoreRecord rec;
    while ((rec = cursor.next()) != null) {
      BranchRecord branch = (BranchRecord) rec.getData();
      branch.setStoreHandle(rec.getHandle());
      branches.put(branch.getXid(), branch);
      pendingXids.addAll(branch.getAllXids());
      if (WSATHelper.isDebugEnabled())
          debug("recovered: " + branch);
    }*/
  }

    /**
     * Called for create of WSATGatewayRM
     * @throws SystemException systemException
     */
  private void registerResourceWithTM() {
   // getTM().registerDynamicResource(resourceRegistrationName, this);
  }

  public void stop() {
    try {
      unregisterResource();
    } catch (SystemException e) {
      e.printStackTrace();
    }
  }

  private void unregisterResource() throws SystemException {
  //  TransactionManager tm = getTM();
  //  tm.unregisterResource(resourceRegistrationName, true);
  }

  /**
   * Enlist a foreign WS-AT resource in the current transaction. It is
   * assumed that the XAResource parameter wraps a WS-AT endpoint. Invoked in
   * the outbound case.
   *
   * @param xid
   *          The current, superior transaction id.
   * @param wsatResource
   *          The foreign WS-AT resource.
   * @throws SystemException from enlistResource
   * @throws RollbackException from enlistResource
   * @throws IllegalStateException from enlistResource
   * @return Xid xid
   */
  public Xid registerWSATResource(Xid xidd, XAResource wsatResource, Transaction tx)
      throws IllegalStateException, RollbackException, SystemException {
    // enlist each WSAT resource, specifically each endpoint, as a separate branch alias
  //  Transaction tx = getTransaction(xid);
    if (tx == null)
        throw new IllegalStateException("Transaction " + tx + " does not exist, wsatResource=" + wsatResource);
    // enlist primary, read-only branch (ensures 2PC)

    /** this is all moved after enlist due to changing xid in GF
    BranchRecord branch = getOrCreateBranch(xid);
     //todo this temporarily removed, this could an issue/inefficiency if a subordinate incorrectly registers twice
    XAResource resource = branch.exists(wsatResource);
    if (resource!=null) {
      return ((WSATXAResource)resource).getXid().getBranchQualifier();
    }
    branch.addSubordinate(wsatResource);
     */
    // enlist resource branch alias
    byte[] bqual;
    synchronized(currentXidLock) {
   //   String branchName = branch.getBranchName(wsatResource) + WSATHelper.assignUUID();
   //   tx.enlistResource(this, branchName);
      tx.enlistResource(this);
      bqual = currentBQual;
      currentBQual = null;
      //todo this is a mismatch/overwrite as the as the bqual is set in BaseRegistration, yet the xid is set here,
      // this is again due to changing xid in GF
      ((WSATXAResource)wsatResource).setXid(currentXid);
      BranchRecord branch = getOrCreateBranch(currentXid);
      branch.addSubordinate(currentXid, wsatResource);
      tx.enlistResource(new WSATNoOpXAResource());
      if (WSATHelper.isDebugEnabled())
        debug("registerWSATResource() xid=" + currentXid + " currentBQual=" + String.valueOf(currentBQual) +
                " bqual="+ String.valueOf(bqual));
    }
    return currentXid;
  }


    /**
     * Implementation of Subordinate/ServerXAResource called in reaction to registerWSATResource enlistResource call
     * This should be the only use/patch of this method
     * NOTE: lock on currentBQual must be obtained before calling this method as it is in
     * @param xid
     * @param flags
     * @throws XAException
     */
    public void start(Xid xid, int flags) throws XAException {
    currentXid = xid;
    debug("start currentXid:"+currentXid);
    debug("start xid:"+xid);
    debug("start currentXid bqual:"+currentXid.getBranchQualifier());
    debug("start currentXid formatid:"+currentXid.getFormatId());
    debug("start currentXid gtrid:"+currentXid.getGlobalTransactionId());
    currentBQual = currentXid.getBranchQualifier();
    if (WSATHelper.isDebugEnabled())
        debug("start() xid=" + xid + ", flags=" + flags + ", currentBQual=" + String.valueOf(currentBQual));
    switch (flags) {
    case XAResource.TMNOFLAGS:
      getOrCreateBranch(xid);
      break;
    case XAResource.TMRESUME:
    case XAResource.TMJOIN:
      BranchRecord branch = getBranch(xid);
      if (branch == null) {
        JTAHelper.throwXAException(XAException.XAER_NOTA, "Attempt to resume xid "
            + xid + " that is not in SUSPENDED state.");
      }
      break;
    case XAResource.TMFAIL:
      // should initiate branch rollback, but RMERR will cause a rollback retry
      JTAHelper.throwXAException(XAException.XAER_RMERR,
          "error while attempting to rollback branch" + resourceRegistrationName);
      break;
    default:
      throw new IllegalArgumentException("invalid flag:" + flags);
    }
  }

  public void end(Xid xid, int flags) throws XAException {
    if (WSATHelper.isDebugEnabled())
        debug("end() xid=" + xid + ", flags=" + flags);
    BranchRecord branch = getBranch(xid);
    if (branch == null) {
      JTAHelper.throwXAException(XAException.XAER_NOTA,
          "end: no branch info for " + xid);
    }
  }

  public int prepare(Xid xid) throws XAException {
    if (WSATHelper.isDebugEnabled()) debug("prepare() xid=" + xid);
    BranchRecord branch = getBranch(xid);
    if (WSATHelper.isDebugEnabled()) debug("prepare() xid=" + xid+" branch="+branch);
    if (branch == null) {
      JTAHelper.throwXAException(XAException.XAER_NOTA, "prepare: no branch info for " + xid);
    }
    if (WSATHelper.isDebugEnabled()) debug("prepare() xid=" + xid);
    persistBranchIfNecessary(branch);
    return branch.prepare(xid);
  }

  public void commit(Xid xid, boolean onePhase) throws XAException {
    if (WSATHelper.isDebugEnabled()) debug("commit() xid=" + xid);
    BranchRecord branch = getBranch(xid);
    if (branch == null) {
      JTAHelper.throwXAException(XAException.XAER_NOTA, "commit: no branch information for xid:" + xid);
    }
    try {
      branch.commit(xid, onePhase);
    } finally {
      deleteBranchIfNecessary(branch);
      // TODO - increment statistics
    }
  }

  public void rollback(Xid xid) throws XAException {
    if (WSATHelper.isDebugEnabled())
        debug("rollback() xid=" + xid);
    BranchRecord branch = getBranch(xid);
    if (branch == null) {
      JTAHelper.throwXAException(XAException.XAER_NOTA,
          "rollback: no branch info for " + xid);
    }
    try {
      branch.rollback(xid);
    } finally {
      deleteBranchIfNecessary(branch);
      // TODO - increment statistics
    }
  }

  public Xid[] recover(int flag) throws XAException {
    if (WSATHelper.isDebugEnabled()) debug("recover() flag=" + flag);
    // return all pending Xids on first call, empty array otherwise
    if ((flag & XAResource.TMSTARTRSCAN) != 0) {
      if (WSATHelper.isDebugEnabled()) debug("WSAT recover("+flag+") returning " + pendingXids);
      Xid[] xids = pendingXids.toArray(new Xid[pendingXids.size()]);
      return xids;
    }
    if (WSATHelper.isDebugEnabled()) debug("recover() returning nothing");
    return new Xid[0];
  }

  public void forget(Xid xid) throws XAException {
    if (WSATHelper.isDebugEnabled()) debug("forget() xid=" + xid);
    BranchRecord branch = getBranch(xid);
    if (branch == null) JTAHelper.throwXAException(XAException.XAER_NOTA, "forget: no branch info for " + xid);
    deleteBranchIfNecessary(branch);
  }

    /**
     * Not applicable
     * @return int -1 as not applicable
     * @throws XAException
     */
  public int getTransactionTimeout() throws XAException {
    return -1;
  }

    /**
     * Not applicable
     * @param seconds int
     * @return boolean always false as not applicable
     * @throws XAException xaException
     */
  public boolean setTransactionTimeout(int seconds) throws XAException {
    return false;
  }

    /**
     * There is only one WSATGatewayRM per server for active transactions and isSameRM should not be called for
     *  any migrated WSATGatewayRM instances
     * @param xares XAResource
     * @return boolean if is same RM which in this WSATGatewayRM case means means the same instance
     * @throws XAException
     */
  public boolean isSameRM(XAResource xares) throws XAException {
    if (!(xares instanceof WSATGatewayRM)) return false;
    WSATGatewayRM oxares = (WSATGatewayRM) xares;
    return this.equals(oxares);
  }


    /**
     * Return true as WSATGatewayRM is always available, health should not change.
     * @return boolean
     */
  public boolean detectedUnavailable() {
    return true;
  }

    /**
     * Always returns TMSUCCESS, avoids unnecessary suspend
     * @return int TMSUCCESS delist flag
     */
  public int getDelistFlag() {
    return TMSUCCESS;
  }

    /**
     * Called from registerWSATResource to gate enlist and again from start with Xid with branchqual
     * therefore it is important that branchqual is not in equality check as we'll have two branchrecords
     * @param xid Xid
     * @return  BranchRecord branchRecord
     */
  private synchronized BranchRecord getOrCreateBranch(Xid xid) {
    BranchRecord branch = getBranch(xid);
    if (branch == null) {
      branch = new BranchRecord(xid);
      branches.put(xid, branch);
    }
    return branch;
  }

  private synchronized BranchRecord getBranch(Xid xid) {
    BranchRecord branch = branches.get(xid);
    if (branch != null && xid.getBranchQualifier() != null) branch.assignBranchXid(xid);
    return branch;
  }

  private void delete(BranchRecord branch)  {
    releaseBranchRecord(branch);
    branches.remove(branch.getXid());
    pendingXids.removeAll(branch.getAllXids());
    branch.cleanup();
  }

    /**
     * Called after prepare in order to persist branch record.
     * @param branch BranchRecord
     * @throws PersistentStoreException persistentStoreException from PersistentStoreTransaction commit
     */
  private void persistBranchRecord(BranchRecord branch) {
 /**   if (WSATHelper.isDebugEnabled())
        debug("persist branch record " + branch);
    PersistentStoreTransaction ptx = store.begin();
    PersistentHandle handle = storeConn.create(ptx, branch, STORE_NO_FLAGS);
    ptx.commit();
    branch.setStoreHandle(handle);
    branch.setLogged(true); */
  }

    /**
     * Called after rollback, commit, and forget in order to delete branch record.
     * @param branch BranchRecord
     * @throws PersistentStoreException persistentStoreException from PersistentStoreTransaction.commit
     */
  private void releaseBranchRecord(BranchRecord branch) {
/**    if (WSATHelper.isDebugEnabled())
        debug("release branch record " + branch);
    PersistentHandle handle = branch.getStoreHandle();
    if (handle == null) return;
    PersistentStoreTransaction ptx = store.begin();
    storeConn.delete(ptx, handle, STORE_NO_FLAGS);
    ptx.commit();
    branch.setStoreHandle(null);
    branch.setLogged(false); */
  }

  private void persistBranchIfNecessary(BranchRecord branch) throws XAException {
/**    try {
      synchronized(branch) {
        if (!branch.isLogged()) {
          persistBranchRecord(branch);
          pendingXids.addAll(branch.getAllXids());
        }
      }
    } catch(PersistentStoreException pse) {
      debug("error persisting branch " + branch + ": " + pse.toString());
      WseeWsatLogger.logErrorPersistingBranchRecord(branch.toString(), pse);
      JTAHelper.throwXAException(XAException.XAER_RMERR, "Error persisting branch " + branch, pse);
    } */
  }

  private boolean deleteBranchIfNecessary(BranchRecord branch) throws XAException {
 /**   boolean deleted = false;
    try {
      synchronized (branch) {
        if (branch.isLogged() && branch.allResourcesCompleted()) {
          delete(branch);
          deleted = true;
        }
      }
    } catch(PersistentStoreException pse) {
      debug("error deleting branch record " + branch + ": " + pse.toString());
      WseeWsatLogger.logErrorDeletingBranchRecord(branch.toString(), pse);
      JTAHelper.throwXAException(XAException.XAER_RMERR, "Error deleting branch record " + branch, pse);
    }
    return deleted; */ return true;
  }

  /**
   * Used to register and unregister
   * @return TransactionManager
   */
  private TransactionManager getTM() {
      return TransactionManagerImpl.getInstance();
  }


  /**
   * Used to register
   * @param xid Xid
   * @return Transaction transaction corresponding to Xid
   */
  private Transaction getTransaction(Xid xid) {
      if(m_transaction!=null)return m_transaction; //for testing only
        try {
            return TransactionManagerImpl.getInstance().getTransaction();
        } catch (SystemException ex) {
            Logger.getLogger(WSATGatewayRM.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
  }

  //for testing only
  private static TransactionManager m_transactionManager;
  static void setTM(TransactionManager transactionManager) {
      m_transactionManager = transactionManager;
  }
  private static Transaction m_transaction;
  static void setTx(Transaction transaction) {
      m_transaction = transaction;
  }

  private void debug(String msg) {
   //   System.out.println("wsatgatewayrm debug:"+msg);
 /**   if (WSATHelper.isDebugEnabled()) {
      debugWSAT.debug("[WSATGatewayRM] " + resourceRegistrationName + " msg:" + msg);
    }
 */ }


  private final class BranchObjectHandler { // implements ObjectHandler {
    private static final int VERSION = 1;

    public Object readObject(ObjectInput in) throws ClassNotFoundException, IOException {
      int version = in.readInt();
      if (version != VERSION)
          throw new IOException("Stream corrupted.  Invalid WS-AT gateway branch version: " + version);
      BranchRecord branch = new BranchRecord();
      branch.readExternal(in);
      if (WSATHelper.isDebugEnabled()) debug("read WS-AT branch " + branch);
      return branch;
    }

    public void writeObject(ObjectOutput out, Object o) throws IOException {
      if (!(o instanceof BranchRecord))
          throw new IOException("Cannot serialize class of type: " + (o == null ? null : o.getClass().toString()) );
      out.writeInt(VERSION);
      BranchRecord branch = (BranchRecord) o;
      branch.writeExternal(out);
      if (WSATHelper.isDebugEnabled()) debug("serialized WS-AT branch " + branch);
    }
  }
}

