#!/bin/sh
USAGE="Usage: migrate-core.sh [-hvf]"

# parse command line arguments
OPTIND=1
while getopts 'hvf' OPT; do
    case "$OPT" in
	h)  echo $USAGE
            exit 0
            ;;
	v)  VERBOSE="-v"
            ;;
	f)  FORCE_RM_FLAG="-f"
            ;;
	?)  # all other characters - error
            echo $USAGE >&2
            exit 1
            ;;
    esac
done
shift `expr $OPTIND - 1`

# access additional parameters through $@ or $* as usual or using this loop:
# for PARAM; do
#    echo $PARAM
# done

#COPY_ONLY_FLAG="-n"
PARENT_MODULE_POM_TEMPLATE="./poms/wsit-module-parent-pom.xml"
POM_TEMPLATE="./poms/wsit-module-pom.xml"
WSIT_MODULE_ROOT="$NEW_PROJECT_ROOT/wsit"

#
# WSIT policy configuration file handling
# TODO: split into submodules
#
MODULE_ROOT="$WSIT_MODULE_ROOT/wsit-config"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WSIT Policy Configuration Project" -i "wsit-config" -P "wsit-project" -p ./poms/wsit-config-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/policy"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="policy"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT Xml document filter API
#
MODULE_ROOT="$WSIT_MODULE_ROOT/xml-filter-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "XML document filtering API" -i "xml-filter-api" -P "wsit-project" -p ./poms/xmlfilterapi-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/xmlfilter"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="xmlfilter"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT MEX
# TODO: split into submodules
#
MODULE_ROOT="$WSIT_MODULE_ROOT/wsmex"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-MetadataExchange Project" -i "wsmex" -P "wsit-project" -p ./poms/wsmex-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/mex"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT Core
# TODO: split into submodules
#
MODULE_ROOT="$WSIT_MODULE_ROOT/wsit-core"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WSIT Core" -i "wsit-core" -P "wsit-project" -p ./poms/wsit-core-pom.xml
SRC_ARTIFACTS="\
com/sun/xml/ws/assembler:com/sun/xml/ws/commons:\
com/sun/xml/ws/dump:\
com/sun/xml/ws/runtime:\
com/sun/xml/ws/security/SecurityContextTokenInfo.java:\
com/sun/xml/ws/security/IssuedTokenContext.java:\
com/sun/xml/ws/security/secconv/SecureConversationInitiator.java"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="\
assembler:\
metro-config:\
wsdl_filter"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT Configuration management
#
WSCM_MODULE_ROOT="$WSIT_MODULE_ROOT/wscm"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$WSCM_MODULE_ROOT" -n "WS-ConfigurationManagement Project" -i "wscm-project" -P "wsit-project" -p $PARENT_MODULE_POM_TEMPLATE
#
# WSIT Configuration management API
#
MODULE_ROOT="$WSCM_MODULE_ROOT/wscm-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-ConfigurationManagement API" -i "wscm-api" -P "wscm-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/api/config"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT Configuration management Impl
#
MODULE_ROOT="$WSCM_MODULE_ROOT/wscm-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-ConfigurationManagement Implementation" -i "wscm-impl" -P "wscm-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/config"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="management"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

mkdir -p $VERBOSE $MODULE_ROOT/src/test/resources/META-INF/services
echo "com.sun.xml.ws.policy.parser.PolicyWSDLParserExtension" >> $MODULE_ROOT/src/test/resources/META-INF/services/com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension

echo "com.sun.xml.ws.addressing.policy.AddressingFeatureConfigurator" >> $MODULE_ROOT/src/test/resources/META-INF/services/com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator
echo "com.sun.xml.ws.encoding.policy.MtomFeatureConfigurator" >> $MODULE_ROOT/src/test/resources/META-INF/services/com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator
echo "com.sun.xml.ws.encoding.policy.FastInfosetFeatureConfigurator" >> $MODULE_ROOT/src/test/resources/META-INF/services/com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator
echo "com.sun.xml.ws.encoding.policy.SelectOptimalEncodingFeatureConfigurator" >> $MODULE_ROOT/src/test/resources/META-INF/services/com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator

echo "com.sun.xml.ws.addressing.policy.AddressingPolicyValidator" >> $MODULE_ROOT/src/test/resources/META-INF/services/com.sun.xml.ws.policy.spi.PolicyAssertionValidator
echo "com.sun.xml.ws.policy.jcaps.JCapsPolicyValidator" >> $MODULE_ROOT/src/test/resources/META-INF/services/com.sun.xml.ws.policy.spi.PolicyAssertionValidator

#rm $VERBOSE $MODULE_ROOT/src/test/java/com/sun/xml/ws/policy/parser/PolicyConfigParserTest.java
echo "TODO: Fix unit test: com.sun.xml.ws.policy.parser.PolicyConfigParserTest.java"
#rm $VERBOSE $MODULE_ROOT/src/test/java/com/sun/xml/ws/policy/parser/PolicyWSDLParserExtensionTest.java
echo "TODO: Fix unit test: com.sun.xml.ws.policy.parser.PolicyWSDLParserExtensionTest.java"

