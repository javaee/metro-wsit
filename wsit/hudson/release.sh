#!/bin/sh
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

while getopts :r:l:v:s:b:m:w:dh arg
do
    case "$arg" in
        r)  RELEASE_VERSION="${OPTARG:?}" ;;
        v)  RELEASE_REVISION="${OPTARG:?}" ;;
        l)  MAVEN_USER_HOME="${OPTARG:?}" ;;
        s)  MAVEN_SETTINGS="${OPTARG:?}" ;;
        b)  METRO_PROMOTION_BUILD="${OPTARG:?}" ;;
        m)  SOURCES_VERSION="${OPTARG:?}" ;;
        w)  WORKROOT="${OPTARG:?}" ;;
        d)  debug=true ;;
        h)
            echo "Usage: release.sh [-r RELEASE_VERSION] --mandatory, the release version string, for example 2.3.1"
            echo "                   [-v RELEASE_REVISION] or [-b METRO_PROMOTION_BUILD] --either one is needed, the svn revision number or the hudson promotion job(metro-trunk-promotion) build number"
            echo "                   [-l MVEN_USER_HOME] -- optional, alternative maven local repository location"
            echo "                   [-w WORKROOT] -- optional, default is current dir (`pwd`)"
            echo "                   [-m SOURCES_VERSION] -- optional, version in pom.xml need to be repaced with \$RELEASE_VERSION, default is \${RELEASE_VERSION}-SNAPSHOT"
            echo "                   [-s MAVEN_SETTINGS] --optional, alternative maven settings.xml"
            echo "                   [-d] -- debug mode"
            exit ;;
        "?")
            echo "ERROR: unknown option \"$OPTARG\"" 1>&2
            echo "" 1>&2 ;;
    esac
done

if [ "$M2_HOME" = "" -o ! -d $M2_HOME ]; then
    echo "ERROR: Check your M2_HOME: $M2_HOME"
    exit 1
fi

if [ "$JAVA_HOME" = "" -o ! -d $JAVA_HOME ]; then
    echo "ERROR: Check your JAVA_HOME: $JAVA_HOME"
    exit 1
fi
export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

PROXYURL=www-proxy.us.oracle.com
PROXYPORT=80

export http_proxy=$PROXYURL:$PROXYPORT
export https_proxy=$http_proxy


export MAVEN_OPTS="-Xms256m -Xmx768m -XX:PermSize=256m -XX:MaxPermSize=512m -Dhttp.proxyHost=$PROXYURL -Dhttp.proxyPort=$PROXYPORT -Dhttps.proxyHost=$PROXYURL -Dhttps.proxyPort=$PROXYPORT"

if [ "$MAVEN_USER_HOME" = "" ]; then
     user=${LOGNAME:-${USER-"`whoami`"}}
     MAVEN_USER_HOME="/scratch/$user/.m2/repository"
fi

if [ -n "$MAVEN_SETTINGS" ]; then
    MAVEN_SETTINGS="-s $MAVEN_SETTINGS"
fi

rm -rf $MAVEN_USER_HOME/org/glassfish/metro
rm -rf $MAVEN_USER_HOME/com/sun/xml

if [ "$WORKROOT" = "" ]; then
    WORKROOT=`pwd`
fi

if [ "$MAVEN_USER_HOME" = "" ]; then
    MAVEN_LOCAL_REPO="-Dmaven.repo.local=${WORKROOT}/.m2/repository"
else
    MAVEN_LOCAL_REPO="-Dmaven.repo.local=${MAVEN_USER_HOME}"
fi

if [ "$RELEASE_REVISION" = "" ]; then
    if [ "$METRO_PROMOTION_BUILD" = "" ]; then
        echo "ERROR: you need to either give the -r with the release revision, or give the -b with the promotion hudson build number"
        exit 1
    else
        RELEASE_REVISION=`wget -q -O - "http://prg10044.cz.oracle.com/hudson/job/metro-trunk-promotion/$METRO_PROMOTION_BUILD/artifact/revision.txt"`
    fi
fi

echo "Release Revision: $RELEASE_REVISION"
if [ "$RELEASE_REVISION" = "" -o $RELEASE_REVISION -eq 0 ]; then
   exit 1;
fi

cd $WORKROOT
if [ -e wsit ] ; then
   echo "INFO: Removing old Metro workspace"
   rm -rf wsit
fi

echo "INFO: Checking out Metro sources for release using revision: $RELEASE_REVISION"
svn checkout --non-interactive -q -r $RELEASE_REVISION https://svn.java.net/svn/wsit~svn/trunk/wsit

