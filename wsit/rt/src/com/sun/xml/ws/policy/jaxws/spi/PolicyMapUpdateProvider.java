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

package com.sun.xml.ws.policy.jaxws.spi;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMapExtender;

/**
 * The service provider implementing this interface will be discovered and called to extend created PolicyMap instance with additional policy
 * bindings. The call is performed directly after WSDL document is parsed.
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public interface PolicyMapUpdateProvider {

  /**
   * A callback method that allows to retrieve policy related information from provided WSDLModel and modify the associated policy map
   * accordingly via provided policy map mutator object, which is associated with the policy map.
   *
   * @param policyMapMutator policy map mutator that may be used to modify/extend policy map instance by adding new policy bindings
   * @param model the service provider can retrieve its WSDLExtensions from the parsed WSDLModel
   */
  void update(PolicyMapExtender policyMapMutator, WSDLModel model) throws PolicyException;        
}
