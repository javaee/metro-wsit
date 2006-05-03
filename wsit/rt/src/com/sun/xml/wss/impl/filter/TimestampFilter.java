/**
 * $Id: TimestampFilter.java,v 1.1 2006-05-03 22:57:49 arungupta Exp $
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

import com.sun.xml.wss.ProcessingContext;
import java.util.Date;
import java.util.Iterator;

import java.text.ParseException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import com.sun.xml.wss.core.Timestamp;
import com.sun.xml.wss.core.SecurityHeader;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;

import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;

import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;

import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;

import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;

import com.sun.xml.wss.impl.HarnessUtil;

/**
 * Processes export and import of wsu:Timestamp
 *
 * Message ANNOTATION is performed as follows:
 *
 *   if (policy resolution should happen)
 *       // make-DPC flag turned on
 *       resolve TimestampPolicy
 *   write wsu:Timestamp to header
 *
 * Message VALIDATION is performed as follows:
 *
 *   if (ADHOC processing mode)
 *       if (policy resolution should happen)
 *           // make-DPC flag turned on
 *           resolve TimestampPolicy
 *       locate wsu:Timestamp element in the header
 *       throw Exception if more than one found
 *       validate wsu:Timestamp (delegate to SecurityEnvironment Implementation)
 *   else
 *       import wsu:Timestamp element
 *       if (POSTHOC processing mode)
 *           construct Timestamp policy and set it on FPC
 *       else
 *       if (DEFAULT processing mode)
 *           validate wsu:Timestamp
 */
public class TimestampFilter {

    protected static Logger log = Logger.getLogger(
        LogDomainConstants.FILTER_DOMAIN,
        LogDomainConstants.FILTER_DOMAIN_BUNDLE);

