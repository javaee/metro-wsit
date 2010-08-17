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

PARENT_MODULE_POM_TEMPLATE="./poms/_wsit-module-parent-pom.xml"
POM_TEMPLATE="./poms/_wsit-module-pom.xml"
WSIT_MODULE_ROOT="$NEW_PROJECT_ROOT/wsit"

#
# Metro configuration project
#
CONFIG_MODULE_ROOT="$WSIT_MODULE_ROOT/config"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$CONFIG_MODULE_ROOT" -n "Metro Configuration Project" -i "config" -P "wsit-project" -p ./poms/config-project-pom.xml

#
# Metro configuration API
#
MODULE_ROOT="$CONFIG_MODULE_ROOT/config-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "Metro Configuration API" -i "config-api" -P "config" -p ./poms/config-api-pom.xml
SRC_ARTIFACTS="\
com/sun/xml/ws/config/metro/dev:\
com/sun/xml/ws/config/metro/util:\
com/sun/xml/ws/runtime/config:\
com/sun/xml/ws/policy/config/PolicyFeature.java"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# Metro configuration Implementation
#
MODULE_ROOT="$CONFIG_MODULE_ROOT/config-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "Metro Configuration Implementation" -i "config-impl" -P "config" -p ./poms/config-impl-pom.xml
SRC_ARTIFACTS="\
com/sun/xml/ws/config/metro:\
com/sun/xml/ws/policy/jcaps:\
com/sun/xml/ws/policy/localization:\
com/sun/xml/ws/policy/parser:\
com/sun/xml/ws/policy/WsitPolicyUtil.java:\
com/sun/xml/ws/policy/config/PolicyFeatureReader.java"
TEST_ARTIFACTS="$SRC_ARTIFACTS:com/sun/xml/ws/policy/testutils"
TEST_RESOURCES="policy:config"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.policy.jcaps.JCapsPolicyValidator" "com.sun.xml.ws.policy.spi.PolicyAssertionValidator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.policy.parser.WsitPolicyResolverFactory" "com.sun.xml.ws.api.policy.PolicyResolverFactory"

#
# XML document filter API
#
MODULE_ROOT="$WSIT_MODULE_ROOT/xmlfilter"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WSIT XML Document Filtering Project" -i "xmlfilter" -P "wsit-project" -p ./poms/xmlfilter-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/xmlfilter"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="xmlfilter"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT MEX
#
MODULE_ROOT="$WSIT_MODULE_ROOT/ws-mex"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-MetadataExchange Service Implementation" -i "ws-mex" -P "wsit-project" -p ./poms/wsmex-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/mex"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.mex.client.MetadataResolverFactoryImpl" "com.sun.xml.ws.api.wsdl.parser.MetadataResolverFactory"

#
# WSIT SOAP/TCP Transport
#
SOAPTCP_MODULE_ROOT="$WSIT_MODULE_ROOT/soaptcp"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$SOAPTCP_MODULE_ROOT" -n "WSIT SOAP over TCP Transport Project" -i "soaptcp" -P "wsit-project" -p ./poms/soaptcp-project-pom.xml

MODULE_ROOT="$SOAPTCP_MODULE_ROOT/legacy-dependencies/gfv2-deployment"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "Glassfish v2 Deplyoment Classes" -i "gfv2-deployment" -P "soaptcp" -p ./poms/gfv2-deployment-pom.xml
if [ ! -e "$MODULE_ROOT/lib" ] ; then
    mkdir -p $VERBOSE "$MODULE_ROOT/lib"
fi
cp $VERBOSE "$OLD_METRO_LIB_DIR/compiletime/appserv-deployment.jar" "$MODULE_ROOT/lib/gfv2-deployment.jar"

#
# WSIT SOAP/TCP Transport API
#
MODULE_ROOT="$SOAPTCP_MODULE_ROOT/soaptcp-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "SOAP over TCP Transport API" -i "soaptcp-api" -P "soaptcp" -p ./poms/soaptcp-api-pom.xml
SRC_ARTIFACTS="\
com/sun/xml/ws/transport/SelectOptimalTransport.java:\
com/sun/xml/ws/transport/SelectOptimalTransportFeature.java:\
com/sun/xml/ws/transport/TcpTransport.java:\
com/sun/xml/ws/transport/TcpTransportFeature.java:\
com/sun/xml/ws/transport/TcpTransportFeatureReader.java:\
com/sun/xml/ws/transport/tcp/util/TCPConstants.java"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT SOAP/TCP Transport Implementation
#
MODULE_ROOT="$SOAPTCP_MODULE_ROOT/soaptcp-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "SOAP over TCP Transport Implementation" -i "soaptcp-impl" -P "soaptcp" -p ./poms/soaptcp-impl-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/transport"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.transport.tcp.policy.TCPTransportFeatureConfigurator:com.sun.xml.ws.transport.tcp.policy.OptimalTransportFeatureConfigurator" "com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.transport.tcp.policy.TCPTransportPolicyMapConfigurator:com.sun.xml.ws.transport.tcp.policy.OptimalTransportPolicyMapConfigurator" "com.sun.xml.ws.policy.jaxws.spi.PolicyMapConfigurator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.transport.tcp.wsit.TCPTransportPolicyValidator" "com.sun.xml.ws.policy.spi.PolicyAssertionValidator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.transport.tcp.wsit.TCPTransportPrefixMapper" "com.sun.xml.ws.policy.spi.PrefixMapper"

