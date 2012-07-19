#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
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

Place containing all hudson job scripts
most of them are usable also in local environment


changeVersion.sh <old_string> <new_string> <file>

    recursively find all <file>s and replace <old_string> with <new_string> there
    typical usage is:

        cd $WSIT_HOME
        changeVersion.sh "2.3-SNAPSHOT" "2.3-b05" pom.xml

   TODO: consider deprecating and replacing this script with:
   cd $WSIT_HOME && mvn versions:set -DnewVersion=<version> -f bom/pom.xml'


cts-smoke.sh -c <cts.zip> (-g <glassfish.zip> || -s <gfsvnroot>) [-w <workingdir>] [-m <metro.zip>]

    unzip GlassFish to workingdir, optionally install Metro on top of it,
    re-create default domain1 and run CTS Smoke test suite

    option      env_var         description
    ------------------------------------
  mandatory:
    -c <file>   $CTS_ZIP        CTS-smoke tests zip distribution, usually: javaee-smoke-6.0_latest.zip
  one of:
    -s <dir>    $GF_SVN_ROOT    GlassFish workspace root
    -g <file>   $GF_ZIP         GlassFish zip distribution to test
                                defaults to: $GF_SVN_ROOT/appserver/distributions/glassfish/target/glassfish.zip
  optional:
    -m <file>   $METRO_ZIP      Metro zip distribution
    -w <dir>    $WORK_DIR       working directory
                                defaults to: /tmp
  Note:
    command line options take precedence over environment variables


devtests.sh -d <devtestssvnroot> (-g <glassfish.zip> || -s <gfsvnroot>) [-w <workingdir>] [-m <metro.zip>]

    unzip GlassFish to workingdir, optionally install Metro on top of it,
    re-create default domain1 and run webservices devtests

    option      env_var         description
    ------------------------------------
  mandatory:
    -d <dir>    $DTEST_SVN_ROOT appserv-tests workspace
  one of:
    -s <dir>    $GF_SVN_ROOT    GlassFish workspace root
    -g <file>   $GF_ZIP         GlassFish zip distribution to test
                                defaults to: $GF_SVN_ROOT/appserver/distributions/glassfish/target/glassfish.zip
  optional:
    -m <file>   $METRO_ZIP      Metro zip distribution
    -w <dir>    $WORK_DIR       working directory
                                defaults to: /tmp
  Note:
    command line options take precedence over environment variables


quicklook.sh -s gfsvnroot [-g glassfish.zip] [-w workingdir] [-m metro.zip]

    unzip GlassFish to workingdir, optionally install Metro on top of it
    and run GlassFish QuickLook test suite

    option      env_var         description
    ------------------------------------
  mandatory:
    -s <dir>    $GF_SVN_ROOT    GlassFish workspace root containing appserver/tests/quicklook folder
  optional:
    -g <file>   $GF_ZIP         GlassFish zip distribution to test
                                defaults to: $GF_SVN_ROOT/appserver/distributions/glassfish/target/glassfish.zip
    -m <file>   $METRO_ZIP      Metro zip distribution
    -w <dir>    $WORK_DIR       working directory
                                defaults to: /tmp
  Note:
    command line options take precedence over environment variables

