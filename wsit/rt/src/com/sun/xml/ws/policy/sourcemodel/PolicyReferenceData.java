/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;

/**
 *
 * @author Marek Potociar
 */
final class PolicyReferenceData {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyReferenceData.class);
    
    public static final QName ATTRIBUTE_URI = new QName(PolicyConstants.POLICY_NAMESPACE_URI, "URI");
    public static final QName ATTRIBUTE_DIGEST = new QName(PolicyConstants.POLICY_NAMESPACE_URI, "Digest");
    public static final QName ATTRIBUTE_DIGEST_ALGORITHM = new QName(PolicyConstants.POLICY_NAMESPACE_URI, "DigestAlgorithm");
    
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
            LOGGER.severe("<init>", LocalizationMessages.WSP_0015_UNABLE_TO_INSTANTIATE_DIGEST_ALG_URI_FIELD(), CLASS_INITIALIZATION_EXCEPTION);
            throw new IllegalStateException(LocalizationMessages.WSP_0015_UNABLE_TO_INSTANTIATE_DIGEST_ALG_URI_FIELD(), CLASS_INITIALIZATION_EXCEPTION);
        }
        
        if (usedDigestAlgorithm != null && expectedDigest == null) {
            LOGGER.severe("<init>", LocalizationMessages.WSP_0072_DIGEST_MUST_NOT_BE_NULL_WHEN_ALG_DEFINED());
            throw new IllegalArgumentException(LocalizationMessages.WSP_0072_DIGEST_MUST_NOT_BE_NULL_WHEN_ALG_DEFINED());
        }
        
        this.referencedModelUri = referencedModelUri;
        if (expectedDigest != null) {
            this.digest = expectedDigest;
            
            if (usedDigestAlgorithm != null) {
                this.digestAlgorithmUri = usedDigestAlgorithm;
            } else {
                this.digestAlgorithmUri = DEFAULT_DIGEST_ALGORITHM_URI;
            }
        } else {
            this.digest = null;
            this.digestAlgorithmUri = null;
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
        if (digest != null) {
            buffer.append(innerIndent).append("digest algorith URI = '").append(digestAlgorithmUri).append('\'').append(PolicyUtils.Text.NEW_LINE);
            buffer.append(innerIndent).append("digest = '").append(digest).append('\'').append(PolicyUtils.Text.NEW_LINE);
        } else {
            buffer.append(innerIndent).append("no digest specified").append(PolicyUtils.Text.NEW_LINE);            
        }
        buffer.append(indent).append('}');
        
        return buffer;
    }
}
