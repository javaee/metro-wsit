/*
 * $Id: DeclarativeSecurityConfiguration.java,v 1.3.2.2 2010-07-14 14:06:24 m_potociar Exp $
 */

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


package com.sun.xml.wss.impl.config;

import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.PolicyTypeUtil;
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

    private boolean retainSecHeader = false;
    private boolean resetMU = false;
    
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
        //enabling this to allow Backward Compatibility with XWSS11
        //Currently XWSS11 with its old xmlsec cannot handle prefixList in
        // Signature CanonicalizationMethod
        senderSettings.isBSP(bspFlag);
        receiverSettings.isBSP(bspFlag);
    }
    
    /*
     *@return the Retain Security Header Config Property
     */
    public boolean retainSecurityHeader() {
        return retainSecHeader;
    }
    
    /*
     *@param arg, set the retainSecurityHeader flag. 
     */
    public void retainSecurityHeader(boolean arg) {
        this.retainSecHeader = arg;
    }
    
    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.DECL_SEC_CONFIG_TYPE;
    }

    public void resetMustUnderstand(boolean value) {
        this.resetMU = value;
    }
    
    public boolean resetMustUnderstand() {
        return this.resetMU; 
    }
}