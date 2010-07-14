/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.tx.at.policy.spi_impl;

import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import java.lang.reflect.Method;

/**
 * Place all dependencies on javax.ejb in one file.
 */
class EjbTransactionAnnotationProcessor {

    enum TransactionAttributeType {
        NOT_SUPPORTED, NEVER, MANDATORY, SUPPORTS, REQUIRES_NEW, REQUIRED
    }

    static boolean isContainerManagedEJB(Class c) {
        TransactionManagement tm = (TransactionManagement) c.getAnnotation(TransactionManagement.class);
        if (tm != null) {
            switch (tm.value()) {
                case BEAN:
                    return false;
                case CONTAINER:
                default:
                    return true;
            }
        }

        // No TransactionManagement annotation. Default is CONTAINER for EJB.
        Stateful stateful = (Stateful) c.getAnnotation(Stateful.class);
        Stateless stateless = (Stateless) c.getAnnotation(Stateless.class);
        if (c.getAnnotation(Stateful.class) != null ||
                c.getAnnotation(Stateless.class) != null) {
            //TODO: Are there any other EJB annotations? 
            return true;
        } else {
            // servlet endpoint
            return false;
        }
    }

    /**
     * Precondition: isContainerManagedEjb(c) returned true.
     */
    static TransactionAttributeType getTransactionAttributeDefault(Class c) {
        // defaults to REQUIRED if no annotation on class
        TransactionAttributeType result = TransactionAttributeType.REQUIRED;
        TransactionAttribute txnAttr = (TransactionAttribute) c.getAnnotation(TransactionAttribute.class);
        if (txnAttr != null) {
            result = convert(txnAttr.value());
        }
        return result;
    }

    /**
     * DefaultTxnAttr was obtained from class TransactionAttribute annotation.
     */
    static TransactionAttributeType getEffectiveTransactionAttribute(Method m, TransactionAttributeType defaultTxnAttr) {
        TransactionAttributeType result = defaultTxnAttr;
        TransactionAttribute txnAttr = m.getAnnotation(TransactionAttribute.class);
        if (txnAttr != null) {
            result = convert(txnAttr.value());
        }
        return result;
    }

    private static TransactionAttributeType convert(javax.ejb.TransactionAttributeType e) {
        return TransactionAttributeType.valueOf(TransactionAttributeType.class, e.name());
    }
}
