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

package simple.server;

import org.xmlsoap.dab.Department;

@javax.jws.WebService (endpointInterface="simple.server.IFinancialService")
public class FSImpl implements IFinancialService {
    
    public String getAccountBalance(Department dept){
        
        String company = dept.getCompanyName();
        System.out.println("company = " + company);
        
        String department = dept.getDepartmentName();
        System.out.println("department = " + department);
        
        String balance = "1,000,000";
        
        return balance;
    }  
}
