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

package com.sun.xml.ws.api.tx.at;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Enumeration of all supported WS-AT namespaces
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public enum WsatNamespace {

    WSAT200410("wsat200410", "http://schemas.xmlsoap.org/ws/2004/10/wsat"),
    WSAT200606("wsat200410", "http://docs.oasis-open.org/ws-tx/wsat/2006/06");
    //
    public static List<String> namespacesList() {
        List<String> retVal = new ArrayList<String>(WsatNamespace.values().length);
        for (WsatNamespace wsatNamespaceEnum : WsatNamespace.values()) {
            retVal.add(wsatNamespaceEnum.namespace);
        }
        return retVal;
    }
    //
    public final String defaultPrefix;
    public final String namespace;

    private WsatNamespace(String defaultPrefix, String namespace) {
        this.defaultPrefix = defaultPrefix;
        this.namespace = namespace;
    }

    public QName createFqn(final String name) {
        return new QName(namespace, name, defaultPrefix);
    }

    public QName createFqn(final String prefix, final String name) {
        return new QName(namespace, name, prefix);
    }

    public static WsatNamespace forNamespaceUri(String uri) {
        for (WsatNamespace ns : WsatNamespace.values()) {
            if (ns.namespace.equals(uri)) {
                return ns;
            }
        }

        return null;
    }
}
