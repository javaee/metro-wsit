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

package com.sun.xml.ws.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * A PolicyMap holds all policies for a scope.
 *
 * This map is modeled around WSDL 1.1 policy scopes according to WS-PolicyAttachment. The map holds an information about
 * every scope for service, endpoint, operation, and input/output/fault message. It also provide accessibility methods for
 * computing and obtaining effective policy on each scope.
 *
 * TODO: rename createWsdlMessageScopeKey to createWsdlInputOutputMessageScopeKey
 */
public final class PolicyMap implements Iterable<Policy> {
    static enum ScopeType {
        SERVICE,
        ENDPOINT,
        OPERATION,
        INPUT_MESSAGE,
        OUTPUT_MESSAGE,
        FAULT_MESSAGE
    }
    
    private static final class ScopeMap implements Iterable<Policy> {
        private Map<PolicyMapKey, PolicyScope> internalMap = new HashMap<PolicyMapKey, PolicyScope>();
        private PolicyMapKeyHandler scopeKeyHandler;
        private PolicyMerger merger;
        
        ScopeMap(PolicyMerger merger, PolicyMapKeyHandler scopeKeyHandler) {
            this.merger = merger;
            this.scopeKeyHandler = scopeKeyHandler;
        }
        
        Policy getEffectivePolicy(PolicyMapKey key, Collection<String> namespaces) throws PolicyException {
            PolicyScope scope = internalMap.get(createLocalCopy(key));
            return (scope != null) ? scope.getEffectivePolicy(namespaces, merger) : null;
        }
        
        Policy getEffectivePolicy(PolicyMapKey key) throws PolicyException {
            PolicyScope scope = internalMap.get(createLocalCopy(key));
            return (scope != null) ? scope.getEffectivePolicy(merger) : null;
        }
        
        void putSubject(PolicyMapKey key, PolicySubject subject) {
            PolicyMapKey localKey = createLocalCopy(key);
            PolicyScope scope = internalMap.get(localKey);
            if (scope == null) {
                List<PolicySubject> list = new LinkedList<PolicySubject>();
                list.add(subject);
                internalMap.put(localKey, new PolicyScope(list));
            } else {
                scope.attach(subject);
            }
        }
        
        void setNewEffectivePolicy(PolicyMapKey key, Policy newEffectivePolicy) {
            // we add this policy map as a subject, because there is nothing reasonable we could add there, since
            // this is an artificial policy subject
            PolicySubject subject = new PolicySubject(key, newEffectivePolicy);
            
            PolicyMapKey localKey = createLocalCopy(key);
            PolicyScope scope = internalMap.get(localKey);
            if (scope == null) {
                List<PolicySubject> list = new LinkedList<PolicySubject>();
                list.add(subject);
                internalMap.put(localKey, new PolicyScope(list));
            } else {
                scope.dettachAllSubjects();
                scope.attach(subject);
            }
        }
        
        Collection<PolicyScope> getStoredScopes() {
            return internalMap.values();
        }
        
        Set<PolicyMapKey> getAllKeys() {
            return internalMap.keySet();
        }
        
        private PolicyMapKey createLocalCopy(PolicyMapKey key) {
            if (key == null) {
                throw new NullPointerException("Provided policy map key must not be null! Create a proper policy map key by calling one of PolicyMap's  createXxxScopeKey(...) methods first.");
            }
            
            PolicyMapKey localKeyCopy = new PolicyMapKey(key);
            localKeyCopy.setHandler(scopeKeyHandler);
            
            return localKeyCopy;
        }
        
        public Iterator<Policy> iterator() {
            return new Iterator<Policy> () {
                private final Iterator<PolicyMapKey> keysIterator = internalMap.keySet().iterator();
                
                public boolean hasNext() {
                    return keysIterator.hasNext();
                }
                
                public Policy next() {
                    PolicyMapKey key = keysIterator.next();
                    try {
                        return getEffectivePolicy(key);
                    } catch (PolicyException e) {
                        throw new java.lang.IllegalStateException("Exception occured while retrieving effective policy for given key [" + key + "]", e);
                    }
                }
                
                public void remove() {
                    throw new UnsupportedOperationException("Remove operation not supported by this iterator.");
                }
            };
        }
        
