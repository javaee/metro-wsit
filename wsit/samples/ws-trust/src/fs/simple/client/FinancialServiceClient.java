/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package simple.client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import java.io.FileInputStream;
import simple.schema.client.Department;

import com.sun.xml.ws.security.trust.WSTrustConstants;
import java.net.URL;

public class FinancialServiceClient {
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
            
        } catch (Exception ex) {
            System.out.println ("Caught Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
    }
    
}
