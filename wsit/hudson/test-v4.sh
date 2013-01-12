#!/bin/bash
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
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

source common.sh

## GF-3.x
GF_312_URL=http://download.java.net/glassfish/3.1.2.2/release/glassfish-3.1.2.2.zip
METRO_MAJOR_VERSION=2.3

export WORK_DIR=$WORKSPACE

export GF_SVN_ROOT=$WORK_DIR/glassfish
export DTEST_SVN_ROOT=$WORK_DIR/appserv-tests
export METRO_SVN_ROOT=$WORK_DIR/wsit

if [ -z "$M2_LOCAL_REPO" ]; then
    export M2_LOCAL_REPO=$WORK_DIR/.repository
fi

#PROXY
set_proxy

export JAVA_MEMORY_PROP="-Xms256m -Xmx768m -XX:PermSize=256m -XX:MaxPermSize=512m"
export ANT_OPTS="$JAVA_MEMORY_PROP $JAVA_PROXY_PROP"
export MAVEN_OPTS="-Dmaven.repo.local=$M2_LOCAL_REPO -Dmaven.javadoc.skip=true $JAVA_MEMORY_PROP $JAVA_PROXY_PROP"


#fallback to defaults if needed
if [ -z "$MVN_REPO_URL" ]; then
    MVN_REPO_URL="https://maven.java.net/content/groups/staging"
fi

if [ -z "$METRO_SVN_REPO" ]; then
    METRO_SVN_REPO="https://svn.java.net/svn/wsit~svn/trunk/wsit"
fi

pushd $WORK_DIR

