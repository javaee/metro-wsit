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

import com.sun.xml.ws.tx.at.runtime.TransactionServices;
import com.sun.xml.ws.tx.at.WSATException;

import javax.transaction.*;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;

public class TransactionServicesImpl implements TransactionServices {



    public static TransactionServices getInstance() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public byte[] getGlobalTransactionId() {
        return new byte[]{'a'};
    }

    public byte[] enlistResource(XAResource resource, Xid xid) throws WSATException {
        return new byte[]{'a'};
    }

    public void registerSynchronization(Synchronization synchronization, Xid xid) throws WSATException {
        
    }

    public int getExpires() {
        return 30000;
    }

    public Xid importTransaction(int timeout, byte[] tId) throws WSATException {
        return new XidImpl(1, new byte[]{'a'}, new byte[]{'a'});
    }

    public String prepare(byte[] tId) throws WSATException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void commit(byte[] tId) throws WSATException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void rollback(byte[] tId) throws WSATException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void replayCompletion(String tId, XAResource xaResource) throws WSATException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EndpointReference getParentReference(Xid xid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
