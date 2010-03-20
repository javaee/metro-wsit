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

package com.sun.xml.ws.security.opt.impl.incoming.processor;

import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jcp.xml.dsig.internal.DigesterOutputStream;
import javax.xml.ws.WebServiceException;
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class StreamingPayLoadDigester implements StreamFilter{
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    
    private XMLStreamReader reader = null;
    private Reference ref = null;
    private StAXEXC14nCanonicalizerImpl canonicalizer = null;
    private int index = 0;
    private boolean payLoad = false;
    private boolean digestDone = false;
    /** Creates a new instance of StreamingPayLoadDigester */
    public StreamingPayLoadDigester(Reference ref,XMLStreamReader reader,StAXEXC14nCanonicalizerImpl canonicalizer,boolean payLoad) {
        this.ref = ref;
        this.reader = reader;
        this.canonicalizer = canonicalizer;
        this.payLoad = payLoad;
    }
    /**
     * calculates the digest of the payload in a streaming fashion
     * @param xMLStreamReader XMLStreamReader
     * @return
     */
    public boolean accept(XMLStreamReader xMLStreamReader) {
        try {
            if(!digestDone){
                StreamUtil.writeCurrentEvent(xMLStreamReader,canonicalizer);
                if(reader.getEventType() == xMLStreamReader.START_ELEMENT){
                    index++;
                }else if(reader.getEventType() == xMLStreamReader.END_ELEMENT ){
                    index --;
                    //|| (reader.getLocalName() == SOAP_BODY_LNAME && (reader.getNamespaceURI() == SOAP_1_1_NS || reader.getNamespaceURI() == SOAP_1_2_NS ))
                    if( index == 0 ){
                        byte [] originalDigest = ref.getDigestValue();
                        if(logger.isLoggable(Level.FINEST)){
                            logger.log(Level.FINEST, LogStringsMessages.WSS_1763_ACTUAL_DEGEST_VALUE(new String(originalDigest)));
                        }
                        canonicalizer.writeEndDocument();
                        digestDone = true;
                        if(canonicalizer.getOutputStream() instanceof DigesterOutputStream){
                            byte [] calculatedDigest = ((DigesterOutputStream)canonicalizer.getOutputStream()).getDigestValue();
                            if(logger.isLoggable(Level.FINEST)){
                                logger.log(Level.FINEST,LogStringsMessages.WSS_1762_CALCULATED_DIGEST_VALUE(new String(calculatedDigest)));
                            }
                            if (!Arrays.equals(originalDigest, calculatedDigest)) {
                                XMLSignatureException xe = new XMLSignatureException(LogStringsMessages.WSS_1717_ERROR_PAYLOAD_VERIFICATION());
                                logger.log(Level.SEVERE, LogStringsMessages.WSS_1717_ERROR_PAYLOAD_VERIFICATION(),xe);
                                throw new WebServiceException(xe);
                            }else{
                                if(logger.isLoggable(Level.FINEST)){
                                    if(!payLoad){
                                        logger.log(Level.FINEST,"Digest verification of Body was successful");
                                    }else{
                                        logger.log(Level.FINEST,"Digest verification of PayLoad was successful");
                                    }
                                }
                            }
                        }else if(canonicalizer.getOutputStream() instanceof ByteArrayOutputStream){
                            byte[] canonicalizedData = ((ByteArrayOutputStream)canonicalizer.getOutputStream()).toByteArray();
                            byte [] calculatedDigest = null;
                            MessageDigest  md = null;
                            try {
                                md = MessageDigest.getInstance("SHA-1");
                            } catch (NoSuchAlgorithmException nsae) {
                                logger.log(Level.SEVERE, LogStringsMessages.WSS_1705_INVALID_DIGEST_ALGORITHM("SHA-1"),nsae);
                                throw new WebServiceException(nsae);
                            }
                            calculatedDigest = md.digest(canonicalizedData);
                            if(logger.isLoggable(Level.FINEST)){
                                logger.log(Level.FINEST,LogStringsMessages.WSS_1762_CALCULATED_DIGEST_VALUE(new String(calculatedDigest)));
                                logger.log(Level.FINEST, LogStringsMessages.WSS_1764_CANONICALIZED_PAYLOAD_VALUE(new String(canonicalizedData)));
                            }
                            if (!Arrays.equals(originalDigest, calculatedDigest)) {
                                XMLSignatureException xe = new XMLSignatureException(LogStringsMessages.WSS_1717_ERROR_PAYLOAD_VERIFICATION());
                                logger.log(Level.SEVERE, LogStringsMessages.WSS_1717_ERROR_PAYLOAD_VERIFICATION(),xe);
                                throw new WebServiceException(xe);
                            }else{
                                if(logger.isLoggable(Level.FINEST)){
                                    if(!payLoad){
                                        logger.log(Level.FINEST,"Digest verification of Body was successful");
                                    }else{
                                        logger.log(Level.FINEST,"Digest verification of PayLoad was successful");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1717_ERROR_PAYLOAD_VERIFICATION(),ex);
            throw new WebServiceException(ex);
        }
        return true;
    }
}
