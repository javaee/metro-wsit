/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.xml.ws.security.impl.policy;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.jaxws.impl.TubeConfiguration;
import com.sun.xml.wss.provider.wsit.logging.LogDomainConstants;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.XMLStreamReaderEx;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 *
 * @author suresh
 */
public class CertificateRetriever {

    protected TubeConfiguration pipeConfig = null;
    protected static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSIT_PVD_DOMAIN,
            LogDomainConstants.WSIT_PVD_DOMAIN_BUNDLE);
    private String location = null;
    private String password = null;
    private String alias = null;
    private Certificate cs = null;
    private FileInputStream fis = null;
    private Policy ep = null;

    public Certificate getServerKeyStore(WSEndpoint wse) throws IOException, XWSSecurityException {

        QName keyStoreQName = new QName("http://schemas.sun.com/2006/03/wss/server", "KeyStore");
        QName eprQName = new QName("http://schemas.sun.com/2006/03/wss/server", "EnableEPRIdentity");
        boolean found = checkforEPRIdentity(wse, eprQName);
        if (found == false) {
            return null;
        }
        setLocationPasswordAndAlias(keyStoreQName, wse);
        if (password == null || location == null || alias == null) {
            return null;
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
        }
        return cs;

    }

    public byte[] digestBST(XMLStreamReader reader) throws XMLStreamException {
        byte[] bstValue = null;
        if (reader == null) {
            throw new RuntimeException("XML stream reader is null");
        }
        while (reader.getEventType() != XMLStreamReader.CHARACTERS && reader.getEventType() != reader.END_ELEMENT) {
            reader.next();
        }
        if (reader.getEventType() == XMLStreamReader.CHARACTERS) {

            if (reader instanceof XMLStreamReaderEx) {
                CharSequence data = ((XMLStreamReaderEx) reader).getPCDATA();
                if (data instanceof Base64Data) {
                    Base64Data binaryData = (Base64Data) data;
                    bstValue = binaryData.getExact();

                }
            }
            try {
                bstValue = Base64.decode(StreamUtil.getCV(reader));


            } catch (Base64DecodingException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            } catch (XMLStreamException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }
        return bstValue;
    }

    public X509Certificate constructCertificate(byte[] bstValue) {
        try {
            X509Certificate cert = null;
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(bstValue));
            return cert;
        } catch (CertificateException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

  
    private boolean checkforEPRIdentity(WSEndpoint wse, QName eprQName) {

        if (wse.getPort() == null) {
            return true;
        }
        getEndpointOROperationalLevelPolicy(wse);
        if (ep == null) {
            return true;
        }
        for (AssertionSet assertionSet : ep) {
            for (PolicyAssertion pa : assertionSet) {
                if (pa.getName().equals(eprQName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getEndpointOROperationalLevelPolicy(WSEndpoint wse) {
        PolicyMap pm = wse.getPolicyMap();
        WSDLPort port = wse.getPort();
        QName serviceName = port.getOwner().getName();
        QName portName = port.getName();

        PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(serviceName, portName);

        try {
            ep = pm.getEndpointEffectivePolicy(endpointKey);
            if (ep == null) {
                for (WSDLBoundOperation operation : port.getBinding().getBindingOperations()) {
                    QName operationName = new QName(operation.getBoundPortType().getName().getNamespaceURI(),
                            operation.getName().getLocalPart());
                    PolicyMapKey operationKey = PolicyMap.createWsdlOperationScopeKey(serviceName, portName, operationName);
                    ep = pm.getOperationEffectivePolicy(operationKey);
                    if (ep != null) {
                        break;
                    }
                }
            }
        } catch (PolicyException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    private void setLocationPasswordAndAlias(QName qName, WSEndpoint wse) {
        if (wse.getPort() == null) {
            return;
        }
        if (ep == null) {
            return;
        }
        for (AssertionSet assertionSet : ep) {
            for (PolicyAssertion pa : assertionSet) {
                if (PolicyUtil.isConfigPolicyAssertion(pa)) {
                    if (!pa.getName().equals(qName)) {
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
                            StringBuffer sb = null;
                            sb = new StringBuffer(location);
                            if (location.startsWith("$WSIT")) {
                                String path = System.getProperty("WSIT_HOME");
                                sb.replace(0, 10, path);
                                location = sb.toString();
                            }

                        } else if (name.getLocalPart().equals("alias")) {
                            alias = (String) atts.get(name);
                        }
                    }

                }

            }

        }

    }   
}