#
# WSIT WS-RX Parent project
# TODO: split rx/policy
#
RX_MODULE_ROOT="$WSIT_MODULE_ROOT/wsrx"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$RX_MODULE_ROOT" -n "WS-RX Project" -i "wsrx-project" -P "wsit-project" -p $PARENT_MODULE_POM_TEMPLATE
#
# WSIT WS-RX common packages
#
MODULE_ROOT="$RX_MODULE_ROOT/commons"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "" -i "wsrx-commons" -P "wsrx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/rx/testing:com/sun/xml/ws/rx/policy:com/sun/xml/ws/rx/util:com/sun/xml/ws/rx/RxConfiguration.java:com/sun/xml/ws/rx/RxConfigurationBase.java:com/sun/xml/ws/rx/RxException.java:com/sun/xml/ws/rx/RxRuntimeException.java"
TEST_ARTIFACTS="com/sun/xml/ws/rx/testing:com/sun/xml/ws/rx/policy:com/sun/xml/ws/rx/util:com/sun/xml/ws/rx/RxConfigurationTest.java:com/sun/xml/ws/rx/RxConfigurationBaseTest.java:com/sun/xml/ws/rx/RxExceptionTest.java:com/sun/xml/ws/rx/RxRuntimeExceptionTest.java"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-RM
# TODO: split into submodules
#
MODULE_ROOT="$RX_MODULE_ROOT/wsrm-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-RealiableMessaging Implementation" -i "wsrm-impl" -P "wsrx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/rx/rm"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="rm"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-MC
# TODO: split into submodules
#
MODULE_ROOT="$RX_MODULE_ROOT/wsmc-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-MakeConnection Implementation" -i "wsmc-impl" -P "wsrx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/rx/mc"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT WS-SX Parent project
# TODO: split into submodules
#
SX_MODULE_ROOT="$WSIT_MODULE_ROOT/wssx"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$SX_MODULE_ROOT" -n "WS-Security Project" -i "wssx-project" -P "wsit-project" -p $PARENT_MODULE_POM_TEMPLATE
#
# WSIT WS-Security implementation
#
MODULE_ROOT="$SX_MODULE_ROOT/wss-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-Security Implementation" -i "wss-impl" -P "wssx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="\
com/sun/xml/wss:\
com/sun/xml/ws/api/security/CallbackHandlerFeature.java:\
com/sun/xml/ws/security/spi"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="\
security/keystore:\
security/policy-binding1.xml:\
security/policy-binding2.xml"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-SecurityPolicy implementation
#
MODULE_ROOT="$SX_MODULE_ROOT/wss-policy-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-SecurityPolicy Implementation" -i "wss-policy-impl" -P "wssx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="\
com/sun/xml/ws/security/addressing:\
com/sun/xml/ws/security/encoding:\
com/sun/xml/ws/security/message:\
com/sun/xml/ws/security/policy:\
com/sun/xml/ws/security/impl/policy:\
com/sun/xml/ws/security/impl/policyconv"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="security"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-SecureConversation API
#
MODULE_ROOT="$SX_MODULE_ROOT/wssc-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-SecureConversation API" -i "wssc-api" -P "wssx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/api/security/secconv"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-SecureConversation implementation
#
MODULE_ROOT="$SX_MODULE_ROOT/wssc-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-SecureConversation Implementation" -i "wssc-impl" -P "wssx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/security/secconv"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-Trust implementation (must go before API to make sure proper sources are moved)
#
MODULE_ROOT="$SX_MODULE_ROOT/wstrust-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-Trust Implementation" -i "wstrust-impl" -P "wssx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="\
com/sun/xml/ws/security/trust/util:\
com/sun/xml/ws/security/trust/impl:\
com/sun/xml/ws/security/trust/sts"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-Trust API
#
MODULE_ROOT="$SX_MODULE_ROOT/wstrust-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-Trust API" -i "wstrust-api" -P "wssx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="\
com/sun/xml/ws/api/security/trust:\
com/sun/xml/ws/security/trust:\
com/sun/xml/ws/security/trust/elements"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT SOAP/TCP Transport
# TODO: split into submodules
#
MODULE_ROOT="$WSIT_MODULE_ROOT/soaptcp"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "SOAP over TCP Transport Project" -i "soaptcp" -P "wsit-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/transport"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT WS-TX Parent project
# TODO wstx-services submodule
#
TX_MODULE_ROOT="$WSIT_MODULE_ROOT/wstx"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$TX_MODULE_ROOT" -n "WS-TX Project" -i "wstx-project" -P "wsit-project" -p $PARENT_MODULE_POM_TEMPLATE
#
# WSIT WS-TX API
#
MODULE_ROOT="$TX_MODULE_ROOT/wstx-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-TX API" -i "wstx-api" -P "wstx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/api/tx"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-TX implementation
#
MODULE_ROOT="$TX_MODULE_ROOT/wstx-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-TX Implementation" -i "wstx-impl" -P "wstx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/tx"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