cd wsit
chmod +x ./hudson/*.sh

SOURCES_VERSION=${SOURCES_VERSION:-"${RELEASE_VERSION}-SNAPSHOT"}
echo "INFO: Replacing project version $SOURCES_VERSION in sources with new release version $RELEASE_VERSION"
echo "INFO: ./hudson/changeVersion.sh $SOURCES_VERSION $RELEASE_VERSION pom.xml"
./hudson/changeVersion.sh $SOURCES_VERSION $RELEASE_VERSION pom.xml
  
if [ "$debug" = "true" ]; then
    echo "DEBUG: build while no deploy"

    mvn $MAVEN_SETTINGS -B -C -DskipTests=true $MAVEN_LOCAL_REPO -Dgpg.passphrase=jaxbgpgpassword -DretryFailedDeploymentCount=10 -Prelease-profile,release -DaltDeploymentRepository=jvnet-nexus-staging::default::https://maven.java.net/service/local/staging/deploy/maven2/ clean package javadoc:jar source:jar gpg:sign install:install 
    mvn $MAVEN_SETTINGS -B -C $MAVEN_LOCAL_REPO -Prelease-docs install
else
    echo "INFO: Build and Deploy ..."
    mvn $MAVEN_SETTINGS -B -C -DskipTests=true $MAVEN_LOCAL_REPO -Dgpg.passphrase=jaxbgpgpassword -DretryFailedDeploymentCount=10 -Prelease-profile,release -DaltDeploymentRepository=jvnet-nexus-staging::default::https://maven.java.net/service/local/staging/deploy/maven2/ clean package javadoc:jar source:jar gpg:sign install:install deploy:deploy 
    mvn $MAVEN_SETTINGS -B -C $MAVEN_LOCAL_REPO -Prelease-docs install
fi
if [ $? -ne 0 ]; then
      exit 1
fi
cd $WORKROOT
if [ "$debug" = "true" ]; then
    echo "DEBUG: debug only, not actually tagging ..."
    echo "DEBUG: svn --username jaxbrobot --password jaxbrobotheslo --non-interactive --no-auth-cache copy wsit https://svn.java.net/svn/wsit~svn/tags/$RELEASE_VERSION -m \"Tag release $RELEASE_VERSION \""
else
    echo "INFO: Tagging release $RELEASE_VERSION ..."
    svn --username jaxbrobot --password jaxbrobotheslo --non-interactive --no-auth-cache copy wsit https://svn.java.net/svn/wsit~svn/tags/$RELEASE_VERSION -m "Tag release $RELEASE_VERSION"
fi

echo "INFO: Updating www docs ..."
if [ -d "www" ]; then
    rm -rf www
fi
svn checkout --non-interactive --depth=empty https://svn.java.net/svn/metro~svn/trunk/www
cd www
mkdir -p $RELEASE_VERSION
cp -r $WORKROOT/wsit/docs/guide/target/www/* $RELEASE_VERSION/
svn add --non-interactive $RELEASE_VERSION

echo "INFO: Update latest download page link to $RELEASE_VERSION"
svn --non-interactive update -q latest
sed -i "s#URL=https://metro.java.net/.*/#URL=https://metro.java.net/$RELEASE_VERSION/#" latest/download.html

echo "INFO: add $RELEASE_VERSION to the left side bar"
svn --non-interactive update -q __modules
line=`sed -n '/    <a href=\"#\">Download<\/a>/=' __modules/left_sidebar.htmlx`
line=`expr $line + 1`
appendLine="\ \ \ \ \ \ \ \ <li><a href=\"http://metro.java.net/$RELEASE_VERSION/\" match=\"/$RELEASE_VERSION/.*\">$RELEASE_VERSION</a>"
sed -i "$line a\
$appendLine" __modules/left_sidebar.htmlx

if [ "$debug" = "true" ]; then
    echo "DEBUG: debug only, not commit the docs."
    echo "DEBUG: svn --username jaxbrobot --password jaxbrobotheslo --non-interactive --no-auth-cache copy wsit https://svn.java.net/svn/wsit~svn/tags/$RELEASE_VERSION -m \"Tag release $RELEASE_VERSION\""
else
    echo "INFO: commit the updated docs"
    svn --username jaxbrobot --password jaxbrobotheslo --non-interactive --no-auth-cache commit -m "Metro $RELEASE_VERSION" .
fi
