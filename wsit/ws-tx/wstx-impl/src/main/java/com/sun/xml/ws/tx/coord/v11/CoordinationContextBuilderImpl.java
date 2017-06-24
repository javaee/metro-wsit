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

package com.sun.xml.ws.tx.coord.v11;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.api.message.Header;
import org.w3c.dom.Element;
import com.sun.xml.ws.tx.coord.common.CoordinationContextBuilder;
import com.sun.xml.ws.tx.coord.common.types.CoordinationContextIF;
import com.sun.xml.ws.tx.coord.common.WSCUtil;
import com.sun.xml.ws.tx.coord.v11.types.CoordinationContext;
import com.sun.xml.ws.tx.coord.v11.types.CoordinationContextType;
import com.sun.xml.ws.tx.coord.v11.types.Expires;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;


public class CoordinationContextBuilderImpl extends CoordinationContextBuilder {
    @Override
    public CoordinationContextIF build() {

        CoordinationContext cct = buildContext();

        return XmlTypeAdapter.adapt(cct);
    }

    protected CoordinationContextIF _fromHeader(Header header) {
        try {
            Unmarshaller unmarshaller = XmlTypeAdapter.CoordinationContextImpl.jaxbContext.createUnmarshaller();
            CoordinationContext cct = header.readAsJAXB(unmarshaller);
            return XmlTypeAdapter.adapt(cct);
        } catch (JAXBException e) {
            throw new WebServiceException(e);
        }
    }

    public JAXBRIContext getJAXBRIContext() {
        return XmlTypeAdapter.CoordinationContextImpl.jaxbContext;
    }

    private CoordinationContext buildContext() {
        CoordinationContext cct = new CoordinationContext();
        if (mustUnderstand) {
           if(soapVersion == null){
               throw new WebServiceException("SOAP version is not specified!");
           }
           cct.getOtherAttributes().put(new QName(soapVersion.nsUri,"mustUnderstand"), "1");
        }
        cct.setCoordinationType(coordinationType);

        CoordinationContextType.Identifier IdentifierObj = new CoordinationContextType.Identifier();
        IdentifierObj.setValue(identifier);
        cct.setIdentifier(IdentifierObj);

        Expires expiresObj = new Expires();
        expiresObj.setValue(expires);
        cct.setExpires(expiresObj);

        cct.setRegistrationService(getEPR());
        return cct;
    }


    private W3CEndpointReference getEPR() {
        Element referenceParameter = WSCUtil.referenceElementTxId(txId);
        Element referenceParameter2 = WSCUtil.referenceElementRoutingInfo();
        return new W3CEndpointReferenceBuilder().address(address).
                referenceParameter(referenceParameter).referenceParameter(referenceParameter2).build();
    }

}
