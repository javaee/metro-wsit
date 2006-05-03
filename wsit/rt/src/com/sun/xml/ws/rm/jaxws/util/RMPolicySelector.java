/*
 * RMPolicySelector.java
 *
 * @author Mike Grogan
 * Created on April 13, 2006, 11:40 AM
 *
 */

package com.sun.xml.ws.rm.jaxws.util;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.spi.PolicySelector; 
import java.util.Iterator;
import static com.sun.xml.ws.rm.RMConstants.version;
import static com.sun.xml.ws.rm.RMConstants.sunVersion;
import static com.sun.xml.ws.rm.RMConstants.microsoftVersion;
/**
 *
 */
public class RMPolicySelector extends PolicySelector {

    private static final ArrayList<QName> supportedAssertions = new ArrayList<QName>();
        
    private static final QName rmQName = new QName(version, "RMAssertion");
    private static final QName flowQName = new QName(microsoftVersion, "RmFlowControl");
    static {
        supportedAssertions.add(new QName(version, "AcknowledgementInterval"));
        supportedAssertions.add(new QName(version, "InactivityTimeout"));
        supportedAssertions.add(new QName(microsoftVersion, "MaxReceiveBufferSize"));
        supportedAssertions.add(new QName(sunVersion, "Ordered"));
    }
    
    public RMPolicySelector() {
        super(supportedAssertions);
    }
        
    public boolean test(PolicyAssertion assertion) {
        QName qname = assertion.getName();
        if (qname.equals(flowQName) || qname.equals(rmQName)){
            Iterator<PolicyAssertion> i = assertion.getNestedAssertionsIterator();
            while (i.hasNext()) {
                if (! supportedAssertions.contains(i.next().getName())) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

}
