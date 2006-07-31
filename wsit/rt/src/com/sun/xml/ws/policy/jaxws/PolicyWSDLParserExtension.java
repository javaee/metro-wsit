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
package com.sun.xml.ws.policy.jaxws;

import com.sun.xml.ws.api.model.wsdl.WSDLObject;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLInput;
import com.sun.xml.ws.api.model.wsdl.WSDLMessage;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLOutput;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.EffectivePolicyModifier;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicyMapMutator;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelUnmarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModelContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.xml.stream.XMLInputFactory;

/**
 *
 * @author japod
 */
public class PolicyWSDLParserExtension extends WSDLParserExtension {
    
    enum HandlerType {
        PolicyUri, AnonymousPolicyId
    }
    
    class PolicyRecordHandler {
        String handler;
        HandlerType type;
        
        PolicyRecordHandler(HandlerType type, String handler) {
            this.type = type;
            this.handler = handler;
        }
        
        HandlerType getType() {
            return type;
        }
        
        String getHandler() {
            return handler;
        }
    }
    
    class PolicyRecord {
        PolicyRecord next;
        String uri;
        PolicySourceModel policyModel;
        Set<String> unresolvedURIs;
        
        PolicyRecord() {
        }
        
        PolicyRecord insert(PolicyRecord insertedRec) {
            if (null==insertedRec.unresolvedURIs || insertedRec.unresolvedURIs.isEmpty()) {
                insertedRec.next = this;
                return insertedRec;
            }
            PolicyRecord head = this;
            PolicyRecord oneBeforeCurrent = null;
            PolicyRecord current;
            for (current = head; null!=current.next; ) {
                if ((null != current.unresolvedURIs) && current.unresolvedURIs.contains(insertedRec.uri)) {
                    if (null == oneBeforeCurrent) {
                        insertedRec.next = current;
                        return insertedRec;
                    } else { // oneBeforeCurrent != null
                        oneBeforeCurrent.next = insertedRec;
                        insertedRec.next = current;
                        return head;
                    } // end-if-else oneBeforeCurrent == null
                }// end-if current record depends on inserted one
                if (insertedRec.unresolvedURIs.remove(current.uri)) {
                    if (insertedRec.unresolvedURIs.isEmpty()) {
                        insertedRec.next = current.next;
                        current.next = insertedRec;
                        return head;
                    } // end-if unresolvedURIs empty
                } // end-if one of unresolved URIs resolved by current record
                current=current.next;
            } // end for (current = head; null!=current.next; )
            insertedRec.next = null;
            current.next = insertedRec;
            return head;
        }
        
        public String toString() {
            String result = uri;
            if (null!=next) {
                result += "->" + next.toString();
            }
            return result;
        }
    }
    
    private static final PolicyLogger logger = PolicyLogger.getLogger(PolicyWSDLParserExtension.class);
    
    //anonymous policy id prefix
    private static final StringBuffer AnonymnousPolicyIdPrefix = new StringBuffer("#__anonymousPolicy__ID");
    
    // anonymous policies count
    private int anonymousPoliciesCount;
    
    // policy queue -- needed for evaluating the right order policy of policy models expansion
    private PolicyRecord expandQueueHead = null;
    
    // storage for policy models with an id passed by
    private Map<String,PolicyRecord> policyRecordsPassedBy = null;
    // storage for anonymous policies defined within given WSDL
    private Map<String,PolicySourceModel> anonymousPolicyModels = null;
    
    // container for URIs of policies referenced
    private List<String> unresolvedUris = null;
    
    // structures for policies really needed to build a map
    private LinkedList<String> urisNeeded = new LinkedList<String>();
    private Map<String, PolicySourceModel> modelsNeeded = new HashMap<String, PolicySourceModel>();
    
    // lookup tables for Policy attachments found
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4ServiceMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4PortMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4PortTypeMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4BindingMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4BoundOperationMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4OperationMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4MessageMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4InputMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4OutputMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4FaultMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4BindingInputOpMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4BindingOutputOpMap = null;
    private Map<WSDLObject, Collection<PolicyRecordHandler>> handlers4BindingFaultOpMap = null;
    
    private PolicyMapBuilder policyBuilder = new PolicyMapBuilder();
    
    private boolean isPolicyProcessed(String policyUri) {
        return modelsNeeded.containsKey(policyUri);
    }
    
    private void addNewPolicyNeeded(String policyUri, PolicySourceModel policyModel) {
        if (!modelsNeeded.containsKey(policyUri)) {
            modelsNeeded.put(policyUri, policyModel);
            urisNeeded.addFirst(policyUri);
        }
    }
    
