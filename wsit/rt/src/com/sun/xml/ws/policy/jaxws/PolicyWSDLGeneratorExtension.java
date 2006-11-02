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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * Marshals the contents of a policy map to WSDL.
 */
public class PolicyWSDLGeneratorExtension extends WSDLGeneratorExtension {
    
    private final static PolicyLogger logger = PolicyLogger.getLogger(PolicyWSDLGeneratorExtension.class);
    
    private PolicyMap policyMap;
    private SEIModel seiModel;
    private final Collection<PolicySubject> subjects = new LinkedList<PolicySubject>();
    private Class endpointClass;
    
    private PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller(true);
    private PolicyMerger merger = PolicyMerger.getMerger();
    
    public void start(WSDLGenExtnContext context) {
        logger.entering("start");
        try {
            this.seiModel = context.getModel();
            this.endpointClass = context.getEndpointClass();
            PolicyMapUpdateProvider[] policyMapUpdateProviders = PolicyUtils.ServiceProvider.load(PolicyMapUpdateProvider.class);
            PolicyMapExtender[] extenders = new PolicyMapExtender[policyMapUpdateProviders.length];
            for (int i=0; i<extenders.length; i++) {
                extenders[i] = PolicyMapExtender.createPolicyMapExtender();
            }
            
            String configId = context.getEndpointClass().getName();
            policyMap = PolicyConfigParser.parse(configId, context.getContainer(), extenders);
            
            if (policyMap!= null) {
                context.getRoot()._namespace(PolicyConstants.POLICY_NAMESPACE_URI, PolicyConstants.POLICY_NAMESPACE_PREFIX);
                WSBinding binding = context.getBinding();
                for (int i=0; i<policyMapUpdateProviders.length; i++) {
                    policyMapUpdateProviders[i].update(extenders[i], policyMap, seiModel, binding);
                    extenders[i].disconnect();
                }
            }
        } catch (PolicyException e) {
            logger.fine("start", LocalizationMessages.FAILED_TO_READ_WSIT_CFG(), e);
        } finally {
            logger.exiting("start");
        }
    }
    
    public void addDefinitionsExtension(TypedXmlWriter definitions) {
        try {
            logger.entering("addDefinitionsExtension");
            if (policyMap != null) {
                subjects.addAll(policyMap.getPolicySubjects());
                boolean usingPolicy = false;
                PolicyModelGenerator generator = null;
                Set<Policy> policiesWritten = null;
                for (PolicySubject subject : subjects) {
                    Object wsdlSubject = subject.getSubject();
                    if (wsdlSubject != null) {
                        if (!usingPolicy) {
                            definitions._element(PolicyConstants.USING_POLICY, TypedXmlWriter.class);
                            usingPolicy = true;
                            policiesWritten = new HashSet<Policy>();
                            generator = PolicyModelGenerator.getGenerator();
                        }
                        Policy policy = subject.getEffectivePolicy(merger);
                        if ((null != policy.getIdOrName()) && (!policiesWritten.contains(policy))) {
                            PolicySourceModel policyInfoset = generator.translate(policy);
                            marshaller.marshal(policyInfoset, definitions);
                            policiesWritten.add(policy);
                        }
                    } else {
                        logger.fine("addDefinitionsExtension", LocalizationMessages.NOT_MARSHALLING_WSDL_SUBJ_NULL(subject));
                    }
                }
            } else {
                logger.fine("addDefinitionsExtension", LocalizationMessages.NOT_MARSHALLING_ANY_POLICIES_POLICY_MAP_IS_NULL());
            }
        } catch (PolicyException e) {
            logger.severe("addDefinitionsExtension", LocalizationMessages.FAILED_TO_MARSHALL_POLICIES(), e);
            throw new WebServiceException(e);
        } finally {
            logger.exiting("addDefinitionsExtension");
        }
    }
    
