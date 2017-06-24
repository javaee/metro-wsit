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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *  <wst:Claims Dialect=”http://schemas.xmlsoap.org/ws/2005/05/identity”
 *       xmlns:wst="http://docs.oasis-open.org/ws-sx/ws-trust/200512"
 *       xmlns:ic="http://schemas.xmlsoap.org/ws/2005/05/identity">
 *      <ic:ClaimType Uri=”http://schemas.xmlsoap.org/ws/2005/05/identity/claims/locality”/>
 *      <ic:ClaimType Uri=”http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role”/>
 *  </wst:Claims>
 * @author jdg
 */
public class MyClaims implements Claims {

    public static final String ROLE = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role";
    public static final String LOCALITY = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/locality";
    
    private List<Object> supportingProps = new ArrayList<Object>();
    private String dialect = "http://schemas.xmlsoap.org/ws/2005/05/identity";
    private Map<QName, String> otherAttrs = new HashMap<QName, String>();
    private List<Object> any = new ArrayList<Object>();

    Document doc;

    public MyClaims(){
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            doc = builder.newDocument();

            Element claims = doc.createElementNS("http://docs.oasis-open.org/ws-sx/ws-trust/200512", "Claims");
            doc.appendChild(claims);
        }catch (Exception ex){
            
        }
    }

    public MyClaims(Claims claims){
        this.dialect = claims.getDialect();
        this.any.addAll(claims.getAny());
        this.otherAttrs.putAll(claims.getOtherAttributes());

    }

    public void addClaimType(String claimType){
        Element ct = doc.createElementNS("http://schemas.xmlsoap.org/ws/2005/05/identity", "ClaimType");
        ct.setPrefix("ic");
        ct.setAttribute("xmlns:ic", "http://schemas.xmlsoap.org/ws/2005/05/identity");
        ct.setAttribute("Uri", claimType);
        doc.getDocumentElement().appendChild(ct);

        any.add(ct);
    }

    public List<String> getClaimsTypes(){
        List<String> claimTypes = new ArrayList<String>();
        for (Object ctObj: any){
            Element ctElement = (Element)ctObj;
            String claimType = ctElement.getAttribute("Uri");
            claimTypes.add(claimType);
        }

        return claimTypes;
    }

    public List<Object> getAny() {
        return any;
    }

    public String getDialect() {
        return dialect;
    }

    public Map<QName, String> getOtherAttributes() {
        return otherAttrs;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public List<Object> getSupportingProperties() {
        return supportingProps;
    }
}
