/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.runtime.dev;

import com.sun.xml.ws.security.SecurityContextTokenInfo;

import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

/**
 * The <code> Session </Session> object is used to manage state between multiple requests
 * from the same client. It contains a session key field to uniquely identify the Session, 
 * a <code>SecurityInfo</code> field that contains the security parameters used to
 * protect the session and  userdata field that can store the state for multiple 
 * requests from the client.
 *
 * @author Bhakti Mehta
 * @author Mike Grogan
 */
@ManagedData
    @Description("RM and SC session information")
public class Session {

    /**
     * Well-known invocationProperty names
     */
    public static final String SESSION_ID_KEY = "com.sun.xml.ws.sessionid";
    public static final String SESSION_KEY = "com.sun.xml.ws.session";
    /**
     * Session manager that can handle Sessions of this exact type.
     * (Different SessionManagers might use different subclasses of this Class)
     */
    private SessionManager manager;
    /*
     * These fields might be persisted
     */
    /**
     * Unique key based either on the SCT or RM sequence id for the session
     */
    private String key;
    /**
     * A container for user-defined data that will be exposed in WebServiceContext.
     */
    private Object userData;

    private SecurityContextTokenInfo securityInfo;

    protected Session(){
    }

    /**
     * Public constructor
     *
     * @param manager - A <code>SessionManager</code> that can handle <code>Sessions</code>
     * of this type.  
     * @param key - The unique session id
     * @param data - Holder for user-defined data.
     */
    public Session(SessionManager manager, String key, Object userData) {
        this();
        
        this.manager = manager;
        this.userData = userData;
        this.key = key;
    }

    /**
     * Accessor for Session Key.
     *
     * @returns The session key
     */
    @ManagedAttribute
    @Description("Session key")
    public String getSessionKey() {
        return key;
    }

    /**
     * Accessor for the <code>userData</code> field.
     *
     * @return The value of the field.
     */
    public Object getUserData() {
        return userData;
    }

    /**
     * Accessor for the <code>securityInfo</code> field.
     * 
     * @returns The value of the field.
     */
    @ManagedAttribute
    @Description("Security context token info")
    public SecurityContextTokenInfo getSecurityInfo() {
        return securityInfo;
    }

    /**
     * Mutator for the <code>securityInfo</code> field.
     * 
     * @returns The value of the field.
     */
    public void setSecurityInfo(SecurityContextTokenInfo securityInfo) {
        this.securityInfo = securityInfo;
    }

    /**
     * Saves the state of the session using whatever persistence mechanism the
     * <code>SessionManager</code> offers.
     */
    public void save() {
        manager.saveSession(getSessionKey());
    }
}

