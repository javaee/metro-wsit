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
/*
 * IdentityComponent.java  Created on July 13, 2009, 4:40 PM
 *
/* To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.xml.wss.provider.wsit;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.security.core.ai.IdentityType;
import com.sun.xml.stream.buffer.XMLStreamBufferResult;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.EndpointComponent;
import com.sun.xml.ws.api.server.EndpointReferenceExtensionContributor;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.ws.security.secext10.BinarySecurityTokenType;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.jaxws.impl.ServerTubeConfiguration;
import com.sun.xml.wss.jaxws.impl.TubeConfiguration;
import com.sun.xml.wss.provider.wsit.logging.LogDomainConstants;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

/**
 *
 * @author suresh
 */
public class IdentityComponent implements EndpointComponent {

    protected TubeConfiguration pipeConfig = null;
    protected PolicyMap pm = null;
    protected WSEndpoint e = null;
    protected Map props = null;
    private Certificate cs = null;
    protected SOAPVersion sp = null;
    protected static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSIT_PVD_DOMAIN,
            LogDomainConstants.WSIT_PVD_DOMAIN_BUNDLE);


    public IdentityComponent(WSEndpoint e, PolicyMap pm, Map props) {
        this.pm = pm;
        this.e = e;
        this.props = props;
        this.sp = e.getBinding().getSOAPVersion();
        URL url = null;
        try {
            url = SecurityUtil.loadFromClasspath("META-INF/ServerCertificate.cert");
            if(url == null){
            getServerKeyStore();
            }else {
                CertificateFactory certFact = CertificateFactory.getInstance("X.509");
                InputStream is = url.openStream();
                cs = certFact.generateCertificate(is);
                is.close();
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch(CertificateException ex){
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);            
        } 


    }

    @SuppressWarnings("unchecked")
    public <T> T getSPI(@NotNull Class<T> spiType) {
        //if id policy enabled &&
        if (spiType.isAssignableFrom(EndpointReferenceExtensionContributor.class)) {
            return (T) new IdentityEPRExtnContributor();

        }
        return null;
    }

    class IdentityEPRExtnContributor extends EndpointReferenceExtensionContributor {

        QName ID_QNAME = new QName("http://schemas.xmlsoap.org/ws/2006/02/addressingidentity", "Identity");

        public WSEndpointReference.EPRExtension getEPRExtension(@Nullable WSEndpointReference.EPRExtension extension) {
            return new WSEndpointReference.EPRExtension() {

                public XMLStreamReader readAsXMLStreamReader() throws XMLStreamException {
                    XMLStreamReader reader = null;
                    try {
                        String id = PolicyUtil.randomUUID();
                        BinarySecurityTokenType bst = new BinarySecurityTokenType();
                        bst.setValueType(MessageConstants.X509v3_NS);
                        bst.setId(id);
                        bst.setEncodingType(MessageConstants.BASE64_ENCODING_NS);
                        if(cs != null){
                        bst.setValue(cs.getEncoded());
                        } 
                        JAXBElement<BinarySecurityTokenType> bstElem = new com.sun.xml.ws.security.secext10.ObjectFactory().createBinarySecurityToken(bst);
                        IdentityType identityElement = new IdentityType();
                        identityElement.getDnsOrSpnOrUpn().add(bstElem);

                        reader = readHeader(identityElement);

                    } catch (CertificateEncodingException ex) {
                        log.log(Level.SEVERE, null, ex);
                    }
                    return reader;
                }

                public QName getQName() {
                    return ID_QNAME;
                }
            };
        }

        public QName getQName() {
            return ID_QNAME;
        }
    }

    public void getServerKeyStore() throws IOException {
        String alias = null;
        String password = null;
        String location = null;
        java.io.FileInputStream fis = null;

        WSDLPort port = (WSDLPort) props.get("WSDL_MODEL");
        pipeConfig = new ServerTubeConfiguration(pm, port, e);
        QName serviceName = pipeConfig.getWSDLPort().getOwner().getName();
        QName portName = pipeConfig.getWSDLPort().getName();
        QName keyStoreQName = new QName("http://schemas.sun.com/2006/03/wss/server", "KeyStore");

        PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(serviceName, portName);
        try {
            Policy ep = pm.getEndpointEffectivePolicy(endpointKey);
            for (AssertionSet assertionSet : ep) {
                inner:
                for (PolicyAssertion pa : assertionSet) {
                    if (PolicyUtil.isConfigPolicyAssertion(pa)) {
                        if (!pa.getName().equals(keyStoreQName)) {
                            continue;
                        }
                        HashMap atts = (HashMap) pa.getAttributes();
                        Set ks = atts.keySet();
                        Iterator itt = ks.iterator();
                        while (itt.hasNext()) {
                            QName name = (QName) itt.next();
                            if (name.getLocalPart().equals("storepass")) {
                                password = (String) atts.get(name);
                            } else if (name.getLocalPart().equals("location")) {
                                location = (String) atts.get(name);
                                if (location.startsWith("$WSIT")) {
                                    String path = System.getProperty("WSIT_HOME");
                                    StringBuffer sb = new StringBuffer(location);
                                    sb.replace(0, 10, path);
                                    location = sb.toString();
                                }
                            } else if (name.getLocalPart().equals("alias")) {
                                alias = (String) atts.get(name);
                            }
                        }
                        if (password == null || location == null || alias == null) {
                            return;
                        }
                        KeyStore keyStore = null;
                        try {
                            keyStore = KeyStore.getInstance("JKS");
                            fis = new java.io.FileInputStream(location);
                            keyStore.load(fis, password.toCharArray());
                            cs = keyStore.getCertificate(alias);
                        } catch (FileNotFoundException ex) {
                            log.log(Level.SEVERE, null, ex);
                            throw new XWSSecurityException(ex);
                        } catch (IOException ex) {
                            log.log(Level.SEVERE, null, ex);
                             throw new RuntimeException(ex);
                        } catch (NoSuchAlgorithmException ex) {
                            log.log(Level.SEVERE, null, ex);
                             throw new XWSSecurityException(ex);
                        } catch (CertificateException ex) {
                            log.log(Level.SEVERE, null, ex);
                             throw new XWSSecurityException(ex);
                        } catch (KeyStoreException ex) {
                            log.log(Level.SEVERE, null, ex);
                             throw new XWSSecurityException(ex);
                        } finally {
                            keyStore = null;
                            fis.close();
                            break inner;
                        }

                    }

                }

            }
        } catch (PolicyException ex) {
            log.log(Level.SEVERE, null, ex);
             
        }

    }

    public XMLStreamReader readHeader(IdentityType identityElem) throws XMLStreamException {
        XMLStreamBufferResult xbr = new XMLStreamBufferResult();
        JAXBElement<IdentityType> idElem =
                (new com.sun.xml.security.core.ai.ObjectFactory()).createIdentity(identityElem);
        try {
            JAXBContext context = JAXBUtil.getJAXBContext();
            Marshaller m = context.createMarshaller();
            m.setProperty("com.sun.xml.bind.xmlDeclaration", false);
            m.marshal(idElem, xbr);
        } catch (JAXBException je) {
           log.log(Level.SEVERE, null, je);
            throw new XMLStreamException(je);
        }
        return xbr.getXMLStreamBuffer().readAsXMLStreamReader();
    }
}