#
# WSIT Commons
#
MODULE_ROOT="$WSIT_MODULE_ROOT/commons"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WSIT Common Utilities and Classes" -i "commons" -P "wsit-project" -p ./poms/wsit-commons-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/commons"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT Runtime API
#
MODULE_ROOT="$WSIT_MODULE_ROOT/runtime-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WSIT Runtime API" -i "runtime-api" -P "wsit-project" -p ./poms/wsit-rt-api-pom.xml
SRC_ARTIFACTS="\
com/sun/xml/ws/assembler/dev:\
com/sun/xml/ws/assembler/ServerPipelineHook.java:\
com/sun/xml/ws/runtime/dev"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT Runtime Implementation
#
MODULE_ROOT="$WSIT_MODULE_ROOT/runtime-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WSIT Runtime Implementation" -i "runtime-impl" -P "wsit-project" -p ./poms/wsit-rt-impl-pom.xml
SRC_ARTIFACTS="\
com/sun/xml/ws/assembler:\
com/sun/xml/ws/dump:\
com/sun/xml/ws/runtime"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="\
assembler:\
metro-config:\
wsdl_filter"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.assembler.TubelineAssemblerFactoryImpl" "com.sun.xml.ws.api.pipe.TubelineAssemblerFactory"

#
# WSIT Configuration management
#
WSCM_MODULE_ROOT="$WSIT_MODULE_ROOT/ws-cm"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$WSCM_MODULE_ROOT" -n "WS-ConfigurationManagement Project" -i "wscm-project" -P "wsit-project" -p ./poms/wscm-project-pom.xml
#
# WSIT Configuration management API
#
MODULE_ROOT="$WSCM_MODULE_ROOT/wscm-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-ConfigurationManagement API" -i "wscm-api" -P "wscm-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/api/config/management:com/sun/xml/ws/config/management/Management.properties"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT Configuration management Impl
#
MODULE_ROOT="$WSCM_MODULE_ROOT/wscm-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-ConfigurationManagement Implementation" -i "wscm-impl" -P "wscm-project" -p ./poms/wscm-impl-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/config/management"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="management"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.config.management.server.EndpointFactoryImpl" "com.sun.xml.ws.api.config.management.ManagedEndpointFactory"

#
# WSIT WS-RX Parent project
#
RX_MODULE_ROOT="$WSIT_MODULE_ROOT/ws-rx"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$RX_MODULE_ROOT" -n "WS-RX Project" -i "wsrx-project" -P "wsit-project" -p ./poms/wsrx-project-pom.xml
#
# WSIT WS-RX common packages
#
MODULE_ROOT="$RX_MODULE_ROOT/wsrx-commons"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-RX Common Utilities and Classes" -i "wsrx-commons" -P "wsrx-project" -p ./poms/wsrx-commons-pom.xml
SRC_ARTIFACTS="\
com/sun/xml/ws/rx/policy:\
com/sun/xml/ws/rx/util:\
com/sun/xml/ws/rx/RxConfiguration.java:\
com/sun/xml/ws/rx/RxConfigurationBase.java:\
com/sun/xml/ws/rx/RxException.java:\
com/sun/xml/ws/rx/RxRuntimeException.java"
TEST_ARTIFACTS="com/sun/xml/ws/rx/policy:com/sun/xml/ws/rx/util:com/sun/xml/ws/rx/RxConfigurationTest.java:com/sun/xml/ws/rx/RxConfigurationBaseTest.java:com/sun/xml/ws/rx/RxExceptionTest.java:com/sun/xml/ws/rx/RxRuntimeExceptionTest.java"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-RX testing support
#
MODULE_ROOT="$RX_MODULE_ROOT/wsrx-testing"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-RX Testing Support" -i "wsrx-testing" -P "wsrx-project" -p ./poms/wsrx-testing-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/rx/testing"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-RM API
#
MODULE_ROOT="$RX_MODULE_ROOT/wsrm-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-RealiableMessaging API" -i "wsrm-api" -P "wsrx-project" -p ./poms/wsrm-api-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/rx/rm/api"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-RM Impl
#
MODULE_ROOT="$RX_MODULE_ROOT/wsrm-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-RealiableMessaging Implementation" -i "wsrm-impl" -P "wsrx-project" -p ./poms/wsrm-impl-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/rx/rm"
TEST_ARTIFACTS="$SRC_ARTIFACTS:com/sun/xml/ws/rx/testutil"
TEST_RESOURCES="rm"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.rx.rm.policy.spi_impl.RmFeatureConfigurator" "com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.rx.rm.policy.spi_impl.RmPolicyMapConfigurator" "com.sun.xml.ws.policy.jaxws.spi.PolicyMapConfigurator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.rx.rm.policy.spi_impl.RmAssertionCreator" "com.sun.xml.ws.policy.spi.PolicyAssertionCreator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.rx.rm.policy.spi_impl.RmAssertionValidator" "com.sun.xml.ws.policy.spi.PolicyAssertionValidator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.rx.rm.policy.spi_impl.RmPrefixMapper" "com.sun.xml.ws.policy.spi.PrefixMapper"

