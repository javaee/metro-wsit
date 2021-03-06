/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0-b26-ea3 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.02.24 at 05:55:09 PM PST 
//


package com.sun.xml.ws.policy.impl.bindings;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.policy.impl.bindings.ObjectFactory;
import com.sun.xml.ws.policy.impl.bindings.OperatorContentType;
import com.sun.xml.ws.policy.impl.bindings.Policy;
import com.sun.xml.ws.policy.impl.bindings.PolicyAttachment;
import com.sun.xml.ws.policy.impl.bindings.PolicyReference;
import com.sun.xml.ws.policy.impl.bindings.UsingPolicy;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sun.xml.ws.policy.impl.bindings package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ExactlyOne_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/09/policy", "ExactlyOne");
    private final static QName _All_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/09/policy", "All");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sun.xml.ws.policy.impl.bindings
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link UsingPolicy }
     * 
     */
    public UsingPolicy createUsingPolicy() {
        return new UsingPolicy();
    }

    /**
     * Create an instance of {@link PolicyReference }
     * 
     */
    public PolicyReference createPolicyReference() {
        return new PolicyReference();
    }

    /**
     * Create an instance of {@link PolicyAttachment }
     * 
     */
    public PolicyAttachment createPolicyAttachment() {
        return new PolicyAttachment();
    }

    /**
     * Create an instance of {@link AppliesTo }
     * 
     */
    public AppliesTo createAppliesTo() {
        return new AppliesTo();
    }

    /**
     * Create an instance of {@link OperatorContentType }
     * 
     */
    public OperatorContentType createOperatorContentType() {
        return new OperatorContentType();
    }

    /**
     * Create an instance of {@link Policy }
     * 
     */
    public Policy createPolicy() {
        return new Policy();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OperatorContentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/09/policy", name = "ExactlyOne")
    public JAXBElement<OperatorContentType> createExactlyOne(OperatorContentType value) {
        return new JAXBElement<OperatorContentType>(_ExactlyOne_QNAME, OperatorContentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OperatorContentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2004/09/policy", name = "All")
    public JAXBElement<OperatorContentType> createAll(OperatorContentType value) {
        return new JAXBElement<OperatorContentType>(_All_QNAME, OperatorContentType.class, null, value);
    }

}
