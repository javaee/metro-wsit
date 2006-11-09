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
package com.sun.xml.ws.tx.coordinator;

import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.tx.webservice.member.coord.CoordinationContext;
import com.sun.xml.ws.tx.webservice.member.coord.CoordinationContextType.Identifier;
import com.sun.xml.ws.tx.webservice.member.coord.Expires;

import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import java.util.Map;


/**
 * This class encapsulates the genertated 2004/10 version of {@link com.sun.xml.ws.tx.webservice.member.coord.CoordinationContextType}
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.1 $
 * @since 1.0
 */
public class CoordinationContext200410 extends CoordinationContextBase {
    CoordinationContext context;

    public CoordinationContext200410() {
        context = new CoordinationContext();
    }

    /**
     * wrapper around JAXB generated CoordinationContext type
     */
    public CoordinationContext200410(CoordinationContext cc) {
        context = cc;
    }

    public String getIdentifier() {
        Identifier id = context.getIdentifier();
        if (id != null) {
            return id.getValue();
        } else {
            return null;
        }
    }

    public void setIdentifier(String identifier) {
        Identifier id = new Identifier();
        id.setValue(identifier);
        context.setIdentifier(id);
    }

    public long getExpires() {
        Expires exp = context.getExpires();
        if (exp != null) {
            return exp.getValue();
        } else {
            return 0L;
        }
    }

    public void setExpires(long expires) {
        assert(!(expires < 0L));
        if (expires != 0L) {
            Expires exp = new Expires();
            exp.setValue(expires);
            context.setExpires(exp);
        } else {
            context.setExpires(null);
        }
    }

    public String getCoordinationType() {
        return context.getCoordinationType();
    }

    public void setCoordinationType(String coordinationType) {
        context.setCoordinationType(coordinationType);
    }

    public EndpointReference getRegistrationService() {
        return context.getRegistrationService();
    }

    public void setRegistrationService(EndpointReference registrationService) {
        context.setRegistrationService((MemberSubmissionEndpointReference) registrationService);
    }

    public Object getValue() {
        return context;
    }

    public Map<QName, String> getOtherAttributes() {
        return context.getOtherAttributes();
    }

}
