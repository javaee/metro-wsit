/*
 * $Id: DumpFilter.java,v 1.1 2006-05-03 22:57:47 arungupta Exp $
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
package com.sun.xml.wss.impl.filter;

import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;

import javax.xml.transform.Source;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import java.util.logging.Logger;

/**
 * Dump a SOAP message for debugging.
 *
 */
public class DumpFilter  {
    
    private static  Level DEFAULT_LOG_LEVEL = Level.INFO;
    
    private static final String lineSeparator = System.getProperty("line.separator");

    private static Logger log =  Logger.getLogger(LogDomainConstants.IMPL_FILTER_DOMAIN,
        LogDomainConstants.IMPL_FILTER_DOMAIN_BUNDLE);
    
    public static void process(ProcessingContext context)
        throws XWSSecurityException {

        OutputStream dest;
        ByteArrayOutputStream baos = null;
        // Collect output in a byte[]
        baos = new ByteArrayOutputStream();
        dest = baos;
        
        String label = "Sending Message";
        
        if (context.isInboundMessage()) {
            label = "Received Message";
        }
        
        String msg1 = "==== " + label + " Start ====" + lineSeparator;
        
        try {
            TeeFilter teeFilter;
            teeFilter = new TeeFilter(dest, true);
            teeFilter.process(context.getSOAPMessage());
        } catch (Exception e) {
            log.log(
            Level.SEVERE,
            "WSS1411.unableto.dump.soapmessage",
            new Object[] { e.getMessage()});
            throw new XWSSecurityException("Unable to dump SOAPMessage");
        }
        
        String msg2 = "==== " + label + " End  ====" + lineSeparator;

        byte[] bytes = baos.toByteArray();       
        String logMsg = msg1 + new String(bytes) + msg2;
        log.log(DEFAULT_LOG_LEVEL, logMsg);
    }
    
}
