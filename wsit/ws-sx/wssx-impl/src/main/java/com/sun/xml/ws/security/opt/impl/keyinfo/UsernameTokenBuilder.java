/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.security.opt.impl.keyinfo;

import com.sun.xml.ws.security.opt.api.keyinfo.BuilderResult;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.reference.DirectReference;
import com.sun.xml.ws.security.opt.impl.tokens.UsernameToken;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import java.util.logging.Level;
import com.sun.xml.wss.logging.impl.opt.token.LogStringsMessages;

/**
 *
 * @author suresh
 */
public class UsernameTokenBuilder extends TokenBuilder {

    AuthenticationTokenPolicy.UsernameTokenBinding binding = null;

    public UsernameTokenBuilder(JAXBFilterProcessingContext context, AuthenticationTokenPolicy.UsernameTokenBinding binding) {
        super(context);
        this.binding = binding;
    }
    /**
     * processes the token ,builds keyinfo and sets it in BuilderResult
     * @return BuilderResult
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public BuilderResult process() throws XWSSecurityException {
        String untokenId = binding.getUUID();
        if (untokenId == null || untokenId.equals("")) {
            untokenId = context.generateID();
        }
        SecurityUtil.checkIncludeTokenPolicyOpt(context, binding, untokenId);
        String referenceType = binding.getReferenceType();       
        BuilderResult result = new BuilderResult();
        if (MessageConstants.DIRECT_REFERENCE_TYPE.equals(referenceType)) {
            UsernameToken unToken = createUsernameToken(binding, binding.getUsernameToken());
            if (unToken == null) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1856_NULL_USERNAMETOKEN());
                throw new XWSSecurityException("Username Token is NULL");
            }
            DirectReference dr = buildDirectReference(unToken.getId(), MessageConstants.USERNAME_STR_REFERENCE_NS);
            buildKeyInfo(dr, binding.getSTRID());
        }                
        result.setKeyInfo(keyInfo);
        return result;    
    }
}
