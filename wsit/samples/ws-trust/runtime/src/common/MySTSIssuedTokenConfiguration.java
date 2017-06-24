/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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
