package com.sun.xml.ws.security.trust.util;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class TrustNamespacePrefixMapper extends NamespacePrefixMapper {

    
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        // I want this namespace to be mapped to "xsi"
        if( "http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri) )
            return "xsi";
         
        // I want the namespace foo to be the default namespace.
        if( "http://schemas.xmlsoap.org/ws/2005/02/trust".equals(namespaceUri) )
            return "wst";

        if( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd".equals(namespaceUri) )
            return "wsu";
        
        if( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd".equals(namespaceUri) )
            return "wsse";
       
        if( "http://schemas.xmlsoap.org/ws/2005/02/sc".equals(namespaceUri) )
            return "wssc";
        
        if( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd".equals(namespaceUri) )
            return "wsse";
        
        if( "http://schemas.xmlsoap.org/ws/2004/09/policy".equals(namespaceUri) )
            return "wsp";
        
        if( "http://www.w3.org/2005/08/addressing".equals(namespaceUri) )
            return "wsa";
        
        // otherwise I don't care. Just use the default suggestion, whatever it may be.
        return suggestion;
    }
}


