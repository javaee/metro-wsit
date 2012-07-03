/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.wss.provider;
import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.xml.soap.SOAPMessage;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import com.sun.enterprise.security.jauth.AuthPolicy;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.WssProviderSecurityEnvironment;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.config.SecurityConfigurationXmlReader;
import com.sun.xml.wss.impl.config.DeclarativeSecurityConfiguration;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.Target;
import com.sun.xml.wss.impl.PolicyTypeUtil;
public class WssProviderAuthModule implements ModuleOptions, ConfigurationStates {
       protected SecurityPolicy _policy = null;
       protected WssProviderSecurityEnvironment _sEnvironment = null;
       private boolean runtimeUsernamePassword = false;
       private static final String SIGN_POLICY      = "com.sun.xml.wss.impl.policy.mls.SignaturePolicy";
       private static final String ENCRYPT_POLICY   = "com.sun.xml.wss.impl.policy.mls.EncryptionPolicy";
       private static final String TIMESTAMP_POLICY = "com.sun.xml.wss.impl.policy.mls.TimestampPolicy";
       private static final String AUTHENTICATION_POLICY = 
                      "com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy";
       private static final String USERNAMETOKEN_POLICY = 
                      "com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy.UsernameTokenBinding";
       private static final String USERNAMETOKEN = "UsernameToken";
       private static final String BODY = "Body";
       public static final String REQUESTER_SUBJECT = "REQUESTER_SUBJECT";
       public static final String REQUESTER_KEYID = "REQUESTER_KEYID";
       public static final String REQUESTER_ISSUERNAME = "REQUESTER_ISSUERNAME";
       public static final String REQUESTER_SERIAL = "REQUESTER_SERIAL";
       public static final String SELF_SUBJECT = "SELF_SUBJECT";
       protected int optimize = MessageConstants.NOT_OPTIMIZED;
       protected boolean configOptimizeAttribute = true;
       public WssProviderAuthModule() {
       }
      /**
       * Initialization method for Client and Server Auth Modules 
       * @param requestPolicy
       *        used to validate request on server side 
       *        and to secure request on client side
       * @param responsePolicy  
       *        used to validate response on client side
       *        and to secure response on server side
       * @param handler
       *        CallbackHandler 
       * @param options
       *        Map of module options
       * @param isClientAuthModule
       *        indicates if the current instance is client or server module
       * @throws RuntimeException
       */
       public void initialize (AuthPolicy requestPolicy,
                               AuthPolicy responsePolicy,
                               CallbackHandler handler,
                               Map options,
                               boolean isClientAuthModule) {
              boolean debugON = false;
              String bg = (String)options.get(DEBUG);
              if (bg !=null && bg.equals("true")) debugON = true;
              // use the requestPolicy to configure recipient in   
              // case of Server and annotator in case of Client -
              // use the responsePolicy to configure annotator in   
              // case of Server and recipient in case of Client -
              // get the security configuration file from options
              String securityConfigurationURL = (String)options.get(SECURITY_CONFIGURATION_FILE);
              String signAlias = (String)options.get(SIGNING_KEY_ALIAS);
              String encryptAlias = (String)options.get(ENCRYPTION_KEY_ALIAS);
              
              try {
                      InputStream is = null;
                      if (securityConfigurationURL != null) {
                          is = new BufferedInputStream(new FileInputStream(new File(securityConfigurationURL)));
                      } else {
                          // try to locate the config file from the classpath
                          if (this instanceof ServerSecurityAuthModule) {
                              is = this.getClass().getResourceAsStream("wss-server-config-2.0.xml");
                          } else {
                              is = this.getClass().getResourceAsStream("wss-client-config-2.0.xml");
                          }
                      }
                      _policy = SecurityConfigurationXmlReader.createDeclarativeConfiguration(is);
                  int request_policy_state =  EMPTY_POLICY_STATE;
                  int response_policy_state = EMPTY_POLICY_STATE;
                  if (requestPolicy != null) {
                      request_policy_state = resolveConfigurationState(requestPolicy, true, isClientAuthModule);
                  }
                  if (responsePolicy != null) {
                      response_policy_state = resolveConfigurationState(responsePolicy, false, isClientAuthModule);
                  }
                  String obj = (String)options.get(DYNAMIC_USERNAME_PASSWORD);
                  if (obj != null) {
                      runtimeUsernamePassword = obj.equalsIgnoreCase("true") ? true : false;
                  }
                  if (isClientAuthModule) {
                     augmentConfiguration(response_policy_state, true, handler, debugON, signAlias, encryptAlias);
                     augmentConfiguration(request_policy_state, false, handler, debugON, signAlias, encryptAlias);
                  } else {
                     augmentConfiguration(response_policy_state, false, handler, debugON, signAlias, encryptAlias);
                     augmentConfiguration(request_policy_state, true, handler, debugON, signAlias, encryptAlias);
                  }
                  _sEnvironment = new WssProviderSecurityEnvironment(handler, options);
              } catch (Exception e) {
                  throw new RuntimeException(e);
              } 
       }
      /**
       * Resolves the state of a policy object
       * @param policy
       *        AuthPolicy object whose state is to be resolved
       * @return configurationState
       *        returns one of the possible states defined in ConfigurationStates
       * @throws RuntimeException
       */
       public int resolveConfigurationState(AuthPolicy policy, boolean isRequestPolicy, boolean isClientAuthModule) {
           boolean orderForValidation = isClientAuthModule ?
                                        (isRequestPolicy ? false : true) :
                                        (isRequestPolicy ? true : false); 
           boolean sourceAuthRequired = policy.isSourceAuthRequired();
           boolean recipientAuthRequired = policy.isRecipientAuthRequired();
           boolean senderAuthRequired = policy.isSenderAuthRequired();
           boolean contentAuthRequired = policy.isContentAuthRequired();
           boolean beforeContent = policy.isRecipientAuthBeforeContent(orderForValidation);
           int configurationState = -1;
           if (sourceAuthRequired && !recipientAuthRequired) {
              if (senderAuthRequired) 
                 configurationState = AUTHENTICATE_SENDER_TOKEN_ONLY;
              else if (contentAuthRequired)
                 configurationState = AUTHENTICATE_SENDER_SIGNATURE_ONLY; 
           } else if (!sourceAuthRequired && recipientAuthRequired) {
              configurationState = AUTHENTICATE_RECIPIENT_ONLY;
           } else if (sourceAuthRequired && recipientAuthRequired) {
              if (beforeContent) {
                if (senderAuthRequired) {
                   configurationState = AUTHENTICATE_RECIPIENT_AUTHENTICATE_SENDER_TOKEN;  
                } else if (contentAuthRequired) {
                   configurationState = AUTHENTICATE_RECIPIENT_AUTHENTICATE_SENDER_SIGNATURE; 
                } 
              } else {
                if (senderAuthRequired) {
                   configurationState = AUTHENTICATE_SENDER_TOKEN_AUTHENTICATE_RECIPIENT; 
                } else if (contentAuthRequired) {
                   configurationState = AUTHENTICATE_SENDER_SIGNATURE_AUTHENTICATE_RECIPIENT; 
                } 
              }
           } else {
              configurationState = EMPTY_POLICY_STATE;
           }
           if (configurationState == -1) {
               // log
               throw new RuntimeException("AuthPolicy configuration error: Invalid policy specification");
           }
           return configurationState; 
       }
      @SuppressWarnings("unchecked")
      private Collection getEncryptPolicies(
          MessagePolicy mPolicy, CallbackHandler handler, boolean senderConfiguration) 
          throws PolicyGenerationException {
          Collection requiredElements = new ArrayList();              
          Iterator it = mPolicy.iterator();
          while (it.hasNext()) {
              WSSPolicy policy = (WSSPolicy)it.next();
              if (PolicyTypeUtil.encryptionPolicy(policy)) {
                  if (!hasEncryptUsernamePolicy((EncryptionPolicy)policy, mPolicy)) {
                      requiredElements.add(policy);
                  }              
              }
          }
          if (requiredElements.isEmpty()) {
              throw new RuntimeException("Operation/Requirement (" + 
                  translate2configurationName(ENCRYPT_POLICY, senderConfiguration) + 
                      ") not specified " + "in the Config. file is required by the policy");
          }
          return requiredElements;
      }
      @SuppressWarnings("unchecked")
      private Collection getEncryptPoliciesOptional(
          MessagePolicy mPolicy, CallbackHandler handler, boolean senderConfiguration) 
          throws PolicyGenerationException {
          Collection requiredElements = new ArrayList();              
          Iterator it = mPolicy.iterator();
          while (it.hasNext()) {
              WSSPolicy policy = (WSSPolicy)it.next();
              if (PolicyTypeUtil.encryptionPolicy(policy)) {
                  if (!hasEncryptUsernamePolicy((EncryptionPolicy)policy, mPolicy)) {
                      requiredElements.add(policy);
                  }              
              }
          }
          return requiredElements;
      }
      @SuppressWarnings("unchecked")
      private Collection getSignPolicies(
          MessagePolicy mPolicy, CallbackHandler handler, boolean senderConfiguration) 
          throws PolicyGenerationException {
          Collection requiredElements = new ArrayList();              
          Iterator it = mPolicy.iterator();
          while (it.hasNext()) {
              WSSPolicy policy = (WSSPolicy)it.next();
              if (PolicyTypeUtil.signaturePolicy(policy)) {
                  requiredElements.add(policy);
              }
          }
          if (requiredElements.isEmpty()) {
              throw new RuntimeException("Operation/Requirement (" + 
                  translate2configurationName(SIGN_POLICY, senderConfiguration) + 
                      ") not specified " + "in the Config. file is required by the policy");
          }
          return requiredElements;
      }
      private WSSPolicy getUsernamePolicy(MessagePolicy mPolicy, CallbackHandler handler, boolean senderConfiguration) 
          throws PolicyGenerationException {
          WSSPolicy usernamePolicy = null;
          Iterator it = mPolicy.iterator();
          while (it.hasNext()) {
              WSSPolicy policy = (WSSPolicy)it.next();
              if (PolicyTypeUtil.authenticationTokenPolicy(policy)) {
                  if ((policy.getFeatureBinding() != null) && 
                      (PolicyTypeUtil.usernameTokenPolicy(policy.getFeatureBinding()))) {
                      if (senderConfiguration && !runtimeUsernamePassword) {
                          setUsernamePassword((AuthenticationTokenPolicy)policy, handler);
                      } 
                      usernamePolicy = policy;
                      break;
                  }
              } 
          }
          if (usernamePolicy == null) {
              throw new RuntimeException("Operation/Requirement (" + 
                  translate2configurationName(USERNAMETOKEN_POLICY, senderConfiguration) + 
                      ") not specified " + "in the Config. file is required by the policy");
          }
          return usernamePolicy;
      }
     @SuppressWarnings("unchecked")
      private Collection getUsernamePolicies(MessagePolicy mPolicy, CallbackHandler handler, boolean senderConfiguration)
          throws PolicyGenerationException {
          Collection requiredElements = new ArrayList();              
          WSSPolicy encryptUsernamePolicy = null;
          Iterator it = mPolicy.iterator();
          while (it.hasNext()) {
              WSSPolicy policy = (WSSPolicy)it.next();
              if (PolicyTypeUtil.authenticationTokenPolicy(policy)) {
                  if ((policy.getFeatureBinding() != null) && 
                      (PolicyTypeUtil.usernameTokenPolicy(policy.getFeatureBinding()))) {
                      if (senderConfiguration && !runtimeUsernamePassword) {
                          setUsernamePassword((AuthenticationTokenPolicy)policy, handler);
                      }
                      requiredElements.add(policy);
                  }
              } else if (PolicyTypeUtil.encryptionPolicy(policy)) {
                  if (isEncryptUsernamePolicy((EncryptionPolicy)policy, mPolicy)) {
                      encryptUsernamePolicy = policy;
                  }
              }
          }
          if (requiredElements.isEmpty()) {
              throw new RuntimeException("Operation/Requirement (" + 
                  translate2configurationName(USERNAMETOKEN_POLICY, senderConfiguration) + 
                      ") not specified " + "in the Config. file is required by the policy");
          }
          if (encryptUsernamePolicy != null) {
              requiredElements.add(encryptUsernamePolicy);
          }
          return requiredElements;
      }
     @SuppressWarnings("unchecked")
     private Collection getEncryptUsernamePolicies(
          MessagePolicy mPolicy, CallbackHandler handler, boolean senderConfiguration) 
          throws PolicyGenerationException {
          Collection requiredElements = new ArrayList();              
          WSSPolicy eBU =  getEncryptBodyUsernamePolicy(mPolicy);
          if (eBU != null) {
              Collection ePolicies = getNonBodyUsernameEncryptPolicies(
                  mPolicy, handler, senderConfiguration);
              requiredElements.addAll(ePolicies);
              requiredElements.add(getUsernamePolicy(mPolicy, handler, senderConfiguration));
              requiredElements.add(eBU);
          } else {
              //FOR BC Reasons we allow this
              Collection ePolicies = getEncryptPoliciesOptional(mPolicy, handler, senderConfiguration);
              requiredElements.addAll(ePolicies);
              requiredElements.addAll(getUsernamePolicies(mPolicy, handler, senderConfiguration));
          }
          if (requiredElements.isEmpty()) {
              throw new RuntimeException("Operation/Requirement (" + 
                  translate2configurationName(ENCRYPT_POLICY, senderConfiguration) + 
                      ") not specified " + "in the Config. file is required by the policy");
          }
          return requiredElements;
      }
     @SuppressWarnings("unchecked")
     private Collection getUsernameEncryptPolicies(
          MessagePolicy mPolicy, CallbackHandler handler, boolean senderConfiguration) 
          throws PolicyGenerationException {
          Collection requiredElements = new ArrayList();              
          WSSPolicy eUB =  getEncryptUsernameBodyPolicy(mPolicy);
          if (eUB != null) {
              requiredElements.add(getUsernamePolicy(mPolicy, handler, senderConfiguration));
              requiredElements.add(eUB);
              Collection ePolicies = getNonBodyUsernameEncryptPolicies(
                  mPolicy, handler, senderConfiguration);
              requiredElements.addAll(ePolicies);
          } else {
              requiredElements.addAll(getUsernamePolicies(mPolicy, handler, senderConfiguration));
              //FOR BC Reasons we allow this
              Collection ePolicies = getEncryptPoliciesOptional(mPolicy, handler, senderConfiguration);
              requiredElements.addAll(ePolicies);
          }
          if (requiredElements.isEmpty()) {
              throw new RuntimeException("Operation/Requirement (" +
                  translate2configurationName(USERNAMETOKEN_POLICY, senderConfiguration) +
                      ") not specified " + "in the Config. file is required by the policy");
          }
          return requiredElements;
      }
      private WSSPolicy getTimestampPolicy(
          MessagePolicy mPolicy, CallbackHandler handler, boolean senderConfiguration) {
          WSSPolicy timestampPolicy = null;
          Iterator it = mPolicy.iterator();
          while (it.hasNext()) {
              WSSPolicy policy = (WSSPolicy)it.next();
              if (PolicyTypeUtil.timestampPolicy(policy)) {
                  timestampPolicy = (policy);
                  break;
              }
          }
          return timestampPolicy;
      }
      /**
       * Modifies the sender/receiver settings according to the corresponding policy
       * @param requiredState
       *        the state of the policy object
       * @param modifyReceiverSettings
       *        indicates if sender/receiver settings need to be modified
       * @throws RuntimeException
       */
       @SuppressWarnings("unchecked")
       private void augmentConfiguration(int requiredState, boolean modifyReceiverSettings, CallbackHandler handler, boolean debugON,
           String signAlias, String encryptAlias) throws PolicyGenerationException {
              MessagePolicy mPolicy = null;
              DeclarativeSecurityConfiguration dConfiguration = (DeclarativeSecurityConfiguration) _policy;
              boolean senderConfiguration = false;
              if (requiredState == EMPTY_POLICY_STATE)  {
                  if (modifyReceiverSettings) {
                      mPolicy = dConfiguration.receiverSettings();
                      mPolicy.removeAll();
 
                  } else {
                     mPolicy = dConfiguration.senderSettings();
                     mPolicy.removeAll();
                  }
                  if (debugON) {
                      mPolicy.dumpMessages(true);
                  }
                  return;
              }
              if (modifyReceiverSettings) {
                 mPolicy = dConfiguration.receiverSettings();
              } else {
                 mPolicy = dConfiguration.senderSettings();
                 senderConfiguration = !senderConfiguration;
              }
              Collection newMPolicy = null;
              WSSPolicy ts = getTimestampPolicy(mPolicy, handler, senderConfiguration);
              boolean requireTimestampPolicy = false;
              switch (requiredState) {
                 case AUTHENTICATE_RECIPIENT_ONLY: 
                                  // Resultant List:  (encrypt+)
                                  newMPolicy = getEncryptPolicies(mPolicy, handler, senderConfiguration);
                                  mPolicy.removeAll();
                                  mPolicy.appendAll(newMPolicy);
                                  break;
                 case AUTHENTICATE_SENDER_TOKEN_ONLY:
                                  // Resultant List:  (authenticate, encrypt?)
                                  newMPolicy = getUsernamePolicies(mPolicy, handler, senderConfiguration);
                                  mPolicy.removeAll();
                                  mPolicy.appendAll(newMPolicy);
                                  if (!modifyReceiverSettings && configOptimizeAttribute) {
                                      optimize=MessageConstants.SECURITY_HEADERS;
                                  }
                                  break;
                 case AUTHENTICATE_SENDER_SIGNATURE_ONLY:
                                  // Resultant List: (sign+) 
                                  newMPolicy = getSignPolicies(mPolicy, handler, senderConfiguration);
                                  requireTimestampPolicy = !(newMPolicy.isEmpty());
                                  mPolicy.removeAll();
                                  mPolicy.appendAll(newMPolicy);
                                  if (!modifyReceiverSettings && configOptimizeAttribute) {
                                      optimize=MessageConstants.SIGN_BODY;
                                  }
                                  break;  
                 case AUTHENTICATE_RECIPIENT_AUTHENTICATE_SENDER_TOKEN:
                                  /* Resultant List: (encrypt+, authenticate, encrypt?) */
                                  newMPolicy = getEncryptUsernamePolicies(mPolicy, handler, senderConfiguration);
                                  mPolicy.removeAll();
                                  mPolicy.appendAll(newMPolicy);
                                  break;  
                 case AUTHENTICATE_SENDER_TOKEN_AUTHENTICATE_RECIPIENT:
                                  /* Resultant List: (authenticate, encrypt+) */
                                  newMPolicy =  getUsernameEncryptPolicies(mPolicy, handler, senderConfiguration);
                                  mPolicy.removeAll();
                                  mPolicy.appendAll(newMPolicy);
                                  break;  
                 case AUTHENTICATE_RECIPIENT_AUTHENTICATE_SENDER_SIGNATURE:
                                  /* Resultant List: (encrypt+, sign+) */
                                  newMPolicy =  getEncryptPolicies(mPolicy, handler, senderConfiguration);
                                  Collection signPolicies = getSignPolicies(mPolicy, handler, senderConfiguration);
                                  requireTimestampPolicy = !(signPolicies.isEmpty());
                                  newMPolicy.addAll(signPolicies);
                                  mPolicy.removeAll();
                                  mPolicy.appendAll(newMPolicy);
                                  break;  
                 case AUTHENTICATE_SENDER_SIGNATURE_AUTHENTICATE_RECIPIENT: 
                                  /* Resultant List: (sign+, encrypt+) */
				  newMPolicy = getSignPolicies(mPolicy, handler, senderConfiguration);
                                  requireTimestampPolicy = !(newMPolicy.isEmpty());
                                  newMPolicy.addAll(getEncryptPolicies(mPolicy, handler, senderConfiguration));
                                  mPolicy.removeAll();
                                  mPolicy.appendAll(newMPolicy);
                                  if (!modifyReceiverSettings && configOptimizeAttribute) {
                                      optimize=MessageConstants.SIGN_ENCRYPT_BODY;
                                  }
                                  break;  
                 default:
                                  break;
              }
              if ((ts != null) && requireTimestampPolicy) {
                  mPolicy.prepend(ts);
              }
              if (debugON) {
                  mPolicy.dumpMessages(true);
              }
              augmentSignAlias(mPolicy, signAlias);
              augmentEncryptAlias(mPolicy, encryptAlias);
       }
       private String translate2configurationName(String opName, boolean senderConfiguration) {
             String value = null;
             if (opName == SIGN_POLICY) 
                 value = senderConfiguration ? "xwss:Sign" : "xwss:RequireSignature";
             else 
             if (opName == ENCRYPT_POLICY) 
                 value = senderConfiguration ? "xwss:Encrypt" : "xwss:RequireEncryption";
             else 
             if (opName == USERNAMETOKEN_POLICY) 
                 value = senderConfiguration ? "xwss:UsernameToken" : "xwss:RequireUsernameToken";
             return value;
       }
       private boolean isEncryptUsernamePolicy(EncryptionPolicy policy, MessagePolicy mPolicy) {
           EncryptionPolicy.FeatureBinding fb = 
               (EncryptionPolicy.FeatureBinding)policy.getFeatureBinding();
           int numTargets = fb.getTargetBindings().size();
           if (numTargets != 1)
               return false;
           Iterator it1 = fb.getTargetBindings().iterator();
           Target target = (Target)it1.next();
           if (target.getType() == Target.TARGET_TYPE_VALUE_URI) {
               //get the URI fragment
               return uriIsUsernameToken(mPolicy, target.getValue());
           } else {
               int idx = target.getValue().indexOf(USERNAMETOKEN);
               if (idx > -1) {
                   return true;
               }
           }
           return false;
       }
       private boolean hasEncryptUsernamePolicy(EncryptionPolicy policy, MessagePolicy mPolicy) {
           EncryptionPolicy.FeatureBinding fb = 
               (EncryptionPolicy.FeatureBinding)policy.getFeatureBinding();
           Iterator it = fb.getTargetBindings().iterator();
           while (it.hasNext()) {
               Target target = (Target)it.next();
               if (target.getType() == Target.TARGET_TYPE_VALUE_URI) {
                   //get the URI fragment
                   return uriIsUsernameToken(mPolicy, target.getValue());
               }else {
                   int idx = target.getValue().indexOf(USERNAMETOKEN);
                   if (idx > -1) {
                       return true;
                   }
               }
           }
           return false;
       }
       private boolean uriIsUsernameToken(MessagePolicy mPolicy, String uri) {
           String fragment = uri;
           if (uri.startsWith("#")) {
               fragment = uri.substring(1);
           }
           Iterator it = mPolicy.iterator();
           while (it.hasNext()) {
              WSSPolicy policy = (WSSPolicy)it.next();
              if (PolicyTypeUtil.authenticationTokenPolicy(policy)) {
                  MLSPolicy feature = policy.getFeatureBinding();
                  if ((feature != null) && PolicyTypeUtil.usernameTokenPolicy(feature)) {
                      AuthenticationTokenPolicy.UsernameTokenBinding fb =
                          (AuthenticationTokenPolicy.UsernameTokenBinding)feature;
                      if (fragment.equals(fb.getUUID())) {
                          return true;
                      }
                  }
               }
           }
           return false;
       }
       private WSSPolicy getEncryptBodyUsernamePolicy(MessagePolicy mPolicy) {
           WSSPolicy ret = null;
           Iterator it = mPolicy.iterator();
           while (it.hasNext()) {
              WSSPolicy policy = (WSSPolicy)it.next();
              if (PolicyTypeUtil.encryptionPolicy(policy)) {
                  EncryptionPolicy.FeatureBinding fb = 
                      (EncryptionPolicy.FeatureBinding)policy.getFeatureBinding();
                  int numTargets = fb.getTargetBindings().size();
                  if (numTargets <= 1) {
                     continue; 
                  }
                  if (hasBodyFollowedByUsername((ArrayList)fb.getTargetBindings())) {
                      ret = policy;
                      return ret;
                  } 
              }
           }
           return ret;
       }
       private WSSPolicy getEncryptUsernameBodyPolicy(MessagePolicy mPolicy) {
           WSSPolicy ret = null;
           Iterator it = mPolicy.iterator();
           while (it.hasNext()) {
              WSSPolicy policy = (WSSPolicy)it.next();
              if (PolicyTypeUtil.encryptionPolicy(policy)) {
                  EncryptionPolicy.FeatureBinding fb = 
                      (EncryptionPolicy.FeatureBinding)policy.getFeatureBinding();
                  int numTargets = fb.getTargetBindings().size();
                  if (numTargets <= 1) {
                     continue; 
                  }
                  if (hasUsernameFollowedByBody((ArrayList)fb.getTargetBindings())) {
                      ret = policy;
                      return ret;
                  } 
              }
           }
           return ret;
       }
       //Assuming single username target
       //TODO: This logic fails if we have more than 2 targets in the ArrayList
       private boolean hasUsernameFollowedByBody(ArrayList targets) {
           Target t = (Target)targets.get(0);
           int idx = t.getValue().indexOf(USERNAMETOKEN);
           if (idx == -1) {
               return false;
           }
           return true;
       }
       //TODO: This logic fails if we have more than 2 targets in the ArrayList
       private boolean hasBodyFollowedByUsername(ArrayList targets) {
           Target t = (Target)targets.get(targets.size() - 1);
           int idx = t.getValue().indexOf(USERNAMETOKEN);
           if (idx == -1) {
               return false;
           }
           return true;
       }
       private void setUsernamePassword(AuthenticationTokenPolicy policy, CallbackHandler handler) {
            AuthenticationTokenPolicy.UsernameTokenBinding
                up = (AuthenticationTokenPolicy.UsernameTokenBinding)policy.getFeatureBinding();
                                                                                                                                                     
                NameCallback nameCallback    = new NameCallback("Username: ");
                PasswordCallback pwdCallback = new PasswordCallback("Password: ", false);
                                                                                                                                                
                try {
                    Callback[] cbs = new Callback[] { nameCallback, pwdCallback };
                    handler.handle(cbs);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                                                                                                                                                     
                up.setUsername(nameCallback.getName());
                up.setPassword(new String(pwdCallback.getPassword()));
       }
       @SuppressWarnings("unchecked")
       private Collection getNonBodyUsernameEncryptPolicies(
            MessagePolicy mPolicy, CallbackHandler handler, boolean senderConfiguration) {
           Collection requiredElements = new ArrayList();
                                                                                                           
           Iterator it = mPolicy.iterator();
           while (it.hasNext()) {
              WSSPolicy policy = (WSSPolicy)it.next();
              if (PolicyTypeUtil.encryptionPolicy(policy)) {
                  if (!hasEncryptBodyPolicy((EncryptionPolicy)policy) && 
                      !hasEncryptUsernamePolicy((EncryptionPolicy)policy, mPolicy)) {
                      requiredElements.add(policy);
                  }
              }
          }
                                                                                                           
          return requiredElements;
       }
       private boolean hasEncryptBodyPolicy(EncryptionPolicy policy) {
           EncryptionPolicy.FeatureBinding fb = 
               (EncryptionPolicy.FeatureBinding)policy.getFeatureBinding();
           Iterator it = fb.getTargetBindings().iterator();
           while (it.hasNext()) {
               Target target = (Target)it.next();
               int idx = target.getValue().indexOf(BODY);
               if (idx > -1) {
                   return true;
               }
           }
           return false;
       }
       protected boolean isOptimized(SOAPMessage msg){
//		     System.out.println("ClassName"+(msg.getClass().getName()));
		     if(msg.getClass().getName().equals("com.sun.xml.messaging.saaj.soap.ver1_1.ExpressMessage1_1Impl") || msg.getClass().getName().equals("com.sun.xml.messaging.saaj.soap.ver1_2.ExpressMessage1_2Impl")){
				return true;
			 }
			 return false;
       }
      private void augmentSignAlias(MessagePolicy mPolicy, String signAlias) {
          if (signAlias == null) {
              return;
          }
          for (Iterator it = mPolicy.iterator(); it.hasNext();) {
              WSSPolicy sp = (WSSPolicy)it.next();
              SecurityPolicy keyBinding = sp.getKeyBinding();
              if (sp instanceof SignaturePolicy) {
                  if ((keyBinding != null) && (keyBinding instanceof AuthenticationTokenPolicy.X509CertificateBinding)) {
                      AuthenticationTokenPolicy.X509CertificateBinding x509KB = 
                          (AuthenticationTokenPolicy.X509CertificateBinding)keyBinding;
                      String certId = x509KB.getCertificateIdentifier();
                      if (certId != null) {
                          x509KB.setCertificateIdentifier(signAlias);
                      }
                  }
              }
          }
      }
      private void augmentEncryptAlias(MessagePolicy mPolicy, String encryptAlias) {
          if (encryptAlias == null) {
              return;
          }
          for (Iterator it = mPolicy.iterator(); it.hasNext();) {
              WSSPolicy sp = (WSSPolicy)it.next();
              SecurityPolicy keyBinding = sp.getKeyBinding();
              if (sp instanceof EncryptionPolicy) {
                  if ((keyBinding != null) && (keyBinding instanceof AuthenticationTokenPolicy.X509CertificateBinding)) {
                      AuthenticationTokenPolicy.X509CertificateBinding x509KB = 
                          (AuthenticationTokenPolicy.X509CertificateBinding)keyBinding;
                      String certId = x509KB.getCertificateIdentifier();
                      if (certId != null) {
                          x509KB.setCertificateIdentifier(encryptAlias);
                      }
                  }
              }
          }
      }
}
