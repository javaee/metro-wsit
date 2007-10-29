/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Marek Potociar
 */
final class PolicyReferenceData {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyReferenceData.class);
    
    private static final URI DEFAULT_DIGEST_ALGORITHM_URI;
    private static final URISyntaxException CLASS_INITIALIZATION_EXCEPTION;
    static {
        URISyntaxException tempEx = null;
        URI tempUri = null;
        try {
            tempUri = new URI("http://schemas.xmlsoap.org/ws/2004/09/policy/Sha1Exc");
        } catch (URISyntaxException e) {
            tempEx = e;
        } finally {
            DEFAULT_DIGEST_ALGORITHM_URI = tempUri;
            CLASS_INITIALIZATION_EXCEPTION = tempEx;
        }
    }
    
    private final URI referencedModelUri;
    private final String digest;
    private final URI digestAlgorithmUri;
    
    /** Creates a new instance of PolicyReferenceData */
    public PolicyReferenceData(URI referencedModelUri) {
        this.referencedModelUri = referencedModelUri;
        this.digest = null;
        this.digestAlgorithmUri = null;
    }
    
    public PolicyReferenceData(URI referencedModelUri, String expectedDigest, URI usedDigestAlgorithm) {
        if (CLASS_INITIALIZATION_EXCEPTION != null) {
            throw LOGGER.logSevereException(new IllegalStateException(LocalizationMessages.WSP_0015_UNABLE_TO_INSTANTIATE_DIGEST_ALG_URI_FIELD(), CLASS_INITIALIZATION_EXCEPTION));
        }
        
        if (usedDigestAlgorithm != null && expectedDigest == null) {
            throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_0072_DIGEST_MUST_NOT_BE_NULL_WHEN_ALG_DEFINED()));
        }
        
        this.referencedModelUri = referencedModelUri;
        if (expectedDigest == null) {
            this.digest = null;
            this.digestAlgorithmUri = null;
        } else {
            this.digest = expectedDigest;
            
            if (usedDigestAlgorithm == null) {
                this.digestAlgorithmUri = DEFAULT_DIGEST_ALGORITHM_URI;
            } else {
                this.digestAlgorithmUri = usedDigestAlgorithm;
            }
        }
    }
    
    public URI getReferencedModelUri() {
        return referencedModelUri;
    }
    
    public String getDigest() {
        return digest;
    }
    
    public URI getDigestAlgorithmUri() {
        return digestAlgorithmUri;
    }
    
    /**
     * An {@code Object.toString()} method override.
     */
    @Override
    public String toString() {
        return toString(0, new StringBuffer()).toString();
    }
    
    /**
     * A helper method that appends indented string representation of this instance to the input string buffer.
     *
     * @param indentLevel indentation level to be used.
     * @param buffer buffer to be used for appending string representation of this instance
     * @return modified buffer containing new string representation of the instance
     */
    public StringBuffer toString(final int indentLevel, final StringBuffer buffer) {
        final String indent = PolicyUtils.Text.createIndent(indentLevel);
        final String innerIndent = PolicyUtils.Text.createIndent(indentLevel + 1);
        
        buffer.append(indent).append("reference data {").append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("referenced policy model URI = '").append(referencedModelUri).append('\'').append(PolicyUtils.Text.NEW_LINE);
        if (digest == null) {
            buffer.append(innerIndent).append("no digest specified").append(PolicyUtils.Text.NEW_LINE);            
        } else {
            buffer.append(innerIndent).append("digest algorith URI = '").append(digestAlgorithmUri).append('\'').append(PolicyUtils.Text.NEW_LINE);
            buffer.append(innerIndent).append("digest = '").append(digest).append('\'').append(PolicyUtils.Text.NEW_LINE);
        }
        buffer.append(indent).append('}');
        
        return buffer;
    }
}
