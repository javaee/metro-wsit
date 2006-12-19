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

import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import java.util.Map;

/**
 * This class encapsulates the genertated 2006/03 version of CoordinationContextType
 * <p/>
 * Just a placeholder for future implementation work...
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.2 $
 * @since 1.0
 */
public class CoordinationContext200603 extends CoordinationContextBase {

    public CoordinationContext200603() {
        throw new UnsupportedOperationException();
    }

    public String getIdentifier() {
        throw new UnsupportedOperationException();
    }

    public void setIdentifier(final String identifier) {
        throw new UnsupportedOperationException();
    }

    public long getExpires() {
        throw new UnsupportedOperationException();
    }

    public void setExpires(final long expires) {
        throw new UnsupportedOperationException();
    }

    public String getCoordinationType() {
        throw new UnsupportedOperationException();
    }

    public void setCoordinationType(final String coordinationType) {
        throw new UnsupportedOperationException();
    }

    public EndpointReference getRegistrationService() {
        throw new UnsupportedOperationException();
    }

    public void setRegistrationService(final EndpointReference registrationService) {
        throw new UnsupportedOperationException();
    }

    public Object getValue() {
        return null;
    }

    public Map<QName, String> getOtherAttributes() {
        return null;
    }
}
