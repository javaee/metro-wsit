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

package com.sun.xml.ws.addressing.impl.policy;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.policy.spi.PolicyAssertionCreator;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class AddressingPolicyAssertionCreator implements PolicyAssertionCreator {
    
    private static HashSet<String> implementedAssertions = new HashSet<String>();
    private static final String [] NS_SUPPORTED_LIST = new String[] { AddressingVersion.MEMBER.nsUri,
                                                                      AddressingVersion.W3C.nsUri };
    
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(AddressingPolicyAssertionCreator.class);

    static{
        implementedAssertions.add("Address");
        implementedAssertions.add("EndpointReference");
    }
    
    /** Creates a new instance of AddressingPolicyAssertionCreator */
    public AddressingPolicyAssertionCreator() {
    }
    
    
    public String[] getSupportedDomainNamespaceURIs() {
        return NS_SUPPORTED_LIST;
    }
    
    protected Class<?> getClass(final AssertionData assertionData) throws AssertionCreationException {
        LOGGER.entering(assertionData);
        try {
            final String className = assertionData.getName().getLocalPart();
            final Class<?> result = Class.forName("com.sun.xml.ws.addressing.impl.policy." + className);
            LOGGER.exiting();
            return result;
        } catch (ClassNotFoundException ex) {
            LOGGER.warning(LocalizationMessages.WSA_0001_UNKNOWN_ASSERTION(assertionData.toString()), ex);
            throw new AssertionCreationException(assertionData,ex);
        }
    }

    public PolicyAssertion createAssertion(AssertionData assertionData, Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative,PolicyAssertionCreator policyAssertionCreator) throws AssertionCreationException {
        String localName = assertionData.getName().getLocalPart();
        if(implementedAssertions.contains(localName)){
            Class<?> cl=null;
            cl = getClass(assertionData);
            //            try {
            Constructor<?> cons = null;
            try {
                
                cons = getConstructor(cl);
                
                //cl.getConstructor(javax.xml.stream.events.StartElement.class);
            } catch (NoSuchMethodException ex) {
                if(LOGGER.isLoggable(Level.SEVERE)){
                    LOGGER.log(Level.SEVERE,LocalizationMessages.WSA_0002_ERROR_OBTAINING_CONSTRUCTOR(assertionData.getName()),ex);
                }
                throw new AssertionCreationException(assertionData,ex);
            }catch (SecurityException ex) {
                if(LOGGER.isLoggable(Level.SEVERE)){
                    LOGGER.log(Level.SEVERE,LocalizationMessages.WSA_0002_ERROR_OBTAINING_CONSTRUCTOR(assertionData.getName()),ex);
                }
                
                
                throw new AssertionCreationException(assertionData,ex);
            }
            if(cons != null){
                try {
                    return (PolicyAssertion)cons.newInstance(assertionData,nestedAssertions,nestedAlternative);
                } catch (IllegalArgumentException ex) {
                    if(LOGGER.isLoggable(Level.SEVERE)){
                        LOGGER.log(Level.SEVERE,LocalizationMessages.WSA_0003_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    
                    throw new AssertionCreationException(assertionData,ex);
                } catch (InvocationTargetException ex) {
                    if(LOGGER.isLoggable(Level.SEVERE)){
                        LOGGER.log(Level.SEVERE,LocalizationMessages.WSA_0003_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    throw new AssertionCreationException(assertionData,ex);
                } catch (InstantiationException ex) {
                    if(LOGGER.isLoggable(Level.SEVERE)){
                        LOGGER.log(Level.SEVERE,LocalizationMessages.WSA_0003_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    throw new AssertionCreationException(assertionData,ex);
                } catch (IllegalAccessException ex) {
                    if(LOGGER.isLoggable(Level.SEVERE)){
                        LOGGER.log(Level.SEVERE,LocalizationMessages.WSA_0003_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    throw new AssertionCreationException(assertionData,ex);
                }
            }else{
                try{
                    return (PolicyAssertion)cl.newInstance();
                } catch (InstantiationException ex) {
                    if(LOGGER.isLoggable(Level.SEVERE)){
                        LOGGER.log(Level.SEVERE,LocalizationMessages.WSA_0003_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    throw new AssertionCreationException(assertionData,ex);
                } catch (IllegalAccessException ex) {
                    if(LOGGER.isLoggable(Level.SEVERE)){
                        LOGGER.log(Level.SEVERE,LocalizationMessages.WSA_0003_ERROR_INSTANTIATING(assertionData.getName()));
                    }
                    throw new AssertionCreationException(assertionData,ex);
                }
            }
            
            
        }
        return policyAssertionCreator.createAssertion(assertionData,nestedAssertions,nestedAlternative,policyAssertionCreator);
        
    }

    private Constructor<?> getConstructor(Class<?> cl) throws NoSuchMethodException{
        return cl.getConstructor(
                com.sun.xml.ws.policy.sourcemodel.AssertionData.class,
                java.util.Collection.class,
                com.sun.xml.ws.policy.AssertionSet.class);
    }
    
}
