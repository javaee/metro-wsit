/*
 * $Id: Subject.java,v 1.1.2.2 2010-07-14 14:09:04 m_potociar Exp $
 */

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

package com.sun.xml.wss.saml.assertion.saml20.jaxb20;

import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.NameIdentifier;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.NameIDType;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.ObjectFactory;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.SubjectConfirmationType;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.SubjectType;
import com.sun.xml.wss.saml.util.SAML20JAXBUtil;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

/**
 * The <code>Subject</code> element specifies one or more subjects. It contains either or
both of the following elements:<code>NameID</code>;
An identification of a subject by its name and security domain.
<code>SubjectConfirmation</code>;
Information that allows the subject to be authenticated.

If a <code>Subject</code> element contains more than one subject specification,
the issuer is asserting that the surrounding statement is true for
all of the subjects specified. For example, if both a
<code>NameID</code> and a <code>SubjectConfirmation</code> element are
present, the issuer is asserting that the statement is true of both subjects
being identified. A <Subject> element SHOULD NOT identify more than one
principal.
*/
public class Subject extends SubjectType implements com.sun.xml.wss.saml.Subject {
    
    private NameID nameId = null;
    private SubjectConfirmation subjectConfirmation = null;
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);


    
    /**
     * Constructs a Subject object from a <code>NameID</code>
     * object and a <code>SubjectConfirmation</code> object.
     *
     * @param NameID <code>NameID</code> object.
     * @param subjectConfirmation <code>SubjectConfirmation</code> object.
     * @exception SAMLException if it could not process the
     *            Element properly, implying that there is an error in the
     *            sender or in the element definition.
     */
    public Subject(NameID nameId, SubjectConfirmation subjectConfirmation){
        ObjectFactory factory = new ObjectFactory();
        
        if ( nameId != null)
            getContent().add(factory.createNameID(nameId));
        
        if ( subjectConfirmation != null)
            getContent().add(factory.createSubjectConfirmation(subjectConfirmation));
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
    public static SubjectType fromElement(org.w3c.dom.Element element) throws SAMLException {
        try {
            JAXBContext jc = SAML20JAXBUtil.getJAXBContext();
                    
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (SubjectType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
    
    public Subject(SubjectType subjectType){
        this.content = subjectType.getContent();
        Iterator it = subjectType.getContent().iterator();
        
        while(it.hasNext()){
            Object obj = it.next();
            if(obj instanceof JAXBElement){
                Object object = ((JAXBElement)obj).getValue();
                if(object instanceof NameIDType){
                    nameId = new NameID((NameIDType)object);
                }else if(object instanceof SubjectConfirmationType){
                    subjectConfirmation = new SubjectConfirmation((SubjectConfirmationType)object);
                }
            }else{                
                if(obj instanceof NameIDType){
                    nameId = new NameID((NameIDType)obj);
                }else if(obj instanceof SubjectConfirmationType){
                    subjectConfirmation = new SubjectConfirmation((SubjectConfirmationType)obj);
                }
            }
        }
    }
    
    public NameIdentifier getNameIdentifier(){
        return null;
    }
    
    public NameID getNameId(){
        return nameId;
    }
    
    public SubjectConfirmation getSubjectConfirmation(){
        return subjectConfirmation;
    }
}