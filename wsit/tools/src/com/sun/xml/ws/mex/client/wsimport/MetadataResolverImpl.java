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

import java.net.URI;

import com.sun.xml.ws.mex.client.MetadataClient;
import com.sun.xml.ws.mex.client.schema.Metadata;
import com.sun.xml.ws.api.wsdl.parser.MetaDataResolver;
import com.sun.xml.ws.api.wsdl.parser.ServiceDescriptor;

import org.xml.sax.EntityResolver;

/**
 * Plugin to wsimport for mex/ws-transfer requests.
 */
public class MetadataResolverImpl extends MetaDataResolver {
    
    MetadataClient mClient;
    EntityResolver resolver;
    
    protected MetadataResolverImpl(EntityResolver resolver) {
        mClient = new MetadataClient();
        this.resolver = resolver;
    }
    
    /**
     * This method is called by JAX-WS code to retrieve metadata.
     * The contract is that, if there are problems trying to get the
     * metadata with mex, this method returns null and the JAX-WS
     * code can try retrieving it another way (for instance, with
     * a ?wsdl http GET call).
     */
    public ServiceDescriptor resolve(URI location) {
        Metadata mData = mClient.retrieveMetadata(location.toString());
        if (mData == null) {
            return null;
        }
        return new ServiceDescriptorImpl(mData);
    }
    
}
