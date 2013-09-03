/*
 * $Id: DumpFilter.java,v 1.6 2009/05/22 12:40:52 sm228678 Exp $
 */

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
package com.sun.xml.wss.impl.filter;

import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;

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
    /**
     * dumps the soap messages and throws XWSSecurityException if it is unable to dump.
     * @param context ProcessingContext
     * @throws com.sun.xml.wss.XWSSecurityException
     */
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
