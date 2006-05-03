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

package com.sun.xml.wss.impl;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import com.sun.xml.wss.*;

/** The <code>WssSoapFaultException</code> exception represents a 
 *  SOAP fault.
 *
 *  <p>The message part in the SOAP fault maps to the contents of
 *  <code>faultdetail</code> element accessible through the 
 *  <code>getDetail</code> method on the <code>WssSoapFaultException</code>.
 *  The method <code>createDetail</code> on the 
 *  <code>javax.xml.soap.SOAPFactory</code> creates an instance 
 *  of the <code>javax.xml.soap.Detail</code>. 
 *
 *  <p>The <code>faultstring</code> provides a human-readable 
 *  description of the SOAP fault. The <code>faultcode</code> 
 *  element provides an algorithmic mapping of the SOAP fault.
 * 
 *  <p>Refer to SOAP 1.1 and WSDL 1.1 specifications for more
 *  details of the SOAP faults. 
 *
 *  @see javax.xml.soap.Detail
 *  @see javax.xml.soap.SOAPFactory#createDetail
**/

public class WssSoapFaultException extends java.lang.RuntimeException  {
  
  private QName faultcode;
  private String faultstring;
  private String faultactor;
  private Detail detail;

  /** Constructor for the SOAPFaultException
   *  @param faultcode   <code>QName</code> for the SOAP faultcode
   *  @param faultstring <code>faultstring</code> element of SOAP fault 
   *  @param faultactor  <code>faultactor</code> element of SOAP fault
   *  @param faultdetail <code>faultdetail</code> element of SOAP fault 
   *
   *  @see javax.xml.soap.SOAPFactory#createDetail
   */
  public WssSoapFaultException(QName faultcode,
		   String faultstring,
		   String faultactor,
		   javax.xml.soap.Detail faultdetail) { 
    super(faultstring);
    this.faultcode = faultcode;
    this.faultstring = faultstring;
    this.faultactor = faultactor;
    this.detail = faultdetail;
  }

  /** Gets the <code>faultcode</code> element. The <code>faultcode</code>
   *  element provides an algorithmic mechanism for identifying the
   *  fault. SOAP defines a small set of SOAP fault codes covering 
   *  basic SOAP faults.
   *
   *  @return QName of the faultcode element
   */
  public QName getFaultCode() {
    return this.faultcode;
  }

  /** Gets the <code>faultstring</code> element. The <code>faultstring</code>
   *  provides a human-readable description of the SOAP fault and 
   *  is not intended for algorithmic processing.
   *
   *  @return faultstring element of the SOAP fault
   */
  public String getFaultString() {
    return this.faultstring;
  }

  /** Gets the <code>faultactor</code> element. The <code>faultactor</code>
   *  element provides information about which SOAP node on the 
   *  SOAP message path caused the fault to happen. It indicates 
   *  the source of the fault.
   * 
   *  @return <code>faultactor</code> element of the SOAP fault 
   */
  public String getFaultActor() {
    return this.faultactor;
  }

  /** Gets the detail element. The detail element is intended for
   *  carrying application specific error information related to
   *  the SOAP Body.
   *
   *  @return <code>detail</code> element of the SOAP fault
   *  @see javax.xml.soap.Detail
   */
  public Detail getDetail() {
    return this.detail;
  }
}
