/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * AcknowledgementListener.java
 *
 * @author Mike Grogan
 * Created on October 30, 2006, 1:17 PM
 *
 */

package com.sun.xml.ws.api.rm;

import com.sun.xml.ws.api.rm.client.ClientSequence;
import com.sun.xml.ws.rm.jaxws.runtime.client.*;

/**
 * Interface implemented by consumers of SequenceAcknowledgement
 * notifications
 */
public interface AcknowledgementListener {
   
   /**
    * Called when the state of a message is changed to <code>complete</code>,
    * meaning that the message has been acked, and its response, if any, has been received.
    * The notification is only called for messages belonging to sequences for which
    * an Acknowledgement has been registered using the <code>setAcknowledgementListener</code> 
    * method of {@link ClientSequence}
    *
    * @param seq The sequence to which the completed message belongs
    * @param messageNumber The message number of the message.
    */
   public void notify(ClientSequence seq, int messageNumber);
}
