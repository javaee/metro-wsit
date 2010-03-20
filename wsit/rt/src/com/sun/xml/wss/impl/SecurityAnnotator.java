/*
 * $Id: SecurityAnnotator.java,v 1.3 2010-03-20 12:33:40 kumarjayanti Exp $
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

package com.sun.xml.wss.impl;

import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.StaticPolicyContext;
import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;
import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.*;

/**
 * This class exports a static Security Service for Securing an Outbound SOAPMessage.
 * The policy to be applied for Securing the Message and the SOAPMessage itself are 
 * supplied in an instance of a com.sun.xml.wss.ProcessingContext
 * @see ProcessingContext
 */
public class SecurityAnnotator {

	private static Logger log = Logger.getLogger(
	    LogDomainConstants.WSS_API_DOMAIN,
	    LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    /**
     * Secure an Outbound SOAP Message. 
     * <P>
     * Calling code should create a com.sun.xml.wss.ProcessingContext object with
     * runtime properties. Specifically, it should set SecurityPolicy, application
     * CallbackHandler Or a SecurityEnvironment and static security policy context. 
     * The SecurityPolicy instance can be of the following types:
     * <UL>
     *  <LI> A concrete WSSPolicy
     *  <LI> A MessagePolicy
     *  <LI> A DynamicSecurityPolicy
     * </UL>
     *
     * A DynamicSecurityPolicy can inturn resolve to the following:
     * <UL>
     *  <LI> A concrete WSSPolicy 
     *  <LI> A MessagePolicy
     * </UL>
     *
     * @param context an instance of com.sun.xml.wss.ProcessingContext
     * @throws com.sun.xml.wss.XWSSecurityException if there was an error in securing the Outbound SOAPMessage
     */
    public static void secureMessage(ProcessingContext context)
    throws XWSSecurityException {

        HarnessUtil.validateContext (context);

        SecurityPolicy policy = context.getSecurityPolicy ();
        SecurityEnvironment handler = context.getSecurityEnvironment ();
        StaticPolicyContext staticContext = context.getPolicyContext ();

        FilterProcessingContext fpContext = setFilterProcessingContext(context);
        
        fpContext.isInboundMessage (false);
         if (fpContext.resetMustUnderstand()) {
            fpContext.getSecurableSoapMessage().setDoNotSetMU(true);
        }
   
        if (PolicyTypeUtil.messagePolicy(policy) &&
                (((MessagePolicy)policy).enableDynamicPolicy() && 
                ((MessagePolicy)policy).size() == 0)) {
            policy = new com.sun.xml.wss.impl.policy.mls.DynamicSecurityPolicy();
        }
        
        if (PolicyTypeUtil.dynamicSecurityPolicy(policy)) {

            // create dynamic callback context
            DynamicApplicationContext dynamicContext = new DynamicApplicationContext (staticContext);
            dynamicContext.setMessageIdentifier (context.getMessageIdentifier ());
            dynamicContext.inBoundMessage (false);
            ProcessingContext.copy (dynamicContext.getRuntimeProperties(), context.getExtraneousProperties());

            // make dynamic policy callback
            DynamicPolicyCallback dpCallback = new DynamicPolicyCallback (policy, dynamicContext);
            try {
               HarnessUtil.makeDynamicPolicyCallback(dpCallback,
                          handler.getCallbackHandler());

            } catch (Exception e) {
               log.log(Level.SEVERE, "WSS0237.failed.DynamicPolicyCallback", e);
               throw new XWSSecurityException (e);
            }

            SecurityPolicy result = dpCallback.getSecurityPolicy ();
            fpContext.setSecurityPolicy (result);

            if (PolicyTypeUtil.messagePolicy(result)) {
                processMessagePolicy (fpContext);
            } else
            if (result instanceof WSSPolicy) {
                HarnessUtil.processWSSPolicy (fpContext);
            } else if ( result != null ) {
                log.log(Level.SEVERE, "WSS0260.invalid.DSP");
                throw new XWSSecurityException ("Invalid dynamic security policy returned by callback handler");
            }

        } else if (PolicyTypeUtil.messagePolicy(policy)) {
            fpContext.enableDynamicPolicyCallback(((MessagePolicy)policy).enableDynamicPolicy());
            processMessagePolicy(fpContext);
        } else if (policy instanceof WSSPolicy) {
            HarnessUtil.processWSSPolicy (fpContext);
        } else {
            log.log(Level.SEVERE, "WSS0251.invalid.SecurityPolicyInstance");
            throw new XWSSecurityException ("SecurityPolicy instance should be of type: " +
                                            "WSSPolicy OR MessagePolicy OR DynamicSecurityPolicy");
        }
    }

    /*
     * @param fpContext com.sun.xml.wss.FilterProcessingContext
     *
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private static void processMessagePolicy (FilterProcessingContext fpContext)
    throws XWSSecurityException {

        MessagePolicy policy = (MessagePolicy) fpContext.getSecurityPolicy ();

        if(policy.enableWSS11Policy()){
            // set a property in context to determine if its WSS11
            fpContext.setExtraneousProperty("EnableWSS11PolicySender","true");
        }

        // DO it always as policy not available in optimized path
        //if (policy.enableSignatureConfirmation()) {
            //For SignatureConfirmation
            //Set a list in extraneous property which will store all the outgoing SignatureValues
            //If there was no Signature in outgoing message this list will be empty
            List scList = new ArrayList();
            fpContext.setExtraneousProperty("SignatureConfirmation", scList);
        //}
        
        Iterator i = policy.iterator ();

        while (i.hasNext ()) {
            SecurityPolicy sPolicy = (SecurityPolicy) i.next();
            fpContext.setSecurityPolicy (sPolicy);
            HarnessUtil.processDeep (fpContext);
        }

        if(!(fpContext instanceof JAXBFilterProcessingContext)){
            if (policy.dumpMessages())
                DumpFilter.process(fpContext);
        }
    }

    /*
     * @param context com.sun.xml.wss.Processing Context
     */
    public static void handleFault (ProcessingContext context) {
        /**
         * TODO:
         */
    }
    
    public static FilterProcessingContext setFilterProcessingContext(ProcessingContext context) 
            throws XWSSecurityException{
        if(context instanceof JAXBFilterProcessingContext)
            return (JAXBFilterProcessingContext)context;
        return new FilterProcessingContext (context);
    }
}
