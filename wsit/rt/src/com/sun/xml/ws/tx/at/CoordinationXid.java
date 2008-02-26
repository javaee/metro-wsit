/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.tx.at;

import java.util.Random;
import javax.transaction.xa.Xid;
import java.util.HashMap;
import java.util.Map;

/**
 * Manage mapping between WS-Atomic Transaction coordination identifier and Xid.
 * <p/>
 * Create Xid for imported txn.  (one flowed in from different vendor)
 * TODO: if root coordinator, try reusing AS txn id (have to export it though)
 * <p/>
 * <p/>
 * Allowing setting of
 *
 * @author jf39279
 */
public class CoordinationXid implements Xid {

    private static Map<String, Xid> coordId2Xid = new HashMap<String, Xid>();
    private static Map<Xid, String> xid2CoordId = new HashMap<Xid, String>();
    final private String coordId;

    public static Xid lookupOrCreate(final String coordId) {
        Xid result = get(coordId);
        if (result == null) {
            result = new CoordinationXid(coordId);
            coordId2Xid.put(coordId, result);
            xid2CoordId.put(result, coordId);
        }
        return result;
    }
    
    public String getCoordinationId() {
        return coordId;
    }

    public static Xid get(final String coordId) {
        return coordId2Xid.get(coordId);
    }

    private static Xid remove(final String coordId) {
        return coordId2Xid.remove(coordId);
    }

    private static String remove(final Xid coordId) {
        return xid2CoordId.remove(coordId);
    }

    /**
     * Cleanup.
     */
    public static void forget(final String coordId) {
        final Xid removed = remove(coordId);
        if (removed != null) {
            remove(removed);
        }
    }

    static private Random random = new Random();

    /**
     * Creates a new instance of CoordinationXid representing an imported
     * coordination id.
     */
    private CoordinationXid(final String coordinationXid) {
        gtrId = new byte[16];
        random.nextBytes(gtrId);
        coordId = coordinationXid;
    }

    public byte[] getGlobalTransactionId() {
        return gtrId.clone();
    }

    public byte[] getBranchQualifier() {
        return BRANCH_QUALIFIER;
    }

    private static final int FORMAT_ID = 200408; // any constant but -1 that is invalid format
    final private byte[] gtrId;
    static private final byte[] BRANCH_QUALIFIER = {1}; // no nested txn
    private String stringForm = null; //cache

    public int getFormatId() {
        return FORMAT_ID;
    }

    /*
    * returns the Transaction id of this transaction
    */
    public String toString() {
        // return cache if it exists
        if (stringForm != null){
            return stringForm;
        }

        // Otherwise format the global identifier.
        //char[] buff = new char[gtrId.length*2 + 2/*'[' and ']'*/ + 3/*bqual and ':'*/];
        char[] buff = new char[gtrId.length * 2 + 3/*bqual and ':'*/];
        int pos = 0;
        //buff[pos++] = '[';

        // Convert the global transaction identifier into a string of hex digits.

        final int globalLen = gtrId.length;
        for (int i = 0; i < globalLen; i++) {
            final int currCharHigh = (gtrId[i] & 0xf0) >> 4;
            final int currCharLow = gtrId[i] & 0x0f;
            buff[pos++] = (char) (currCharHigh + (currCharHigh > 9 ? 'A' - 10 : '0'));
            buff[pos++] = (char) (currCharLow + (currCharLow > 9 ? 'A' - 10 : '0'));
        }

        //buff[pos++] = ':';
        buff[pos++] = '_';
        final int currCharHigh = (0 & 0xf0) >> 4;
        final int currCharLow = 0 & 0x0f;
        buff[pos++] = (char) (currCharHigh + (currCharHigh > 9 ? 'A' - 10 : '0'));
        buff[pos++] = (char) (currCharLow + (currCharLow > 9 ? 'A' - 10 : '0'));
        //buff[pos] = ']';

        // Cache the string form of the global identifier.
        stringForm = new String(buff);
        return stringForm;
    }

}
