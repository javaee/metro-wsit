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

package common;

import javax.security.auth.Subject;
import com.sun.xml.ws.api.security.trust.*;
import java.security.Principal;
import java.util.*;
import javax.xml.namespace.*;

public class SampleSTSAttributeProvider implements STSAttributeProvider {
    
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

	QName testQName = new QName("http://sun.com","Role");
	List<String> testAttrs = new ArrayList<String>();
	testAttrs.add(getUserRole(name));
	attrs.put(testQName,testAttrs);

	return attrs;
    }  
    
    private String getUserPseduoName(String userName){
        
        if ("alice".equals(userName)){
            return "123";
        }
        
        if ("bob".equals(userName)){
            return "231";
        }
        
        return null;
    }
    
    private String getUserRole(String userName){
        if ("alice".equals(userName)){
            return "staff ";
        }
        
        if ("bob".equals(userName)){
            return "manager";
        }
        
        return null;
    }
}
