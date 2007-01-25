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

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLInput;
import com.sun.xml.ws.api.model.wsdl.WSDLMessage;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLOutput;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;
import com.sun.xml.ws.api.wsdl.writer.WSDLGenExtnContext;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMerger;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.jaxws.spi.PolicyMapUpdateProvider;
import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelGenerator;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelMarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * Marshals the contents of a policy map to WSDL.
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
public class PolicyWSDLGeneratorExtension extends WSDLGeneratorExtension {
    
    static enum ScopeType {
        SERVICE,
        ENDPOINT,
        OPERATION,
        INPUT_MESSAGE,
        OUTPUT_MESSAGE,
        FAULT_MESSAGE
    }
    
    private final static PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyWSDLGeneratorExtension.class);
    
    private PolicyMap policyMap;
    private SEIModel seiModel;
    private Class endpointClass;
    private final Collection<PolicySubject> subjects = new LinkedList<PolicySubject>();
    
    private PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller(true);
    private PolicyMerger merger = PolicyMerger.getMerger();
    
    public void start(final WSDLGenExtnContext context) {
        LOGGER.entering("start");
        try {
            this.seiModel = context.getModel();
            this.endpointClass = context.getEndpointClass();
            
            final PolicyMapUpdateProvider[] policyMapUpdateProviders = PolicyUtils.ServiceProvider.load(PolicyMapUpdateProvider.class);
            final PolicyMapExtender[] extenders = new PolicyMapExtender[policyMapUpdateProviders.length];
            for (int i=0; i < policyMapUpdateProviders.length; i++) {
                extenders[i] = PolicyMapExtender.createPolicyMapExtender();
            }
            
            try {
                final String configId = context.getEndpointClass().getName();
                policyMap = PolicyConfigParser.parse(configId, context.getContainer(), extenders);
                if (policyMap == null) {
                    LOGGER.fine("start", LocalizationMessages.WSP_001034_CREATE_POLICY_MAP_FOR_CONFIG(configId));
                    policyMap = PolicyMap.createPolicyMap(Arrays.asList(extenders));
                }
            } catch (PolicyException e) {
                LOGGER.fine("start", LocalizationMessages.WSP_001027_FAILED_TO_READ_WSIT_CFG(), e);
            }            
            
            try {
                context.getRoot()._namespace(PolicyConstants.POLICY_NAMESPACE_URI, PolicyConstants.POLICY_NAMESPACE_PREFIX);
                final WSBinding binding = context.getBinding();
                
                for (int i = 0; i < policyMapUpdateProviders.length; i++) {
                    policyMapUpdateProviders[i].update(extenders[i], policyMap, seiModel, binding);
                    extenders[i].disconnect();
                }
            } catch (PolicyException e) {
                LOGGER.severe("start", LocalizationMessages.WSP_001048_MAP_UPDATE_FAILED(), e);
                throw new WebServiceException(LocalizationMessages.WSP_001048_MAP_UPDATE_FAILED(), e);
            }
        } finally {
            LOGGER.exiting("start");
        }
    }
    
    public void addDefinitionsExtension(final TypedXmlWriter definitions) {
        try {
            LOGGER.entering("addDefinitionsExtension");
            if (policyMap != null) {
                subjects.addAll(policyMap.getPolicySubjects());
                boolean usingPolicy = false;
                PolicyModelGenerator generator = null;
                Set<String> policyIDsOrNamesWritten = null;
                for (PolicySubject subject : subjects) {
                    if (subject.getSubject() != null) {
                        if (!usingPolicy) {
                            definitions._element(PolicyConstants.USING_POLICY, TypedXmlWriter.class);
                            usingPolicy = true;
                            policyIDsOrNamesWritten = new HashSet<String>();
                            generator = PolicyModelGenerator.getGenerator();
                        }
                        final Policy policy = subject.getEffectivePolicy(merger);
                        if ((null != policy.getIdOrName()) && (!policyIDsOrNamesWritten.contains(policy.getIdOrName()))) {
                            final PolicySourceModel policyInfoset = generator.translate(policy);
                            marshaller.marshal(policyInfoset, definitions);
                            policyIDsOrNamesWritten.add(policy.getIdOrName());
                        } else {
                            LOGGER.fine("addDefinitionsExtension", LocalizationMessages.WSP_001047_POLICY_ID_NULL_OR_DUPLICATE(policy));
                        }
                    } else {
                        LOGGER.fine("addDefinitionsExtension", LocalizationMessages.WSP_001019_NOT_MARSHALLING_WSDL_SUBJ_NULL(subject));
                    }
                }
            } else {
                LOGGER.fine("addDefinitionsExtension", LocalizationMessages.WSP_001020_NOT_MARSHALLING_ANY_POLICIES_POLICY_MAP_IS_NULL());
            }
        } catch (PolicyException e) {
            LOGGER.severe("addDefinitionsExtension", LocalizationMessages.WSP_001029_FAILED_TO_MARSHALL_POLICIES(), e);
            throw new WebServiceException(LocalizationMessages.WSP_001029_FAILED_TO_MARSHALL_POLICIES(), e);
        } finally {
            LOGGER.exiting("addDefinitionsExtension");
        }
    }
    
    public void addServiceExtension(final TypedXmlWriter service) {
        LOGGER.entering("addServiceExtension");
        final String serviceName = ((null != seiModel) && (null != endpointClass)) ?
            WSEndpoint.getDefaultServiceName(endpointClass).getLocalPart() :
            null;
        selectAndProcessSubject(service, WSDLService.class, ScopeType.SERVICE, serviceName);
        LOGGER.exiting("addServiceExtension");
    }
    
    public void addPortExtension(final TypedXmlWriter port) {
        LOGGER.entering("addPortExtension");
        final String portName = ((null != seiModel) && (null != endpointClass)) ?
            WSEndpoint.getDefaultPortName(seiModel.getServiceQName(), endpointClass).getLocalPart() :
            null;
        selectAndProcessSubject(port, WSDLPort.class, ScopeType.ENDPOINT, portName);
        LOGGER.exiting("addPortExtension");
    }
    
    public void addPortTypeExtension(final TypedXmlWriter portType) {
        LOGGER.entering("addPortTypeExtension");
        final String portTypeName = (null != seiModel) ? seiModel.getPortTypeName().getLocalPart() : null;
        selectAndProcessSubject(portType, WSDLPortType.class, ScopeType.ENDPOINT, portTypeName);
        LOGGER.exiting("addPortTypeExtension");
    }
    
    public void addBindingExtension(final TypedXmlWriter binding) {
        LOGGER.entering("addBindingExtension");
        final String bindingName = ((null != seiModel) && (null != endpointClass))?
            (WSEndpoint.getDefaultPortName(seiModel.getServiceQName(), endpointClass).getLocalPart() + "Binding") :
            null;
        selectAndProcessSubject(binding, WSDLBoundPortType.class, ScopeType.ENDPOINT, bindingName);
        LOGGER.exiting("addBindingExtension");
    }
    
    public void addOperationExtension(final TypedXmlWriter operation, final JavaMethod method) {
        LOGGER.entering("addOperationExtension");
        selectAndProcessSubject(operation, WSDLOperation.class, ScopeType.OPERATION, method);
        LOGGER.exiting("addOperationExtension");
    }
    
    public void addBindingOperationExtension(final TypedXmlWriter operation, final JavaMethod method) {
        LOGGER.entering("addBindingOperationExtension");
        selectAndProcessSubject(operation, WSDLBoundOperation.class, ScopeType.OPERATION, method);
        LOGGER.exiting("addBindingOperationExtension");
    }
    
    public void addInputMessageExtension(final TypedXmlWriter message, final JavaMethod method) {
        LOGGER.entering("addInputMessageExtension");
        final String messageName = (null != method) ? method.getRequestMessageName() : null;
        selectAndProcessSubject(message, WSDLMessage.class, ScopeType.INPUT_MESSAGE, messageName);
        LOGGER.exiting("addInputMessageExtension");
    }
    
    public void addOutputMessageExtension(final TypedXmlWriter message, final JavaMethod method) {
        LOGGER.entering("addOutputMessageExtension");
        final String messageName = (null != method) ? method.getResponseMessageName() : null;
        selectAndProcessSubject(message, WSDLMessage.class, ScopeType.OUTPUT_MESSAGE, messageName);
        LOGGER.exiting("addOutputMessageExtension");
    }
    
    public void addFaultMessageExtension(final TypedXmlWriter message, final JavaMethod method, final CheckedException ce) {
        LOGGER.entering("addFaultMessageExtension");
        final String messageName = (null != ce) ? ce.getMessageName() : null;
        selectAndProcessSubject(message, WSDLMessage.class, ScopeType.FAULT_MESSAGE, messageName);
        LOGGER.exiting("addFaultMessageExtension");
    }
    
    public void addOperationInputExtension(final TypedXmlWriter input, final JavaMethod method) {
        LOGGER.entering("addOperationInputExtension");
        final String messageName = (null != method) ? method.getRequestMessageName() : null;
        selectAndProcessSubject(input, WSDLInput.class, ScopeType.INPUT_MESSAGE, messageName);
        LOGGER.exiting("addOperationInputExtension");
    }
    
    public void addOperationOutputExtension(final TypedXmlWriter output, final JavaMethod method) {
        LOGGER.entering("addOperationOutputExtension");
        final String messageName = (null != method) ? method.getResponseMessageName() : null;
        selectAndProcessSubject(output, WSDLOutput.class, ScopeType.OUTPUT_MESSAGE, messageName);
        LOGGER.exiting("addOperationOutputExtension");
    }
    
    public void addOperationFaultExtension(final TypedXmlWriter fault, final JavaMethod method, final CheckedException ce) {
        LOGGER.entering("addOperationFaultExtension");
        final String messageName = (null != ce) ? ce.getMessageName() : null;
        selectAndProcessSubject(fault, WSDLFault.class, ScopeType.FAULT_MESSAGE, messageName);
        LOGGER.exiting("addOperationFaultExtension");
    }
    
    public void addBindingOperationInputExtension(final TypedXmlWriter input, final JavaMethod method) {
        LOGGER.entering("addBindingOperationInputExtension");
        final String messageName = (null != method) ? method.getOperationName() : null;
        selectAndProcessSubject(input, WSDLBoundOperation.class, ScopeType.INPUT_MESSAGE, messageName);
        LOGGER.exiting("addBindingOperationInputExtension");
    }
    
    public void addBindingOperationOutputExtension(final TypedXmlWriter output, final JavaMethod method) {
        LOGGER.entering("addBindingOperationOutputExtension");
        final String messageName = (null != method) ? method.getOperationName() : null;
        selectAndProcessSubject(output, WSDLBoundOperation.class, ScopeType.OUTPUT_MESSAGE, messageName);
        LOGGER.exiting("addBindingOperationOutputExtension");
    }
    
    public void addBindingOperationFaultExtension(final TypedXmlWriter fault, final JavaMethod method, final CheckedException ce) {
        LOGGER.entering("addBindingOperationFaultExtension");
        final String messageName = (null != ce) ? ce.getMessageName() : null;
        selectAndProcessSubject(fault, WSDLFault.class, ScopeType.FAULT_MESSAGE, messageName);
        LOGGER.exiting("addBindingOperationFaultExtension");
    }
    
    /**
     * This method should only be invoked by interface methods that deal with operations because they
     * may use JavaMethod as PolicySubject instead of a WSDL object.
     */
    private void selectAndProcessSubject(final TypedXmlWriter xmlWriter, final Class clazz, final ScopeType scopeType, final JavaMethod method) {
        LOGGER.entering("selectAndProcessSubject", new Object[] {xmlWriter, clazz, scopeType, method});
        if (method != null) {
            if (subjects != null) {
                for (PolicySubject subject : subjects) {
                    if (method.equals(subject.getSubject())) {
                        writePolicyOrReferenceIt(subject, xmlWriter);
                        return;
                    }
                }
            }
            selectAndProcessSubject(xmlWriter, clazz, scopeType, method.getOperationName());
        }
        else {
            selectAndProcessSubject(xmlWriter, clazz, scopeType, (String) null);
        }
        LOGGER.exiting("selectAndProcessSubject");
    }
    
    private void selectAndProcessSubject(final TypedXmlWriter xmlWriter, final Class clazz, final ScopeType scopeType, final String wsdlName) {
        LOGGER.entering("selectAndProcessSubject");
        if (subjects != null) {
            for (PolicySubject subject : subjects) { // iterate over all subjects in policy map
                if (isCorrectType(policyMap, subject, scopeType)) {
                    final Object wsdlSubject = subject.getSubject();
                    if (wsdlSubject != null && clazz.isInstance(wsdlSubject)) { // is it our class?
                        if (null == wsdlName) { // no name provided to check
                            writePolicyOrReferenceIt(subject, xmlWriter);
                        } else {
                            try {
                                final Method getNameMethod = clazz.getDeclaredMethod("getName");
                                if (stringEqualsToStringOrQName(wsdlName, getNameMethod.invoke(wsdlSubject))) {
                                    writePolicyOrReferenceIt(subject, xmlWriter);
                                }
                            } catch (NoSuchMethodException nsme) {
                                handleCheckingElementQNameWithReflectionException(nsme);
                            } catch (IllegalAccessException iae) {
                                handleCheckingElementQNameWithReflectionException(iae);
                            } catch (InvocationTargetException ite) {
                                handleCheckingElementQNameWithReflectionException(ite);
                            }
                        }
                    }
                }
            }
        }
        LOGGER.exiting("selectAndProcessSubject");
    }
    
    private static final boolean isCorrectType(final PolicyMap map, final PolicySubject subject, final ScopeType type) {
        switch (type) {
            case OPERATION :
                return ! (map.isInputMessageSubject(subject) || map.isOutputMessageSubject(subject));
            case INPUT_MESSAGE :
                return map.isInputMessageSubject(subject);
            case OUTPUT_MESSAGE :
                return map.isOutputMessageSubject(subject);
            default:
                return true;
        }
    }
    
    
    private boolean stringEqualsToStringOrQName(final String first, final Object second) {
        return (second instanceof QName) ? first.equals(((QName)second).getLocalPart()) : first.equals(second) ;
    }
    
    private void handleCheckingElementQNameWithReflectionException(final Exception e) {
        LOGGER.severe("handleCheckingElementQNameWithReflectionException", LocalizationMessages.WSP_001011_UNABLE_TO_CHECK_ELEMENT_NAME(), e);
        throw new WebServiceException(LocalizationMessages.WSP_001011_UNABLE_TO_CHECK_ELEMENT_NAME(), e);
    }
    
    
    /**
     * Adds a PolicyReference element that points to the policy of the element,
     * if the policy does not have any id or name. Writes policy inside the element otherwise.
     *
     * @param policy to be referenced or marshalled
     * @param element A TXW element to which we shall add the PolicyReference
     */
    private void writePolicyOrReferenceIt(final PolicySubject policySubject, final TypedXmlWriter xmlWriter) {
        try {
            final Policy policy = policySubject.getEffectivePolicy(merger);
            if (policy != null) {
                if (null != policy.getIdOrName()) {
                    final TypedXmlWriter policyReference = xmlWriter._element(PolicyConstants.POLICY_REFERENCE, TypedXmlWriter.class);
                    policyReference._attribute(PolicyConstants.POLICY_URI.getLocalPart(), '#' + policy.getIdOrName());
                } else {
                    final PolicyModelGenerator generator = PolicyModelGenerator.getGenerator();
                    final PolicySourceModel policyInfoset = generator.translate(policy);
                    marshaller.marshal(policyInfoset, xmlWriter);
                }
            }
        } catch (PolicyException pe) {
            LOGGER.severe("processPolicy", LocalizationMessages.WSP_001010_UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE(), pe);
            throw new WebServiceException(LocalizationMessages.WSP_001010_UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE(), pe);
        }
    }
}

