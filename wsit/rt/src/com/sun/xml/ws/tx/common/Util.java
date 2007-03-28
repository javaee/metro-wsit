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
package com.sun.xml.ws.tx.common;

/**
 * This class contains various utility methods shared among
 * other modules.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.6 $
 * @since 1.0
 */
public class Util {

    final static private TxLogger logger = TxLogger.getLogger(Util.class);
    
    static public boolean isClassAvailable(String classname) {
        Class tmClass = null;
        try {
            tmClass = Class.forName(classname);
        } catch (ClassNotFoundException ex) {
        }
        return tmClass != null;
    }
   
    private static Boolean hasJTA = null;
    
    /**
     * Identify if Java Transaction API available. 
     * If not available, container is not capable of creating a JTA transaction.
     */
    static public boolean isJTAAvailable() {
        if (hasJTA == null) {
            hasJTA = isClassAvailable("javax.transaction.TransactionManager") ? Boolean.TRUE : Boolean.FALSE;
            if (hasJTA && ! TransactionManagerImpl.getInstance().isTransactionManagerAvailable()) {
                hasJTA = Boolean.FALSE;
            }
        }
        return hasJTA.booleanValue();
    }
}
