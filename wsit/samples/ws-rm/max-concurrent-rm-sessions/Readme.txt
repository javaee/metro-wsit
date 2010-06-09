max-concurrent-rm-sessions sample demonstrates the proprietary
metrormp:MaxConcurrentSessions policy assertion introduced by Metro Reliable Messaging
implementation. This sample requires Metro 2.1 or later.

In etc/AddNumbers.wsdl, Notice the use of
<wsp:PolicyReference URI="#Binding_Policy"/> in the binding section.

The attached policy in the WSDL specifies the requirement for the use of
WS-Addressing, WS-ReliableMessaging and metrormp:MaxConcurrentSessions through
the policy defined as

<wsp:Policy wsu:Id="Binding_Policy">
    <wsp:ExactlyOne>
        <wsp:All>
            <wsam:Addressing wsp:Optional="false"/>

            <wsrmp:RMAssertion>
                <wsp:Policy>
                    <wsrmp:DeliveryAssurance>
                        <wsp:Policy>
                            <wsrmp:InOrder/>
                        </wsp:Policy>
                    </wsrmp:DeliveryAssurance>
                </wsp:Policy>
            </wsrmp:RMAssertion>

            <metrormp:MaxConcurrentSessions>2</metrormp:MaxConcurrentSessions>

        </wsp:All>
    </wsp:ExactlyOne>
</wsp:Policy>

* etc - configuration files
    * AddNumbers.wsdl wsdl file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * metro_sample/client/AddNumbersClient.java - client application
    * metro_sample/server/AddNumberImpl.java - server implementation

* wsimport ant task is run to compile etc/AddNumbers.wsdl
    * generates
      SEI - AddNumbersPortType
      service class - AddNumbersService
      and exception class - AddNumbersFault_Exception

* To run
    * set METRO_HOME to the Metro installation directory
    * ant clean server - runs wsimport to compile AddNumbers.wsdl and generate
      server side artifacts and does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to users@metro.dev.java.net.
