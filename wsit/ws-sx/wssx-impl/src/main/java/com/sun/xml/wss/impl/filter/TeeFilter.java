/*
 * $Id: TeeFilter.java,v 1.1 2010-10-05 11:43:50 m_potociar Exp $
 */

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
package com.sun.xml.wss.impl.filter;

import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import javax.xml.soap.SOAPMessage;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.logging.LogStringsMessages;
import java.util.logging.Logger;

/**
 * Copies the SOAP message into an OutputStream using an optional stylesheet
 * to format the message.  The original message is not modified.  This is
 * analogous to the "tee" unix command.
 *
 * @author Edwin Goei
 */
public class TeeFilter {
    // TODO Fix the stylesheet to pretty print a SOAP Message
    private static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    private static final String prettyPrintStylesheet =
    "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:wsse='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd'\n"
    + "  version='1.0'>\n"
    + "  <xsl:output method='xml' indent='yes'/>\n"
    + "  <xsl:strip-space elements='*'/>\n"
    + "  <xsl:template match='/'>\n"
    + "    <xsl:apply-templates/>\n"
    + "  </xsl:template>\n"
    + "  <xsl:template match='node() | @*'>\n"
    + "    <xsl:choose>\n"
    + "      <xsl:when test='contains(name(current()), \"wsse:Password\")'>\n"
    + "        <wsse:Password Type='{@Type}'>****</wsse:Password>\n"
    + "      </xsl:when>\n"
    + "      <xsl:otherwise>\n"
    + "        <xsl:copy>\n"
    + "          <xsl:apply-templates select='node() | @*'/>\n"
    + "        </xsl:copy>\n"
    + "      </xsl:otherwise>\n"
    + "    </xsl:choose>\n"
    + "  </xsl:template>\n"
    + "</xsl:stylesheet>\n";
    
    /** OutputStream for output. */
    private OutputStream out;
    
    /** Represents a stylesheet */
    private Templates templates;
    
    
    /**
     * Copy and optionally format a message
     *
     * @param out destination OutputStream
     * @param stylesheet XSLT stylesheet for format or if null, then does
     *     not format
     */
    public TeeFilter(OutputStream out, Source stylesheet)
    throws XWSSecurityException {
        init(out, stylesheet);
    }
    
    /**
     * Copy and optionally pretty print a message
     *
     * @param out destination OutputStream
     * @param prettyPrint true means to use built-in pretty print stylesheet
     * @throws XWSSecurityException
     */
    public TeeFilter(OutputStream out, boolean prettyPrint)
    throws XWSSecurityException {
        if (prettyPrint) {
            init(out, getPrettyPrintStylesheet());
        } else {
            init(out, null);
        }
    }
    
    /**
     * Saves a copy of message to Outputstream out
     *
     * @param out
     * @throws XWSSecurityException
     */
    public TeeFilter(OutputStream out) throws XWSSecurityException {
        init(out, null);
    }
    
    /**
     * A no-op
     *
     * @throws XWSSecurityException
     */
    public TeeFilter() throws XWSSecurityException {
        init(null, null);
    }
    
    private void init(OutputStream out, Source stylesheet)
    throws XWSSecurityException {
        this.out = out;
        
        if (stylesheet == null) {
            templates = null;
        } else {
            TransformerFactory tf = TransformerFactory.newInstance();
            //new com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl();
            try {
                templates = tf.newTemplates(stylesheet);
            } catch (TransformerConfigurationException e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0147_DIAG_CAUSE_1(),
                new Object[] {e.getMessage()});
                throw new XWSSecurityException("Unable to use stylesheet", e);
            }
        }
    }
    
    private Source getPrettyPrintStylesheet() {
        //        if (true) {
        //            if (defaultStylesheetSource == null) {
        //                byte[] xsltBytes = defaultStylesheet.getBytes();
        //                ByteArrayInputStream bais = new ByteArrayInputStream(xsltBytes);
        //                defaultStylesheetSource = new StreamSource(bais);
        //            }
        //            return defaultStylesheetSource;
        //        } else {
        byte[] xsltBytes = prettyPrintStylesheet.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(xsltBytes);
        Source stylesheetSource = new StreamSource(bais);
        return stylesheetSource;
        //        }
    }
    
    /**
     * Invokes the MessageFilter on the SOAPMessage sm.  A
     * XWSSecurityException is thrown if the operation did not succeed.
     *
     * @param secureMessage SOAPMessage to perform the operation on
     *
     * @throws com.sun.xml.wss.XWSSecurityException if the operation did not
     *    succeed
     */
    public void process(SOAPMessage secureMessage) throws XWSSecurityException {
        if (out == null) {
            return;
        }
        
        Transformer transformer;
        try {
            if (secureMessage.countAttachments() > 0) {
                secureMessage.writeTo(out);
            } else {
                if (templates == null) {
                    // Use identity transform
                    transformer =
                    new com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl()
                    .newTransformer();
                } else {
                    // Use supplied stylesheet via Templates object
                    transformer = templates.newTransformer();
                }
                Source msgSource = secureMessage.getSOAPPart().getContent();
                transformer.transform(msgSource, new StreamResult(out));
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0148_UNABLETO_PROCESS_SOAPMESSAGE(new Object[] {ex.getMessage()}));
            throw new XWSSecurityException("Unable to process SOAPMessage", ex);
        }
    }
}
