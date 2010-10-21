/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package common;

import javax.security.auth.Subject;
import com.sun.xml.ws.api.security.trust.*;
import java.security.Principal;
import java.util.*;
import javax.xml.namespace.*;

/**
 *  <wst:Claims Dialect=”http://schemas.xmlsoap.org/ws/2005/05/identity”
 *       xmlns:wst="http://docs.oasis-open.org/ws-sx/ws-trust/200512"
 *       xmlns:ic="http://schemas.xmlsoap.org/ws/2005/05/identity">
 *      <ic:ClaimType Uri=”http://schemas.xmlsoap.org/ws/2005/05/identity/claims/locality”/>
 *      <ic:ClaimType Uri=”http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role”/>
 *  </wst:Claims>
 * @author jdg
 */

public class MySTSAttributeProvider implements STSAttributeProvider {
    
    public Map<QName, List<String>> getClaimedAttributes(Subject subject, String appliesTo, String tokenType, Claims claims)
    {
        String name = null; 
        
        Set<Principal> principals = subject.getPrincipals();
        if (principals != null){
            final Iterator iterator = principals.iterator();
            while (iterator.hasNext()){
                String cnName = principals.iterator().next().getName();
                int pos = cnName.indexOf("=");
                name = cnName.substring(pos+1);
                break;
            }       
        }
        
        Map<QName, List<String>> attrs = new HashMap<QName, List<String>>();

        QName nameIdQName = new QName("http://sun.com",STSAttributeProvider.NAME_IDENTIFIER);
        List<String> nameIdAttrs = new ArrayList<String>();
        nameIdAttrs.add(getUserPseduoName(name));
        attrs.put(nameIdQName,nameIdAttrs);

        if (claims != null){
            MyClaims myClaims = new MyClaims(claims);
            List<String> claimTypes = myClaims.getClaimsTypes();
            for (String claimType : claimTypes){
                if (MyClaims.ROLE.equals(claimType)){
                    QName testQName = new QName("http://sun.com","Role");
                    List<String> testAttrs = new ArrayList<String>();
                    testAttrs.add(getUserRole(name));
                    attrs.put(testQName,testAttrs);
                } else if (MyClaims.LOCALITY.equals(claimType)){
                    QName testQName = new QName("http://sun.com","Locality");
                    List<String> testAttrs = new ArrayList<String>();
                    testAttrs.add(getUserLocality(name));
                    attrs.put(testQName,testAttrs);
                }
            }
        }
        return attrs;
    }  
    
    private String getUserPseduoName(String userName){
        
        if ("alice".equals(userName)){
            return "123";
        }
        
        if ("bob".equals(userName)){
            return "231";
        }
        
        return "456";
    }
    
    private String getUserRole(String userName){
        if ("alice".equals(userName)){
            return "staff ";
        }
        
        if ("bob".equals(userName)){
            return "manager";
        }
        
        return "staff";
    }

    private String getUserLocality(String userName){
        if ("alice".equals(userName)){
            return "Santa Clara, CA";
        }

        if ("bob".equals(userName)){
            return "New York, NY";
        }

        return "Santa Clara, CA";
    }
}
