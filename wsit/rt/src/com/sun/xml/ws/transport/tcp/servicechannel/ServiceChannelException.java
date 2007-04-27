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

package com.sun.xml.ws.transport.tcp.servicechannel;

import com.sun.istack.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.ws.WebFault;

/**
 * @author Alexey Stashok
 */
@WebFault(name = "ServiceChannelException", targetNamespace = "http://servicechannel.tcp.transport.ws.xml.sun.com/", faultBean = "com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelException$ServiceChannelExceptionBean")
public class ServiceChannelException extends Exception {
    private ServiceChannelExceptionBean faultInfo;
    
    public ServiceChannelException() {
        faultInfo = new ServiceChannelExceptionBean();
    }
    
    public ServiceChannelException(ServiceChannelErrorCode errorCode, @Nullable final String message) {
        super(message);
        faultInfo = new ServiceChannelExceptionBean(errorCode , message);
    }
    
    public ServiceChannelException(final String message, final ServiceChannelExceptionBean faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }
    
    public ServiceChannelException(final String message, final ServiceChannelExceptionBean faultInfo, final Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }
    
    public ServiceChannelExceptionBean getFaultInfo() {
        return faultInfo;
    }
    
    public void setFaultInfo(final ServiceChannelExceptionBean faultInfo) {
        this.faultInfo = faultInfo;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "serviceChannelExceptionBean", propOrder = {
        "errorCode",
        "message"
    })
    public static class ServiceChannelExceptionBean {
        @XmlElement(required = true)
        private ServiceChannelErrorCode errorCode;
        
        private String message;
        
        public ServiceChannelExceptionBean() {
        }
        
        public ServiceChannelExceptionBean(final ServiceChannelErrorCode errorCode, final String message) {
            this.errorCode = errorCode;
            this.message = message;
        }
        
        public ServiceChannelErrorCode getErrorCode() {
            return errorCode;
        }
        
        public void setErrorCode(ServiceChannelErrorCode errorCode)  {
            this.errorCode = errorCode;
        }
        
        public String getMessage() {
            return this.message;
        }
        
        public void setMessage(final String message) {
            this.message = message;
        }
    }
}