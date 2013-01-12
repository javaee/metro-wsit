#!/bin/bash -ex
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

source setenv.sh

USAGE="Usage: `basename $0` -d devtestssvnroot (-g glassfish.zip || [-s gfsvnroot]) [-w workingdir] [-m metro.zip]"

while getopts "d:g:w:s:m:" opt ; do
    case $opt in
        d)
         DTEST_SVN_ROOT=$OPTARG
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

print_env
echo "Test settings:"
echo "====================="
echo "Devtests SVN root: $DTEST_SVN_ROOT"
print_test_env
echo
print_ports
echo

#validate input
declare -a errors
if [[ ( -z "$DTEST_SVN_ROOT" ) || ( ! -d $DTEST_SVN_ROOT ) ]]; then
    errors+="DTEST_SVN_ROOT, "
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

AS_HOME=$GF_WORK_DIR/glassfish3/glassfish
APS_HOME=$DTEST_SVN_ROOT
S1AS_HOME=$AS_HOME
APPCPATH=$APS_HOME/lib/reporter.jar

echo "AS_HOME: $AS_HOME"
echo "APS_HOME: $APS_HOME"
echo "S1AS_HOME: $S1AS_HOME"
echo "APPCPATH: $APPCPATH"
echo

if [ ! -z "$METRO_ZIP" ]; then
    install_metro $AS_HOME
fi

#domain setup
echo "Re-creating default domain"
echo "AS_ADMIN_PASSWORD=" > $APS_HOME/config/adminpassword.txt
create_domain
if [ ! -e $AS_HOME/domains/domain1/config/glassfish-acc.xml ]; then
    #in GF SVN r51053 - sun-acc.xml has been replaced by glassfish-acc.xml
    #this makes sure that devtests in GF trunk will do their job even for GF pre-4.0 releases
    cp $AS_HOME/domains/domain1/config/sun-acc.xml $AS_HOME/domains/domain1/config/glassfish-acc.xml
fi

echo "admin.domain=domain1
admin.domain.dir=\${env.AS_HOME}/domains
admin.port=${ADMIN_PORT}
admin.user=admin
admin.host=localhost
http.port=${INSTANCE_PORT}
https.port=${SSL_PORT}
http.host=localhost
http.address=127.0.0.1
http.alternate.port=${ALTERNATE_PORT}
orb.port=${ORB_PORT}
admin.password=
ssl.password=changeit
master.password=changeit
admin.password.file=\${env.APS_HOME}/config/adminpassword.txt
appserver.instance.name=server
config.dottedname.prefix=server
resources.dottedname.prefix=domain.resources
results.mailhost=localhost
results.mailer=QLTestsForPEInstallOrDASInEEInstall@sun.com
results.mailee=yourname@sun.com
autodeploy.dir=\${env.AS_HOME}/domains/\${admin.domain}/autodeploy
precompilejsp=true
jvm.maxpermsize=192m
appserver.instance.dir=\${admin.domain.dir}/\${admin.domain}" > $APS_HOME/config.properties

echo "Domain config:"
cat $APS_HOME/config.properties
echo

echo "Running GlassFish webservices devtests..."

pushd $APS_HOME/devtests/webservice
$AS_HOME/bin/asadmin start-domain
AS_HOME=$AS_HOME APS_HOME=$APS_HOME S1AS_HOME=$S1AS_HOME APPCPATH=$APPCPATH ant all | tee $WORK_DIR/test-devtests.log.txt
$AS_HOME/bin/asadmin stop-domain
popd

echo "Done."
