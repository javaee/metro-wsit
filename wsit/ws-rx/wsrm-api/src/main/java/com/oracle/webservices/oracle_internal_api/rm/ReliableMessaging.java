/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

// GENERATED CODE.  DO NOT EDIT.
// GENERATED FROM reliable-messaging-internap-api-properties.xml

package com.oracle.webservices.oracle_internal_api.rm;

import javax.xml.ws.spi.WebServiceFeatureAnnotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  * Configure WS-ReliableMessaging.
  */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebServiceFeatureAnnotation(id=ReliableMessagingFeature.ID, bean=ReliableMessagingFeature.class)
public @interface ReliableMessaging
{
    /**
     * 
         Enable this feature.  Defaults to true.
     */
    public boolean enabled() default true;

    /**
     * 
       Specifies the period (in milliseconds) of a sequence manager maintenance task execution.
       Default value: 60000
     */
    public long sequenceManagerMaintenancePeriod() default 60000;

    /**
     * 
       Specifies how many concurrently active RM sessions (measured based on
       inbound RM sequences) the sequence manager dedicated to the WS Endpoint
       accepts before starting to refuse new requests for sequence creation.
       Default value: 100
     */
    public int maxConcurrentSession() default 100;

    /**
     * 
       When an XA TX is used to link the RMD with the user's ProviderRequest, this specifies the timeout value in seconds.
       The default value of 0 says to use the system default.
     */
    public int userTransactionTimeout() default 0;
}
