/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.xml.wss.provider.wsit;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
//import com.sun.xml.internal.ws.api.addressing.WSEndpointReference;
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
    PolicyMap pm = null;
    WSEndpoint e = null;
    Map props = null;
    Certificate cs = null;
    SOAPVersion sp = null;
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
                        Logger.getLogger(IdentityComponent.class.getName()).log(Level.SEVERE, null, ex);
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
         inner:  for (PolicyAssertion pa : assertionSet) {
                    if (PolicyUtil.isConfigPolicyAssertion(pa)) {
                        if(!pa.getName().equals(keyStoreQName)){
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
                                if(location.startsWith("$WSIT")){
                                    String path = System.getProperty("WSIT_HOME");
                                    StringBuffer sb = new StringBuffer(location);
                                    sb.replace(0, 10, path);
                                    location = sb.toString();
                                }
                            } else if (name.getLocalPart().equals("alias")) {
                                alias = (String) atts.get(name);
                            }
                        }
                        if(password == null || location == null || alias == null){
                            return;
                        }
                            KeyStore keyStore = null;
                        try {                            
                            keyStore = KeyStore.getInstance("JKS");
                            fis = new java.io.FileInputStream(location);
                            keyStore.load(fis, password.toCharArray());
                            cs = keyStore.getCertificate(alias);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(IdentityComponent.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(IdentityComponent.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(IdentityComponent.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (CertificateException ex) {
                            Logger.getLogger(IdentityComponent.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (KeyStoreException ex) {
                            Logger.getLogger(IdentityComponent.class.getName()).log(Level.SEVERE, null, ex);
                            ex.printStackTrace();
                        } finally {
                            keyStore = null;
                            fis.close();
                            break inner;
                        }

                    }
                    
                }
               
            }
        } catch (PolicyException ex) {
            Logger.getLogger(IdentityComponent.class.getName()).log(Level.SEVERE, null, ex);
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
            //log here
            throw new XMLStreamException(je);
        }
        return xbr.getXMLStreamBuffer().readAsXMLStreamReader();
    }
}