if [ ! -z "$GF_URL" ]; then
    _wget $GF_URL
    export GF_ZIP=$WORK_DIR/${GF_URL##*/}
fi

METRO_BUNDLE="org/glassfish/metro/metro-standalone"
if [ -z "$METRO_VERSION" ]; then
    LATEST_METRO_BUILD=`curl -s -k $MVN_REPO_URL/$METRO_BUNDLE/maven-metadata.xml | grep "<version>$METRO_MAJOR_VERSION-b[1-9]" | cut -d ">" -f2,2 | cut -d "<" -f1,1 | tail -1 | cut -d "b" -f2,2`
    METRO_VERSION="$METRO_MAJOR_VERSION-b$LATEST_METRO_BUILD"
    echo "Latest metro build: " $LATEST_METRO_BUILD
    echo "Metro version: " $METRO_VERSION
fi

if [ -z "$METRO_REVISION" ]; then
    METRO_URL="$MVN_REPO_URL/$METRO_BUNDLE/$METRO_VERSION/metro-standalone-$METRO_VERSION.zip"
    _wget $METRO_URL
    export METRO_ZIP=$WORK_DIR/${METRO_URL##*/}
fi

if [ -z "$CTS_ZIP" ]; then
    _wget $CTS_URL
    export CTS_ZIP=$WORK_DIR/${CTS_URL##*/}
fi
popd

echo "GF_ZIP: $GF_ZIP"
echo "METRO_ZIP: $METRO_ZIP"
echo "CTS_ZIP: $CTS_ZIP"
echo "Metro SVN root: $METRO_SVN_ROOT"


echo "Preparing workspace:"
#delete old
pushd $WORK_DIR
for dir in "test_results" ".repository" "wsit"
do
    if [ -e "$dir" ] ; then
        echo "Removing $dir"
        rm -rf $dir
    fi
done

if [ -e "metro.patch" ] ; then
  echo "Removing old patch for GlassFish"
  rm -f metro.patch
fi

if [ ! -z "$SR_MVN_REPO" ]; then
    wget -N --no-proxy $SR_MVN_REPO/xpp3/xpp3_min/1.1.3.4.O/xpp3_min-1.1.3.4.O.jar
    mvn install:install-file -DgroupId=xpp3 -DartifactId=xpp3_min -Dversion=1.1.3.4.O -Dpackaging=jar -Dfile=xpp3_min-1.1.3.4.O.jar -Dmaven.repo.local=$M2_LOCAL_REPO
    wget -N --no-proxy $SR_MVN_REPO/org/apache/xmlgraphics/batik-xml/1.7/batik-xml-1.7.jar
    mvn install:install-file -DgroupId=org.apache.xmlgraphics -DartifactId=batik-xml -Dversion=1.7 -Dpackaging=jar -Dfile=batik-xml-1.7.jar -Dmaven.repo.local=$M2_LOCAL_REPO
fi

popd


if [ -z "$METRO_URL" ]; then
    if [ ! -z "$METRO_REVISION" ]; then
        rm -rf $METRO_SVN_ROOT || true
        echo "Checking out Metro sources using revision: $METRO_REVISION"
        svn --non-interactive -q co -r $METRO_REVISION "$METRO_SVN_REPO" $METRO_SVN_ROOT
    fi
    pushd $METRO_SVN_ROOT
    JAXB_VERSION=`mvn dependency:tree -Dincludes=com.sun.xml.bind:jaxb-impl | grep com.sun.xml.bind:jaxb-impl | tail -1 | cut -f4 -d':'`
    JAXB_API_VERSION=`mvn dependency:tree -Dincludes=javax.xml.bind:jaxb-api | grep javax.xml.bind:jaxb-api | tail -1 | cut -f4 -d':'`
    echo "Setting project version in sources to new promoted version $METRO_VERSION"
    mvn versions:set -DnewVersion="$METRO_VERSION" -f bom/pom.xml
    popd

    pushd $GF_SVN_ROOT/appserver
    echo "Updating webservices.version property in GlassFish main pom.xml to $METRO_VERSION"
    perl -i -pe "s|<webservices.version>.*</webservices.version>|<webservices.version>$METRO_VERSION</webservices.version>|g" pom.xml
    echo "Updating jaxb.version property in GlassFish main pom.xml to $JAXB_VERSION"
    perl -i -pe "s|<jaxb.version>.*</jaxb.version>|<jaxb.version>$JAXB_VERSION</jaxb.version>|g" pom.xml
    echo "Updating jaxb-api.version property in GlassFish main pom.xml to $JAXB_API_VERSION"
    perl -i -pe "s|<jaxb-api.version>.*</jaxb-api.version>|<jaxb-api.version>$JAXB_API_VERSION</jaxb-api.version>|g" pom.xml
    echo "Updating IPS version to: metro_version=\"`echo $METRO_VERSION | cut -d \- -f 1`,0-`echo $METRO_VERSION | cut -d b -f 2`\""
    pushd packager/resources/
    sed -in 's/'`grep metro pkg_conf.py`'/metro_version="'`echo $METRO_VERSION | cut -d \- -f 1`',0-'`echo $METRO_VERSION | cut -d b -f 2`'"/g' pkg_conf.py
    popd
    echo "Prepared patch:"
    svn diff pom.xml packager/resources/pkg_conf.py
    echo "back-uping original pom.xml"
    cp pom.xml pom.xml.orig
    echo "Adding staging repository definition to GlassFish's pom.xml"
    perl -i -pe "s|</project>|<repositories><repository><id>staging.java.net</id><url>$MVN_REPO_URL</url></repository></repositories></project>|g" pom.xml
    popd

    echo -e "\nBuilding projects:"
    for project in "$METRO_SVN_ROOT" "$GF_SVN_ROOT"
    do
        echo "Building $project"
        pushd $project
        mvn -U -C clean install
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
export QL_RESULTS_DIR=$RESULTS_DIR/quick_look
export DEVTESTS_RESULTS_DIR=$RESULTS_DIR/devtests
export CTS_RESULTS_DIR=$RESULTS_DIR/cts-smoke

pushd "$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

mkdir -p $RESULTS_DIR
export ALL=$RESULTS_DIR/test-summary.txt
rm -f $ALL || true
touch $ALL
echo "Tested configuration:" >> $ALL
echo -e "\nJAVA_HOME: $JAVA_HOME" >> $ALL
echo "GlassFish: $GF_URL" >> $ALL
echo -e "Metro: $METRO_URL\n" >> $ALL

./quicklook.sh
mkdir -p $QL_RESULTS_DIR
pushd $GF_SVN_ROOT/appserver/tests/quicklook
cp quicklook_summary.txt *.output $QL_RESULTS_DIR
popd
cp $GF_WORK_DIR/glassfish3/glassfish/domains/domain1/logs/server.log* $QL_RESULTS_DIR
mv $WORK_DIR/test-quicklook.log.txt $RESULTS_DIR

if [ "`grep -E '.*Failures: 0.*' $QL_RESULTS_DIR/quicklook_summary.txt`" ]; then
    echo -e "\nQuickLook tests: OK\n" >> $ALL
else
    echo -e "\nQuickLook tests: `awk '/,/ { print $6 }' $QL_RESULTS_DIR/quicklook_summary.txt | cut -d ',' -f 1` failure(s)" >> $ALL
    grep "FAILED:" $RESULTS_DIR/test-quicklook.log.txt >> $ALL
    cat $ALL
    exit 1
fi
if [ "`grep 'BUILD FAILURE' $RESULTS_DIR/test-quicklook.log.txt`" ]; then
    echo "QuickLook tests: build failure" >> $ALL
    cat $ALL
    exit 1
fi

./devtests.sh
mkdir -p $DEVTESTS_RESULTS_DIR
pushd $DTEST_SVN_ROOT
cp test_results.* $DEVTESTS_RESULTS_DIR
pushd devtests/webservice
cp webservice.output $DEVTESTS_RESULTS_DIR/webservice.output.txt
cp count.txt $DEVTESTS_RESULTS_DIR
popd
popd
cp $GF_WORK_DIR/glassfish3/glassfish/domains/domain1/logs/server.log* $DEVTESTS_RESULTS_DIR
mv $WORK_DIR/test-devtests.log.txt $RESULTS_DIR

if [ "`grep 'Java Result: -1' $RESULTS_DIR/test-devtests.log.txt`" ]; then
    #TODO: break the build after fixing appserv-tests/devtests/webservice/ejb_annotations/ejbwebservicesinwar-2
    echo -e "\ndevtests tests: TODO - fix devtests/webservice/ejb_annotations/ejbwebservicesinwar-2" >> $ALL
fi
if [ "`grep -E 'FAILED=( )+0' $DEVTESTS_RESULTS_DIR/count.txt`" ]; then
    echo -e "\ndevtests tests: OK\n" >> $ALL
else
    echo -e "\ndevtests tests: `awk '/FAILED=( )+/ { print $2 }' $DEVTESTS_RESULTS_DIR/count.txt` failure(s)" >> $ALL
    grep ": FAIL" $DEVTESTS_RESULTS_DIR/webservice.output.txt >> $ALL
    cat $ALL
#    exit 1
fi
if [ "`grep 'BUILD FAILED' $RESULTS_DIR/test-devtests.log.txt`" ]; then
    echo "devtests tests: build failure" >> $ALL
    cat $ALL
#    exit 1
fi

./cts-smoke.sh
mkdir -p $CTS_RESULTS_DIR
mv $WORK_DIR/test_results-cts/* $CTS_RESULTS_DIR
rm -rf $WORK_DIR/test_results-cts
cp $GF_WORK_DIR/glassfish3/glassfish/domains/domain1/logs/server.log* $CTS_RESULTS_DIR
mv $WORK_DIR/test-cts-smoke.log.txt $RESULTS_DIR

popd

if [ ! "`grep 'Failed.' $CTS_RESULTS_DIR/summary.txt`" ]; then
    echo -e "\nCTS-smoke tests: OK\n" >> $ALL
else
    echo -e "\nCTS-smoke tests: `grep -c 'Failed.' $CTS_RESULTS_DIR/summary.txt` failure(s)" >> $ALL
    grep "Failed." $CTS_RESULTS_DIR/summary.txt >> $ALL
    cat $ALL
#    exit 1
fi
if [ "`grep 'BUILD FAILED' $RESULTS_DIR/test-cts-smoke.log.txt`" ]; then
    echo "CTS-smoke tests: build failure" >> $ALL
    cat $ALL
#    exit 1
fi

cat $ALL

cd $GF_SVN_ROOT
svn diff appserver/pom.xml appserver/packager/resources/pkg_conf.py > $WORK_DIR/metro.patch
echo "Patch created!"