        public boolean isEmpty() {
            return internalMap.isEmpty();
        }
        
        public String toString() {
            return internalMap.toString();
        }
    }
    
    private static final PolicyMerger merger = PolicyMerger.getMerger();
    
    private ScopeMap serviceMap = new ScopeMap(merger, new PolicyMapKeyHandler() {
        public boolean areEqual(PolicyMapKey key1, PolicyMapKey key2) {
            return key1.service.equals(key2.service);
        }
        
        public int generateHashCode(PolicyMapKey key) {
            int result = 17;
            
            result = 37 * result + key.service.hashCode();
            
            return result;
        }
    });
    
    private ScopeMap endpointMap = new ScopeMap(merger, new PolicyMapKeyHandler() {
        public boolean areEqual(PolicyMapKey key1, PolicyMapKey key2) {
            boolean retVal = true;
            
            retVal = retVal && key1.service.equals(key2.service);
            retVal = retVal && ((key1.port != null) ? key1.port.equals(key2.port) : key2.port == null);
            
            return retVal;
        }
        
        public int generateHashCode(PolicyMapKey key) {
            int result = 17;
            
            result = 37 * result + key.service.hashCode();
            result = 37 * result + ((key.port != null) ? key.port.hashCode() : 0);
            
            return result;
        }
    });
    
    private PolicyMapKeyHandler operationAndInputOutputMessageKeyHandler = new PolicyMapKeyHandler() {
        // we use the same algorithm to handle operation and input/output message keys
        
        public boolean areEqual(PolicyMapKey key1, PolicyMapKey key2) {
            boolean retVal = true;
            
            retVal = retVal && key1.service.equals(key2.service);
            retVal = retVal && ((key1.port != null) ? key1.port.equals(key2.port) : key2.port == null);
            retVal = retVal && ((key1.operation != null) ? key1.operation.equals(key2.operation) : key2.operation == null);
            
            return retVal;
        }
        
        public int generateHashCode(PolicyMapKey key) {
            int result = 17;
            
            result = 37 * result + key.service.hashCode();
            result = 37 * result + ((key.port != null) ? key.port.hashCode() : 0);
            result = 37 * result + ((key.operation != null) ? key.operation.hashCode() : 0);
            
            return result;
        }
    };
    
    
    private ScopeMap operationMap = new ScopeMap(merger, operationAndInputOutputMessageKeyHandler);
    private ScopeMap inputMessageMap = new ScopeMap(merger, operationAndInputOutputMessageKeyHandler);
    private ScopeMap outputMessageMap = new ScopeMap(merger, operationAndInputOutputMessageKeyHandler);
    
    private ScopeMap faultMessageMap = new ScopeMap(merger, new PolicyMapKeyHandler() {
        public boolean areEqual(PolicyMapKey key1, PolicyMapKey key2) {
            boolean retVal = true;
            
            retVal = retVal && key1.service.equals(key2.service);
            retVal = retVal && ((key1.port != null) ? key1.port.equals(key2.port) : key2.port == null);
            retVal = retVal && ((key1.operation != null) ? key1.operation.equals(key2.operation) : key2.operation == null);
            retVal = retVal && ((key1.faultMessage != null) ? key1.faultMessage.equals(key2.faultMessage) : key2.faultMessage == null);
            
            return retVal;
        }
        
        public int generateHashCode(PolicyMapKey key) {
            int result = 17;
            
            result = 37 * result + key.service.hashCode();
            result = 37 * result + ((key.port != null) ? key.port.hashCode() : 0);
            result = 37 * result + ((key.operation != null) ? key.operation.hashCode() : 0);
            result = 37 * result + ((key.faultMessage != null) ? key.faultMessage.hashCode() : 0);
            
            return result;
        }
    });
    
    private PolicyMap() {
    }
    
