/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.security.policy;

/**
 *
 * @author ashutosh.shahi@sun.com
 */
public enum SecurityPolicyVersion {
    
    
    SECURITYPOLICY200507("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy",
                         "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Once",
                         "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Never",
                         "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/AlwaysToRecipient",
                         "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Always"){
        
        @Override
        public String getNamespaceURI() {
            return namespaceUri;
        }

        @Override
        public String getIncludeTokenOnce() {
            return includeTokenOnce;
        }

        @Override
        public String getIncludeTokenNever() {
            return includeTokenNever;
        }

        @Override
        public String getIncludeTokenAlwaysToRecipient() {
            return includeTokenAlwaysToRecipient;
        }

        @Override
        public String getIncludeTokenAlways() {
            return includeTokenAlways;
        }
        
    },
    SECURITYPOLICY12NS("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702",
                       "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702/IncludeToken/Once",
                       "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702/IncludeToken/Never",
                       "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702/IncludeToken/AlwaysToRecipient",
                       "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702/IncludeToken/Always"){
        
        @Override
        public String getNamespaceURI() {
            return namespaceUri;
        }

        @Override
        public String getIncludeTokenOnce() {
            return includeTokenOnce;
        }

        @Override
        public String getIncludeTokenNever() {
            return includeTokenNever;
        }

        @Override
        public String getIncludeTokenAlwaysToRecipient() {
            return includeTokenAlwaysToRecipient;
        }

        @Override
        public String getIncludeTokenAlways() {
            return includeTokenAlways;
        }
    },
    
    SECURITYPOLICY200512("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200512",
                       "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200512/IncludeToken/Once",
                       "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200512/IncludeToken/Never",
                       "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200512/IncludeToken/AlwaysToRecipient",
                       "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200512/IncludeToken/Always"){
        
        @Override
        public String getNamespaceURI() {
            return namespaceUri;
        }

        @Override
        public String getIncludeTokenOnce() {
            return includeTokenOnce;
        }

        @Override
        public String getIncludeTokenNever() {
            return includeTokenNever;
        }

        @Override
        public String getIncludeTokenAlwaysToRecipient() {
            return includeTokenAlwaysToRecipient;
        }

        @Override
        public String getIncludeTokenAlways() {
            return includeTokenAlways;
        }
    },

    MS_SECURITYPOLICY200507("http://schemas.microsoft.com/ws/2005/07/securitypolicy",
                         "http://schemas.microsoft.com/ws/2005/07/securitypolicy/IncludeToken/Once",
                         "http://schemas.microsoft.com/ws/2005/07/securitypolicy/IncludeToken/Never",
                         "http://schemas.microsoft.com/ws/2005/07/securitypolicy/IncludeToken/AlwaysToRecipient",
                         "http://schemas.microsoft.com/ws/2005/07/securitypolicy/IncludeToken/Always"){
        
        @Override
        public String getNamespaceURI() {
            return namespaceUri;
        }

        @Override
        public String getIncludeTokenOnce() {
            return includeTokenOnce;
        }

        @Override
        public String getIncludeTokenNever() {
            return includeTokenNever;
        }

        @Override
        public String getIncludeTokenAlwaysToRecipient() {
            return includeTokenAlwaysToRecipient;
        }

        @Override
        public String getIncludeTokenAlways() {
            return includeTokenAlways;
        }
        
    };
    
    
    public final String namespaceUri;
    
    public final String includeTokenOnce;
    
    public final String includeTokenNever;
    
    public final String includeTokenAlwaysToRecipient;
    
    public final String includeTokenAlways;
    
    public abstract String getNamespaceURI();
    
    public abstract String getIncludeTokenOnce();
    
    public abstract String getIncludeTokenNever();
    
    public abstract String getIncludeTokenAlwaysToRecipient();
    
    public abstract String getIncludeTokenAlways();
    
    /** Creates a new instance of SecurityPolicyVersion */
    private SecurityPolicyVersion(String nsUri, String includeOnce, 
            String includeNever, String includeAlwaysToRecipient,
            String includeAlways) {
        this.namespaceUri = nsUri;
        this.includeTokenOnce = includeOnce;
        this.includeTokenNever = includeNever;
        this.includeTokenAlwaysToRecipient = includeAlwaysToRecipient;
        this.includeTokenAlways = includeAlways;
    }
    
}
