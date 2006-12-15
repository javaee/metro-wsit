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
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;
import com.sun.xml.ws.tx.coordinator.CoordinationManager;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
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
<<<<<<< TxServerPipe.java
 * @version $Revision: 1.6 $
=======
 * @version $Revision: 1.6 $
>>>>>>> 1.5
 * @since 1.0
 */
// suppress known deprecation warnings about using pipes.
@SuppressWarnings("deprecation")
public class TxServerPipe implements Pipe {

    static private TxLogger logger = TxLogger.getLogger(TxServerPipe.class);
    static private TransactionManagerImpl tm = TransactionManagerImpl.getInstance();

    // In future there can be multiple WS-AT namespaces: 2004 member submission, OASIS WS-TX
    final static private List<String> WSAT_NS_LST = Arrays.asList(WSAT_SOAP_NSURI);

    // Might need CoordinationContext for OASIS and 2004 input submission.
    // They will have different target namespaces.
    // might need additional classes due to extensibility of CoordinationContext (anyAttribute, any element)
    private Unmarshaller unmarshaller = null;
    private WSDLPort port = null;
    final Pipe next;


    /**
     * @param port WSDL port for this pipe
     * @param map  PolicyMap
     * @param next Next pipe to be executed.
     */
    public TxServerPipe(WSDLPort port,
                        PolicyMap map,
                        Pipe next) {
        unmarshaller = TxJAXBContext.createUnmarshaller();
        this.port = port;
        try {
            cacheOperationToPolicyMappings(map, port.getBinding());
        } catch (PolicyException e) {
            throw new WebServiceException(
                    LocalizationMessages.AT_POLICY_ASSERTION_PROCESSING_FAILED(e.getLocalizedMessage(),
                            port.getAddress().getURI(),
                            port.getBinding().getName().toString()));
        }
        this.next = next;
    }

