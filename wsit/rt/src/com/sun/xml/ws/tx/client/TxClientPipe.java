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
package com.sun.xml.ws.tx.client;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.assembler.dev.ClientTubelineAssemblyContext;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.tx.at.ATCoordinator;
import com.sun.xml.ws.tx.common.ATAssertion;
import static com.sun.xml.ws.tx.common.ATAssertion.NOT_ALLOWED;
import static com.sun.xml.ws.tx.common.ATAssertion.ALLOWED;
import static com.sun.xml.ws.tx.common.ATAssertion.MANDATORY;
import static com.sun.xml.ws.tx.common.Constants.WSAT_2004_PROTOCOL;
import com.sun.xml.ws.tx.common.StatefulWebserviceFactoryFactory;
import com.sun.xml.ws.tx.common.TxBasePipe;
import com.sun.xml.ws.tx.common.TxJAXBContext;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.coordinator.ContextFactory;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;
import com.sun.xml.ws.tx.coordinator.CoordinationManager;
import com.sun.xml.ws.tx.webservice.member.coord.CoordinationContext;
import com.sun.xml.ws.tx.webservice.member.coord.CoordinationContextType;
import java.util.logging.Level;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * This class process transactional context for client outgoing message.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.17 $
 * @since 1.0
 */
// suppress known deprecation warnings about using pipes.
@SuppressWarnings("deprecation")
public class TxClientPipe extends TxBasePipe {

    static private TxLogger logger = TxLogger.getCoordLogger(TxClientPipe.class);

    final QName COORD_CTX_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/10/wscoor", "CoordinationContext");

    /**
     * Contains policy assertions
     */
    private final PolicyMap policyMap;
    private final WSDLPort wsdlPort;

    private final SOAPVersion soapVersion;

    /**
     * Interact with Security pipe that ran previously.
     */
    //private final SecurityPipeContext spctx;

    
    /**
     * Marshaller
     */
    private Marshaller marshaller;

    /**
     * Construct a new outbound tx pipe.
     *
     * @param pcfg ws-policy configuration
     * @param next the next pipe in the chain
     */
    public TxClientPipe(ClientTubelineAssemblyContext context, Pipe next) {
        super(next);
        this.policyMap = context.getPolicyMap();
        this.wsdlPort = context.getWsdlPort();
        this.soapVersion = context.getBinding().getSOAPVersion();

        //this.spctx = spctx;
        this.marshaller = TxJAXBContext.createMarshaller();
    }

    /**
     * Constructor used by copy method
     */
    private TxClientPipe(TxClientPipe orig, PipeCloner cloner) {
        super(cloner.copy(orig.next));
        cloner.add(orig, this);
        this.policyMap = orig.policyMap;
        this.wsdlPort = orig.wsdlPort;
        this.soapVersion = orig.soapVersion;
        this.marshaller = TxJAXBContext.createMarshaller();
    }

