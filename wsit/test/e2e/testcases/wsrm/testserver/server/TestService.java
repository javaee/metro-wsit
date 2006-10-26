package wsrm.testserver.server;
import com.sun.xml.ws.rm.Message;
import java.util.HashMap;
import com.sun.xml.ws.rm.Sequence;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.jaxws.runtime.server.RMDestination;
import com.sun.xml.ws.rm.jaxws.runtime.InboundSequence;
import javax.xml.ws.WebServiceContext;
import com.sun.xml.ws.rm.jaxws.util.ProcessingFilter;

public class TestService implements ProcessingFilter {
    
    private boolean duplicatesReceived = false;
    private boolean timedOutOnce = false;
    public static String sequenceId = "";
    private InboundSequence sequence = null;
    public static int count = 0;
    public static HashMap<Integer, Integer> messages = 
            new HashMap<Integer, Integer>();
    
    
    public WebServiceContext context;
   
    public void setContext(WebServiceContext context) {
        this.context = context;
    }
    
    public boolean handleClientResponseMessage(Message mess) {
       return true;
    }
    
    public boolean handleClientRequestMessage(Message mess) {
        return true;
    } 
    
    public void handleEndpointRequestMessage(Message mess) {
     
        //first message will be call to clear() which sets sequenceId to null.
        //next message belongs to sequence we want to track
        if (sequenceId == null) {
            sequence = (InboundSequence)mess.getSequence();
            sequenceId = mess.getSequence().getId();
        }
        
        synchronized (this) {
            if (mess.getSequence().getId().equals(sequenceId)) {
                
                int messageNumber = mess.getMessageNumber();
                if (messages.containsValue(messageNumber)) {
                    duplicatesReceived = true;
                }
                messages.put(count++, mess.getMessageNumber());
       
            }
        }
    }
    
    public boolean handleEndpointResponseMessage(Message mess) {
        return true;
    }
    
    public void handleOutboundHeaders(Message mess) {}
    
    public void processImpl(String s) {
        if (s.equals("block")) {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {}
        }
        System.out.println("received message " + s.toString());
    }
    
    public String reportCount(String s) {
        System.out.println("reportCount.. count = " + count);
        return Integer.toString(count);
    }
    
    public String reportSequence(String s) {
        String result = "";
  
        for (int i = 0; i < count; i++) {
            String num = messages.get(i).toString();
            result += num;
            result += ",";
        }
        
        return result;
    }
    
    public void clearImpl(String s) {
     System.out.println("clear called");
        sequenceId = null;
        System.out.println("sequenceId = \"" + sequenceId + "\"");
        
        
        messages = 
            new HashMap<Integer, Integer>();
        count = 0;
        duplicatesReceived = false;
        timedOutOnce = false;
    
    }
    
    public String getInactivityTimeout(String s) {
        
        SequenceConfig config = sequence.getSequenceConfig();
        System.out.println("config = " + config);
        System.out.println("sequence = " + sequence);
        System.out.println("timeout = " + config.getInactivityTimeout());
        String ret = Long.toString(
                sequence.getSequenceConfig().getInactivityTimeout());
        return ret;
    }
    
    public String getFlowControl(String s) {
        return Boolean.toString(sequence.getSequenceConfig().getFlowControl());
    }
    
    public String getBufferSize(String s) {
        return Integer.toString(
                sequence.getSequenceConfig().getBufferSize());
    }
    
    public String getOrdered(String s) {
        return Boolean.toString(
         
            sequence.getSequenceConfig().getOrdered());    
    }
    
    public String getSequenceId(String s) {
        return sequenceId;
    }
    
    public String isSequenceAlive(String s) {
        
        Sequence seq = RMDestination.
                getRMDestination().getInboundSequence(s);
        if (seq == null) {
            return "false";
        } else {
            return "true";
        } 
      
    }
    
    public String reportDuplicates(String s) {
        if (duplicatesReceived) {
            return "true";
        } else {
            return "false";
        }
    }
    
    public String getSessionId(String s) {
    
        
        String id = null;
        if (context != null ) { 
            id = (String)context.getMessageContext().
                            get("com.sun.xml.ws.sessionid");
        } 
        
        if (id != null) {
            return id;
        } else {
            return "no session id";
        }
    }
    
    public String testTimeout(String s) {
        if (!timedOutOnce) {
            timedOutOnce = true;
            
            try {
                Thread.currentThread().sleep(10000);
            } catch (Exception e) {}
            return "false";
        } else {
            return "true";
        }
    }
}
