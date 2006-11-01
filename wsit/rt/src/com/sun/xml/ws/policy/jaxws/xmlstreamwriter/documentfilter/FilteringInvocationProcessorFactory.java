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

import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.InvocationProcessor;
import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.InvocationProcessorFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class FilteringInvocationProcessorFactory implements InvocationProcessorFactory {
    public enum FilterType {
        PRIVATE_ASSERTION_FILTER,
        MEX_FILTER
    }
    
    private FilterType filterType;
    
    public FilteringInvocationProcessorFactory(FilterType type) {
        this.filterType = type;
    }
    
    public InvocationProcessor createInvocationProcessor(XMLStreamWriter writer) throws XMLStreamException {
        switch (filterType) {
            case PRIVATE_ASSERTION_FILTER :
                return new PrivateAssertionFilteringInvocationProcessor(writer);
            case MEX_FILTER:
                return new MexImportFilteringInvocationProcessor(writer);
            default:
                throw new XMLStreamException(Messages.UNEXPECTED_FILTER_TYPE.format(filterType));
        }
    }
    
}
