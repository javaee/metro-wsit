package com.sun.xml.ws.addressing.policy;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.jaxws.spi.PolicyMapUpdateProvider;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;

import java.util.ArrayList;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.AddressingFeature;

/**
 * Generate an wsaw:UsingAddressing policy assertion and updates the PolicyMap if AddressingFeature is enabled.
 * This is done in WSIT just for backwards compatibility of WSIT for interoperability with old clients.
 * JAX-WS generates wsam:Addressing assertion for the same when Addressing is enabled.
 *
 *
 * @author Rama Pulavarthi
 */
public class WsawAddressingMapUpdateProvider implements PolicyMapUpdateProvider {

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(WsawAddressingMapUpdateProvider.class);

    private static final class AddressingAssertion extends PolicyAssertion {

        AddressingAssertion(AssertionData assertionData) {
            super(assertionData, null);
        }
    }


    /**
     * Puts an addressing policy into the PolicyMap if the addressing feature was set.
     */
    public void update(final PolicyMapExtender policyMapMutator, final PolicyMap policyMap, final SEIModel model, final WSBinding wsBinding)
            throws PolicyException {
        LOGGER.entering(policyMapMutator, policyMap, model, wsBinding);

        if (policyMap != null) {
            final AddressingFeature addressingFeature = wsBinding.getFeature(AddressingFeature.class);
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("addressingFeature = " + addressingFeature);
            }
            if ((addressingFeature != null) && addressingFeature.isEnabled()) {
                //add wsaw:UsingAddressing for WSIT compatibility.
                addWsawUsingAddressingForCompatibility(policyMapMutator, policyMap, model, addressingFeature);

            }
        } // endif policy map not null
        LOGGER.exiting();
    }


    private void addWsawUsingAddressingForCompatibility(PolicyMapExtender policyMapMutator, PolicyMap policyMap, SEIModel model, AddressingFeature addressingFeature) throws PolicyException {
        final AddressingVersion addressingVersion = AddressingVersion.fromFeature(addressingFeature);
        final QName usingAddressing = new QName(addressingVersion.policyNsUri, "UsingAddressing");
        final PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(model.getServiceQName(), model.getPortName());
        final Policy existingPolicy = policyMap.getEndpointEffectivePolicy(endpointKey);
        if ((existingPolicy == null) || !existingPolicy.contains(usingAddressing)) {
            final QName bindingName = model.getBoundPortTypeName();
            final Policy addressingPolicy = createWsawAddressingPolicy(bindingName, usingAddressing, addressingFeature.isRequired());
            final PolicySubject addressingPolicySubject = new PolicySubject(bindingName, addressingPolicy);
            final PolicyMapKey aKey = PolicyMap.createWsdlEndpointScopeKey(model.getServiceQName(), model.getPortName());
            policyMapMutator.putEndpointSubject(aKey, addressingPolicySubject);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Added addressing policy with ID \"" + addressingPolicy.getIdOrName() + "\" to binding element \"" + bindingName + "\"");
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Addressing policy exists already, doing nothing");
            }
        }
    }

    /**
     * Create a policy with an WSAW UsingAddressing assertion.
     *
     * @param bindingName   The wsdl:binding element name. Used to generate a (locally) unique ID for the policy.
     * @param assertionName The fully qualified name of the addressing policy assertion.
     * @param isRequired    True, if the addressing feature was set to required, false otherwise.
     * @return A policy that contains one policy assertion that corresponds to the given assertion name.
     */
    private Policy createWsawAddressingPolicy(final QName bindingName, final QName assertionName, final boolean isRequired) {
        final ArrayList<AssertionSet> assertionSets = new ArrayList<AssertionSet>(1);
        final ArrayList<PolicyAssertion> assertions = new ArrayList<PolicyAssertion>(1);
        final AssertionData addressingData =
                AssertionData.createAssertionData(assertionName);
        if (!isRequired) {
            addressingData.setOptionalAttribute(true);
        }
        assertions.add(new AddressingAssertion(addressingData));
        assertionSets.add(AssertionSet.createAssertionSet(assertions));
        return Policy.createPolicy(null, bindingName.getLocalPart() + "_Wsaw_Addressing_Policy", assertionSets);
    }
}