    private TxServerPipe(TxServerPipe from, PipeCloner cloner) {
        cloner.add(from, this);
        this.next = cloner.copy(from.next);
        this.port = from.port;
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
     * Process WS-AT transactional context in incoming message.
     * <p/>
     * <p/>
     * Transactional context processing is driven by ws-at policy assertions associated with
     * wsdl:binding/wsdl:operation of parameter <code>pkt</code>.
     *
     * @param pkt a packet with JAX-WS properties that has an incoming request message
     * @return processing of pkt by next pipe in pipeline
     */
    public Packet process(Packet pkt) {
         if (logger.isLogging(Level.FINEST)) {
            logger.finest("process(Packet)", "Enter TxServerPipe.process(pkt=" + pkt.toString() + " Thread: " +
                    "name:" + Thread.currentThread().getName() + " thread instance:" + Thread.currentThread().toString());
         }
        
        // Precondition check
        assertNoCurrentTransaction("JTA Transaction MUST not exist at start of TxServerPipe.process.");
        
        com.sun.xml.ws.tx.common.Message msg = new Message(pkt.getMessage());
        WSDLBoundOperation msgOp = msg.getOperation(port);
        QName msgOperation = msgOp.getName();
        OperationATPolicy opat = getATPolicy(msgOperation);
        CoordinationContextInterface CC = null;
        
        try {
            CC = msg.getCoordinationContext(unmarshaller);
        } catch (JAXBException je) {
            if (opat.atAssertion == ATAssertion.REQUIRED || opat.atAssertion == ATAssertion.ALLOWED) {
                throw new WebServiceException(je.getLocalizedMessage());
            } else {
                logger.warning("TxServerPipe", "failed to unmarshall unexpected CoordinationContext with JAXBException " + je.getLocalizedMessage());
            }
        }
        
        
        // verify coordination type protocol
        if (CC != null) {
            if (CC.getCoordinationType().equals(WSAT_2004_PROTOCOL)) {
                //  let jax-ws runtime know that coordination context header was understood.
                if (logger.isLogging(Level.FINEST)) {
                    
                    logger.finest("process(Packet)", "Processing wscoor:CoordinationContext for protocol " + CC.getCoordinationType() + "  in " + msgOp.getName().toString());
                }
                msg.setCoordCtxUnderstood();
            } // else check for other supported protocols in future.
            else {  // unrecognized ws coordination protocol type
                // Another pipe *may* process CoordinationContext with this unknown protocol type.  log for now.
                if (logger.isLogging(Level.WARNING)) {
                    logger.warning("TxServerPipe.process(Packet)", "ignoring unrecognized CoordinationContext protocol:" + CC.getCoordinationType());
                }
                CC = null;
            }
        }

        // wsat:ATAssertion policy assertion check.  Note 2004 WS-AT does not require this.
        if (opat.atAssertion == ATAssertion.REQUIRED && CC == null) {
            throw new WebServiceException(
                    LocalizationMessages.MUST_FLOW_WSAT_COORDINATION_CONTEXT(msgOperation.toString()));
        }

        /*
         * Absence of a ws-at atassertion does not forbid WS-AT CoordinationContext from flowing.
         * OASIS WS-TX refers to this case as no claims made.
         */
        if (CC != null && opat.atAssertion == ATAssertion.NOT_ALLOWED) {
            // Not an error just log this is occuring.
            logger.warning("TxServerPipe", "atomic transaction flowed with operation request that was not annotated with wsat:ATAssertion : " +
                    msgOperation.toString());
        }

        boolean importedTxn = false;
        Packet responsePkt = null;
        Transaction txn = null;
        Exception rethrow = null;
        ATCoordinator coord = null;

        if (CC != null) {
            coord = (ATCoordinator) CoordinationManager.getInstance().lookupOrCreateCoordinator(CC);
            assert coord != null;

            txn = coord.getTransaction();
            if (txn != null) {
                if (logger.isLogging(Level.FINEST)) {
                    logger.finest("TxServerPipe", "JTA transaction flowed within same app server instance, resume the JTA transaction.");
                }
                boolean performSuspend = true;
                try {
                    tm.resume(txn);
                } catch (IllegalStateException e) {
                    // ignore.  transaction context already setup
                    performSuspend = false;
                } catch (Exception ex) {
                    throw new WebServiceException(ex.getMessage(), ex);
                }
                try {
                    responsePkt = next.process(pkt);
                } catch (Exception e) {
                    rethrow = e;
                }
                if (performSuspend) {
                    try {
                        tm.suspend();
                    } catch (SystemException ex) {
                        //ignore
                    }
                }
            } else if (coord.isSubordinateCoordinator()) {
                if (logger.isLogging(Level.FINEST)) {
                    logger.finest("TxServerPipe", "importing ws-at activity id:" + CC.getIdentifier() +   
                                  " from external WS-AT coordinator");
                }
                importedTxn = true;
                beginImportTransaction(CC, coord);
                try {
                    responsePkt = next.process(pkt);
                } catch (Exception e) {
                    rethrow = e;
                    tm.setRollbackOnly();
                }
                endImportTransaction(CC);
            } else {
                // just in case first two cases are not met
                responsePkt = next.process(pkt);
            }
        } else if (opat.ATAlwaysCapability == true) {
             if (logger.isLogging(Level.FINEST)) {
                    logger.finest("TxServerPipe", "create JTA Transaction, no CoordinationContext flowed with operation and wsat:ATAlwaysCapability is enabled");
                }
            // no Transaction context flowed with message but WS-AT policy assertion requests auto creation of txn 
            // context on server side invocation of method.
            beginTransaction();
            try {
                responsePkt = next.process(pkt);
            } catch (Exception e) {
                rethrow = e;
                tm.setRollbackOnly();
            }
            commitTransaction();
        } else {  // just in case first two cases are not met
            responsePkt = next.process(pkt);
        }
        // Postcondition check
        assertNoCurrentTransaction("JTA Transaction MUST not exist when returning from TxServerPipe.process ");
        
        if (rethrow != null) {
            if (logger.isLogging(Level.FINE)) {
                logger.fine("TxServerPipe", "rethrowing caught exception " + rethrow.getMessage());
            }    
            throw new WebServiceException(rethrow.getMessage(), rethrow);
        }
        if (logger.isLogging(Level.FINEST)) {
            logger.finest("process(Packet)", "Exit TxServerPipe.process(pkt=" + pkt.toString() + " Thread: " +
                    "name:" + Thread.currentThread().getName() + " thread instance:" + Thread.currentThread().toString());
         }
        return responsePkt;
    }
    private Xid activeImportedXid = null;

    /**
     * Import a transactional context from an external transaction manager via WS-AT Coordination Context.
     *
     * @see #endImportTransaction(CoordinationContextInterface)
     */
    private void beginImportTransaction(CoordinationContextInterface CC, ATCoordinator coordinator) {
        assert activeImportedXid == null;

        // TODO:
        // JTS recovery must process WS-AT SubordinateCoordinator.
        // So  WS-AT SubordinateCoordinator mustbe registered as coordinator resource with JTS.

        Transaction currentTxn;
        activeImportedXid = CoordinationXid.lookupOrCreate(CC.getIdentifier());
        try {    
            ((TransactionImport) tm).recreate(activeImportedXid, CC.getExpires());
        } catch (IllegalStateException ex) {
            logger.warning("beginImportTransaction", "beginImportTransaction failed. caught exception " + ex.getLocalizedMessage(), ex);
            throw new WebServiceException(ex.getMessage(), ex);
        } 
        try{  
            currentTxn = tm.getTransaction();
        } catch (SystemException ex) {
            logger.warning("beginImportTransaction", "getTransaction() failed. caught exception " + ex.getLocalizedMessage(), ex);
            throw new WebServiceException(ex.getMessage(), ex);
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

        Xid importXid = CoordinationXid.get(CC.getIdentifier());
        try {
            ((TransactionImport) tm).release(activeImportedXid);
        } catch (Error e) {
            logger.warning("endImportTransaction", "ignoring exception raised while releasing imported transaction", e);
        }
        activeImportedXid = null;
        ATCoordinator coord = (ATCoordinator) CoordinationManager.getInstance().getCoordinator(CC.getIdentifier());
        coord.setTransaction(null);
    }

    enum ATAssertion {
        NOT_ALLOWED, ALLOWED, REQUIRED
    }

    static class OperationATPolicy {
        ATAssertion atAssertion = ATAssertion.NOT_ALLOWED;
        boolean ATAlwaysCapability = false;
    }

    /**
     * Cache of operation name to its WS-AT policies computed in constructor.
     * An operation is not inserted into cache if its WS-AT policies are the default values.
     */
    /* this is a space/time tradeoff.  Saves time in process call by taking up
       more memory for pipe.  Makes sense for server-side, probably not for client-side*/
    private Map<QName, OperationATPolicy> opPolicyCache = new HashMap<QName, OperationATPolicy>();

    static OperationATPolicy DEFAULT = null;

    OperationATPolicy getDefaultATPolicy() {
        if (DEFAULT == null) {
            DEFAULT = new OperationATPolicy();
        }
        return DEFAULT;
    }

    // Does not handle overloaded operationName.
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
    private void cacheOperationToPolicyMappings(PolicyMap pmap, WSDLBoundPortType binding) throws PolicyException {

        // Cache wsat policy for each wsdl:binding/wsdl:operation for binding
        for (WSDLBoundOperation bindingOp : binding.getBindingOperations()) {
            WSDLOperation op = bindingOp.getOperation();
            PolicyMapKey opKey = pmap.createWsdlMessageScopeKey(port.getOwner().getName(), port.getName(), bindingOp.getName());
            Policy effectivePolicy =
                    pmap.getOperationEffectivePolicy(opKey);

            if (effectivePolicy != null) {
                OperationATPolicy opat = new OperationATPolicy();
                Iterator<AssertionSet> iter = effectivePolicy.iterator();

                // only one set of assertions for WS-AT Policy Assertions.
                if (iter.hasNext()) {
                    AssertionSet wsatAssertionSet = iter.next();
                    for (PolicyAssertion wsatpa : wsatAssertionSet) {

                        // Code below needs to be updated to not use wsat namespace
                        // in comparison when need to support xmlsoap 2004 and OASIS WS-TX ns
                        if (wsatpa.getName().equals(AT_ASSERTION)) {
                            opat.atAssertion = (wsatpa.isOptional() ?
                                    ATAssertion.ALLOWED : ATAssertion.REQUIRED);
                        } else if (wsatpa.getName().equals(AT_ALWAYS_CAPABILITY)) {
                            opat.ATAlwaysCapability = true;
                        }
                    }
                }
                opPolicyCache.put(bindingOp.getName(), opat);
            }
        }
    }

    /**
     * Create a transaction context for pipe to be processed within.
     */
    private void beginTransaction() {
        try {
            tm.begin();
        } catch (NotSupportedException ex) {
            throw new WebServiceException(ex.getMessage(), ex);
        } catch (SystemException ex) {
            throw new WebServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * Complete transaction context for pipe to be processed within.
     */
    private void commitTransaction() {
        try {
            tm.commit();
        } catch (Exception ex) {
            logger.warning("commitTransaction", "commit failed with exception " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    private void assertNoCurrentTransaction(String message) {
        Transaction txn = null;
        try {
            txn = tm.getTransaction();
        } catch (SystemException ex) {
            ex.printStackTrace();
        }     
        if (txn != null) {
            logger.severe("TxServerPipe.process", message + " Unexpected JTA Txn: " + txn.toString());
            try {
                tm.suspend();
            } catch (SystemException ex) {
                ex.printStackTrace();
            }
        } 
    }
}

