package com.sun.xml.ws.rm;

import com.sun.xml.bind.api.JAXBRIContext;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is the class which will determine which RM version we are dealing with
 * WSRM 1.0 or WSRM 1.1
 */
public enum RMVersion {

    WSRM10("http://schemas.xmlsoap.org/ws/2005/02/rm",
            "http://schemas.xmlsoap.org/ws/2005/02/rm/policy",
            "com.sun.xml.ws.rm.protocol"

    ) {

        @Override
        public String getNamespaceURI(){
            return namespaceUri;
        }

        @Override
        public String getPolicyNamespaceURI(){
            return policyNamespaceUri;
        }

       @Override
        public  String getPackageName() {
            return packageName;
        }


    },
    WSRM11("http://docs.oasis-open.org/ws-rx/wsrm/200702",
            "http://docs.oasis-open.org/ws-rx/wsrmp/200702",
            "com.sun.xml.ws.rm.v200702") {

        @Override
        public String getNamespaceURI (){
            return namespaceUri;
        }

        @Override
        public String getPolicyNamespaceURI(){
            return policyNamespaceUri;
        }

        @Override
        public  String getPackageName() {
            return packageName;
        }

    };

    public  final String namespaceUri;

    public  final String policyNamespaceUri;

    public final String packageName;

    private  JAXBRIContext jc;

    public  JAXBRIContext jaxbricontext;


    private RMVersion(String nsUri ,String policynsuri, String packagename) {
        this.namespaceUri = nsUri;
        this.policyNamespaceUri = policynsuri;
        this.packageName = packagename;
        init();

    }

    public abstract String getNamespaceURI();

    public abstract String getPolicyNamespaceURI();

    public abstract String getPackageName();

    public  String getCreateSequenceAction() {
        return namespaceUri +"CreateSequence";
    }

    public  String getTerminateSequenceAction() {
        return namespaceUri + "TerminateSequence";
    }


    public  String getAckRequestedAction() {
        return namespaceUri +"AckRequested";
    }


    public  String getLastMessageAction() {
        return namespaceUri + "LastMessage";
    }


    public  String getCreateSequenceResponseAction() {
        return namespaceUri + "CreateSequenceResponse";
    }

    public  String getSequenceAcknowledgementAction() {
        return namespaceUri + "SequenceAcknowledgement";
    }

    

    /**
     * Returns {@link RMVersion} whose {@link #nsUri} equals to
     * the given string.
     *
     *
     *
     * @param nsUri
     *      must not be null.
     * @return always non-null.
     */
    public static RMVersion fromNsUri(String nsUri) {
        if (nsUri.equals(WSRM10.namespaceUri)) {
            return WSRM10;

        } else   {
            //return WSRM 1.1 by default
            return WSRM11;
        }


    }

      private List<Class> getHeaderClassesToBeBound() throws RMException{
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
            classList.add(RMConstants.W3C.getAcksToClass());
            return classList;
        } catch (ClassNotFoundException e) {
            throw new RMException("Cannot bind the following class with JAXBContext" + e);
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
            classList.add( RMConstants.W3C.getAcksToClass());
            return classList;
        } catch (ClassNotFoundException e) {
            throw new RMException("Cannot bind the following class with JAXBContext" + e);
        }
    }

     private void init(){
        try {

            List<Class> classes = getClassesToBeBound();
            jc = JAXBRIContext.newInstance(classes.toArray(new Class[0]),null,null,null,false,null);

            Class[] clazzes = getHeaderClassesToBeBound().toArray(new Class[0]);
            jaxbricontext = JAXBRIContext.newInstance(clazzes,
                                                        null,
                                                        null, null,false, null);

        } catch (JAXBException e) {
            throw new Error(e);
        } catch(RMException e ) {
            throw new Error(e);
        }

    }


    public  JAXBRIContext getJAXBContext() {
        return jc;
    }

    public JAXBRIContext getJAXBRIContextHeaders() {
        return jaxbricontext;
    }

    public Marshaller createMarshaller() {
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

}
