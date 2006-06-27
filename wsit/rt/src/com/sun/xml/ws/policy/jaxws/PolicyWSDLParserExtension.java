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

/**
 *
 * @author japod
 */
public class PolicyWSDLParserExtension extends WSDLParserExtension {
    
    enum HandlerType {
        PolicyUri, AnonymousPolicyId
    }
    
    class PolicyHandler {
        String handler;
        HandlerType type;
        
        PolicyHandler(HandlerType type, String handler) {
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
            PolicyRecord current;
            for (current = head; null!=current.next; current=current.next) {
                if (insertedRec.unresolvedURIs.remove(current.uri)) {
                    if (insertedRec.unresolvedURIs.isEmpty()) {
                        insertedRec.next = current.next;
                        current.next = insertedRec;
                        return head;
                    }
                }
            }
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
    static final StringBuffer AnonymnousPolicyIdPrefix = new StringBuffer("#__anonymousPolicy__ID");
    
    // anonymous policies count
    int anonymousPoliciesCount;
    
    // policy queue -- needed for evaluating the right order policy of policy models expansion
    PolicyRecord queueHead = null;
    
    // storage for policies with an id defined within given WSDL
    HashMap<String,PolicySourceModel> policyModels = null;
    // storage for anonymous policies defined within given WSDL
    HashMap<String,PolicySourceModel> anonymousPolicyModels = null;
    
    // lookup tables for Policy attachments found
    HashMap<WSDLService,Collection<PolicyHandler>> handlers4ServiceMap = null;
    HashMap<WSDLPort,Collection<PolicyHandler>> handlers4PortMap = null;
    HashMap<WSDLPortType,Collection<PolicyHandler>> handlers4PortTypeMap = null;
    HashMap<WSDLBoundPortType,Collection<PolicyHandler>> handlers4BindingMap = null;
    HashMap<WSDLBoundOperation,Collection<PolicyHandler>> handlers4BoundOperationMap = null;
    HashMap<WSDLOperation,Collection<PolicyHandler>> handlers4OperationMap = null;
    HashMap<WSDLMessage,Collection<PolicyHandler>> handlers4MessageMap = null;
    HashMap<WSDLInput,Collection<PolicyHandler>> handlers4InputMap = null;
    HashMap<WSDLOutput,Collection<PolicyHandler>> handlers4OutputMap = null;
    HashMap<WSDLFault,Collection<PolicyHandler>> handlers4FaultMap = null;
    HashMap<WSDLBoundOperation,Collection<PolicyHandler>> handlers4BindingInputOpMap = null;
    HashMap<WSDLBoundOperation,Collection<PolicyHandler>> handlers4BindingOutputOpMap = null;
    HashMap<WSDLBoundOperation,Collection<PolicyHandler>> handlers4BindingFaultOpMap = null;
    
    PolicyMapBuilder policyBuilder = new PolicyMapBuilder();
    
    HashMap<String,PolicySourceModel> getPolicyModels() {
        if (null==policyModels) {
            policyModels = new HashMap<String,PolicySourceModel>();
        }
        return policyModels;
    }
    
    HashMap<String,PolicySourceModel> getAnonymousPolicyModels() {
        if (null==anonymousPolicyModels) {
            anonymousPolicyModels = new HashMap<String,PolicySourceModel>();
        }
        return anonymousPolicyModels;
    }
    
    HashMap<WSDLService,Collection<PolicyHandler>> getHandlers4ServiceMap() {
        if (null==handlers4ServiceMap) {
            handlers4ServiceMap = new HashMap<WSDLService,Collection<PolicyHandler>>();
        }
        return handlers4ServiceMap;
    }
    
    HashMap<WSDLPort,Collection<PolicyHandler>> getHandlers4PortMap() {
        if (null==handlers4PortMap) {
            handlers4PortMap = new HashMap<WSDLPort,Collection<PolicyHandler>>();
        }
        return handlers4PortMap;
    }
    
    HashMap<WSDLPortType,Collection<PolicyHandler>> getHandlers4PortTypeMap() {
        if (null==handlers4PortTypeMap) {
            handlers4PortTypeMap = new HashMap<WSDLPortType,Collection<PolicyHandler>>();
        }
        return handlers4PortTypeMap;
    }
    
    HashMap<WSDLBoundPortType,Collection<PolicyHandler>> getHandlers4BindingMap() {
        if (null==handlers4BindingMap) {
            handlers4BindingMap = new HashMap<WSDLBoundPortType,Collection<PolicyHandler>>();
        }
        return handlers4BindingMap;
    }
    
    HashMap<WSDLOperation,Collection<PolicyHandler>> getHandlers4OperationMap() {
        if (null==handlers4OperationMap) {
            handlers4OperationMap = new HashMap<WSDLOperation,Collection<PolicyHandler>>();
        }
        return handlers4OperationMap;
    }
    
    HashMap<WSDLBoundOperation,Collection<PolicyHandler>> getHandlers4BoundOperationMap() {
        if (null==handlers4BoundOperationMap) {
            handlers4BoundOperationMap = new HashMap<WSDLBoundOperation,Collection<PolicyHandler>>();
        }
        return handlers4BoundOperationMap;
    }
    
    HashMap<WSDLMessage,Collection<PolicyHandler>> getHandlers4MessageMap() {
        if (null==handlers4MessageMap) {
            handlers4MessageMap = new HashMap<WSDLMessage,Collection<PolicyHandler>>();
        }
        return handlers4MessageMap;
    }
    
    HashMap<WSDLInput,Collection<PolicyHandler>> getHandlers4InputMap() {
        if (null==handlers4InputMap) {
            handlers4InputMap = new HashMap<WSDLInput,Collection<PolicyHandler>>();
        }
        return handlers4InputMap;
    }
    
    HashMap<WSDLOutput,Collection<PolicyHandler>> getHandlers4OutputMap() {
        if (null==handlers4OutputMap) {
            handlers4OutputMap = new HashMap<WSDLOutput,Collection<PolicyHandler>>();
        }
        return handlers4OutputMap;
    }
    
    HashMap<WSDLFault,Collection<PolicyHandler>> getHandlers4FaultMap() {
        if (null==handlers4FaultMap) {
            handlers4FaultMap = new HashMap<WSDLFault,Collection<PolicyHandler>>();
        }
        return handlers4FaultMap;
    }
    
    HashMap<WSDLBoundOperation,Collection<PolicyHandler>> getHandlers4BindingInputOpMap() {
        if (null==handlers4BindingInputOpMap) {
            handlers4BindingInputOpMap = new HashMap<WSDLBoundOperation,Collection<PolicyHandler>>();
        }
        return handlers4BindingInputOpMap;
    }
    
    HashMap<WSDLBoundOperation,Collection<PolicyHandler>> getHandlers4BindingOutputOpMap() {
        if (null==handlers4BindingOutputOpMap) {
            handlers4BindingOutputOpMap = new HashMap<WSDLBoundOperation,Collection<PolicyHandler>>();
        }
        return handlers4BindingOutputOpMap;
    }
    
    HashMap<WSDLBoundOperation,Collection<PolicyHandler>> getHandlers4BindingFaultOpMap() {
        if (null==handlers4BindingFaultOpMap) {
            handlers4BindingFaultOpMap = new HashMap<WSDLBoundOperation,Collection<PolicyHandler>>();
        }
        return handlers4BindingFaultOpMap;
    }
    
    void queuePolicyRec(PolicyRecord policyRec) {
        if (null==queueHead) {
            queueHead = policyRec;
        } else {
            queueHead = queueHead.insert(policyRec);
        }
    }
    
    
    /**
     * Creates a new instance of PolicyWSDLParserExtension
     */
    public PolicyWSDLParserExtension() {
    }
        
    PolicyHandler readSinglePolicy(PolicyRecord policyRec, boolean inner) {
        PolicyHandler handler = null;
        if (null!=policyRec.policyModel.getPolicyId()) {           // policy id defined, keep the policy
            policyRec.uri = (new StringBuffer("#")).append(policyRec.policyModel.getPolicyId()).toString();
            handler = new PolicyHandler(HandlerType.PolicyUri,policyRec.uri);
            getPolicyModels().put(policyRec.uri, policyRec.policyModel);
            queuePolicyRec(policyRec);
        } else if (inner) { // no id given to the policy --> keep as an annonymous policy model
            String anonymousId = AnonymnousPolicyIdPrefix.append(anonymousPoliciesCount++).toString();
            handler = new PolicyHandler(HandlerType.AnonymousPolicyId,anonymousId);
            getAnonymousPolicyModels().put(anonymousId,policyRec.policyModel);
        }
        return handler;
    }
    
    private void addHandlerToServiceMap(WSDLService key, PolicyHandler handler) {
        Map<WSDLService,Collection<PolicyHandler>> map = getHandlers4ServiceMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToPortMap(WSDLPort key, PolicyHandler handler) {
        Map<WSDLPort,Collection<PolicyHandler>> map = getHandlers4PortMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToBindingMap(WSDLBoundPortType key, PolicyHandler handler) {
        Map<WSDLBoundPortType,Collection<PolicyHandler>> map = getHandlers4BindingMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToPortTypeMap(WSDLPortType key, PolicyHandler handler) {
        Map<WSDLPortType,Collection<PolicyHandler>> map = getHandlers4PortTypeMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToOperationMap(WSDLOperation key, PolicyHandler handler) {
        Map<WSDLOperation,Collection<PolicyHandler>> map = getHandlers4OperationMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToBoundOperationMap(WSDLBoundOperation key, PolicyHandler handler) {
        Map<WSDLBoundOperation,Collection<PolicyHandler>> map = getHandlers4BoundOperationMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToMessageMap(WSDLMessage key, PolicyHandler handler) {
        Map<WSDLMessage,Collection<PolicyHandler>> map = getHandlers4MessageMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToInputMap(WSDLInput key, PolicyHandler handler) {
        Map<WSDLInput,Collection<PolicyHandler>> map = getHandlers4InputMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToOutputMap(WSDLOutput key, PolicyHandler handler) {
        Map<WSDLOutput,Collection<PolicyHandler>> map = getHandlers4OutputMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToFaultMap(WSDLFault key, PolicyHandler handler) {
        Map<WSDLFault,Collection<PolicyHandler>> map = getHandlers4FaultMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToBindingInputOpMap(WSDLBoundOperation key, PolicyHandler handler) {
        Map<WSDLBoundOperation,Collection<PolicyHandler>> map = getHandlers4BindingInputOpMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToBindingOutputOpMap(WSDLBoundOperation key, PolicyHandler handler) {
        Map<WSDLBoundOperation,Collection<PolicyHandler>> map = getHandlers4BindingOutputOpMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    private void addHandlerToBindingFaultOpMap(WSDLBoundOperation key, PolicyHandler handler) {
        Map<WSDLBoundOperation,Collection<PolicyHandler>> map = getHandlers4BindingFaultOpMap();
        if (map.containsKey(key)) {
            map.get(key).add(handler);
        } else {
            Collection<PolicyHandler> newSet = new LinkedList<PolicyHandler>();
            newSet.add(handler);
            map.put(key,newSet);
        }
    }
    
    
    public boolean portElements(WSDLPort port, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // "PolicyReference" element interests us
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToPortMap(port,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            } //endif null!=policyUri
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToPortMap(port,handler);
            } // endif null!=handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void portAttributes(WSDLPort port, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToPortMap(port,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    public boolean serviceElements(WSDLService service, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // "PolicyReference" element interests us
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToServiceMap(service,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToServiceMap(service,handler);
            } // endif null!=handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void serviceAttributes(WSDLService service, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToServiceMap(service,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    
    public boolean definitionsElements(XMLStreamReader reader){
        if (PolicyConstants.POLICY.equals(reader.getName())) {     // Only "Policy" element interests me
            readSinglePolicy(skipPolicyElement(reader),false);
            return true;
        }
        return false;
    }
    
    public boolean bindingElements(WSDLBoundPortType binding, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // Only "PolicyReference" element interests me
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToBindingMap(binding,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToBindingMap(binding,handler);
            } // endif null!=handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void bindingAttributes(WSDLBoundPortType binding, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToBindingMap(binding,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    public boolean portTypeElements(WSDLPortType portType, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // Only "PolicyReference" element interests me
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToPortTypeMap(portType,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToPortTypeMap(portType,handler);
            } // endif null!=handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void portTypeAttributes(WSDLPortType portType, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToPortTypeMap(portType,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    public boolean portTypeOperationElements(WSDLOperation operation, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // Only "PolicyReference" element interests me
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToOperationMap(operation,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToOperationMap(operation,handler);
            } // endif null!=handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void portTypeOperationAttributes(WSDLOperation operation, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToOperationMap(operation,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    public boolean bindingOperationElements(WSDLBoundOperation boundOperation, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // Only "PolicyReference" element interests me
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToBoundOperationMap(boundOperation,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToBoundOperationMap(boundOperation,handler);
            } // endif null!=handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void bindingOperationAttributes(WSDLBoundOperation boundOperation, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToBoundOperationMap(boundOperation,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    public boolean messageElements(WSDLMessage msg, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // Only "PolicyReference" element interests me
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToMessageMap(msg,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToMessageMap(msg,handler);
            } // endif null!=handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void messageAttributes(WSDLMessage msg, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToMessageMap(msg,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    
    public boolean portTypeOperationInputElements(WSDLInput input, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // Only "PolicyReference" element interests me
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToInputMap(input,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToInputMap(input,handler);
            } // endif null!=handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void portTypeOperationInputAttributes(WSDLInput input, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToInputMap(input,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    
    public boolean portTypeOperationOutputElements(WSDLOutput output, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // Only "PolicyReference" element interests me
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToOutputMap(output,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToOutputMap(output,handler);
            } // endif null!=handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void portTypeOperationOutputAttributes(WSDLOutput output, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToOutputMap(output,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    
    public boolean portTypeOperationFaultElements(WSDLFault fault, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // Only "PolicyReference" element interests me
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToFaultMap(fault,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToFaultMap(fault,handler);
            } // endif null!=handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void portTypeOperationFaultAttributes(WSDLFault fault, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToFaultMap(fault,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    
    public boolean bindingOperationInputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // "PolicyReference" element interests me
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToBindingInputOpMap(operation,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToBindingInputOpMap(operation,handler);
            } // endif null!=policyId
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void bindingOperationInputAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToBindingInputOpMap(operation,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    
    public boolean bindingOperationOutputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // Only "PolicyReference" element interests me
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToBindingOutputOpMap(operation,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToBindingOutputOpMap(operation,handler);
            } // endif null!=handler
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void bindingOperationOutputAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToBindingOutputOpMap(operation,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    public boolean bindingOperationFaultElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        if (PolicyConstants.POLICY_REFERENCE.equals(reader.getName())) {     // Only "PolicyReference" element interests me
            String policyUri = readPolicyReferenceElement(reader);      // get the URI
            if (null!=policyUri) {
                addHandlerToBindingFaultOpMap(operation,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
            return true;
        } else if (PolicyConstants.POLICY.equals(reader.getName())) {   // policy could be defined here
            PolicyHandler handler = readSinglePolicy(skipPolicyElement(reader),true);
            if (null!=handler) {           // only policies with an Id can work for us
                addHandlerToBindingFaultOpMap(operation,handler);
            } // endif null!=policyId
            return true; // element consumed
        }//end if Policy element found
        return false;
    }
    
    public void bindingOperationFaultAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        String[] uriArray = getPolicyURIsFromAttr(reader);
        if (null!=uriArray) {
            for (String policyUri : uriArray) {
                addHandlerToBindingFaultOpMap(operation,new PolicyHandler(HandlerType.PolicyUri,policyUri));
            }
        }
    }
    
    
    private PolicyMapBuilder getPolicyMapBuilder() {
        if (null==policyBuilder) {
            policyBuilder = new PolicyMapBuilder();
        }
        return policyBuilder;
    }
    
    private Collection<String> getPolicyURIs(Collection<PolicyHandler> handlers, PolicySourceModelContext modelContext) throws PolicyException{
        Collection<String> result = new ArrayList<String>(handlers.size());
        String policyUri;
        for (PolicyHandler handler: handlers) {
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
    
    public void finished(WSDLModel model) {
        // expand all the models
        try {
            PolicySourceModelContext modelContext = PolicySourceModelContext.createContext();
            if(null!=queueHead) {
                for (PolicyRecord currentRec = queueHead; null!=currentRec; currentRec=currentRec.next) {
                    try {
                        currentRec.policyModel.expand(modelContext);
                        modelContext.addModel(new URI(currentRec.uri),currentRec.policyModel);
                    } catch (URISyntaxException use) {
                        logger.severe("finished",use.getMessage(),use);
                    }
                }
                queueHead = null; // cut the queue off
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
    String[] getPolicyURIsFromAttr(XMLStreamReader reader) {
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
    private PolicyRecord skipPolicyElement(XMLStreamReader reader){
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
                        if (null==curName.getPrefix()
                        ||"".equals(curName.getPrefix())) {           // no prefix
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
                            if (null==reader.getAttributePrefix(i)
                            ||"".equals(reader.getAttributePrefix(i))) {  // no attribute prefix
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
