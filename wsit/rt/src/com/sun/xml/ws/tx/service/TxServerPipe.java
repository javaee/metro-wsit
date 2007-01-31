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
package com.sun.xml.ws.tx.service;

import com.sun.enterprise.transaction.TransactionImport;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.tx.at.ATCoordinator;
import com.sun.xml.ws.tx.at.CoordinationXid;
import static com.sun.xml.ws.tx.common.Constants.*;
import com.sun.xml.ws.tx.common.Message;
import com.sun.xml.ws.tx.common.TransactionManagerImpl;
import com.sun.xml.ws.tx.common.TxJAXBContext;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.common.WsaHelper;
import com.sun.xml.ws.tx.common.TxFault;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;
import com.sun.xml.ws.tx.coordinator.CoordinationManager;
import javax.servlet.ServletContext;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.Xid;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Process transactional context for incoming message to server.
 * <p/>
 * Supports following WS-Coordination protocols: 2004 WS-Atomic Transaction protocol
 *
 * @version $Revision: 1.11 $
 * @since 1.0
 */
// suppress known deprecation warnings about using pipes.
@SuppressWarnings("deprecation")
public class TxServerPipe implements Pipe {

    static private TxLogger logger = TxLogger.getLogger(TxServerPipe.class);
    static private TransactionManagerImpl tm = TransactionManagerImpl.getInstance();

    // unmarshalls supported WS-AT coordination context formats
    final private Unmarshaller unmarshaller;
    
    final private WSDLPort port;
    final private WSBinding wsbinding;
    final private Pipe next;


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
    public TxServerPipe(WSDLPort port,
                        WSBinding wsbinding,
                        PolicyMap map,
                        Pipe next) {
        unmarshaller = TxJAXBContext.createUnmarshaller();
        this.port = port;
        this.wsbinding = wsbinding;
        cacheOperationToPolicyMappings(map, port.getBinding());
        this.next = next;
    }

