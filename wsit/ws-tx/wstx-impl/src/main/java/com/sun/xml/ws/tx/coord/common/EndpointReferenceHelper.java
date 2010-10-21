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

package com.sun.xml.ws.tx.coord.common;

import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.lang.reflect.Field;
import java.util.List;


public abstract class EndpointReferenceHelper {


    public static EndpointReferenceHelper newInstance(EndpointReference epr){
      if(epr == null) throw new IllegalArgumentException("EndpointReference can't be null");
      if(epr instanceof MemberSubmissionEndpointReference)
        return new MemberSubmissionEndpointReferenceHelper((MemberSubmissionEndpointReference) epr);
      else if(epr instanceof W3CEndpointReference)
        return new W3CEndpointReferenceHelper((W3CEndpointReference) epr);
      else throw new IllegalArgumentException(epr.getClass() +"is not a supported EndpointReference");
    }


    public abstract String getAddress();

    public abstract  Node[] getReferenceParameters();

    static class MemberSubmissionEndpointReferenceHelper extends EndpointReferenceHelper {
        MemberSubmissionEndpointReference epr;

        MemberSubmissionEndpointReferenceHelper(MemberSubmissionEndpointReference epr) {
            this.epr = epr;
        }

        @Override
        public String getAddress() {
            return epr.addr.uri;
        }

        @Override
        public Node[] getReferenceParameters() {
            return epr.referenceParameters.elements.toArray(new Element[0]);
        }
    }

    static class W3CEndpointReferenceHelper extends EndpointReferenceHelper {
        private static Field address = null;
        private static Field referenceParameters = null;
        private static Class address_class = null;
        private static Class referenceParameters_class = null;
        private static Field uri = null;
        private static Field elements = null;

        static {
            try {
                address = W3CEndpointReference.class.getDeclaredField("address");
                address.setAccessible(true);
                referenceParameters = W3CEndpointReference.class.getDeclaredField("referenceParameters");
                referenceParameters.setAccessible(true);
                address_class = Class.forName("javax.xml.ws.wsaddressing.W3CEndpointReference$Address");
                referenceParameters_class = Class.forName("javax.xml.ws.wsaddressing.W3CEndpointReference$Elements");
                uri = address_class.getDeclaredField("uri");
                uri.setAccessible(true);
                elements = referenceParameters_class.getDeclaredField("elements");
                elements.setAccessible(true);
            } catch (Exception e) {
                throw new AssertionError(e);
            }

        }

        W3CEndpointReference epr;

        W3CEndpointReferenceHelper(W3CEndpointReference epr) {
            this.epr = epr;
        }

        @Override
        public String getAddress() {
            try {
                return (String) uri.get(address.get(epr));
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }

        @Override
        public Node[] getReferenceParameters() {
            try {
                return ((List<Element>) elements.get(referenceParameters.get(epr))).toArray(new Element[0]);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
    }

}
