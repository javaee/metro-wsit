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

package com.sun.xml.ws.tx.at.internal;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.tx.at.Transactional;

import javax.transaction.Transaction;
import javax.transaction.xa.Xid;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.EndpointReference;
import java.io.*;

/**
 * Context that is persisted as part of tx log and used for bottom-up queries either during runtime or recovery.
 */
public class ForeignRecoveryContext implements Externalizable {

    private static final long serialVersionUID = -3257083889097518770L;
    private static int klassVersion =1032;
    private Xid fxid;
    private Transactional.Version version;
    private EndpointReference epr;
    private String txLogLocation;
    private boolean recovered;
    transient private static Logger LOGGER = Logger.getLogger(ForeignRecoveryContext.class);

    /**
     * For recovery, for Externalizable
     */
    public ForeignRecoveryContext() {
    }

    /**
     * For runtime, called before Register call by ForeignRecoveryContextManager
     * @param xid Xid
     */
    ForeignRecoveryContext(Xid xid) {
        this.fxid = xid;
    }

    /**
     * Sets the parent coordinator's epr as obtained from RegisterResponse
     * @param epr EndpointReference Transactional.Version
     * @param version Transactional.Version
     */
     public void setEndpointReference(EndpointReference epr, Transactional.Version version) {
        this.epr = epr;
        this.version = version;
    }

      public Xid getXid() {
        return fxid;
      }

 /* *//**
     * Called by add and contextworker in order to obtain tid
     * @return byte[] tid
     *//*
    byte[] getTid() {
        return tid;
    }
*/
    /**
     * Called by participant and contextworker in order to get parentreference of coordinator port
     * @return EndpointReference
     */
    public EndpointReference getEndpointReference() {
        return epr;
    }

    /**
     * Called by contextworker to get version for coordinator port
     * @return Transactional.Version
     */
     Transactional.Version getVersion() {
        return version;
     }

    /**
     * Called from ForeignRecoveryContextManager$ContextTimerListener.timerExpired
     *
     * @return .transaction.Transaction
     */
    Transaction getTransaction() {
        if (fxid == null) {
            throw new AssertionError("No Tid to Xid mapping for " + this);
        }
        return null;
        //todoremove ((TransactionManager)TransactionHelper.getTransactionHelper().
        //todoremove         getTransactionManager()).getTransaction(fxid);
    }


    /**
     * Reads context from ObjectInput and adds to contextmanager
     * @param in ObjectInput
     * @throws ClassNotFoundException classNotFoundException from in.readObject()
     * @throws IOException ioException
     */
    public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException{
        klassVersion = in.readInt();
        fxid = (Xid) in.readObject();
        debug("ForeignRecoveryContext.readExternal tid:" + fxid);
        version = (Transactional.Version) in.readObject();
        int len = in.readInt();
        byte[] eprBytes = new byte[len];
        in.readFully(eprBytes);
        epr = EndpointReference.readFrom(new StreamSource(new ByteArrayInputStream(eprBytes)));
        debug("ForeignRecoveryContext.readExternal EndpointReference:" + epr);
        ForeignRecoveryContextManager.getInstance().add(this);
    }

    /**
     * Writes context to ObjectInput
     * @param out ObjectInput
     * @throws IOException ioException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(klassVersion);
        out.writeObject(fxid);
        out.writeObject(version);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        epr.writeTo(new StreamResult(bos));
        byte[] eprBytes = bos.toByteArray();
        out.writeInt(eprBytes.length);
        out.write(eprBytes);
    }

    public String toString() {
        return "ForeignRecoveryContext[tid=" + fxid + ", endPointreference="
                + epr + ", version = "
                + version + "]";
    }

    private void debug(String msg) {
        LOGGER.info(msg);
    }

    public void setTxLogLocation(String logLocation) {
        txLogLocation = logLocation;
    }

    public String getTxLogLocation() {
        return txLogLocation;
    }

    public void setRecovered() {
        recovered = true;
    }

    public boolean isRecovered() {
        return recovered;
    }
}
