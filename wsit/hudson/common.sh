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

# $1 - URL
function _wget() {
    if [[ "$1" == *.oracle.com* ]]; then
        WGET_PROXY="--no-proxy"
    else
        WGET_PROXY=""
    fi
echo "wget: $WGET_PROXY"
    wget $WGET_PROXY -N $1
}

function set_proxy() {
    if [ ! -z "$PROXY_HOST" ]; then
        if [ ! -z "$PROXY_PORT" ]; then
            export http_proxy=$PROXY_HOST:$PROXY_PORT
            JAVA_PROXY_PORT="-Dhttp.proxyPort=$PROXY_PORT -Dhttps.proxyPort=$PROXY_PORT"
        else
            export http_proxy=$PROXY_HOST
        fi
        export https_proxy=$http_proxy
        export HTTP_PROXY=$http_proxy
        export HTTPS_PROXY=$https_proxy
        export JAVA_PROXY_PROP="-Dhttp.proxyHost=$PROXY_HOST -Dhttps.proxyHost=$PROXY_HOST $JAVA_PROXY_PORT"
    fi
}