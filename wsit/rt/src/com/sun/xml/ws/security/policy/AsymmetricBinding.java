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


package com.sun.xml.ws.security.policy;

/**
 * Represents Asymmetric Token information to be used for Signature and Encryption
 * by the client and the service. If the message pattern requires multiple messages,
 * this binding defines that the Initiator Token is used for the message signature
 * from initiator to the recipient, and for encryption from recipient to initiator.
 * The Recipient Token is used for encryption from initiator to recipient, and for
 * the message signature from recipient to initiator. This interface represents normalized
 * AsymmetricBinding security policy assertion as shown below.
 *
 * <pre><xmp>
 *      <sp:AsymmetricBinding ... >
 *              <wsp:Policy>
 *                  <sp:InitiatorToken>
 *                      <wsp:Policy> ... </wsp:Policy>
 *                  </sp:InitiatorToken>
 *                  <sp:RecipientToken>
 *                      <wsp:Policy> ... </wsp:Policy>
 *                  </sp:RecipientToken>
 *                  <sp:AlgorithmSuite ... >
 *                      ...
 *                  </sp:AlgorithmSuite>
 *                  <sp:Layout ... > ... </sp:Layout> ?
 *                  <sp:IncludeTimestamp ... /> ?
 *                  <sp:EncryptBeforeSigning ... /> ?
 *                  <sp:EncryptSignature ... /> ?
 *                  <sp:ProtectTokens ... /> ?
 *                  <sp:OnlySignEntireHeadersAndBody ... /> ?
 *                      ...
 *             </wsp:Policy>
 *          ...
 *      </sp:AsymmetricBinding>
 *
 *  </xmp></pre>
 *
 * @author K.Venugopal@sun.com
 */
public interface AsymmetricBinding extends Binding{
   
    /**
     * returns Recipient token
     * @return {@link X509Token}
     */
    public Token getRecipientToken();
   
    /**
     * returns Initiator token
     * @return {@link X509Token}
     */
    public Token getInitiatorToken();
}
