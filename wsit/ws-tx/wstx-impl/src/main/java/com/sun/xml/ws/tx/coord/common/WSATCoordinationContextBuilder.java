/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.tx.coord.common;

import com.sun.xml.ws.tx.coord.common.types.CoordinationContextIF;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.tx.at.Transactional;


public abstract class WSATCoordinationContextBuilder {
    protected String coordinationType;
    protected String identifier;
    protected long expires;
    protected String registrationCoordinatorAddress;
    protected String txId;
    protected boolean mustUnderstand = false;
    protected SOAPVersion soapVersion = SOAPVersion.SOAP_11;

    public static WSATCoordinationContextBuilder newInstance(Transactional.Version version) {
        if(Transactional.Version.WSAT10 == version)
        return new com.sun.xml.ws.tx.coord.v10.WSATCoordinationContextBuilderImpl();
        else if(Transactional.Version.WSAT11 == version || Transactional.Version.WSAT12 == version) {
          return new com.sun.xml.ws.tx.coord.v11.WSATCoordinationContextBuilderImpl();
        }else {
            throw new IllegalArgumentException(version + "is not a supported ws-at version");
        }
    }


    public WSATCoordinationContextBuilder txId(String txId) {
        this.txId = txId;
        return this;
    }

    public WSATCoordinationContextBuilder registrationCoordinatorAddress(String registrationCoordinatorAddress) {
        this.registrationCoordinatorAddress = registrationCoordinatorAddress;
        return this;
    }

    public WSATCoordinationContextBuilder soapVersion(SOAPVersion soapVersion){
        if(soapVersion == null)
            throw new IllegalArgumentException("SOAP version can't null!");
        this.soapVersion = soapVersion;
        return this;
    }

    public WSATCoordinationContextBuilder mustUnderstand(boolean mustUnderstand){
        this.mustUnderstand = mustUnderstand;
        return this;
    }

    public WSATCoordinationContextBuilder expires(long expires) {
        this.expires = expires;
        return this;
    }


    public CoordinationContextIF build() {
        CoordinationContextBuilder builder = configBuilder();
        return builder.build();
    }


    private CoordinationContextBuilder configBuilder() {
        if (registrationCoordinatorAddress == null)
            registrationCoordinatorAddress = getDefaultRegistrationCoordinatorAddress();
        CoordinationContextBuilder builder = newCoordinationContextBuilder();
        builder.coordinationType(getCoordinationType()).
                address(registrationCoordinatorAddress).
                identifier("urn:uuid:" + txId).
                txId(txId).
                expires(expires).
                soapVersion(soapVersion).
                mustUnderstand(mustUnderstand);
        return builder;
    }

    protected abstract CoordinationContextBuilder newCoordinationContextBuilder();

    protected abstract String getCoordinationType();

    protected abstract String getDefaultRegistrationCoordinatorAddress();
}
