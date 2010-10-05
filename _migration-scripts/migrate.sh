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

    cd `dirname $EXPORTED_MASTER_ROOT`
    cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d `basename $EXPORTED_MASTER_ROOT` wsit/wsit/LICENSE.txt
    cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d `basename $EXPORTED_MASTER_ROOT` wsit/wsit/CDDLv1.0.1.txt
    cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d `basename $EXPORTED_MASTER_ROOT` wsit/wsit/CDDLv1.0.1.html

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

source ./migrate-wsit-sources.sh

# Adding license maintenance tools
echo "Adding license maintenance tools..."
pushd .

cd $NEW_PROJECT_ROOT
cvs -d :pserver:guest@cvs.dev.java.net:/cvs -z 9 $CVS_QUIET export -f -R -r HEAD -d legal wsit/_migration-scripts/metro-legal

# Migrating 3rd party licenses
mv $EXPORTED_ROOT/licenses $NEW_PROJECT_ROOT/legal/3rd-party-licenses
popd

# Creating Metro bundle projects
echo "Creating Metro bundle projects"
BUNDLES_MODULE_ROOT="$NEW_PROJECT_ROOT/bundles"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m "$BUNDLES_MODULE_ROOT" -n "Metro Bundles" -i "bundles" -p ./poms/bundles-pom.xml

ensureDir $BUNDLES_MODULE_ROOT

MODULE_NAME=wsit-api
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Web Services Interoperability Technology API Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

MODULE_NAME=wsit-impl
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Web Services Interoperability Technology Implementation Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

MODULE_NAME=webservices-api-osgi
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Web Services API OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

MODULE_NAME=webservices-osgi
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Web Services Runtime OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

mkdir -p $MODULE_ROOT/src/main/resources/META-INF/jaxrpc/
cp $EXPORTED_RT_ROOT/toolPlugin/ToolPlugin.xml $MODULE_ROOT/src/main/resources/META-INF/jaxrpc/

MODULE_NAME=webservices-api
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Web Services API non-OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

MODULE_NAME=webservices-rt
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Web Services Runtime non-OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

mkdir -p $MODULE_ROOT/src/main/resources/META-INF/jaxrpc/
mv $EXPORTED_RT_ROOT/toolPlugin/ToolPlugin.xml $MODULE_ROOT/src/main/resources/META-INF/jaxrpc/
mv $EXPORTED_RT_ROOT/etc/META-INF/MANIFEST.MF $MODULE_ROOT/src/main/resources/META-INF/

MODULE_NAME=webservices-tools
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Web Services Tools non-OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

mkdir -p $MODULE_ROOT/src/main/resources/META-INF/
mv $EXPORTED_ROOT/tools/etc/META-INF/MANIFEST.MF $MODULE_ROOT/src/main/resources/META-INF/

MODULE_NAME=webservices-extra-api
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Web Services Extra API non-OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

MODULE_NAME=webservices-extra
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Web Services Extra Runtime non-OSGi Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"

MODULE_NAME=webservices-extra-jdk-packages
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Extra JDK 6 packages required by Metro Web Services OSGi bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"
mkdir -p $MODULE_ROOT/src/main/resources/
touch $MODULE_ROOT/src/main/resources/empty

MODULE_NAME=metro-standalone
MODULE_ROOT="$BUNDLES_MODULE_ROOT/$MODULE_NAME"
source ./setup-module.sh $VERBOSE $FORCE_RM_FLAG -m $MODULE_ROOT -n "Metro Web Services Standalone Zipped Bundle" -i "$MODULE_NAME" -P "bundles" -p "./poms/bundles-${MODULE_NAME}-pom.xml"
mkdir -p $MODULE_ROOT/src/main/resources/
mv $EXPORTED_ROOT/CDDLv1.0.1.html $MODULE_ROOT/src/main/resources/LICENSE.html
mv $EXPORTED_ROOT/CDDLv1.0.1.txt $MODULE_ROOT/src/main/resources/LICENSE.txt
mv $EXPORTED_ROOT/etc/metro-on-*.xml $MODULE_ROOT/src/main/resources/
mv $EXPORTED_ROOT/etc/readme.html $MODULE_ROOT/src/main/resources/

mkdir -p $MODULE_ROOT/src/main/assembly/
cp ./poms/bundles-${MODULE_NAME}-assembly.xml $MODULE_ROOT/src/main/assembly/assembly.xml

echo "TODO: Finish metro zip bundle module"

# Migrating etc/* data
echo "Migrations data from etc/ directory"
ensureDir "$NEW_PROJECT_ROOT/etc"
mv $EXPORTED_ROOT/etc/schemas $NEW_PROJECT_ROOT/etc/
mv $EXPORTED_ROOT/etc/sql $NEW_PROJECT_ROOT/etc/

# Migrating E2E tests
echo "Migrating E2E tests"
mv $EXPORTED_ROOT/test $NEW_PROJECT_ROOT/tests
echo "TODO: fix migration of E2E tests"

# Adding Metro hudson job scripts
echo "Adding Metro hudson job scripts"
ensureDir "$NEW_PROJECT_ROOT/hudson"
echo "TODO: create hudson job scripts"

# Migrating Metro samples
echo "Migrating Metro samples"
mv $EXPORTED_ROOT/samples $NEW_PROJECT_ROOT/samples

# Migrating Metro release notes
echo "Migrating Metro release notes"
mv $EXPORTED_ROOT/status-notes $NEW_PROJECT_ROOT/status-notes
