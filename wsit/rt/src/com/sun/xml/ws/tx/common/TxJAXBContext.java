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
            logger.severe("getJAXBContext", LocalizationMessages.FAILED_TO_CREATE_JAXBCONTEXT_2002(e));
        } catch (ClassNotFoundException e) {
            logger.severe("getJAXBContext", LocalizationMessages.FAILED_TO_CREATE_JAXBCONTEXT_2002(e));
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
            logger.severe("createMarshaller", LocalizationMessages.FAILED_TO_CREATE_MARSHALLER_2003(e));
            return null;
        }
    }

    public static Unmarshaller createUnmarshaller() {
        try {
            return getJAXBContext().createUnmarshaller();
        } catch (JAXBException e) {
            logger.severe("createUnmarshaller", LocalizationMessages.FAILED_TO_CREATE_UNMARSHALLER_2004(e));
            return null;
        }
    }

    private static List<Class> getClassesToBeBound() throws ClassNotFoundException {
        final Class[] classes;
        final ArrayList<Class> classList;

        classes = new Class[]{
                Class.forName("com.sun.xml.ws.tx.webservice.member.coord.CoordinationContext")
        };
        classList = new ArrayList<Class>(Arrays.asList(classes));
        return classList;
    }
}
