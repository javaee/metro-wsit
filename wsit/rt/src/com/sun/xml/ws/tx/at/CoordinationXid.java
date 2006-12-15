/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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

    public static Xid lookupOrCreate(String coordId) {
        Xid result = get(coordId);
        if (result == null) {
            result = new CoordinationXid(coordId);
            coordId2Xid.put(coordId, result);
            xid2CoordId.put(result, coordId);
        }
        return result;
    }

    public static Xid get(String coordId) {
        return coordId2Xid.get(coordId);
    }

    private static Xid remove(String coordId) {
        return coordId2Xid.remove(coordId);
    }

    private static String remove(Xid coordId) {
        return xid2CoordId.remove(coordId);
    }

    /**
     * Cleanup.
     */
    public static void forget(String coordId) {
        Xid removed = remove(coordId);
        if (removed != null) {
            remove(removed);
        }
    }

    static private Random random = new Random();

    /**
     * Creates a new instance of CoordinationXid representing an imported
     * coordination id.
     */
    private CoordinationXid(String coordinationXid) {
        imported = true;
        
        gtrId = new byte[16];
        random.nextBytes(gtrId);
    }

    public byte[] getGlobalTransactionId() {
        return gtrId;
    }

    public byte[] getBranchQualifier() {
        return branchQualifier;
    }

    private static final int formatId = 200408; // any constant but -1 that is invalid format
    private byte[] gtrId;
    static private byte[] branchQualifier = {1}; // no nested txn
    private String stringForm = null; //cache
    private boolean imported = true;

    public int getFormatId() {
        return formatId;
    }

    /*
    * returns the Transaction id of this transaction
    */
    public String toString() {
        // return cache if it exists
        if (stringForm != null) return stringForm;

        // Otherwise format the global identifier.
        //char[] buff = new char[gtrId.length*2 + 2/*'[' and ']'*/ + 3/*bqual and ':'*/];
        char[] buff = new char[gtrId.length * 2 + 3/*bqual and ':'*/];
        int pos = 0;
        //buff[pos++] = '[';

        // Convert the global transaction identifier into a string of hex digits.

        int globalLen = gtrId.length;
        for (int i = 0; i < globalLen; i++) {
            int currCharHigh = (gtrId[i] & 0xf0) >> 4;
            int currCharLow = gtrId[i] & 0x0f;
            buff[pos++] = (char) (currCharHigh + (currCharHigh > 9 ? 'A' - 10 : '0'));
            buff[pos++] = (char) (currCharLow + (currCharLow > 9 ? 'A' - 10 : '0'));
        }

        //buff[pos++] = ':';
        buff[pos++] = '_';
        int currCharHigh = (0 & 0xf0) >> 4;
        int currCharLow = 0 & 0x0f;
        buff[pos++] = (char) (currCharHigh + (currCharHigh > 9 ? 'A' - 10 : '0'));
        buff[pos++] = (char) (currCharLow + (currCharLow > 9 ? 'A' - 10 : '0'));
        //buff[pos] = ']';

        // Cache the string form of the global identifier.
        stringForm = new String(buff);
        return stringForm;
    }

}
