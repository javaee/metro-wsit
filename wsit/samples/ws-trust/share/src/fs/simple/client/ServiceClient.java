/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package simple.client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import java.io.FileInputStream;
import simple.schema.client.Department;
import javax.xml.ws.Holder;
import org.xmlsoap.ping.Ping;

import com.sun.xml.ws.security.trust.WSTrustConstants;
import java.net.URL;

public class ServiceClient {
    public static void main (String[] args) {
        try {
            FinancialService service = new FinancialService();
            IFinancialService stub = service.getIFinancialServicePort(); 
      
            // use static stubs to override endpoint property of WSDL       
            String serviceHost = System.getProperty("endpoint.host");
            String servicePort = System.getProperty("endpoint.port");
            String serviceURLFragment = System.getProperty("service.url");
            String serviceURL = 
                "http://" + serviceHost + ":" + servicePort + serviceURLFragment;

            System.out.println("Service URL=" + serviceURL);
      
            //PreConfigured STS info
            String stsHost = System.getProperty("sts.host");
            String stsPort = System.getProperty("sts.port");
            String stsURLFragment = System.getProperty("sts.url");
            String stsURL = 
                "http://" + stsHost + ":" + stsPort + stsURLFragment;
            System.out.println("STS URL=" + stsURL);
            
            ((BindingProvider)stub).getRequestContext().
                put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL); 

            Department dept = new Department();
            dept.setCompanyName("A");
            dept.setDepartmentName("B");
            
            String balance = stub.getAccountBalance(dept);
            
            System.out.println("balance=" + balance);

            PingService service1 = new PingService();
            IPingService stub1 = service1.getCustomBindingIPingService();

            // use static stubs to override endpoint property of WSDL
            String service1Host = System.getProperty("endpoint1.host");
            String service1Port = System.getProperty("endpoint1.port");
            String service1URLFragment = System.getProperty("service1.url");
            String service1URL =
                "http://" + service1Host + ":" + service1Port + service1URLFragment;

            System.out.println("Service URL=" + service1URL);

            ((BindingProvider)stub1).getRequestContext().
                put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, service1URL);

             stub1.ping(new Holder("1"), new Holder("sun"), new Holder("Passed!"));
            
        } catch (Exception ex) {
            System.out.println ("Caught Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
    }
    
}
