/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.tx.at.common.endpoint;


import com.sun.xml.ws.tx.at.*;
import com.sun.xml.ws.tx.at.common.WSATVersion;
import com.sun.xml.ws.tx.at.common.WSATVersionStub;
import com.sun.xml.ws.tx.at.runtime.TransactionServices;
import com.sun.xml.ws.tx.at.internal.XidStub;

import javax.transaction.xa.Xid;
import javax.xml.ws.WebServiceContext;

/**
 *
 * @author paulparkinson
 */
public class EmulatedCoordinator extends Coordinator {

    public int preparedOperationCount;
    public int abortedOperationCount;
    public int readOnlyOperationCount;
    public int committedOperationCount;
    public int replayOperationCount;
    private Xid m_xid;
    private WSATXAResourceStub m_wsatXAResourceStub;
    private WSATHelperStub m_wsatHelper;
    private boolean m_isCallSuper;

    public EmulatedCoordinator(
            WebServiceContext m_context, WSATVersion m_version, Xid xid, WSATXAResourceStub wsatXAResourceStub,
            boolean callSuper) {
        super(m_context, m_version);
        m_xid = xid;
        m_wsatXAResourceStub = wsatXAResourceStub;
        m_wsatHelper = new WSATHelperStub(m_wsatXAResourceStub);
        m_isCallSuper = callSuper;
    }

    public static EmulatedCoordinator createDefault() {
        WebServiceContext context = null;
        Xid xid = new XidStub(true);
        WSATXAResourceStub wsatXAResourceStub = WSATXAResourceTest.createStubWSATXAResourceForXid(xid);
        WSATVersion version = new WSATVersionStub();
        return new EmulatedCoordinator(context, version, xid, wsatXAResourceStub, false);
    }

    @Override
    protected TransactionServices getTransactionServices() {
        return new EmulatedTransactionServices(); //this is not actually necessary
    }

    @Override
    WSATXAResource createWSATXAResourceForXidFromReplyTo(Xid xid) {
        return m_wsatXAResourceStub;
    }

    @Override
    Xid getXid() {
        return m_xid;
    }

    @Override
    boolean isDebugEnabled() {
        return false;
    }

    @Override
    protected WSATHelper getWSATHelper() {
        return m_wsatHelper;
    }

    public void preparedOperation(Object parameters) {
        if(m_isCallSuper) super.preparedOperation(parameters);
        preparedOperationCount++;
    }

    public void abortedOperation(Object parameters) {
        if(m_isCallSuper) super.abortedOperation(parameters);
        abortedOperationCount++;
    }

    public void readOnlyOperation(Object parameters) {
        if(m_isCallSuper) super.readOnlyOperation(parameters);
        readOnlyOperationCount++;
    }

    public void committedOperation(Object parameters) {
        if(m_isCallSuper) super.committedOperation(parameters);
        committedOperationCount++;
    }

    public void replayOperation(Object parameters) {
        if(m_isCallSuper) super.replayOperation(parameters);
        replayOperationCount++;
    }
}
