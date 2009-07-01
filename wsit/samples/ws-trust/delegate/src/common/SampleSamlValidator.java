/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.callback.SAMLAssertionValidator;
import com.sun.xml.wss.saml.util.SAMLUtil;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.AssertionUtil;
import com.sun.xml.wss.saml.Attribute;
import com.sun.xml.wss.saml.AttributeStatement;
import com.sun.xml.wss.saml.AuthenticationStatement;
import com.sun.xml.wss.saml.NameID;
import com.sun.xml.wss.saml.NameIdentifier;
import java.util.*;
import org.w3c.dom.Element;

import com.sun.xml.wss.saml.Subject;

/**
 *
 * @author jdg
 */
public class SampleSamlValidator implements SAMLAssertionValidator {

    public void validate(Element assertionEle) throws SAMLValidationException {
        String name = null;
        String actAs = null;
        String role = null;
        
        try{
            Assertion assertion = AssertionUtil.fromElement(assertionEle);

            Subject subject = null;
            NameID nameID = null;

            // SAML 2.0
            try {
                subject = assertion.getSubject();
            } catch (Exception ex){
                subject = null;
            }

            if (subject != null){
                nameID = subject.getNameId();
            }

            List<Object> statements = assertion.getStatements();
            for (Object s : statements){
                if (s instanceof AttributeStatement){
                    List<Attribute> samlAttrs = ((AttributeStatement)s).getAttributes();
                    for (Attribute samlAttr : samlAttrs){
                        String attrName = samlAttr.getName();
                        String attrValue = ((Element)samlAttr.getAttributes().iterator().next()).getFirstChild().getNodeValue();

                        if (attrName.equals("ActAs")){
                            actAs = attrValue;
                        } else if (attrName.equals("Role")){
                            role = attrValue;
                        }
                    }

                    // for SAML 1.0, 1.1
                    if (subject == null){
                        subject = ((AttributeStatement)s).getSubject();
                    }
                } else if (s instanceof AuthenticationStatement){
                    subject = ((AuthenticationStatement)s).getSubject();
                }
            }

            // Get the user identifier in the Subject:
            if (nameID != null){
                //SAML 2.0 case
                name = nameID.getValue();
            }else{
                // SAML 1.0, 1.1. case
                NameIdentifier nameIdentifier = subject.getNameIdentifier();
                if (nameIdentifier != null){
                    name = nameIdentifier.getValue();
                }
            }
        }catch (Exception ex){
            throw new SAMLValidationException(ex);
        }

        System.out.println("User: " + name + " accesses the Ping service, ");
        if (actAs != null){
            System.out.println("acting as: " + actAs);
        }
        System.out.println("with the role: " + role);
    }

    public void validate(XMLStreamReader assertion) throws SAMLValidationException {
        if (assertion != null){
            try {
                Element element = SAMLUtil.createSAMLAssertion(assertion);
                this.validate(element);
            } catch (XWSSecurityException ex) {
               throw new SAMLValidationException(ex);
            } catch (XMLStreamException ex) {
                 throw new SAMLValidationException(ex);
            }
        }
    }
}
