/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.wss.jaxws.impl;

import com.sun.xml.ws.api.policy.ModelTranslator;
import com.sun.xml.ws.api.policy.ModelUnmarshaller;
import com.sun.xml.ws.api.policy.SourceModel;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.rx.mc.McVersion;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * TODO: Make this configurable
 * @author K.Venugopal@sun.com
 */
public class RMPolicyResolver {
    
    SecurityPolicyVersion spVersion;
    RmVersion rmVersion;
    McVersion mcVersion;
    boolean encrypt = false;
    
    /** Creates a new instance of RMPolicyResolver */
    public RMPolicyResolver() {
        spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
        rmVersion = RmVersion.WSRM200502;
        mcVersion = McVersion.WSMC200702;
    }
    
    public RMPolicyResolver(SecurityPolicyVersion spVersion, RmVersion rmVersion) {
        this.spVersion = spVersion;
        this.rmVersion = rmVersion;
        mcVersion = McVersion.WSMC200702;
    }

    public RMPolicyResolver(SecurityPolicyVersion spVersion, RmVersion rmVersion, McVersion mcVersion, boolean encrypt) {
        this.spVersion = spVersion;
        this.rmVersion = rmVersion;
        this.mcVersion = mcVersion;
        this.encrypt = encrypt;
    }
    
    public Policy getOperationLevelPolicy() throws PolicyException{
        PolicySourceModel model;
        try {
            String rmMessagePolicy = encrypt ? "rm-msglevel-policy-encrypt.xml" : "rm-msglevel-policy.xml";
            if(SecurityPolicyVersion.SECURITYPOLICY12NS == spVersion && RmVersion.WSRM200502 == rmVersion){
                rmMessagePolicy = "rm-msglevel-policy-sp12.xml";
            }else if(SecurityPolicyVersion.SECURITYPOLICY12NS == spVersion && (RmVersion.WSRM200702  == rmVersion )){
                rmMessagePolicy = encrypt ? "rm-msglevel-policy-sx-encrypt.xml" :"rm-msglevel-policy-sx.xml";
            }else if(SecurityPolicyVersion.SECURITYPOLICY200507 == spVersion && (RmVersion.WSRM200702 == rmVersion )){
                rmMessagePolicy = "rm-msglevel-policy-sx-sp10.xml";
            }
            
            model = unmarshalPolicy("com/sun/xml/ws/security/impl/policyconv/" + rmMessagePolicy);
        }catch (IOException ex) {
            throw new PolicyException(ex);
        }
        Policy mbp = ModelTranslator.getTranslator().translate(model);
        return mbp;
    }
    
    private PolicySourceModel unmarshalPolicy(String resource) throws PolicyException, IOException {
        Reader reader = getResourceReader(resource);
        PolicySourceModel model = ModelUnmarshaller.getUnmarshaller().unmarshalModel(reader);
        reader.close();
        return model;
    }
    
    private Reader getResourceReader(String resourceName) {
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName));
    }    
}
