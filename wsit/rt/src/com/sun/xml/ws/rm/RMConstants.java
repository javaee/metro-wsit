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

/*
 * RMConstants.java
 *
 * @author Mike Grogan
 * Created on October 9, 2005, 9:14 PM
 *
 */

package com.sun.xml.ws.rm;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.net.URI;


/**
 * Class contains accessors for constants defined by the 02/2005 version of the 
 * WS-RM specification.
 */
public enum RMConstants {


    W3C(AddressingVersion.W3C) {
        @Override
        public Class getAcksToClass (){
            return W3CEndpointReference.class;
        }
    },
    MEMBER(AddressingVersion.MEMBER)  {
        @Override
        public Class getAcksToClass (){
            return MemberSubmissionEndpointReference.class;
        }
    };




    private final AddressingVersion addressingVersion ;
    // TODO FIX ME ADDRESSING_FIXME
    // private static final JAXBContext jc;



    private RMConstants(AddressingVersion addVersion) {
        this.addressingVersion = addVersion;


    }

    public static RMConstants getRMConstants(AddressingVersion version) {
        if(version == AddressingVersion.W3C) {
            return RMConstants.W3C;
        } else {
            return RMConstants.MEMBER;
        }
    }



   

   //Policy Assertiion QNames
    public QName getRMAssertionQName() {
        return new QName(Constants.version, "RMAssertion");
    }

    public QName getOrderedQName() {
        return new QName(Constants.sunVersion, "Ordered");
    }
    
    public QName getAllowDuplicatesQName() {
        return new QName(Constants.sunVersion, "AllowDuplicates");
    }

    public QName getResendIntervalQName() {
        return new QName(Constants.sunClientVersion, "ResendInterval");
    }

    public QName getAckRequestIntervalQName() {
        return new QName(Constants.sunClientVersion, "AckRequestInterval");
    }
    
    public QName getCloseTimeoutQName() {
        return new QName(Constants.sunClientVersion, "CloseTimeout");
    }

    public QName getInactivityTimeoutQName() {
        return new QName(Constants.version, "InactivityTimeout");
    }

    public QName getAcknowledgementIntervalQName() {
        return new QName(Constants.version, "AcknowledgementInterval");
    }

    public QName getMillisecondsQName() {
        return new QName(Constants.version, "Milliseconds");
    }

    public QName getRMFlowControlQName() {
        return new QName(Constants.microsoftVersion, "RmFlowControl");
    }

    public QName getMaxReceiveBufferSizeQName() {
        return new QName(Constants.microsoftVersion, "MaxReceiveBufferSize");
    }

  


    public abstract Class getAcksToClass();

    public  AddressingVersion getAddressingVersion() {
        return addressingVersion;
    }



    public URI getAnonymousURI() {
        try {

            return new URI(getAddressingVersion().anonymousUri);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }

    }
}
   


