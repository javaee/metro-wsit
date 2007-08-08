package com.sun.xml.ws.rm;

/**
 * This is the class which will determine which RM version we are dealing with
 * WSRM 1.0 or WSRM 1.1
 */
public enum RMVersion {

    WSRM10("http://schemas.xmlsoap.org/ws/2005/02/rm",
            "http://schemas.xmlsoap.org/ws/2005/02/rm/policy"
    ) {

        @Override
        public String getNamespaceURI(){
            return namespaceUri;
        }

        @Override
        public String getPolicyNamespaceURI(){
            return policyNamespaceUri;
        }


    },
    WSRM11("http://docs.oasis-open.org/ws-rx/wsrm/200702",
            "http://docs.oasis-open.org/ws-rx/wsrmp/200702") {

        @Override
        public String getNamespaceURI (){
            return namespaceUri;
        }

        @Override
        public String getPolicyNamespaceURI(){
            return policyNamespaceUri;
        }


    };

    public  final String namespaceUri;

    public  final String policyNamespaceUri;


    private RMVersion(String nsUri ,String policynsuri) {
        this.namespaceUri = nsUri;
        this.policyNamespaceUri = policynsuri;

    }

    public abstract String getNamespaceURI();

    public abstract String getPolicyNamespaceURI();



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

}