    private TxServerPipe(TxServerPipe from, PipeCloner cloner) {
        cloner.add(from, this);
        this.next = cloner.copy(from.next);
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
     * Invoked before the last copy of the pipeline is about to be discarded,
     * to give Pipes a chance to clean up any resources.
     */
    public void preDestroy() {
        next.preDestroy();
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
           LocalizationMessages.INVALID_JTA_TRANSACTION_ENTERING_5002(bindingName, msgOperation.toString()));
        
        try {
            coordTxnCtx = msg.getCoordinationContext(unmarshaller);
        } catch (JAXBException je) {
            String invalidMsg = 
                    LocalizationMessages.INVALID_COORDINATION_CONTEXT_5006(
                                    msg.getMessageID().toString(), 
                                    bindingName, 
                                    msgOperation.toString(), 
                                    je.getLocalizedMessage());
            logger.warning(METHOD_NAME, invalidMsg, je);
            
            // send fault S4.3 wscoor:InvalidParameters
            WsaHelper.sendFault(
                    msg.getFaultTo(),
                    msg.getReplyTo().toSpec(),
                    SOAPVersion.SOAP_11,
                    TxFault.InvalidParameters,
                    invalidMsg,
                    msg.getMessageID());
        }
        
        // verify coordination type protocol
        if (coordTxnCtx != null) {
            if (coordTxnCtx.getCoordinationType().equals(WSAT_2004_PROTOCOL)) {
                if (logger.isLogging(Level.FINEST)) {
                    logger.finest(METHOD_NAME, "Processing wscoor:CoordinationContext(protocol=" + coordTxnCtx.getCoordinationType() +
                                  "coordId=" + coordTxnCtx.getIdentifier().toString() + ") for binding:" + bindingName + 
                                  "operation:"  + msgOp.getName().toString());
                }
                
                //  let jax-ws runtime know that coordination context header was understood.
                msg.setCoordCtxUnderstood();
            } // else check for other supported protocols in future.
            else {  // unrecognized ws coordination protocol type
                // Another pipe *may* process CoordinationContext with this unknown protocol type so just log this.
                logger.info(METHOD_NAME, 
                        LocalizationMessages.IGNORING_UNRECOGNIZED_PROTOCOL_5005(
                        coordTxnCtx.getCoordinationType(),
                        coordTxnCtx.getIdentifier().toString(),
                        bindingName,
                        msgOperation.toString()));
                coordTxnCtx = null;
            }
        }

        // wsat:ATAssertion policy assertion check.  Note 2004 WS-Atomic Transaction does not require this check.
        if (msgOpATPolicy.atAssertion == ATAssertion.MANDATORY && coordTxnCtx == null) {
            String inconsistencyMsg = 
                    LocalizationMessages.MUST_FLOW_WSAT_COORDINATION_CONTEXT_5000(
                                bindingName, msgOperation.toString(), msg.getMessageID().toString());
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
        if (coordTxnCtx != null && msgOpATPolicy.atAssertion == ATAssertion.NOT_ALLOWED) {
            
            // Not an error just log this as occuring since it would be helpful to know about this.
            logger.info(METHOD_NAME, 
                    LocalizationMessages.UNEXPECTED_FLOWED_TXN_CONTEXT_5004(
                                  bindingName, msgOperation.toString(), msg.getMessageID().toString(),
                                  coordTxnCtx.getIdentifier().toString()));
        }

        boolean importedTxn = false;
        Packet responsePkt = null;
        Transaction jtaTxn = null;
        Exception rethrow = null;
        ATCoordinator coord = null;

        if (coordTxnCtx != null) {
            coord = (ATCoordinator) CoordinationManager.getInstance().lookupOrCreateCoordinator(coordTxnCtx);
            assert coord != null;

            jtaTxn = coord.getTransaction();
            if (jtaTxn != null) {
                if (logger.isLogging(Level.FINER)) {
                    logger.finer(METHOD_NAME, "A WS-AT Transaction flowed within same app server instance, resume the JTA transaction already associated with WS-AT CoordinationContext.");
                }
                resumeTransaction(jtaTxn);    
                try {
                    responsePkt = next.process(pkt);
                } catch (Exception e) {
                    logger.warning(METHOD_NAME,
                                   LocalizationMessages.HANDLE_EXCEPTION_TO_COMPLETE_TRANSACTION_5012(coordTxnCtx.getIdentifier().toString(),
                                                                                                 jtaTxn.toString()), 
                                   e);
                    rethrow = e;
                    tm.setRollbackOnly();
                }
                suspendTransaction();
            } else if (coord.isSubordinateCoordinator()) {
                if (logger.isLogging(Level.FINER)) {
                    logger.finer(METHOD_NAME, "importing ws-at activity id:" + coordTxnCtx.getIdentifier() +   
                                              " from external WS-AT coordinator");
                }
                importedTxn = true;
                beginImportTransaction(coordTxnCtx, coord);
                try {
                    responsePkt = next.process(pkt);
                } catch (Exception e) {
                    logger.warning(METHOD_NAME,
                                   LocalizationMessages.HANDLE_EXCEPTION_TO_RELEASE_IMPORTED_TXN_5013(coordTxnCtx.getIdentifier().toString(),
                                                                                                 jtaTxn.toString()), 
                                   e);
                    rethrow = e;
                    tm.setRollbackOnly();
                }
                endImportTransaction(coordTxnCtx);
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
            } catch (Exception e) {
                rethrow = e;
                logger.warning(METHOD_NAME, 
                               LocalizationMessages.HANDLE_EXCEPTION_TO_COMMIT_CREATED_TXN_5015(), e);
                tm.setRollbackOnly();
            }
            if (isServlet(pkt)) {
                 commitTransaction();
            } // else allow Java EE EJB container to finish transaction context
              // for Sun Application Server:  see BaseContainer.postInvokeTx
        } else { 
            responsePkt = next.process(pkt);
        }
        
        // Postcondition check
        assertNoCurrentTransaction(
                LocalizationMessages.INVALID_JTA_TRANSACTION_RETURNING_5003(bindingName, 
                                                                            msgOperation.toString()));
        if (rethrow != null) {
            String exMsg = LocalizationMessages.WSTXRETHROW_5014(bindingName, 
                                                                 msgOperation.toString());
            throw new WebServiceException(exMsg, rethrow);
        }
        
        logger.exiting(METHOD_NAME, responsePkt);
        return responsePkt;
    }
    
    private Xid activeImportedXid = null;

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
     * Import a transactional context from an external transaction manager via WS-AT Coordination Context
     * that was propagated in a SOAP request message.
     *
     * @see #endImportTransaction(CoordinationContextInterface)
     */
    private void beginImportTransaction(CoordinationContextInterface CC, ATCoordinator coordinator) {
        assert activeImportedXid == null;

        Transaction currentTxn;
        activeImportedXid = CoordinationXid.lookupOrCreate(CC.getIdentifier());
        try {    
            ((TransactionImport) tm).recreate(activeImportedXid, CC.getExpires());
        } catch (IllegalStateException ex) {
            String message = LocalizationMessages.IMPORT_TRANSACTION_FAILED_5009(CC.getIdentifier().toString(),
                                                                                 activeImportedXid.toString());
            logger.warning("beginImportTransaction", message, ex);
            throw new WebServiceException(message, ex);
        } 
        try{  
            currentTxn = tm.getTransaction();
        } catch (SystemException ex) {
            String message = LocalizationMessages.IMPORT_TXN_GET_TXN_FAILED_5010(CC.getIdentifier().toString());
            logger.warning("beginImportTransaction", message, ex);
            throw new WebServiceException(message, ex);
        }     
        assert currentTxn != null;
        coordinator.setTransaction(currentTxn);
        tm.setCoordinationContext(CC);
    }

    /**
     * Ends the importing of an external transaction.
     * <p/>
     * <p> Post-condition: terminates beginImportTransaction.
     *
     * @param CC coordination context of imported transaction.
     * @see #beginImportTransaction(com.sun.xml.ws.tx.coordinator.CoordinationContextInterface, com.sun.xml.ws.tx.at.ATCoordinator)
     */
    private void endImportTransaction(CoordinationContextInterface CC) {
        assert activeImportedXid != null;

        try {
            ((TransactionImport) tm).release(activeImportedXid);
        } catch (Error e) {
            logger.warning("endImportTransaction",
                    LocalizationMessages.EXCEPTION_RELEASING_IMPORTED_TRANSACTION_5007(), e);
        }
        activeImportedXid = null;
        ATCoordinator coord = (ATCoordinator) CoordinationManager.getInstance().getCoordinator(CC.getIdentifier());
        coord.setTransaction(null);
    }

    /**
     * wsat:ATAssertion representations 
     */
    enum ATAssertion {
        NOT_ALLOWED, // Absence of <wsat:ATAssertion/>
        ALLOWED,     // <wsat:ATAssertion wsp:Optional="true"/> 
        MANDATORY    // <wsat:ATAssertion/>
    }

    /**
     * Representation of all WS-AT policy assertions: ATAssertion and ATAlwaysCapability.
     */
    static class OperationATPolicy {
        ATAssertion atAssertion = ATAssertion.NOT_ALLOWED;
        boolean ATAlwaysCapability = false;
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
            WSDLOperation op = bindingOp.getOperation();
            PolicyMapKey opKey = pmap.createWsdlMessageScopeKey(port.getOwner().getName(), port.getName(), bindingOp.getName());
            Policy effectivePolicy = null;
            try {
                effectivePolicy = pmap.getOperationEffectivePolicy(opKey);
            } catch (PolicyException ex) {
                logger.warning("cacheOperationToPolicyMappings", 
                        LocalizationMessages.AT_POLICY_ASSERTION_PROCESSING_FAILED_5001(binding.getName().toString(),
                                                                                   bindingOp.getName().toString()), 
                        ex);
            }

            if (effectivePolicy != null) {
                OperationATPolicy opat = new OperationATPolicy();
                Iterator<AssertionSet> iter = effectivePolicy.iterator();

                // only one set of assertions for WS-AT Policy Assertions.
                if (iter.hasNext()) {
                    AssertionSet wsatAssertionSet = iter.next();
                    for (PolicyAssertion wsatpa : wsatAssertionSet) {

                        // Check for 2004 WS-Atomic Transaction Policy Assertions
                        if (wsatpa.getName().equals(AT_ASSERTION)) {
                            opat.atAssertion = (wsatpa.isOptional() ?
                                    ATAssertion.ALLOWED : ATAssertion.MANDATORY);
                        } else if (wsatpa.getName().equals(AT_ALWAYS_CAPABILITY)) {
                            opat.ATAlwaysCapability = true;
                        }
                        
                        // TODO: To implement OASIS WS-Atomic Transaction,
                        // check for OASIS WS-Atomic Transaction Policy Assertion ATAssertion here
                    }
                }
                if (logger.isLogging(Level.FINE)) {
                    logger.fine("cacheOperationToPolicyMappings", "Operation: " +
                                 binding.getName() + ":" +
                                 bindingOp.getName() + " WS-AT Policy Assertions: ATAssertion:" + opat.atAssertion +
                                         " ATAlwaysCapability:" + opat.ATAlwaysCapability);
               }
               opPolicyCache.put(bindingOp.getName(), opat);
            }
        }
    }
    
