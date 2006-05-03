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

/*
 * JAXWSProcessingContextImpl.java
 *
 * Created on January 30, 2006, 5:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.jaxws.impl;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.ProcessingContextImpl;

/**
 *
 * @authors Vbkumar.Jayanti@Sun.COM, K.Venugopal@sun.com
 */
public class JAXWSProcessingContextImpl extends FilterProcessingContext implements JAXWSProcessingContext{
    
    private Message _message;
    /** Creates a new instance of JAXWSProcessingContextImpl */
    public JAXWSProcessingContextImpl() {
    }
    
    public void setMessage(Message message) {
        this._message = message;
    }
    
    public Message getMessage() {
        return _message;
    }
    
}
