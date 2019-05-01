#!/bin/bash -ex
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

#set env like CTS_URL [required] , MVN_SETTINGS[optional] , GF_URL [optional]
METRO_MAJOR_VERSION=2.4.0
METRO_REVISION=$BRANCH_NAME
export WORK_DIR=$WORKSPACE
echo "WORK_DIR: $WORK_DIR"
export GF_SVN_ROOT=$WORK_DIR/glassfish
export DTEST_SVN_ROOT=$GF_SVN_ROOT/appserver/tests/appserv-tests
export METRO_SVN_ROOT=$WORK_DIR/metro-wsit


cd $METRO_SVN_ROOT/wsit/hudson/
source common.sh
if [ -z "$M2_LOCAL_REPO" ]; then
    export M2_LOCAL_REPO=$WORK_DIR/.repository
fi

#PROXY
set_proxy

export ANT_OPTS=" $JAVA_PROXY_PROP"
export MAVEN_OPTS="-Dmaven.repo.local=$M2_LOCAL_REPO -Dmaven.javadoc.skip=true $JAVA_PROXY_PROP"

source setenv.sh

set_common
print_env

#prepare git source
pushd $METRO_SVN_ROOT
git reset --hard
git clean -fd
popd


if [! -d "$GF_SVN_ROOT"  ]; then
  git init $GF_SVN_ROOT
  git clone git@github.com:javaee/glassfish.git
fi

pushd $GF_SVN_ROOT
git reset --hard
git clean -fdx
popd


#fallback to defaults if needed
if [ -z "$MVN_REPO_URL" ]; then
    MVN_REPO_URL="https://maven.java.net/content/groups/staging"
fi

if [ -z "$METRO_SVN_REPO" ]; then
    METRO_SVN_REPO=$METRO_FORKED_REPO
fi

pushd $WORK_DIR

#CLEAN UP
rm -rf $GF_WORK_DIR
rm -rf $METRO_WORK_DIR
rm -f *.patch
rm -f *.zip

