
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
public class IPingImpl implements ProcessingFilter {
    
    /**
     * Instance of Class containg methods that are actually called by echoString
     * and ping using reflection.
     */
    private Object service;
    
    @Resource
    public WebServiceContext context;
    
    public IPingImpl() {
        init();       
    }
    
    public void init() {
        try {
            String packageName = getClass().getPackage().getName();
            Class serviceClass = Class.forName(packageName + ".TestService");
            service = serviceClass.newInstance();
            
            RMDestination.getRMDestination().setProcessingFilter(this);
            
           
        } catch (Exception e) {
            service = null;
        }
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
    
    public String handleTwoWay(String text, String seq) {
        
        try {
            if (service != null) {
                Method method = service.getClass().getMethod(text, String.class);
                if (method != null) {
                    return (String)method.invoke(service, seq);
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void handleOneWay(String text, String seq) {
        try {
            if (service != null) {
                setContext();
                Method method = service.getClass().getMethod(text, String.class);
                if (method != null) {
                    method.invoke(service, seq);
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public boolean handleClientRequestMessage(Message mess) {
        try {
            
            if (service != null) {
                setContext();
                Method method = service.getClass()
                    .getMethod("handleClientRequestMessage",
                               com.sun.xml.ws.rm.Message.class);
                if (method != null) {
                    Object obj = method.invoke(service, mess);
                    return (Boolean)obj;
                }
            }
            
        } catch (Exception e) {  
        }
        return true;
    }
    
    public boolean handleClientResponseMessage(Message mess) {
        try {
            
            if (service != null) {
                Method method = service.getClass()
                    .getMethod("handleClientResponseMessage",
                               com.sun.xml.ws.rm.Message.class);
                if (method != null) {
                    Object obj = method.invoke(service, mess);
                    return (Boolean)obj;
                }
            }
            
        } catch (Exception e) {  
        }
        return true;
    }
    
    public void handleEndpointRequestMessage(Message mess) {
        try {
            
            if (service != null) {
                Method method = service.getClass()
                    .getMethod("handleEndpointRequestMessage",
                               com.sun.xml.ws.rm.Message.class);
                if (method != null) {
                    method.invoke(service, mess);
                }
            }
            
        } catch (Exception e) {  
        }

    }
    
    public boolean handleEndpointResponseMessage(Message mess){
        try {
            
            if (service != null) {
                Method method = service.getClass()
                    .getMethod("handleEndpointResponseMessage",
                               com.sun.xml.ws.rm.Message.class);
                if (method != null) {
                    method.invoke(service, mess);
                }
            }
            
        } catch (Exception e) {  
        }
        
        return true;
    }

     public void setContext() {
        
        try {
            if (service != null && context != null) {
                Method method = service.getClass().getMethod("setContext", javax.xml.ws.WebServiceContext.class);
                if (method != null) {
                   method.invoke(service, context);
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    }
    public void handleOutboundHeaders(Message mess){}
}
