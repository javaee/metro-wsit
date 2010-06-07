
package xwss.s19.server;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the xwss.s19.server package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _PingResponse_QNAME = new QName("http://xmlsoap.org/Ping", "PingResponse");
    private final static QName _Ping_QNAME = new QName("http://xmlsoap.org/Ping", "Ping");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: xwss.s19.server
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xmlsoap.org/Ping", name = "PingResponse")
    public JAXBElement<String> createPingResponse(String value) {
        return new JAXBElement<String>(_PingResponse_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xmlsoap.org/Ping", name = "Ping")
    public JAXBElement<String> createPing(String value) {
        return new JAXBElement<String>(_Ping_QNAME, String.class, null, value);
    }

}
