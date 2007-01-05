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

import static com.sun.xml.ws.tx.common.Constants.WSCOOR_SUN_URI;
import com.sun.istack.NotNull;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

/**
 * @author jf39279
 */

@XmlRootElement(name = ActivityIdentifier.ACTIVITY_ID, namespace = WSCOOR_SUN_URI)
public class ActivityIdentifier extends Identifier {

    public static final String ACTIVITY_ID = "ActivityId";
    public static final QName QNAME = new QName(WSCOOR_SUN_URI, ACTIVITY_ID);

    /**
     * silence JAXB IllegalAnnotationsException: does not have a no-arg default constructor
     */
    public ActivityIdentifier() {
        // JAXB annotation processing requires public no-arg default ctor 
    }

    /**
     * Create an activity identified from the given id string
     * @param id id
     */
    public ActivityIdentifier(@NotNull String id) {
        value = id;
    }

    @NotNull
    public QName getName() {
        return QNAME;
    }
}