    /**
     * Creates new policy map instance and connects provided collection of policy map mutators to the created policy map.
     *
     * @param mutators collection of mutators that should be connected to the newly created map.
     * @return new policy map instance (mutable via provided collection of mutators).
     */
    public static PolicyMap createPolicyMap(Collection<? extends PolicyMapMutator> mutators) {
        PolicyMap result = new PolicyMap();
        
        if (mutators != null && !mutators.isEmpty()) {
            for (PolicyMapMutator mutator : mutators) {
                mutator.connect(result);
            }
        }
        
        return result;
    }
    
    public Policy getServiceEffectivePolicy(PolicyMapKey key) throws PolicyException {
        return serviceMap.getEffectivePolicy(key);
    }
    
    public Policy getEndpointEffectivePolicy(PolicyMapKey key) throws PolicyException {
        return endpointMap.getEffectivePolicy(key);
    }
    
    public Policy getOperationEffectivePolicy(PolicyMapKey key) throws PolicyException {
        return operationMap.getEffectivePolicy(key);
    }
    
    public Policy getInputMessageEffectivePolicy(PolicyMapKey key) throws PolicyException {
        return inputMessageMap.getEffectivePolicy(key);
    }
    
    public Policy getOutputMessageEffectivePolicy(PolicyMapKey key) throws PolicyException {
        return outputMessageMap.getEffectivePolicy(key);
    }
    
    public Policy getFaultMessageEffectivePolicy(PolicyMapKey key) throws PolicyException {
        return faultMessageMap.getEffectivePolicy(key);
    }
    
    public Policy getServiceEffectivePolicy(PolicyMapKey key, Collection<String> namespaces) throws PolicyException {
        return serviceMap.getEffectivePolicy(key, namespaces);
    }
    
    public Policy getEndpointEffectivePolicy(PolicyMapKey key, Collection<String> namespaces) throws PolicyException {
        return endpointMap.getEffectivePolicy(key, namespaces);
    }
    
    public Policy getOperationEffectivePolicy(PolicyMapKey key, Collection<String> namespaces) throws PolicyException {
        return operationMap.getEffectivePolicy(key, namespaces);
    }
    
    public Policy getInputMessageEffectivePolicy(PolicyMapKey key, Collection<String> namespaces) throws PolicyException {
        return inputMessageMap.getEffectivePolicy(key, namespaces);
    }
    
    public Policy getOutputMessageEffectivePolicy(PolicyMapKey key, Collection<String> namespaces) throws PolicyException {
        return outputMessageMap.getEffectivePolicy(key, namespaces);
    }
    
    public Policy getFaultMessageEffectivePolicy(PolicyMapKey key, Collection<String> namespaces) throws PolicyException {
        return faultMessageMap.getEffectivePolicy(key, namespaces);
    }
    
    /**
     * Returns all service scope keys stored in this policy map
     *
     * @return collection of service scope policy map keys stored in the map.
     */
    public Collection<PolicyMapKey> getAllServiceScopeKeys() {
        return serviceMap.getAllKeys();
    }
    
    /**
     * Returns all endpoint scope keys stored in this policy map
     *
     * @return collection of endpoint scope policy map keys stored in the map.
     */
    public Collection<PolicyMapKey> getAllEndpointScopeKeys() {
        return endpointMap.getAllKeys();
    }
    
    /**
     * Returns all operation scope keys stored in this policy map
     *
     * @return collection of operation scope policy map keys stored in the map.
     */
    public Collection<PolicyMapKey> getAllOperationScopeKeys() {
        return operationMap.getAllKeys();
    }
    
    /**
     * Returns all input message scope keys stored in this policy map
     *
     * @return collection of input message scope policy map keys stored in the map.
     */
    public Collection<PolicyMapKey> getAllInputMessageScopeKeys() {
        return inputMessageMap.getAllKeys();
    }
    
    /**
     * Returns all output message scope keys stored in this policy map
     *
     * @return collection of output message scope policy map keys stored in the map.
     */
    public Collection<PolicyMapKey> getAllOutputMessageScopeKeys() {
        return outputMessageMap.getAllKeys();
    }
    
