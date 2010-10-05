/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;
import com.sun.xml.ws.security.trust.impl.client.DefaultSTSIssuedTokenConfiguration;
/**
 *
 * @author jdg
 */
public class MySTSIssuedTokenConfiguration extends DefaultSTSIssuedTokenConfiguration{

    private String stsEndpoint = null;
    private String stsMexAddress = null;
    private Claims claims = null;

    private String appliesTo = null;
    
    public String getSTSEndpoint(){
        configure();
        return stsEndpoint;
    }

    public String getSTSMEXAddress(){
        configure();
        return stsMexAddress;
    }

    public Claims getClaims(){
        configure();
        return claims;
    }

    private void configure(){
        String appTo = (String)getOtherOptions().get(STSIssuedTokenConfiguration.APPLIES_TO);
        if (appTo.equals(appliesTo)){
            return;
        }
        appliesTo = appTo;
        STSIssuedTokenConfiguration issuedToken = (STSIssuedTokenConfiguration)getOtherOptions().get(STSIssuedTokenConfiguration.ISSUED_TOKEN);
        if ("http://localhost:8080/jaxws-fs-sts/sts".equals(appliesTo)){
            this.stsEndpoint = "http://localhost:8080/jaxws-fs-mysts/mysts";
            this.stsMexAddress = "http://localhost:8080/jaxws-fs-mysts/mysts/mex";
            MyClaims cms = new MyClaims();
            cms.addClaimType(MyClaims.ROLE);
            cms.addClaimType(MyClaims.LOCALITY);
            this.claims = cms;
        }
    }
}
