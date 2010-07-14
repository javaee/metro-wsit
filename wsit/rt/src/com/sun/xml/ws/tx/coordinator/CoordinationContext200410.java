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
 * @version $Revision: 1.6.22.2 $
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