    /**
     * Returns all fault message scope keys stored in this policy map
     *
     * @return collection of input message scope policy map keys stored in the map.
     */
    public Collection<PolicyMapKey> getAllFaultMessageScopeKeys() {
        return faultMessageMap.getAllKeys();
    }
    
    /**
     * Places new subject into policy map under the scope identified by it's type and policy map key.
     *
     * @param scopeType the type of the scope the subject belongs to
     * @param key a policy map key to be used to store the subject
     * @param subject actual policy subject to be stored in the policy map
     *
     * @throw IllegalArgumentException in case the scope type is not recognized.
     */
    void putSubject(ScopeType scopeType, PolicyMapKey key, PolicySubject subject) {
        switch (scopeType) {
            case SERVICE:
                serviceMap.putSubject(key, subject);
                break;
            case ENDPOINT:
                endpointMap.putSubject(key, subject);
                break;
            case OPERATION:
                operationMap.putSubject(key, subject);
                break;
            case INPUT_MESSAGE:
                inputMessageMap.putSubject(key, subject);
                break;
            case OUTPUT_MESSAGE:
                outputMessageMap.putSubject(key, subject);
                break;
            case FAULT_MESSAGE:
                faultMessageMap.putSubject(key, subject);
                break;
            default:
                throw new IllegalArgumentException("Unrecoginzed scope type: '" + scopeType + "'");
        }
    }
    
    /**
     * Replaces current effective policy on given scope (identified by a {@code key} parameter) with the new efective
     * policy provided as a second input parameter. If no policy was defined for the presented key, the new policy is simply
     * stored with the key.
     *
     * @param scopeType the type of the scope the subject belongs to. Must not be {@code null}.
     * @param key identifier of the scope the effective policy should be replaced with the new one. Must not be {@code null}.
     * @param newEffectivePolicy the new policy to replace the old effective policy of the scope. Must not be {@code null}.
     *
     * @throw NullPointerException in case any of the input parameters is {@code null}.
     * @throw IllegalArgumentException in case the scope type is not recognized.
     */
    void setNewEffectivePolicyForScope(ScopeType scopeType, PolicyMapKey key, Policy newEffectivePolicy) {
        if (scopeType == null || key == null || newEffectivePolicy == null) {
            throw new NullPointerException("Input parameters must not be 'null'");
        }
        
        switch (scopeType) {
            case SERVICE :
                serviceMap.setNewEffectivePolicy(key, newEffectivePolicy);
                break;
            case ENDPOINT :
                endpointMap.setNewEffectivePolicy(key, newEffectivePolicy);
                break;
            case OPERATION :
                operationMap.setNewEffectivePolicy(key, newEffectivePolicy);
                break;
            case INPUT_MESSAGE :
                inputMessageMap.setNewEffectivePolicy(key, newEffectivePolicy);
                break;
            case OUTPUT_MESSAGE :
                outputMessageMap.setNewEffectivePolicy(key, newEffectivePolicy);
                break;
            case FAULT_MESSAGE :
                faultMessageMap.setNewEffectivePolicy(key, newEffectivePolicy);
                break;
            default:
                throw new IllegalArgumentException("Unrecoginzed scope type: '" + scopeType + "'");
        }
    }
    
    /**
     * Returns all policy subjects contained by this map.
     *
     * @return All policy subjects contained by this map
     */
    public Collection<PolicySubject> getPolicySubjects() {
        List<PolicySubject> subjects = new LinkedList<PolicySubject>();
        addSubjects(subjects, serviceMap);
        addSubjects(subjects, endpointMap);
        addSubjects(subjects, operationMap);
        addSubjects(subjects, inputMessageMap);
        addSubjects(subjects, outputMessageMap);
        addSubjects(subjects, faultMessageMap);
        return subjects;
    }
    
