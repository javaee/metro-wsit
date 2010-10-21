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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

import javax.transaction.xa.Xid;

/**
 * Xid implementation used for persisting branch state.
 * Wrapper over  XidImpl to override semantics of hashCode and equals
 */
public class BranchXidImpl implements Xid, Externalizable {

  private Xid delegate;
  
  public BranchXidImpl() {
  }

  public BranchXidImpl(Xid xid) {
    this.delegate = xid;
  }
  
  public byte[] getBranchQualifier() {
    return delegate.getBranchQualifier();
  }

  public int getFormatId() {
    return delegate.getFormatId();
  }

  public byte[] getGlobalTransactionId() {
    return delegate.getGlobalTransactionId();
  }

  public Xid getDelegate() {
    return delegate;
  }
  
  // 
  // Object
  //
  
  public boolean equals(Object o) { //todo return this branchqual type check
  //  if (!(o instanceof BranchXidImpl)) return false;
  //  BranchXidImpl that = (BranchXidImpl) o;
    if (!(o instanceof Xid)) return false;
    Xid that = (Xid) o;
        final boolean formatId = getFormatId() == that.getFormatId();
        final boolean txid = Arrays.equals(getGlobalTransactionId(), that.getGlobalTransactionId());
        final boolean bqual = Arrays.equals(getBranchQualifier(), that.getBranchQualifier());
    return formatId
        && txid
        && bqual;
  }
  
  public int hashCode() {
    return delegate.hashCode();
  }
  
  public String toString() {
    return "BranchXidImpl:" + delegate.toString();
  }
  
  //
  // Externalizable
  //
  
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    delegate = (Xid) in.readObject();
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(delegate);
  }

}
