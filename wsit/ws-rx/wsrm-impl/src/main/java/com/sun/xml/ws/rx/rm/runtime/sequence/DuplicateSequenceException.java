/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;

/**
 * Inicates that the sequence with given sequence identifier already exists in a given environment.
 * 
 * This exceptions is used under the following conditions:
 *  <ul>
 *      <li>sequence with such {@code sequenceId} is already registered and managed by a given sequence manager</li>
 *  </ul>
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class DuplicateSequenceException extends RxRuntimeException {
    private static final long serialVersionUID = -4888405115401229826L;
    //
    private final String sequenceId;
    
    /**
     * Constructs an instance of <code>DuplicateSequenceException</code> for the sequence with {@code sequenceId} identifier.
     * @param sequenceId the identifier of the duplicate sequence.
     */
    public DuplicateSequenceException(String sequenceId) {
        super(DuplicateSequenceException.createErrorMessage(sequenceId));
        this.sequenceId = sequenceId;
    }

    /**
     * Returns the identifier of the unknown sequence
     * @return the unknown sequence identifier
     */
    public String getSequenceId() {
        return sequenceId;
    }        
    
    private static String createErrorMessage(String sequenceId) {
        return LocalizationMessages.WSRM_1126_DUPLICATE_SEQUENCE_ID(sequenceId);
    } 
}
