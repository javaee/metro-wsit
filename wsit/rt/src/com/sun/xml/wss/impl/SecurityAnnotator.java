/*
 * $Id: SecurityAnnotator.java,v 1.1 2006-05-03 22:57:37 arungupta Exp $
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

package com.sun.xml.wss.impl;

import com.sun.xml.wss.ProcessingContext;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.StaticPolicyContext;
import com.sun.xml.wss.impl.policy.DynamicSecurityPolicy;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;

import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;

import  com.sun.xml.wss.impl.filter.DumpFilter;
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

        FilterProcessingContext fpContext = new FilterProcessingContext (context);
        fpContext.isInboundMessage (false);
   
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
               // log
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
                // log
                throw new XWSSecurityException ("Invalid dynamic security policy returned by callback handler");
            }

        } else if (PolicyTypeUtil.messagePolicy(policy)) {
            fpContext.enableDynamicPolicyCallback(((MessagePolicy)policy).enableDynamicPolicy());
            processMessagePolicy(fpContext);
        } else if (policy instanceof WSSPolicy) {
            HarnessUtil.processWSSPolicy (fpContext);
        } else {
            // log
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

        if (policy.enableSignatureConfirmation()) {
            //For SignatureConfirmation
            //Set a list in extraneous property which will store all the outgoing SignatureValues
            //If there was no Signature in outgoing message this list will be empty
            List scList = new ArrayList();
            fpContext.setExtraneousProperty("SignatureConfirmation", scList);
        }
        
        Iterator i = policy.iterator ();

        while (i.hasNext ()) {
            SecurityPolicy sPolicy = (SecurityPolicy) i.next();
            fpContext.setSecurityPolicy (sPolicy);
            HarnessUtil.processDeep (fpContext);
        }

        if (policy.dumpMessages())
            DumpFilter.process(fpContext);
    }

    /*
     * @param context com.sun.xml.wss.Processing Context
     */
    public static void handleFault (ProcessingContext context) {
        /**
         * TODO:
         */
    }
}
