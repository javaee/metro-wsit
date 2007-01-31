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
package com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public enum InvocationProcessingState {
    START_BUFFERING((byte) 0x01),
    RESTART_BUFFERING((byte) 0x01), // releases old buffer and starts new
    STOP_BUFFERING((byte) 0x04),
    START_FILTERING((byte) 0x08),
    STOP_FILTERING((byte) 0x10),
    NO_STATE_CHANGE((byte) 0x00);
    
    private final byte flag;
    
    /**
     * Method combines this state with another state and returns the state that results from combination of these states;
     */
    private InvocationProcessingState(byte flag) {
        this.flag = flag;
    }
    
     public boolean isSet(final byte bitArray) {
         return this.flag == 0x00 || (bitArray & this.flag) > 0;
     }
     
     public byte setFlag(byte bitArray) {
         return (byte) (bitArray | this.flag);
     }
}
