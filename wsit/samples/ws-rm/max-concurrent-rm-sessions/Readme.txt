#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

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
