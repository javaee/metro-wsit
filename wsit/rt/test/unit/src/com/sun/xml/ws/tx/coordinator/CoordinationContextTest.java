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

package com.sun.xml.ws.tx.coordinator;

import com.sun.corba.se.impl.orbutil.closure.Constant;
import com.sun.xml.ws.tx.common.Constants;
import com.sun.xml.ws.tx.webservice.member.coord.CoordinationContext;
import javax.xml.ws.EndpointReference;
import junit.framework.TestCase;

/**
 *
 * @author Ryan.Shoemaker@Sun.COM
 */
public class CoordinationContextTest extends TestCase {
    
    /** Creates a new instance of CoordinationContextTest */
    public CoordinationContextTest(String testname) {
        super(testname);
    }
    
    CoordinationContextInterface c;
    
    protected void setUp() throws Exception {
        // create a context to poke with a stick
        c = ContextFactory.createTestContext(Constants.WSAT_2004_PROTOCOL, 0l);
    }
    
    public void testContextFactory1() throws Exception {
        ContextFactory.createTestContext(Constants.WSAT_2004_PROTOCOL, 0l);
    }
    
    public void testContextFactory2() throws Exception {
        try {
            ContextFactory.createTestContext(Constants.WSAT_OASIS_NSURI, 0l);
        } catch(UnsupportedOperationException uoe) {
            return; // pass - expected exception
        }
        fail("Was expecting UnsupportedOperationException");
    }

    public void testContextType() throws Exception {
        assertEquals(Constants.WSAT_SOAP_NSURI, c.getCoordinationType());
    }
    
    public void testContextExpiration1() throws Exception {
        assertEquals(0l, c.getExpires());
    }
    
    public void testContextExpiration2() throws Exception {
        CoordinationContext200410 c = new CoordinationContext200410();
        c.setExpires(-1);
        assert(c.getExpires() == 0l);
    }
    
    public void testContextIdPrefix() throws Exception {
        assert(c.getIdentifier().startsWith("uuid:WSCOOR-SUN-"));
    }
    
    public void testContextRegServiceEPR() throws Exception {
        EndpointReference epr = c.getRegistrationService();
        // TODO: figure out how to assert that the EPR is our registration
        //       service EPR
    }
    
    public void testContextGetAttrs() throws Exception {
        assert(c.getOtherAttributes().size()==0);  // should initially be empty
    }
    
    public void testContextRootReg() throws Exception {
        assertNull(c.getRootRegistrationService()); // should initially be null
    }
    
    public void testContextValue() throws Exception {
        assert(c.getValue() instanceof CoordinationContext200410);
    }
    
    public void testContextSetters() throws Exception {
        CoordinationContext200410 c = new CoordinationContext200410();
        c.setIdentifier("foo");
        c.setExpires(5000);
        c.setRegistrationService(null);
        c.setRootCoordinatorRegistrationService(null);
    }
    
}