if [ ! -f "$GF_ZIP" -a ! -z "$GF_URL" ]; then
  wget -N $GF_URL
  export GF_ZIP=$WORK_DIR/${GF_URL##*/}
fi

echo "METRO_VERSION: $METRO_VERSION"
echo "METRO_REVISION: $METRO_REVISION"
echo "CTS_ZIP: $CTS_ZIP"

if [[ ! -z $METRO_REVISION ]]; then #generate a temp metro version
  DATESTAMP=`date +%y%m%d.%H%M`
  BUILD_NUMBER=b${DATESTAMP}
  METRO_VERSION=$METRO_MAJOR_VERSION-$BUILD_NUMBER
fi

METRO_BUNDLE="org/glassfish/metro/metro-standalone"
if [ -z "$METRO_VERSION" ]; then
    LATEST_METRO_BUILD=`curl -s -k $MVN_REPO_URL/$METRO_BUNDLE/maven-metadata.xml | grep "<version>$METRO_MAJOR_VERSION-b[0-9]" | cut -d ">" -f2,2 | cut -d "<" -f1,1 | tail -1 | cut -d "b" -f2,2`
    METRO_VERSION="$METRO_MAJOR_VERSION-b$LATEST_METRO_BUILD"
    echo "Latest metro build: " $LATEST_METRO_BUILD
    echo "Metro version: " $METRO_VERSION
fi

if [ -z "$METRO_REVISION" ]; then
    METRO_URL="$MVN_REPO_URL/$METRO_BUNDLE/$METRO_VERSION/metro-standalone-$METRO_VERSION.zip"
    _wget $METRO_URL
    export METRO_ZIP=$WORK_DIR/${METRO_URL##*/}
fi

if [ ! -f "$CTS_ZIP" ]; then
    wget $CTS_URL
    export CTS_ZIP=$WORK_DIR/${CTS_URL##*/}
fi
popd

echo "GF_ZIP: $GF_ZIP"
echo "METRO_ZIP: $METRO_ZIP"
echo "CTS_ZIP: $CTS_ZIP"
echo "Metro SVN root: $METRO_SVN_ROOT"

echo "Preparing WORKDIR:"
#delete old
pushd $WORK_DIR
for dir in "test_results" ".repository" 
do
    if [ -e "$dir" ] ; then
        echo "Removing $dir"
        rm -rf $dir
    fi
done
popd

cd $WORK_DIR
if [ -z "$METRO_URL" ]; then
    if [ ! -z "$METRO_REVISION" ]; then
        rm -rf $METRO_SVN_ROOT || true
        echo "Checking out Metro sources using revision: $METRO_REVISION"
        git clone $METRO_SVN_REPO $METRO_SVN_ROOT
        pushd $METRO_SVN_ROOT/wsit/
        git checkout $METRO_REVISION
    fi
    JAXB_VERSION=`mvn -s $MVN_SETTINGS dependency:tree -f metro-runtime/metro-runtime-api/pom.xml -Dincludes=com.sun.xml.bind:jaxb-impl | grep com.sun.xml.bind:jaxb-impl | tail -1 | cut -f4 -d':'`
    JAXB_API_VERSION=`mvn -s $MVN_SETTINGS dependency:tree -Dincludes=javax.xml.bind:jaxb-api | grep javax.xml.bind:jaxb-api | tail -1 | cut -f4 -d':'`
    SOAP_API_VERSION=`mvn -s $MVN_SETTINGS dependency:tree -Dincludes=javax.xml.soap:javax.xml.soap-api | grep javax.xml.soap:javax.xml.soap-api | tail -1 | cut -f4 -d':'`
    MIMEPULL_VERSION=`mvn -s $MVN_SETTINGS dependency:tree -Dincludes=org.jvnet.mimepull:mimepull | grep org.jvnet.mimepull:mimepull | tail -1 | cut -f4 -d':'`
    echo "Setting project version in sources to new promoted version $METRO_VERSION"
    #mvn versions:set -Pstaging -DnewVersion="$METRO_VERSION" -f boms/bom/pom.xml -s /net/bat-sca/repine/export2/hudson/tools/maven-3.0.3/settings-nexus.xml
    ./hudson/changeVersion.sh $METRO_MAJOR_VERSION-SNAPSHOT $METRO_VERSION pom.xml
    popd

    pushd $GF_SVN_ROOT/appserver
    echo "Updating webservices.version property in GlassFish main pom.xml to $METRO_VERSION"
    perl -i -pe "s|<webservices.version>.*</webservices.version>|<webservices.version>$METRO_VERSION</webservices.version>|g" pom.xml
    echo "Updating jaxb.version property in GlassFish main pom.xml to $JAXB_VERSION"
    perl -i -pe "s|<jaxb.version>.*</jaxb.version>|<jaxb.version>$JAXB_VERSION</jaxb.version>|g" pom.xml
    echo "Updating jaxb-api.version property in GlassFish main pom.xml to $JAXB_API_VERSION"
    perl -i -pe "s|<jaxb-api.version>.*</jaxb-api.version>|<jaxb-api.version>$JAXB_API_VERSION</jaxb-api.version>|g" pom.xml
    echo "Updating javax.xml.soap-api.version property in GlassFish main pom.xml to $SOAP_API_VERSION"
    perl -i -pe "s|<javax.xml.soap-api.version>.*</javax.xml.soap-api.version>|<javax.xml.soap-api.version>$SOAP_API_VERSION</javax.xml.soap-api.version>|g" pom.xml


    pushd $GF_SVN_ROOT/nucleus
    echo "Updating mimepull.version property in GlassFish nucleus-parent pom.xml to $MIMEPULL_VERSION"
    perl -i -pe "s|<mimepull.version>.*</mimepull.version>|<mimepull.version>$MIMEPULL_VERSION</mimepull.version>|g" pom.xml
    popd

    echo "Prepared patch:"
    git diff  pom.xml ../nucleus/pom.xml packager/resources/pkg_conf.py
    echo "back-uping original pom.xml"
    cp pom.xml pom.xml.orig
    echo "Adding staging repository definition to GlassFish's pom.xml"
    perl -i -pe "s|</project>|<repositories><repository><id>staging.java.net</id><url>$MVN_REPO_URL</url></repository></repositories></project>|g" pom.xml
    popd

    echo -e "\nBuilding projects:"
     for project in "$METRO_SVN_ROOT/wsit/" "$GF_SVN_ROOT"
     do
         echo "Building $project"
         pushd $project
        # mvn -s $MVN_SETTINGS -U -C  -DskipTests=true  clean install
        mvn -s $MVN_SETTINGS -U -C  clean install
         popd
         echo "$project done"
     done
     echo -e "\nDone building projects\n"

     pushd $GF_SVN_ROOT/appserver
     echo "Restoring GlassFish's pom.xml"
     rm -f pom.xml
     mv pom.xml.orig pom.xml
     popd
fi

export RESULTS_DIR=$WORK_DIR/test_results
export DEVTESTS_RESULTS_DIR=$RESULTS_DIR/devtests
export CTS_RESULTS_DIR=$RESULTS_DIR/cts-smoke

pushd "$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

mkdir -p $RESULTS_DIR
export ALL=$RESULTS_DIR/test-summary.txt
rm -f $ALL || true
touch $ALL
echo "Tested configuration:" >> $ALL
echo -e "\nJAVA_HOME: $JAVA_HOME" >> $ALL

if [ -z "$GF_URL" ]; then
    pushd $GF_SVN_ROOT
    echo "GlassFish: `git status $GF_SVN_ROOT`" >> $ALL
    popd
else
    echo "GlassFish: $GF_URL" >> $ALL
fi
if [ -z "$METRO_URL" ]; then
    pushd $METRO_SVN_ROOT
    echo "Metro: `git status`\n" >> $ALL
    popd
else
    echo "Metro: $METRO_URL\n" >> $ALL
fi


pushd $METRO_SVN_ROOT/wsit/hudson/

./cts-smoke.sh -g $GF_ZIP -c $CTS_ZIP
mkdir -p $CTS_RESULTS_DIR
mv $WORK_DIR/test_results-cts/* $CTS_RESULTS_DIR
rm -rf $WORK_DIR/test_results-cts
cp $GF_WORK_DIR/glassfish5/glassfish/domains/domain1/logs/server.log* $CTS_RESULTS_DIR
mv $WORK_DIR/test-cts-smoke.log.txt $RESULTS_DIR
popd

if [ ! "`grep 'Failed.' $CTS_RESULTS_DIR/text/summary.txt`" ]; then
    echo -e "\nCTS-smoke tests: OK\n" >> $ALL
else
    echo -e "\nCTS-smoke tests: `grep -c 'Failed.' $CTS_RESULTS_DIR/text/summary.txt` failure(s)" >> $ALL
    grep "Failed." $CTS_RESULTS_DIR/text/summary.txt >> $ALL
    cat $ALL
#    exit 1
fi
if [ "`grep 'BUILD FAILED' $RESULTS_DIR/test-cts-smoke.log.txt`" ]; then
    echo "CTS-smoke tests: build failure" >> $ALL
    cat $ALL
#    exit 1
fi

pushd $METRO_SVN_ROOT/wsit/hudson/

for QL_TEST_PROFILE in "all"
do
    export QL_RESULTS_DIR=$RESULTS_DIR/quick_look-$QL_TEST_PROFILE
    ./quicklook.sh -g $GF_ZIP  -p $QL_TEST_PROFILE
    mkdir -p $QL_RESULTS_DIR
    pushd $GF_SVN_ROOT/appserver/tests/quicklook
    cp quicklook_summary.txt *.output $QL_RESULTS_DIR
    popd
    cp $GF_WORK_DIR/glassfish5/glassfish/domains/domain1/logs/server.log* $QL_RESULTS_DIR
    mv $WORK_DIR/test-quicklook-$QL_TEST_PROFILE.log.txt $RESULTS_DIR

    if [ "`grep -E '.*Failures: 0.*' $QL_RESULTS_DIR/quicklook_summary.txt`" ]; then
        echo -e "\nQuickLook tests: OK\n" >> $ALL
    else
        echo -e "\nQuickLook tests: `awk '/,/ { print $6 }' $QL_RESULTS_DIR/quicklook_summary.txt | cut -d ',' -f 1` failure(s)" >> $ALL
        grep "FAILED:" $RESULTS_DIR/test-quicklook-$QL_TEST_PROFILE.log.txt >> $ALL
        cat $ALL
        exit 1
    fi

    if [ "`grep -E '.*Configuration Failures:.*' $QL_RESULTS_DIR/quicklook_summary.txt`" ]; then
        echo -e "\nQuickLook tests: `awk '/,/ { print $3 }' $QL_RESULTS_DIR/quicklook_summary.txt | cut -d ',' -f 1 | cut -d ':' -f 2` configuration failure(s)" >> $ALL
        grep "FAILED CONFIGURATION:" $RESULTS_DIR/test-quicklook-$QL_TEST_PROFILE.log.txt >> $ALL
        echo -e "\nQuickLook tests: `awk '/,/ { print $8 }' $QL_RESULTS_DIR/quicklook_summary.txt | cut -d ',' -f 1` skip(s)" >> $ALL
        grep "SKIPPED:" $RESULTS_DIR/test-quicklook-$QL_TEST_PROFILE.log.txt >> $ALL
        cat $ALL
        exit 1
    fi

    if [ "`grep 'BUILD FAILURE' $RESULTS_DIR/test-quicklook-$QL_TEST_PROFILE.log.txt`" ]; then
        echo "QuickLook tests ($QL_TEST_PROFILE): build failure" >> $ALL
        cat $ALL
        exit 1
    fi
done

#Disable devtest due to test has not been moved to app-test and build issue.
# pushd $METRO_SVN_ROOT/wsit/hudson/
# ./devtests.sh -g $GF_ZIP
# mkdir -p $DEVTESTS_RESULTS_DIR
# pushd $DTEST_SVN_ROOT
# cp test_results.* $DEVTESTS_RESULTS_DIR
# pushd devtests/webservice
# cp webservice.output $DEVTESTS_RESULTS_DIR/webservice.output.txt
# #cp count.txt $DEVTESTS_RESULTS_DIR
# popd
# popd
# cp $GF_WORK_DIR/glassfish5/glassfish/domains/domain1/logs/server.log* $DEVTESTS_RESULTS_DIR
# mv $WORK_DIR/test-devtests.log.txt $RESULTS_DIR
#
# if [ "`grep 'Java Result: -1' $RESULTS_DIR/test-devtests.log.txt`" ]; then
#    #TODO: break the build after fixing appserv-tests/devtests/webservice/ejb_annotations/ejbwebservicesinwar-2
#    echo -e "\ndevtests tests: TODO - fix devtests/webservice/ejb_annotations/ejbwebservicesinwar-2" >> $ALL
# fi
# if [ "`grep -E 'FAILED=( )+0' $DEVTESTS_RESULTS_DIR/count.txt`" ]; then
#    echo -e "\ndevtests tests: OK\n" >> $ALL
# else
#    echo -e "\ndevtests tests: `awk '/FAILED=( )+/ { print $2 }' $DEVTESTS_RESULTS_DIR/count.txt` failure(s)" >> $ALL
#    grep ": FAIL" $DEVTESTS_RESULTS_DIR/webservice.output.txt >> $ALL
#    cat $ALL
# #    exit 1
# fi
# if [ "`grep 'BUILD FAILED' $RESULTS_DIR/test-devtests.log.txt`" ]; then
#    echo "devtests tests: build failure" >> $ALL
#    cat $ALL
# #    exit 1
# fi
if [ -z "$METRO_REVISION" ]; then #create patch only for public metro
  pushd $GF_SVN_ROOT

  git config user.name $JOB_GIT_USERNAME
  git config user.email $JOB_GIT_EMAIL

  git add pom.xml appserver/pom.xml nucleus/pom.xml
  git commit -m "PATCH with "+$METRO_VERSION
  git format-patch -1
  git reset --hard HEAD~1
  mv *.patch $WORKDIR
  echo "Patch created!"
fi
