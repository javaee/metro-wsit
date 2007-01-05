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

package com.sun.xml.ws.tx.common;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * @author jf39279
 */
public class TxJAXBContext {
    private static final TxLogger logger = TxLogger.getLogger(TxJAXBContext.class);

    private static JAXBContext jc;

    static {
        try {
            final List<Class> classes = getClassesToBeBound();
            jc = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
        } catch (JAXBException e) {
            logger.severe("getJAXBContext", "failed to create", e);
        } catch (ClassNotFoundException e) {
            logger.severe("getJAXBContext", "failed to create", e);
        }
    }

    public static JAXBContext getJAXBContext() {
        return jc;
    }

    /**
     * Marshal xml fragment, not an XML document.
     */
    public static Marshaller createMarshaller() {
        try {
            final Marshaller marshaller = getJAXBContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            return marshaller;
        } catch (JAXBException e) {
            if (logger.isLogging(Level.SEVERE)) {
                logger.severe("createMarshaller", "failed creation", e);
            }
            // TODO should this throw an exception also
            return null;
        }
    }

    public static Unmarshaller createUnmarshaller() {
        try {
            return getJAXBContext().createUnmarshaller();
        } catch (JAXBException e) {
            if (logger.isLogging(Level.SEVERE)) {
                logger.severe("createMarshaller", "failed creation", e);
            }
            // TODO: rethrow some exception.
            return null;
        }
    }

    private static List<Class> getClassesToBeBound() throws ClassNotFoundException {
        final Class[] classes;
        final ArrayList<Class> classList;

        classes = new Class[]{
                Class.forName("com.sun.xml.ws.tx.webservice.member.coord.CoordinationContext"),
                Class.forName("com.sun.xml.ws.tx.common.RegistrantIdentifier"), //reference param
                Class.forName("com.sun.xml.ws.tx.common.ActivityIdentifier")    // reference param
        };
        classList = new ArrayList<Class>(Arrays.asList(classes));
        return classList;
    }
}
