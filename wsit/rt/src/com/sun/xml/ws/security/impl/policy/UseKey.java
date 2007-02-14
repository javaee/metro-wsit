/* 
 *  The contents of this file are subject to the terms 
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

/*
 * UseKey.java 
 *
 * Created on February 22, 2006, 6:37 PM 
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.impl.policy;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;

/** 
 * 
 * @author Abhijit Das
 */

public class UseKey extends PolicyAssertion implements com.sun.xml.ws.security.policy.UseKey, SecurityAssertionValidator {    
    
    private static QName sig = new QName("sig"); 
    private URI signatureID;  
    private boolean populated = false;    
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    
    /** Creates a new instance of UseKeyIMpl */ 
    
    public UseKey(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) { 
        super(name,nestedAssertions,nestedAlternative);    
    }  
    
    public AssertionFitness validate(boolean isServer) {    
        return populate(isServer);  
    }  
    
    private void populate(){ 
        populate(false);  
    }    
    
    private synchronized AssertionFitness populate(boolean isServer) { 
        if(!populated){   
            try {        
                this.signatureID = new URI(this.getAttributeValue(sig));       
            } catch (URISyntaxException ex) { 
                logger.log(Level.SEVERE,LogStringsMessages.SP_0102_INVALID_URI_VALUE(this.getAttributeValue(sig)),ex);
                fitness = AssertionFitness.HAS_INVALID_VALUE;        
            }   
            populated = true;   
        }      
        return fitness;  
    }
}
