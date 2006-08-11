/*
 * STSImpl.java
 *
 *
 */


package wstrust.scenario5.sts;

import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelTranslator;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelUnmarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustContract;
import com.sun.xml.ws.security.trust.WSTrustFactory;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.impl.STSConfiguration;
import com.sun.xml.ws.security.trust.impl.TrustSPMetadata;
import com.sun.xml.ws.security.trust.sts.BaseSTSImpl;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import javax.xml.namespace.QName;

import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;

import java.util.List;
import javax.xml.transform.Source;
import javax.xml.ws.handler.MessageContext;

@ServiceMode(value=Service.Mode.PAYLOAD)
@WebServiceProvider(wsdlLocation="WEB-INF/wsdl/sts.wsdl")
public class STSImpl extends BaseSTSImpl implements Provider<Source>{
    @Resource
    protected WebServiceContext context;
    
    public Source invoke(Source rstElement){
        return super.invoke(rstElement);
    }
    
    protected MessageContext getMessageContext() {        
        MessageContext msgCtx = context.getMessageContext(); 
        return msgCtx;
    }
    
}
