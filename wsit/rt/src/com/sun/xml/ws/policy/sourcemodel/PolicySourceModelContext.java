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

package com.sun.xml.ws.policy.sourcemodel;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marek Potociar, Jakub Podlesak
 */
public final class PolicySourceModelContext {
    
    Map<URI,PolicySourceModel> policyModels;
    
    /** Creates a new instance of ModelUnmarshallerContext */
    private PolicySourceModelContext() {
    }
    
    private Map<URI,PolicySourceModel> getModels() {
        if (null==policyModels) {
            policyModels = new HashMap<URI,PolicySourceModel>();
        }
        return policyModels;
    }
    
    public void addModel(URI modelUri, PolicySourceModel model) {
        getModels().put(modelUri,model);
    }
    
    public static PolicySourceModelContext createContext() {
        return new PolicySourceModelContext();
    }
    
    public boolean containsModel(URI modelUri) {
        return getModels().containsKey(modelUri);
    }
    
    PolicySourceModel retrieveModel(URI modelUri) throws PolicyModelAccessException {
        return getModels().get(modelUri);
    }
    
    PolicySourceModel retrieveModel(URI modelUri, URI digestAlgorithm, String digest) throws PolicyModelAccessException {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
    
    public String toString() {
        return policyModels.toString();
    }
}
