/*
 * $Id: DeclarativeSecurityConfiguration.java,v 1.1 2006-05-03 22:57:45 arungupta Exp $
 */

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

package com.sun.xml.wss.impl.config;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import com.sun.xml.wss.impl.configuration.*;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;

/**
 * Represents an XWS-Security configuration object, corresponding to the
 * <code>xwss:SecurityConfiguration</code> element (as defined in XWS-Security,
 * configuration schema, xwssconfig.xsd).
 */
public class DeclarativeSecurityConfiguration implements SecurityPolicy {
    
    
    private MessagePolicy senderSettings   = new MessagePolicy();
    private MessagePolicy receiverSettings = new MessagePolicy();
    
    
    
    
    /*
     *@param doDumpMessages set it to true to enable dumping of messages
     */
    public void setDumpMessages(boolean doDumpMessages) {
        senderSettings.dumpMessages(doDumpMessages);
        receiverSettings.dumpMessages(doDumpMessages);
    }
    
    /*
     *@param flag set it to true to enable DynamicPolicyCallbacks for sender side Policies
     */
    public void enableDynamicPolicy(boolean flag) {
        senderSettings.enableDynamicPolicy(flag);
        receiverSettings.enableDynamicPolicy(flag);
    }
    
    /**
     *@return the <code>MessagePolicy</code> applicable for outgoing requests.
     */
    public MessagePolicy senderSettings() {
        return senderSettings;
    }
    
    /**
     *@return the <code>MessagePolicy</code> applicable for incoming requests.
     */
    public MessagePolicy receiverSettings() {
        return receiverSettings;
    }
    
    /*
     *@param bspFlag set it to true of the BSP conformance flag was specified in the configuration
     */
    public void isBSP(boolean bspFlag) {
        //senderSettings.isBSP(bspFlag);
        receiverSettings.isBSP(bspFlag);
    }
    
    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.DECL_SEC_CONFIG_TYPE;
    }
}
