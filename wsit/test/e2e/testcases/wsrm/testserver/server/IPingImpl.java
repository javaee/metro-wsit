
package wsrm.testserver.server;

import javax.xml.bind.JAXBElement;
import javax.jws.WebService;
import javax.xml.bind.*;
import javax.xml.namespace.*;
import javax.jws.WebService;
import javax.jws.WebParam;
import javax.xml.bind.JAXBElement;
import java.util.Map;
import com.sun.xml.ws.rm.jaxws.util.ProcessingFilter;
import com.sun.xml.ws.rm.Message;
import com.sun.xml.ws.rm.jaxws.runtime.server.RMDestination;
import java.lang.reflect.*;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

@WebService(endpointInterface="wsrm.testserver.server.IPing")
@javax.xml.ws.BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public class IPingImpl extends TestService {
    
    static int lastMessageNumber = 0;
    static boolean ordered = true;
    
    @Resource
    WebServiceContext context;
    
    public IPingImpl() {
        RMDestination.getRMDestination().setProcessingFilter(this);
    }
    
    public void handleOneWay(String a, String b) {
        
    }
    
    public String handleTwoWay(String a, String b) {
        return a;
    }
    
    public EchoResponseBodyType echoString(EchoRequestBodyType echoString) {
        
        EchoResponseBodyType pr =
                new ObjectFactory().createEchoResponseBodyType();
        
        JAXBElement<String> text = echoString.getText();
        JAXBElement<String> seq = echoString.getSequence();
        
        String ret = handleTwoWay(text.getValue(), seq.getValue());
        
        JAXBElement<String> val =
                new JAXBElement<String>(new QName("http://tempuri.org/",
                "EchoStringReturn"),
                String.class,
                new String("Returning hello "));
        
        val.setValue(ret);
        
        pr.setEchoStringReturn(val);
        
        return pr;
    }
    
    public void ping(PingRequestBodyType echoString) {
        
        JAXBElement<String> text = echoString.getText();
        JAXBElement<String> seq = echoString.getSequence();
        
        handleOneWay(text.getValue(), seq.getValue());
    }
    
    public void process(String param) {
        if (param.equals("test_order")) {
            int messageNumber = (Integer)(context.getMessageContext().get("com.sun.xml.ws.messagenumber"));
            if (messageNumber != lastMessageNumber + 1) {
                ordered  = false;
            }
            lastMessageNumber = messageNumber;
            
        }
        processImpl(param);
    }
    public String clear(String param) {
        clearImpl(param);
        return "";
    }
    
    public String getCount(String param) {
        return reportCount(param);
    }
    
    public String getSequence(String param) {
        return reportSequence(param);   
    }
    
    public String getDuplicates(String param) {
        return reportDuplicates(param);
    }
    public String getAlive(String param) {
        return isSequenceAlive(param);
    }
    
    public String getInactivityTimeout(String s) {
        return super.getInactivityTimeout(s);
    }
    
    public String getOrdered(String s) {
        if (ordered) {
            return "true";
        } else {
            return "false";
        }
    }
    
   
    
}
