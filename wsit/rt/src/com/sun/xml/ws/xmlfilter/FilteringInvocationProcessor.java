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
package com.sun.xml.ws.xmlfilter;

import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.istack.logging.Logger;

import static com.sun.xml.ws.xmlfilter.ProcessingStateChange.START_BUFFERING;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class FilteringInvocationProcessor implements InvocationProcessor {

    private static final Logger LOGGER = Logger.getLogger(FilteringInvocationProcessor.class);
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    private static final class StateMachineContext {

        private final FilteringStateMachine stateMachine;
        private WeakReference<InvocationBuffer> bufferRef;

        StateMachineContext(final FilteringStateMachine stateMachine) {
            this.stateMachine = stateMachine;
            bufferRef = null;
        }

        public FilteringStateMachine getStateMachine() {
            return stateMachine;
        }

        public InvocationBuffer getBuffer() {
            return (bufferRef == null) ? null : bufferRef.get();
        }

        public void setBuffer(final InvocationBuffer buffer) {
            this.bufferRef = new WeakReference<InvocationBuffer>(buffer);
        }
    }

    private static final class InvocationBuffer {

        private final Queue<Invocation> queue;
        private int referenceCount;

        InvocationBuffer(int refCount) {
            this.queue = new LinkedList<Invocation>();
            this.referenceCount = refCount;
        }

        public Queue<Invocation> getQueue() {
            return queue;
        }

        public int removeReference() {
            if (referenceCount > 0) {
                referenceCount--;
            }
            return referenceCount;
        }

        public void clear() {
            queue.clear();
            referenceCount = 0;
        }
    }
    private final XMLStreamWriter originalWriter; // underlying XML stream writer which we use to eventually serve the requests
    private final XMLStreamWriter mirrorWriter;   // mirror XML stream writer we use to buffer original requests (even those filtered) so that we can serve queries during filtering phase
    private final LinkedList<InvocationBuffer> invocationBuffers; // parser method invocation queue that stores invocation requests to be still executed on the underlying XML output stream
    private final StateMachineContext[] stateMachineContexts; // state machines driving filtering mechanisms
    private final List<StateMachineContext> startBufferingCandidates;
    private final List<StateMachineContext> stopBufferingCandidates;
    private final List<StateMachineContext> startFilteringCandidates;
    private final InvocationTransformer invocationTransformer;
    private int filteringCount; // indicates how many state machines currently require filtering
    private boolean filtering; // indicates if filtering is currently swithed on or not

    /** Creates a new instance of FilteringInvocationProcessor */
    public FilteringInvocationProcessor(final XMLStreamWriter writer, final FilteringStateMachine... stateMachines) throws XMLStreamException {
        this(writer, null, stateMachines);
    }

    /** Creates a new instance of FilteringInvocationProcessor */
    public FilteringInvocationProcessor(final XMLStreamWriter writer, final InvocationTransformer transformer, final FilteringStateMachine... stateMachines) throws XMLStreamException {
        this.originalWriter = writer;
        this.stateMachineContexts = new StateMachineContext[stateMachines.length];
        for (int i = 0; i < stateMachines.length; i++) {
            this.stateMachineContexts[i] = new StateMachineContext(stateMachines[i]);
        }

        this.mirrorWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(new StringWriter());
        this.invocationBuffers = new LinkedList<InvocationBuffer>();
        this.startBufferingCandidates = new LinkedList<StateMachineContext>();
        this.stopBufferingCandidates = new LinkedList<StateMachineContext>();
        this.startFilteringCandidates = new LinkedList<StateMachineContext>();
        this.invocationTransformer = transformer;
    }

    public Object process(final Invocation invocation) throws InvocationProcessingException {
        if (invocation.getMethodType().isFilterable()) {
            if (invocationTransformer != null) {
                Collection<Invocation> transformedInvocations = invocationTransformer.transform(invocation);

                Object returnValue = null;
                for (Invocation transformedInvocation : transformedInvocations) {
                    if (transformedInvocation == invocation) {
                        returnValue = filter(transformedInvocation);
                    } else {
                        filter(transformedInvocation);
                    }
                }
                return returnValue;
            }

            return filter(invocation);
        } else {
            switch (invocation.getMethodType()) {
                case FLUSH:
                    invocation.execute(originalWriter);
                    return invocation.execute(mirrorWriter);
                case CLOSE:
                    executeAllBufferedInvocations(originalWriter);
                    invocation.execute(originalWriter);
                    return invocation.execute(mirrorWriter);
                default:
                    return invocation.execute(mirrorWriter);
            }
        }
    }

    private Object filter(final Invocation invocation) throws InvocationProcessingException {
        LOGGER.entering(invocation);
        try {
            resolveStateChangeCandidates(invocation);

            processStartFilteringCandidates();
            processStopBufferingCandidates();
            processStartBufferingCandidates();
            updateFilteringStatus();

            // choose invocation target and executeBatch invocation
            XMLStreamWriter invocationTarget;
            if (filtering) {
                filtering = filteringCount > 0; // stop filtering for the next call if there are no more filtering requests active
                invocationTarget = mirrorWriter;
            } else {
                if (invocationBuffers.isEmpty()) {
                    invocation.execute(mirrorWriter);
                    invocationTarget = originalWriter;
                } else {
                    invocationBuffers.getLast().getQueue().offer(invocation);
                    invocationTarget = mirrorWriter;
                }
            }

            return invocation.execute(invocationTarget);
        } finally {
            LOGGER.exiting();
        }
    }

    private void processStartBufferingCandidates() {
        //started buffers (must be placed after stopped buffers so that restart buffering works properly)
        if (filteringCount == 0 && startBufferingCandidates.size() > 0) {
            final InvocationBuffer buffer = new InvocationBuffer(startBufferingCandidates.size());
            invocationBuffers.addLast(buffer);
            for (StateMachineContext context : startBufferingCandidates) {
                context.setBuffer(buffer);
            }
        }
    }

    private void processStartFilteringCandidates() {
        // filtered buffers
        int firstFilteredBufferIndex = invocationBuffers.size();
        for (StateMachineContext context : startFilteringCandidates) {
            final InvocationBuffer buffer = context.getBuffer();
            context.setBuffer(null);
            int currentBufferIndex;
            if ((currentBufferIndex = invocationBuffers.indexOf(buffer)) < firstFilteredBufferIndex) {
                firstFilteredBufferIndex = currentBufferIndex;
            }
        }
        while (invocationBuffers.size() > firstFilteredBufferIndex) {
            final InvocationBuffer filteredBuffer = invocationBuffers.removeLast();
            filteredBuffer.clear();
        }
    }

    private void processStopBufferingCandidates() throws InvocationProcessingException {
        // stopped buffers
        for (StateMachineContext context : stopBufferingCandidates) {
            final InvocationBuffer buffer = context.getBuffer();
            context.setBuffer(null);
            if (buffer == null) {
                continue;
            }

            if (buffer.removeReference() == 0) {
                int bufferIndex;
                if ((bufferIndex = invocationBuffers.indexOf(buffer)) != -1) {
                    invocationBuffers.remove(bufferIndex);
                    if (bufferIndex == 0) {
                        Invocation.executeBatch(originalWriter, buffer.getQueue());
                    } else {
                        invocationBuffers.get(bufferIndex - 1).getQueue().addAll(buffer.getQueue());
                    }
                }
            }
        }
    }

    private void resolveStateChangeCandidates(Invocation invocation) {
        this.startBufferingCandidates.clear();
        this.stopBufferingCandidates.clear();
        this.startFilteringCandidates.clear();
        for (StateMachineContext context : this.stateMachineContexts) {
            switch (context.getStateMachine().getStateChange(invocation, mirrorWriter)) {
                case START_BUFFERING:
                    this.startBufferingCandidates.add(context);
                    break;
                case RESTART_BUFFERING:
                    this.startBufferingCandidates.add(context);
                    if (context.getBuffer() != null) {
                        this.stopBufferingCandidates.add(context);
                    }
                    break;
                case STOP_BUFFERING:
                    if (context.getBuffer() != null) {
                        this.stopBufferingCandidates.add(context);
                    }
                    break;
                case START_FILTERING:
                    if (context.getBuffer() != null) {
                        this.startFilteringCandidates.add(context);
                    }
                    filteringCount++;
                    break;
                case STOP_FILTERING:
                    filteringCount--;
                    break;
                default:
                    break;
                }
        }
    }

    private void updateFilteringStatus() {
        // start filtering if it is not active and should be
        if (!filtering) {
            filtering = filteringCount > 0;
        }
    }

    private void executeAllBufferedInvocations(XMLStreamWriter target) {
        while (!invocationBuffers.isEmpty()) {
            InvocationBuffer buffer = invocationBuffers.removeFirst();
            Invocation.executeBatch(target, buffer.getQueue());
            buffer.clear();
        }
    }
}
