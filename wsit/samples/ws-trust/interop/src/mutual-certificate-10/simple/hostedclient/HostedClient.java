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

package simple.hostedclient;


import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;


import org.tempuri.*;
import xwsinterop.interoprt.*;

public class HostedClient {
    
   
    private static final String PARAM_STSAddress = "STS_Endpoint_Address";
    private static final String PARAM_ServiceAddress = "Service_Endpoint_Address";
    private static final String PARAM_ConfigName = "Service_Endpoint_ConfigName";
    private static final String featureName ="WSTRUST";
    private static final String SCENARIO_2 = "Scenario_2_IssuedToken_MutualCertificate10";
    
    public static void main(String [] args) throws UnknownHostException{
        
        String serviceUrl = System.getProperty("service.url");
        String sts = System.getProperty("sts");
        String stsUrl = System.getProperty("msclient."+sts+"sts.url");
        if(InetAddress.getByName(URI.create(serviceUrl).getHost()).isLoopbackAddress()){
            serviceUrl = serviceUrl.replaceFirst("localhost",InetAddress.getLocalHost().getHostAddress());
        }
        if(InetAddress.getByName(URI.create(stsUrl).getHost()).isLoopbackAddress()){
            stsUrl = stsUrl.replaceFirst("localhost",InetAddress.getLocalHost().getHostAddress());
        }
        
        
        HostedClientSoap proxy = createProxy();
             
        ArrayOfHostedClientParameter paramArray = new ArrayOfHostedClientParameter();
        List<HostedClientParameter> list = paramArray.getHostedClientParameter();
        
        HostedClientParameter stsParameter = readParameter(stsUrl, PARAM_STSAddress);
        list.add(stsParameter);
        HostedClientParameter serviceParameter = readParameter(serviceUrl, PARAM_ServiceAddress);
        list.add(serviceParameter);
        HostedClientParameter configParameter = readParameter(SCENARIO_2, PARAM_ConfigName);
        list.add(configParameter);
        runScenario(SCENARIO_2,paramArray,proxy);
    }
    
    public static HostedClientParameter readParameter(String endpoint, String parameterName) {
        HostedClientParameter parameter = new HostedClientParameter();
        parameter.setKey(parameterName);
        parameter.setValue(endpoint);
        return parameter;
    }
    
    public static HostedClientSoap createProxy() {
        HostedClientSoapImpl hostclisvc = new HostedClientSoapImpl();
        return hostclisvc.getBasicHttpBindingHostedClientSoap();        
    }
    
    public static void runScenario(String scenarioName, ArrayOfHostedClientParameter paramArray, HostedClientSoap proxy){
        System.out.println("Run Scenario: " + scenarioName);
        List<HostedClientParameter> list = paramArray.getHostedClientParameter();
        for(int i = 0; i<list.size();i++) {
            System.out.println(list.get(i).getKey() + ":" + list.get(i).getValue());
        }
        
        HostedClientResult result = proxy.run(featureName, scenarioName,  paramArray);
        System.out.println("Result: " + (result.isSuccess() ? "PASS" : "FAIL"));
        System.out.println("Debuglog: " + result.getDebugLog());
    }

}
