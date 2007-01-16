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
import com.sun.xml.ws.assembler.ClientPipeConfiguration;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import static com.sun.xml.ws.policy.PolicyMap.createWsdlOperationScopeKey;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.tx.at.ATCoordinator;
import com.sun.xml.ws.tx.common.ATAssertion;
import static com.sun.xml.ws.tx.common.ATAssertion.NOT_ALLOWED;
import static com.sun.xml.ws.tx.common.ATAssertion.ALLOWED;
import static com.sun.xml.ws.tx.common.ATAssertion.REQUIRED;
import static com.sun.xml.ws.tx.common.Constants.AT_ASSERTION;
import static com.sun.xml.ws.tx.common.Constants.WSAT_2004_PROTOCOL;
import com.sun.xml.ws.tx.common.TransactionManagerImpl;
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
import java.util.Iterator;


/**
 * This class process transactional context for client outgoing message.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.3 $
 * @since 1.0
 */
// suppress known deprecation warnings about using pipes.
@SuppressWarnings("deprecation")
public class TxClientPipe implements Pipe {

    static private TxLogger logger = TxLogger.getCoordLogger(TxClientPipe.class);

    final QName COORD_CTX_QNAME = new QName("http://schemas.xmlsoap.org/ws/2004/10/wscoor", "CoordinationContext");

    /**
     * Contains policy assertions
     */
    private final ClientPipeConfiguration pipeConfig;

    private final SOAPVersion soapVersion;

    /**
     * Interact with Security pipe that ran previously.
     */
    //private final SecurityPipeContext spctx;

    /**
     * next pipe in the chain
     */
    private final Pipe next;

    /**
     * Marshaller
     */
    private Marshaller marshaller;

    /**
     * Construct a new outbound tx pipe.
     *
     * @param pcfg ws-policy configuration
     * @param next the next pipe in the chain
     *             param spctx security context
     */
    public TxClientPipe(ClientPipeConfiguration pcfg,
                        Pipe next/*,
                        SecurityPipeContext spctx*/) {
        this.pipeConfig = pcfg;
        this.soapVersion = pcfg.getBinding().getSOAPVersion();

        //this.spctx = spctx;
        this.next = next;
        this.marshaller = TxJAXBContext.createMarshaller();
    }

