/*
 * To change this template, choose Tools | Templates | Licenses | Default License
 * and open the template in the editor.
 */

package com.sun.xml.ws.policy.sourcemodel.wspolicy;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public enum XmlToken {
    Policy("Policy", true),
    ExactlyOne("ExactlyOne", true),
    All("All", true),
    PolicyReference("PolicyReference", true),
    UsingPolicy("UsingPolicy", true),
    Name("Name", false),
    Optional("Optional", false),
    Ignorable("Ignorable", false),
    PolicyUris("PolicyURIs", false),
    Uri("URI", false),
    Digest("Digest", false),
    DigestAlgorithm("DigestAlgorithm", false),
    
    UNKNOWN("", true);
    
    
    
    /**
     * Resolves URI represented as a String into an enumeration value. If the URI 
     * doesn't represent any existing enumeration value, method returns {@code null}
     * 
     * @param uri
     * @return Enumeration value that represents given URI or {@code null} if 
     * no enumeration value exists for given URI.
     */
    public static XmlToken resolveToken(String name) {
        for (XmlToken token : XmlToken.values()) {
            if (token.toString().equals(name)) {
                return token;
            }
        }

        return UNKNOWN;
    }
    
    private String tokenName;
    private boolean element;
    
    private XmlToken(final String name, boolean element) {
        this.tokenName = name;
        this.element = element;
    }

    public boolean isElement() {
        return element;
    }        

    @Override
    public String toString() {
        return tokenName;
    }        
}