    public void addServiceExtension(TypedXmlWriter service) {
        logger.entering("addServiceExtension");
        String serviceName = ((null != seiModel) && (null != endpointClass)) ?
            WSEndpoint.getDefaultServiceName(endpointClass).getLocalPart() :
            null;
        selectAndProcessSubject(service, WSDLService.class, null);
        logger.exiting("addServiceExtension");
    }
    
    public void addPortExtension(TypedXmlWriter port) {
        logger.entering("addPortExtension");
        String portName = ((null != seiModel) && (null != endpointClass)) ?
            WSEndpoint.getDefaultPortName(seiModel.getServiceQName(), endpointClass).getLocalPart() :
            null;
        selectAndProcessSubject(port, WSDLPort.class, null);
        logger.exiting("addPortExtension");
    }
    
    public void addPortTypeExtension(TypedXmlWriter portType) {
        logger.entering("addPortTypeExtension");
        String portTypeName = (null != seiModel) ? seiModel.getPortTypeName().getLocalPart() : null;
        selectAndProcessSubject(portType, WSDLPortType.class, portTypeName);
        logger.exiting("addPortTypeExtension");
    }
    
    public void addBindingExtension(TypedXmlWriter binding) {
        logger.entering("addBindingExtension");
        String bindingName = ((null != seiModel) && (null != endpointClass))?
            (WSEndpoint.getDefaultPortName(seiModel.getServiceQName(), endpointClass).getLocalPart() + "Binding") :
            null;
        selectAndProcessSubject(binding, WSDLBoundPortType.class, bindingName);
        logger.exiting("addBindingExtension");
    }
    
    public void addOperationExtension(TypedXmlWriter operation, JavaMethod method) {
        logger.entering("addOperationExtension");
        String operationName = (null != method) ? method.getOperationName() : null;
        selectAndProcessSubject(operation, WSDLOperation.class, operationName);
        logger.exiting("addOperationExtension");
    }
    
    public void addBindingOperationExtension(TypedXmlWriter operation, JavaMethod method) {
        logger.entering("addBindingOperationExtension");
        String operationName = (null != method) ? method.getOperationName() : null;
        selectAndProcessSubject(operation, WSDLBoundOperation.class, operationName);
        logger.exiting("addBindingOperationExtension");
    }
    
    public void addInputMessageExtension(TypedXmlWriter message, JavaMethod method) {
        logger.entering("addInputMessageExtension");
        String messageName = (null != method) ? method.getRequestMessageName() : null;
        selectAndProcessSubject(message, WSDLMessage.class, messageName);
        logger.exiting("addInputMessageExtension");
    }
    
    public void addOutputMessageExtension(TypedXmlWriter message, JavaMethod method) {
        logger.entering("addOutputMessageExtension");
        String messageName = (null != method) ? method.getResponseMessageName() : null;
        selectAndProcessSubject(message, WSDLMessage.class, messageName);
        logger.exiting("addOutputMessageExtension");
    }
    
    public void addFaultMessageExtension(TypedXmlWriter message, JavaMethod method, CheckedException ce) {
        logger.entering("addFaultMessageExtension");
        String messageName = (null != ce) ? ce.getMessageName() : null;
        selectAndProcessSubject(message, WSDLMessage.class, messageName);
        logger.exiting("addFaultMessageExtension");
    }
    
    public void addOperationInputExtension(TypedXmlWriter input, JavaMethod method) {
        logger.entering("addOperationInputExtension");
        String messageName = (null != method) ? method.getRequestMessageName() : null;
        selectAndProcessSubject(input, WSDLInput.class, messageName);
        logger.exiting("addOperationInputExtension");
    }
    
    public void addOperationOutputExtension(TypedXmlWriter output, JavaMethod method) {
        logger.entering("addOperationOutputExtension");
        String messageName = (null != method) ? method.getResponseMessageName() : null;
        selectAndProcessSubject(output, WSDLOutput.class, messageName);
        logger.exiting("addOperationOutputExtension");
    }
    
