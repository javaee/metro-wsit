/*
 * $Id: SecurityConfigurationXmlReader.java,v 1.3.2.2 2010-07-14 14:06:25 m_potociar Exp $
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

import com.sun.xml.wss.impl.policy.mls.Parameter;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.crypto.dsig.spec.XPathType;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.PrintStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xml.wss.impl.policy.mls.Target;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.core.Timestamp;

import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;

import com.sun.xml.wss.impl.configuration.*;
import com.sun.xml.wss.impl.policy.mls.DynamicSecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;

/* TODO: add logging */

/**
 * Represents a Parser for reading an XWS-Security configuration and creating an
 * appropriate XWS-Security configuration object.
 * The parser expects the root element of an XWS-Security configuration
 * to be either a <code>xwss:JAXRPCSecurity</code> or a <code>xwss:SecurityConfiguration</code>.
 *
 * @see xwssconfig.xsd (the XWS-Security configuration schema)
 */

public class SecurityConfigurationXmlReader implements ConfigurationConstants {    

    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.CONFIGURATION_DOMAIN,
            LogDomainConstants.CONFIGURATION_DOMAIN_BUNDLE);
    
    static Random rnd = new Random();
    private static Document parseXmlString(String sourceXml) throws Exception {
        return parseXmlStream(
                new ByteArrayInputStream(sourceXml.getBytes()));
    }
    
    //Note: Assumes the passed element is a <SecurityConfiguration> element
    private static void validateConfiguration(Element element)
    throws Exception {
        
        // Check if more than one xwss:Timestamp element exists
        NodeList timestamps =
                element.getElementsByTagNameNS(
                CONFIGURATION_URL,
                TIMESTAMP_ELEMENT_NAME);
        
        if (timestamps.getLength() > 1) {
            // log
            throw new IllegalStateException(
                    "More than one xwss:Timestamp element " +
                    "in security configuration file");
        }
        
        // Check if more than one xwss:RequireTimestamp element exists
        NodeList requireTimestamps =
                element.getElementsByTagNameNS(
                CONFIGURATION_URL,
                TIMESTAMP_REQUIREMENT_ELEMENT_NAME);
        
        if (requireTimestamps.getLength() > 1) {
            // log
            throw new IllegalStateException(
                    "More than one xwss:RequireTimestamp element " +
                    "in security configuration file");
        }
        
        // Check if more than one xwss:UsernameAndPassword element exists
        NodeList usernamePasswords =
                element.getElementsByTagNameNS(
                CONFIGURATION_URL,
                USERNAME_PASSWORD_AUTHENTICATION_ELEMENT_NAME);
        
        if (usernamePasswords.getLength() > 1) {
            // log
            throw new IllegalStateException(
                    "More than one xwss:UsernameToken element " +
                    "in security configuration file");
        }
        
        // Check if more than one xwss:RequireUsernameAndPassword element exists
        NodeList requireUsernamePasswords =
                element.getElementsByTagNameNS(
                CONFIGURATION_URL,
                USERNAMETOKEN_REQUIREMENT_ELEMENT_NAME);
        
        if (requireUsernamePasswords.getLength() > 1) {
            // log
            throw new IllegalStateException(
                    "More than one xwss:RequireUsernameToken element " +
                    "in security configuration file");
        }
        
        // Check if more than one xwss:OptionalTargets element exists
        NodeList optionalTargets =
                element.getElementsByTagNameNS(
                CONFIGURATION_URL,
                OPTIONAL_TARGETS_ELEMENT_NAME);
        
        if (optionalTargets.getLength() > 1) {
            // log
            throw new IllegalStateException(
                    "More than one xwss:OptionalTargets element " +
                    "in security configuration file");
        }
        
        // Check if more than one xwss:SAMLASSERTION elements exist
        NodeList samlAssertions =
                element.getElementsByTagNameNS(
                CONFIGURATION_URL,
                SAML_ASSERTION_ELEMENT_NAME);
        
        if (samlAssertions.getLength() > 1) {
            // log
            throw new IllegalStateException(
                    "More than one xwss:SAMLAssertion element " +
                    "in security configuration file");
        }
        
        checkIdUniqueness(element);
    }
    
    /**
     * read an XWS-Security configuration String representing an <code>xwss:JAXRPCSecurity</code> element
     * and return an ApplicationSecurityConfiguration instance.
     * @param sourceXml the configuration String
     * @return  an <code>ApplicationSecurityConfiguration</code> corresponding to the configuration
     * @exception Exception if there was an error in creating the configuration
     */
    public static ApplicationSecurityConfiguration
            readApplicationSecurityConfigurationString(
            String sourceXml) throws Exception {
        return (ApplicationSecurityConfiguration)
        createSecurityConfiguration(
                parseXmlString(sourceXml).
                getDocumentElement());
    }
    
    private static Document parseXmlStream(
            InputStream xmlStream) throws Exception {
        return parseXmlStream(xmlStream, null);
    }
    
    private static Document parseXmlStream(
            InputStream xmlStream, PrintStream out)
            throws Exception {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                //new com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl();
        factory.setAttribute(
                "http://apache.org/xml/features/validation/dynamic",
                Boolean.FALSE);
        factory.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
        InputStream is =
                SecurityConfigurationXmlReader.class.
                getResourceAsStream("xwssconfig.xsd");
        factory.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaSource", is);
        factory.setValidating(true);
        factory.setIgnoringComments(true);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler(out));
        Document configurationDocument = builder.parse(xmlStream);
        
        // not very efficient, can we do it as part of regular parsing
        NodeList nodeList = configurationDocument.getElementsByTagNameNS
                (CONFIGURATION_URL,DECLARATIVE_CONFIGURATION_ELEMENT_NAME);
        for(int i=0;i<nodeList.getLength();i++) {
            validateConfiguration((Element)nodeList.item(i));
        }
        return configurationDocument;
    }
    
    /**
     * Parse and validate an XWS-Security configuration
     * @param xmlStream the InputStream representing the configuration
     * @param out the PrintStream to which Errors messages should be logged
     * @exception Exception if there was an error while validating the configuration
     */
    public static void validate(
            InputStream xmlStream, PrintStream out)
            throws Exception {
        parseXmlStream(xmlStream, out);
    }
    
    /**
     * read an XWS-Security configuration representing a <code>xwss:SecurityConfiguration</code> element
     * and return a DeclarativeSecurityConfiguration instance.
     * @param xmlStream the InputStream for the configuration
     * @return  a <code>DeclarativeSecurityConfiguration</code> corresponding to the configuration
     * @exception Exception if there was an error in creating the configuration
     */
    public static DeclarativeSecurityConfiguration
            createDeclarativeConfiguration(InputStream xmlStream)
            throws Exception {
        return readContainerForBaseConfigurationData(
                parseXmlStream(xmlStream).getDocumentElement(),
                new DeclarativeSecurityConfiguration());
    }
    
    /**
     * read an XWS-Security configuration  representing an <code>xwss:JAXRPCSecurity</code> element
     * and return an ApplicationSecurityConfiguration instance.
     * @param xmlStream the InputStream for the configuration
     * @return  an <code>ApplicationSecurityConfiguration</code> corresponding to the configuration
     * @exception Exception if there was an error in creating the configuration
     */
    public static ApplicationSecurityConfiguration
            createApplicationSecurityConfiguration(
            InputStream xmlStream) throws Exception {
        ApplicationSecurityConfiguration config =
                (ApplicationSecurityConfiguration) createSecurityConfiguration(
                parseXmlStream(xmlStream).getDocumentElement());
        config.init();
        return config;
    }
    
    private static DeclarativeSecurityConfiguration
            createDeclarativeConfiguration(
            Element configData) throws Exception {
        DeclarativeSecurityConfiguration declarations =
                new DeclarativeSecurityConfiguration();
        readContainerForBaseConfigurationData(configData, declarations);
        return declarations;
    }
    @SuppressWarnings("unchecked")
    private static SecurityPolicy
            createSecurityConfiguration(Element configData)
            throws Exception {
        
        QName qname = getQName(configData);
        
        if (JAXRPC_SECURITY_ELEMENT_QNAME.equals(qname)) {
            ApplicationSecurityConfiguration declarations =
                    new ApplicationSecurityConfiguration();
            
            String secEnvHandler = getSecurityEnvironmentHandler(configData);
            if (secEnvHandler != null)
                declarations.setSecurityEnvironmentHandler(secEnvHandler);
            
            if (!configHasSingleService(configData)) {
                throw new IllegalStateException(
                        "Single <xwss:Service> element expected under <xwss:JAXRPCSecurity> element");
            }
            
            String optimize = configData.getAttribute(OPTIMIZE_ATTRIBUTE_NAME);

            boolean opt = Boolean.valueOf(optimize);

            declarations.isOptimized(opt); 
            
            String retainSecHeader = configData.getAttribute(RETAIN_SEC_HEADER);
            declarations.retainSecurityHeader(Boolean.valueOf(retainSecHeader));

            String resetMU = configData.getAttribute(RESET_MUST_UNDERSTAND);
            declarations.resetMustUnderstand(Boolean.valueOf(resetMU));
            
            Element previousDefinitionElement = null;
            Element eachDefinitionElement = getFirstChildElement(configData);
            int secEnvTagCount = 0;
            HashMap serviceNameMap = new HashMap();
            
            // we should encounter a single Sec-Env-Handler and one or more Services
            while (eachDefinitionElement != null) {
                QName definitionType = getQName(eachDefinitionElement);
                
                if (SERVICE_ELEMENT_QNAME.equals(definitionType)) {
                    StaticApplicationContext jaxrpcSContext = new StaticApplicationContext();
                    
                    // when there are multiple services the name has to be unique
                    String name = eachDefinitionElement.getAttribute(NAME_ATTRIBUTE_NAME);
                    if (serviceNameMap.containsKey(name)) {
                        //log
                        throw new IllegalStateException(
                                "Service Name " + name +
                                " Already in use for another Service");
                    } else {
                        serviceNameMap.put(name,null);
                    }
                    
                    
                    readApplicationSecurityConfiguration(
                            eachDefinitionElement, declarations, null, jaxrpcSContext);
                    
                } else if (SECURITY_ENVIRONMENT_HANDLER_ELEMENT_QNAME.
                        equals(definitionType)) {
                    
                    if (secEnvTagCount == 1) {
                        // log
                        throw new IllegalStateException(
                                "More than one xwss:SecurityEnvironmentHandler " +
                                "element in security configuration file");
                    }
                    secEnvTagCount++;
                    
                } else {
                    log.log(Level.SEVERE,
                            "WSS0413.illegal.configuration.element",
                            eachDefinitionElement.getTagName());
                    throw new IllegalStateException(
                            eachDefinitionElement.getTagName()
                            + " is not a recognized definition type");
                }
                
                previousDefinitionElement = eachDefinitionElement;
                eachDefinitionElement = getNextElement(eachDefinitionElement);
            }
            
            // check that the SecurityEnvironmentHandler was the last one
            // TODO: not sure if this check is required/enforced by schema
            QName definitionType = getQName( previousDefinitionElement);
            if (!SECURITY_ENVIRONMENT_HANDLER_ELEMENT_QNAME.equals(definitionType)) {
                // log
                throw new IllegalStateException(
                        "The SecurityEnvironmentHandler must appear as " +
                        "the last Element inside a <xwss:JAXRPCSecurity>");
            }            
            // implementation optimization for most common case
            //declarations.configurationHasPorts(configHasPorts(configData));
            declarations.singleServiceNoPorts(
                    configHasSingleServiceAndNoPorts(configData));
            declarations.hasOperationPolicies(
                    configHasOperations(configData));
            
            return declarations;
            
        } else if (DECLARATIVE_CONFIGURATION_ELEMENT_QNAME.equals(qname)) {
            if (dynamicPolicy(configData)) {
                SecurityPolicy declarations = new DynamicSecurityPolicy();
                return declarations;
            }
            DeclarativeSecurityConfiguration declarations =
                    new DeclarativeSecurityConfiguration();
            readContainerForBaseConfigurationData(
                    configData, declarations);
            return declarations;
        } else {
            log.log(Level.SEVERE,
                    "WSS0413.illegal.configuration.element",
                    configData.getTagName());
            throw new IllegalStateException(configData.getTagName()
            + " is not a recognized definition type");
        }
    }
    
    /*
     * Need to be coupled with schema checks
     */
    private static void readApplicationSecurityConfiguration(
            Element configData,
            SecurityPolicy declarations,
            SecurityPolicy subDeclarations,
            StaticApplicationContext iContext)
            throws Exception {
        
        QName qname = getQName(configData);
        
        if (SERVICE_ELEMENT_QNAME.equals(qname)) {
            
            String id = getIdAttribute(configData);
            String name = configData.getAttribute(NAME_ATTRIBUTE_NAME);
            String useCache = configData.getAttribute(USECACHE_ATTRIBUTE_NAME);
            
            boolean isBSP = getBSPAttribute(configData, null);
            
            iContext.isService(true);
            iContext.setUUID(id);
            iContext.setServiceIdentifier(name);
            
            // temporary work-around for applicationId
            if (!"".equals(name)) {
                iContext.setApplicationContextRoot(name);
            } else if (!"".equals(id)) {
                iContext.setApplicationContextRoot(id);
            } else {
                iContext.setApplicationContextRoot(generateUUID());
            }
            
            
            ApplicationSecurityConfiguration innerDeclarations =
                    new ApplicationSecurityConfiguration();
            
            innerDeclarations.isBSP(isBSP);
            innerDeclarations.useCache(
                    parseBoolean(USECACHE_ATTRIBUTE_NAME, useCache));
            
            ((ApplicationSecurityConfiguration) declarations).
                    setSecurityPolicy(iContext, innerDeclarations);
            
            String secEnvHandler = getSecurityEnvironmentHandler(configData);
            if (secEnvHandler != null) {
                innerDeclarations.setSecurityEnvironmentHandler(secEnvHandler);
            } else if (((ApplicationSecurityConfiguration)declarations).
                    getSecurityEnvironmentHandler() != null) {
                innerDeclarations.setSecurityEnvironmentHandler(
                        ((ApplicationSecurityConfiguration)declarations).
                        getSecurityEnvironmentHandler());
            } else {
                throw new IllegalStateException(
                        "Missing <xwss:SecurityEnvironmentHandler> element for " +
                        qname.getLocalPart());
            }
            
            NodeList nl = configData.getChildNodes();
            for (int i=0; i < nl.getLength(); i++) {
                // assuming all element nodes
                Node child = (Node) nl.item(i);
                if (child instanceof Element) {
                    readApplicationSecurityConfiguration(
                            (Element)child, declarations, innerDeclarations, iContext);
                }
            }
            
        } else
            if (PORT_ELEMENT_QNAME.equals(qname)) {
            
            if (subDeclarations == null) {
                throw new Exception(
                        "Unexpected <xwss:Port> element without a parent " +
                        "<xwss:Service> encountered");
            }
            
            String port = configData.getAttribute(NAME_ATTRIBUTE_NAME);
            
            StaticApplicationContext jContext = new StaticApplicationContext();
            jContext.copy(iContext);
            jContext.isPort(true);
            jContext.isService(false);
            jContext.setPortIdentifier(port);
            
            ApplicationSecurityConfiguration innerDeclarations =
                    new ApplicationSecurityConfiguration();
            
            boolean isBSP = getBSPAttribute(
                    configData, (ApplicationSecurityConfiguration)subDeclarations);
            
            innerDeclarations.isBSP(isBSP);
            innerDeclarations.setSecurityEnvironmentHandler(
                    ((ApplicationSecurityConfiguration)subDeclarations).
                    getSecurityEnvironmentHandler());
            
            ((ApplicationSecurityConfiguration) declarations).
                    setSecurityPolicy(jContext, innerDeclarations);
            ((ApplicationSecurityConfiguration) subDeclarations).
                    setSecurityPolicy(jContext, innerDeclarations);
            
            NodeList nl = configData.getChildNodes();
            for (int i=0; i < nl.getLength(); i++) {
                // assuming all element nodes
                Node child = (Node) nl.item(i);
                if (child instanceof Element) {
                    readApplicationSecurityConfiguration(
                            (Element)child, declarations, innerDeclarations, jContext);
                }
            }
            
            } else
                if (OPERATION_ELEMENT_QNAME.equals(qname)) {
            
            String operation = configData.getAttribute(NAME_ATTRIBUTE_NAME);
            
            StaticApplicationContext kContext = new StaticApplicationContext();
            
            kContext.copy(iContext);
            kContext.isOperation(true);
            kContext.isPort(false);
            kContext.setOperationIdentifier(operation);
            
            
            ApplicationSecurityConfiguration innerDeclarations =
                    new ApplicationSecurityConfiguration();
            
            ((ApplicationSecurityConfiguration) declarations).
                    setSecurityPolicy(kContext, innerDeclarations);
            ((ApplicationSecurityConfiguration) subDeclarations).
                    setSecurityPolicy(kContext, innerDeclarations);
            
            boolean isBSP = getBSPAttribute(
                    configData, (ApplicationSecurityConfiguration)subDeclarations);
            innerDeclarations.isBSP(isBSP);
            innerDeclarations.setSecurityEnvironmentHandler(
                    ((ApplicationSecurityConfiguration)subDeclarations).
                    getSecurityEnvironmentHandler());
            
            NodeList nl = configData.getChildNodes();
            for (int i=0; i < nl.getLength(); i++) {
                // assuming all element nodes
                Node child = (Node) nl.item(i);
                if (child instanceof Element) {
                    readApplicationSecurityConfiguration(
                            (Element)child, declarations, innerDeclarations, kContext);
                }
            }
            
                } else
                    if (DECLARATIVE_CONFIGURATION_ELEMENT_QNAME.equals(qname)) {
            
            if (dynamicPolicy(configData)) {
                ((ApplicationSecurityConfiguration) subDeclarations).
                        setSecurityPolicy(iContext, new DynamicSecurityPolicy());
            }
            
            DeclarativeSecurityConfiguration  innerDeclarations =
                    new DeclarativeSecurityConfiguration();
            
            boolean isBSP = getBSPAttribute(
                    configData, (ApplicationSecurityConfiguration)subDeclarations);
            innerDeclarations.isBSP(isBSP);
            
            ((ApplicationSecurityConfiguration) subDeclarations).
                    setSecurityPolicy(iContext, innerDeclarations);
            
            readContainerForBaseConfigurationData(
                    configData, innerDeclarations,
                    ((ApplicationSecurityConfiguration)subDeclarations).
                    getSecurityEnvironmentHandler());
            
                    } else if (
                SECURITY_ENVIRONMENT_HANDLER_ELEMENT_QNAME.equals(qname)) {
            
            //TODO: check here that number of handler specified is not > 1
            if (!iContext.isService()) {
                // log
                throw new IllegalStateException(
                        "An <xwss:SecurityEnvironmentHandler> can only appear" +
                        "under a <xwss:Service>/<xwss:JAXRPCSecurity> element");
            }
                    }
    }
    
    private static DeclarativeSecurityConfiguration
            readContainerForBaseConfigurationData(
            Element configData,
            DeclarativeSecurityConfiguration declarations)
            throws Exception {
        return readContainerForBaseConfigurationData(configData, declarations, null);
    }
    
    private static DeclarativeSecurityConfiguration
            readContainerForBaseConfigurationData(
            Element configData,
            DeclarativeSecurityConfiguration declarations,
            String securityHandlerClass)
            throws Exception {
        
        QName qname = getQName(configData);
        
        if (DECLARATIVE_CONFIGURATION_ELEMENT_QNAME.equals(qname)) {
            
            NamedNodeMap configurationAttributes = configData.getAttributes();
            int attributeCount = configurationAttributes.getLength();
            String attributeName = null;
            
            for (int index = 0; index < attributeCount; index++) {
                Attr configurationAttribute =
                        (Attr) configurationAttributes.item(index);
                attributeName = configurationAttribute.getName();
                
                if (DUMP_MESSAGES_ATTRIBUTE_NAME.
                        equalsIgnoreCase(attributeName)) {
                    declarations.setDumpMessages(
                            parseBoolean(
                            DUMP_MESSAGES_ATTRIBUTE_NAME,
                            configurationAttribute.getValue()));
                    
                } else if (MessageConstants.NAMESPACES_NS.equals(
                        configurationAttribute.getNamespaceURI())) {
                    // Ignore namespace declaration
                } else if (ENABLE_DYNAMIC_POLICY_ATTRIBUTE_NAME.
                        equalsIgnoreCase(attributeName)) {
                    declarations.enableDynamicPolicy(
                            parseBoolean(
                            ENABLE_DYNAMIC_POLICY_ATTRIBUTE_NAME,
                            configurationAttribute.getValue()));
                } else if(ENABLE_WSS11_POLICY_ATTRIBUTE_NAME.
                        equalsIgnoreCase(attributeName)){
                    boolean wss11Enabled = parseBoolean(ENABLE_WSS11_POLICY_ATTRIBUTE_NAME,
                            configurationAttribute.getValue());                 
                    declarations.senderSettings().enableWSS11Policy(wss11Enabled);
                    declarations.receiverSettings().enableWSS11Policy(wss11Enabled);
                } else if (RETAIN_SEC_HEADER.equalsIgnoreCase(attributeName)) {
                     String retainSecHeader = configurationAttribute.getValue();
                     declarations.retainSecurityHeader(Boolean.valueOf(retainSecHeader));
                } else if (RESET_MUST_UNDERSTAND.equalsIgnoreCase(attributeName)) {
                     String resetMU = configurationAttribute.getValue();
                     declarations.resetMustUnderstand(Boolean.valueOf(resetMU));
                } else {
                    log.log(Level.SEVERE,
                            "WSS0412.illegal.attribute.name",
                            new Object[]
                    {attributeName, configData.getTagName()});
                    throw new IllegalStateException(
                            attributeName
                            + " is not a recognized attribute of SecurityConfiguration");
                }
            }
            readBaseConfigurationData(configData, declarations, securityHandlerClass);
        } else {
            log.log(Level.SEVERE,
                    "WSS0413.illegal.configuration.element",
                    configData.getTagName());
            throw new IllegalStateException(configData.getTagName()
            + " is not a recognized definition type");
        }
        return declarations;
    }
    
    private static void readBaseConfigurationData(
            Element configData,
            DeclarativeSecurityConfiguration declarations,
            String securityHandlerClass)
            throws PolicyGenerationException, XWSSecurityException {
        
        Element eachDefinitionElement = getFirstChildElement(configData);
        boolean timestampFound = false;
        
        boolean senderEnableDynamicPolicy = declarations.senderSettings().enableDynamicPolicy();
        boolean receiverEnableDynamicPolicy = declarations.receiverSettings().enableDynamicPolicy();
        boolean receiverBSPFlag = declarations.receiverSettings().isBSP();
        //added for BackwardCompatibility with XWSS1.1, the xmlsec in XWSS11 cannot
        //accept PrefixList in CanonicalizationMethod parameters
        boolean senderBSPFlag = declarations.senderSettings().isBSP();
        
        while (eachDefinitionElement != null) {
            QName definitionType = getQName(eachDefinitionElement);
            
            if (TIMESTAMP_ELEMENT_QNAME.equals(definitionType)) {
                
                if (!timestampFound) {
                    TimestampPolicy timestampPolicy = new TimestampPolicy();
                    readTimestampSettings(timestampPolicy, eachDefinitionElement);
                    applyDefaults(timestampPolicy, senderEnableDynamicPolicy);

                    declarations.senderSettings().append(timestampPolicy);
                    timestampFound = true;
                } else {
                    log.log(Level.SEVERE,
                            "WSS0516.duplicate.configuration.element",
                            new Object[] {
                        definitionType, configData.getLocalName()});
                        throw new IllegalStateException(
                                "Duplicate Timestamp element");
                }
                
            } else if (ENCRYPT_OPERATION_ELEMENT_QNAME.equals(definitionType)) {
                
                EncryptionPolicy encryptionPolicy = new EncryptionPolicy();
                readEncryptionSettings(encryptionPolicy, eachDefinitionElement);
                applyDefaults(encryptionPolicy, senderEnableDynamicPolicy);
                declarations.senderSettings().append(encryptionPolicy);
                
            } else if (SIGN_OPERATION_ELEMENT_QNAME.equals(definitionType)) {
                SignaturePolicy signaturePolicy = new SignaturePolicy();
                readSigningSettings(signaturePolicy, eachDefinitionElement, senderEnableDynamicPolicy);               
                //declarations.senderSettings().append(signaturePolicy);
                //added for BackwardCompatibility with XWSS1.1, the xmlsec in XWSS11 cannot
                //accept PrefixList in CanonicalizationMethod parameters
                SignaturePolicy.FeatureBinding fb = 
                         (SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding();
                if (fb != null) {
                     fb.isBSP(senderBSPFlag);
                }
                //end of XWSS11 BC fix
                
                String includeTimeStamp =
                        eachDefinitionElement.getAttribute(INCLUDE_TIMESTAMP_ATTRIBUTE_NAME);
                boolean timeStamp = getBooleanValue(includeTimeStamp);
                
                if (timeStamp && !hasTimestampSiblingPolicy(eachDefinitionElement)) {
                    //System.out.println("Adding from SIGN");
                    TimestampPolicy t = new TimestampPolicy();
                    t.setMaxClockSkew(Timestamp.MAX_CLOCK_SKEW);
                    t.setTimestampFreshness(Timestamp.TIMESTAMP_FRESHNESS_LIMIT);
                    applyDefaults(t, senderEnableDynamicPolicy);
                    declarations.senderSettings().append(t);
                }
                
                declarations.senderSettings().append(signaturePolicy);
                
            } else if (
                    USERNAME_PASSWORD_AUTHENTICATION_ELEMENT_QNAME.
                    equals(definitionType)) {
                
                try {
                    AuthenticationTokenPolicy utBinding =
                            new AuthenticationTokenPolicy();
                    AuthenticationTokenPolicy.UsernameTokenBinding
                            featureBinding =
                            (AuthenticationTokenPolicy.UsernameTokenBinding)
                            utBinding.newUsernameTokenFeatureBinding();
                    featureBinding.newTimestampFeatureBinding();
                    readUsernamePasswordSettings(featureBinding, eachDefinitionElement);
                    applyDefaults(featureBinding, senderEnableDynamicPolicy);
                    declarations.senderSettings().append(utBinding);
                } catch (PolicyGenerationException pge) {
                    // log
                    throw new IllegalStateException(pge.getMessage());
                }
                
            } else if (SAML_ELEMENT_QNAME.equals(definitionType)) {
                
                try {
                    AuthenticationTokenPolicy samlBinding =
                            new AuthenticationTokenPolicy();
                    AuthenticationTokenPolicy.SAMLAssertionBinding
                            featureBinding =
                            (AuthenticationTokenPolicy.SAMLAssertionBinding)
                            samlBinding.newSAMLAssertionFeatureBinding();
                    readSAMLTokenSettings(
                            featureBinding, eachDefinitionElement);
                    applyDefaults(featureBinding, senderEnableDynamicPolicy);
                    declarations.senderSettings().append(samlBinding);
                } catch (PolicyGenerationException pge) {
                    // log
                    throw new IllegalStateException(pge.getMessage());
                }
            } else if (SIGNATURE_REQUIREMENT_ELEMENT_QNAME.equals(
                    definitionType)) {
                SignaturePolicy signaturePolicy = new SignaturePolicy();
                readVerifySettings(signaturePolicy, eachDefinitionElement,receiverBSPFlag, receiverEnableDynamicPolicy);
                declarations.receiverSettings().append(signaturePolicy);
                
                String requireTimeStamp =
                        eachDefinitionElement.getAttribute(TIMESTAMP_REQUIRED_ATTRIBUTE_NAME);
                boolean timeStamp = getBooleanValue(requireTimeStamp);
                
                if (timeStamp && !hasTimestampSiblingPolicy(eachDefinitionElement)) {
                    //System.out.println("Adding from RequireSignature");
                    TimestampPolicy t = new TimestampPolicy();
                    //t.setMaxClockSkew(Timestamp.MAX_CLOCK_SKEW);
                    //t.setTimestampFreshness(Timestamp.TIMESTAMP_FRESHNESS_LIMIT);
                    applyReceiverDefaults(t, receiverBSPFlag, securityHandlerClass, receiverEnableDynamicPolicy);
                    declarations.receiverSettings().append(t);
                }
                
            } else if (ENCRYPTION_REQUIREMENT_ELEMENT_QNAME.equals(
                    definitionType)) {
                EncryptionPolicy encryptionPolicy = new EncryptionPolicy();
                readDecryptionSettings(encryptionPolicy, eachDefinitionElement);
                applyReceiverDefaults(encryptionPolicy, receiverBSPFlag, receiverEnableDynamicPolicy);
                declarations.receiverSettings().append(encryptionPolicy);
                
            } else if (USERNAMETOKEN_REQUIREMENT_ELEMENT_QNAME.equals(
                    definitionType)) {
                try {
                    AuthenticationTokenPolicy utBinding =
                            new AuthenticationTokenPolicy();
                    AuthenticationTokenPolicy.UsernameTokenBinding
                            featureBinding =
                            (AuthenticationTokenPolicy.UsernameTokenBinding)
                            utBinding.newUsernameTokenFeatureBinding();
                    featureBinding.newTimestampFeatureBinding();
                    readUsernamePasswordRequirementSettings(
                            featureBinding, eachDefinitionElement);
                    applyReceiverDefaults(featureBinding, receiverBSPFlag, securityHandlerClass, receiverEnableDynamicPolicy);
                    declarations.receiverSettings().append(utBinding);
                    if (MessageConstants.debug) {
                        log.log(Level.FINEST, "Added usernameToken Requirement ...." + featureBinding);
                    }
                } catch (PolicyGenerationException pge) {
                    // log
                    throw new IllegalStateException(pge.getMessage());
                }
            } else if (TIMESTAMP_REQUIREMENT_ELEMENT_QNAME.equals(
                    definitionType)) {
                
                TimestampPolicy timestampPolicy = new TimestampPolicy();
                readTimestampRequirementSettings(
                        timestampPolicy, eachDefinitionElement);
                applyReceiverDefaults(timestampPolicy, receiverBSPFlag, securityHandlerClass, receiverEnableDynamicPolicy);
                declarations.receiverSettings().append(timestampPolicy);
                
            } else if (SAML_REQUIREMENT_ELEMENT_QNAME.equals(
                    definitionType)) {
                
                // read SAML requirement element
                try {
                    AuthenticationTokenPolicy samlBinding =
                            new AuthenticationTokenPolicy();
                    AuthenticationTokenPolicy.SAMLAssertionBinding
                            featureBinding =
                            (AuthenticationTokenPolicy.SAMLAssertionBinding)
                            samlBinding.newSAMLAssertionFeatureBinding();
                    readRequireSAMLTokenSettings(
                            featureBinding, eachDefinitionElement);
                    applyReceiverDefaults(featureBinding, receiverBSPFlag, receiverEnableDynamicPolicy);
                    declarations.receiverSettings().append(samlBinding);
                } catch (PolicyGenerationException pge) {
                    // log
                    throw new IllegalStateException(pge.getMessage());
                }
                
            } else if (OPTIONAL_TARGETS_ELEMENT_QNAME.equals(definitionType)) {
                readOptionalTargetSettings(
                        declarations.receiverSettings(), eachDefinitionElement);
            } else {
                log.log(Level.SEVERE,
                        "WSS0513.illegal.configuration.element",
                        definitionType.toString());
                throw new IllegalStateException(definitionType
                        + " is not a recognized definition type");
            }
            
            eachDefinitionElement = getNextElement(eachDefinitionElement);
        }
    }
    
    
    private static void readVerifySettings( SignaturePolicy signaturePolicy,
            Element signingSettings,boolean bsp, boolean dp) {
        readVerifySettings(signaturePolicy, signingSettings);
        applyReceiverDefaults(signaturePolicy, bsp, dp);
        String includeTimeStamp = signingSettings.getAttribute(TIMESTAMP_REQUIRED_ATTRIBUTE_NAME);
        boolean timeStamp = getBooleanValue(includeTimeStamp);
        if(timeStamp){
            /*if (!hasTimestampSiblingPolicy(signingSettings)) {
                ((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding())
                .includeTimestamp(timeStamp);
            } else {*/
            // add an Xpath target to existing singular timestamp
            SignatureTarget st = new SignatureTarget();
            st.setType("qname");
            st.setValue(MessageConstants.TIMESTAMP_QNAME);
            st.setDigestAlgorithm(DigestMethod.SHA1); //SHA1
            ((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding()).
                    addTargetBinding(st);
            
            //}
        }
        signaturePolicy.isBSP(bsp);
    }
    
    private static void readVerifySettings(
            SignaturePolicy signaturePolicy, Element signingSettings) {
        readSigningSettings(signaturePolicy, signingSettings);
    }
    
    private static void readSigningSettings(SignaturePolicy signaturePolicy,
            Element signingSettings,boolean enableDynamicPolicy) {
        readSigningSettings(signaturePolicy, signingSettings);
        applyDefaults(signaturePolicy, enableDynamicPolicy);
        
        String includeTimeStamp = signingSettings.getAttribute(INCLUDE_TIMESTAMP_ATTRIBUTE_NAME);
        boolean timeStamp = getBooleanValue(includeTimeStamp);
        
        if(timeStamp){
            /*if (!hasTimestampSiblingPolicy(signingSettings)) {
                ((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding())
                .includeTimestamp(timeStamp);
            } else {*/
            // add an Xpath target to existing singular timestamp
            SignatureTarget st = new SignatureTarget();
            st.setType("qname");
            st.setDigestAlgorithm(DigestMethod.SHA1); //SHA1
            st.setValue(MessageConstants.TIMESTAMP_QNAME);
            ((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding()).
                    addTargetBinding(st);
            
            //}
        }
    }
    
    private static boolean hasTimestampSiblingPolicy(Element signElement) {
        if (SIGN_OPERATION_ELEMENT_NAME.equals(signElement.getLocalName())) {
            Element signParent =(Element) signElement.getParentNode();
            NodeList timeStampNodes = signParent.getElementsByTagNameNS(ConfigurationConstants.CONFIGURATION_URL,TIMESTAMP_ELEMENT_NAME);
            if(timeStampNodes.getLength() > 0){
                return true;
            }
        }else{
            Element signParent =(Element) signElement.getParentNode();
            NodeList timeStampNodes = signParent.getElementsByTagNameNS(ConfigurationConstants.CONFIGURATION_URL,TIMESTAMP_REQUIREMENT_ELEMENT_NAME);
            if(timeStampNodes.getLength() > 0){
                return true;
            }
            NamedNodeMap requireTimestampAttrNode = null;
            Node requireSignatureNode = signElement.getPreviousSibling();
            while ( requireSignatureNode != null) {
                if ( SIGNATURE_REQUIREMENT_ELEMENT_NAME.equals(requireSignatureNode.getLocalName()) ) {
                    requireTimestampAttrNode = requireSignatureNode.getAttributes();
                    //If there is no requireTimestamp attrbute then the defaultvalue is true
                    //System.out.println(requireTimestampAttrNode.getNamedItem(TIMESTAMP_REQUIRED_ATTRIBUTE_NAME));
                    if ( "true".equalsIgnoreCase(
                            requireTimestampAttrNode.getNamedItem(
                            TIMESTAMP_REQUIRED_ATTRIBUTE_NAME).getLocalName())) {
                        return true;
                    }
                }
                requireSignatureNode = requireSignatureNode.getPreviousSibling();
            }
            
        }
        return false;
    }
    
    /*
     */
    private static void readSigningSettings(
            SignaturePolicy signaturePolicy, Element signingSettings) {
        
        String id = getIdAttribute(signingSettings);
        signaturePolicy.setUUID(id);
        // Read sign attributes
        NamedNodeMap signingAttributes = signingSettings.getAttributes();
        int attributeCount = signingAttributes.getLength();
        String attributeName = null;
        
        for (int index = 0; index < attributeCount; index++) {
            Attr signingAttribute = (Attr) signingAttributes.item(index);
            attributeName = signingAttribute.getName();
            if (ID_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                // do nothing
            } else if (INCLUDE_TIMESTAMP_ATTRIBUTE_NAME.equalsIgnoreCase
                    (attributeName) && (SIGN_OPERATION_ELEMENT_NAME.equals(signingSettings.getLocalName()))) {
                /*((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding())
                .includeTimestamp(getBooleanValue
                (signingAttribute.getValue()));*/
            } else if (TIMESTAMP_REQUIRED_ATTRIBUTE_NAME.equalsIgnoreCase
                    (attributeName) && (SIGNATURE_REQUIREMENT_ELEMENT_NAME.equals(signingSettings.getLocalName()))) {
                /*((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding())
                .includeTimestamp(getBooleanValue
                (signingAttribute.getValue()));*/
            } else {
                log.log(Level.SEVERE,
                        "WSS0512.illegal.attribute.name",
                        new Object[]
                {attributeName, signingSettings.getTagName()});
                throw new IllegalStateException(attributeName +
                        " is not a recognized attribute of " +
                        signingSettings.getTagName());
            }
        }
        
        Element eachSubElement = getFirstChildElement(signingSettings);
        int keyBearingTokensSeen = 0;
        
        while (eachSubElement != null) {
            QName subElementQName = getQName(eachSubElement);
            
            if (TARGET_QNAME.equals(subElementQName)) {
                SignaturePolicy.FeatureBinding featureBinding =
                        (SignaturePolicy.FeatureBinding)
                        signaturePolicy.getFeatureBinding();
                featureBinding.addTargetBinding(
                        readTargetSettings(eachSubElement, true));
            } else if (X509TOKEN_ELEMENT_QNAME.equals(subElementQName)) {
                if (keyBearingTokensSeen > 0) {
                    log.log(Level.SEVERE,
                            "WSS0520.illegal.configuration.state");
                    throw new IllegalStateException(
                            "Atmost one of X509token/SymmetricKey/SAMLAssertion " +
                            " key bindings can be configured " +
                            "for an Sign/RequireSignature operation");
                }
                keyBearingTokensSeen++;
                readX509TokenSettings(
                        (AuthenticationTokenPolicy.X509CertificateBinding)
                        signaturePolicy.newX509CertificateKeyBinding(), eachSubElement);
            } else if (SYMMETRIC_KEY_ELEMENT_QNAME.equals(subElementQName)) {
                if (keyBearingTokensSeen > 0) {
                    log.log(Level.SEVERE,
                            "WSS0520.illegal.configuration.state");
                    throw new IllegalStateException(
                            "Atmost one of X509token/SymmetricKey/SAMLAssertion " +
                            " key bindings can be configured " +
                            "for an Sign/RequireSignature operation");
                }
                keyBearingTokensSeen++;
                readSymmetricKeySettings(
                        (SymmetricKeyBinding) signaturePolicy.newSymmetricKeyBinding(),
                        eachSubElement);
            } else if (SAML_ELEMENT_QNAME.equals(subElementQName)) {
                if (keyBearingTokensSeen > 0) {
                    log.log(Level.SEVERE,
                            "WSS0520.illegal.configuration.state");
                    throw new IllegalStateException(
                            "Atmost one of X509token/SymmetricKey/SAMLAssertion " +
                            " key bindings can be configured " +
                            "for an Sign/RequireSignature operation");
                }
                keyBearingTokensSeen++;
                readSAMLTokenSettings(
                        (AuthenticationTokenPolicy.SAMLAssertionBinding)
                        signaturePolicy.newSAMLAssertionKeyBinding(),
                        eachSubElement);
            } else if (SIGNATURE_TARGET_ELEMENT_QNAME.equals(subElementQName)) {
                SignaturePolicy.FeatureBinding featureBinding =
                        (SignaturePolicy.FeatureBinding)
                        signaturePolicy.getFeatureBinding();
                featureBinding.addTargetBinding(
                        readSignatureTargetSettings(eachSubElement));
            } else if (CANONICALIZATION_METHOD_ELEMENT_QNAME.equals(subElementQName)) {
                readCanonMethodSettings(signaturePolicy, eachSubElement);
            } else if (SIGNATURE_METHOD_ELEMENT_QNAME.equals(subElementQName)) {
                readSigMethodSettings(signaturePolicy, eachSubElement);
            } else {
                log.log(Level.SEVERE,
                        "WSS0513.illegal.configuration.element",
                        subElementQName.toString());
                throw new IllegalStateException(subElementQName
                        + " is not a recognized sub-element of Sign/RequireSignature");
            }
            eachSubElement = getNextElement(eachSubElement);
        }
    }
    
    /*
     * TODO: was the keyAlias an optional attribute in last release
     */
    private static void readSymmetricKeySettings(
            SymmetricKeyBinding keyBinding, Element symmKeyElement) {
        
        NamedNodeMap symmKeyAttributes = symmKeyElement.getAttributes();
        int attributeCount = symmKeyAttributes.getLength();
        String attributeName = null;
        
        if(attributeCount == 0){
            throw new IllegalStateException(
                    "A SymmetricKey must specify keyAlias, certAlias or useReceivedSecret as an attribute");
        }
        
        for (int index = 0; index < attributeCount; index++) {
            Attr symmKeyAttribute = (Attr) symmKeyAttributes.item(index);
            attributeName = symmKeyAttribute.getName();
            
            if (SYMMETRIC_KEY_ALIAS_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                keyBinding.setKeyIdentifier(symmKeyAttribute.getValue());
            }else if ("certAlias".equalsIgnoreCase(attributeName)) {
                keyBinding.setCertAlias(symmKeyAttribute.getValue());
            }else if ("useReceivedSecret".equalsIgnoreCase(attributeName)) {
                try{
                keyBinding.setUseReceivedSecret(parseBoolean(attributeName ,symmKeyAttribute.getValue()));
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else {
                log.log(Level.SEVERE,
                        "WSS0512.illegal.attribute.name",
                        new Object[]
                {attributeName, "xwss:SymmetricKey"});
                throw new IllegalStateException(attributeName
                        + " is not a recognized attribute of SymmetricKey");
            }
        }
    }
    
    /*
     * TODO: Make use of MessageConstants.<appropriateStrategyConstant>
     */
    private static void readX509TokenSettings(
            AuthenticationTokenPolicy.X509CertificateBinding keyBinding,
            Element token) {
        keyBinding.newPrivateKeyBinding();
        String id = getIdAttribute(token);
        keyBinding.setUUID(id);
        
        NamedNodeMap tokenAttributes = token.getAttributes();
        int attributeCount = tokenAttributes.getLength();
        String attributeName = null;
        for (int index = 0; index < attributeCount; index++) {
            Attr tokenAttribute = (Attr) tokenAttributes.item(index);
            attributeName = tokenAttribute.getName();
            
            if (ID_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                //do nothing
            } else if(KEY_REFERENCE_TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                String keyReferenceStrategy = tokenAttribute.getValue();
                keyBinding.setReferenceType(keyReferenceStrategy);

            } else if(CERTIFICATE_ALIAS_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                String certificateAlias = tokenAttribute.getValue();
                keyBinding.setCertificateIdentifier(certificateAlias);
            } else if (ENCODING_TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                String encodingType = tokenAttribute.getValue();
                keyBinding.setEncodingType(encodingType);
            } else if (VALUE_TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                String valueType = tokenAttribute.getValue();
                keyBinding.setValueType(valueType);
            } else if (STRID.equalsIgnoreCase(attributeName)){
                String strid = tokenAttribute.getValue();
                keyBinding.setSTRID(strid);
            }else{
                log.log(Level.SEVERE,
                        "WSS0512.illegal.attribute.name",
                        new Object[]
                {attributeName, "xwss:X509Token"});
                throw new IllegalStateException(attributeName
                        + " is not a recognized attribute of X509Token");
            }
        }
    }
    
    
    /*
     */
    @SuppressWarnings("unchecked")
    private static void readOptionalTargetSettings(
            MessagePolicy requirements,
            Element optionalTargetSettings) throws XWSSecurityException {
        ArrayList targets = new ArrayList();
        Element eachSubElement = getFirstChildElement(optionalTargetSettings);
        while (eachSubElement != null) {
            QName subElementQName = getQName(eachSubElement);
            
            if (TARGET_QNAME.equals(subElementQName)) {
                Target t = new Target();
                t.setEnforce(false);
                Target t1 = readTargetSettings(eachSubElement, t);
                targets.add(t1);
            } else {
                log.log(Level.SEVERE,
                        "WSS0513.illegal.configuration.element",
                        subElementQName.toString());
                throw new IllegalStateException(subElementQName
                        + " is not a recognized sub-element of OptionalTargets");
            }
            eachSubElement = getNextElement(eachSubElement);
        }
        
        requirements.addOptionalTargets(targets);        
        //call iterator once to update the optional targets into policies
        requirements.iterator();
    }
    
    private static void readDecryptionSettings(
            EncryptionPolicy encryptionPolicy,
            Element encryptionSettings) {
        readEncryptionSettings(encryptionPolicy, encryptionSettings);
    }
    
    /*
     */
    private static void readEncryptionSettings(
            EncryptionPolicy encryptionPolicy,
            Element encryptionSettings) {
        
        
        String id = getIdAttribute(encryptionSettings);
        encryptionPolicy.setUUID(id);
        
        // read attributes
        NamedNodeMap encryptAttributes =
                encryptionSettings.getAttributes();
        int attributeCount = encryptAttributes.getLength();
        
        String attributeName = null;
        
        for (int index = 0; index < attributeCount; index++) {
            Attr encAttr =
                    (Attr)encryptAttributes.item(index);
            attributeName = encAttr.getName();
            
            if (ID_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                //do nothing
            } else  {
                log.log(Level.SEVERE,
                        "WSS0512.illegal.attribute.name",
                        new Object[]
                {attributeName, encryptionSettings.getTagName()});
                throw new IllegalStateException(attributeName
                        + " is not a recognized attribute of " +
                        encryptionSettings.getTagName());
            }
        }
        
        // read sub-elements
        int keyBearingTokensSeen = 0;
        
        Element eachSubElement = getFirstChildElement(encryptionSettings);
        while (eachSubElement != null) {
            QName subElementQName = getQName(eachSubElement);
            
            if (TARGET_QNAME.equals(subElementQName)) {
                EncryptionPolicy.FeatureBinding featureBinding =
                        (EncryptionPolicy.FeatureBinding)
                        encryptionPolicy.getFeatureBinding();
                featureBinding.addTargetBinding(
                        readTargetSettings(eachSubElement, false));
            } else if (X509TOKEN_ELEMENT_QNAME.equals(subElementQName)) {
                if (keyBearingTokensSeen > 0) {
                    log.log(Level.SEVERE,
                            "WSS0520.illegal.configuration.state");
                    throw new IllegalStateException(
                            "Atmost one of X509token/SymmetricKey/SAMLAssertion " +
                            " key bindings can be configured " +
                            "for an Encrypt/RequireEncryption operation");
                }
                keyBearingTokensSeen++;
                readX509TokenSettings(
                        (AuthenticationTokenPolicy.X509CertificateBinding)
                        encryptionPolicy.newX509CertificateKeyBinding(), eachSubElement);
            } else if (SYMMETRIC_KEY_ELEMENT_QNAME.equals(subElementQName)) {
                if (keyBearingTokensSeen > 0) {
                    log.log(Level.SEVERE,
                            "WSS0520.illegal.configuration.state");
                    throw new IllegalStateException(
                            "Atmost one of X509token/SymmetricKey/SAMLAssertion " +
                            " key bindings can be configured " +
                            "for an Encrypt/RequireEncryption operation");
                }
                keyBearingTokensSeen++;
                readSymmetricKeySettings(
                        (SymmetricKeyBinding) encryptionPolicy.newSymmetricKeyBinding(),
                        eachSubElement);
            } else if (SAML_ELEMENT_QNAME.equals(subElementQName)) {
                if (keyBearingTokensSeen > 0) {
                    log.log(Level.SEVERE,
                            "WSS0520.illegal.configuration.state");
                    throw new IllegalStateException(
                            "Atmost one of X509token/SymmetricKey/SAMLAssertion " +
                            " key bindings can be configured " +
                            "for an Encrypt/RequireEncryption operation");
                }
                keyBearingTokensSeen++;
                readSAMLTokenSettings(
                        (AuthenticationTokenPolicy.SAMLAssertionBinding)
                        encryptionPolicy.newSAMLAssertionKeyBinding(),
                        eachSubElement);
            } else if (ENCRYPTION_TARGET_ELEMENT_QNAME.equals(subElementQName)) {
                EncryptionPolicy.FeatureBinding featureBinding =
                        (EncryptionPolicy.FeatureBinding)
                        encryptionPolicy.getFeatureBinding();
                featureBinding.addTargetBinding(
                        readEncryptionTargetSettings(eachSubElement));
            } else if (KEY_ENCRYPTION_METHOD_ELEMENT_QNAME.equals(subElementQName)) {
                readKeyEncMethodSettings(encryptionPolicy, eachSubElement);
            } else if (DATA_ENCRYPTION_METHOD_ELEMENT_QNAME.equals(subElementQName)) {
                readDataEncMethodSettings(encryptionPolicy, eachSubElement);
            } else {
                log.log(Level.SEVERE,
                        "WSS0513.illegal.configuration.element",
                        subElementQName.toString());
                throw new IllegalStateException(subElementQName
                        + " is not a recognized sub-element of Encrypt/RequireEncryption");
            }
            eachSubElement = getNextElement(eachSubElement);
        }
        
    }
    
    private static void readKeyEncMethodSettings(
            EncryptionPolicy encryptionPolicy, Element keyEncSettings) {
        String algorithm =
                keyEncSettings.getAttribute(ALGORITHM_ATTRIBUTE_NAME);
        if ("".equals(algorithm)) {
            throw new IllegalArgumentException(
                    "Empty/Missing algorithm attribute on " +
                    keyEncSettings.getTagName());
            
        }
        checkCompatibility(algorithm, keyEncSettings);
        SecurityPolicy keyBinding = encryptionPolicy.getKeyBinding();
        if (keyBinding == null) {
            keyBinding =
                    encryptionPolicy.newX509CertificateKeyBinding();
            ((AuthenticationTokenPolicy.X509CertificateBinding)keyBinding).
                    setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
        }
        setKeyAlgorithm(keyBinding, algorithm);
    }
    
    private static void checkCompatibility(String keyEncAlgo, Element keyEncSettings) {
        if (MessageConstants.RSA_OAEP_KEY_TRANSPORT.equals(keyEncAlgo) ||
                MessageConstants.RSA_15_KEY_TRANSPORT.equals(keyEncAlgo)) {
           /* if (!hasX509Sibling(keyEncSettings)) {
                // log
                throw new IllegalStateException("Missing X509Token/SAML key association for " +
                KEY_ENCRYPTION_METHOD_ELEMENT_NAME + " " + keyEncAlgo);
            }*/
            if (hasSymmetricKeySibling(keyEncSettings)) {
                // log
                throw new IllegalStateException("Invalid SymmetricKey association specified for " +
                        KEY_ENCRYPTION_METHOD_ELEMENT_NAME + " " + keyEncAlgo +
                        ", required X509Token/SAML key association");
            }
        } else if (MessageConstants.TRIPLE_DES_KEY_WRAP.equals(keyEncAlgo) ||
                keyEncAlgo.startsWith("http://www.w3.org/2001/04/xmlenc#kw-aes")) {
            
            if (!hasSymmetricKeySibling(keyEncSettings)) {
                // log
                throw new IllegalStateException("Missing SymmetricKey association  for " +
                        KEY_ENCRYPTION_METHOD_ELEMENT_NAME + " " + keyEncAlgo);
            }
            
            if (hasX509Sibling(keyEncSettings)) {
                // log
                throw new IllegalStateException("Invalid X509Token/SAML key association specified for " +
                        KEY_ENCRYPTION_METHOD_ELEMENT_NAME + " " + keyEncAlgo +
                        ",  required SymmetricKey association");
            }
        } else {
            throw new IllegalArgumentException("Invalid/Unsupported Algorithm " + keyEncAlgo +
                    " specified for " + KEY_ENCRYPTION_METHOD_ELEMENT_NAME);
        }
    }
    
    //TODO : Add SAML support here later. An HOK assertion here can serve the same purpose
    private static  boolean hasX509Sibling(Element keyEncSettings) {
        Element parent = (Element)keyEncSettings.getParentNode();
        NodeList x509Nodes = parent.getElementsByTagNameNS(ConfigurationConstants.CONFIGURATION_URL, X509TOKEN_ELEMENT_NAME);
        if(x509Nodes.getLength() > 0) {
            return true;
        }
        return false;
    }
    
    private static boolean hasSymmetricKeySibling(Element keyEncSettings) {
        Element parent = (Element)keyEncSettings.getParentNode();
        NodeList symKeyNodes = parent.getElementsByTagNameNS(ConfigurationConstants.CONFIGURATION_URL, SYMMETRIC_KEY_ELEMENT_NAME);
        if(symKeyNodes.getLength() > 0) {
            return true;
        }
        return false;
    }
    
    private static void setDefaultKeyAlgorithm(SecurityPolicy keyBinding, String algorithm) {
        
        if (PolicyTypeUtil.samlTokenPolicy(keyBinding)) {
            AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding =
                    (AuthenticationTokenPolicy.SAMLAssertionBinding)keyBinding;
            if ("".equals(samlBinding.getKeyAlgorithm()))
                samlBinding.setKeyAlgorithm(algorithm);
        } else if (PolicyTypeUtil.x509CertificateBinding(keyBinding)) {
            AuthenticationTokenPolicy.X509CertificateBinding x509Binding =
                    (AuthenticationTokenPolicy.X509CertificateBinding)keyBinding;
            if ("".equals( x509Binding.getKeyAlgorithm()))
                x509Binding.setKeyAlgorithm(algorithm);
        } else if (PolicyTypeUtil.symmetricKeyBinding(keyBinding)) {
            SymmetricKeyBinding symBinding = (SymmetricKeyBinding)keyBinding;
            if ("".equals(symBinding.getKeyAlgorithm()))
                symBinding.setKeyAlgorithm(algorithm);
        } else {
            throw new IllegalArgumentException("Unknown Key Type " + keyBinding.getClass().getName());
        }
    }
    
    private static void setKeyAlgorithm(SecurityPolicy keyBinding, String algorithm) {
        
        if (PolicyTypeUtil.samlTokenPolicy(keyBinding)) {
            AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding =
                    (AuthenticationTokenPolicy.SAMLAssertionBinding)keyBinding;
            samlBinding.setKeyAlgorithm(algorithm);
        } else if (PolicyTypeUtil.x509CertificateBinding(keyBinding)) {
            AuthenticationTokenPolicy.X509CertificateBinding x509Binding =
                    (AuthenticationTokenPolicy.X509CertificateBinding)keyBinding;
            x509Binding.setKeyAlgorithm(algorithm);
           if(MessageConstants.HMAC_SHA1_SIGMETHOD.equals(algorithm)){ 
                String certAlias = x509Binding.getCertificateIdentifier();
                if(certAlias == null || certAlias.equals("")){
                    throw new IllegalArgumentException("The certificate Alias should be set when algorithm is:" + algorithm);
                }
            }
        } else if (PolicyTypeUtil.symmetricKeyBinding(keyBinding)) {
            SymmetricKeyBinding symBinding = (SymmetricKeyBinding)keyBinding;
            symBinding.setKeyAlgorithm(algorithm);
        } else {
            throw new IllegalArgumentException("Unknown Key Type " + keyBinding.getClass().getName());
        }
    }
    
    private static void readDataEncMethodSettings(
            EncryptionPolicy encryptionPolicy, Element dataEncSettings) {
        String algorithm =
                dataEncSettings.getAttribute(ALGORITHM_ATTRIBUTE_NAME);
        if ("".equals(algorithm)) {
            throw new IllegalArgumentException(
                    "Empty/Missing algorithm attribute on " +
                    dataEncSettings.getTagName());
            
        }
        EncryptionPolicy.FeatureBinding featureBinding =
                (EncryptionPolicy.FeatureBinding)
                encryptionPolicy.getFeatureBinding();
        featureBinding.setDataEncryptionAlgorithm(algorithm);
    }
    
    private static void readCanonMethodSettings(
            SignaturePolicy signaturePolicy, Element canonSettings) {
        String algorithm =
                canonSettings.getAttribute(ALGORITHM_ATTRIBUTE_NAME);
        boolean disableInclusivePrefix = false;
       try{
           disableInclusivePrefix = parseBoolean(DISABLE_INCLUSIVE_PREFIX, canonSettings.getAttribute(DISABLE_INCLUSIVE_PREFIX));
       } catch(Exception e){
           e.printStackTrace();
       }
        if ("".equals(algorithm)) {
            throw new IllegalArgumentException(
                    "Empty/Missing algorithm attribute on " +
                    canonSettings.getTagName());
            
        }
        SignaturePolicy.FeatureBinding featureBinding =
                (SignaturePolicy.FeatureBinding)
                signaturePolicy.getFeatureBinding();
        featureBinding.setCanonicalizationAlgorithm(algorithm);
        featureBinding.setDisbaleInclusivePrefix(disableInclusivePrefix);
    }
    
    private static void readSigMethodSettings(
            SignaturePolicy signaturePolicy, Element sigMethodSettings) {
        String algorithm =
                sigMethodSettings.getAttribute(ALGORITHM_ATTRIBUTE_NAME);
        if ("".equals(algorithm)) {
            throw new IllegalArgumentException(
                    "Empty/Missing algorithm attribute on " +
                    sigMethodSettings.getTagName());
            
        }
        
        SecurityPolicy keyBinding = signaturePolicy.getKeyBinding();
        if (keyBinding == null) {
            keyBinding =
                    signaturePolicy.newX509CertificateKeyBinding();
            ((AuthenticationTokenPolicy.X509CertificateBinding)keyBinding).
                    setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
        }
        setKeyAlgorithm(keyBinding, algorithm);
    }
    
    private static QName getQName(Node element) {
        return new QName(element.getNamespaceURI(), element.getLocalName());
    }
    
    private static Element getFirstChildElement(Node node) {
        Node nextSibling = node.getFirstChild();
        while (nextSibling != null) {
            if (nextSibling instanceof Element) {
                break;
            }
            nextSibling = nextSibling.getNextSibling();
        }
        return (Element) nextSibling;
    }
    
    private static Element getNextElement(Node node) {
        Node nextSibling = node;
        while (nextSibling != null) {
            nextSibling = nextSibling.getNextSibling();
            if (nextSibling instanceof Element) {
                break;
            }
        }
        return (Element) nextSibling;
    }
    
    private static class ErrorHandler extends DefaultHandler {
        PrintStream out;
        
        public ErrorHandler(PrintStream out) {
            this.out = out;
        }
        
        public void error(SAXParseException e) throws SAXException {
            if (out != null)
                out.println(e);
            throw e;
        }
        
        public void warning(SAXParseException e) throws SAXException {
            if (out != null)
                out.println(e);
            else
                ;// log
        }
        
        public void fatalError(SAXParseException e) throws SAXException {
            if (out != null)
                out.println(e);
            throw e;
        }
    }
    
    private static boolean parseBoolean(String attr, String value) throws Exception {
        if ("1".equals(value) || "true".equalsIgnoreCase(value)) {
            return true;
        } else if ("0".equals(value) || "false".equalsIgnoreCase(value)) {
            return false;
        } else {
            log.log(Level.SEVERE, "WSS0511.illegal.boolean.value", value);
            throw new Exception(
                    "Boolean attribute " + attr +
                    " has value other than 'true' or 'false'");
        }
    }
    
    
    private static long parseLong(String str) {
        if (!"".equals(str)) {
            String ret = str;
            int idx = str.indexOf(".");
            if (idx > 0) {
                ret = str.substring(0, idx);
            }
            return Long.parseLong(ret);
        }
        return 0;
    }
    
    private static void readTimestampSettings(
            TimestampPolicy policy, Element timestampSettings) {
        String id = getIdAttribute(timestampSettings);
        policy.setUUID(id);
        String timeout = timestampSettings.getAttribute(TIMEOUT_ATTRIBUTE_NAME);
        policy.setTimeout(parseLong(timeout) * 1000);
        
        
        Element someElement = getFirstChildElement(timestampSettings);
        if (someElement != null) {
            log.log(Level.SEVERE,
                    "WSS0513.illegal.configuration.element",
                    getQName(someElement));
            throw new IllegalStateException(getQName(someElement)
            + " is not a recognized sub-element of Timestamp");
        }
    }
    
    private static void readTimestampRequirementSettings(
            TimestampPolicy policy, Element timestampSettings) {
        String id = getIdAttribute(timestampSettings);
        policy.setUUID(id);
        
        String maxClockSkew = timestampSettings.getAttribute(MAX_CLOCK_SKEW);
        String timestampFreshness = timestampSettings.getAttribute(TIMESTAMP_FRESHNESS_LIMIT);
        
        //set them on the policy
        policy.setMaxClockSkew(parseLong(maxClockSkew) * 1000);
        policy.setTimestampFreshness(parseLong(timestampFreshness) * 1000);
        
        Element someElement = getFirstChildElement(timestampSettings);
        if (someElement != null) {
            log.log(Level.SEVERE,
                    "WSS0513.illegal.configuration.element",
                    getQName(someElement));
            throw new IllegalStateException(getQName(someElement)
            + " is not a recognized sub-element of RequireTimestamp");
        }
    }    
    
    /*
     */
    private static void readUsernamePasswordSettings(
            AuthenticationTokenPolicy.UsernameTokenBinding utBinding,
            Element usernamePasswordSettings) {
        
        String id = getIdAttribute(usernamePasswordSettings);
        utBinding.setUUID(id);
        
        NamedNodeMap usernameAttributes =
                usernamePasswordSettings.getAttributes();
        int attributeCount = usernameAttributes.getLength();
        
        String attributeName = null;
        
        for (int index = 0; index < attributeCount; index++) {
            Attr usernamePasswordAttribute =
                    (Attr) usernameAttributes.item(index);
            
            attributeName = usernamePasswordAttribute.getName();
            if (ID_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                utBinding.setUUID(usernamePasswordAttribute.getValue());
            } else if (USERNAME_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                utBinding.setUsername(usernamePasswordAttribute.getValue());
            } else if (PASSWORD_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                utBinding.setPassword(usernamePasswordAttribute.getValue());
            } else if (USE_NONCE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                utBinding.setUseNonce(getBooleanValue(
                        usernamePasswordAttribute.getValue()));
            } else if (DIGEST_PASSWORD_ATTRIBUTE_NAME
                    .equalsIgnoreCase(attributeName)) {
                utBinding.setDigestOn(getBooleanValue(
                        usernamePasswordAttribute.getValue()));
            } else {
                log.log(Level.SEVERE,
                        "WSS0512.illegal.attribute.name",
                        new Object[]
                {attributeName, usernamePasswordSettings.getTagName()});
                throw new IllegalStateException(attributeName
                        + " is not a recognized attribute of UsernameToken");
            }
            
        }
        if ( utBinding.getDigestOn() && !utBinding.getUseNonce() ) {
            throw new IllegalStateException("useNonce attribute must be true if digestPassword is true");
        }
        
        Element someElement = getFirstChildElement(usernamePasswordSettings);
        if (someElement != null) {
            log.log(Level.SEVERE,
                    "WSS0513.illegal.configuration.element",
                    getQName(someElement));
            throw new IllegalStateException(getQName(someElement)
            + " is not a recognized sub-element of UsernameToken");
        }
    }
    
    /*
     */
    private static void readUsernamePasswordRequirementSettings(
            AuthenticationTokenPolicy.UsernameTokenBinding utBinding,
            Element authenticateUserSettings) {
        
        String id = getIdAttribute(authenticateUserSettings);
        utBinding.setUUID(id);
        
        //set them on the policy
        TimestampPolicy tPolicy = null;
        try {
            tPolicy = (TimestampPolicy) utBinding.newTimestampFeatureBinding();
        } catch(Exception e) {
            //log
            throw new IllegalStateException(e.getMessage());
        }
        
        NamedNodeMap authenticateUserAttributes = authenticateUserSettings.getAttributes();
        int attributeCount = authenticateUserAttributes.getLength();
        String attributeName = null;
        
        for (int index = 0; index < attributeCount; index++) {
            Attr authenticateUserAttribute = (Attr) authenticateUserAttributes.item(index);
            attributeName = authenticateUserAttribute.getName();
            
            if (ID_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                // do nothing
            }  else if (NONCE_REQUIRED_ATTRIBUTE_NAME
                    .equalsIgnoreCase(attributeName)) {
                utBinding.setUseNonce(
                        getBooleanValue(authenticateUserAttribute.getValue()));
            } else if (PASSWORD_DIGEST_REQUIRED_ATTRIBUTE_NAME
                    .equalsIgnoreCase(attributeName)) {
                utBinding.setDigestOn(
                        getBooleanValue(authenticateUserAttribute.getValue()));
            } else if (MAX_CLOCK_SKEW.equalsIgnoreCase(attributeName)) {
                tPolicy.setMaxClockSkew(parseLong(authenticateUserAttribute.getValue()) * 1000);
            } else if (TIMESTAMP_FRESHNESS_LIMIT.equalsIgnoreCase(attributeName)) {
                tPolicy.setTimestampFreshness(parseLong(authenticateUserAttribute.getValue()) * 1000);
            } else if (MAX_NONCE_AGE.equalsIgnoreCase(attributeName)) {
                utBinding.setMaxNonceAge(parseLong(authenticateUserAttribute.getValue()) * 1000);
            } else {
                log.log(Level.SEVERE,
                        "WSS0512.illegal.attribute.name",
                        new Object[]
                {attributeName, "xwss:RequireUsernameToken"});
                throw new IllegalStateException(attributeName
                        + " is not a recognized attribute of RequireUsernameToken");
            }
        }
        
        Element someElement = getFirstChildElement(authenticateUserSettings);
        if (someElement != null) {
            log.log(Level.SEVERE,
                    "WSS0513.illegal.configuration.element",
                    getQName(someElement));
            throw new IllegalStateException(getQName(someElement)
            + " is not a recognized sub-element of RequireUsernameToken");
        }
        
        if ( utBinding.getDigestOn() && !utBinding.getUseNonce() ) {
            throw new IllegalStateException("nonceRequired attribute must be true if passwordDigestRequired is true");
        }
    }
    
    private static void readSAMLTokenSettings(
            AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding,
            Element samlTokenSettings) {
        
        String id = getIdAttribute(samlTokenSettings);
        samlBinding.setUUID(id);
        
        String type = samlTokenSettings.getAttribute(SAML_ASSERTION_TYPE_ATTRIBUTE_NAME);
        validateSAMLType(type, samlTokenSettings);
        
        NamedNodeMap samlAttributes = samlTokenSettings.getAttributes();
        int attributeCount = samlAttributes.getLength();
        String attributeName = null;
        
        for (int index = 0; index < attributeCount; index++) {
            Attr samlAttribute = (Attr) samlAttributes.item(index);
            
            attributeName = samlAttribute.getName();
            
            if (ID_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                // do nothing
            }  else if (SAML_ASSERTION_TYPE_ATTRIBUTE_NAME
                    .equalsIgnoreCase(attributeName)) {
                samlBinding.setAssertionType(samlAttribute.getValue());
            } else if (SAML_AUTHORITY_ID_ATTRIBUTE_NAME.
                    equalsIgnoreCase(attributeName)) {
                samlBinding.setAuthorityIdentifier(samlAttribute.getValue());
            } else if (SAML_KEYIDENTIFIER_ATTRIBUTE_NAME.
                    equalsIgnoreCase(attributeName)) {
                samlBinding.setKeyIdentifier(samlAttribute.getValue());
            } else if (KEY_REFERENCE_TYPE_ATTRIBUTE_NAME.
                    equalsIgnoreCase(attributeName)) {
                String attributeValue = samlAttribute.getValue();
                validateSAMLKeyReferenceType(attributeValue);
                samlBinding.setReferenceType(attributeValue);
            } else if (STRID.equalsIgnoreCase(attributeName)){
                String strid = samlAttribute.getValue();
                samlBinding.setSTRID(strid);
            } else {
                log.log(Level.SEVERE,
                        "WSS0512.illegal.attribute.name",
                        new Object[]
                {attributeName, "xwss:SAMLAssertion"});
                throw new IllegalStateException(attributeName
                        + " is not a recognized attribute of SAMLAssertion");
            }
        }
    }
    
    
    private static void readRequireSAMLTokenSettings(
            AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding,
            Element samlTokenSettings) {
        
        String id = getIdAttribute(samlTokenSettings);
        samlBinding.setUUID(id);
        
        String type = samlTokenSettings.getAttribute(SAML_ASSERTION_TYPE_ATTRIBUTE_NAME);
        validateRequireSAMLType(type, samlTokenSettings);
        
        NamedNodeMap samlAttributes = samlTokenSettings.getAttributes();
        int attributeCount = samlAttributes.getLength();
        String attributeName = null;
        
        for (int index = 0; index < attributeCount; index++) {
            Attr samlAttribute = (Attr) samlAttributes.item(index);
            
            attributeName = samlAttribute.getName();
            
            if (ID_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                // do nothing
            } else if (SAML_ASSERTION_TYPE_ATTRIBUTE_NAME
                    .equalsIgnoreCase(attributeName)) {
                samlBinding.setAssertionType(samlAttribute.getValue());
            } else if (SAML_AUTHORITY_ID_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                samlBinding.setAuthorityIdentifier(samlAttribute.getValue());
            } else if (KEY_REFERENCE_TYPE_ATTRIBUTE_NAME.
                    equalsIgnoreCase(attributeName)) {
                String attributeValue = samlAttribute.getValue();
                validateSAMLKeyReferenceType(attributeValue);
                samlBinding.setReferenceType(attributeValue);
            } else if (STRID.equalsIgnoreCase(attributeName)){
                String strid = samlAttribute.getValue();
                samlBinding.setSTRID(strid);
            } else {
                log.log(Level.SEVERE,
                        "WSS0512.illegal.attribute.name",
                        new Object[]
                {attributeName, "xwss:RequireSAMLAssertion"});
                throw new IllegalStateException(attributeName
                        + " is not a recognized attribute of RequireSAMLAssertion");
            }
        }
    }
    
    
    /*
     */
    private static EncryptionTarget readEncryptionTargetSettings(
            Element targetSettings) {
        EncryptionTarget target = new EncryptionTarget();
        
        // Read-in the target type attribute
        NamedNodeMap targetAttributes = targetSettings.getAttributes();
        int attributeCount = targetAttributes.getLength();
        String attributeName = null;
        for (int index = 0; index < attributeCount; index++) {
            Attr targetAttribute = (Attr) targetAttributes.item(index);
            attributeName = targetAttribute.getName();
            
            if (TARGET_TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                String targetType = targetAttribute.getValue();
                // valid values of targetType are xpath, qname, id
                if (Target.TARGET_TYPE_VALUE_QNAME.equalsIgnoreCase(targetType)){
                    target.setType(Target.TARGET_TYPE_VALUE_QNAME);
                }else if(Target.TARGET_TYPE_VALUE_XPATH.equalsIgnoreCase(targetType)){
                    target.setType(Target.TARGET_TYPE_VALUE_XPATH);
                }else if(Target.TARGET_TYPE_VALUE_URI.equalsIgnoreCase(targetType)) {
                    target.setType(Target.TARGET_TYPE_VALUE_URI);
                } else {
                    log.log(Level.SEVERE,
                            "WSS0519.illegal.attribute.value",
                            "xwss:Target@Type");
                    throw new IllegalStateException(targetType
                            + " is not a recognized type of Target");
                }
            } else if
                    (CONTENT_ONLY_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                
                String contentOnly = targetAttribute.getValue();
                target.setContentOnly(getBooleanValue(contentOnly));
                
            } else if
                    (ENFORCE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                String enforce_S = targetAttribute.getValue();
                boolean enforce = Boolean.valueOf(enforce_S);
                target.setEnforce(enforce);
            } else if
                    (VALUE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                
            } else {
                log.log(Level.SEVERE,
                        "WSS0512.illegal.attribute.name",
                        new Object[]
                {attributeName, "xwss:Target"});
                throw new IllegalStateException(attributeName
                        + " is not a recognized attribute of Target");
            }
        }
        
        // Read-in the target type attribute
        //read value attribute
        String targetValue = targetSettings.getAttribute(VALUE_ATTRIBUTE_NAME);
        if (targetValue == null ) {
            //|| targetValue.equals("")) {
            // log
            throw new IllegalStateException(
                "value attribute of the EncryptionTarget element missing/empty");
        }
                
        if (targetValue.startsWith("#"))
            targetValue = targetValue.substring(1);
                
                
        target.setValue(targetValue);
        
        //read any transform child elements
        Element eachDefinitionElement = getFirstChildElement(targetSettings);
        while (eachDefinitionElement != null) {
            QName definitionType = getQName(eachDefinitionElement);
            if (TRANSFORM_ELEMENT_QNAME.equals(definitionType)) {
                EncryptionTarget.Transform transform =
                        readEncTransform(eachDefinitionElement);
                target.addCipherReferenceTransform(transform);
            } else {
                log.log(
                        Level.SEVERE,
                        "WSS0513.illegal.configuration.element",
                        definitionType.toString());
                throw new IllegalStateException(definitionType +
                        " is not a recognized sub-element of EncryptionTarget");
            }
            eachDefinitionElement = getNextElement(eachDefinitionElement);
        }
        
        return target;
    }
    
    private static Target readTargetSettings(Element targetSettings, boolean signature) {
        if (signature) {
            SignatureTarget target = new SignatureTarget();
            target.setDigestAlgorithm(DigestMethod.SHA1); //SHA1
            return readTargetSettings(targetSettings, target);
        } else {
            EncryptionTarget target = new EncryptionTarget();
            return readTargetSettings(targetSettings, target);
        }
    }
    
    /*
     */
    private static Target readTargetSettings(Element targetSettings, Target target) {
        
        // Read-in the target type attribute
        NamedNodeMap targetAttributes = targetSettings.getAttributes();
        int attributeCount = targetAttributes.getLength();
        String attributeName = null;
        for (int index = 0; index < attributeCount; index++) {
            Attr targetAttribute = (Attr) targetAttributes.item(index);
            attributeName = targetAttribute.getName();
            
            if (TARGET_TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                String targetType = targetAttribute.getValue();
                // valid values of targetType are xpath, qname, id
                if (Target.TARGET_TYPE_VALUE_QNAME.equalsIgnoreCase(targetType)){
                    target.setType(Target.TARGET_TYPE_VALUE_QNAME);
                }else if(Target.TARGET_TYPE_VALUE_XPATH.equalsIgnoreCase(targetType)){
                    target.setType(Target.TARGET_TYPE_VALUE_XPATH);
                }else if(Target.TARGET_TYPE_VALUE_URI.equalsIgnoreCase(targetType)) {
                    target.setType(Target.TARGET_TYPE_VALUE_URI);
                } else {
                    log.log(Level.SEVERE,
                            "WSS0519.illegal.attribute.value",
                            "xwss:Target@Type");
                    throw new IllegalStateException(targetType
                            + " is not a recognized type of Target");
                }
            } else if
                    (CONTENT_ONLY_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                if (targetAttribute.getSpecified()) {
                    validateTargetContentOnly(targetSettings);
                }
                String contentOnly = targetAttribute.getValue();
                target.setContentOnly(getBooleanValue(contentOnly));
            } else if
                    (ENFORCE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                String enforce_S = targetAttribute.getValue();
                boolean enforce = getBooleanValue(enforce_S);
                Node parent = targetSettings.getParentNode();
                if (OPTIONAL_TARGETS_ELEMENT_NAME.equals(parent.getLocalName())) {
                    if (targetAttribute.getSpecified() && enforce) {
                        log.warning("WSS0760.warning.optionaltarget.enforce.ignored");
                    }
                } else {
                    target.setEnforce(enforce);
                }
            } else {
                log.log(Level.SEVERE,
                        "WSS0512.illegal.attribute.name",
                        new Object[]
                {attributeName, "xwss:Target"});
                throw new IllegalStateException(attributeName
                        + " is not a recognized attribute of Target");
            }
        }
        
        // Read-in the target value
        String targetValue = XMLUtil.getFullTextFromChildren(targetSettings);
        
        if (targetValue == null || targetValue.equals("")) {
            // log
            throw new IllegalStateException(
                    "Value of the Target element is required to be specified");
        }
        
        // ignore the proceeding # in case this is a xpointer uri
        if (targetValue.startsWith("#")) {
            targetValue = targetValue.substring(1);
        }
        
        target.setValue(targetValue);
        
        return target;
    }
    
    /*
     */
    private static SignatureTarget readSignatureTargetSettings(
            Element targetSettings) {
        SignatureTarget target = new SignatureTarget();
            
        // Read-in the target type attribute
        NamedNodeMap targetAttributes = targetSettings.getAttributes();
        int attributeCount = targetAttributes.getLength();
        String attributeName = null;
        for (int index = 0; index < attributeCount; index++) {
            Attr targetAttribute = (Attr) targetAttributes.item(index);
            attributeName = targetAttribute.getName();
            
            if (TARGET_TYPE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                String targetType = targetAttribute.getValue();
                // valid values of targetType are xpath, qname, id
                if (Target.TARGET_TYPE_VALUE_QNAME.equalsIgnoreCase(targetType)){
                    target.setType(Target.TARGET_TYPE_VALUE_QNAME);
                }else if(Target.TARGET_TYPE_VALUE_XPATH.equalsIgnoreCase(targetType)){
                    target.setType(Target.TARGET_TYPE_VALUE_XPATH);
                }else if(Target.TARGET_TYPE_VALUE_URI.equalsIgnoreCase(targetType)) {
                    target.setType(Target.TARGET_TYPE_VALUE_URI);
                } else {
                    log.log(Level.SEVERE,
                            "WSS0519.illegal.attribute.value",
                            "xwss:Target@Type");
                    throw new IllegalStateException(targetType
                            + " is not a recognized type of Target");
                }
            } else if
                    (CONTENT_ONLY_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                if (targetAttribute.getSpecified()) {
                    throw new IllegalStateException(
                            "invalid contentOnly attribute in a xwss:SignatureTarget");
                }
                
                /* we could check if the reference is to an attachement
                 * and add a Transform
                String contentOnly = targetAttribute.getValue();
                validateContentOnly(targetSettings);
                boolean contentValue = getBooleanValue(contentOnly);
                SignatureTarget.Transform transform =  null;
                if (contentValue) {
                    transform = new SignatureTarget.Transform(
                    MessageConstants.ATTACHMENT_CONTENT_ONLY_TRANSFORM_URI);
                } else {
                    transform = new SignatureTarget.Transform(
                    MessageConstants.ATTACHMENT_COMPLETE_TRANSFORM_URI);
                }
                target.addTransform(transform);
                 */
                
            } else if
                    (ENFORCE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)) {
                String enforce_S = targetAttribute.getValue();
                boolean enforce = getBooleanValue(enforce_S);
                target.setEnforce(enforce);
            }else if(VALUE_ATTRIBUTE_NAME.equalsIgnoreCase(attributeName)){
                
            }else {
                log.log(Level.SEVERE,
                        "WSS0512.illegal.attribute.name",
                        new Object[]
                {attributeName, "xwss:Target"});
                throw new IllegalStateException(attributeName
                        + " is not a recognized attribute of Target");
            }
        }
        //read value attribute
        String targetValue = targetSettings.getAttribute(VALUE_ATTRIBUTE_NAME);
        if (targetValue == null ){
            //|| targetValue.equals("")) {
            // log
            throw new IllegalStateException(
                    "value attribute of the SignatureTarget element missing/empty");
        }
        target.setValue(targetValue);
        
        //read the DigestMethod child
        boolean attachmentTxSeen = false;
        Element eachDefinitionElement = getFirstChildElement(targetSettings);
        while (eachDefinitionElement != null) {
            QName definitionType = getQName(eachDefinitionElement);
            if (DIGEST_METHOD_ELEMENT_QNAME.equals(definitionType)) {
                String algorithm = readDigestMethod(eachDefinitionElement);
                target.setDigestAlgorithm(algorithm);
            }else if (TRANSFORM_ELEMENT_QNAME.equals(definitionType)) {
                SignatureTarget.Transform transform =
                        readSigTransform(eachDefinitionElement);
                if (transform.getTransform().equals(
                        MessageConstants.ATTACHMENT_CONTENT_ONLY_TRANSFORM_URI) ||
                        transform.getTransform().equals(
                        MessageConstants.ATTACHMENT_COMPLETE_TRANSFORM_URI)) {
                    attachmentTxSeen = true;
                }
                target.addTransform(transform);
            } else {
                log.log(
                        Level.SEVERE,
                        "WSS0513.illegal.configuration.element",
                        definitionType.toString());
                throw new IllegalStateException(definitionType +
                        " is not a recognized sub-element of SignatureTarget");
            }
            eachDefinitionElement = getNextElement(eachDefinitionElement);
        }
        if ("".equals(target.getDigestAlgorithm())) {
            target.setDigestAlgorithm(MessageConstants.SHA1_DIGEST);
        }
        if (target.getValue().startsWith("cid") ||
                target.getValue().startsWith("CID") ||
                target.getValue().startsWith(MessageConstants.ATTACHMENTREF)) {
            if (!attachmentTxSeen) {
                throw new IllegalStateException("Missing Transform specification for Attachment Target " + target.getValue());
            }
        }
        
        
        return target;
    }
    
    private static String readDigestMethod(Element digestMethod) {
        String algorithm = digestMethod.getAttribute(ALGORITHM_ATTRIBUTE_NAME);
        if ("".equals(algorithm)) {
            throw new IllegalArgumentException(
                    "Empty/missing algorithm attribute on SignatureTarget");
        }
        return algorithm;
    }
    
    private static SignatureTarget.Transform readSigTransform(Element transform) {
        
        
        String algorithm = transform.getAttribute(ALGORITHM_ATTRIBUTE_NAME);
        boolean disableInclusivePrefix = false;
        try{
           disableInclusivePrefix = parseBoolean(DISABLE_INCLUSIVE_PREFIX, transform.getAttribute(DISABLE_INCLUSIVE_PREFIX));
       } catch(Exception e){
           e.printStackTrace();
       }
        if ("".equals(algorithm)) {
            // log
            throw new IllegalStateException(
                    " Empty/Missing algorithm attribute on xwss:Transform element");
        }
        
        Element eachDefinitionElement = getFirstChildElement(transform);
        //HashMap props = new HashMap();
        SignatureTarget.Transform trans = new SignatureTarget.Transform();
        trans.setTransform(algorithm);
        trans.setDisbaleInclusivePrefix(disableInclusivePrefix);
        
        if(algorithm.equals(Transform.XPATH )){
            fillXPATHTransformParams(eachDefinitionElement, trans);
        }else if(algorithm.equals(Transform.XPATH2 )){
            fillXPATH2TransformParams(eachDefinitionElement, trans);
        }else if(algorithm.equals(MessageConstants.STR_TRANSFORM_URI)){
            fillSTRTransformParams(eachDefinitionElement, trans);
        }else {
            if(log.getLevel()== Level.FINE){
                log.log(Level.FINE,"Algorithm Parameters not supported" +
                        "for transform",algorithm);
            }
        }             
                
        return trans;
    }
    
    
    private static void fillXPATHTransformParams(Element algoElement , SignatureTarget.Transform transform){
        QName definitionType = getQName(algoElement);
        if (ALGORITHM_PARAMETER_ELEMENT_QNAME.equals(definitionType)) {
            String name = algoElement.getAttribute(NAME_ATTRIBUTE_NAME);
            String value = algoElement.getAttribute(VALUE_ATTRIBUTE_NAME);
            
            if(name.equals("XPATH")){
                transform.setAlgorithmParameters(new XPathFilterParameterSpec(value));
            }else{
                throw new IllegalStateException("XPATH Transform must have XPATH attribute"
                        +" name and an XPATH Expression as value");
            }
        }else {
            log.log(
                    Level.SEVERE,
                    "WSS0513.illegal.configuration.element",
                    definitionType.toString());
            throw new IllegalStateException(definitionType +
                    " is not a recognized sub-element of Transform");
        }
        return;
    }
    @SuppressWarnings("unchecked")
    private static void fillXPATH2TransformParams(Element algoElement , SignatureTarget.Transform transform){
        
        ArrayList xpathTypeList = new ArrayList();
        while (algoElement != null) {
            QName definitionType = getQName(algoElement);
            if (ALGORITHM_PARAMETER_ELEMENT_QNAME.equals(definitionType)) {
                
                
                String name = algoElement.getAttribute(NAME_ATTRIBUTE_NAME);
                String value = algoElement.getAttribute(VALUE_ATTRIBUTE_NAME);
                if(name.equalsIgnoreCase("UNION")){
                    xpathTypeList.add(new XPathType(value,XPathType.Filter.UNION));
                }else if(name.equalsIgnoreCase("INTERSECT")){
                    xpathTypeList.add(new XPathType(value,XPathType.Filter.INTERSECT));
                }else if(name.equalsIgnoreCase("SUBTRACT")){
                    xpathTypeList.add(new XPathType(value,XPathType.Filter.SUBTRACT));
                }else{
                    throw new IllegalStateException("XPATH2 Transform AlgorithmParameter name attribute"
                            +" should be one of UNION,INTERSECT,SUBTRACT");
                }
                
            } else {
                log.log(
                        Level.SEVERE,
                        "WSS0513.illegal.configuration.element",
                        definitionType.toString());
                throw new IllegalStateException(definitionType +
                        " is not a recognized sub-element of Transform");
            }
            algoElement = getNextElement(algoElement);
            
        }
        transform.setAlgorithmParameters(new XPathFilter2ParameterSpec(xpathTypeList));
        return;
    }
    
    private static void fillSTRTransformParams(Element algoElement , SignatureTarget.Transform transform){
        QName definitionType = getQName(algoElement);
        if (ALGORITHM_PARAMETER_ELEMENT_QNAME.equals(definitionType)) {
            String name = algoElement.getAttribute(NAME_ATTRIBUTE_NAME);
            String value = algoElement.getAttribute(VALUE_ATTRIBUTE_NAME);
            transform.setAlgorithmParameters(new Parameter(name,value));
        }else {
            log.log(
                    Level.SEVERE,
                    "WSS0513.illegal.configuration.element",
                    definitionType.toString());
            throw new IllegalStateException(definitionType +
                    " is not a recognized sub-element of Transform");
        }
        return;
    }
    
    private static EncryptionTarget.Transform readEncTransform(
            Element transform) {
        
        String algorithm = transform.getAttribute(ALGORITHM_ATTRIBUTE_NAME);
        if ("".equals(algorithm)) {
            // log
            throw new IllegalStateException(
                    " Empty/Missing algorithm attribute on xwss:Transform element");
        }
        
        //Element eachDefinitionElement = getFirstChildElement(transform);
       /* HashMap props = new HashMap();
        while (eachDefinitionElement != null) {
            QName definitionType = getQName(eachDefinitionElement);
            if (ALGORITHM_PARAMETER_ELEMENT_QNAME.equals(definitionType)) {
                readAlgorithmProperties(props, eachDefinitionElement);
            } else {
                log.log(
                        Level.SEVERE,
                        "WSS0513.illegal.configuration.element",
                        definitionType.toString());
                throw new IllegalStateException(definitionType +
                        " is not a recognized sub-element of Transform");
            }
            eachDefinitionElement = getNextElement(eachDefinitionElement);
        }
        */
        EncryptionTarget.Transform trans = new EncryptionTarget.Transform();
        trans.setTransform(algorithm);
        //trans.setAlgorithmParameters(props);
        return trans;
    }
    
    
    private static void validateContentOnly(Element target) {
        
        Node parent = target.getParentNode();
        String parentName = parent.getLocalName();
        
        if (SIGNATURE_REQUIREMENT_ELEMENT_NAME.equalsIgnoreCase(parentName) ||
                SIGN_OPERATION_ELEMENT_NAME.equalsIgnoreCase(parentName)) {
            String targetValue = target.getAttribute(VALUE_ATTRIBUTE_NAME);
            String targetType = target.getAttribute(TARGET_TYPE_ATTRIBUTE_NAME);
            if (URI_TARGET.equalsIgnoreCase(targetType)) {
                if (!targetValue.startsWith("cid") &&
                        !targetValue.startsWith("CID")) {
                    throw new IllegalStateException(
                            "invalid contentOnly attribute on a non-attachment " +
                            "SignatureTarget");
                }
            } else {
                throw new IllegalStateException(
                        "invalid contentOnly attribute in a SignatureTarget");
            }
        }
        
        if (!ENCRYPT_OPERATION_ELEMENT_NAME.equalsIgnoreCase(parentName) &&
                !ENCRYPTION_REQUIREMENT_ELEMENT_NAME.
                equalsIgnoreCase(parentName)) {
            throw new IllegalStateException(
                    "contentOnly attribute not allowed on Targets under element " +
                    parentName);
        }
    }
    
    private static void validateSAMLKeyReferenceType(String typeName) {
        if (!MessageConstants.KEY_INDETIFIER_TYPE.equalsIgnoreCase(typeName) &&
                !MessageConstants.EMBEDDED_REFERENCE_TYPE.equalsIgnoreCase(typeName)) {
            throw new IllegalStateException(
                    "Reference Type " + typeName +
                    " not allowed for SAMLAssertion References");
        }
        
    }
    
    private static void validateRequireSAMLType(String type, Element samlTokenSettings) {
        if (!SV_SAML_TYPE.equals(type)) {
            throw new IllegalStateException(
                    "Allowed Assertion Types for <xwss:RequireSAMLAssertion> is SV only");
        }
        
        // the parent node should be a <SecurityConfiguration> if the type = SV
        Node parent = samlTokenSettings.getParentNode();
        if (parent == null) {
            //should never happen
            throw new IllegalStateException(
                    "<xwss:RequireSAMLAssertion> cannot occur at this position");
        }
        
        String parentName = parent.getLocalName();
        if (!DECLARATIVE_CONFIGURATION_ELEMENT_NAME.equals(parentName)) {
            throw new IllegalStateException(
                    "<xwss:RequireSAMLAssertion> of Type=SV cannot occur as child of " + parentName);
        }
    }
    
    private static void validateSAMLType(String type, Element samlTokenSettings) {
        if (!SV_SAML_TYPE.equals(type) && !HOK_SAML_TYPE.equals(type)) {
            throw new IllegalStateException(
                    type + " not a valid SAML Assertion Type, require one of HOK|SV");
        }
        
        // the parent node should be a <SecurityConfiguration> if the type = SV
        if (SV_SAML_TYPE.equals(type)) {
            
            Node parent = samlTokenSettings.getParentNode();
            if (parent == null) {
                //should never happen
                throw new IllegalStateException(
                        "SAML Assertion cannot occur at this position");
            }
            
            String parentName = parent.getLocalName();
            if (!DECLARATIVE_CONFIGURATION_ELEMENT_NAME.equals(parentName)) {
                throw new IllegalStateException(
                        "SAML Assertion of Type=SV cannot occur as child of " + parentName);
            }
        }
    }
    
    private static boolean dynamicPolicy(Element configData) {
        String dynamicFlag = configData.getAttribute(ENABLE_DYNAMIC_POLICY_ATTRIBUTE_NAME);
        NodeList nl = configData.getElementsByTagName("*");
        
        if ("".equals(dynamicFlag) || "false".equals(dynamicFlag) ||
                "0".equals(dynamicFlag))
            return false;
        
        if ("true".equals(dynamicFlag) || "1".equals(dynamicFlag)) {
            if (nl.getLength() == 0) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean getBSPAttribute(Element configData, ApplicationSecurityConfiguration parent) {
        String conformanceValue = configData.getAttribute(CONFORMANCE_ATTRIBUTE_NAME);
        if (BSP_CONFORMANCE.equals(conformanceValue)) {
            return true;
        } else if ("".equals(conformanceValue) && (parent != null)) {
            return parent.isBSP();
        }
        return false;
    }
    
    private static String getIdAttribute(Element configData) {
        String id = configData.getAttribute(ID_ATTRIBUTE_NAME);
        
        if (id.startsWith("#")) {
            throw new IllegalArgumentException("Illegal id attribute " + id +
                    ", id attributes on policy elements cannot begin with a '#' character");
        }
        
        if ("".equals(id)) {
            id = generateUUID();
        }
        return id;
    }
    
    //TODO: rewrite this
    private static String generateUUID() {
        //Random rnd = new Random();
        int intRandom = rnd.nextInt();
        String id = "XWSSGID-"+String.valueOf(System.currentTimeMillis())+String.valueOf(intRandom);
        return id;
    }
    
    private static void validateTargetContentOnly(Element target) {
        
        Node parent = target.getParentNode();
        String parentName = parent.getLocalName();
        if (!ENCRYPT_OPERATION_ELEMENT_NAME.equalsIgnoreCase(parentName) &&
                !ENCRYPTION_REQUIREMENT_ELEMENT_NAME.
                equalsIgnoreCase(parentName)) {
            throw new IllegalStateException(
                    "contentOnly attribute not allowed on Targets under element " +
                    parentName);
        }
    }
    @SuppressWarnings("unchecked")
    private static String getSecurityEnvironmentHandler(Element element) {
        
        int secEnvCount = 0;
        Element eachDefinitionElement = getFirstChildElement(element);
        String handlerClsName = null;
        
        while (eachDefinitionElement != null) {
            QName definitionType = getQName(eachDefinitionElement);
            if (SECURITY_ENVIRONMENT_HANDLER_ELEMENT_QNAME.equals(definitionType)) {
                if (secEnvCount > 0) {
                    // log
                    throw new IllegalStateException(
                            "More than one <xwss:SecurityEnvironmentHandler> element " +
                            "under " + element.getTagName());
                }
                secEnvCount++;
                
                handlerClsName =
                        XMLUtil.getFullTextFromChildren(
                        eachDefinitionElement);
                if (handlerClsName == null || handlerClsName.equals("")) {
                    // log
                    throw new IllegalStateException(
                            "A Handler class name has to be specified " +
                            "in security configuration file");
                }
            }
            eachDefinitionElement = getNextElement(eachDefinitionElement);
        }
        return handlerClsName;
    }
    
    //TODO : might actually want to set XPath2ParameterSpec for XPATH2
    @SuppressWarnings("unchecked")
    private static void readAlgorithmProperties(HashMap props, Element eachDefinitionElement) {
        String name = eachDefinitionElement.getAttribute(NAME_ATTRIBUTE_NAME);
        String value = eachDefinitionElement.getAttribute(VALUE_ATTRIBUTE_NAME);
        props.put(name, value);
    }
    
    private static boolean getBooleanValue(String valueString) {
        if ("0".equals(valueString) || "false".equalsIgnoreCase(valueString)) {
            return false;
        }
        if ("1".equals(valueString) || "true".equalsIgnoreCase(valueString)) {
            return true;
        }
        log.log(Level.SEVERE, "WSS0511.illegal.boolean.value", valueString);
        throw new IllegalArgumentException(valueString +
                " is not a valid boolean value");
    }
    
    private static void applyDefaults(TimestampPolicy policy, boolean dp) {
        if (policy.getTimeout() == 0) {
            policy.setTimeout(5000);
        }
    }
    
    private static void applyDefaults(EncryptionPolicy policy, boolean dp) {
        
        // get the target list
        EncryptionPolicy.FeatureBinding featureBinding =
                (EncryptionPolicy.FeatureBinding)policy.getFeatureBinding();
        boolean targetsEmpty = (featureBinding.getTargetBindings().size() == 0);
        if (!dp && targetsEmpty) {
            // this much will automatically set the SOAPBody as target and
            // contentOnly as TRUE.
            Target t = new EncryptionTarget();
            featureBinding.addTargetBinding(t);
        }
        if (policy.getKeyBinding() == null) {
            AuthenticationTokenPolicy.X509CertificateBinding x509Policy =
                    (AuthenticationTokenPolicy.X509CertificateBinding)policy.newX509CertificateKeyBinding();
            x509Policy.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
        }
        
    }
    
    private static void applyDefaults(SignaturePolicy policy, boolean dp) {
        
        // get the target list
        SignaturePolicy.FeatureBinding featureBinding =
                (SignaturePolicy.FeatureBinding)policy.getFeatureBinding();
        
        boolean targetsEmpty = (featureBinding.getTargetBindings().size() == 0);
        if(MessageConstants.debug){
            log.log(Level.FINEST, "In ApplyDefaults"+featureBinding.getTargetBindings().size());
        }
        if (!dp && targetsEmpty) {
            // this much will automatically set the SOAPBody as target
            SignatureTarget t = new SignatureTarget();
            t.setDigestAlgorithm(DigestMethod.SHA1); //SHA1
            featureBinding.addTargetBinding(t);
        }
        if (policy.getKeyBinding() == null) {
            AuthenticationTokenPolicy.X509CertificateBinding x509Binding =
                    (AuthenticationTokenPolicy.X509CertificateBinding)policy.newX509CertificateKeyBinding();
            x509Binding.newPrivateKeyBinding();
            x509Binding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
        }
        //If symmetricKey Binding set default algorithm to HMAC-SHA1 else to RSA-SHA1
        if(PolicyTypeUtil.symmetricKeyBinding(policy.getKeyBinding())){
            setDefaultKeyAlgorithm(policy.getKeyBinding(), MessageConstants.HMAC_SHA1_SIGMETHOD);
        }else{
            setDefaultKeyAlgorithm(policy.getKeyBinding(), javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1);
        }
        if ("".equals(featureBinding.getCanonicalizationAlgorithm())) {
            featureBinding.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
        }
        
    }
    
    private static void applyDefaults(
            AuthenticationTokenPolicy.UsernameTokenBinding policy, boolean dp) {
        
        // defaults are applied here which is useNonce = true, doDigest=true
        
    }
    
    private static void applyDefaults(
            AuthenticationTokenPolicy.SAMLAssertionBinding policy, boolean dp) {
        
        if ("".equals(policy.getReferenceType())) {
            policy.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
        }
    }
    
    private static void applyReceiverDefaults(SignaturePolicy policy, boolean bsp, boolean dp) {
        
        // get the target list
        SignaturePolicy.FeatureBinding featureBinding =
                (SignaturePolicy.FeatureBinding)policy.getFeatureBinding();
        boolean targetsEmpty = (featureBinding.getTargetBindings().size() == 0);
        if (!dp && targetsEmpty) {
            // this much will automatically set the SOAPBody as target
            SignatureTarget t = new SignatureTarget();
            //if (!bsp)
            t.setDigestAlgorithm(DigestMethod.SHA1); //SHA1
            featureBinding.addTargetBinding(t);
        }
        
        // if bsp is true the filters will actually have code to verify that the
        // incoming algorithms (are BSP defined ones)
        // so nothing todo here
        //if (!bsp) {
        //    if ("".equals(featureBinding.getCanonicalizationAlgorithm())) {
        //        featureBinding.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
        //    }
        //}
        policy.isBSP(bsp);
    }
    
    private static void applyReceiverDefaults(EncryptionPolicy policy, boolean bsp, boolean dp) {
        
        // get the target list
        EncryptionPolicy.FeatureBinding featureBinding =
                (EncryptionPolicy.FeatureBinding)policy.getFeatureBinding();
        boolean targetsEmpty = (featureBinding.getTargetBindings().size() == 0);
        if (!dp && targetsEmpty) {
            // this much will automatically set the SOAPBody as target and
            // contentOnly as TRUE.
            Target t = new EncryptionTarget();
            featureBinding.addTargetBinding(t);
        }
        
        // if bsp is true the filters will actually have code to verify that the
        // incoming algorithms (for key and data enc are BSP defined ones)
        // so nothing todo here
        //if (!bsp) {
        //    if ("".equals(((EncryptionPolicy.FeatureBinding)
        //    policy.getFeatureBinding()).getDataEncryptionAlgorithm())) {
        //        ((EncryptionPolicy.FeatureBinding)
        //        policy.getFeatureBinding()).
        //                setDataEncryptionAlgorithm(DEFAULT_DATA_ENC_ALGO);
        //    }
        //}
        policy.isBSP(bsp);
    }
    
    private static void applyReceiverDefaults(
            AuthenticationTokenPolicy.UsernameTokenBinding policy,
            boolean bsp,
            String securityHandlerClass, boolean dp)
            throws PolicyGenerationException {
        
        // defaults are applied here which is RequireNonce = true, RequireDigest=true        
        policy.isBSP(bsp);
    }
    
    private static void applyReceiverDefaults(
            TimestampPolicy timestampPolicy,
            boolean bsp,
            String securityHandlerClass, boolean dp) {
        if ( timestampPolicy.getMaxClockSkew() == 0 ) {
             timestampPolicy.setMaxClockSkew(Timestamp.MAX_CLOCK_SKEW);
        }
                                                                                                                    
        if (timestampPolicy.getTimestampFreshness() == 0) {
            timestampPolicy.setTimestampFreshness(Timestamp.TIMESTAMP_FRESHNESS_LIMIT);
        }
        
        timestampPolicy.isBSP(bsp);
    }
    
    private static void applyReceiverDefaults(
            AuthenticationTokenPolicy.SAMLAssertionBinding policy, boolean bsp, boolean dp) {
        
        if ("".equals(policy.getReferenceType())) {
            policy.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
        }
        policy.isBSP(bsp);
    }
    
    private static boolean configHasSingleService(Element config) {
        
        NodeList services =
                config.getElementsByTagNameNS(
                CONFIGURATION_URL, SERVICE_ELEMENT_NAME);
        if ((services.getLength() > 1) || (services.getLength() == 0)) {
            return false;
        }
        return true;
    }
    
    private static boolean configHasSingleServiceAndNoPorts(Element config) {
        
        NodeList services =
                config.getElementsByTagNameNS(
                CONFIGURATION_URL, SERVICE_ELEMENT_NAME);
        if ((services.getLength() > 1) || (services.getLength() == 0)) {
            return false;
        }
        
        NodeList ports =
                config.getElementsByTagNameNS(
                CONFIGURATION_URL, PORT_ELEMENT_NAME);
        
        if (ports.getLength() == 0) {
            return true;
        }
        return false;
    }
    
    private static boolean configHasOperations(Element config) {
        NodeList ops =
                config.getElementsByTagNameNS(
                CONFIGURATION_URL, OPERATION_ELEMENT_NAME);
        if (ops.getLength() > 0) {
            return true;
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    private static void checkIdUniqueness(Element elem) {
        
        NodeList nl = elem.getElementsByTagNameNS(CONFIGURATION_URL, "*");
        int len = nl.getLength();
        HashMap map = new HashMap();
        for (int i=0; i < len; i++) {
            Element subElem = (Element)nl.item(i);
            String idAttr = subElem.getAttribute(ID_ATTRIBUTE_NAME);
            if (!"".equals(idAttr)) {
                if (map.containsKey(idAttr)) {
                    throw new IllegalArgumentException("id attribute value '" + idAttr + "' not unique");
                } else {
                    map.put(idAttr, idAttr);
                }
            }
            
            idAttr = subElem.getAttribute(STRID);
            if (!"".equals(idAttr)) {
                if (map.containsKey(idAttr)) {
                    throw new IllegalArgumentException("strId/id attribute value '" + idAttr + "' not unique");
                } else {
                    map.put(idAttr, idAttr);
                }
            }
        }

    }
    
}