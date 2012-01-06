/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.policy.spi.PolicyAssertionCreator;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import static com.sun.xml.ws.security.impl.policy.Constants.logger;
import static com.sun.xml.ws.security.impl.policy.Constants.SECURITY_POLICY_PACKAGE_DIR;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class SecurityPolicyAssertionCreator implements PolicyAssertionCreator{
    
    
    private static HashSet<String> implementedAssertions = new HashSet<String>();
    private static final String [] nsSupportedList = new String[]{SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri,
           SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri,
           "http://schemas.microsoft.com/ws/2005/07/securitypolicy",
           SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri};
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
        implementedAssertions.add(Constants.InitiatorSignatureToken);
        implementedAssertions.add(Constants.InitiatorEncryptionToken);
        
        implementedAssertions.add(Constants.KerberosToken);
        
        implementedAssertions.add(Constants.Lifetime);
        implementedAssertions.add(Constants.Layout);
        
        implementedAssertions.add(Constants.ProtectionToken);
        
        implementedAssertions.add(Constants.RecipientToken);
        implementedAssertions.add(Constants.RecipientSignatureToken);
        implementedAssertions.add(Constants.RecipientEncryptionToken);
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
        implementedAssertions.add(Constants.SessionManagerStore);
        implementedAssertions.add(Constants.TrustStore);
        implementedAssertions.add(Constants.CallbackHandler);
        implementedAssertions.add(Constants.CallbackHandlerConfiguration);
        implementedAssertions.add(Constants.Validator);
        implementedAssertions.add(Constants.ValidatorConfiguration);
        implementedAssertions.add(Constants.CertStore);
        implementedAssertions.add(Constants.KerberosConfig);
        implementedAssertions.add(Constants.RsaToken);
        
        // WS-SecurityPolicy 1.2 assertions
        implementedAssertions.add(Constants.KeyValueToken);
        implementedAssertions.add(Constants.EncryptedSupportingTokens);
        implementedAssertions.add(Constants.SignedEncryptedSupportingTokens);
        implementedAssertions.add(Constants.SignedEndorsingEncryptedSupportingTokens);
        implementedAssertions.add(Constants.EndorsingEncryptedSupportingTokens);
        implementedAssertions.add(Constants.Trust13);
        implementedAssertions.add(Constants.IssuerName);
        implementedAssertions.add(Constants.Claims);
        
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
            //made mistake here, we are now in SCF and cannot change the classname
            //will make a change for WSIT 1.1
            if (Constants.CertStore.equals(className)) {
                className = "CertStoreConfig";
            }
            return Class.forName("com.sun.xml.ws.security.impl.policy." + className);
        } catch (ClassNotFoundException ex) {
            if(logger.isLoggable(Level.SEVERE)){
                logger.log(Level.SEVERE,LogStringsMessages.SP_0110_ERROR_LOCATING_CLASS(SECURITY_POLICY_PACKAGE_DIR +className),ex);
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
                if(logger.isLoggable(Level.SEVERE)){
                    logger.log(Level.SEVERE,LogStringsMessages.SP_0111_ERROR_OBTAINING_CONSTRUCTOR(assertionData.getName()),ex);
                }
                throw new AssertionCreationException(assertionData,ex);
            }catch (SecurityException ex) {
                if(logger.isLoggable(Level.SEVERE)){
                    logger.log(Level.SEVERE,LogStringsMessages.SP_0111_ERROR_OBTAINING_CONSTRUCTOR(assertionData.getName()),ex);
                }
                
                
                throw new AssertionCreationException(assertionData,ex);
            }
            if(cons != null){
                try {
                    return (PolicyAssertion)cons.newInstance(assertionData,nestedAssertions,nestedAlternative);
                } catch (IllegalArgumentException ex) {
                    if(logger.isLoggable(Level.SEVERE)){
                        logger.log(Level.SEVERE,LogStringsMessages.SP_0112_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    
                    throw new AssertionCreationException(assertionData,ex);
                } catch (InvocationTargetException ex) {
                    if(logger.isLoggable(Level.SEVERE)){
                        logger.log(Level.SEVERE,LogStringsMessages.SP_0112_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    throw new AssertionCreationException(assertionData,ex);
                } catch (InstantiationException ex) {
                    if(logger.isLoggable(Level.SEVERE)){
                        logger.log(Level.SEVERE,LogStringsMessages.SP_0112_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    throw new AssertionCreationException(assertionData,ex);
                } catch (IllegalAccessException ex) {
                    if(logger.isLoggable(Level.SEVERE)){
                        logger.log(Level.SEVERE,LogStringsMessages.SP_0112_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    throw new AssertionCreationException(assertionData,ex);
                }
            }else{
                try{
                    return (PolicyAssertion)cl.newInstance();
                } catch (InstantiationException ex) {
                    if(logger.isLoggable(Level.SEVERE)){
                        logger.log(Level.SEVERE,LogStringsMessages.SP_0112_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    throw new AssertionCreationException(assertionData,ex);
                } catch (IllegalAccessException ex) {
                    if(logger.isLoggable(Level.SEVERE)){
                        logger.log(Level.SEVERE,LogStringsMessages.SP_0112_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    throw new AssertionCreationException(assertionData,ex);
                }
            }
            
            
        }
        return policyAssertionCreator.createAssertion(assertionData,nestedAssertions,nestedAlternative,policyAssertionCreator);
        
    }
    @SuppressWarnings("unchecked")
    private Constructor getConstructor(Class cl) throws NoSuchMethodException{       
        return cl.getConstructor(com.sun.xml.ws.policy.sourcemodel.AssertionData.class,java.util.Collection.class,com.sun.xml.ws.policy.AssertionSet.class);
    }  
    
}
