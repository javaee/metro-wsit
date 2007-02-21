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
package com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter;

import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.Invocation;
import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.InvocationProcessingException;
import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.InvocationProcessor;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter.InvocationProcessingState.START_BUFFERING;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class FilteringInvocationProcessor implements InvocationProcessor {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(FilteringInvocationProcessor.class);
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();
    
    private final class StateMachineContext {
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
            return (bufferRef != null) ? bufferRef.get() : null;
        }
        
        public void setBuffer(InvocationBuffer buffer) {
            this.bufferRef = new WeakReference<InvocationBuffer>(buffer);
        }
        
    }
    
    private final class InvocationBuffer {
        private Queue<Invocation> queue;
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
    
    private int filteringCount; // indicates how many state machines require filtering
    private boolean doFiltering; // indicates if filtering is performed or not
    
    /** Creates a new instance of FilteringInvocationProcessor */
    public FilteringInvocationProcessor(final XMLStreamWriter writer, final FilteringStateMachine... stateMachines) throws XMLStreamException{
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
    }
    
    public Object process(final Invocation invocation) throws InvocationProcessingException {
        LOGGER.entering(invocation);
        try {
            this.startBufferingCandidates.clear();
            this.stopBufferingCandidates.clear();
            this.startFilteringCandidates.clear();
            for (StateMachineContext context : this.stateMachineContexts) {
                InvocationProcessingState state = context.getStateMachine().getState(invocation, mirrorWriter);
                
                switch (state) {
                    case START_BUFFERING:
                    case RESTART_BUFFERING:
                        this.startBufferingCandidates.add(context);
                        if (state == START_BUFFERING) {
                            break;
                        }
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
            
            // filtered buffers
            int firstFilteredBufferIndex = invocationBuffers.size();
            for (StateMachineContext context : startFilteringCandidates) {
                InvocationBuffer buffer = context.getBuffer();
                context.setBuffer(null);
                int currentBufferIndex;
                if ((currentBufferIndex = invocationBuffers.indexOf(buffer)) < firstFilteredBufferIndex) {
                    firstFilteredBufferIndex = currentBufferIndex;
                }
            }
            while (invocationBuffers.size() > firstFilteredBufferIndex) {
                InvocationBuffer filteredBuffer = invocationBuffers.removeLast();
                filteredBuffer.clear();
            }
            
            // stopped buffers
            for (StateMachineContext context : stopBufferingCandidates) {
                InvocationBuffer buffer = context.getBuffer();
                context.setBuffer(null);
                if (buffer != null) {
                    int newRefCount = buffer.removeReference();
                    if (newRefCount == 0) {
                        int bufferIndex;
                        if ((bufferIndex = invocationBuffers.indexOf(buffer)) != -1) {
                            invocationBuffers.remove(bufferIndex);
                            if (bufferIndex == 0) {
                                executeCommands(originalWriter, buffer);
                            } else {
                                invocationBuffers.get(bufferIndex - 1).getQueue().addAll(buffer.getQueue());
                            }
                        }
                    }
                }
            }
            
            //started buffers (must be placed after stopped buffers so that restart buffering works properly)
            if (filteringCount == 0 && startBufferingCandidates.size() > 0) {
                InvocationBuffer buffer = new InvocationBuffer(startBufferingCandidates.size());
                invocationBuffers.addLast(buffer);
                for (StateMachineContext context : startBufferingCandidates) {
                    context.setBuffer(buffer);
                }
            }
            
            // start filtering if it is not active and should be
            if (!doFiltering) {
                doFiltering = filteringCount > 0;
            }
            
            // choose invocation target and execute invocation
            Object invocationTarget;
            if (doFiltering) {
                doFiltering = filteringCount > 0; // stop filtering for the next call if there are no more filtering requests active
                invocationTarget = mirrorWriter;
            } else {
                if (!invocationBuffers.isEmpty()) {
                    invocationBuffers.getLast().getQueue().offer(invocation);
                    invocationTarget = mirrorWriter;
                } else {
                    invocation.execute(mirrorWriter);
                    invocationTarget = originalWriter;
                }
            }
            
            return invocation.execute(invocationTarget);
        } catch (IllegalArgumentException e) {
            throw LOGGER.logSevereException(new InvocationProcessingException(invocation, e));
        } catch (InvocationTargetException e) {
            throw LOGGER.logSevereException(new InvocationProcessingException(invocation, e.getCause()));
        } catch (IllegalAccessException e) {
            throw LOGGER.logSevereException(new InvocationProcessingException(invocation, e));
        } finally {
            LOGGER.exiting();
        }
    }
    
    private void executeCommands(final XMLStreamWriter writer, InvocationBuffer invocationBuffer) throws IllegalAccessException, InvocationProcessingException {
        Queue<Invocation> invocationQueue = invocationBuffer.getQueue();
        while (!invocationQueue.isEmpty()) {
            final Invocation command = invocationQueue.poll();
            try {
                command.execute(writer);
            } catch (InvocationTargetException e) {
                throw LOGGER.logSevereException(new InvocationProcessingException(command, e));
            }
        }
    }
}
