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

package com.sun.xml.ws.api.security.trust.client;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.security.Token;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jiandong Guo
 */
public abstract class STSIssuedTokenConfiguration implements IssuedTokenConfiguration{
    
    public static final String PROTOCOL_10 = "http://schemas.xmlsoap.org/ws/2005/02/trust";
    public static final String PROTOCOL_13 = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";

    public static final String ISSUED_TOKEN = "IssuedToken";
    public static final String APPLIES_TO = "AppliesTo";
    public static final String ACT_AS ="ActAs";
    public static final String SHARE_TOKEN = "shareToken";
    public static final String RENEW_EXPIRED_TOKEN = "renewExpiredToken";
    public static final String STS_ENDPOINT = "sts-endpoint";
    public static final String STS_MEX_ADDRESS = "sts-mex-address";
    public static final String STS_WSDL_LOCATION ="sts-wsdlLocation";
    public static final String STS_SERVICE_NAME ="sts-service-name";
    public static final String STS_PORT_NAME ="sts-port-name";
    public static final String STS_NAMESPACE ="sts-namespace";
    public static final String LIFE_TIME = "LifeTime";
    
    protected String protocol;
    
    protected String stsEndpoint;
    
    protected String stsMEXAddress = null;
    
    protected String stsWSDLLocation = null;;
    
    protected String stsServiceName = null;
    
    protected String stsPortName = null;
    
    protected String stsNamespace = null;

    protected SecondaryIssuedTokenParameters sisPara = null;

    private Map<String, Object> otherOptions = new HashMap<String, Object>();
    
    protected STSIssuedTokenConfiguration(){

    }
    protected STSIssuedTokenConfiguration(String stsEndpoint, String stsMEXAddress){
        this(PROTOCOL_10, stsEndpoint, stsMEXAddress);
    }
    protected STSIssuedTokenConfiguration(String protocol, String stsEndpoint, String stsMEXAddress){
        this.protocol = protocol;
        this.stsEndpoint = stsEndpoint;
        this.stsMEXAddress = stsMEXAddress;
    }
    
    protected STSIssuedTokenConfiguration(String stsEndpoint, 
                          String stsWSDLLocation, String stsServiceName, String stsPortName, String stsNamespace){
        this(PROTOCOL_10, stsEndpoint, stsWSDLLocation, stsServiceName, stsPortName, stsNamespace);
    }
    
    protected STSIssuedTokenConfiguration(String protocol, String stsEndpoint, 
                          String stsWSDLLocation, String stsServiceName, String stsPortName, String stsNamespace){
        this.protocol = protocol;
        this.stsEndpoint = stsEndpoint;
        this.stsWSDLLocation = stsWSDLLocation;
        this.stsServiceName = stsServiceName;
        this.stsPortName = stsPortName;
        this.stsNamespace = stsNamespace;
    }
    
    public String getProtocol(){
        return protocol;
    }
     
    public String getSTSEndpoint(){
        return this.stsEndpoint;
    }
    
    public String getSTSMEXAddress(){
        return this.stsMEXAddress;
    }
    
    public String getSTSWSDLLocation(){
        return this.stsWSDLLocation;
    }
    
    public String getSTSServiceName(){
        return this.stsServiceName;
    }
    
    public String getSTSPortName(){
        return this.stsPortName;
    }
    
    public String getSTSNamespace(){
        return this.stsNamespace;
    }

    public SecondaryIssuedTokenParameters getSecondaryIssuedTokenParameters(){
        return this.sisPara;
    }

    public Map<String, Object> getOtherOptions(){
        return this.otherOptions;
    }
    
    public abstract String getTokenType();
    
    public abstract String getKeyType();
    
    public abstract long getKeySize();
    
    public abstract String getSignatureAlgorithm();
    
    public abstract String getEncryptionAlgorithm();
    
    public abstract String getCanonicalizationAlgorithm();
    
    public abstract String getKeyWrapAlgorithm();
    
    public abstract String getSignWith();
    
    public abstract String getEncryptWith();
    
    public abstract Claims getClaims();
    
    public abstract Token getOBOToken();
}