    private void resumeTransaction(Transaction txn) throws WebServiceException {
        try {
            tm.resume(txn);
        } catch (Exception ex) {
            String handlerMsg = LocalizationMessages.TXN_MGR_RESUME_FAILED_5016(txn.toString());
            logger.severe("resumeTransaction", handlerMsg, ex);
            throw new WebServiceException(handlerMsg, ex);
        }
    }
    
    private void suspendTransaction() {
        try {
            tm.suspend();
        } catch (SystemException ex) {
            String handlerMsg = LocalizationMessages.TXN_MGR_OPERATION_FAILED_5011("suspend");
            logger.warning("suspendTransaction", handlerMsg, ex);
        }
    }

    /**
     * Create a transaction context for pipe to be processed within.
     */
    private void beginTransaction() {
        try {
           tm.getUserTransaction().begin();
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
            tm.getUserTransaction().commit();
        } catch (Exception ex) {
            String commitExceptionMsg = LocalizationMessages.EXCEPTION_DURING_COMMIT_5008();
            logger.warning("commitTransaction", commitExceptionMsg, ex);
            throw new WebServiceException(commitExceptionMsg, ex);
        }
    }
    
    private void assertNoCurrentTransaction(String message) {
        Transaction txn = null;
        try {
            txn = tm.getTransaction();
        } catch (SystemException ex) {
            String handlerMsg = LocalizationMessages.TXN_MGR_OPERATION_FAILED_5011("getTransaction");
            logger.warning("assertNoCurrentTransaction", handlerMsg);
            throw new WebServiceException(handlerMsg, ex);
        }     
        if (txn != null) {
            logger.severe("TxServerPipe.process", message + " " + txn.toString());
            try {
                tm.suspend();
            } catch (SystemException ex) {
                String handlerMsg = LocalizationMessages.TXN_MGR_OPERATION_FAILED_5011("suspend");
                logger.warning("assertNoCurrentTransaction", handlerMsg);
                throw new WebServiceException(handlerMsg, ex);
            }
        } 
    }
}

