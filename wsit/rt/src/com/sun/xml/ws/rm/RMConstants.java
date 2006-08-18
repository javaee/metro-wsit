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

/*
 * RMConstants.java
 *
 * @author Mike Grogan
 * Created on October 9, 2005, 9:14 PM
 *
 */

package com.sun.xml.ws.rm;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingConstants;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;


/**
 * Class contains accessors for constants defined by the 02/2005 version of the 
 * WS-RM specification.
 */
public class RMConstants {

    /*
     * Policy namespaces.
     */
    public static final String version = "http://schemas.xmlsoap.org/ws/2005/02/rm/policy";

    public static final String microsoftVersion = "http://schemas.microsoft.com/net/2005/02/rm/policy";
    
    public static final String sunVersion = "http://sun.com/2006/03/rm";
        
    private static AddressingBuilder ab;

    private static final JAXBContext jc;


    static {
        try {
            //jc = JAXBContext.newInstance(getPackageName()+":com.sun.xml.ws.security.impl.bindings");
            ab = AddressingBuilder.newInstance();
            List<Class> classes = getClassesToBeBound();

            jc = JAXBContext.newInstance(classes.toArray(new Class[0]));
        } catch (JAXBException e) {
            throw new Error(e);
        } catch(RMException e ) {
            throw new Error(e);
        }

    }


    /**
     * Returns the package name for protocol classes.  Used to initialize a JAXBContext.
     *
     * @return the package name (com.sun.xml.ws.rm.protocol)
     */
    private static String getPackageName() {
        return Constants.PROTOCOL_PACKAGE_NAME;
    }


    /**
     * Returns the namespace URI for the WS-RM spec.
     *
     * @return The URI (http://schemas.xmlsoap.org/ws/2005/02/rm)
     */
    public static String getNamespaceURI() {
        return Constants.WS_RM_NAMESPACE;
    }

    /**
     * Returns the <code>AddressingBuilder</code> for the version of WS-Addressing
     * being used.
     *
     * @return The AddressingBuilder
     */
    public  static AddressingBuilder getAddressingBuilder() {
        return ab;
    }

    public static AddressingConstants getAddressingConstants(){
        return ab.newAddressingConstants();
    }

    /**
     * Returns the value of the WS-Addressing Action property for CreateSeqence
     * messages to an RMDestination.
     *
     * @return The Action value (http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequence)
     */
    public String getCreateSequenceAction() {
        return "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequence";
    }

    /**
     * Returns the value of the WS-Addressing Action property for 
     * CreateSeqenceResponse messages
     *
     * @return The Action value (http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse)
     */
    public String getCreateSequenceResponseAction() {
         return "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse";
    }
    /**
     * Returns the QName of the CreateSequence element defined by the WS-RM schema.
     *
     * @return The QName.
     */
    public  QName getCreateSequenceQName() {
        return new QName(getNamespaceURI(), "CreateSequence");
    }

    /**
     * Returns the value of the WS-Addressing Action property for TerminateSeqence
     * messages to an RMDestination.
     *
     * @return The Action value (http://schemas.xmlsoap.org/ws/2005/02/rm/TerminateSequence)
     */
    public String getTerminateSequenceAction() {
        return "http://schemas.xmlsoap.org/ws/2005/02/rm/TerminateSequence";
    }



    /**
     * Returns the QName of the TerminateSequence element defined by the WS-RM schema.
     *
     * @return The QName.
     */
    public  QName getTerminateSequenceQName() {
        return new QName(getNamespaceURI(), "TerminateSequence");
    }

    /**
     * Returns the value of the WS-Addressing Action property stand alone AckRequested
     * messages to an RMDestination.
     *
     * @return The Action value (http://schemas.xmlsoap.org/ws/2005/02/rm/AckRequested)
     */
    public String getAckRequestedAction() {
        return "http://schemas.xmlsoap.org/ws/2005/02/rm/AckRequested";
    }

    /**
     * Returns the QName of the AcksTo element defined by the WS-RM spec.
     *
     * @return The QName.
     */
    public  QName getAcksToQName() {
        return new QName(getNamespaceURI(), "AcksTo");
    }


    /**
     * Returns the value of the WS-Addressing Action property stand alone Sequence
     * messages with Last child.
     *
     * @return The Action value (http://schemas.xmlsoap.org/ws/2005/02/rm/Last)
     */
    public String getLastAction() {
        return "http://schemas.xmlsoap.org/ws/2005/02/rm/LastMessage";
    }

