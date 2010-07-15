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
package com.sun.xml.ws.tx.at.validation;

import com.sun.xml.ws.tx.at.api.Transactional;

import javax.xml.ws.WebServiceException;
import java.util.HashSet;
import java.util.Set;

public class TXAttributesValidator {

  public static final short TX_NOT_SET = -1;
  public static final short TX_NOT_SUPPORTED = 0;
  public static final short TX_REQUIRED = 1;
  public static final short TX_SUPPORTS = 2;
  public static final short TX_REQUIRES_NEW = 3;
  public static final short TX_MANDATORY = 4;
  public static final short TX_NEVER = 5;

   Set<InvalidCombination> inValidateCombinations = new HashSet<InvalidCombination>();
  static Set<Combination> validateCombinations = new HashSet<Combination>();

  static {
    validateCombinations.add(new Combination(TransactionAttributeType.REQUIRED, Transactional.TransactionFlowType.MANDATORY));
    validateCombinations.add(new Combination(TransactionAttributeType.REQUIRED, Transactional.TransactionFlowType.NEVER));
    validateCombinations.add(new Combination(TransactionAttributeType.MANDATORY, Transactional.TransactionFlowType.MANDATORY));
    validateCombinations.add(new Combination(TransactionAttributeType.REQUIRED, Transactional.TransactionFlowType.SUPPORTS));
    validateCombinations.add(new Combination(TransactionAttributeType.SUPPORTS, Transactional.TransactionFlowType.SUPPORTS));
    validateCombinations.add(new Combination(TransactionAttributeType.REQUIRES_NEW, Transactional.TransactionFlowType.NEVER));
    validateCombinations.add(new Combination(TransactionAttributeType.NEVER, Transactional.TransactionFlowType.NEVER));
    validateCombinations.add(new Combination(TransactionAttributeType.NOT_SUPPORTED, Transactional.TransactionFlowType.NEVER));
    //this is not on the FS.
    validateCombinations.add(new Combination(TransactionAttributeType.SUPPORTS, Transactional.TransactionFlowType.NEVER));
    validateCombinations.add(new Combination(TransactionAttributeType.SUPPORTS, Transactional.TransactionFlowType.MANDATORY));
  }

  public void visitOperation(String operationName, short attribute, Transactional.TransactionFlowType wsatType) {
    TransactionAttributeType ejbTx = fromIndex(attribute);
    visitOperation(operationName,ejbTx, wsatType);
  }

  public void validate() throws WebServiceException {
    StringBuilder sb = new StringBuilder();
    for (InvalidCombination combination : inValidateCombinations) {
      sb.append("The effective TransactionAttributeType "+combination.ejbTx).append(" and WS-AT Transaction flowType ").append(combination.wsat).append(" on WebService operation ").append(combination.operationName).append(" is not a valid combination! ");
    }
    if (sb.length() > 0)
      throw new WebServiceException(sb.toString());
  }

  public void visitOperation(String operationName, TransactionAttributeType ejbTx, Transactional.TransactionFlowType wsatType) {
    if (wsatType == null) wsatType = Transactional.TransactionFlowType.NEVER;
    Combination combination = new Combination(ejbTx, wsatType);
    if (!validateCombinations.contains(combination)) {
      inValidateCombinations.add(new InvalidCombination(ejbTx, wsatType, operationName));
    }
  }

  public static boolean isValid(TransactionAttributeType ejbTx, Transactional.TransactionFlowType wsatType) {
    return validateCombinations.contains(new Combination(ejbTx, wsatType));
  }

  private static TransactionAttributeType fromIndex(Short index) {
    switch (index) {
      case TX_NOT_SUPPORTED:
        return TransactionAttributeType.NOT_SUPPORTED;
      case TX_REQUIRED:
        return TransactionAttributeType.REQUIRED;
      case TX_SUPPORTS:
        return TransactionAttributeType.SUPPORTS;
      case TX_REQUIRES_NEW:
        return TransactionAttributeType.REQUIRES_NEW;
      case TX_MANDATORY:
        return TransactionAttributeType.MANDATORY;
      case TX_NEVER:
        return TransactionAttributeType.NEVER;
      default:
        return TransactionAttributeType.SUPPORTS;
    }
  }

  static class Combination {
    TransactionAttributeType ejbTx;
    Transactional.TransactionFlowType wsat;

    Combination(TransactionAttributeType ejbTx, Transactional.TransactionFlowType wsat) {
      this.ejbTx = ejbTx;
      this.wsat = wsat;
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Combination that = (Combination) o;

      if (ejbTx != that.ejbTx) return false;
      if (wsat != that.wsat) return false;

      return true;
    }

    public int hashCode() {
      int result;
      result = ejbTx.hashCode();
      result = 31 * result + wsat.hashCode();
      return result;
    }
  }

  static class InvalidCombination {
    TransactionAttributeType ejbTx;
    Transactional.TransactionFlowType wsat;
    String operationName;

    InvalidCombination(TransactionAttributeType ejbTx, Transactional.TransactionFlowType wsat, String operationName) {
      this.ejbTx = ejbTx;
      this.wsat = wsat;
      this.operationName = operationName;
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      InvalidCombination that = (InvalidCombination) o;

      if (ejbTx != that.ejbTx) return false;
      if (!operationName.equals(that.operationName)) return false;
      if (wsat != that.wsat) return false;

      return true;
    }

    public int hashCode() {
      int result;
      result = ejbTx.hashCode();
      result = 31 * result + wsat.hashCode();
      result = 31 * result + operationName.hashCode();
      return result;
    }
  }


public enum TransactionAttributeType {

   MANDATORY,
   REQUIRED,
   REQUIRES_NEW,
   SUPPORTS,
   NOT_SUPPORTED,
   NEVER
}

}
