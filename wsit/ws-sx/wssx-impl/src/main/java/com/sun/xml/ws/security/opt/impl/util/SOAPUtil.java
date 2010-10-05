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
/*
 * SOAPUtil.java
 *
 * Created on July 24, 2006, 4:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.xml.ws.security.opt.impl.util;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.security.secconv.WSSecureConversationRuntimeException;
import com.sun.xml.wss.impl.WssSoapFaultException;
import javax.xml.namespace.QName;
import com.sun.xml.ws.security.opt.api.keyinfo.BinarySecurityToken;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

/**
 *
 * @author ashutosh.shahi@sun.com
 */
public class SOAPUtil {

    private static boolean enableFaultDetail = false;
    private static final String WSS_DEBUG_PROPERTY = "com.sun.xml.wss.debug";
    private static final String ENABLE_FAULT_DETAIL = "FaultDetail";

    static {
        String enableDetailFlag = System.getProperty(WSS_DEBUG_PROPERTY);
        if (enableDetailFlag != null && enableDetailFlag.contains(ENABLE_FAULT_DETAIL)) {
            enableFaultDetail = true;
            System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "true");
        }
    }

    public static SOAPFaultException getSOAPFaultException(QName faultCode, WSSecureConversationRuntimeException wsre, SOAPFactory soapFactory, SOAPVersion sOAPVersion) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Creates a new instance of SOAPUtil */
    public SOAPUtil() {

    }

    public static String getIdFromFragmentRef(String ref) {
        char start = ref.charAt(0);
        if (start == '#') {
            return ref.substring(1);
        }
        return ref;
    }

    public static X509Certificate getCertificateFromToken(BinarySecurityToken bst) throws
            XWSSecurityException {
            byte[] data = bst.getTokenValue();
        try {
            CertificateFactory certFact = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certFact.generateCertificate(new ByteArrayInputStream(data));

        } catch (Exception e) {
            throw new XWSSecurityException(
                    "Unable to create X509Certificate from data");
        }
     }

    /**
     * Create and initialize a WssSoapFaultException.
     */
    public static WssSoapFaultException newSOAPFaultException(
            String faultstring,
            Throwable th) {
        WssSoapFaultException sfe =
                new WssSoapFaultException(null, faultstring, null, null);
        sfe.initCause(th);
        return sfe;
    }

    /**
     * Create and initialize a WssSoapFaultException.
     */
    public static WssSoapFaultException newSOAPFaultException(
            QName faultCode,
            String faultstring,
            Throwable th) {

        WssSoapFaultException sfe =
                new WssSoapFaultException(faultCode, faultstring, null, null);
        sfe.initCause(th);
        return sfe;
    }

    protected static SOAPFault getSOAPFault(WssSoapFaultException sfe, SOAPFactory soapFactory, SOAPVersion version) {

             SOAPFault fault;
        String reasonText = sfe.getFaultString();
        if (reasonText == null) {
            reasonText = (sfe.getMessage() != null) ? sfe.getMessage() : "";
        }
        try {
            if (version == SOAPVersion.SOAP_12) {
                fault = soapFactory.createFault(reasonText, SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(sfe.getFaultCode());
            } else {
                fault = soapFactory.createFault(reasonText, sfe.getFaultCode());
            }
        } catch (Exception e) {
            throw new XWSSecurityRuntimeException(e);
        }
        return fault;
    }

    protected static SOAPFault getSOAPFault(QName faultCode, String faultString, SOAPFactory soapFactory, SOAPVersion version) {

           SOAPFault fault;
        try {
            if (version == SOAPVersion.SOAP_12) {
                fault = soapFactory.createFault(faultString, SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(faultCode);
            } else {
                fault = soapFactory.createFault(faultString, faultCode);
            }
        } catch (Exception e) {
            throw new XWSSecurityRuntimeException(e);
        }
        return fault;
    }

    public static SOAPFaultException getSOAPFaultException(WssSoapFaultException ex, SOAPFactory factory, SOAPVersion version) {
        SOAPFault fault = getSOAPFault(ex, factory, version);
        if (!enableFaultDetail) {
           return createSOAPFault(fault,ex);
 
        }
        Throwable cause = ex.getCause();
        setFaultDetail(fault, cause);
        return createSOAPFault(fault,ex);

    }

     public static SOAPFaultException getSOAPFaultException(QName faultCode, Exception ex, SOAPFactory factory, SOAPVersion version) {
        String msg = ex.getMessage();
        if (msg == null) {
            msg = ex.getClass().getName();
        }
        SOAPFault fault = getSOAPFault(faultCode, msg, factory, version);
        if (!enableFaultDetail) {
            return createSOAPFault(fault,ex);

        }
        setFaultDetail(fault, ex);
        return createSOAPFault(fault,ex);
    }

    public static SOAPFaultException getSOAPFaultException(Exception ex, SOAPFactory factory, SOAPVersion version) {
        String msg = ex.getMessage();
        if (msg == null) {
            msg = ex.getClass().getName();
        }
        SOAPFault fault = getSOAPFault(MessageConstants.WSSE_INVALID_SECURITY, msg, factory, version);
        if (!enableFaultDetail) {
            return createSOAPFault(fault,ex);

        }
        setFaultDetail(fault, ex);
        return createSOAPFault(fault,ex);

    }
    private static SOAPFaultException createSOAPFault(SOAPFault fault, Throwable cause) {
     SOAPFaultException sfe = new SOAPFaultException(fault);
       sfe.initCause(cause);
       return sfe;
    }

    
    private static void setFaultDetail(SOAPFault fault, Throwable cause) {
        try {
            //Add Detail element to the fault.
            Detail detail = fault.addDetail();
            QName name = new QName("https://xwss.dev.java.net", "FaultDetail", "xwssfault");
            DetailEntry entry = detail.addDetailEntry(name);
            String exception = "Cause Not Set";
            if (cause != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                PrintWriter s = new PrintWriter(bos);
                cause.printStackTrace(s);
                s.flush();
                exception = bos.toString();
            }
            entry.addTextNode(exception);
        } catch (SOAPException ex) {
        //ignore for now
        }
    }
}

