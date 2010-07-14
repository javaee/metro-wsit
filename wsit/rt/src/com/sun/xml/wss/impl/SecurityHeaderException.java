/*
 * $Id: SecurityHeaderException.java,v 1.3.2.2 2010-07-14 14:05:53 m_potociar Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.wss.impl;

import com.sun.xml.wss.*;

/**
 * A SecurityHeaderException indicates that there is a problem with the
 * security header elements and subelements.
 * It indicates that there is an error in the input message to a MessageFilter.  
 * For example, a ds:keyInfo element may not contain a reference to a
 * security token. If such a reference is missing, then to
 * indicate this problem, an instance of this Exception would be thrown.
 * 
 * <p>
 * This is as opposed to a problem with processing the message itself.  An
 * example would be a MessageFilter that needs to look up data in a
 * database that is not currently available. A XWSSecurityException would
 * be thrown in the latter case.
 *
 * @author Edwin Goei
 * @author Manveen Kaur
 *
 */
public class SecurityHeaderException extends XWSSecurityException {
    public SecurityHeaderException(String message) {
        super(message);
    }

    public SecurityHeaderException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SecurityHeaderException(Throwable cause) {
        super(cause);
    }
}