    /*
     * TODO: reconsider this QUICK HACK FOR J1
     */
    public boolean isInputMessageSubject(PolicySubject subject) {
        for (PolicyScope scope : inputMessageMap.getStoredScopes()) {
            if (scope.getPolicySubjects().contains(subject)) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * TODO: reconsider this QUICK HACK FOR J1
     */
    public boolean isOutputMessageSubject(PolicySubject subject) {
        for (PolicyScope scope : outputMessageMap.getStoredScopes()) {
            if (scope.getPolicySubjects().contains(subject)) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Returns true if this map contains no key - policy pairs
     *
     * A null object key or policy constitutes a non-empty map.
     *
     * @return true if this map contains no key - policy pairs
     */
    public boolean isEmpty() {
        return serviceMap.isEmpty() && endpointMap.isEmpty() &&
                operationMap.isEmpty() && inputMessageMap.isEmpty() &&
                outputMessageMap.isEmpty() && faultMessageMap.isEmpty();
    }
    
    
    /**
     * Add all subjects in the given map to the collection
     *
     * @param subjects A collection that should hold subjects. The new subjects are added to the collection. Must not be {@code null}.
     * @param scopeMap A scope map that holds policy scopes. The subjects are retrieved from the scope objects.
     */
    private void addSubjects(Collection<PolicySubject> subjects, ScopeMap scopeMap) {
        for (PolicyScope scope : scopeMap.getStoredScopes()) {
            Collection<PolicySubject> scopedSubjects = scope.getPolicySubjects();
            subjects.addAll(scopedSubjects);
        }
    }
    
    /**
     * Creates a service policy scope <emph>locator</emph> object, that serves as a access key into
     * a {@code PolicyMap} where actual service policy scope for given service can be retrieved.
     *
     * @param service qualified name of the service. Must not be {@code null}.
     * @throws NullPointerException in case service, port or operation parameter is {@code null}.
     */
    public static PolicyMapKey createWsdlServiceScopeKey(QName service) throws NullPointerException {
        if (service == null) {
            throw new NullPointerException("Parameter must not be 'null': service = '" + service + "'");
        }
        return new PolicyMapKey(service, null, null);
    }
    
    /**
     * Creates an endpoint policy scope <emph>locator</emph> object, that serves as a access key into
     * a {@code PolicyMap} where actual endpoint policy scope for given endpoint can be retrieved.
     *
     * @param service qualified name of the service. Must not be {@code null}.
     * @param port qualified name of the endpoint. Must not be {@code null}.
     * @throws NullPointerException in case service, port or operation parameter is {@code null}.
     */
    public static PolicyMapKey createWsdlEndpointScopeKey(QName service, QName port) throws NullPointerException {
        if (service == null || port == null) {
            throw new NullPointerException("Parameters must not be 'null': service='" + service + "', port='" + port + "'");
        }
        return new PolicyMapKey(service, port, null);
    }
    
    /**
     * Creates an operation policy scope <emph>locator</emph> object, that serves as a access key into
     * a {@code PolicyMap} where actual operation policy scope for given bound operation can be retrieved.
     *
     * @param service qualified name of the service. Must not be {@code null}.
     * @param port qualified name of the endpoint. Must not be {@code null}.
     * @param operation qualified name of the operation. Must not be {@code null}.
     * @throws NullPointerException in case service, port or operation parameter is {@code null}.
     */
    public static PolicyMapKey createWsdlOperationScopeKey(QName service, QName port, QName operation) throws NullPointerException {
        return createOperationOrInputOutputMessageKey(service, port, operation);
    }
    
    /**
     * Creates an input/output message policy scope <emph>locator</emph> object identified by a bound operation, that serves as a
     * access key into {@code PolicyMap} where actual input/output message policy scope for given input message of a bound operation
     * can be retrieved.
     * <p/>
     * The method returns a key that is compliant with <emph>WSDL 1.1 Basic Profile Specification</emph>, according to which there
     * should be no two operations with the same name in a single port type definition.
     *
     * @param service qualified name of the service. Must not be {@code null}.
     * @param port qualified name of the endpoint. Must not be {@code null}.
     * @param operation qualified name of the operation. Must not be {@code null}.
     * @throws NullPointerException in case service, port or operation parameter is {@code null}.
     *
     */
    public static PolicyMapKey createWsdlMessageScopeKey(QName service, QName port, QName operation) throws NullPointerException {
        return createOperationOrInputOutputMessageKey(service, port, operation);
    }
    
    /**
     * Creates an fault message policy scope <emph>locator</emph> object identified by a bound operation, that serves as a
     * access key into {@code PolicyMap} where actual fault message policy scope for given input message of a bound operation
     * can be retrieved.
     * <p/>
     * The method returns a key that is compliant with <emph>WSDL 1.1 Basic Profile Specification</emph>, according to which there
     * should be no two operations with the same name in a single port type definition.
     *
     * @param service qualified name of the service. Must not be {@code null}.
     * @param port qualified name of the endpoint. Must not be {@code null}.
     * @param operation qualified name of the operation. Must not be {@code null}.
     * @param faultMessage qualified name of the fault message. Must not be {@code null}.
     * @throws NullPointerException in case service, port or operation parameter is {@code null}.
     *
     */
    public static PolicyMapKey createWsdlFaultMessageScopeKey(QName service, QName port, QName operation, QName faultMessage) throws NullPointerException {
        if (service == null || port == null || operation == null || faultMessage == null) {
            throw new NullPointerException("Parameters must not be 'null': service='" + service + "', port='" + port + "', operation='" + operation + "', message='" + faultMessage + "'");
        }
        
        return new PolicyMapKey(service, port, operation, faultMessage);
    }
    
    private static PolicyMapKey createOperationOrInputOutputMessageKey(final QName service, final QName port, final QName operation) {
        if (service == null || port == null || operation == null) {
            throw new NullPointerException("Parameters must not be 'null': service='" + service + "', port='" + port + "', operation='" + operation + "'");
        }
        
        return new PolicyMapKey(service, port, operation);
    }
    
    public String toString(){
        // TODO
        StringBuffer result = new StringBuffer();
        if(null!=this.serviceMap) {
            result.append("\nServiceMap=").append(this.serviceMap);
        }
        if(null!=this.endpointMap) {
            result.append("\nEndpointMap=").append(this.endpointMap);
        }
        if(null!=this.operationMap) {
            result.append("\nOperationMap=").append(this.operationMap);
        }
        if(null!=this.inputMessageMap) {
            result.append("\nInputMessageMap=").append(this.inputMessageMap);
        }
        if(null!=this.outputMessageMap) {
            result.append("\nOutputMessageMap=").append(this.outputMessageMap);
        }
        if(null!=this.faultMessageMap) {
            result.append("\nFaultMessageMap=").append(this.faultMessageMap);
        }
        return result.toString();
    }
    
    public Iterator<Policy> iterator() {
        return new Iterator<Policy> () {
            private final Iterator<Iterator<Policy>> mainIterator;
            private Iterator<Policy> currentScopeIterator;
            
            { // instance initialization
                Collection<Iterator<Policy>> scopeIterators = new ArrayList<Iterator<Policy>>(6);
                scopeIterators.add(serviceMap.iterator());
                scopeIterators.add(endpointMap.iterator());
                scopeIterators.add(operationMap.iterator());
                scopeIterators.add(inputMessageMap.iterator());
                scopeIterators.add(outputMessageMap.iterator());
                scopeIterators.add(faultMessageMap.iterator());
                
                mainIterator = scopeIterators.iterator();
                currentScopeIterator = mainIterator.next();
            }
            
            public boolean hasNext() {
                while (!currentScopeIterator.hasNext()) {
                    if (mainIterator.hasNext()) {
                        currentScopeIterator = mainIterator.next();
                    } else {
                        return false;
                    }
                }
                
                return true;
            }
            
            public Policy next() {
                if (hasNext()) {
                    return currentScopeIterator.next();
                }
                
                throw new NoSuchElementException("There are no more elements in the policy map.");
            }
            
            public void remove() {
                throw new UnsupportedOperationException("Remove operation not supported by this iterator.");
            }
        };
    }
    
}