    /**
     * Constructor used by copy method
     */
    private TxClientPipe(TxClientPipe orig, PipeCloner cloner) {
        cloner.add(orig, this);
        this.next = cloner.copy(orig.next);
        this.pipeConfig = orig.pipeConfig;
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
     * Invoked before the last copy of the pipeline is about to be discarded,
     * to give Pipes a chance to clean up any resources.
     */
    public void preDestroy() {
        // no-op
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
        final WSDLPort wsdlModel = pipeConfig.getWSDLModel();
        Packet responsePacket = null;
        
        // TODO:  minimizing process overhead for cases that do not flow a transaction.
        //        Current assumption is it is cheaper to check if there is a current JTA transaction
        //        than it is to check if a wsdl:binding/wsdl:operation has wsat:ATAssertion
        //        If that assumption is incorrect, exchange this check with one checking for
        //        existence of wsat policy assertion.
        //        
        Transaction currentTxn = checkCurrentJTATransaction(msg, wsdlModel);
        if (currentTxn == null) {
            return next.process(pkt);
        }

        // NOTE: if necessary, use AddressingContext, don't set the values directly.  The
        //       context abstracts away WSA version differences.

        // TODO: throw UnsupportedOperationException if this is an async call

        // get trust plugin from security pipe
        // encryption through security pipe
    
        final WSDLBoundOperation wsdlBoundOp = msg.getOperation(wsdlModel);    
        ATAssertion atAssertion = getOperationATPolicy(pipeConfig.getPolicyMap(), wsdlModel, wsdlBoundOp);
        if (atAssertion == NOT_ALLOWED ) {
                // no ws-at policy assertion on the wsdl:binding/wsdl:operation, so no work to do here
                if (logger.isLogging(Level.FINE)) {
                    logger.fine("process", "no ws-at policy asssertion for " + wsdlBoundOp.getName().toString());
                }
                return next.process(pkt);
        }

        // get the coordination context from JTS ThreadLocal data
        CoordinationContextInterface context = lookupOrCreateCoordinationContext(atAssertion);
        CoordinationContext CC = (CoordinationContext) context.getValue(); // TODO: fix cast
        Header ccHeader;
        if (atAssertion == REQUIRED || atAssertion == ALLOWED) {
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

        // TODO: Only need the suspend and resume when the web service invocation of this client is to a web service
        // in same application server.  Must figure out how to identify this case. For now, just hardcode assumption
        // that it is in same app server and this is necessary.  It does not hurt to do this, it is probably inefficient.
        
        try {
            currentTxn = TransactionManagerImpl.getInstance().suspend();
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
                TransactionManagerImpl.getInstance().resume(currentTxn);
            } catch (Exception ex) {
                // TODO: should we somehow link in rethrow if it isn't null???
                throw new WebServiceException(ex.getMessage(), ex);
            }
            if (rethrow != null) {
                throw new WebServiceException(rethrow.getMessage(), rethrow);
            }
        }
        return responsePacket;
    }

    private CoordinationContextInterface lookupOrCreateCoordinationContext(ATAssertion required) {
        Transaction currentTxn;
        CoordinationContextInterface result = null;
        try {
            currentTxn = TransactionManagerImpl.getInstance().getTransaction();
        } catch (SystemException e) {
            throw new WebServiceException(e.getMessage(), e);
        }

        //wsat policy assertion validation when no current txn scope CC
        if ((currentTxn == null) && (required == REQUIRED)) {
            // txn scope required to invoke this operation, notify user
            throw new WebServiceException(LocalizationMessages.MISSING_TX_SCOPE());
        }

        if (currentTxn != null) {
            // see if a coordination context is already associated with the current JTA transaction.
            result = TransactionManagerImpl.getInstance().getCoordinationContext();
            if (result == null) {
                // create & associate a coordination context with current thread's transaction context
                result = ContextFactory.createContext(WSAT_2004_PROTOCOL,
                        TransactionManagerImpl.getInstance().getDefaultTransactionTimeout());

                // create a new coordinator object for this context. Associate JTA transaction with ATCoordinator.
                ATCoordinator coord = new ATCoordinator(result);
                coord.setTransaction(currentTxn);
                CoordinationManager.getInstance().putCoordinator(coord);

                // cache the resulting context in the transaction context
                TransactionManagerImpl.getInstance().setCoordinationContext(result);
            }
        }
        return result;
    }

    private Transaction checkCurrentJTATransaction(Message msg, WSDLPort wsdlModel) {
        Transaction currentTxn = null;
        try {
            currentTxn = TransactionManagerImpl.getInstance().getTransaction();
        } catch (SystemException ex) {
            // ignore
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
    
    /**
     * Return the ws-at policy assertion associated with wsdlBoundOp.
     */ 
    private ATAssertion getOperationATPolicy(PolicyMap pmap, WSDLPort wsdlModel, WSDLBoundOperation wsdlBoundOp) 
        throws WebServiceException 
    {
        // get the ws-at policy assertion wsat:ATAssertion (don't need wsat:ATAlwaysCapable on the client side).
        ATAssertion atAssertion = NOT_ALLOWED;
        try {
            if (pmap != null) {
                PolicyMapKey opKey =
                        createWsdlOperationScopeKey(
                        wsdlModel.getOwner().getName(), // service
                        wsdlModel.getName(), // port
                        wsdlBoundOp.getName() // operation
                        );
                Policy effectivePolicy =
                        pmap.getOperationEffectivePolicy(opKey);
                if (effectivePolicy != null) {
                    Iterator<AssertionSet> assertionIter = effectivePolicy.iterator();
                    AssertionSet assertionSet;
                    while (assertionIter.hasNext()) {
                        assertionSet = assertionIter.next();
                        for (PolicyAssertion pa : assertionSet) {
                            if (pa.getName().equals(AT_ASSERTION)) {
                                atAssertion = pa.isOptional() ? ALLOWED : REQUIRED;
                            }
                        }
                    }
                }
            }
        } catch (PolicyException pe) {
            throw new WebServiceException(pe.getMessage(), pe);
        }
        return atAssertion;
    }
}
