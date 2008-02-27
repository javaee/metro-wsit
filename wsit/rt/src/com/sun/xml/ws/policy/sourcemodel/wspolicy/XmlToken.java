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
