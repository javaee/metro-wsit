/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * This is the base class for the implementations of <code> SequenceAcknowledgementElement </code> based on the
 * two versions of the RM specification
 *
 * @author Bhakti Mehta
 * @author Mike Grogan
 */
public abstract class AbstractSequenceAcknowledgement {

    /**
     *  Gets the value of the nack property.
     *
     * @return The value of the property, which is a list of BigIntegers
     *
     *
     */
    protected abstract List<BigInteger> getNack();

    /**
     * Gets the value of the any property representing extensibility elements
     *
     * @return The list of elements.
     *
     */
    protected abstract List<Object> getAny();

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     *
     * @return The value of the property
     */
    protected abstract Map<QName, String> getOtherAttributes();

    /**
     * Sets the Identifier
     * @param id
     */
    public abstract void setId(String id);

    /**
     * Gets the identifier associated with the Sequence
     * @return     String
     */
    protected abstract String getId();

    /**
     * Gets the BufferRemaining value
     * @return   int
     */
    protected abstract int getBufferRemaining();

    /**
     * Sets the BufferRemaining value
     * @return void
     */
    public abstract void setBufferRemaining(int value);

    public abstract void addAckRange(long lower, long upper);

    public abstract void addNack(long index);
}
