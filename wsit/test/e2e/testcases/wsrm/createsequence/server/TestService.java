/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package wsrm.createsequence.server;
import com.sun.xml.ws.rm.Message;
import java.util.HashMap;
import com.sun.xml.ws.rm.Sequence;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.jaxws.runtime.server.RMDestination;
import com.sun.xml.ws.rm.jaxws.runtime.InboundSequence;
import javax.xml.ws.WebServiceContext;
import com.sun.xml.ws.rm.jaxws.util.ProcessingFilter;
import javax.annotation.Resource;
import javax.xml.ws.*;

public class TestService implements ProcessingFilter {
    
    private boolean duplicatesReceived = false;
    private boolean timedOutOnce = false;
    public static String sequenceId = "";
    private InboundSequence sequence = null;
    public static int count = 0;
    public static HashMap<Integer, Integer> messages = 
            new HashMap<Integer, Integer>();
    
    public WebServiceContext wscontext;
   
    public void setContext(WebServiceContext context) {
        this.wscontext = context;
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
         System.out.println("sequenceId = \"" + sequenceId + "\"");
         System.out.println("sequence = \"" + sequence + "\"");
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
        InboundSequence iseq = RMDestination
                .getRMDestination().getInboundSequence(sequenceId);
        SequenceConfig config = iseq.getSequenceConfig();
        
        System.out.println("timeout = " + config.getInactivityTimeout());
        String ret = Long.toString(
                config.getInactivityTimeout());
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
        if (wscontext != null ) { 
            id = (String)wscontext.getMessageContext().
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
