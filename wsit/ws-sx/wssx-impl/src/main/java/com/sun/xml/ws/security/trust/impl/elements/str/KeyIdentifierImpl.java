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
 * $Id: KeyIdentifierImpl.java,v 1.2 2010-10-21 15:36:57 snajper Exp $
 */

package com.sun.xml.ws.security.trust.impl.elements.str;

import com.sun.xml.ws.security.secext10.KeyIdentifierType;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;

import java.net.URI;

/**
 * KeyIdentifier implementation
 */
public class KeyIdentifierImpl extends KeyIdentifierType implements KeyIdentifier {
    
    
    public KeyIdentifierImpl() {
        // default c'tor
    }

    public KeyIdentifierImpl(String valueType, String encodingType) {
        setValueType(valueType);
        setEncodingType(encodingType);
    }
    
    public KeyIdentifierImpl(KeyIdentifierType kidType){
        this(kidType.getValueType(), kidType.getEncodingType());
        setValue(kidType.getValue());
    }
    
    public URI getValueTypeURI(){
        return URI.create(super.getValueType());
    }
    
    public URI getEncodingTypeURI (){
        return URI.create(super.getEncodingType());
    }
    
    public String getType(){
        return "KeyIdentifier";
    }
}
