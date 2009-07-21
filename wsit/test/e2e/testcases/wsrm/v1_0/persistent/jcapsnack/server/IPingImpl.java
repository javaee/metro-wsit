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
package wsrm.v1_0.persistent.jcapsnack.server;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

@WebService(endpointInterface = "wsrm.v1_0.persistent.jcapsnack.server.IPing")
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public class IPingImpl {
    private static final Logger LOGGER = Logger.getLogger(IPingImpl.class.getName());
    private static final AtomicBoolean FIRST_MESSAGE_ALREADY_REJECTED = new AtomicBoolean(false);
    private static final AtomicBoolean FIRST_MESSAGE_RESEND_DETECTED = new AtomicBoolean(false);
    //
    @Resource WebServiceContext wsContext;
    
    @WebMethod
    public void ping(String message) {
        MessageContext msgCtx = wsContext.getMessageContext();        
        long msgNumber = (Long) msgCtx.get("com.sun.xml.ws.messagenumber");

        if (msgNumber == 1) {
            if (FIRST_MESSAGE_ALREADY_REJECTED.compareAndSet(false, true)) {
                msgCtx.put("RM_ACK", "false");                
                LOGGER.log(Level.ALL, String.format("Rejecting message '%s' with message number %d", message, msgNumber));
            } else {
                LOGGER.log(Level.ALL, String.format("Detected resent message '%s' with message number %d", message, msgNumber));
                FIRST_MESSAGE_RESEND_DETECTED.set(true);
            }
        } else if (!FIRST_MESSAGE_RESEND_DETECTED.get()) {
            String errorMessage = String.format("Received message '%s' with message number %d without detecting a resend of rejected message.", message, msgNumber);
            LOGGER.log(Level.ALL, errorMessage);
            throw new RuntimeException(errorMessage);
        } else {
            LOGGER.log(Level.ALL, String.format("Received expected message '%s' with message number %d", message, msgNumber));            
        }             
    }
}
