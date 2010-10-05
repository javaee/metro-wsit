/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.transport.tcp.servicechannel;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.DocumentAddressResolver;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public final class ServiceChannelWSDLGenerator {

    private static final String TCP_ENDPOINT_ADDRESS_STUB = TCPConstants.PROTOCOL_SCHEMA + "://CHANGED_BY_RUNTIME";
    
    public static void main(final String[] args) throws Exception {
        final QName serviceName = WSEndpoint.getDefaultServiceName(ServiceChannelWSImpl.class);
        final QName portName = WSEndpoint.getDefaultPortName(serviceName, ServiceChannelWSImpl.class);
        final BindingID bindingId = BindingID.parse(ServiceChannelWSImpl.class);
        final WSBinding binding = bindingId.createBinding();
        final Collection<SDDocumentSource> docs = new ArrayList<SDDocumentSource>(0);
        
        final WSEndpoint<?> endpoint = WSEndpoint.create(
                ServiceChannelWSImpl.class, true,
                null,
                serviceName, portName, null, binding,
                null, docs, (URL) null
                );
        
        final DocumentAddressResolver resolver = new DocumentAddressResolver() {
            public String getRelativeAddressFor(SDDocument current, SDDocument referenced) {
                if (current.isWSDL() && referenced.isSchema() && referenced.getURL().getProtocol().equals("file")) {
                    return referenced.getURL().getFile().substring(1);
                }
                
                return referenced.getURL().toExternalForm();
            }
        };
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /* seems now transformer doesnt support "pretty-output",
        but may be for future using it will make sense */
        final TransformerFactory tFactory =
                TransformerFactory.newInstance();
        final Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT,"yes");
        
        for(final Iterator<SDDocument> it = endpoint.getServiceDefinition().iterator(); it.hasNext();) {
            final SDDocument document = it.next();
            baos.reset();
            
            document.writeTo(new PortAddressResolver() {
                public @Nullable String getAddressFor(QName serviceName, @NotNull String portName) {
                    return TCP_ENDPOINT_ADDRESS_STUB;
                }
            }, resolver, baos);
            final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            
            final FileOutputStream fos = new FileOutputStream("./etc/" + document.getURL().getFile());
            final Source source = new StreamSource(bais);
            final StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
            fos.close();
            bais.close();
        }
        
        baos.close();
    }
}
