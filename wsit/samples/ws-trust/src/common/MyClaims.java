/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package common;

import com.sun.xml.ws.api.security.trust.Claims;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *  <wst:Claims Dialect=”http://schemas.xmlsoap.org/ws/2005/05/identity”
 *       xmlns:wst="http://docs.oasis-open.org/ws-sx/ws-trust/200512"
 *       xmlns:ic="http://schemas.xmlsoap.org/ws/2005/05/identity">
 *      <ic:ClaimType Uri=”http://schemas.xmlsoap.org/ws/2005/05/identity/claims/locality”/>
 *      <ic:ClaimType Uri=”http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role”/>
 *  </wst:Claims>
 * @author jdg
 */
public class MyClaims implements Claims {

    public static final String ROLE = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role";
    public static final String LOCALITY = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/locality";
    
    private List<Object> supportingProps = new ArrayList<Object>();
    private String dialect = "http://schemas.xmlsoap.org/ws/2005/05/identity";
    private Map<QName, String> otherAttrs = new HashMap<QName, String>();
    private List<Object> any = new ArrayList<Object>();

    Document doc;

    public MyClaims(){
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            doc = builder.newDocument();

            Element claims = doc.createElementNS("http://docs.oasis-open.org/ws-sx/ws-trust/200512", "Claims");
            doc.appendChild(claims);
        }catch (Exception ex){
            
        }
    }

    public MyClaims(Claims claims){
        this.dialect = claims.getDialect();
        this.any.addAll(claims.getAny());
        this.otherAttrs.putAll(claims.getOtherAttributes());

    }

    public void addClaimType(String claimType){
        Element ct = doc.createElementNS("http://schemas.xmlsoap.org/ws/2005/05/identity", "ClaimType");
        ct.setPrefix("ic");
        ct.setAttribute("xmlns:ic", "http://schemas.xmlsoap.org/ws/2005/05/identity");
        ct.setAttribute("Uri", claimType);
        doc.getDocumentElement().appendChild(ct);

        any.add(ct);
    }

    public List<String> getClaimsTypes(){
        List<String> claimTypes = new ArrayList<String>();
        for (Object ctObj: any){
            Element ctElement = (Element)ctObj;
            String claimType = ctElement.getAttribute("Uri");
            claimTypes.add(claimType);
        }

        return claimTypes;
    }

    public List<Object> getAny() {
        return any;
    }

    public String getDialect() {
        return dialect;
    }

    public Map<QName, String> getOtherAttributes() {
        return otherAttrs;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public List<Object> getSupportingProperties() {
        return supportingProps;
    }
}
