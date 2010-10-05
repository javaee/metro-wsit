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
package com.sun.xml.ws.tx.coord.common;

import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.sun.xml.ws.api.tx.at.Transactional;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.util.ArrayList;
import java.util.List;


public abstract class EndpointReferenceBuilder<T extends EndpointReference> {
    protected String address;
    protected List<Element> referenceParameters = new ArrayList<Element>();

    public static EndpointReferenceBuilder newInstance(Transactional.Version version) {
        if(Transactional.Version.WSAT10 == version||Transactional.Version.DEFAULT == version)
          return MemberSubmission();
        else if(Transactional.Version.WSAT11 == version || Transactional.Version.WSAT12 == version) {
          return W3C();
        }else {
            throw new IllegalArgumentException(version + "is not a supported ws-at version");
        }
    }
    public  static EndpointReferenceBuilder<W3CEndpointReference> W3C() {
      return new  W3CEndpointReferenceBuilder();
    }

    public  static EndpointReferenceBuilder<MemberSubmissionEndpointReference> MemberSubmission() {
      return new  MemberSubmissionEndpointReferenceBuilder();
    }

    public EndpointReferenceBuilder<T> address(String address){
        this.address = address;
        return this;
    }

    public EndpointReferenceBuilder<T> referenceParameter(Element... elements){
        for (Element element : elements) {
            referenceParameters.add(element);
        }
        return this;
    }

    public EndpointReferenceBuilder<T> referenceParameter(Node... elements){
        for (Node element : elements) {
            referenceParameters.add((Element) element);
        }
        return this;
    }


    public EndpointReferenceBuilder<T> referenceParameter(List<Element> elements){
        this.referenceParameters.addAll(elements);
        return this;
    }

    public abstract T build();

    static class W3CEndpointReferenceBuilder extends EndpointReferenceBuilder<W3CEndpointReference>{

        public W3CEndpointReference build() {
            javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder builder = new javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder();
            for (int i = 0; i < referenceParameters.size(); i++) {
                Element element =  referenceParameters.get(i);
                builder.referenceParameter(element);
            }
            W3CEndpointReference w3CEndpointReference = builder.address(address).build();
            return w3CEndpointReference;
        }
    }

    static class MemberSubmissionEndpointReferenceBuilder extends EndpointReferenceBuilder<MemberSubmissionEndpointReference>{

        public MemberSubmissionEndpointReference build() {
            MemberSubmissionEndpointReference epr = new MemberSubmissionEndpointReference();
            epr.addr = new MemberSubmissionEndpointReference.Address();
            epr.addr.uri = address;
            epr.referenceParameters = new MemberSubmissionEndpointReference.Elements();
            epr.referenceParameters.elements = new ArrayList<Element>();
            epr.referenceParameters.elements.addAll(referenceParameters);
            return epr;
        }
    }

}
