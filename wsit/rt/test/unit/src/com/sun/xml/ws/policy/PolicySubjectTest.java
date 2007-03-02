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

import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public class PolicySubjectTest extends TestCase {
    private Object subject = new Object();
    private PolicyMerger merger = PolicyMerger.getMerger();
    
    public PolicySubjectTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }           
    
    public void testCreatePolicySubjectWithNullSubjectMustThrowIAE() throws Exception {
        try {
            new PolicySubject(null, Policy.createNullPolicy());
            fail ("PolicySubject creation must throw IllegalArgumentException on 'null' subject");
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }
    
    public void testCreatePolicySubjectWithNullPolicyMustThrowIAE() throws Exception {
        try {
            Policy p = null;
            new PolicySubject(subject, p);
            fail ("PolicySubject creation must throw IllegalArgumentException on 'null' policy");
        } catch (IllegalArgumentException e) {
            // ok.
        }        
    }

    public void testCreatePolicySubjectWithNullPolicyCollcetionMustThrowIAE() throws Exception {
        try {
            Collection<Policy> c = null;
            new PolicySubject(subject, c);
            fail ("PolicySubject creation must throw IllegalArgumentException on 'null' policy collection");
        } catch (IllegalArgumentException e) {
            // ok.
        }        
    }
    
    public void testCreatePolicySubjectWithEmptyPolicyCollectionMustThrowIAE() throws Exception {
        try {
            Collection<Policy> c = new ArrayList<Policy>();
            new PolicySubject(subject, c);
            fail ("PolicySubject creation must throw IllegalArgumentException on empty policy collection");
        } catch (IllegalArgumentException e) {
            // ok.
        }        
    }
    
    public void testGetSubjectReturnsCorrectSubject() throws Exception {
        PolicySubject ps = new PolicySubject(subject, Policy.createNullPolicy());
        assertEquals("Subject used in constructor must equal to subject returned from getter.", subject, ps.getSubject());
    }
    
    public void testGetSubjectReturnsCorrectReference() throws Exception {
        StringBuffer subject = new StringBuffer('a');
        PolicySubject ps = new PolicySubject(subject, Policy.createNullPolicy());
        subject.append('b');
        assertEquals("Subject used in constructor must equal to subject returned from getter.", subject, ps.getSubject());
    }
    
    public void testAttachingNullPolicyThrowsIAE() throws Exception {
        PolicySubject ps = new PolicySubject(subject, Policy.createNullPolicy());        
        try {
        ps.attach(null);
        fail("Attaching a 'null' policy to the policyt subject must throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }
    
    public void testAttachingNoPolicyToAnyPolicyResultsInNoEffectivePolicy() throws Exception {
        PolicySubject ps = new PolicySubject(subject, Policy.createEmptyPolicy());        
        Policy noPolicy = Policy.createNullPolicy();
        ps.attach(noPolicy);
        
        assertEquals(noPolicy, ps.getEffectivePolicy(merger));
    }
    
}
