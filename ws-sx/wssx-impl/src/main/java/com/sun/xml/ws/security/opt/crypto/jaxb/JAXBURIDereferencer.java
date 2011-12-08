/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * JAXBURIDereferencer.java
 *
 * Created on January 24, 2006, 11:47 AM
 */
package com.sun.xml.ws.security.opt.crypto.jaxb;



import javax.xml.bind.JAXBContext;

import javax.xml.bind.JAXBElement;

import javax.xml.crypto.Data;

import javax.xml.crypto.URIReference;

import javax.xml.crypto.URIReferenceException;

import javax.xml.crypto.XMLCryptoContext;



import com.sun.xml.ws.security.opt.impl.crypto.JAXBDataImpl;



/**

 *

 * @author Abhijit Das

 */

public class JAXBURIDereferencer  implements javax.xml.crypto.URIDereferencer {

    private JAXBElement jaxbElement = null;

    private JAXBContext jbContext = null;

    /**

     * Creates a new instance of JAXBURIDereferencer

     */

    public JAXBURIDereferencer() {

    }



    /**

     * Dereferences the specified URIReference and returns the dereferenced data.

     *

     * uriReference - the URIReference

     * xMLCryptoContext - an XMLCryptoContext that may contain additional useful 

     * information for dereferencing the URI. This implementation should 

     * dereference the specified URIReference against the context's baseURI 

     * parameter, if specified.

     *

     * 

     * @return Data - the dereferenced data

     */

    public Data dereference(URIReference uRIReference, XMLCryptoContext xMLCryptoContext) throws URIReferenceException {

        JAXBDataImpl data = new JAXBDataImpl(getJaxbElement(), getJbContext(), new com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx(false));

        return data;

    }



    /**

     * Get the JAXBElement

     *

     * @return JAXBElement

     */

    public JAXBElement getJaxbElement() {

        return jaxbElement;

    }



    /*

     * Set JAXBElement

     * @param - jaxbElement 

     */

    public void setJaxbElement(JAXBElement jaxbElement) {

        this.jaxbElement = jaxbElement;

    }



    /**

     * Get JAXBContext

     *

     * @return JAXBContext

     */

    public JAXBContext getJbContext() {

        return jbContext;

    }



    /**

     * Set JAXBContext

     *

     * @param jbContext - JAXBContext

     */

    public void setJbContext(JAXBContext jbContext) {

        this.jbContext = jbContext;

    }

    

}

