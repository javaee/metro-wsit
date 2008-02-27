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