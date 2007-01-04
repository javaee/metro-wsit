/*
 * $Id: BinaryExchangeImpl.java,v 1.3 2007-01-04 00:42:25 manveen Exp $
 */

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

package com.sun.xml.ws.security.trust.impl.elements;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.xml.ws.security.trust.elements.BinaryExchange;
import com.sun.xml.ws.security.trust.impl.bindings.BinaryExchangeType;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

import com.sun.istack.NotNull;

import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

/**
 *
 * @author Manveen Kaur (manveen.kaur@sun.com).
 */

public class BinaryExchangeImpl extends BinaryExchangeType implements BinaryExchange {
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    public BinaryExchangeImpl(String encodingType, String valueType, byte[] rawText) {
        setEncodingType(encodingType);
        setValueType(valueType);
        setRawValue(rawText);
    }
    
    public BinaryExchangeImpl(BinaryExchangeType bcType)throws Exception{
        setEncodingType(bcType.getEncodingType());
        setValueType(bcType.getValueType());
        setValue(bcType.getValue());
    }
    
    public byte[] getRawValue() {
        try {
            return Base64.decode(getTextValue());
        } catch (Base64DecodingException de) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0020_ERROR_DECODING(getTextValue(), de));
            throw new RuntimeException("Error while decoding :" + getTextValue() , de);
        }
    }
    
    public String getTextValue() {
        return super.getValue();
    }
    
    public void setTextValue(@NotNull final String encodedText) {
        super.setValue(encodedText);
    }
    
    public void setRawValue(@NotNull final byte[] rawText) {
        super.setValue(Base64.encode(rawText));
    }
    
}
