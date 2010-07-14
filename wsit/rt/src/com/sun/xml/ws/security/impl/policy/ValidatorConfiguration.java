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
package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Iterator;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.Collection;
import javax.xml.namespace.QName;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class ValidatorConfiguration extends PolicyAssertion implements com.sun.xml.ws.security.policy.ValidatorConfiguration, SecurityAssertionValidator{
    
    
    private boolean populated = false;
    private Iterator<PolicyAssertion> ast  = null;
    private static QName cmaxClockSkew =  new QName(Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS,"maxClockSkew");
    private static QName smaxClockSkew =  new QName(Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,"maxClockSkew");
    private static QName ctimestampFreshnessLimit  =  new QName(Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS,"timestampFreshnessLimit");
    private static QName stimestampFreshnessLimit  =  new QName(Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,"timestampFreshnessLimit"); 
    private static QName smaxNonceAge =  new QName(Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,"maxNonceAge");
    private static QName crevocationEnabled =  new QName(Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS,"revocationEnabled");
    private static QName srevocationEnabled =  new QName(Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,"revocationEnabled");
    private static QName cenforceKeyUsage=  new QName(Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS,"enforceKeyUsage");
    private static QName senforceKeyUsage =  new QName(Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,"enforceKeyUsage");
    
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    /** Creates a new instance of ValidatorConfiguration */
    public ValidatorConfiguration() {
    }
    
    public ValidatorConfiguration(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public Iterator<? extends PolicyAssertion> getValidators() {
        populate();
        return ast;
    }
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {        
        if(!populated){
            this.ast  = this.getNestedAssertionsIterator();
            populated  = true;
        }
        return fitness;        
    }
    
    public String getMaxClockSkew() {       
        if(this.getAttributes().containsKey(cmaxClockSkew)){
            return this.getAttributeValue(cmaxClockSkew);
        }else if(this.getAttributes().containsKey(smaxClockSkew)){
            return this.getAttributeValue(smaxClockSkew);
        }
        return null;
    }
    
    public String getTimestampFreshnessLimit() {
         if(this.getAttributes().containsKey(ctimestampFreshnessLimit)){
            return this.getAttributeValue(ctimestampFreshnessLimit);
        }else if(this.getAttributes().containsKey(stimestampFreshnessLimit)){
            return this.getAttributeValue(stimestampFreshnessLimit);
        }
        return null;        
    }
    
    public String getMaxNonceAge() {
        if(this.getAttributes().containsKey(smaxNonceAge)){
            return this.getAttributeValue(smaxNonceAge);
        }
        return null;            
    }

    public String getRevocationEnabled() {
        if(this.getAttributes().containsKey(crevocationEnabled)){
            return this.getAttributeValue(crevocationEnabled);
        }else if(this.getAttributes().containsKey(srevocationEnabled)){
            return this.getAttributeValue(srevocationEnabled);
        }
        return null;
    }
    
    public String getEnforceKeyUsage() {
        if(this.getAttributes().containsKey(cenforceKeyUsage)){
            return this.getAttributeValue(cenforceKeyUsage);
        }else if(this.getAttributes().containsKey(senforceKeyUsage)){
            return this.getAttributeValue(senforceKeyUsage);
        }
        return null;
    }
}
