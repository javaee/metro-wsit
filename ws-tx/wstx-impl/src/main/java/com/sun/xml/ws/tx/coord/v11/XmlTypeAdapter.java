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

package com.sun.xml.ws.tx.coord.v11;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.tx.coord.common.types.*;
import com.sun.xml.ws.tx.coord.v11.types.*;
import com.sun.xml.ws.tx.coord.v11.types.RegisterResponseType;
import com.sun.xml.ws.tx.at.WSATConstants;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.Map;


public class XmlTypeAdapter {



    public static BaseExpires<Expires> adapt(final Expires delegate) {
        if (delegate == null) return null;
        else return new ExpiresImpl(delegate);
    }

    public static BaseIdentifier<CoordinationContextType.Identifier> adapt(final CoordinationContextType.Identifier delegate) {
        if (delegate == null) return null;
        else return new IdentifierImpl(delegate);
    }

/*
    public CoordinationContextTypeIF<W3CEndpointReference, Expires, CoordinationContextType.Identifier,CoordinationContextType> adapt(final CoordinationContextType delegate) {
        if (delegate == null) return null;
        else return new CoordinationContextTypeImpl(delegate, this);
    }
*/

    public static CoordinationContextIF<W3CEndpointReference, Expires, CoordinationContextType.Identifier,CoordinationContextType> adapt(final CoordinationContext delegate) {
        if (delegate == null) return null;
        else return new CoordinationContextImpl(delegate);
    }


    public static BaseRegisterType<W3CEndpointReference, RegisterType> adapt(final RegisterType delegate) {
        if (delegate == null) return null;
        else return new RegisterTypeImpl(delegate);
    }

    public static BaseRegisterType<W3CEndpointReference, RegisterType> newRegisterType() {
         return new RegisterTypeImpl(new RegisterType());
    }

   public static BaseRegisterResponseType<W3CEndpointReference,RegisterResponseType> adapt(final RegisterResponseType delegate) {
       if (delegate == null) return null;
       else return new RegisterResponseTypeImpl(delegate);
   }

    public static BaseRegisterResponseType newRegisterResponseType() {
        return new RegisterResponseTypeImpl(new RegisterResponseType());
    }

    static class ExpiresImpl extends BaseExpires<Expires> {

        protected ExpiresImpl(Expires delegate) {
            super(delegate);
        }

        public long getValue() {
            return delegate.getValue();
        }

        public void setValue(long value) {
            delegate.setValue(value);
        }

        public Map getOtherAttributes() {
            return delegate.getOtherAttributes();
        }
    }

    static class IdentifierImpl extends BaseIdentifier<CoordinationContextType.Identifier> {

        protected IdentifierImpl(CoordinationContextType.Identifier delegate) {
            super(delegate);
        }

        public String getValue() {
            return delegate.getValue();
        }

        public void setValue(String value) {
            delegate.setValue(value);
        }

        public Map<QName, String> getOtherAttributes() {
            return delegate.getOtherAttributes();
        }

        public QName getQName() {
            return new QName(WSATConstants.WSCOOR11_NS_URI,WSATConstants.IDENTIFIER);
        }

    }


    public static class CoordinationContextTypeImpl implements CoordinationContextTypeIF<W3CEndpointReference,Expires, CoordinationContextType.Identifier,CoordinationContextType> {
        private CoordinationContextType delegate;

        public CoordinationContextTypeImpl(CoordinationContextType delegate) {
            this.delegate = delegate;
        }

        public BaseIdentifier<CoordinationContextType.Identifier> getIdentifier() {
            return XmlTypeAdapter.adapt(delegate.getIdentifier());
        }

        public void setIdentifier(BaseIdentifier<CoordinationContextType.Identifier> value) {
            delegate.setIdentifier(value.getDelegate());
        }

        public BaseExpires<Expires> getExpires() {
            return XmlTypeAdapter.adapt(delegate.getExpires());
        }

        public void setExpires(BaseExpires<Expires> value) {
            delegate.setExpires(value.getDelegate());
        }


        public String getCoordinationType() {
            return delegate.getCoordinationType();
        }

        public void setCoordinationType(String value) {
            delegate.setCoordinationType(value);
        }

        public W3CEndpointReference getRegistrationService() {
            return delegate.getRegistrationService();
        }

        public void setRegistrationService(W3CEndpointReference value) {
            delegate.setRegistrationService(value);
        }

        public Map<QName, String> getOtherAttributes() {
            return delegate.getOtherAttributes();
        }

        public CoordinationContextType getDelegate() {
            return delegate;
        }
    }

    public static class CoordinationContextImpl extends CoordinationContextTypeImpl implements CoordinationContextIF<W3CEndpointReference, Expires, CoordinationContextType.Identifier, CoordinationContextType> {
        final static JAXBRIContext jaxbContext  = getCoordinationContextJaxbContext();
        private static JAXBRIContext getCoordinationContextJaxbContext() {
            try {
                return (JAXBRIContext)JAXBRIContext.newInstance(CoordinationContext.class);
            } catch (JAXBException e) {
                throw new WebServiceException("Error creating JAXBContext for CoordinationContext. ", e);
            }
        }
        public CoordinationContextImpl(CoordinationContext delegate) {
            super(delegate);
        }

        public List<Object> getAny() {
            return getDelegate().getAny();  //To change body of implemented methods use File | Settings | File Templates.
        }

        public JAXBRIContext getJAXBRIContext() {
            return jaxbContext;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public CoordinationContext getDelegate() {
            return (CoordinationContext) super.getDelegate();    //To change body of overridden methods use File | Settings | File Templates.
        }
    }


    public static class RegisterTypeImpl extends BaseRegisterType<W3CEndpointReference, RegisterType> {

        RegisterTypeImpl(RegisterType delegate) {
            super(delegate);
        }

        @Override
        public String getProtocolIdentifier() {
            return delegate.getProtocolIdentifier();
        }

        @Override
        public void setProtocolIdentifier(String value) {
            delegate.setProtocolIdentifier(value);
        }

        @Override
        public W3CEndpointReference getParticipantProtocolService() {
            return delegate.getParticipantProtocolService();
        }

        @Override
        public void setParticipantProtocolService(W3CEndpointReference value) {
            delegate.setParticipantProtocolService(value);
        }

        @Override
        public List<Object> getAny() {
            return delegate.getAny();
        }

        @Override
        public Map<QName, String> getOtherAttributes() {
            return delegate.getOtherAttributes();
        }

        @Override
        public boolean isDurable() {
            return WSATConstants.WSAT11_DURABLE_2PC.equals(delegate.getProtocolIdentifier());
        }

        @Override
        public boolean isVolatile() {
            return WSATConstants.WSAT11_DURABLE_2PC.equals(delegate.getProtocolIdentifier());
        }
    }

    static class RegisterResponseTypeImpl extends BaseRegisterResponseType<W3CEndpointReference, RegisterResponseType> {

        RegisterResponseTypeImpl(RegisterResponseType delegate) {
            super(delegate);
        }


        @Override
        public W3CEndpointReference getCoordinatorProtocolService() {
            return delegate.getCoordinatorProtocolService();
        }

        @Override
        public void setCoordinatorProtocolService(W3CEndpointReference value) {
            delegate.setCoordinatorProtocolService(value);
        }

        @Override
        public List<Object> getAny() {
            return delegate.getAny();
        }

        @Override
        public Map<QName, String> getOtherAttributes() {
            return delegate.getOtherAttributes();
        }

        @Override
        public RegisterResponseType getDelegate() {
            return delegate;
        }
    }
}
