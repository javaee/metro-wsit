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
import com.sun.istack.NotNull;

import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import java.util.Map;


/**
 * This class encapsulates the genertated 2004/10 version of {@link com.sun.xml.ws.tx.webservice.member.coord.CoordinationContextType}
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.4 $
 * @since 1.0
 */
public class CoordinationContext200410 extends CoordinationContextBase {
    CoordinationContext context;

    public CoordinationContext200410() {
        context = new CoordinationContext();
    }

    /**
     * wrapper around JAXB generated CoordinationContext type
     * @param cc coordination context
     */
    public CoordinationContext200410(@NotNull CoordinationContext cc) {
        context = cc;
    }

    @NotNull
    public String getIdentifier() {
        return context.getIdentifier().getValue();
    }

    public void setIdentifier(@NotNull final String identifier) {
        final Identifier id = new Identifier();
        id.setValue(identifier);
        context.setIdentifier(id);
    }

    public long getExpires() {
        final Expires exp = context.getExpires();
        if (exp == null) {
            return 0L;
        } else {
            return exp.getValue();
        }
    }

    public void setExpires(final long expires) {
        if (expires <= 0L) {
            context.setExpires(null);
        } else {
            final Expires exp = new Expires();
            exp.setValue(expires);
            context.setExpires(exp);
        }
    }

    @NotNull
    public String getCoordinationType() {
        return context.getCoordinationType();
    }

    public void setCoordinationType(@NotNull final String coordinationType) {
        context.setCoordinationType(coordinationType);
    }

    @NotNull
    public EndpointReference getRegistrationService() {
        return context.getRegistrationService();
    }

    public void setRegistrationService(@NotNull final EndpointReference registrationService) {
        context.setRegistrationService((MemberSubmissionEndpointReference) registrationService);
    }

    public Object getValue() {
        return context;
    }

    public Map<QName, String> getOtherAttributes() {
        return context.getOtherAttributes();
    }

}
