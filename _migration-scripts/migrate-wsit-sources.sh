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

POM_TEMPLATE="./poms/wsit-module-pom.xml"
WSIT_MODULE_ROOT="$NEW_PROJECT_ROOT/wsit"

MODULE_ROOT="$WSIT_MODULE_ROOT/core"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-core" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/assembler:com/sun/xml/ws/commons:com/sun/xml/ws/dump:com/sun/xml/ws/runtime"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="assembler:metro-config"
source ./move-sources.sh -n $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

MODULE_ROOT="$WSIT_MODULE_ROOT/config-management"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-config-management" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/config/management"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="management"
source ./move-sources.sh -n $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

MODULE_ROOT="$WSIT_MODULE_ROOT/mex"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-mex" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/mex"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh -n $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

MODULE_ROOT="$WSIT_MODULE_ROOT/policy-config"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-policy-config" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/policy"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="policy"
source ./move-sources.sh -n $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

MODULE_ROOT="$WSIT_MODULE_ROOT/rx"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -N -m "$MODULE_ROOT" -n "wsit-rx" $POM_TEMPLATE
#---
MODULE_ROOT="$WSIT_MODULE_ROOT/rx/commons"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-rx-commons" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/rx/testing:com/sun/xml/ws/rx/util:com/sun/xml/ws/rx/RxConfiguration.java:com/sun/xml/ws/rx/RxConfigurationBase.java:com/sun/xml/ws/rx/RxException.java:com/sun/xml/ws/rx/RxRuntimeException.java"
TEST_ARTIFACTS="com/sun/xml/ws/rx/testing:com/sun/xml/ws/rx/util:com/sun/xml/ws/rx/RxConfigurationTest.java:com/sun/xml/ws/rx/RxConfigurationBaseTest.java:com/sun/xml/ws/rx/RxExceptionTest.java:com/sun/xml/ws/rx/RxRuntimeExceptionTest.java"
TEST_RESOURCES=""
source ./move-sources.sh -n $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#---
MODULE_ROOT="$WSIT_MODULE_ROOT/rx/rm"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-rm" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/rx/rm"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="rm"
source ./move-sources.sh -n $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#---
MODULE_ROOT="$WSIT_MODULE_ROOT/rx/mc"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-mc" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/rx/mc"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh -n $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
#--- TODO: rx/policy

MODULE_ROOT="$WSIT_MODULE_ROOT/security"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-security" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/security:com/sun/xml/wss"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES="security"
source ./move-sources.sh -n $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES
# TODO: split security into submodules

MODULE_ROOT="$WSIT_MODULE_ROOT/soaptcp"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-soaptcp" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/transport"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh -n $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

MODULE_ROOT="$WSIT_MODULE_ROOT/tx"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$MODULE_ROOT" -n "wsit-tx" $POM_TEMPLATE
SRC_ARTIFACTS="com/sun/xml/ws/tx"
TEST_ARTIFACTS="$SRC_ARTIFACTS"
TEST_RESOURCES=""
source ./move-sources.sh -n $VERBOSE $FORCE_RM_FLAG $MODULE_ROOT $SRC_ARTIFACTS $TEST_ARTIFACTS $TEST_RESOURCES