    private Map<String, PolicySourceModel> getPolicyModels() {
        return modelsNeeded;
    }
    
    private Map<String,PolicyRecord> getPolicyRecordsPassedBy() {
        if (null==policyRecordsPassedBy) {
            policyRecordsPassedBy = new HashMap<String,PolicyRecord>();
        }
        return policyRecordsPassedBy;
    }
    
    private Map<String,PolicySourceModel> getAnonymousPolicyModels() {
        if (null==anonymousPolicyModels) {
            anonymousPolicyModels = new HashMap<String,PolicySourceModel>();
        }
        return anonymousPolicyModels;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4ServiceMap() {
        if (null==handlers4ServiceMap) {
            handlers4ServiceMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4ServiceMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4PortMap() {
        if (null==handlers4PortMap) {
            handlers4PortMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4PortMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4PortTypeMap() {
        if (null==handlers4PortTypeMap) {
            handlers4PortTypeMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4PortTypeMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4BindingMap() {
        if (null==handlers4BindingMap) {
            handlers4BindingMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4BindingMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4OperationMap() {
        if (null==handlers4OperationMap) {
            handlers4OperationMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4OperationMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4BoundOperationMap() {
        if (null==handlers4BoundOperationMap) {
            handlers4BoundOperationMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4BoundOperationMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4MessageMap() {
        if (null==handlers4MessageMap) {
            handlers4MessageMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4MessageMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4InputMap() {
        if (null==handlers4InputMap) {
            handlers4InputMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4InputMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4OutputMap() {
        if (null==handlers4OutputMap) {
            handlers4OutputMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4OutputMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4FaultMap() {
        if (null==handlers4FaultMap) {
            handlers4FaultMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4FaultMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4BindingInputOpMap() {
        if (null==handlers4BindingInputOpMap) {
            handlers4BindingInputOpMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4BindingInputOpMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4BindingOutputOpMap() {
        if (null==handlers4BindingOutputOpMap) {
            handlers4BindingOutputOpMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4BindingOutputOpMap;
    }
    
    private Map<WSDLObject, Collection<PolicyRecordHandler>> getHandlers4BindingFaultOpMap() {
        if (null==handlers4BindingFaultOpMap) {
            handlers4BindingFaultOpMap = new HashMap<WSDLObject,Collection<PolicyRecordHandler>>();
        }
        return handlers4BindingFaultOpMap;
    }
    
    private List<String> getUnresolvedUris(boolean emptyListNeeded) {
        if ((null == unresolvedUris) || emptyListNeeded) {
            unresolvedUris = new LinkedList<String>();
        }
        return unresolvedUris;
    }
    
    
    
    private void policyRecToExpandQueue(PolicyRecord policyRec) {
        if (null==expandQueueHead) {
            expandQueueHead = policyRec;
        } else {
            expandQueueHead = expandQueueHead.insert(policyRec);
        }
    }
    
    
    /**
     * Creates a new instance of PolicyWSDLParserExtension
     */
    public PolicyWSDLParserExtension() {
    }
    
    private PolicyRecordHandler readSinglePolicy(PolicyRecord policyRec, boolean inner) {
        PolicyRecordHandler handler = null;
        if (null!=policyRec.policyModel.getPolicyId()) {           // policy id defined, keep the policy
            handler = new PolicyRecordHandler(HandlerType.PolicyUri,policyRec.uri);
            getPolicyRecordsPassedBy().put(policyRec.uri, policyRec);
            policyRecToExpandQueue(policyRec);
        } else if (inner) { // no id given to the policy --> keep as an annonymous policy model
            String anonymousId = AnonymnousPolicyIdPrefix.append(anonymousPoliciesCount++).toString();
            handler = new PolicyRecordHandler(HandlerType.AnonymousPolicyId,anonymousId);
            getAnonymousPolicyModels().put(anonymousId,policyRec.policyModel);
        }
        return handler;
    }
    
    
    private void addHandlerToMap(Map<WSDLObject, Collection<PolicyRecordHandler>> map, WSDLObject key, PolicyRecordHandler handler) {
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyRecordHandler> newSet = new LinkedList<PolicyRecordHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private String getBaseUrl(String policyUri) {
        if (null == policyUri) {
            return null;
        }
        // TODO: encoded urls (escaped characters) might be a problem ?
        int fragmentIdx = policyUri.indexOf('#');
        return (fragmentIdx == -1) ? policyUri : policyUri.substring(0, fragmentIdx);
    }
    
    private boolean processSubelement(WSDLObject element, XMLStreamReader reader, Map<WSDLObject, Collection<PolicyRecordHandler>> map) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // "PolicyReference" element interests us
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null != policyUri) {
                addHandlerToMap(map, element, new PolicyRecordHandler(HandlerType.PolicyUri, policyUri));
                if (!policyUri.startsWith("#")) {
                    getUnresolvedUris(false).add(policyUri);
                } // end-if external policy uri
            } //endif null != policyUri
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyRecordHandler handler = readSinglePolicy(skipPolicyElement(reader, ""), true);
            if (null != handler) {           // only policies with an Id can work for us
                addHandlerToMap(map, element, handler);
            } // endif null != handler
            return true; // element consumed
        }//end if Policy element found
        return false;
        
    }
    
    private void processAttributes(WSDLObject element, XMLStreamReader reader, Map<WSDLObject, Collection<PolicyRecordHandler>> map) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToMap(map, element, new PolicyRecordHandler(HandlerType.PolicyUri, policyUri));
                if (!policyUri.startsWith("#")) {
                    getUnresolvedUris(false).add(policyUri);
                } // end-if external policy uri
            }
        }
    }
    
    public boolean portElements(WSDLPort port, XMLStreamReader reader) {
        return processSubelement(port, reader, getHandlers4PortMap());
    }
    
    public void portAttributes(WSDLPort port, XMLStreamReader reader) {
        processAttributes(port, reader, getHandlers4PortMap());
    }
    
    public boolean serviceElements(WSDLService service, XMLStreamReader reader) {
        return processSubelement(service, reader, getHandlers4ServiceMap());
    }
    
    public void serviceAttributes(WSDLService service, XMLStreamReader reader) {
        processAttributes(service, reader, getHandlers4ServiceMap());
    }
    
    
    public boolean definitionsElements(XMLStreamReader reader){
        if (PolicyConstants.POLICY.equals(reader.getName())) {     // Only "Policy" element interests me
            readSinglePolicy(skipPolicyElement(reader, ""), false);
            return true;
        }
        return false;
    }
    
    public boolean bindingElements(WSDLBoundPortType binding, XMLStreamReader reader) {
        return processSubelement(binding, reader, getHandlers4BindingMap());
    }
    
    public void bindingAttributes(WSDLBoundPortType binding, XMLStreamReader reader) {
        processAttributes(binding, reader, getHandlers4BindingMap());
    }
    
    public boolean portTypeElements(WSDLPortType portType, XMLStreamReader reader) {
        return processSubelement(portType, reader, getHandlers4PortTypeMap());
    }
    
    public void portTypeAttributes(WSDLPortType portType, XMLStreamReader reader) {
        processAttributes(portType, reader, getHandlers4PortTypeMap());
    }
    
    public boolean portTypeOperationElements(WSDLOperation operation, XMLStreamReader reader) {
        return processSubelement(operation, reader, getHandlers4OperationMap());
    }
    
    public void portTypeOperationAttributes(WSDLOperation operation, XMLStreamReader reader) {
        processAttributes(operation, reader, getHandlers4OperationMap());
    }
    
    public boolean bindingOperationElements(WSDLBoundOperation boundOperation, XMLStreamReader reader) {
        return processSubelement(boundOperation, reader, getHandlers4BoundOperationMap());
    }
    
    public void bindingOperationAttributes(WSDLBoundOperation boundOperation, XMLStreamReader reader) {
        processAttributes(boundOperation, reader, getHandlers4BoundOperationMap());
    }
    
    public boolean messageElements(WSDLMessage msg, XMLStreamReader reader) {
        return processSubelement(msg, reader, getHandlers4MessageMap());
    }
    
    public void messageAttributes(WSDLMessage msg, XMLStreamReader reader) {
        processAttributes(msg, reader, getHandlers4MessageMap());
    }
    
    
    public boolean portTypeOperationInputElements(WSDLInput input, XMLStreamReader reader) {
        return processSubelement(input, reader, getHandlers4InputMap());
    }
    
    public void portTypeOperationInputAttributes(WSDLInput input, XMLStreamReader reader) {
        processAttributes(input, reader, getHandlers4InputMap());
    }
    
    
    public boolean portTypeOperationOutputElements(WSDLOutput output, XMLStreamReader reader) {
        return processSubelement(output, reader, getHandlers4OutputMap());
    }
    
    public void portTypeOperationOutputAttributes(WSDLOutput output, XMLStreamReader reader) {
        processAttributes(output, reader, getHandlers4OutputMap());
    }
    
    
    public boolean portTypeOperationFaultElements(WSDLFault fault, XMLStreamReader reader) {
        return processSubelement(fault, reader, getHandlers4FaultMap());
    }
    
    public void portTypeOperationFaultAttributes(WSDLFault fault, XMLStreamReader reader) {
        processAttributes(fault, reader, getHandlers4FaultMap());
    }
    
    public boolean bindingOperationInputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return processSubelement(operation, reader, getHandlers4BindingInputOpMap());
    }
    
    public void bindingOperationInputAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        processAttributes(operation, reader, getHandlers4BindingInputOpMap());
    }
    
    
    public boolean bindingOperationOutputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return processSubelement(operation, reader, getHandlers4BindingOutputOpMap());
    }
    
    public void bindingOperationOutputAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        processAttributes(operation, reader, getHandlers4BindingOutputOpMap());
    }
    
    public boolean bindingOperationFaultElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return processSubelement(operation, reader, getHandlers4BindingFaultOpMap());
    }
    
    public void bindingOperationFaultAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        processAttributes(operation, reader, getHandlers4BindingFaultOpMap());
    }
    
    
    private PolicyMapBuilder getPolicyMapBuilder() {
        if (null==policyBuilder) {
            policyBuilder = new PolicyMapBuilder();
        }
        return policyBuilder;
    }
    
    private Collection<String> getPolicyURIs(Collection<PolicyRecordHandler> handlers, PolicySourceModelContext modelContext) throws PolicyException{
        Collection<String> result = new ArrayList<String>(handlers.size());
        String policyUri;
        for (PolicyRecordHandler handler: handlers) {
            policyUri = handler.handler;
            if (HandlerType.AnonymousPolicyId==handler.type) {
                PolicySourceModel policyModel = getAnonymousPolicyModels().get(policyUri);
                policyModel.expand(modelContext);
                while (getPolicyModels().containsKey(policyUri)) {
                    policyUri = AnonymnousPolicyIdPrefix.append(anonymousPoliciesCount++).toString();
                }
                getPolicyModels().put(policyUri,policyModel);
            }
            result.add(policyUri);
        }
        return result;
    }
    
    private void readExternalFile(String fileUrl) {
        try {
            URL xmlURL = new URL(fileUrl);
            InputStream ios = xmlURL.openStream();
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(ios);
            while (reader.hasNext()) {
                if (reader.isStartElement()) {
                    if (PolicyConstants.POLICY.equals(reader.getName())) {
                        readSinglePolicy(skipPolicyElement(reader, fileUrl), false);
                    }
                }
                reader.next();
            }
        } catch (IOException ioe) {
            // not necessary to solve it here
        } catch (XMLStreamException xmlse) {
            // not necessary to solve it here
        }
    }
    
    public void finished(WSDLModel model) {
        try {
            // need to make sure proper beginning order of internal policies within unresolvedUris list
            if (null != expandQueueHead) { // any policies found
                List<String> externalUris = getUnresolvedUris(false); // protect list of possible external policies
                getUnresolvedUris(true); // cleaning up the list only
                LinkedList<String> baseUnresolvedUris = new LinkedList<String>();
                for (PolicyRecord currentRec = expandQueueHead; null!=currentRec; currentRec=currentRec.next) {
                    baseUnresolvedUris.addFirst(currentRec.uri);
                }
                getUnresolvedUris(false).addAll(baseUnresolvedUris);
                expandQueueHead = null; // cut the queue off
                getUnresolvedUris(false).addAll(externalUris);
            }
            Set<String> urlsRead = new HashSet<String>();
            urlsRead.add("");
            while (!getUnresolvedUris(false).isEmpty()) {
                List<String> urisToBeSolvedList = getUnresolvedUris(false);
                getUnresolvedUris(true); // just cleaning up the list
                for (String currentUri : urisToBeSolvedList) {
                    if (!isPolicyProcessed(currentUri)) {
                        PolicyRecord prefetchedRecord = getPolicyRecordsPassedBy().get(currentUri);
                        if (null != prefetchedRecord) {
                            if (null != prefetchedRecord.unresolvedURIs) {
                                getUnresolvedUris(false).addAll(prefetchedRecord.unresolvedURIs);
                            } // end-if null != prefetchedRecord.unresolvedURIs
                            addNewPolicyNeeded(currentUri, prefetchedRecord.policyModel);
                        } else { // policy has not been yet passed by
                            if (urlsRead.contains(getBaseUrl(currentUri))) { // big problem --> unresolvable policy
                                throw new PolicyException("Can not resolve policy: "+currentUri);
                            } else {
                                readExternalFile(getBaseUrl(currentUri));
                            }
                        }
                    } // end-if policy already processed
                } // end-foreach unresolved uris
            }
            PolicySourceModelContext modelContext = PolicySourceModelContext.createContext();
            for (String policyUri : urisNeeded) {
                PolicySourceModel sourceModel = modelsNeeded.get(policyUri);
                try {
                    sourceModel.expand(modelContext);
                    modelContext.addModel(new URI(policyUri), sourceModel);
                } catch (URISyntaxException use) {
                    logger.severe("finished", use.getMessage(), use);
                }
            }
            // iterating over all services and binding all the policies read before
            for (WSDLService service : model.getServices().values()) {
                if (getHandlers4ServiceMap().containsKey(service)) {
                    getPolicyMapBuilder().registerHandler(new BuilderHandlerServiceScope(
                            getPolicyURIs(getHandlers4ServiceMap().get(service),modelContext)
                            ,getPolicyModels()
                            ,service
                            ,service.getName()));
                }
                for (WSDLPort port : service.getPorts()) {
                    if (getHandlers4PortMap().containsKey(port)) {
                        getPolicyMapBuilder().registerHandler(
                                new BuilderHandlerEndpointScope(
                                getPolicyURIs(getHandlers4PortMap().get(port),modelContext)
                                ,getPolicyModels()
                                ,port
                                ,port.getOwner().getName()
                                ,port.getName()));
                    }
                    if ( // port.getBinding may not be null, but in case ...
                            null != port.getBinding()) {
                        if ( // handler for binding
                                getHandlers4BindingMap().containsKey(port.getBinding())) {
                            getPolicyMapBuilder()
                            .registerHandler(
                                    new BuilderHandlerEndpointScope(
                                    getPolicyURIs(getHandlers4BindingMap().get(port.getBinding()),modelContext)
                                    ,getPolicyModels()
                                    ,port.getBinding()
                                    ,service.getName()
                                    ,port.getName()));
                        } // endif handler for binding
                        if ( // handler for port type
                                getHandlers4PortTypeMap().containsKey(port.getBinding().getPortType())) {
                            getPolicyMapBuilder()
                            .registerHandler(
                                    new BuilderHandlerEndpointScope(
                                    getPolicyURIs(getHandlers4PortTypeMap().get(port.getBinding().getPortType()),modelContext)
                                    ,getPolicyModels()
                                    ,port.getBinding().getPortType()
                                    ,service.getName()
                                    ,port.getName()));
                        } // endif handler for port type
                        for (WSDLBoundOperation boundOperation : port.getBinding().getBindingOperations()) {
                            WSDLOperation operation = boundOperation.getOperation();
                            WSDLInput input = operation.getInput();
                            WSDLOutput output = operation.getOutput();
                            WSDLMessage inputMsg = null;
                            WSDLMessage outputMsg = null;
                            if (null!=input) {
                                inputMsg = input.getMessage();
                            }
                            if (null!=output) {
                                outputMsg = output.getMessage();
                            }
                            if ( // handler for operation scope -- by boundOperation
                                    getHandlers4BoundOperationMap().containsKey(boundOperation)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerOperationScope(
                                        getPolicyURIs(getHandlers4BoundOperationMap().get(boundOperation),modelContext)
                                        ,getPolicyModels()
                                        ,boundOperation
                                        ,service.getName()
                                        ,port.getName()
                                        ,operation.getName()));
                            } // endif handler for operation scope -- boundOperation
                            if ( // handler for operation scope -- by operation map
                                    getHandlers4OperationMap().containsKey(operation)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerOperationScope(
                                        getPolicyURIs(getHandlers4OperationMap().get(operation),modelContext)
                                        ,getPolicyModels()
                                        ,operation
                                        ,service.getName()
                                        ,port.getName()
                                        ,operation.getName()));
                            } // endif for operation scope -- by operation map
                            if ( // input msg scope -- by binding input op
                                    getHandlers4BindingInputOpMap().containsKey(boundOperation)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerMessageScope(
                                        getPolicyURIs(getHandlers4BindingInputOpMap().get(boundOperation),modelContext)
                                        ,getPolicyModels()
                                        ,boundOperation
                                        ,BuilderHandlerMessageScope.Scope.InputMessageScope
                                        ,service.getName()
                                        ,port.getName()
                                        ,operation.getName()
                                        ,null));
                            } // endif input msg scope -- by binding input op
                            if ( // output msg scope -- by binding output op
                                    getHandlers4BindingOutputOpMap().containsKey(boundOperation)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerMessageScope(
                                        getPolicyURIs(getHandlers4BindingOutputOpMap().get(boundOperation),modelContext)
                                        ,getPolicyModels()
                                        ,boundOperation
                                        ,BuilderHandlerMessageScope.Scope.OutputMessageScope
                                        ,service.getName()
                                        ,port.getName()
                                        ,operation.getName()
                                        ,null));
                            } // endif input msg scope -- by binding output op
                            if ( // fault msg scope -- by binding fault op
                                    getHandlers4BindingFaultOpMap().containsKey(boundOperation)) {
                                for (WSDLFault fault :boundOperation.getOperation().getFaults()) {
                                    getPolicyMapBuilder()
                                    .registerHandler(
                                            new BuilderHandlerMessageScope(
                                            getPolicyURIs(getHandlers4BindingFaultOpMap().get(boundOperation),modelContext)
                                            ,getPolicyModels()
                                            ,boundOperation
                                            ,BuilderHandlerMessageScope.Scope.FaultMessageScope
                                            ,service.getName()
                                            ,port.getName()
                                            ,operation.getName()
                                            ,fault.getMessage().getName()));
                                }
                            } // endif input msg scope -- by binding fault op
                            if ( null != inputMsg   // input msg scope -- by message
                                    && getHandlers4MessageMap().containsKey(inputMsg)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerMessageScope(
                                        getPolicyURIs(getHandlers4MessageMap().get(inputMsg),modelContext)
                                        ,getPolicyModels()
                                        ,inputMsg
                                        ,BuilderHandlerMessageScope.Scope.InputMessageScope
                                        ,service.getName()
                                        ,port.getName()
                                        ,operation.getName()
                                        ,null));
                            } // endif input msg scope -- by message
                            if ( null != input    // input msg scope -- by input
                                    && getHandlers4InputMap().containsKey(input)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerMessageScope(
                                        getPolicyURIs(getHandlers4InputMap().get(input),modelContext)
                                        ,getPolicyModels()
                                        ,input
                                        ,BuilderHandlerMessageScope.Scope.InputMessageScope
                                        ,service.getName()
                                        ,port.getName()
                                        ,operation.getName()
                                        ,null));
                            } // endif input msg scope -- by input
                            if ( null != outputMsg  // output msg scope -- by message
                                    && getHandlers4MessageMap().containsKey(outputMsg)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerMessageScope(
                                        getPolicyURIs(getHandlers4MessageMap().get(outputMsg),modelContext)
                                        ,getPolicyModels()
                                        ,outputMsg
                                        ,BuilderHandlerMessageScope.Scope.OutputMessageScope
                                        ,service.getName()
                                        ,port.getName()
                                        ,operation.getName()
                                        ,null));
                            } // endif output msg scope -- by message
                            if ( null != output // output msg scope -- by output
                                    && getHandlers4OutputMap().containsKey(output)) {
                                getPolicyMapBuilder()
                                .registerHandler(
                                        new BuilderHandlerMessageScope(
                                        getPolicyURIs(getHandlers4OutputMap().get(output),modelContext)
                                        ,getPolicyModels()
                                        ,output
                                        ,BuilderHandlerMessageScope.Scope.OutputMessageScope
                                        ,service.getName()
                                        ,port.getName()
                                        ,operation.getName()
                                        ,null));
                            } // endif output msg scope -- by output
                            for (WSDLFault fault : operation.getFaults()) {
                                WSDLMessage faultMsg = fault.getMessage();
                                if ( null != faultMsg   // fault msg scope -- by message
                                        && getHandlers4MessageMap().containsKey(faultMsg)) {
                                    getPolicyMapBuilder()
                                    .registerHandler(
                                            new BuilderHandlerMessageScope(
                                            getPolicyURIs(getHandlers4MessageMap().get(faultMsg),modelContext)
                                            ,getPolicyModels()
                                            ,faultMsg
                                            ,BuilderHandlerMessageScope.Scope.FaultMessageScope
                                            ,service.getName()
                                            ,port.getName()
                                            ,operation.getName()
                                            ,faultMsg.getName()));
                                } // endif fault msg scope -- by message
                                if ( // fault msg scope -- by fault
                                        getHandlers4FaultMap().containsKey(fault)) {
                                    getPolicyMapBuilder()
                                    .registerHandler(
                                            new BuilderHandlerMessageScope(
                                            getPolicyURIs(getHandlers4FaultMap().get(fault),modelContext)
                                            ,getPolicyModels()
                                            ,fault
                                            ,BuilderHandlerMessageScope.Scope.FaultMessageScope
                                            ,service.getName()
                                            ,port.getName()
                                            ,operation.getName()
                                            ,faultMsg.getName()));
                                } // endif fault msg scope -- by fault
                            } // end foreach fault in operation
                        } // end foreach boundOperation in port
                    } // endif port.getBinding() != null
                } // end foreach port in service
            } // end foreach service in wsdl
            // finally register a wrapper for getting WSDL policy map
            
            EffectivePolicyModifier modifier = EffectivePolicyModifier.createEffectivePolicyModifier();
            PolicyMapExtender extender = PolicyMapExtender.createPolicyMapExtender();
            
            WSDLPolicyMapWrapper wrapper = new WSDLPolicyMapWrapper(policyBuilder.getPolicyMap(Arrays.asList(new PolicyMapMutator[] {modifier, extender})), modifier, extender);
            model.addExtension(wrapper);
            // TODO: replace after J1
            processMtomPolicyAssertion(model);
        } catch(PolicyException pe) {
            logger.severe("finished",pe.getMessage(),pe);
        }
    }
    
    /**
     * Reads policy reference element <wsp:PolicyReference/> and returns referenced policy URI as String
     */
    private String readPolicyReferenceElement(XMLStreamReader reader) {
        try {
            if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // "PolicyReference" element interests me
                for (int i=0; i<reader.getAttributeCount(); i++) {
                    if (PolicyConstants.POLICY_URI.getLocalPart().equals(reader.getAttributeName(i).getLocalPart())) {
                        String uriValue = reader.getAttributeValue(i);
                        reader.next();
                        return uriValue;
                    }
                }
            }
            reader.next();
            return null;
        } catch(XMLStreamException e) {
            return null;
        }
    }
    
    
    /**
     * Reads policy reference URIs from PolicyURIs attribute and returns them as a String array
     * returns null if there is no such attribute
     */
    private String[] getPolicyURIsFromAttr(XMLStreamReader reader) {
        String policyURIs = reader.getAttributeValue(
                PolicyConstants.POLICY_URIs.getNamespaceURI(),PolicyConstants.POLICY_URIs.getLocalPart());
        if (null!=policyURIs) {
            return policyURIs.split("[\\n ]+");
        }
        return null;
    }
    
    
    /**
     *  skips current element (should be in START_ELEMENT state) and returns its content as String
     */
    private PolicyRecord skipPolicyElement(XMLStreamReader reader, String baseUrl){
        if (null==reader) {
            return null;
        }
        if (!reader.isStartElement()) {
            return null;
        }
        StringBuffer elementCode = new StringBuffer();
        PolicyRecord policyRec = new PolicyRecord();
        QName elementName = reader.getName();
        boolean insidePolicyReferenceAttr;
        int depth = 0;
        try{
            do {
                switch (reader.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:  // process start of next element
                        QName curName = reader.getName();
                        insidePolicyReferenceAttr = PolicyConstants.POLICY_REFERENCE.equals(curName);
                        if (elementName.equals(curName)) {  // it is our element !
                            depth++;                        // we are then deeper
                        }
                        StringBuffer xmlnsCode = new StringBuffer();    // take care about namespaces as well
                        Set<String> tmpNsSet = new HashSet<String>();
                        if ((null == curName.getPrefix()) || ("".equals(curName.getPrefix()))) {           // no prefix
                            elementCode
                                    .append('<')                     // start tag
                                    .append(curName.getLocalPart());
                            xmlnsCode
                                    .append(" xmlns=\"")
                                    .append(curName.getNamespaceURI())
                                    .append('"');
                            
                        } else {                                    // prefix presented
                            elementCode
                                    .append('<')                     // start tag
                                    .append(curName.getPrefix())
                                    .append(':')
                                    .append(curName.getLocalPart());
                            xmlnsCode
                                    .append(" xmlns:")
                                    .append(curName.getPrefix())
                                    .append("=\"")
                                    .append(curName.getNamespaceURI())
                                    .append('"');
                            tmpNsSet.add(curName.getPrefix());
                        }
                        int attrCount = reader.getAttributeCount();     // process element attributes
                        StringBuffer attrCode = new StringBuffer();
                        for (int i=0; i<attrCount; i++) {
                            if (insidePolicyReferenceAttr && "URI".equals(
                                    reader.getAttributeName(i).getLocalPart())) { // PolicyReference found
                                if (null==policyRec.unresolvedURIs) { // first such URI found
                                    policyRec.unresolvedURIs = new HashSet<String>(); // initialize URIs set
                                }
                                policyRec.unresolvedURIs.add(reader.getAttributeValue(i)); // add the URI
                            } // end-if PolicyReference attribute found
                            if ("xmlns".equals(reader.getAttributePrefix(i)) && tmpNsSet.contains(reader.getAttributeLocalName(i))) {
                                continue; // do not append already defined ns
                            }
                            if ((null == reader.getAttributePrefix(i)) || ("".equals(reader.getAttributePrefix(i)))) {  // no attribute prefix
                                attrCode
                                        .append(' ')
                                        .append(reader.getAttributeLocalName(i))
                                        .append("=\"")
                                        .append(reader.getAttributeValue(i))
                                        .append('"');
                            } else {                                        // prefix`presented
                                attrCode
                                        .append(' ')
                                        .append(reader.getAttributePrefix(i))
                                        .append(':')
                                        .append(reader.getAttributeLocalName(i))
                                        .append("=\"")
                                        .append(reader.getAttributeValue(i))
                                        .append('"');
                                if (!tmpNsSet.contains(reader.getAttributePrefix(i))) {
                                    xmlnsCode
                                            .append(" xmlns:")
                                            .append(reader.getAttributePrefix(i))
                                            .append("=\"")
                                            .append(reader.getAttributeNamespace(i))
                                            .append('"');
                                    tmpNsSet.add(reader.getAttributePrefix(i));
                                } // end if prefix already processed
                            }
                        } // end foreach attr
                        elementCode
                                .append(xmlnsCode)          // complete the start element tag
                                .append(attrCode)
                                .append('>');
                        break;
                        //case XMLStreamConstants.ATTRIBUTE:   Unreachable (I hope ;-)
                        //    break;
                        //case XMLStreamConstants.NAMESPACE:   Unreachable (I hope ;-)
                        //    break;
                    case XMLStreamConstants.END_ELEMENT:
                        curName = reader.getName();
                        if (elementName.equals(curName)) {  // it is our element !
                            depth--;                        // go up
                        }
                        elementCode
                                .append("</")                     // append appropriate XML code
                                .append("".equals(curName.getPrefix())?"":curName.getPrefix()+':')
                                .append(curName.getLocalPart())
                                .append('>');                        // complete the end element tag
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        elementCode.append(reader.getText());           // append text data
                        break;
                    case XMLStreamConstants.CDATA:
                        elementCode
                                .append("<![CDATA[")                // append CDATA delimiters
                                .append(reader.getText())
                                .append("]]>");
                        break;
                    case XMLStreamConstants.COMMENT:    // Ignore any comments
                        break;
                    case XMLStreamConstants.SPACE:      // Ignore spaces as well
                        break;
                }
                if (reader.hasNext() && depth>0) {
                    reader.next();
                }
            } while (XMLStreamConstants.END_DOCUMENT!=reader.getEventType() && depth>0);
            policyRec.policyModel = PolicyModelUnmarshaller.getXmlUnmarshaller().unmarshalModel(
                    new StringReader(elementCode.toString()));
            if (null != policyRec.policyModel.getPolicyId()) {
                policyRec.uri = baseUrl + "#" + policyRec.policyModel.getPolicyId();
            }
        }catch(Exception e){
            logger.log(Level.SEVERE,"definitionsElements","Exception while reading policy expression",e);
            logger.log(Level.SEVERE, "definitionsElements",elementCode.toString());
            return null;
        };
        
        return policyRec;
    }
    
    private static final QName mtomAssertion =
            new QName("http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization", "OptimizedMimeSerialization");
    /**
     * process Mtom policy assertions and if found and is not optional then mtom is enabled on the
     * {@link WSDLBoundPortType}
     *
     * @param boundPortType must be non-null
     */
    private void processMtomPolicyAssertion(WSDLModel model){
        if (null==model) {
            return;
        }
        WSDLPolicyMapWrapper wrapper = model.getExtension(WSDLPolicyMapWrapper.class);
        if (null==wrapper) {
            return;
        }
        PolicyMap policyMap = wrapper.getPolicyMap();
        if(null==policyMap) {
            return;
        }
        for (WSDLService service:model.getServices().values()) {
            for (WSDLPort port : service.getPorts()) {
                PolicyMapKey key = PolicyMap.createWsdlEndpointScopeKey(service.getName(),port.getName());
                try {
                    Policy policy = policyMap.getEndpointEffectivePolicy(key);
                    if (null!=policy && policy.contains(mtomAssertion)) {
                        Iterator <AssertionSet> assertions = policy.iterator();
                        while(assertions.hasNext()){
                            AssertionSet assertionSet = assertions.next();
                            Iterator<PolicyAssertion> policyAssertion = assertionSet.iterator();
                            while(policyAssertion.hasNext()){
                                PolicyAssertion assertion = policyAssertion.next();
                                if(assertion.getName().equals(mtomAssertion) && !assertion.isOptional()){
                                    port.getBinding().enableMTOM();
                                } // end-if non optional mtom assertion found
                            } // next assertion
                        } // next alternative
                    } // end-if policy contains mtom assertion
                } catch (PolicyException pe) {
                    // TODO: decide how to handle pe
                }
            } // end foreach port
        } // end foreach service
    }
}