    /**
     * Returns the value of the WS-Addressing Action property for stand alone
     * SequenceAcknowledgement messages.
     *
     * @return The Action value (http://schemas.xmlsoap.org/ws/2005/02/rm/SequenceAcknowledgement)
     */
     public String getSequenceAcknowledgementAction() {
        return "http://schemas.xmlsoap.org/ws/2005/02/rm/SequenceAcknowledgement";
    }

    /**
     * Returns the QName of the Address element defined by the WS-RM spec.
     *
     * @return The QName.
     */
    public  QName getAddressQName() {
        return new QName(getNamespaceURI(), "Address");
    }

    /**
     * Returns the QName of the Offer element defined by the WS-RM spec.
     *
     * @return The QName.
     */
    public QName getOfferQName() {
        return new QName(getNamespaceURI(), "Offer");
    }

    /**
     * Returns the QName of the Identifier element defined by the WS-RM spec.
     *
     * @return The QName.
     */
    public QName getIdentifierQName() {
        return new QName(getNamespaceURI(), "Identifier");
    }

    /**
     * Returns the QName of the AckRequested element defined by the WS-RM spec.
     *
     * @return The QName.
     */
    public QName getAckRequestedQName() {
        return new QName(getNamespaceURI(), "AckRequested");
    }


    /**
     * Returns the QName of the Seqence element defined by the WS-RM spec.
     *
     * @return The QName.
     */
    public  QName getSequenceQName() {
        return new QName(getNamespaceURI(), "Sequence");
    }

    /**
     * Returns the QName of the SequenceAcknowledgement element defined by the WS-RM spec.
     *
     * @return The QName.
     */
    public QName getSequenceAcknowledgementQName() {
        return new QName(getNamespaceURI(),
                "SequenceAcknowledgement");
    }

    //Policy Assertiion QNames
    public QName getRMAssertionQName() {
        return new QName(version, "RMAssertion");
    }

     public QName getOrderedQName() {
        return new QName(sunVersion, "Ordered");
    }
     
    public QName getResendIntervalQName() {
        return new QName(sunVersion, "ResendInterval");
    }
    
    public QName getAckRequestIntervalQName() {
        return new QName(sunVersion, "AckRequestInterval");
    }
    
    public QName getInactivityTimeoutQName() {
        return new QName(version, "InactivityTimeout");
    }
       
    public QName getAcknowledgementIntervalQName() {
        return new QName(version, "AcknowledgementInterval");
    }

    public QName getMillisecondsQName() {
        return new QName(version, "Milliseconds");
    }

    public QName getRMFlowControlQName() {
        return new QName(microsoftVersion, "RmFlowControl");
    }

    public QName getMaxReceiveBufferSizeQName() {
        return new QName(microsoftVersion, "MaxReceiveBufferSize");
    }

    public static JAXBContext getJAXBContext() {
        return jc;
    }

    public static Marshaller createMarshaller() {
        try {
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            return marshaller;
        } catch (JAXBException e) {
            return null;
        }
    }

    public static Unmarshaller createUnmarshaller()  {
        try {
            return jc.createUnmarshaller();
        } catch (JAXBException e) {
            return null;
        }
    }


    private static List<Class> getClassesToBeBound() throws RMException{
        final Class[] classes;
        final ArrayList<Class> classList;
        try {
            classes = new Class[]{
                    Class.forName(getPackageName()+ ".AckRequestedElement"),
                    Class.forName(getPackageName()+ ".CreateSequenceElement"),
                    Class.forName(getPackageName()+ ".CreateSequenceResponseElement"),
                    Class.forName(getPackageName()+ ".SequenceAcknowledgementElement"),
                    Class.forName(getPackageName()+ ".SequenceElement"),
                    Class.forName(getPackageName()+ ".TerminateSequenceElement"),
                    Class.forName(getPackageName()+ ".AcceptType"),
                    Class.forName(getPackageName()+ ".Identifier"),
                    Class.forName(getPackageName()+ ".OfferType"),
                    Class.forName(getPackageName()+ ".Expires")
            };
            classList = new ArrayList<Class>(Arrays.asList(classes));
             if (ab.newAddressingConstants().getPackageName().
                    equals("com.sun.xml.ws.addressing")){

                classList.add(Class.forName(getPackageName()+ ".W3CAcksToImpl"));
            }    else {
                classList.add(Class.forName(getPackageName()+".MemberSubmissionAcksToImpl"));

            }
        } catch (ClassNotFoundException e) {
            throw new RMException("Cannot bind the following class with JAXBContext" + e);
        }
        return classList;
    }

}
