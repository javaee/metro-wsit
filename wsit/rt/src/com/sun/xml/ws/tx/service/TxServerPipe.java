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
package com.sun.xml.ws.tx.service;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.assembler.dev.ServerTubelineAssemblyContext;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.tx.at.ATCoordinator;
import com.sun.xml.ws.tx.at.ATSubCoordinator;
import static com.sun.xml.ws.tx.common.ATAssertion.*;
import static com.sun.xml.ws.tx.common.Constants.*;
import com.sun.xml.ws.tx.common.Message;
import com.sun.xml.ws.tx.common.TxBasePipe;
import com.sun.xml.ws.tx.common.TxFault;
import com.sun.xml.ws.tx.common.TxJAXBContext;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.common.WsaHelper;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;
import com.sun.xml.ws.tx.coordinator.CoordinationManager;

import javax.servlet.ServletContext;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Process transactional context for incoming message to server.
 * <p/>
 * Supports following WS-Coordination protocols: 2004 WS-Atomic Transaction protocol
 *
 * @version $Revision: 1.23.2.2 $
 * @since 1.0
 */
// suppress known deprecation warnings about using pipes.
@SuppressWarnings("deprecation")
public class TxServerPipe extends TxBasePipe {

    static private TxLogger logger = TxLogger.getLogger(TxServerPipe.class);
   
    // unmarshalls supported WS-AT coordination context formats
    final private Unmarshaller unmarshaller;
    
    final private WSDLPort port;
    final private WSBinding wsbinding;


    /**
     * Deployment time computations for WS-Atomic Tranaction processing.
     *
     * <p>
     * Computes WS-Atomic Policy Assertions for all wsdl bound operations for this <code>wsbinding</code>.
     *
     * @param port WSDL port for this pipe
     * @param map  PolicyMap
     * @param next Next pipe to be executed.
     */
    public TxServerPipe(ServerTubelineAssemblyContext context, Pipe next) {
        super(next);
        unmarshaller = TxJAXBContext.createUnmarshaller();
        this.port = context.getWsdlPort();
        this.wsbinding = context.getEndpoint().getBinding();
        cacheOperationToPolicyMappings(context.getPolicyMap(), context.getWsdlPort().getBinding());
    }

    private TxServerPipe(TxServerPipe from, PipeCloner cloner) {
        super(cloner.copy(from.next));
        cloner.add(from, this);
        this.port = from.port;
        this.wsbinding = from.wsbinding;
        this.unmarshaller = TxJAXBContext.createUnmarshaller();
        this.opPolicyCache = from.opPolicyCache;
    }

    /**
     * Creates an identical clone of this Pipe.
     */
    public Pipe copy(PipeCloner cloner) {
        return new TxServerPipe(this, cloner);
    }

