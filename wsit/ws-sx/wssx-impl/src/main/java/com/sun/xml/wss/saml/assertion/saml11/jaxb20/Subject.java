/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

/*
 * $Id: Subject.java,v 1.2 2010-10-21 15:38:00 snajper Exp $
 */

package com.sun.xml.wss.saml.assertion.saml11.jaxb20;

import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.NameID;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.NameIdentifierType;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.ObjectFactory;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.SubjectConfirmationType;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.SubjectType;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

/**
 * The <code>Subject</code> element specifies one or more subjects. It contains either or
both of the following elements:<code>NameIdentifier</code>;
An identification of a subject by its name and security domain.
<code>SubjectConfirmation</code>;
Information that allows the subject to be authenticated.

If a <code>Subject</code> element contains more than one subject specification,
the issuer is asserting that the surrounding statement is true for
all of the subjects specified. For example, if both a
<code>NameIdentifier</code> and a <code>SubjectConfirmation</code> element are
present, the issuer is asserting that the statement is true of both subjects
being identified. A <Subject> element SHOULD NOT identify more than one
principal.
*/
public class Subject extends SubjectType
    implements com.sun.xml.wss.saml.Subject {
    
    private NameIdentifier nameIdentifier;
    private SubjectConfirmation subjectConfirmation;
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /**
     * Constructs a Subject object from a <code>NameIdentifier</code>
     * object and a <code>SubjectConfirmation</code> object.
     *
     * @param nameIdentifier <code>NameIdentifier</code> object.
     * @param subjectConfirmation <code>SubjectConfirmation</code> object.
     * @exception SAMLException if it could not process the
     *            Element properly, implying that there is an error in the
     *            sender or in the element definition.
     */
    public Subject(
        NameIdentifier nameIdentifier, SubjectConfirmation subjectConfirmation)
        {
        ObjectFactory factory = new ObjectFactory();
        
        if ( nameIdentifier != null)
            getContent().add(factory.createNameIdentifier(nameIdentifier));
        
        if ( subjectConfirmation != null)
            getContent().add(factory.createSubjectConfirmation(subjectConfirmation));
    }

    public Subject(SubjectType subjectType){
        Iterator it = subjectType.getContent().iterator();
        
        while(it.hasNext()){
            Object obj = it.next();
            if(obj instanceof JAXBElement){
                Object object = ((JAXBElement)obj).getValue();
                if(object instanceof NameIdentifierType){
                    nameIdentifier = new NameIdentifier((NameIdentifierType)object);
                }else if(object instanceof SubjectConfirmationType){
                    subjectConfirmation = new SubjectConfirmation((SubjectConfirmationType)object);
                }
            }else{                
                if(obj instanceof NameIdentifierType){
                    nameIdentifier = new NameIdentifier((NameIdentifierType)obj);
                }else if(obj instanceof SubjectConfirmationType){
                    subjectConfirmation = new SubjectConfirmation((SubjectConfirmationType)obj);
                }
            }
        }
    }
    
    
    public NameIdentifier getNameIdentifier(){
        return nameIdentifier;
    }
    
    public NameID getNameId(){
        return null;
    }
    
    public SubjectConfirmation getSubjectConfirmation(){
        return subjectConfirmation;
    }
            
    /**
     * This constructor builds a subject element from an existing XML block
     * which has already been built into a DOM.
     *
     * @param subjectElement An Element representing DOM tree for Subject object
     * @exception SAMLException if it could not process the Element properly,
     *            implying that there is an error in the sender or in the
     *            element definition.
     */
    public static SubjectType fromElement(org.w3c.dom.Element element)
        throws SAMLException {
        try {
            JAXBContext jc = SAMLJAXBUtil.getJAXBContext();
                    
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (SubjectType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
}
