
package wstrust.wssx_scenario7.sts;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the wstrust.wssx_scenario7.sts package. 
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

    private final static QName _MessageBody_QNAME = new QName("http://schemas.message.com/Message", "MessageBody");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: wstrust.wssx_scenario7.sts
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MessageBodyType }
     * 
     */
    public MessageBodyType createMessageBodyType() {
        return new MessageBodyType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MessageBodyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.message.com/Message", name = "MessageBody")
    public JAXBElement<MessageBodyType> createMessageBody(MessageBodyType value) {
        return new JAXBElement<MessageBodyType>(_MessageBody_QNAME, MessageBodyType.class, null, value);
    }

}
