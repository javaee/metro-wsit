/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rx.rm.api;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Class contains constants for policy namespaces used by this RM implementation.
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public enum RmAssertionNamespace {

    WSRMP_200502("http://schemas.xmlsoap.org/ws/2005/02/rm/policy", "wsrmp10"),
    WSRMP_200702("http://docs.oasis-open.org/ws-rx/wsrmp/200702", "wsrmp"),
    MICROSOFT_200502("http://schemas.microsoft.com/net/2005/02/rm/policy", "net30rmp"),
    MICROSOFT_200702("http://schemas.microsoft.com/ws-rx/wsrmp/200702", "net35rmp"),
    METRO_200603("http://sun.com/2006/03/rm", "sunrmp"),
    METRO_CLIENT_200603("http://sun.com/2006/03/rm/client", "sunrmcp"),
    METRO_200702("http://java.sun.com/xml/ns/metro/ws-rx/wsrmp/200702", "metro");
    
    public static List<String> namespacesList() {
        List<String> retVal = new ArrayList<String>(RmAssertionNamespace.values().length);
        for (RmAssertionNamespace pns : RmAssertionNamespace.values()) {
            retVal.add(pns.toString());
        }
        return retVal;
    }

    private final String namespace;
    private final String prefix;

    private RmAssertionNamespace(String namespace, String prefix) {
        this.namespace = namespace;
        this.prefix = prefix;
    }

    public String defaultPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return namespace;
    }

    public QName getQName(String name) {
        return new QName(namespace, name);
    }
}
