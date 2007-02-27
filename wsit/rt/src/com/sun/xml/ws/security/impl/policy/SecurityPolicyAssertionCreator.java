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

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.policy.spi.PolicyAssertionCreator;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import static com.sun.xml.ws.security.impl.policy.Constants.logger;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class SecurityPolicyAssertionCreator implements PolicyAssertionCreator{
    
    
    private static HashSet<String> implementedAssertions = new HashSet<String>();
    private static final String [] nsSupportedList = new String[]{Constants.SECURITY_POLICY_NS};
    //    Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS,
    //    Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS,
    //    Constants.SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS,Constants.SUN_SECURE_SERVER_CONVERSATION_POLICY_NS
    //    ,Constants.SUN_TRUST_CLIENT_SECURITY_POLICY_NS,Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS};
    static{
        implementedAssertions.add(Constants.AlgorithmSuite);
        implementedAssertions.add(Constants.AsymmetricBinding);
        implementedAssertions.add(Constants.Address);
        implementedAssertions.add(Constants.EncryptedElements);
        implementedAssertions.add(Constants.EncryptedParts);
        implementedAssertions.add(Constants.EncryptionToken);
        implementedAssertions.add(Constants.EndorsingSupportingTokens);
        implementedAssertions.add(Constants.EndpointReference);
        
        implementedAssertions.add(Constants.HEADER);
        implementedAssertions.add(Constants.HttpsToken);
        implementedAssertions.add(Constants.IssuedToken);
        implementedAssertions.add(Constants.Issuer);
        implementedAssertions.add(Constants.InitiatorToken);
        
        implementedAssertions.add(Constants.KerberosToken);
        
        implementedAssertions.add(Constants.Lifetime);
        implementedAssertions.add(Constants.Layout);
        
        implementedAssertions.add(Constants.ProtectionToken);
        
        implementedAssertions.add(Constants.RecipientToken);
        implementedAssertions.add(Constants.RelToken);
        implementedAssertions.add(Constants.RequestSecurityTokenTemplate);
        implementedAssertions.add(Constants.RequiredElements);
        
        implementedAssertions.add(Constants.SamlToken);
        implementedAssertions.add(Constants.SecurityContextToken);
        implementedAssertions.add(Constants.SecureConversationToken);
        implementedAssertions.add(Constants.SignedElements);
        implementedAssertions.add(Constants.SignedSupportingTokens);
        implementedAssertions.add(Constants.SignedEndorsingSupportingTokens);
        implementedAssertions.add(Constants.SignedParts);
        implementedAssertions.add(Constants.SpnegoContextToken);
        implementedAssertions.add(Constants.SupportingTokens);
        implementedAssertions.add(Constants.SignatureToken);
        implementedAssertions.add(Constants.SymmetricBinding);
        
        implementedAssertions.add(Constants.TransportBinding);
        implementedAssertions.add(Constants.TransportToken);
        implementedAssertions.add(Constants.Trust10);
        
        implementedAssertions.add(Constants.UsernameToken);
        implementedAssertions.add(Constants.UseKey);
        
        implementedAssertions.add(Constants.Wss10);
        implementedAssertions.add(Constants.Wss11);
        implementedAssertions.add(Constants.X509Token);
        implementedAssertions.add(Constants.KeyStore);
        implementedAssertions.add(Constants.TrustStore);
        implementedAssertions.add(Constants.CallbackHandler);
        implementedAssertions.add(Constants.CallbackHandlerConfiguration);
        implementedAssertions.add(Constants.Validator);
        implementedAssertions.add(Constants.ValidatorConfiguration);
        
        
    };
    /** Creates a new instance of SecurityPolicyAssertionCreator */
    
    public SecurityPolicyAssertionCreator() {
        
    }
    
    
    public String[] getSupportedDomainNamespaceURIs() {
        return nsSupportedList;
    }
    protected Class getClass(AssertionData assertionData) throws AssertionCreationException{
        String className ="";
        try {
            className = assertionData.getName().getLocalPart();
            return Class.forName("com.sun.xml.ws.security.impl.policy." + className);
        } catch (ClassNotFoundException ex) {
            if(logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE,"Error occurred while location SecurityPolicy assertion creator class "+"com.sun.xml.ws.security.impl.policy." +className,ex);
            }
            throw new AssertionCreationException(assertionData,ex);
        }
    }
    
    public PolicyAssertion createAssertion(AssertionData assertionData, Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative,PolicyAssertionCreator policyAssertionCreator) throws AssertionCreationException {
        String localName = assertionData.getName().getLocalPart();
        if(implementedAssertions.contains(localName)){
            Class cl=null;
            cl = getClass(assertionData);
            //            try {
            Constructor cons = null;
            try {
                
                cons = getConstructor(cl);
                
                //cl.getConstructor(javax.xml.stream.events.StartElement.class);
            } catch (NoSuchMethodException ex) {
                if(logger.isLoggable(Level.FINE)){
                    logger.log(Level.FINE,"Error occurred while obtaining the constructor for SecurityPolicy assertion"+assertionData.getName());
                }
                throw new AssertionCreationException(assertionData,ex);
            }catch (SecurityException ex) {
                if(logger.isLoggable(Level.FINE)){
                    logger.log(Level.FINE,"Error occurred while obtaining the constructor for SecurityPolicy assertion"+assertionData.getName());
                }
                throw new AssertionCreationException(assertionData,ex);
            }
            if(cons != null){
                try {
                    return (PolicyAssertion)cons.newInstance(assertionData,nestedAssertions,nestedAlternative);
                } catch (IllegalArgumentException ex) {
                    if(logger.isLoggable(Level.FINE)){
                        logger.log(Level.FINE,"Error occurred while instantiating  SecurityPolicy assertion"+assertionData.getName());
                    }
                    
                    throw new AssertionCreationException(assertionData,ex);
                } catch (InvocationTargetException ex) {
                    if(logger.isLoggable(Level.FINE)){
                        logger.log(Level.FINE,"Error occurred while instantiating  SecurityPolicy assertion"+assertionData.getName());
                    }
                    throw new AssertionCreationException(assertionData,ex);
                } catch (InstantiationException ex) {
                    if(logger.isLoggable(Level.FINE)){
                        logger.log(Level.FINE,"Error occurred while instantiating  SecurityPolicy assertion"+assertionData.getName());
                    }
                    throw new AssertionCreationException(assertionData,ex);
                } catch (IllegalAccessException ex) {
                    if(logger.isLoggable(Level.FINE)){
                        logger.log(Level.FINE,"Error occurred while instantiating  SecurityPolicy assertion"+assertionData.getName());
                    }
                    throw new AssertionCreationException(assertionData,ex);
                }
            }else{
                try{
                    return (PolicyAssertion)cl.newInstance();
                } catch (InstantiationException ex) {
                    if(logger.isLoggable(Level.FINE)){
                        logger.log(Level.FINE,"Error occurred while instantiating  SecurityPolicy assertion"+assertionData.getName());
                    }
                    throw new AssertionCreationException(assertionData,ex);
                } catch (IllegalAccessException ex) {
                    if(logger.isLoggable(Level.FINE)){
                        logger.log(Level.FINE,"Error occurred while instantiating  SecurityPolicy assertion"+assertionData.getName());
                    }
                    throw new AssertionCreationException(assertionData,ex);
                }
            }
            
            
        }
        return policyAssertionCreator.createAssertion(assertionData,nestedAssertions,nestedAlternative,policyAssertionCreator);
        
    }
    
    private Constructor getConstructor(Class cl) throws NoSuchMethodException{
        Constructor [] cList = cl.getConstructors();
        return cl.getConstructor(com.sun.xml.ws.policy.sourcemodel.AssertionData.class,java.util.Collection.class,com.sun.xml.ws.policy.AssertionSet.class);
    }
    
    
}
