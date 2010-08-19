#!/bin/sh
USAGE="Usage: `basename $0` [-n] [-h] [-v] [-f] [-c]"

# parse command line arguments
CVS_QUIET="-Q"
OPTIND=1
while getopts 'nhvfc' OPT; do
    case "$OPT" in
	h)  echo $USAGE
            exit 0
            ;;
	v)  VERBOSE="-v"
            CVS_QUIET=""
            ;;
	n)  NO_EXPORT=1
            ;;
	f)  FORCE_RM_FLAG="-f"
            ;;
	c)  autoContinueFlag="-c"
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

continueChoice () {
    if [ -n "$autoContinueFlag" ] ; then
        return
    fi

    printf "$* Continue? [Y/n]"

    read -n 1 -rs choice # reading single character
    echo

    if [ "$choice" = "n" ] || [ "$choice" = "N" ] ; then
        echo "Stopping script..."
        exit 0;
    fi
}

ensureDir () {
    if [ ! -d $1 ] ; then
        mkdir -p $VERBOSE $1
    fi
}

pushd .
cd `pwd`/`dirname $0`/..
SECURITY_EXTRAS_ROOT=`pwd`/\_security-extras
NEW_PROJECT_ROOT=`pwd`/metro
EXPORTED_MASTER_ROOT=`pwd`/\_tmp-exported-master
EXPORTED_ROOT=`pwd`/\_tmp-exported
EXPORTED_RT_ROOT=$EXPORTED_ROOT/rt
OLD_METRO_LIB_DIR=`pwd`/wsit/lib
popd

continueChoice "Security extras root is set to: $SECURITY_EXTRAS_ROOT\nNew project root is set to: $NEW_PROJECT_ROOT\nExported WSIT MASTER root is set to: $EXPORTED_MASTER_ROOT\nExported WSIT root is set to: $EXPORTED_ROOT\n"

echo "\n===============[ Starting migration ]===============\n"

if [ ! -n "$NO_EXPORT" ] ; then
    if [ -e $EXPORTED_MASTER_ROOT ] ; then
        rm -rf $VERBOSE $EXPORTED_MASTER_ROOT
    fi
    mkdir $VERBOSE $EXPORTED_MASTER_ROOT

    pushd .
    cd $EXPORTED_MASTER_ROOT

    EXPORTED_DIR=etc
    cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d $EXPORTED_DIR wsit/wsit/$EXPORTED_DIR

    EXPORTED_DIR=licenses
    cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d $EXPORTED_DIR wsit/wsit/$EXPORTED_DIR

    EXPORTED_DIR=rt
    cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d $EXPORTED_DIR wsit/wsit/$EXPORTED_DIR

    EXPORTED_DIR=samples
    cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d $EXPORTED_DIR wsit/wsit/$EXPORTED_DIR

    EXPORTED_DIR=status-notes
    cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d $EXPORTED_DIR wsit/wsit/$EXPORTED_DIR

    EXPORTED_DIR=test
    cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d $EXPORTED_DIR wsit/wsit/$EXPORTED_DIR

    EXPORTED_DIR=tools
    cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d $EXPORTED_DIR wsit/wsit/$EXPORTED_DIR

    popd
fi

if [ -e $EXPORTED_ROOT ] ; then
    rm -rf $VERBOSE $EXPORTED_ROOT
fi
cp -R $EXPORTED_MASTER_ROOT/ $EXPORTED_ROOT/


if [ -e $NEW_PROJECT_ROOT ] ; then
    rm -rf $NEW_PROJECT_ROOT
fi
mkdir -p $VERBOSE $NEW_PROJECT_ROOT

source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $NEW_PROJECT_ROOT -n "Metro Web Services Stack Project" -p ./poms/metro-pom.xml
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $NEW_PROJECT_ROOT/wsit -n "Web Services Interoperability Technology Project" -p ./poms/wsit-pom.xml

source ./migrate-wsit-sources.sh

# Exporting license & adding license maintenance tools
echo "Exporting license & adding license maintenance tools..."
pushd .
cd `dirname $NEW_PROJECT_ROOT`
cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d `basename $NEW_PROJECT_ROOT` wsit/wsit/LICENSE.txt

cd $NEW_PROJECT_ROOT
cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d legal wsit/_migration-scripts/metro-legal
popd

# Creating Metro bundle projects
echo "Creating Metro bundle projects"
BUNDLES_MODULE_ROOT="$NEW_PROJECT_ROOT/bundles"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$BUNDLES_MODULE_ROOT" -n "Metro Bundles" -i "bundles" -p ./poms/bundles-pom.xml

ensureDir $BUNDLES_MODULE_ROOT

MODULE_NAME=webservices-api-osgi
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Webservices API OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"
echo "TODO: implement $MODULE_NAME Metro bundle pom"

MODULE_NAME=webservices-osgi
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Webservices Runtime OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"
echo "TODO: implement $MODULE_NAME Metro bundle pom"

MODULE_NAME=webservices-api
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Webservices API non-OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

MODULE_NAME=webservices-rt
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Webservices Runtime non-OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"
echo "TODO: uncomment WS-TX dependencies"
echo "TODO: jaxrpc-api.jar -> include or remove?"
echo "TODO: jsr173_api.jar -> include or remove?"
echo "TODO: txnannprocessor.jar -> include or remove?"
echo "TODO: keyidentifierspi.jar -> locate"

MODULE_NAME=webservices-tools
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Webservices Tools non-OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

MODULE_NAME=webservices-extra-api
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Webservices Extra API non-OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

MODULE_NAME=webservices-extra
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Webservices Extra Runtime non-OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"
echo "$MODULE_NAME TODO: Find grizzly.jar dependency in maven repos"

echo "TODO: migrate installer"
echo "TODO: migrate E2E tests"

# Adding Metro hudson job scripts
echo "Adding Metro hudson job scripts"

ensureDir "$NEW_PROJECT_ROOT/hudson"
echo "TODO: create hudson job scripts"

# Migrating Metro samples
echo "Migrating Metro samples"

ensureDir "$NEW_PROJECT_ROOT/samples"
echo "TODO: migrate samples"

# Migrating Metro release notes
echo "Migrating Metro release notes"

echo "TODO: migrate status notes"