    /**
     * Process WS-AT transactional context in incoming request message.
     * <p/>
     * <p>
     * Transactional context processing is driven by ws-at policy assertions associated with
     * wsdl:binding/wsdl:operation of parameter <code>pkt</code>.
     *
     * @param pkt a packet is an incoming request message with JAX-WS properties
     * @return processing of pkt by next pipe in pipeline
     */
    public Packet process(Packet pkt) {
         final String METHOD_NAME = "TxServerPipe.process";
         if (logger.isLogging(Level.FINER)) {
             Object[] params = new Object[1];
             params[0] = pkt;
             logger.entering(METHOD_NAME, params);
         }
        
        com.sun.xml.ws.tx.common.Message msg = new Message(pkt.getMessage(), wsbinding);
        WSDLBoundOperation msgOp = msg.getOperation(port);
        QName msgOperation = msgOp.getName();
        OperationATPolicy msgOpATPolicy = getATPolicy(msgOperation);
        String bindingName = port.getBinding().getName().toString();
        CoordinationContextInterface coordTxnCtx = null;
        
        // Precondition check
        assertNoCurrentTransaction(
           LocalizationMessages.INVALID_JTA_TRANSACTION_ENTERING_5002(bindingName, msgOperation.getLocalPart()));
        
        try {
            coordTxnCtx = msg.getCoordinationContext(unmarshaller);
        } catch (JAXBException je) {
            String invalidMsg = 
                    LocalizationMessages.INVALID_COORDINATION_CONTEXT_5006(
                                    msg.getMessageID(), 
                                    bindingName, 
                                    msgOperation.getLocalPart(), 
                                    je.getLocalizedMessage());
            logger.warning(METHOD_NAME, invalidMsg, je);
            
            // send fault S4.3 wscoor:InvalidParameters
            
            // DO NOT LOCALIZE englishReason
            // 2004 WS-Coordination, S4. Coordination Faults "[Reason] The English language reason element""
            String englishReason = "WSTX-SERVICE-5006: Unable to read CoordinationContext in request message, msgId=" + 
                                msg.getMessageID() + ", sent to endpoint:operation " + bindingName + ":" + 
                                msgOperation.getLocalPart() + " due to JAXException " + je.getMessage(); 
            WSEndpointReference replyTo = msg.getReplyTo();
            if (replyTo != null) {
                WsaHelper.sendFault(
                        msg.getFaultTo(),
                        replyTo.toSpec(),
                        SOAPVersion.SOAP_11,
                        TxFault.InvalidParameters,
                        englishReason,
                        msg.getMessageID());
            }
        }
        
        // verify coordination type protocol
        if (coordTxnCtx != null) {
            if (coordTxnCtx.getCoordinationType().equals(WSAT_2004_PROTOCOL)) {
                if (logger.isLogging(Level.FINEST)) {
                    logger.finest(METHOD_NAME, "Processing wscoor:CoordinationContext(protocol=" + coordTxnCtx.getCoordinationType() +
                                  "coordId=" + coordTxnCtx.getIdentifier() + ") for binding:" + bindingName + 
                                  "operation:"  + msgOp.getName());
                }
                
                //  let jax-ws runtime know that coordination context header was understood.
                msg.setCoordCtxUnderstood();
            } // else check for other supported protocols in future.
            else {  // unrecognized ws coordination protocol type
                // Another pipe *may* process CoordinationContext with this unknown protocol type so just log this.
                logger.info(METHOD_NAME, 
                        LocalizationMessages.IGNORING_UNRECOGNIZED_PROTOCOL_5005(
                        coordTxnCtx.getCoordinationType(),
                        coordTxnCtx.getIdentifier(),
                        bindingName,
                        msgOperation.getLocalPart()));
                coordTxnCtx = null;
            }
        }

        // wsat:ATAssertion policy assertion check.  Note 2004 WS-Atomic Transaction does not require this check.
        if (msgOpATPolicy.atAssertion == MANDATORY && coordTxnCtx == null) {
            String inconsistencyMsg = 
                    LocalizationMessages.MUST_FLOW_WSAT_COORDINATION_CONTEXT_5000(
                                bindingName, msgOperation.getLocalPart(), msg.getMessageID());
            logger.warning(METHOD_NAME, inconsistencyMsg);
            // TODO:  complete evaluation if desire to throw this as an exception or not.
            //        Since WS-AT specification does not require this, might be best not to throw exception.
            throw new WebServiceException(inconsistencyMsg);
        }

        /*
         * Absence of a ws-at ATAssertion does not forbid WS-AT CoordinationContext from flowing.
         * OASIS WS-AT refers to this case as no claims made. Rules external to WS-AT may have
         * caused this situation to occur.
         */
        if (coordTxnCtx != null && msgOpATPolicy.atAssertion == NOT_ALLOWED) {
            
            // Not an error just log this as occuring since it would be helpful to know about this.
            logger.info(METHOD_NAME, 
                    LocalizationMessages.UNEXPECTED_FLOWED_TXN_CONTEXT_5004(
                                  bindingName, msgOperation.getLocalPart(), msg.getMessageID(),
                                  coordTxnCtx.getIdentifier()));
        }

        boolean importedTxn = false;
        Packet responsePkt = null;
        Transaction jtaTxn = null;
        Exception rethrow = null;
        boolean isRolledBackTxn = false;
        ATCoordinator coord = null;

        if (coordTxnCtx != null) {
            coord = (ATCoordinator) CoordinationManager.getInstance().lookupOrCreateCoordinator(coordTxnCtx);
            assert coord != null;

            jtaTxn = coord.getTransaction();
            if (jtaTxn != null) {
                if (logger.isLogging(Level.FINER)) {
                    logger.finer(METHOD_NAME, "Resume JTA Txn already associated with coordId=" + 
                                  coordTxnCtx.getIdentifier());
                }
                coord.resumeTransaction();  
                try {
                    responsePkt = next.process(pkt);
                } catch (Exception e) {
                    logger.warning(METHOD_NAME,
                                   LocalizationMessages.HANDLE_EXCEPTION_TO_COMPLETE_TRANSACTION_5012(coordTxnCtx.getIdentifier(),
                                                                                                 jtaTxn.toString()), 
                                   e);
                    rethrow = e;
                    txnMgr.setRollbackOnly();
                }
                coord.suspendTransaction();
            } else if (coord.isSubordinateCoordinator()) {
                if (logger.isLogging(Level.FINER)) {
                    logger.finer(METHOD_NAME, "importing ws-at activity id:" + coordTxnCtx.getIdentifier() +   
                                              " from external WS-AT coordinator");
                }
                importedTxn = true;
                ((ATSubCoordinator)coord).beginImportTransaction();
                try {
                    responsePkt = next.process(pkt);
                } catch (Exception e) {
                    jtaTxn = coord.getTransaction();
                    final String jtaTxnString = jtaTxn == null ? "" : jtaTxn.toString();
                    logger.warning(METHOD_NAME,
                                   LocalizationMessages.HANDLE_EXCEPTION_TO_RELEASE_IMPORTED_TXN_5013(coordTxnCtx.getIdentifier(),
                                                                                                 jtaTxnString), 
                                   e);
                    rethrow = e;
                    txnMgr.setRollbackOnly();
                }
                // Sun App Server 9.1 does not support suspend/resume with TransactionInflow, so release imported txn here.
                //coord.suspendTransaction();
                ((ATSubCoordinator)coord).endImportTransaction();
            } else {
                responsePkt = next.process(pkt);
            }
        } else if (msgOpATPolicy.ATAlwaysCapability == true) {
            
            // no Transaction context flowed with message but WS-AT policy assertion requests auto creation of txn
            // context on server side invocation of method.
            if (isServlet(pkt)) {
                beginTransaction();
                
                if (logger.isLogging(Level.FINER)) {
                    logger.finer(METHOD_NAME, "create JTA Transaction, no CoordinationContext flowed with operation and wsat:ATAlwaysCapability is enabled");
                }
            }    // else allow Java EE EJB container to create transaction context
                 // for Sun Application Server:  see BaseContainer.preInvokeTx
            try {
                responsePkt = next.process(pkt);
                if (isServlet(pkt)) {
                    // Test if transaction was marked for rollback by method
                    isRolledBackTxn = (txnMgr.getUserTransaction ().getStatus() == Status.STATUS_MARKED_ROLLBACK);
                }
            } catch (Exception e) {
                rethrow = e;
                logger.warning(METHOD_NAME, 
                               LocalizationMessages.HANDLE_EXCEPTION_TO_COMMIT_CREATED_TXN_5015(), e);
                txnMgr.setRollbackOnly();
            }
            if (isServlet(pkt)) {
                try {
                  commitTransaction();
                } catch (WebServiceException ex) {
                    if (ex.getCause () instanceof RollbackException) {
                        // Rethrow exception only if it was unexpected rollback
                        if (!isRolledBackTxn) {
                            throw ex;
                        }
                    } else {
                        throw ex;
                    }
                }
            } // else allow Java EE EJB container to finish transaction context
              // for Sun Application Server:  see BaseContainer.postInvokeTx
        } else { 
            responsePkt = next.process(pkt);
        }
        
        // Postcondition check
        assertNoCurrentTransaction(
                LocalizationMessages.INVALID_JTA_TRANSACTION_RETURNING_5003(bindingName, 
                                                                            msgOperation.getLocalPart()));
        if (rethrow != null) {
            String exMsg = LocalizationMessages.WSTXRETHROW_5014(bindingName, 
                                                                 msgOperation.toString());
            throw new WebServiceException(exMsg, rethrow);
        }
        
        logger.exiting(METHOD_NAME, responsePkt);
        return responsePkt;
    }
    
