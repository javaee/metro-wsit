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
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import com.sun.xml.bind.api.JAXBRIContext; 
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
    private JAXBContext jc;
    
    public JAXBRIContext jaxbricontext;


    private RMConstants(AddressingVersion addVersion) {
        this.addressingVersion = addVersion;
        init();

    }

    public static RMConstants getRMConstants(AddressingVersion version) {
        if(version == AddressingVersion.W3C) {
            return RMConstants.W3C;
        } else {
            return RMConstants.MEMBER;
        }
    }

    private void init(){
        try {

            List<Class> classes = getClassesToBeBound();
            jc = JAXBContext.newInstance(classes.toArray(new Class[0]));
            
            Class[] clazzes = getHeaderClassesToBeBound().toArray(new Class[0]);
            jaxbricontext = JAXBRIContext.newInstance(clazzes, 
                                                        null, 
                                                        null, false);
            
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
    private  String getPackageName() {
        return Constants.PROTOCOL_PACKAGE_NAME;
    }


    /**
     * Returns the namespace URI for the WS-RM spec.
     *
     * @return The URI (http://schemas.xmlsoap.org/ws/2005/02/rm)
     */
    public String getNamespaceURI() {
        return Constants.WS_RM_NAMESPACE;
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
     * Returns the QName of the TerminateSequence element defined by the WS-RM schema.
     *
     * @return The QName.
     */
    public  QName getTerminateSequenceQName() {
        return new QName(getNamespaceURI(), "TerminateSequence");
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

    public  JAXBContext getJAXBContext() {
        return jc;
    }
    
    public JAXBRIContext getJAXBRIContext() {
        return jaxbricontext;
    }

    public  Marshaller createMarshaller() {
        try {
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            return marshaller;
        } catch (JAXBException e) {
            return null;
        }
    }

    public Unmarshaller createUnmarshaller()  {
        try {
            return jc.createUnmarshaller();
        } catch (JAXBException e) {
            return null;
        }
    }


    private  List<Class> getClassesToBeBound() throws RMException{
        final Class[] classes;
        final ArrayList<Class> classList;
        try {
            classes = new Class[]{
                    Class.forName(getPackageName()+ ".AckRequestedElement"),
                    Class.forName(getPackageName()+ ".SequenceElement"),
                     Class.forName(getPackageName()+ ".SequenceAcknowledgementElement"),
                    Class.forName(getPackageName()+ ".Identifier"),
                    Class.forName(getPackageName()+ ".CreateSequenceElement"),
                    Class.forName(getPackageName()+ ".CreateSequenceResponseElement"),                  
                    Class.forName(getPackageName()+ ".SequenceFaultElement"),
                    Class.forName(getPackageName()+ ".TerminateSequenceElement"),
                    Class.forName(getPackageName()+ ".AcceptType"),                    
                    Class.forName(getPackageName()+ ".OfferType"),
                    Class.forName(getPackageName()+ ".Expires")
            };
            classList = new ArrayList<Class>(Arrays.asList(classes));
            classList.add(getAcksToClass());
            return classList;
        } catch (ClassNotFoundException e) {
            throw new RMException("Cannot bind the following class with JAXBContext" + e);
        }
    }
    
     private  List<Class> getHeaderClassesToBeBound() throws RMException{
        final Class[] classes;
        final ArrayList<Class> classList;
        try {
            classes = new Class[]{
                    Class.forName(getPackageName()+ ".AckRequestedElement"),
                    Class.forName(getPackageName()+ ".SequenceElement"),
                     Class.forName(getPackageName()+ ".SequenceAcknowledgementElement"),
                    Class.forName(getPackageName()+ ".Identifier"),
                    
            };
            classList = new ArrayList<Class>(Arrays.asList(classes));
            classList.add(getAcksToClass());
            return classList;
        } catch (ClassNotFoundException e) {
            throw new RMException("Cannot bind the following class with JAXBContext" + e);
        }
    }

    public abstract Class getAcksToClass();

    public  AddressingVersion getAddressingVersion() {
        return addressingVersion;
    }



    public URI getAnonymousURI() {
        try {

            return new URI(getAddressingVersion().getAnonymousUri());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }

    }
}
   


