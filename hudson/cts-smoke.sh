#!/bin/bash -ex
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
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

source setenv.sh

USAGE="Usage: `basename $0` -c cts.zip (-g glassfish.zip || -s gfsvnroot) [-w workingdir] [-m metro.zip]"

while getopts "c:g:w:s:m:" opt; do
    case $opt in
        c)
         CTS_ZIP=$OPTARG
         ;;
        s)
         GF_SVN_ROOT=$OPTARG
         ;;
        g)
         GF_ZIP=$OPTARG
         ;;
        w)
         WORK_DIR=$OPTARG
         ;;
        m)
         METRO_ZIP=$OPTARG
         ;;
        \?)
         echo $USAGE >&2
         exit 1
         ;;
    esac
done

#fallback to defaults if needed
if [[ ( -z "$GF_ZIP" ) && ( ! -z "$GF_SVN_ROOT" ) && ( -d $GF_SVN_ROOT ) ]] ; then
    GF_ZIP=$GF_SVN_ROOT/appserver/distributions/glassfish/target/glassfish.zip
    echo "setting GF_ZIP to $GF_ZIP"
fi

set_common
set_ports

#CTS specific:
if [ -z "$DERBY_PORT" ] ; then
    DERBY_PORT=1527
fi
if [ -z "$WEB_SERVICE_SECURE_PORT" ] ; then
    WEB_SERVICE_SECURE_PORT=1044
fi
if [ -z "$MAIL_USER" ]; then
    MAIL_USER="root"
fi

CTS_WORK_DIR=$WORK_DIR/tmp-cts
ORB_HOST=localhost

print_env
echo "Test settings:"
echo "====================="
echo "CTS zip: $CTS_ZIP"
print_test_env
echo "Working directory - CTS: $CTS_WORK_DIR"
print_ports
echo "DERBY_PORT: $DERBY_PORT"
echo "WEB_SERVICE_SECURE_PORT: $WEB_SERVICE_SECURE_PORT"
echo

#validate input
declare -a errors
if [[ ( -z "$CTS_ZIP" ) || ( ! -f $CTS_ZIP ) ]]; then
    errors+="CTS_ZIP, "
fi
if [[ ( -z "$GF_SVN_ROOT" ) && ( ! -f "$GF_ZIP" ) ]]; then
    errors+="GF_SVN_ROOT or GF_ZIP, "
fi
if [ ! -w "$WORK_DIR" ]; then
    errors+="WORK_DIR, "
fi
if [[ ( ! -z "$METRO_ZIP" ) && ( ! -f $METRO_ZIP ) ]]; then
    errors+="METRO_ZIP "
fi
if [ ${#errors[@]} -gt 0 ]; then
    echo "${errors[*]} not set correctly"
    exit 1
fi

_unzip $GF_ZIP $GF_WORK_DIR

if [ -d "$GF_WORK_DIR/glassfish4" ]; then
    SERVER_DIR=glassfish4
else
    SERVER_DIR=glassfish3
fi

AS_HOME=$GF_WORK_DIR/$SERVER_DIR/glassfish

echo "AS_HOME: $AS_HOME"
echo

if [ ! -z "$METRO_ZIP" ]; then
    install_metro $AS_HOME
fi

#domain setup
echo "Re-creating default domain"
create_domain

_unzip $CTS_ZIP $CTS_WORK_DIR

pushd $CTS_WORK_DIR/javaee-smoke/bin
echo "Updating ts.jte in `pwd`"

X=`echo "$AS_HOME" | awk '{gsub(/\//,"\\\/");print}'`
Y=`echo "$WORK_DIR/test_results-cts" | awk '{gsub(/\//,"\\\/");print}'`

echo "CTS java.home(.ri): $X"
sed -in 's/javaee.home=/javaee.home='"$X"'/g' ts.jte
sed -in 's/javaee.home.ri=/javaee.home.ri='"$X"'/g' ts.jte

echo "CTS orb.host(.ri): $ORB_HOST"
sed -in 's/orb.host=/orb.host='"$ORB_HOST"'/g' ts.jte
sed -in 's/orb.host.ri=/orb.host.ri='"$ORB_HOST"'/g' ts.jte

echo "CTS orb.port.ri: $ORB_PORT"
sed -in 's/3700/'$ORB_PORT'/g' ts.jte

echo "CTS *.admin.port: $ADMIN_PORT"
sed -in 's/4848/'$ADMIN_PORT'/g' ts.jte

echo "CTS derby.port: $DERBY_PORT"
sed -in 's/1527/'$DERBY_PORT'/g' ts.jte

echo "CTS webServerPort: $ALTERNATE_PORT"
sed -in 's/8001/'$ALTERNATE_PORT'/g' ts.jte

echo "CTS securedWebServicePort: $WEB_SERVICE_SECURE_PORT"
sed -in 's/1044/'$WEB_SERVICE_SECURE_PORT'/g' ts.jte

if [ ! -z "$MAIL_HOST" ]; then
    echo "CTS mailuser1: $MAIL_USER@$MAIL_HOST"
    sed -in 's/mailuser1=/mailuser1='"$MAIL_USER@$MAIL_HOST"'/g' ts.jte
    echo "CTS mailHost: $MAIL_HOST"
    sed -in 's/mailHost=/mailHost='"$MAIL_HOST"'/g' ts.jte
    echo "CTS mailFrom: $MAIL_USER_FROM@$MAIL_HOST"
    sed -in 's/mailFrom=/mailFrom='"$MAIL_USER_FROM@$MAIL_HOST"'/g' ts.jte
    echo "CTS javamail.password: $MAIL_PWD"
    sed -in 's/javamail.password=/javamail.password='"$MAIL_PWD"'/g' ts.jte
fi

echo "CTS report.dir: $Y"
sed -in 's/report.dir=\/tmp\/JTReport/report.dir='"$Y"'/g' ts.jte

echo "CTS work.dir: /tmp/JTWork"
sed -in 's/work.dir=\/tmp\/JTWork/work.dir=\/tmp\/JTWork/g' ts.jte

popd

echo "Running GlassFish CTS smoke tests..."

pushd $CTS_WORK_DIR/javaee-smoke/bin/xml
ant -f smoke.xml smoke | tee $WORK_DIR/test-cts-smoke.log.txt
popd

if [ -z "$MAIL_HOST" ]; then
    echo
    echo "6 failures are expected because nobody told me how to send emails..."
    echo "(MAIL_HOST has not been set)"
    echo
fi

echo "Done."