#
# WSIT WS-MC API
#
MODULE_ROOT="$RX_MODULE_ROOT/wsmc-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-MakeConnection API" -i "wsmc-api" -P "wsrx-project" -p ./poms/wsmc-api-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/rx/mc/api:com/sun/xml/ws/rx/mc/dev"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-MC Impl
#
MODULE_ROOT="$RX_MODULE_ROOT/wsmc-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-MakeConnection Implementation" -i "wsmc-impl" -P "wsrx-project" -p ./poms/wsmc-impl-pom.xml
SRC_ARTIFACTS="com/sun/xml/ws/rx/mc"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.rx.mc.policy.spi_impl.McFeatureConfigurator" "com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.rx.mc.policy.spi_impl.McPolicyMapConfigurator" "com.sun.xml.ws.policy.jaxws.spi.PolicyMapConfigurator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.rx.mc.policy.spi_impl.McAssertionCreator" "com.sun.xml.ws.policy.spi.PolicyAssertionCreator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.rx.mc.policy.spi_impl.McAssertionValidator" "com.sun.xml.ws.policy.spi.PolicyAssertionValidator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.rx.mc.policy.spi_impl.McPrefixMapper" "com.sun.xml.ws.policy.spi.PrefixMapper"


#
# WSIT WS-SX Parent project
#
SX_MODULE_ROOT="$WSIT_MODULE_ROOT/ws-sx"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$SX_MODULE_ROOT" -n "WS-Security Project" -i "wssx-project" -P "wsit-project" -p ./poms/wssx-project-pom.xml
#
# WSIT WS-SX API
#
MODULE_ROOT="$SX_MODULE_ROOT/wssx-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-SX API" -i "wssx-api" -P "wssx-project" -p ./poms/wssx-api-pom.xml
SRC_ARTIFACTS="\
com/sun/xml/ws/api/security:\
com/sun/xml/ws/policy/impl/bindings:\
com/sun/xml/ws/security/EncryptedKey.java:\
com/sun/xml/ws/security/SecurityContextToken.java:\
com/sun/xml/ws/security/SecurityTokenReference.java:\
com/sun/xml/ws/security/DerivedKeyToken.java:\
com/sun/xml/ws/security/IssuedTokenContext.java:\
com/sun/xml/ws/security/Token.java:\
com/sun/xml/ws/security/SecurityContextTokenInfo.java:\
com/sun/xml/ws/security/policy/SecurityPolicyVersion.java:\
com/sun/xml/ws/security/policy/Token.java:\
com/sun/xml/ws/security/impl/IssuedTokenContextImpl.java:\
com/sun/xml/ws/security/secconv/SecureConversationInitiator.java:\
com/sun/xml/ws/security/secconv/WSSecureConversationException.java:\
com/sun/xml/ws/security/secconv/WSSCVersion.java:\
com/sun/xml/ws/security/secconv/impl/WSSCVersion10.java:\
com/sun/xml/ws/security/secconv/impl/wssx/WSSCVersion13.java:\
com/sun/xml/ws/security/secext10:\
com/sun/xml/ws/security/trust/WSTrustConstants.java:\
com/sun/xml/ws/security/trust/WSTrustElementFactory.java:\
com/sun/xml/ws/security/trust/WSTrustVersion.java:\
com/sun/xml/ws/security/trust/elements:\
com/sun/xml/ws/security/trust/impl/WSTrustVersion10.java:\
com/sun/xml/ws/security/trust/impl/wssx/WSTrustVersion13.java:\
com/sun/xml/ws/security/wsu10:\
com/sun/xml/wss/XWSSecurityException.java"

TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-SX implementation
#
MODULE_ROOT="$SX_MODULE_ROOT/wssx-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-SX Implementation" -i "wssx-impl" -P "wssx-project" -p ./poms/wssx-impl-pom.xml
SRC_ARTIFACTS="\
com/sun/wsit/security:\
com/sun/xml/messaging:\
com/sun/xml/rpc:\
com/sun/xml/security:\
com/sun/xml/ws/installer/UpdateSharedLoaderProp.java:\
com/sun/xml/ws/policy/impl/bindings:\
com/sun/xml/ws/security:\
com/sun/xml/wss:\
com/sun/xml/xwss"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="security"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

./register-providers.sh $MODULE_ROOT "com.sun.xml.wss.provider.wsit.IdentityEPRExtnContributor" "com.sun.xml.ws.api.server.EndpointReferenceExtensionContributor"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.security.addressing.policy.WsawAddressingFeatureConfigurator" "com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.security.addressing.policy.WsawAddressingPolicyMapConfigurator" "com.sun.xml.ws.policy.jaxws.spi.PolicyMapConfigurator"

PROVIDERS="\
com.sun.xml.ws.security.addressing.impl.policy.AddressingPolicyAssertionCreator:\
com.sun.xml.ws.security.impl.policy.SecurityPolicyAssertionCreator:\
com.sun.xml.ws.security.impl.policy.TrustPolicyAssertionCreator:\
com.sun.xml.ws.security.impl.policy.WSSClientConfigAssertionCreator:\
com.sun.xml.ws.security.impl.policy.WSSServerConfigAssertionCreator:\
com.sun.xml.ws.security.impl.policy.TrustClientConfigAssertionCreator:\
com.sun.xml.ws.security.impl.policy.TrustServerConfigAssertionCreator:\
com.sun.xml.ws.security.impl.policy.SCClientConfigAssertionCreator:\
com.sun.xml.ws.security.impl.policy.SCServerConfigAssertionCreator"
./register-providers.sh $MODULE_ROOT $PROVIDERS "com.sun.xml.ws.policy.spi.PolicyAssertionCreator"

PROVIDERS="\
com.sun.xml.ws.security.addressing.policy.WsawAddressingPolicyValidator:\
com.sun.xml.ws.security.impl.policy.SecurityPolicyValidator"
./register-providers.sh $MODULE_ROOT $PROVIDERS "com.sun.xml.ws.policy.spi.PolicyAssertionValidator"

PROVIDERS="\
com.sun.xml.ws.security.addressing.policy.WsawAddressingPrefixMapper:\
com.sun.xml.ws.security.impl.policy.SecurityPrefixMapper"
./register-providers.sh $MODULE_ROOT $PROVIDERS "com.sun.xml.ws.policy.spi.PrefixMapper"

#
# WSIT WS-TX Parent project
#
TX_MODULE_ROOT="$WSIT_MODULE_ROOT/ws-tx"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$TX_MODULE_ROOT" -n "WS-TX Project" -i "wstx-project" -P "wsit-project" -p $PARENT_MODULE_POM_TEMPLATE

echo "TODO: Uncomment ws-tx module in the WSIT project (?)"

#
# WSIT WS-TX API
#
MODULE_ROOT="$TX_MODULE_ROOT/wstx-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "WS-TX API" -i "wstx-api" -P "wstx-project" -p $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/tx/at/api"
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

./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.tx.at.policy.spi_impl.AtFeatureConfigurator" "com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.tx.at.policy.spi_impl.AtPolicyMapConfigurator" "com.sun.xml.ws.policy.jaxws.spi.PolicyMapConfigurator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.tx.at.policy.spi_impl.AtAssertionCreator" "com.sun.xml.ws.policy.spi.PolicyAssertionCreator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.tx.at.policy.spi_impl.AtAssertionValidator" "com.sun.xml.ws.policy.spi.PolicyAssertionValidator"
./register-providers.sh $MODULE_ROOT "com.sun.xml.ws.tx.at.policy.spi_impl.AtPrefixMapper" "com.sun.xml.ws.policy.spi.PrefixMapper"

echo "TODO: create WS-TX WAR module (?)"


echo "TODO: Clean-up WSIT module dependencies"
echo "TODO: turn on Woodstox for Metro/WSIT (via service providers)"
echo "TODO: Migrate ToolPlugin.xml"
echo "TODO: Migrate metro-default.xml"
echo "TODO: Migrate metro XSD schemas"
echo "TODO: Migrate metro durable RM SQL script"