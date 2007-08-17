/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * SequenceSettings.java
 *
 *
 * @author Mike Grogan
 * Created on January 19, 2007, 8:59 AM
 *
 */

package com.sun.xml.ws.api.rm;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.rm.RMConstants;
import com.sun.xml.ws.rm.RMVersion;

import java.io.Serializable;

/**
 * Initialization data for a sequence, which can be persisted
 * and used to reinitialize a sequence.
 */
public class SequenceSettings implements Serializable {
    
    public SequenceSettings() {
    }
    
    /**
     * AcksTo URI for the sequence
     */
    public String acksTo;

    /**
     * For OutboundSequences, determines whether destination guarantees ordered delivery.
     */
    public boolean ordered;

    /**
     * Number of milliseconds after which destination may terminate sequence.
     */
    public long inactivityTimeout;


    /**
     * Indicates whether flow control is enabled.
     */
    public boolean flowControl;
    
    /**
     * Number of messages that destination will buffer pending delivery.
     */
    public int bufferSize;

    /**
     * The SOAPVersion which will be passed on to the protocol elements
     * populated from the Pipe
     */
    public SOAPVersion soapVersion;
    
    
    /**
     * Length of time between resends
     */
    public long resendInterval;
    
    /**
     * Length of time between ackRequests.
     */
    public long ackRequestInterval;
    
    
    /**
     * Lenth of time that RMClientPipe.preDestroy will block while
     * waiting for unacknowledged messages to arrive.
     */
    public long closeTimeout;


    /**
     * Do we suppress duplicates at the endpoint?
     */
    public boolean allowDuplicates;
    
    /**
     * RMConstants enum value using correct addressing version.
     */
    public RMConstants constants ;

    /**
     * SequenceId for the sequence.  This field is not assumed to be populated
     * in the (@link SequenceConfig} subclass.
     */
    public String sequenceId;
    
    
     /**
     * SequenceId for the companion sequence, if any.  This field is not assumed 
     * to be populated in the (@link SequenceConfig} subclass.
     */
    public String companionSequenceId;


    /**
     * The RM version if it is WSRM 1.0 or WSRM 1.1
     */
    public RMVersion rmVersion;
    
    /**
     * SequenceSTR setting from Policy.
     */
    public boolean sequenceSTR;
    
    /**
     * SequenceTransportSecurity setting from Policy.
     */
    public boolean sequenceTransportSecurity;
}
