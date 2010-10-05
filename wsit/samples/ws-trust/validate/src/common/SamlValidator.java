/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import com.sun.xml.ws.api.security.trust.Status;
import com.sun.xml.ws.api.security.trust.client.IssuedTokenManager;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.impl.client.DefaultSTSIssuedTokenConfiguration;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.callback.SAMLAssertionValidator;
import com.sun.xml.wss.saml.util.SAMLUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Element;

/**
 *
 * @author TOSHIBA USER
 */
public class SamlValidator implements SAMLAssertionValidator {

    public void validate(Element assertion) throws SAMLValidationException {
        if (assertion != null){   
            String stsEndpoint = "http://localhost:8080/jaxws-fs-sts/sts/validate";
            String stsMexAddress = "http://localhost:8080/jaxws-fs-sts/sts/mex";
            DefaultSTSIssuedTokenConfiguration config = new DefaultSTSIssuedTokenConfiguration(
                        stsEndpoint, stsMexAddress);
            Status status = null;
            try{
                IssuedTokenManager manager = IssuedTokenManager.getInstance();

                IssuedTokenContext ctx = manager.createIssuedTokenContext(config, null);
                ctx.setTarget(new GenericToken(assertion));
                manager.validateIssuedToken(ctx);
                status = (Status)ctx.getOtherProperties().get(IssuedTokenContext.STATUS);
            }catch(Exception ex){
                throw new SAMLValidationException(ex);
            }
            
            if (!status.isValid()){
                throw new SAMLValidationException(status.getReason());
            }
        }
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
