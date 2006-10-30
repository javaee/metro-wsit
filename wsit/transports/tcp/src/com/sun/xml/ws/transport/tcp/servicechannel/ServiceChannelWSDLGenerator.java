/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
import com.sun.xml.ws.api.server.WSEndpoint;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class ServiceChannelWSDLGenerator {

    private static final String TCP_ENDPOINT_ADDRESS_STUB = "tcp://CHANGED_BY_RUNTIME";
    
    public static void main(String[] args) throws Exception {
        QName serviceName = WSEndpoint.getDefaultServiceName(ServiceChannelWSImpl.class);
        QName portName = WSEndpoint.getDefaultPortName(serviceName, ServiceChannelWSImpl.class);
        BindingID bindingId = BindingID.parse(ServiceChannelWSImpl.class);
        WSBinding binding = bindingId.createBinding();
        Map docs = new HashMap();
        
        WSEndpoint<?> endpoint = WSEndpoint.create(
                ServiceChannelWSImpl.class, true,
                InstanceResolver.createSingleton(ServiceChannelWSImpl.class.newInstance()).createInvoker(),
                serviceName, portName, null, binding,
                null, docs.values(), (URL) null
                );
        
        DocumentAddressResolver resolver = new DocumentAddressResolver() {
            public String getRelativeAddressFor(SDDocument current, SDDocument referenced) {
                if (current.isWSDL() && referenced.isSchema() && referenced.getURL().getProtocol().equals("file")) {
                    return referenced.getURL().getFile().substring(1);
                }
                
                return referenced.getURL().toExternalForm();
            }
        };
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /* seems now transformer doesnt support "pretty-output",
        but may be for future using it will make sense */
        TransformerFactory tFactory =
                TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT,"yes");
        
        for(Iterator<SDDocument> it = endpoint.getServiceDefinition().iterator(); it.hasNext();) {
            SDDocument document = it.next();
            baos.reset();
            
            document.writeTo(new PortAddressResolver() {
                public @Nullable String getAddressFor(@NotNull String portName) {
                    return TCP_ENDPOINT_ADDRESS_STUB;
                }
            }, resolver, baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            
            FileOutputStream fos = new FileOutputStream("./etc/" + document.getURL().getFile());
            Source source = new StreamSource(bais);
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
            fos.close();
            bais.close();
        }
        
        baos.close();
    }
}
