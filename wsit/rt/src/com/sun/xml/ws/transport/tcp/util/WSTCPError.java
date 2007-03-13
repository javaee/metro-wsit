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

package com.sun.xml.ws.transport.tcp.util;

/**
 * @author Alexey Stashok
 */
public class WSTCPError {
    private final int code;
    private final int subCode;
    private final String description;
    
    public static WSTCPError createCriticalError(final int subCode, final String description) {
        return new WSTCPError(TCPConstants.CRITICAL_ERROR, subCode, description);
    }
    
    public static WSTCPError createNonCriticalError(final int subCode, final String description) {
        return new WSTCPError(TCPConstants.NON_CRITICAL_ERROR, subCode, description);
    }

    public static WSTCPError createError(final int code, final int subCode, final String description) {
        return new WSTCPError(code, subCode, description);
    }

    private WSTCPError(final int code, final int subCode, final String description) {
        this.code = code;
        this.subCode = subCode;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public int getSubCode() {
        return subCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCritical() {
        return code == TCPConstants.CRITICAL_ERROR;
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer(100);
        sb.append("Code: ");
        sb.append(code);
        sb.append(" SubCode: ");
        sb.append(subCode);
        sb.append(" Description: ");
        sb.append(description);
        return sb.toString();
    }
}
