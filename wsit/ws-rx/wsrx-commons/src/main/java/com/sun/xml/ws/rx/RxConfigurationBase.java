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

package com.sun.xml.ws.rx;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import org.glassfish.gmbal.ManagedObjectManager;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public abstract class RxConfigurationBase implements RxConfiguration {

    private final boolean rmEnabled;
    private final boolean mcSupportEnabled;
    private final SOAPVersion soapVersion;
    private final AddressingVersion addressingVersion;
    private final boolean requestResponseDetected;
    private final ManagedObjectManager managedObjectManager;
    private final HighAvailabilityProvider haProvider;

    protected RxConfigurationBase(
            final boolean isRmEnabled,
            final boolean isMcEnabled,
            final SOAPVersion soapVersion,
            final AddressingVersion addressingVersion,
            final boolean requestResponseDetected,
            final ManagedObjectManager managedObjectManager,
            final HighAvailabilityProvider haProvider) {
        this.rmEnabled = isRmEnabled;
        this.mcSupportEnabled = isMcEnabled;

        this.soapVersion = soapVersion;
        this.addressingVersion = addressingVersion;
        this.requestResponseDetected = requestResponseDetected;
	this.managedObjectManager = managedObjectManager;
        this.haProvider = haProvider;
    }

    public boolean isReliableMessagingEnabled() {
        return rmEnabled;
    }

    public boolean isMakeConnectionSupportEnabled() {
        return mcSupportEnabled;
    }

    public SOAPVersion getSoapVersion() {
        return soapVersion;
    }

    public AddressingVersion getAddressingVersion() {
        return addressingVersion;
    }

    public boolean requestResponseOperationsDetected() {
        return requestResponseDetected;
    }

    public ManagedObjectManager getManagedObjectManager() {
	return managedObjectManager;
    }

    public HighAvailabilityProvider getHighAvailabilityProvider() {
        return haProvider;
    }
}
