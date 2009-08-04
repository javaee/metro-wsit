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
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.callback.CertificateValidationCallback.CertificateValidationException;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.misc.DefaultCallbackHandler;
import com.sun.xml.wss.impl.misc.DefaultCallbackHandler.X509CertificateValidatorImpl;
import com.sun.xml.wss.jaxws.impl.TubeConfiguration;
import com.sun.xml.wss.provider.wsit.PipeConstants;
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
import java.util.Map;

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

    public Certificate getServerKeyStore(WSEndpoint wse) throws IOException, XWSSecurityException {

        QName keyStoreQName = new QName("http://schemas.sun.com/2006/03/wss/server", "KeyStore");

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
        if(reader == null){
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

    public boolean validateCertificate(Certificate cert, Map props) {
        QName trustStoreQName = new QName("http://schemas.sun.com/2006/03/wss/client", "TrustStore");
        boolean valid = false;
        try {
            setLocationPasswordAndAlias(trustStoreQName, props);
            KeyStore trustStore = KeyStore.getInstance("JKS");
            if ( location == null){
              throw new KeyStoreException("trustStore location is null");
            }
            fis = new java.io.FileInputStream(location);
            if ( password == null){
              throw new KeyStoreException("trustStore password is null");
            }
            trustStore.load(fis, password.toCharArray());
            DefaultCallbackHandler dch = new DefaultCallbackHandler(null, null);
            DefaultCallbackHandler.X509CertificateValidatorImpl certValidator =
                       dch.new X509CertificateValidatorImpl(trustStore,null,false);
            if(cert == null){
                throw new RuntimeException("certificate is null");
            }
            valid = certValidator.validate((X509Certificate) cert);

        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (KeyStoreException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (CertificateException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (XWSSecurityException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (CertificateValidationException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        return valid;
    }

    private void setParameters(PolicyMap pm, WSDLPort port,QName qName) {
        if(port == null){
            return;
        }
        QName serviceName = port.getOwner().getName();
        QName portName = port.getName();

        PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(serviceName, portName);
        try {
            com.sun.xml.ws.policy.Policy ep = pm.getEndpointEffectivePolicy(endpointKey);

            if (ep == null) {
                for (WSDLBoundOperation operation : port.getBinding().getBindingOperations()) {
                    QName operationName = new QName(operation.getBoundPortType().getName().getNamespaceURI(),
                            operation.getName().getLocalPart());
                    PolicyMapKey operationKey = PolicyMap.createWsdlOperationScopeKey(serviceName, portName, operationName);
                    ep = pm.getOperationEffectivePolicy(operationKey);
                    if(ep != null){
                    break;
                    }
                }
            }
            if(ep == null){
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
        } catch (PolicyException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);

        }

    }

    private void setLocationPasswordAndAlias(QName trustStoreQName, Map props) {

        PolicyMap pm = (PolicyMap) props.get(PipeConstants.POLICY);
        WSDLPort port = (WSDLPort) props.get(PipeConstants.WSDL_MODEL);
        setParameters(pm,port,trustStoreQName);
    }

    private void setLocationPasswordAndAlias(QName qName, WSEndpoint wse) throws IOException {

        PolicyMap pm = wse.getPolicyMap();
        WSDLPort port =wse.getPort();
        setParameters(pm,port,qName);
        
    }
}
