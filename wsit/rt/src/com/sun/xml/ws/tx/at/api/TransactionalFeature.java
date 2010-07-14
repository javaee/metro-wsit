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
package com.sun.xml.ws.tx.at.api;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.xml.ws.WebServiceFeature;
import java.util.HashMap;
import java.util.Map;

/**
 * This feature represents the use of WS-AT with a
 * web service.
 * <p/>
 * <p/>
 * The following describes the affects of this feature with respect
 * to being enabled or disabled:
 * <ul>
 * <li> ENABLED: In this Mode, WS-AT will be enabled.
 * <li> DISABLED: In this Mode, WS-AT will be disabled
 * </ul>
 * <p/>
 */
public class TransactionalFeature extends WebServiceFeature{
  /**
   * Constant value identifying the TransactionalFeature
   */
  public static final String ID = "com.sun.xml.ws.tx.at.api.TransactionalFeature";

  private Transactional.TransactionFlowType flowType = Transactional.TransactionFlowType.SUPPORTS;
  private boolean isExplicitMode;
  private Transactional.Version version = Transactional.Version.DEFAULT;
  private Map<String, Transactional.TransactionFlowType> flowTypeMap = new HashMap<String, Transactional.TransactionFlowType>();
  private Map<String, Boolean> enabledMap = new HashMap<String, Boolean>();

  @FeatureConstructor({"enabled", "value", "version"})
  public TransactionalFeature(boolean enabled, Transactional.TransactionFlowType value, Transactional.Version version) {
    this.enabled = enabled;
    this.flowType = value;
    this.version = version;
  }

  /**
   * Create an <code>TransactionalFeature</code>.
   * The instance created will be enabled.
   */

  public TransactionalFeature() {
    this.enabled = true;
  }

  /**
   * Create an <code>TransactionalFeature</code>
   *
   * @param enabled specifies whether this feature should
   *                be enabled or not.
   */
  public TransactionalFeature(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns the default Transaction flow type for all operations.
   * @return Transactional.TransactionFlowType
   */
  public Transactional.TransactionFlowType getFlowType() {
    return flowType;
  }

  /**
   * Returns the Transaction flow type for a given operation.
   * @return Transactional.TransactionFlowType
   */
  public Transactional.TransactionFlowType getFlowType(String operationName) {
    Transactional.TransactionFlowType type = flowTypeMap.get(operationName);
    if (!isExplicitMode && type == null)
      type = flowType;
    return type;
  }

  /**
   * Set the default Transaction flow type for all operations.
   * @param flowType
   */
  public void setFlowType(Transactional.TransactionFlowType flowType) {
    this.flowType = flowType;
  }

  /**
   * Set the Transaction flow type for a given wsdl:operation.
   * @param operationName  the local part of wsdl:opration
   * @param flowType Transaction flow type
   */
  public void setFlowType(String operationName, Transactional.TransactionFlowType flowType) {
    flowTypeMap.put(operationName, flowType);
  }


  public String getID() {
    return ID;
  }

  /**
   * Enable/disable this feature at port level
   *
   * @param enabled
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Enable/disable this feature on a given operation
   *
   * @param operationName the local part of operation.
   * @param enabled
   */
  public void setEnabled(String operationName, boolean enabled) {
    enabledMap.put(operationName, enabled);
  }

  /**
   * Returns <code>true</code> if WS-AT is enabled on the given operation.
   *
   * @param operationName  the local part of wsdl:operation
   * @return <code>true</code> if and only if the WS-AT is enabled on the given operation.
   */
  public boolean isEnabled(String operationName) {
    Boolean isEnabled = enabledMap.get(operationName);
    if (isEnabled == null) {
      return isExplicitMode ? false : enabled;
    } else
      return isEnabled;
  }


  /**
   * Returns the version of WS-AT to be used.
   *
   * @return  Transactional.Version
   */
  public Transactional.Version getVersion() {
    return version;
  }

  /**
   * Set the version of WS-AT to be used.
   *
   * @param version   the version of WS-AT to be used.
   */
  public void setVersion(Transactional.Version version) {
    this.version = version;
  }

  /**
   *  return a map listing the Transactional flow options for operations.
    * @return a mapping listing the Transactional flow options explicit on operations.
   */
  public Map<String, Transactional.TransactionFlowType> getFlowTypeMap() {
    return flowTypeMap;
  }

  /**
   *
    * @return a mapping listing the transactional enabled attributes explicitly set on operations.
   */
  public Map<String, Boolean> getEnabledMap() {
    return enabledMap;
  }

  /**
   * Transactional Feature has two modes, explicit Mode or implicit Mode.
   * In the implicit Mode, the Transactional Feature can be enabled at port level
   *  and be inherited or override at operation level. In the explicit Mode,
   *  transactional flow option can only specified and enabled at operation level.
   *  the default is explicit Mode.
   * @return whether this Transactional Feature is in explicit mode.
   */
  public boolean isExplicitMode() {
    return isExplicitMode;
  }

  /**
   * Change the Transactional Feature mode
   * @param explicitMode whether set to explicit Mode.
   */
  public void setExplicitMode(boolean explicitMode) {
    isExplicitMode = explicitMode;
  }

}