    public void addOperationFaultExtension(TypedXmlWriter fault, JavaMethod method, CheckedException ce) {
        logger.entering("addOperationFaultExtension");
        String messageName = (null != ce) ? ce.getMessageName() : null;
        selectAndProcessSubject(fault, WSDLFault.class, messageName);
        logger.exiting("addOperationFaultExtension");
    }
    
    public void addBindingOperationInputExtension(TypedXmlWriter input, JavaMethod method) {
        logger.entering("addBindingOperationInputExtension");
        String messageName = (null != method) ? method.getOperationName() : null;
        selectAndProcessSubject(input, WSDLBoundOperation.class, messageName);
        logger.exiting("addBindingOperationInputExtension");
    }
    
    public void addBindingOperationOutputExtension(TypedXmlWriter output, JavaMethod method) {
        logger.entering("addBindingOperationOutputExtension");
        String messageName = (null != method) ? method.getOperationName() : null;
        selectAndProcessSubject(output, WSDLBoundOperation.class, messageName);
        logger.exiting("addBindingOperationOutputExtension");
    }
    
    public void addBindingOperationFaultExtension(TypedXmlWriter fault, JavaMethod method, CheckedException ce) {
        logger.entering("addBindingOperationFaultExtension");
        String messageName = (null != ce) ? ce.getMessageName() : null;;
        selectAndProcessSubject(fault, WSDLFault.class, messageName);
        logger.exiting("addBindingOperationFaultExtension");
    }
    
    private void selectAndProcessSubject(TypedXmlWriter xmlWriter, Class clazz, String wsdlName) {
        logger.entering("selectAndProcessSubject");
        if (subjects != null) {
            for (PolicySubject subject : subjects) { // iterate over all subjects in policy map
                Object wsdlSubject = subject.getSubject();
                if (wsdlSubject != null && clazz.isInstance(wsdlSubject)) { // is it our class?
                    if (null == wsdlName) { // no name provided to check
                        writePolicyOrReferenceIt(subject, xmlWriter);
                    } else {
                        try {
                            Method getNameMethod = clazz.getDeclaredMethod("getName");
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
        logger.exiting("selectAndProcessSubject");
    }

    private boolean stringEqualsToStringOrQName(String first, Object second) {
        return (second instanceof QName) ? first.equals(((QName)second).getLocalPart()) : first.equals(second) ;
    }
    
    private void handleCheckingElementQNameWithReflectionException(Exception e) {
        logger.severe("handleCheckingElementQNameWithReflectionException",
                LocalizationMessages.UNABLE_TO_CHECK_ELEMENT_NAME(), e);
        throw new WebServiceException(e);
    }
    
    
    /**
     * Adds a PolicyReference element that points to the policy of the element,
     * if the policy does not have any id or name. Writes policy inside the element otherwise.
     *
     * @param policy to be referenced or marshalled
     * @param element A TXW element to which we shall add the PolicyReference
     */
    private void writePolicyOrReferenceIt(PolicySubject policySubject, TypedXmlWriter xmlWriter) {
        try {
            Policy policy = policySubject.getEffectivePolicy(merger);
            if (policy != null) {
                if (null != policy.getIdOrName()) {
                    TypedXmlWriter policyReference = xmlWriter._element(PolicyConstants.POLICY_REFERENCE, TypedXmlWriter.class);
                    policyReference._attribute(PolicyConstants.POLICY_URI.getLocalPart(), '#' + policy.getIdOrName());
                } else {
                    PolicyModelGenerator generator = PolicyModelGenerator.getGenerator();
                    PolicySourceModel policyInfoset = generator.translate(policy);
                    marshaller.marshal(policyInfoset, xmlWriter);
                }
            }
        } catch (PolicyException pe) {
            logger.severe("processPolicy", LocalizationMessages.UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE(), pe);
            throw new WebServiceException(pe);
        }
    }
    
}