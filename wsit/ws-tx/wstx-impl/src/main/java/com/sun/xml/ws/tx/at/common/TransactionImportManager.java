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

package com.sun.xml.ws.tx.at.common;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages; 

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.resource.spi.XATerminator;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 *  Access Transaction Inflow Contract from Java Connector 1.5 API.
 *  Assumption is the underlying TransactionManager is implementing this
 *  interface.
 *
 *  Separate this from TransactionManagerImpl since this provides mostly service side assistance.
 *  Assists in supporting application client and standalone client to separate from more commonly
 *  used methods in TransactionManagerImpl.
 */
public class TransactionImportManager implements TransactionImportWrapper {

    private static final class MethodInfo<T> {

        final String methodName;
        final Class<?>[] parameterTypes;
        final Class<?> returnType;
        final Class<T> returnTypeCaster;
        //
        Method method;

        public MethodInfo(String methodName, Class<?>[] parameterTypes, Class<T> returnType) {
            this(methodName, parameterTypes, returnType, returnType);
        }

        public MethodInfo(String methodName, Class<?>[] parameterTypes, Class<?> returnType, Class<T> returnTypeCaster) {
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.returnType = returnType;
            this.returnTypeCaster = returnTypeCaster;
        }

        public boolean isCompatibleWith(Method m) {
            if (!methodName.equals(m.getName())) {
                return false;
            }

            if (!Modifier.isPublic(m.getModifiers())) {
                return false;
            }

            if (!returnType.isAssignableFrom(m.getReturnType())) {
                return false;
            }

            Class<?>[] otherParamTypes = m.getParameterTypes();
            if (parameterTypes.length != otherParamTypes.length) {
                return false;
            }
            for (int i = 0; i < parameterTypes.length; i++) {
                if (!parameterTypes[i].isAssignableFrom(otherParamTypes[i])) {
                    return false;
                }
            }

            return true;
        }

        public T invoke(TransactionManager tmInstance, Object... args) {
            try {
                Object result = method.invoke(tmInstance, args);
                return returnTypeCaster.cast(result);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    private static final Logger LOGGER = Logger.getLogger(TransactionImportManager.class);
    private static TransactionImportManager INSTANCE;

    public static TransactionImportManager getInstance() {
        if(INSTANCE==null) INSTANCE = new TransactionImportManager();
        return INSTANCE;

    }
    //
    static private TransactionManager javaeeTM;
    private final MethodInfo<?> recreate;
    private final MethodInfo<?> release;
    private final MethodInfo<XATerminator> getXATerminator;
    private final MethodInfo<Integer> getTransactionRemainingTimeout;
    private final MethodInfo<Xid> getXid;
    private final MethodInfo<Transaction> getTransaction;
    private final MethodInfo<String> getTxLogLocation;
    static private MethodInfo<?> registerRecoveryResourceHandler;

    private TransactionImportManager() {
        this(TransactionManagerImpl.getInstance().getTransactionManager());
    }

    private TransactionImportManager(TransactionManager tm) {
        javaeeTM = tm;

        this.recreate = new MethodInfo<Void>(
                "recreate",
                new Class<?>[]{Xid.class, long.class},
                void.class);
        this.release = new MethodInfo<Void>(
                "release",
                new Class<?>[]{Xid.class},
                void.class);
        this.getXATerminator = new MethodInfo<XATerminator>(
                "getXATerminator",
                new Class<?>[]{},
                XATerminator.class);
        this.getTransactionRemainingTimeout = new MethodInfo<Integer>(
                "getTransactionRemainingTimeout",
                new Class<?>[]{},
                int.class,
                Integer.class);
        this.getXid = new MethodInfo<Xid>(
                "getXid",
                new Class<?>[]{},
                Xid.class);
        this.getTransaction = new MethodInfo<Transaction>(
                "getTransaction",
                new Class<?>[]{Xid.class},
                Transaction.class);
        this.getTxLogLocation = new MethodInfo<String>(
                "getTxLogLocation",
                new Class<?>[]{},
                String.class);
        registerRecoveryResourceHandler = new MethodInfo<Void>(
                "registerRecoveryResourceHandler",
                new Class<?>[]{XAResource.class},
                void.class);
        MethodInfo<?>[] requiredMethods = new MethodInfo<?>[]{
            recreate,
            release,
            getXATerminator,
            getTransactionRemainingTimeout,
            getXid,
            getTransaction,
            getTxLogLocation,
            registerRecoveryResourceHandler    
        };

        int remainingMethodsToFind = requiredMethods.length;

        if (javaeeTM != null) {
            for (Method m : javaeeTM.getClass().getDeclaredMethods()) {
                for (MethodInfo mi : requiredMethods) {
                    if (mi.isCompatibleWith(m)) {
                        mi.method = m;
                        remainingMethodsToFind--;
                    }
                }

                if (remainingMethodsToFind == 0) {
                    break;
                }
            }
        }

        if (remainingMethodsToFind != 0) {
            StringBuilder sb =
                    new StringBuilder("Missing required extension methods detected on '" + TransactionManager.class.getName() + "' implementation '" + javaeeTM.getClass().getName() + "':\n");
            for (MethodInfo mi : requiredMethods) {
                if (mi.method == null) {
                    sb.append(mi.methodName).append("\n");
                }
            }
            // TODO log and throw? error
        }
    }

    /**
     * ${@inheritDoc }
     */
    public void recreate(final Xid xid, final long timeout) {
        recreate.invoke(javaeeTM, xid, timeout);
    }

    /**
     * ${@inheritDoc }
     */
    public void release(final Xid xid) {
        release.invoke(javaeeTM, xid);
    }

    /**
     * ${@inheritDoc }
     */
    public XATerminator getXATerminator() {
        return getXATerminator.invoke(javaeeTM);
    }

    /**
     * ${@inheritDoc }
     */
    public int getTransactionRemainingTimeout() throws SystemException {
        final String METHOD = "getTransactionRemainingTimeout";
        int result = 0;
        try {
            result = getTransactionRemainingTimeout.invoke(javaeeTM);
        } catch (IllegalStateException ise) {
            LOGGER.finest(METHOD + " " + LocalizationMessages.WSAT_4617_TXN_MGR_LOOKUP_TXN_TIMEOUT(), ise);
        }
        return result;
    }

    //todo exception handling....
    public Xid getXid() throws SystemException {
        return getXid.invoke(javaeeTM);
    }

    public Transaction getTransaction(Xid xid) {
        return getTransaction.invoke(javaeeTM, xid);
    }

    public String getTxLogLocation() {
        return getTxLogLocation.invoke(javaeeTM);
    }

    public static void registerRecoveryResourceHandler(XAResource xaResource) {
        registerRecoveryResourceHandler.invoke(javaeeTM, xaResource);
    }
}
