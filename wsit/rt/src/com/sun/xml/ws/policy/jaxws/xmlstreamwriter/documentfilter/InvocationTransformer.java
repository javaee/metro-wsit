/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter;

import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.Invocation;
import java.util.Collection;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public interface InvocationTransformer {
    
    /**
     * Before this invocation is processed by {@link FilteringStateMachine} instances
     * the {@link InvocationTransformer} gets a chance to transform the {@code invocation}
     * into series of several invocations. Original invocation may be included as well.
     * 
     * @param invocation original invocation to be transformed.
     * @return collection of invocations as a result of the transformation
     */
    public Collection<Invocation> transform(Invocation invocation);
}
