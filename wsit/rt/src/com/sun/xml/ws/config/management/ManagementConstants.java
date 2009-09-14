/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.config.management;

import com.sun.xml.ws.policy.PolicyConstants;

import javax.xml.namespace.QName;

/**
 * Constants used by several management classes.
 *
 * @author Fabian Ritzmann
 */
public class ManagementConstants {

    /**
     * Named parameter ID for the policy attachment configuration passed into the
     * managed endpoint.
     */
    public static final String CONFIGURATION_DATA_PARAMETER_NAME = "CONFIGURATION_DATA";

    /**
     * Name of the JDBC data source parameter in the config management configuration.
     */
    public static final QName JDBC_DATA_SOURCE_PARAMETER_NAME =
            new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JdbcDataSourceName");

    /**
     * Name of the JDBC table for the config management configuration.
     */
    public static final QName JDBC_TABLE_NAME_PARAMETER_NAME =
            new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JdbcTableName");

    /**
     * Name of the JDBC table column that holds the web service ID.
     */
    public static final QName JDBC_ID_COLUMN_NAME_PARAMETER_NAME =
            new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JdbcIdColumnName");

    /**
     * Name of the JDBC table column that holds the current version number.
     */
    public static final QName JDBC_VERSION_COLUMN_NAME_PARAMETER_NAME =
            new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JdbcVersionColumnName");

    /**
     * Name of the JDBC table column that holds the web service configuration data.
     */
    public static final QName JDBC_CONFIG_COLUMN_NAME_PARAMETER_NAME =
            new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JdbcConfigColumnName");

    /**
     * Default name of the database table that holds the configuration data.
     */
    public static final String JDBC_DEFAULT_TABLE_NAME = "METRO_CONFIG";

    /**
     * Default name of the database column that holds the web service ID.
     */
    public static final String JDBC_DEFAULT_ID_COLUMN_NAME = "id";

    /**
     * Default name of the database column that holds the configuration data's
     * current version number.
     */
    public static final String JDBC_DEFAULT_VERSION_COLUMN_NAME = "version";

    /**
     * Default name of the database column that holds the configuration data.
     */
    public static final String JDBC_DEFAULT_CONFIG_COLUMN_NAME = "config";

    /**
     * Prevent this class from being instantiated.
     */
    private ManagementConstants() {
    }

}