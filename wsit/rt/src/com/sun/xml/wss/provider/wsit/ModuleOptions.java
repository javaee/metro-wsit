/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
/*
 * $Id: ModuleOptions.java,v 1.1 2006-10-04 16:49:06 kumarjayanti Exp $
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.wss.provider.wsit;

public interface ModuleOptions {
     public static final String SECURITY_CONFIGURATION_FILE = "security.config";
     public static final String ALIASES = "aliases";
     public static final String PASSWORDS = "keypasswords"; 
     public static final String DEBUG = "debug";
     public static final String SIGNING_KEY_ALIAS = "signature.key.alias";
     public static final String ENCRYPTION_KEY_ALIAS = "encryption.key.alias";
     public static final String DYNAMIC_USERNAME_PASSWORD = "dynamic.username.password";
}
