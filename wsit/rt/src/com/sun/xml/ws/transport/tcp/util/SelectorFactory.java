/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.xml.ws.transport.tcp.util;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Class was copied from GlassFish Grizzly sources to be available
 * also for client side and don't require GlassFish to be installed

 * Factory used to dispatch/share <code>Selector</code>.
 *
 * @author Scott Oaks
 * @author Jean-Francois Arcand
 */
public final class SelectorFactory{
    
    /**
     * The timeout before we exit.
     */
    static long timeout = 5000;
    
    
    /**
     * The number of <code>Selector</code> to create.
     */
    static int maxSelectors = 20;
    
    
    /**
     * Cache of <code>Selector</code>
     */
    private final static Stack<Selector> selectors = new Stack<Selector>();
    
    
    /**
     * Creates the <code>Selector</code>
     */
    static {
        try{
            for (int i = 0; i < maxSelectors; i++) 
                selectors.add(Selector.open());
        } catch (IOException ex){
        }
    }

    
    /**
     * Get a exclusive <code>Selector</code>
     */
    public static Selector getSelector() {
        synchronized(selectors) {
            Selector s = null;
            try {
                if ( selectors.size() != 0 )
                    s = selectors.pop();
            } catch (EmptyStackException ex){}
                       
            int attempts = 0;
            try{
                while (s == null && attempts < 2) {
                    selectors.wait(timeout);
                    try {
                        if ( selectors.size() != 0 )
                            s = selectors.pop();
                    } catch (EmptyStackException ex){
                        break;
                    }
                    attempts++;
                }
            } catch (InterruptedException ex){};
            return s;
        }
    }


    /**
     * Return the <code>Selector</code> to the cache
     */
    public static void returnSelector(final Selector s) {
        synchronized(selectors) {
            selectors.push(s);
            if (selectors.size() == 1)
                selectors.notify();
        }
    }

}