    /**
     * Returns true if <code>pkt</code> was sent to a servlet.
     *
     * @param pkt  packet representing a request message
     */
    private boolean isServlet(Packet pkt) {
        ServletContext sCtx = pkt.endpoint.getContainer().getSPI(javax.servlet.ServletContext.class);
        return sCtx != null;
    }
    
    /**
     * Cache of operation name to its WS-AT policies computed in constructor.
     * An operation is not inserted into cache if its WS-AT policies are the default values.
     * This is a space/time tradeoff.  Saves time in process call by taking up
     * more memory for pipe.  Makes sense for server-side, probably not for client-side
     */
    private Map<QName, OperationATPolicy> opPolicyCache = new HashMap<QName, OperationATPolicy>();
    
    static private OperationATPolicy DEFAULT = new OperationATPolicy();
    
    private OperationATPolicy getDefaultATPolicy() {
        return DEFAULT;
    }
    
    /**
     * Return the WS-AT policy assertions for wsdl bounded <code>operationName</code>.
     *
     * @param operationName wsdl bound operation
     * @return WS-AT policy assertions for <code>operationName</code>
     */
    private OperationATPolicy getATPolicy(QName operationName) {
        OperationATPolicy result = opPolicyCache.get(operationName);
        if (result == null) {
            // return default wsat policies
            result = getDefaultATPolicy();
        }
        return result;
    }
    
