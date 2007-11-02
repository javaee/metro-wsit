/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.assembler;

import com.sun.xml.ws.api.pipe.Tube;
import java.util.LinkedList;
import java.util.Queue;
import javax.xml.ws.WebServiceFeature;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class MessageDumpingFeature extends WebServiceFeature {
    private static final String featureId = MessageDumpingFeature.class.getName();
    
    private Tube tube;
    private Queue<String> messageQueue = new LinkedList<String>();
    
    /** Creates a new instance of MessageDumpingFeature */
    public MessageDumpingFeature() {
    }
    
    public String getID() {
        return featureId;
    }
    
    public Tube createMessageDumpingTube(Tube next) {
        return new MessageDumpingTube(messageQueue, next);
    }
    
    public String nextMessage() {
        return messageQueue.poll();
    }
}
