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
package com.sun.xml.ws.tx.at.internal;

import java.util.Locale;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;


/**
 * JTA-specific helper methods.
 */
class JTAHelper {

  static void throwXAException(int errCode, String errMsg) throws XAException {
    XAException ex = new XAException(xaErrorCodeToString(errCode) + ".  " + errMsg);
    ex.errorCode =  errCode;
    throw ex;
  }

  static void throwXAException(int errCode, String errMsg, Throwable t) 
    throws XAException 
  {
    XAException ex = new XAException(xaErrorCodeToString(errCode) + ".  " + errMsg);
    ex.errorCode =  errCode;
    ex.initCause(t);
    throw ex;
  }
  
  static String xaErrorCodeToString(int err) {
    return xaErrorCodeToString(err, true);
  }

  static String xaErrorCodeToString(int err, boolean detail) {
    StringBuffer msg = new StringBuffer(10);
    switch (err) {
    case XAResource.XA_OK:
      return "XA_OK";
    case XAException.XA_RDONLY:
      return "XA_RDONLY";
    case XAException.XA_HEURCOM:
      msg.append("XA_HEURCOM");
      if (detail) msg.append(" : The transaction branch has been heuristically committed");
      return msg.toString();
    case XAException.XA_HEURHAZ:
      msg.append("XA_HEURHAZ");
      if (detail) msg.append(" : The transaction branch may have been heuristically completed");
      return msg.toString();
    case XAException.XA_HEURMIX:
      msg.append("XA_HEURMIX");
      if (detail) msg.append(" : The transaction branch has been heuristically committed and rolled back");
      return msg.toString();
    case XAException.XA_HEURRB:
      msg.append("XA_HEURRB");
      if (detail) msg.append(" : The transaction branch has been heuristically rolled back");
      return msg.toString();                             
    case XAException.XA_RBCOMMFAIL:
      msg.append("XA_RBCOMMFAIL");
      if (detail) msg.append(" : Rollback was caused by communication failure");
      return msg.toString();
    case XAException.XA_RBDEADLOCK:
      msg.append("XA_RBDEADLOCK");
      if (detail) msg.append(" : A deadlock was detected");
      return msg.toString();
    case XAException.XA_RBINTEGRITY:
      msg.append("XA_RBINTEGRITY");
      if (detail) msg.append(" : A condition that violates the integrity of the resource was detected");
      return msg.toString();
    case XAException.XA_RBOTHER:
      msg.append("XA_RBOTHER");
      if (detail) msg.append(" : The resource manager rolled back the transaction branch for a reason not on this list");
      return msg.toString();
    case XAException.XA_RBPROTO:
      msg.append("XA_RBPROTO");
      if (detail) msg.append(" : A protocol error occured in the resource manager");
      return msg.toString();
    case XAException.XA_RBROLLBACK:
      msg.append("XA_RBROLLBACK");
      if (detail) msg.append(" : Rollback was caused by unspecified reason");
      return msg.toString();
    case XAException.XA_RBTIMEOUT:
      msg.append("XA_RBTIMEOUT");
      if (detail) msg.append(" : A transaction branch took too long");
      return msg.toString();
    case XAException.XA_RBTRANSIENT:
      msg.append("XA_RBTRANSIENT");
      if (detail) msg.append(" : May retry the transaction branch");
      return msg.toString();
    case XAException.XAER_ASYNC:
      msg.append("XAER_ASYNC");
      if (detail) msg.append(" : Asynchronous operation already outstanding");
      return msg.toString();      
    case XAException.XAER_DUPID:
      msg.append("XAER_DUPID");
      if (detail) msg.append(" : The XID already exists");
      return msg.toString();      
    case XAException.XAER_INVAL:
      msg.append("XAER_INVAL");
      if (detail) msg.append(" : Invalid arguments were given");
      return msg.toString();      
    case XAException.XAER_NOTA:
      msg.append("XAER_NOTA");
      if (detail) msg.append(" : The XID is not valid");
      return msg.toString();      
    case XAException.XAER_OUTSIDE:
      msg.append("XAER_OUTSIDE");
      if (detail) msg.append(" : The resource manager is doing work outside global transaction");
      return msg.toString();      
    case XAException.XAER_PROTO:
      msg.append("XAER_PROTO");
      if (detail) msg.append(" : Routine was invoked in an inproper context");
      return msg.toString();      
    case XAException.XAER_RMERR:
      msg.append("XAER_RMERR");
      if (detail) msg.append(" : A resource manager error has occured in the transaction branch");
      return msg.toString();      
    case XAException.XAER_RMFAIL:
      msg.append("XAER_RMFAIL");
      if (detail) msg.append(" : Resource manager is unavailable");
      return msg.toString();      
    default:
      return Integer.toHexString(err).toUpperCase(Locale.ENGLISH);
    }
  }
}
