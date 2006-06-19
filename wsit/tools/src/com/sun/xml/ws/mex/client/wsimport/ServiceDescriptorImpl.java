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
package com.sun.xml.ws.mex.client.wsimport;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import com.sun.tools.ws.api.ServiceDescriptor;
import static com.sun.xml.ws.mex.MetadataConstants.SCHEMA_DIALECT;
import static com.sun.xml.ws.mex.MetadataConstants.WSDL_DIALECT;
import com.sun.xml.ws.mex.client.schema.Metadata;
import com.sun.xml.ws.mex.client.schema.MetadataSection;

import org.w3c.dom.Node;

public class ServiceDescriptorImpl extends ServiceDescriptor {
    
    private List<Source> wsdls;
    private List<Source> schemas;
    
    public ServiceDescriptorImpl(Metadata mData) {
        wsdls = new ArrayList<Source>();
        schemas = new ArrayList<Source>();
        for (MetadataSection section : mData.getMetadataSection()) {
            String dialect = section.getDialect();
            if (dialect.equals(WSDL_DIALECT)) {
                wsdls.add(createSource(section));
            } else if (dialect.equals(SCHEMA_DIALECT)) {
                schemas.add(createSource(section));
            } else {
                // log unknown dialect
            }
        }
    }
    
    public List<Source> getWSDLs() {
        return wsdls;
    }

    public List<Source> getSchemas() {
        return schemas;
    }
    
    private Source createSource(MetadataSection section) {
        Node n = (Node) section.getAny().get(0);
        return new DOMSource(n);
    }
    
}
