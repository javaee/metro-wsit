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

package com.sun.xml.ws.transport.tcp.encoding;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.encoding.fastinfoset.FastInfosetStreamReaderFactory;
import java.io.InputStream;

/**
 * @author Alexey Stashok
 */
public class WSTCPFastInfosetStreamReaderRecyclable extends StAXDocumentParser implements XMLStreamReaderFactory.RecycleAware {
    private final RecycleAwareListener listener;
    
    public WSTCPFastInfosetStreamReaderRecyclable(InputStream in, RecycleAwareListener listener) {
        super(in);
        this.listener = listener;
    }
    
    public void onRecycled() {
        listener.onRecycled();
    }
    
    public interface RecycleAwareListener {
        public void onRecycled();
    }
}