    /**
     * @param context FilterProcessingContext
     *
     * @throws XWSSecurityException
     */
    public static void process (FilterProcessingContext context) throws XWSSecurityException {

         if (!context.isInboundMessage ()) {

             //hack to prevent multiple timestamp exports
             //TODO: revisit
             if (context.timestampExported())
                 return;

             TimestampPolicy policy = (TimestampPolicy) context.getSecurityPolicy();
             long timeout = policy.getTimeout();
             String created = policy.getCreationTime();
             String id = policy.getUUID();
             
             if (context.makeDynamicPolicyCallback()) {
                TimestampPolicy policyClone = (TimestampPolicy) policy.clone();
		try {
                      DynamicApplicationContext dynamicContext = 
                          new DynamicApplicationContext (context.getPolicyContext ());

                      dynamicContext.setMessageIdentifier (context.getMessageIdentifier ());
                      dynamicContext.inBoundMessage (false);
                      // TODO: copy runtime properties into callback context
                      DynamicPolicyCallback callback = 
                          new DynamicPolicyCallback (policyClone, dynamicContext);
                      ProcessingContext.copy (dynamicContext.getRuntimeProperties(), context.getExtraneousProperties());
                      HarnessUtil.makeDynamicPolicyCallback(callback, 
                          context.getSecurityEnvironment().getCallbackHandler());

                 } catch (Exception e) {
                    // log
                    throw new XWSSecurityException (e);
                 }

                 timeout   = policyClone.getTimeout();
                 created = policyClone.getCreationTime();
             }

             SecurityHeader secHeader = context.getSecurableSoapMessage().findOrCreateSecurityHeader();

             Timestamp wsuTimestamp = new Timestamp();
             if ("".equals(created)) {
                 wsuTimestamp.setCreated(null);
             }else {
                 wsuTimestamp.setCreated(created);
             }

             wsuTimestamp.setTimeout(timeout);
             if (id != null) {
                 wsuTimestamp.setId(id);
             }
             secHeader.insertHeaderBlock(wsuTimestamp);
 
             //hack to prevent multiple timestamp exports
             //TODO: revisit
             context.timestampExported(true);

         } else {     
             
             // Processing inbound messages             
             Timestamp timestamp = null;
             
             if (context.getMode() == FilterProcessingContext.ADHOC) {
                 
                 if (context.makeDynamicPolicyCallback()) {
                     TimestampPolicy policyClone = (TimestampPolicy) 
                         ((TimestampPolicy)context.getSecurityPolicy()).clone();
		     try {
                         DynamicApplicationContext dynamicContext = 
                             new DynamicApplicationContext (context.getPolicyContext ());

                         dynamicContext.setMessageIdentifier (context.getMessageIdentifier ());
                         dynamicContext.inBoundMessage (true);
                         DynamicPolicyCallback callback = 
                             new DynamicPolicyCallback (policyClone, dynamicContext);
                         ProcessingContext.copy (dynamicContext.getRuntimeProperties(), context.getExtraneousProperties());
                         HarnessUtil.makeDynamicPolicyCallback(callback, 
                             context.getSecurityEnvironment().getCallbackHandler());

                     } catch (Exception e) {
                     // log
                         throw new XWSSecurityException (e);
                     }
                     context.setSecurityPolicy(policyClone);
                 }
                 
                 TimestampPolicy policy = (TimestampPolicy) context.getSecurityPolicy();
                 long maxClockSkew = policy.getMaxClockSkew ();
                 long timeStampFreshness = policy.getTimestampFreshness ();

                 SecurityHeader secHeader = context.getSecurableSoapMessage().findSecurityHeader();
                 if (secHeader == null) {
		     // log
		     throw new XWSSecurityException(
                        "Message does not conform to Timestamp policy: " +
	                "wsu:Timestamp element not found in header");
                 }

                 SOAPElement ts = null;

                 try {
		     SOAPFactory factory = SOAPFactory.newInstance();
		     Name name = factory.createName(
                         MessageConstants.TIMESTAMP_LNAME,
		         MessageConstants.WSU_PREFIX,
			 MessageConstants.WSU_NS);
		     Iterator i = secHeader.getChildElements (name);
                     
		     if (i.hasNext()) {
		         ts = (SOAPElement) i.next();
			 if (i.hasNext()) {
                             log.log(Level.SEVERE, "BSP3219.Single.Timestamp");
			     throw new XWSSecurityException("More than one wsu:Timestamp element in the header");
			 }
		     } else {
			 // log
			 throw new XWSSecurityException(
                             "Message does not conform to Timestamp policy: " +
		             "wsu:Timestamp element not found in header");
		     }
		 } catch (SOAPException se) {
			 // log
			 throw new XWSSecurityRuntimeException (se);
		 }

		 try {
		     timestamp = new Timestamp (ts);                     
                 } catch (XWSSecurityException xwsse) {
                    throw SecurableSoapMessage.newSOAPFaultException(
                         MessageConstants.WSSE_INVALID_SECURITY,
                         "Failure in Timestamp internalization.\n" +
                         "Message is: " + xwsse.getMessage(),
                         xwsse);
                 }

                 try {
                     context.getSecurityEnvironment().validateTimestamp(
                         context.getExtraneousProperties(), timestamp, maxClockSkew, timeStampFreshness);
                 } catch (XWSSecurityException xwsse) {
                    throw SecurableSoapMessage.newSOAPFaultException(
                         MessageConstants.WSSE_INVALID_SECURITY,
                         "Failure in Timestamp validation.\n" +
                         "Message is: " + xwsse.getMessage(),
                         xwsse);
                 }

                 if (MessageConstants.debug) {
                     log.log(Level.FINEST, "Validated TIMESTAMP.....");
                 }
             } else {
                 
                 if (context.getMode() == FilterProcessingContext.POSTHOC) {
                     throw new XWSSecurityException("Internal Error: Called TimestampFilter in POSTHOC Mode");
                 }

                 if (context.getMode() == FilterProcessingContext.WSDL_POLICY) {
                     TimestampPolicy ts =  new TimestampPolicy();
                     context.getInferredSecurityPolicy().append(ts);
                 }

                 SecurityHeader secHeader = context.getSecurableSoapMessage().findSecurityHeader();
                 try {
                     timestamp = (Timestamp)
                         SecurityHeaderBlockImpl.fromSoapElement(
                             secHeader.getCurrentHeaderElement(),Timestamp.class);
                 } catch (XWSSecurityException xwsse) {
                     throw SecurableSoapMessage.newSOAPFaultException(
                           MessageConstants.WSSE_INVALID_SECURITY,
                           "Failure in Timestamp internalization.\n" +
                           "Message is: " + xwsse.getMessage(),
                           xwsse);
                 }
	         // FilterProcessingContext.DEFAULT
                 try {
                     context.getSecurityEnvironment().validateTimestamp (
                          context.getExtraneousProperties(),
                          timestamp, 
                          Timestamp.MAX_CLOCK_SKEW, 
                          Timestamp.TIMESTAMP_FRESHNESS_LIMIT);
                 } catch (XWSSecurityException xwsse) {
                    throw SecurableSoapMessage.newSOAPFaultException(
                         MessageConstants.WSSE_INVALID_SECURITY,
                         "Failure in Timestamp validation.\n" +
                         "Message is: " + xwsse.getMessage(),
                         xwsse);
                 }

                 if (MessageConstants.debug) {
                     log.log(Level.FINEST, "Validated TIMESTAMP.....");
                 }
            }
         }
     }
}
