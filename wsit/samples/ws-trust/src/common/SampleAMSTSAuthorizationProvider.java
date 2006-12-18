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
package common;

import com.sun.xml.wss.SubjectAccessor;

import java.util.*;

import javax.security.auth.Subject;

import com.iplanet.am.util.Debug;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOException;

import com.sun.identity.policy.PolicyException;
import com.sun.identity.policy.client.PolicyEvaluator;

import com.sun.xml.ws.api.security.trust.STSAuthorizationProvider;

public class SampleAMSTSAuthorizationProvider implements STSAuthorizationProvider {
    
    private static Debug debug = Debug.getInstance("SampleAMSTSAuthorizationProvider");

    private static SSOToken getSSOToken(Subject subject)
    {
        Set pc = //subject.getPublicCredentials();
       // if (pc == null){
                SubjectAccessor.getRequesterSubject().getPublicCredentials();
        //}
        
        if (pc == null)
            System.out.println("No pc in the subject");
        
        if (pc != null){
            if (pc == null){
                pc = SubjectAccessor.getRequesterSubject().getPublicCredentials();
            }
            Iterator ite = pc.iterator();
            while (ite.hasNext()){
                Object obj = ite.next();
                if (obj instanceof com.iplanet.sso.SSOToken){
                    return (SSOToken)obj;
                }
            }
        }
        return null;
    }
    
    public boolean isAuthorized(Subject subject, String appliesTo, String tokenType, String keyType)
    {
        String serviceName = "iPlanetAMWebAgentService";
        String action = "POST";
        SSOToken token = getSSOToken(subject);

        try
        {
            debug.message("Authorizing access - SSOToken is "+token);
            debug.message("Checking policy for "+action+" on URL "+appliesTo);
            PolicyEvaluator pe = new PolicyEvaluator(serviceName);
            debug.message("Got PolicyEvaluator for "+serviceName);
            boolean isAllowed = pe.isAllowed(token, appliesTo, action);
            debug.message("Access " + (isAllowed ? "is" : "is not" ) + " allowed");
            return isAllowed;
        }
        catch ( PolicyException pe )
        {
            debug.error("Exception evaluating policy", pe);
        }
        catch ( SSOException ssoe )
        {
            debug.error("Exception evaluating policy", ssoe);
        }

        return false;
    }  
}
