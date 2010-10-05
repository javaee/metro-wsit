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

package common;

import com.sun.xml.ws.api.security.trust.client.IssuedTokenManager;
import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.impl.client.DefaultSTSIssuedTokenConfiguration;
import com.sun.xml.wss.XWSSConstants;
import java.io.*;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.*;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 *
 * @author  TOSHIBA USER
 */
public  class SamlCallbackHandler implements CallbackHandler {
 
    public SamlCallbackHandler() {
   
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i=0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof SAMLCallback) {
		SAMLCallback samlCallback = (SAMLCallback)callbacks[i];
                Map runtimeProp = samlCallback.getRuntimeProperties();
                Element samlAssertion = getSAMLAssertionFromSTS();
		samlCallback.setAssertionElement(samlAssertion);
            }         
	}
    }

    private Element getSAMLAssertionFromSTS() {
        String stsEndpoint = "http://localhost:8080/jaxws-fs-sts/sts";
        String stsMexAddress = "http://localhost:8080/jaxws-fs-sts/sts/mex";
        DefaultSTSIssuedTokenConfiguration config = new DefaultSTSIssuedTokenConfiguration(
                    stsEndpoint, stsMexAddress);
           
        config.setKeyType("http://schemas.xmlsoap.org/ws/2005/05/identity/NoProofKey");
          
        try{
            IssuedTokenManager manager = IssuedTokenManager.getInstance();
           
            String appliesTo = "http://localhost:8080/jaxws-fs/simple";
            IssuedTokenContext ctx = manager.createIssuedTokenContext(config, appliesTo);
            manager.getIssuedToken(ctx);
            Token issuedToken = ctx.getSecurityToken();
           
            return (Element)issuedToken.getTokenValue();
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
