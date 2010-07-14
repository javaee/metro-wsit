/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.policy;

import java.util.ArrayList;
import java.util.Collection;
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
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
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
