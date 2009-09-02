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

package simple.server;

import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.GenericToken;
import org.xmlsoap.dab.Department;

import javax.xml.ws.Holder;
import simple.schema.client.Ping;
import simple.schema.client.PingResponseBody;

import simple.client.PingService;
import simple.client.IPingService;

import com.sun.xml.ws.security.trust.STSIssuedTokenFeature;
import com.sun.xml.ws.security.trust.impl.client.DefaultSTSIssuedTokenConfiguration;

import com.sun.xml.wss.SubjectAccessor;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.saml.util.SAMLUtil;
import java.util.Set;
import javax.annotation.Resource;
import javax.security.auth.Subject;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import org.w3c.dom.Element;

import javax.xml.ws.WebServiceFeature;

@javax.jws.WebService (endpointInterface="simple.server.IFinancialService")
public class FSImpl implements IFinancialService {
    @Resource
    private WebServiceContext context;
    
    public String getAccountBalance(Department dept){
        
        String company = dept.getCompanyName();
        System.out.println("company = " + company);
        
        String department = dept.getDepartmentName();
        System.out.println("department = " + department);

        // Call the PingService
        ping();
        
        String balance = "1,000,000";
        
        return balance;
    }

    private void ping(){
        PingService service = new PingService();

        IPingService stub = service.getCustomBindingIPingService();
        ((BindingProvider)stub).getRequestContext().put("userSAMLAssertion", getSAMLAssertion());
        stub.ping(new Holder("1"), new Holder("sun"), new Holder("Passed!"));
    }

    private Element getSAMLAssertion() {
        Element samlAssertion = null;
        try {
            Subject subj = SubjectAccessor.getRequesterSubject(context);
            Set<Object> set = subj.getPublicCredentials();
            for (Object obj : set) {
                if (obj instanceof XMLStreamReader) {
                    XMLStreamReader reader = (XMLStreamReader) obj;
                    //To create a DOM Element representing the Assertion :
                    samlAssertion = SAMLUtil.createSAMLAssertion(reader);
                    break;
                } else if (obj instanceof Element) {
                    samlAssertion = (Element) obj;
                    break;
                }
            }
        } catch (XMLStreamException ex) {
            throw new XWSSecurityRuntimeException(ex);
        } catch (XWSSecurityException ex) {
            throw new XWSSecurityRuntimeException(ex);
        }
        return samlAssertion;
    }
}
