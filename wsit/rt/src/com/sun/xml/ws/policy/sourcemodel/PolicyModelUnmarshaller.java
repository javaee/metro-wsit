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

import com.sun.xml.ws.policy.PolicyException;

/**
 * Abstract class defines interface for policy model unmarshaller implementations that are specific to underlying
 * persistence layer.
 *
 * @author Marek Potociar
 */
public abstract class PolicyModelUnmarshaller {
    private static final PolicyModelUnmarshaller xmlUnmarshaller = new XmlPolicyModelUnmarshaller();
    
    /**
     * Default constructor to ensure we have a common model unmarshaller base, but only our API classes implemented in this
     * package will be able to extend this abstract class. This is to restrict attempts of extending the class from
     * a client code.
     */
    PolicyModelUnmarshaller() {
    }
    
    /**
     * Unmarshalls single policy source model from provided storage reference. Method expects that the storage
     * cursor to be alread placed on the start of a policy expression. Inner comments and whitespaces are skipped
     * in processing. Any other cursor position results in a PolicyException being thrown.
     *
     * @param storage reference to underlying storage that should be used for model unmarshalling
     * @return unmarshalled policy source model. If no policies are found, returns {@code null}.
     * @throws PolicyException in case of the unmarshalling problems
     */
    public abstract PolicySourceModel unmarshalModel(Object storage) throws PolicyException;
    
    public static PolicyModelUnmarshaller getXmlUnmarshaller() {
        return xmlUnmarshaller;
    }
}
