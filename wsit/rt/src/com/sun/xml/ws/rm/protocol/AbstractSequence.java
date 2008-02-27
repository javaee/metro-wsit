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
package com.sun.xml.ws.rm.protocol;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * This is the base class for the implementations of <code> SequenceElement </code> based on the
 * two versions of the RM specification
 *
 * @author Bhakti Mehta
 * @author Mike Grogan
 */
public abstract class AbstractSequence {

    public String getLocalPart() {
        return "Sequence";
    }

    /**
     * Mutator for the Id property.  Maps to the Identifier property in the underlying
     * JAXB class.
     *
     * @param id The new value.
     */
    public abstract void setId(String id);

    /**
     * Accessor for the Id property.  Maps to the Identifier property in the underlying
     * JAXB class
     * @return The sequence id
     */
    protected abstract String getId();

    /**
     * Mutator for the Number property which maps to the MessageNumber property in
     * the underlying JAXB class.
     *
     * @param l The Message number.
     */
    public void setNumber(int l) {
        setMessageNumber(l);
    }

    /**
     * Accessor for the Number property which maps to the MessageNumber property in
     * the underlying JAXB class.
     *
     * @return The Message number.
     */
    protected int getNumber() {
        return getMessageNumber();
    }

    /**
     * Gets the value of the messageNumber property.
     *
     * @return The value of the property.
     *
     */
    protected abstract Integer getMessageNumber();

    /**
     * Sets the value of the messageNumber property.
     *
     * @param value The new value.
     *
     */
    public abstract void setMessageNumber(Integer value);

    /**
     * Gets the value of the any property.
     *
     * @return The value of the property.
     *
     *
     */
    public abstract List<Object> getAny();

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     *
     * @return The map of attributes.
     */
    public abstract Map<QName, String> getOtherAttributes();
}
