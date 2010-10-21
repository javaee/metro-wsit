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

package com.sun.xml.ws.api.tx.at;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.xml.namespace.QName;
import javax.xml.ws.spi.WebServiceFeatureAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
@WebServiceFeatureAnnotation(id = TransactionalFeature.ID, bean = TransactionalFeature.class)
public @interface Transactional {

    enum TransactionFlowType {
        MANDATORY, SUPPORTS, NEVER
    }

    enum Version {

        WSAT10("wsat10", WsatNamespace.WSAT200410),
        WSAT11("wsat11", WsatNamespace.WSAT200606),
        WSAT12("wsat12", WsatNamespace.WSAT200606),
        DEFAULT("wsat", WsatNamespace.WSAT200606);

        public final QName qname;
        public final WsatNamespace namespaceVersion;

        Version(String prefix, WsatNamespace namespaceVersion) {
            this.namespaceVersion = namespaceVersion;

            this.qname = new QName((namespaceVersion != null) ? namespaceVersion.namespace : "", "ATAssertion", prefix);
        }

        public QName getQName() {
            return qname;
        }

        public static Version forNamespaceVersion(WsatNamespace nsVersion) {
            for (Version version : Version.values()) {
                if (version == WSAT11 || version == DEFAULT) {
                    continue; // return WSAT12 for this namespace
                }

                if (version.namespaceVersion == nsVersion) {
                    return version;
                }
            }
            return DEFAULT;
        }

        public static Version forNamespaceUri(String ns) {
            for (Version version : Version.values()) {
                if (version == WSAT11 || version == DEFAULT) {
                    continue; // return WSAT12 for this namespace
                }

                if (version.qname.getNamespaceURI().equals(ns)) {
                    return version;
                }
            }
            return DEFAULT;
        }
    }

    /**
     * Specifies if this feature is enabled or disabled.
     */
    boolean enabled() default true;

    /**
     * Specifies the transaction flow type.
     */
    TransactionFlowType value() default TransactionFlowType.SUPPORTS;

    /**
     * Specifies the version of WS-AT being supported, when used together with
     * @WebServiceRef, the default value Version.WSAT10. When used together with
     * @Webservice and @Provider, all versions will be supported, the real version
     * will be determined by the request message.
     */
    Version version() default Version.DEFAULT;
}
