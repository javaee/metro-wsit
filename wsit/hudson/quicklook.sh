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

USAGE="Usage: `basename $0` -s gfsvnroot [-g glassfish.zip] [-w workingdir] [-m metro.zip] [-p profile]"

while getopts "g:w:s:m:p:" opt; do
    case $opt in
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
        p)
         QL_TEST_PROFILE=$OPTARG
         ;;
        \?)
         echo $USAGE >&2
         exit 1
         ;;
    esac
done

#fallback to defaults if needed
if [ -z "$GF_ZIP" ]; then
    GF_ZIP=$GF_SVN_ROOT/appserver/distributions/glassfish/target/glassfish.zip
    echo "setting GF_ZIP to $GF_ZIP"
fi

if [ -z "$QL_TEST_PROFILE" ]; then
    QL_TEST_PROFILE="all"
    echo "setting QL_TEST_PROFILE to $QL_TEST_PROFILE"
fi

set_common

print_env
echo "Test settings:"
echo "====================="
print_test_env

#validate input
declare -a errors
if [[ ( -z "$GF_SVN_ROOT" ) || ( ! -d $GF_SVN_ROOT ) ]]; then
    errors+="GF_SVN_ROOT"
fi
if [ ! -f $GF_ZIP ]; then
    errors+="GF_ZIP "
fi
if [ ! -w "$WORK_DIR" ]; then
    errors+="WORK_DIR "
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

if [ ! -z "$METRO_ZIP" ]; then
    install_metro $GF_WORK_DIR/$SERVER_DIR/glassfish
fi

echo "Running GlassFish QuickLook (Profile: $QL_TEST_PROFILE) tests..."

pushd $GF_SVN_ROOT/appserver/tests/quicklook
mvn -s $MVN_SETTINGS -P$QL_TEST_PROFILE -Dglassfish.home=$GF_WORK_DIR/$SERVER_DIR/glassfish test | tee $WORK_DIR/test-quicklook-$QL_TEST_PROFILE.log.txt
popd

echo "Done."
