/*
 * STSImpl.java
 *
 *
 */


package wstrust.scenario5n10.sts;

import com.sun.xml.ws.security.trust.sts.BaseSTSImpl;
import javax.annotation.Resource;

import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;

import javax.xml.transform.Source;
import javax.xml.ws.handler.MessageContext;

@ServiceMode(value=Service.Mode.PAYLOAD)
@WebServiceProvider(wsdlLocation="WEB-INF/wsdl/sts.wsdl")
@javax.xml.ws.BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
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
