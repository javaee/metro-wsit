
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
    private static final String SCENARIO_6 = "Scenario_6_IssuedTokenForCertificateSecureConversation_MutualCertificate11";
    
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
        HostedClientParameter configParameter = readParameter(SCENARIO_6, PARAM_ConfigName);
        list.add(configParameter);
        runScenario(SCENARIO_6,paramArray,proxy);
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
