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
POM_TEMPLATE="./poms/wsit-module-pom.xml"
WSIT_MODULE_ROOT="$NEW_PROJECT_ROOT/wsit"

#
# WSIT Core
# TODO: split into submodules
#
MODULE_ROOT="$WSIT_MODULE_ROOT/wsit-core"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-core" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/assembler:com/sun/xml/ws/commons:com/sun/xml/ws/dump:com/sun/xml/ws/runtime"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="assembler:metro-config"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT Configuration management
#
WSCM_MODULE_ROOT="$WSIT_MODULE_ROOT/wscm"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -N -m "$WSCM_MODULE_ROOT" -n "wscm-project" $POM_TEMPLATE
#
# WSIT Configuration management API
#
MODULE_ROOT="$WSCM_MODULE_ROOT/wscm-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wscm-api" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/api/config"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT Configuration management Impl
#
MODULE_ROOT="$WSCM_MODULE_ROOT/wscm-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wscm-impl" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/config"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="management"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT MEX
# TODO: split into submodules
#
MODULE_ROOT="$WSIT_MODULE_ROOT/wsmex"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsmex" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/mex"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT policy configuration file handling
# TODO: split into submodules
#
MODULE_ROOT="$WSIT_MODULE_ROOT/wsit-config"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-config" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/policy"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="policy"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT WS-RX Parent project
# TODO: split rx/policy
#
RX_MODULE_ROOT="$WSIT_MODULE_ROOT/wsrx"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -N -m "$RX_MODULE_ROOT" -n "wsrx-project" $POM_TEMPLATE
#
# WSIT WS-RX common packages
#
MODULE_ROOT="$RX_MODULE_ROOT/commons"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsrx-commons" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/rx/testing:com/sun/xml/ws/rx/util:com/sun/xml/ws/rx/RxConfiguration.java:com/sun/xml/ws/rx/RxConfigurationBase.java:com/sun/xml/ws/rx/RxException.java:com/sun/xml/ws/rx/RxRuntimeException.java"
TEST_ARTIFACTS="com/sun/xml/ws/rx/testing:com/sun/xml/ws/rx/util:com/sun/xml/ws/rx/RxConfigurationTest.java:com/sun/xml/ws/rx/RxConfigurationBaseTest.java:com/sun/xml/ws/rx/RxExceptionTest.java:com/sun/xml/ws/rx/RxRuntimeExceptionTest.java"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-RM
# TODO: split into submodules
#
MODULE_ROOT="$RX_MODULE_ROOT/wsrm-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsrm-impl" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/rx/rm"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="rm"
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-MC
# TODO: split into submodules
#
MODULE_ROOT="$RX_MODULE_ROOT/wsmc-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsmc-impl" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/rx/mc"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT WS-SX Parent project
# TODO: split into submodules
#
SX_MODULE_ROOT="$WSIT_MODULE_ROOT/wssx"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -N -m "$SX_MODULE_ROOT" -n "wssx-project" $POM_TEMPLATE
#
# WSIT WS-SecurityPolicy API
#
MODULE_ROOT="$SX_MODULE_ROOT/wss-policy-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wss-policy-api" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/api/security/CallbackHandlerFeature.java"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-SecurityPolicy implementation
#
MODULE_ROOT="$SX_MODULE_ROOT/wss-policy-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wss-policy-impl" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/security/impl/policy:com/sun/xml/ws/security/policy"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-Security API
#
#MODULE_ROOT="$SX_MODULE_ROOT/wss-api"
#source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wss-api" $POM_TEMPLATE
#SRC_ARTIFACTS=""
#TEST_ARTIFACTS="$SRC_ARTIFACTS"
#TEST_RESOURCES=""
#source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-Security implementation
#
MODULE_ROOT="$SX_MODULE_ROOT/wss-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wss-impl" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/wss"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-SecureConversation API
#
MODULE_ROOT="$SX_MODULE_ROOT/wssc-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wssc-api" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/api/security/secconv"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-SecureConversation implementation
#
MODULE_ROOT="$SX_MODULE_ROOT/wssc-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wssc-impl" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/security/secconv:com/sun/xml/ws/security/impl/policyconv"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-Trust API
#
MODULE_ROOT="$SX_MODULE_ROOT/wstrust-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wstrust-api" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/api/security/trust"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-Trust implementation
#
MODULE_ROOT="$SX_MODULE_ROOT/wstrust-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wstrust-impl" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/security/trust"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT SOAP/TCP Transport
# TODO: split into submodules
#
MODULE_ROOT="$WSIT_MODULE_ROOT/soaptcp"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "soaptcp" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/transport"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

#
# WSIT WS-TX Parent project
# TODO wstx-services submodule
#
TX_MODULE_ROOT="$WSIT_MODULE_ROOT/wstx"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -N -m "$TX_MODULE_ROOT" -n "wstx-project" $POM_TEMPLATE
#
# WSIT WS-TX API
#
MODULE_ROOT="$TX_MODULE_ROOT/wstx-api"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wstx-api" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/api/tx"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#
# WSIT WS-TX implementation
#
MODULE_ROOT="$TX_MODULE_ROOT/wstx-impl"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wstx-impl" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/tx"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh $COPY_ONLY_FLAG $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

