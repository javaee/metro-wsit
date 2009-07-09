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
package com.sun.xml.ws.rx.util;

import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.istack.logging.Logger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.EndpointReference;

/**
 * TODO javadoc
 *
 * <b>
 * WARNING: This class is a private utility class used by WS-RX implementation. Any usage outside
 * the intedned scope is strongly discouraged. The API exposed by this class may be changed, replaced
 * or removed without any advance notice.
 * </b>
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public final class JaxbContextRepository {

    private static final Logger LOGGER = Logger.getLogger(JaxbContextRepository.class);
    //
    private Map<AddressingVersion, JAXBRIContext> jaxbContexts;
    private ThreadLocal<Map<AddressingVersion, Unmarshaller>> threadLocalUnmarshallers = new ThreadLocal<Map<AddressingVersion, Unmarshaller>>() {

        @Override
        protected Map<AddressingVersion, Unmarshaller> initialValue() {
            Map<AddressingVersion, Unmarshaller> result = new HashMap<AddressingVersion, Unmarshaller>();
            for (Map.Entry<AddressingVersion, JAXBRIContext> entry : jaxbContexts.entrySet()) {
                try {
                    result.put(entry.getKey(), entry.getValue().createUnmarshaller());
                } catch (JAXBException ex) {
                    LOGGER.severe("Unable to create JAXB unmarshaller", ex);
                    throw new RxRuntimeException("Unable to create JAXB unmarshaller", ex);
                }
            }
            return result;
        }
    };

    public JaxbContextRepository(Class<?>... classes) throws RxRuntimeException {
        this.jaxbContexts = new HashMap<AddressingVersion, JAXBRIContext>();
        for (AddressingVersion av : AddressingVersion.values()) {
            this.jaxbContexts.put(av, createContext(av, classes));
        }
    }

    private static final JAXBRIContext createContext(AddressingVersion av, Class<?>... classes) throws RxRuntimeException {
        /**
         * We need to add all supported WS-A EndpointReference implementation classes to the array
         * before we pass the array to the JAXBRIContext factory method.
         */
        LinkedList<Class<?>> jaxbElementClasses = new LinkedList<Class<?>>(Arrays.asList(classes));
        jaxbElementClasses.add(av.eprType.eprClass);

        Map<Class, Class> eprClassReplacementMap = new HashMap<Class, Class>();
        eprClassReplacementMap.put(EndpointReference.class, av.eprType.eprClass);

        try {
            return JAXBRIContext.newInstance(jaxbElementClasses.toArray(classes),
                    null,
                    eprClassReplacementMap,
                    null,
                    false,
                    null);
        } catch (JAXBException ex) {
            throw new RxRuntimeException("Unable to create JAXB RI Context", ex);
        }
    }

    /**
     * Creates JAXB {@link Unmarshaller} that is able to unmarshall elements for specified classes.
     * <p />
     * As JAXB unmarshallers are not thread-safe, this method should be used to create a new {@link Unmarshaller}
     * instance whenever there is a chance that the same instance might be invoked concurrently from multiple
     * threads. On th other hand, it is prudent to cache or pool {@link Unmarshaller} instances if possible as
     * constructing a new {@link Unmarshaller} instance is rather expensive.
     * <p />
     * For additional information see this <a href="https://jaxb.dev.java.net/guide/Performance_and_thread_safety.html">blog entry</a>.
     *
     * @return created JAXB unmarshaller
     *
     * @exception RxRuntimeException in case the creation of unmarshaller failed
     */
    public Unmarshaller getUnmarshaller(AddressingVersion av) throws RxRuntimeException {
        return threadLocalUnmarshallers.get().get(av);
    }

    /**
     * Returns JAXB context that is intitialized based on a given addressing version.
     *
     * @param av addressing version used to initialize JAXB context
     *
     * @return JAXB context that is intitialized based on a given addressing version.
     */
    public JAXBRIContext getJaxbContext(AddressingVersion av) {
        return jaxbContexts.get(av);
    }
}
