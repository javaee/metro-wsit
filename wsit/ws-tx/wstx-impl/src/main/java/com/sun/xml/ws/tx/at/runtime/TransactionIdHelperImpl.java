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

package com.sun.xml.ws.tx.at.runtime;

import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.tx.at.internal.XidImpl;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import javax.transaction.xa.Xid;

/**
 *
 * @author paulparkinson
 */
class TransactionIdHelperImpl extends TransactionIdHelper {
  private static final int FFID = 0xFF1D;

  // private final DebugLogger debugWSAT = DebugLogger.getDebugLogger("DebugWSAT");

  private Map<String, Xid> tids2xids;
  private Map<Xid, String> xids2tids;

  public TransactionIdHelperImpl() throws NoSuchAlgorithmException {
    tids2xids = new HashMap<String, Xid>();
    xids2tids = new HashMap<Xid, String>();
  }

  public String xid2wsatid(Xid xid) {
      return xidToString(xid, true);
  // return xid.toString();
  }

  //XAResourceHelper.xidToString(Xid xid, true)
  static String xidToString(Xid xid, boolean includeBranchQualifier) {
    if (xid == null) return "";
    StringBuffer sb = new StringBuffer()
      .append(Integer.toHexString(xid.getFormatId()).toUpperCase(Locale.ENGLISH)).append("-")
      .append(byteArrayToString(xid.getGlobalTransactionId()));
    if (includeBranchQualifier) {
      String bqual = byteArrayToString(xid.getBranchQualifier());
      if (!bqual.equals("")) {
        sb.append("-").append(byteArrayToString(xid.getBranchQualifier()));
      }
    }
    return sb.toString();
  }

  private static final char DIGITS[]   = {
    '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

  private static String byteArrayToString(byte[] barray) {
    if (barray == null) return "";
    char[] res = new char[barray.length * 2]; // Two chars per byte
    int j = 0;
    for( int i = 0; i < barray.length; i++) {
        res[j++] = DIGITS[(barray[i] & 0xF0) >>> 4];
        res[j++] = DIGITS[barray[i] & 0x0F];
    }
    return new String(res);

  }

  public Xid wsatid2xid(String wsatid) {
    return create(wsatid);
  }


  public static XidImpl create(String xid) {
    StringTokenizer tok = new StringTokenizer(xid, "-");
    if (tok.countTokens() < 2) return null;

    String formatIdString = tok.nextToken();
    String gtridString = tok.nextToken();
    String bqualString = null;
    if (tok.hasMoreElements()) {
      bqualString = tok.nextToken();
    }
    return new XidImpl(Integer.parseInt(formatIdString, 16),
                       stringToByteArray(gtridString),
                       (bqualString != null) ? stringToByteArray(bqualString) : new byte[]{});
  }


  static private byte[] stringToByteArray(String str) {
    if (str == null) return new byte[0];
    byte[] bytes = new byte[str.length()/2];
    for (int i = 0, j = 0; i < str.length(); i++, j++) {
      bytes[j] = (byte) ((Byte.parseByte(str.substring(i,++i), 16) << 4) |
                         Byte.parseByte(str.substring(i,i+1), 16));
    }
    return bytes;
  }

  public synchronized Xid getOrCreateXid(byte[] tid) {
    Xid xid = getXid(tid);
    if (xid != null) return xid;
    byte[] gtrid = WSATHelper.assignUUID().getBytes();
    // xid = XIDFactory.createXID(FFID, gtrid, null);

    xid = new XidImpl(FFID, gtrid, null);
    String stid = new String(tid);
    tids2xids.put(stid, xid);
    xids2tids.put(xid, stid);
  //  if (debugWSAT.isDebugEnabled()) {
  //    debugWSAT.debug("created mapping foreign Transaction Id " + stid + " to local Xid " + xid);
  //  }

    return xid;
  }


  public synchronized byte[] getTid(Xid xid) {
    String stid = xids2tids.get(xid);
    if (stid == null) return null;
    return stid.getBytes();
  }

  public synchronized Xid getXid(byte[] tid) {
    return tids2xids.get(new String(tid));
  }

  public synchronized Xid remove(byte[] tid) {
    if (getXid(tid) == null)
      return null;
    Xid xid = tids2xids.remove(tid);
    xids2tids.remove(xid);
    return xid;
  }

  public synchronized byte[] remove(Xid xid) {
    if (getTid(xid) == null)
      return null;
    String stid = xids2tids.remove(xid);
    tids2xids.remove(stid);
    return stid.getBytes();
  }

  public String toString() {
    return tids2xids.toString();
  }

}