    /**
     * This method caches WS-AT policy assertion for all binding operations for the pipe.
     * If an operation has the default WS-AT policy assertions, nothing is inserted in cache for
     * method, the getATPolicy() method handles this case.
     */
    private void cacheOperationToPolicyMappings(PolicyMap pmap, WSDLBoundPortType binding) {
        
        // Cache wsat policy for each wsdl:binding/wsdl:operation for binding
        for (WSDLBoundOperation bindingOp : binding.getBindingOperations()) {
            TxBasePipe.OperationATPolicy opat = DEFAULT;
            try {
                opat = getOperationATPolicy(pmap, port, bindingOp);
                opPolicyCache.put(bindingOp.getName(), opat);
            } catch (WebServiceException wse) {
                logger.warning("cacheOperationToPolicyMappings", 
                        LocalizationMessages.WSAT_POLICY_PROCESSING_FAILURE_5017(
                                                       binding.getName(), bindingOp.getName()), 
                        wse);
            }
            if (logger.isLogging(Level.FINE)) {
                logger.fine("cacheOperationToPolicyMappings", "Operation: " +
                        binding.getName() + ":" +
                        bindingOp.getName() + " WS-AT Policy Assertions: ATAssertion:" + opat.atAssertion +
                        " ATAlwaysCapability:" + opat.ATAlwaysCapability);
            }
            
        }
    }

    /**
     * Create a transaction context for pipe to be processed within.
     */
    private void beginTransaction() {
        try {
           txnMgr.getUserTransaction().begin();
        } catch (NotSupportedException ex) {
            String handlerMsg = LocalizationMessages.TXN_MGR_OPERATION_FAILED_5011("getUserTransaction().begin()");
            logger.warning("beginTransaction", handlerMsg, ex);
            throw new WebServiceException(handlerMsg, ex);
        } catch (SystemException ex) {
            String handlerMsg = LocalizationMessages.TXN_MGR_OPERATION_FAILED_5011("getUserTransaction().begin()");
            logger.warning("beginTransaction", handlerMsg, ex);
            throw new WebServiceException(handlerMsg, ex);
        }
    }

    /**
     * Complete transaction context for pipe to be processed within.
     */
    private void commitTransaction() {
        try {
            txnMgr.getUserTransaction().commit();
        } catch (Exception ex) {
            String commitExceptionMsg = LocalizationMessages.EXCEPTION_DURING_COMMIT_5008();
            logger.warning("commitTransaction", commitExceptionMsg, ex);
            throw new WebServiceException(commitExceptionMsg, ex);
        }
    }
    
    private void assertNoCurrentTransaction(String message) {
        Transaction txn = null;
        try {
            txn = txnMgr.getTransaction();
        } catch (SystemException ex) {
            String handlerMsg = LocalizationMessages.TXN_MGR_OPERATION_FAILED_5011("getTransaction");
            logger.warning("assertNoCurrentTransaction", handlerMsg);
            throw new WebServiceException(handlerMsg, ex);
        }     
        if (txn != null) {
            logger.severe("TxServerPipe.process", message + " " + txn.toString());
            try {
                txnMgr.suspend();
            } catch (SystemException ex) {
                String handlerMsg = LocalizationMessages.TXN_MGR_OPERATION_FAILED_5011("suspend");
                logger.warning("assertNoCurrentTransaction", handlerMsg);
                throw new WebServiceException(handlerMsg, ex);
            }
        } 
    }
}