    /**
     * Creates an identical clone of this Pipe.
     */
    public Pipe copy(PipeCloner cloner) {
        return new TxClientPipe(this, cloner);
    }

    
    /**
     * Process transactional context in outgoing message.
     *
     * Transactional context is only flowed if the following conditions are met:
     * <ul>
     *   <li>current JTA Transaction</li>
     *   <li>wsdl:binding/wsdl:operation of this packet has wsat:ATAssertion</li>
     * </ul>
     *
     * @param pkt
     * @return null
     */
    public Packet process(Packet pkt) {
        final Message msg = pkt.getMessage();
        Packet responsePacket = null;
        
        // TODO:  minimizing process overhead for cases that do not flow a transaction.
        //        Current assumption is it is cheaper to check if there is a current JTA transaction
        //        than it is to check if a wsdl:binding/wsdl:operation has wsat:ATAssertion
        //        If that assumption is incorrect, exchange this check with one checking for
        //        existence of wsat policy assertion.
        //        
        Transaction currentTxn = checkCurrentJTATransaction(msg, wsdlPort);
        if (currentTxn == null) {
            return next.process(pkt);
        }

        // get trust plugin from security pipe
        // encryption through security pipe
    
        final WSDLBoundOperation wsdlBoundOp = msg.getOperation(wsdlPort);    
        ATAssertion atAssertion = getOperationATPolicy(policyMap, wsdlPort, wsdlBoundOp).atAssertion;
        if (atAssertion == NOT_ALLOWED ) {
                // no ws-at policy assertion on the wsdl:binding/wsdl:operation, so no work to do here
                if (logger.isLogging(Level.FINE)) {
                    logger.fine("process", "no ws-at policy asssertion for " + wsdlBoundOp.getName().toString());
                }
                return next.process(pkt);
        }

//        TODO: Issue a warning if can not flow a WS-AT transaction context due to following implementation limitations
//        1. From an Application Container
//        2. On an asynchronous web service invocation   (should be able to detect this statically)
//        Recorded as wsit issue 471.
        if ((atAssertion == MANDATORY || atAssertion == ALLOWED) ) {
            // TODO: Is it possible to determine (2) in this pipe??
          
            // Following conditional is true in application client OR when 
            // configuration of wstx service is invalid. 
            // Possible misconfigurations are wrong port for wstx_service,
            // improper security certificates.
            if (! StatefulWebserviceFactoryFactory.getInstance().isWSTXServiceAvailable()) {
                logger.warning("TxClientPipe",
                        LocalizationMessages.
                        WSAT_TXN_CONTEXT_NOT_FLOWED_1001(
                        msg.getOperation(wsdlPort).getName().toString()));
                return next.process(pkt);
            }  
        }
        
        // get the coordination context from JTS ThreadLocal data
        CoordinationContextInterface context = lookupOrCreateCoordinationContext(atAssertion);
        CoordinationContext CC = (CoordinationContext) context.getValue(); // TODO: fix cast
        Header ccHeader;
        if (atAssertion == MANDATORY || atAssertion == ALLOWED) {
            // flow current txn scope CC with msg
            // Generate <wst:IssuedToken> header.
            // The <wscoor:Identifier> is passed as <wsp:AppliesTo> in the
            // <wst:RequestSecurityTokenResponse>.  The resulting <wst:IssuedToken>
            // must be injected into the CoodinationContext and subsequently encrypted
            CoordinationContextType.Identifier id = CC.getIdentifier(); // coordinated activity id
            //IssuedTokenContext itCtx = spctx.processClientInitRSCTR(msg, id, scid /* what's  scid? */);
            // ISSUE: who will inject the <wst:IssuedToken> txser???

            // add coordination context into message header

            // Add soap:MustUnderstand.
            CC.getOtherAttributes().put(new QName(soapVersion.nsUri, "mustUnderstand"), "true");
            ccHeader = Headers.create(soapVersion, marshaller, COORD_CTX_QNAME, CC);

            msg.getHeaders().add(ccHeader);
        }

        // Suspend transaction from current thread.
        // If web service invocations is within same application server vm,
        // then the same JTA transaction will be used and it must only be associated
        // with one thread at any point in time. Will resume transaction when it returns.
        try {
            currentTxn = txnMgr.suspend();
        } catch (SystemException ex) {
            throw new WebServiceException(ex.getMessage(), ex);
        }
        Exception rethrow = null;
        try {
            responsePacket = next.process(pkt);
        } catch (Exception e) {
            rethrow = e;
        } finally {
            try {
                // flow of control is transfered back from caller, resume transaction.
                txnMgr.resume(currentTxn);
            } catch (Exception ex) {
                if (rethrow != null) {
                    rethrow.initCause(ex);
                    throw new WebServiceException(ex.getMessage(), rethrow);
                } else {
                    rethrow = ex;
                }
            }
            if (rethrow != null) {
                throw new WebServiceException(rethrow.getMessage(), rethrow);
            }
        }
        return responsePacket;
    }

    private CoordinationContextInterface lookupOrCreateCoordinationContext(ATAssertion assertion) {
        Transaction currentTxn;
        CoordinationContextInterface result = null;
        try {
            currentTxn = txnMgr.getTransaction();
        } catch (SystemException e) {
            throw new WebServiceException(e.getMessage(), e);
        }

        // wsat policy assertion validation when no current txn scope CC
        if ((currentTxn == null) && (assertion == MANDATORY)) {
            // txn scope MANDATORY to invoke this operation, notify user
            throw new WebServiceException(LocalizationMessages.MISSING_TX_SCOPE_1000());
        }

        if (currentTxn != null) {
            // see if a coordination context is already associated with the current JTA transaction.
            result = txnMgr.getCoordinationContext();
            if (result == null) {
                // create & associate a coordination context with current thread's transaction context
                final long EXPIRES = txnMgr.getRemainingTimeout() * 1000L;
                result = ContextFactory.createContext(WSAT_2004_PROTOCOL, EXPIRES);

                // create a new coordinator object for this context. Associate JTA transaction with ATCoordinator.
                ATCoordinator coord = new ATCoordinator(result);
                coord.setTransaction(currentTxn);
                CoordinationManager.getInstance().putCoordinator(coord);

                // cache the resulting context in the transaction context
                txnMgr.setCoordinationContext(result);
            }
        }
        return result;
    }

    static private boolean reportCheckCurrentJTAStacktrace = true;
    
    private Transaction checkCurrentJTATransaction(Message msg, WSDLPort wsdlModel) {
        Transaction currentTxn = null;
        try {
            currentTxn = txnMgr.getTransaction();
       
// workaround for glassfksh issue 2659, catch throwable instead of just SystemException.
//        } catch (SystemException se) {
          } catch (Throwable t) {
            if (logger.isLogging(Level.FINEST)) {
                if (reportCheckCurrentJTAStacktrace) {
                    reportCheckCurrentJTAStacktrace = false;
                    logger.finest("checkCurrentJTATransaction",
                            "handled exception thrown during checkCurrentJTATransaction",
                             t);
                } else {
                     logger.finest("checkCurrentJTATransaction",
                            "handled exception thrown during checkCurrentJTATransaction");
                }
            }
        }
        if (currentTxn == null) {
            // no current JTA transaction, so no work to do here
            if (logger.isLogging(Level.FINEST)) {
                logger.finest("process", "no current JTA transaction for invoked operation " +
                        msg.getOperation(wsdlModel).getName().toString());
            }
        }
        return currentTxn;
    }
}
