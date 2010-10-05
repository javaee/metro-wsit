/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.config.metro.parser;

import javax.xml.ws.WebServiceFeature;

/**
 *
 * @author Fabian Ritzmann
 */
public class ParsedElement {

    private final String portComponentName;
    private final String portComponentRefName;
    private final String operationWsdlName;
    private final boolean isInputMessage;
    private final boolean isOutputMessage;
    private final String faultWsdlName;
    private final WebServiceFeature webServiceFeature;

    /**
     * It would have been nice to have constructors like
     * ParsedElement(String portComponentName, WebServiceFeature feature) but that
     * does not work when you start adding e.g.
     * ParsedElement(String portComponentRefName, WebServiceFeature feature).
     *
     * @param portComponentName
     * @param feature
     */
    private ParsedElement(final String portComponentName, final String portComponentRefName,
            final String operationName, final boolean isInputMessage, final boolean isOutputMessage,
            final String faultName, final WebServiceFeature feature) {
        this.portComponentName = portComponentName;
        this.portComponentRefName = portComponentRefName;
        this.operationWsdlName = operationName;
        this.isInputMessage = isInputMessage;
        this.isOutputMessage = isOutputMessage;
        this.faultWsdlName = faultName;
        this.webServiceFeature = feature;
    }

    public static ParsedElement createPortComponentElement(final String portComponentName,
            final WebServiceFeature feature) {
        return new ParsedElement(portComponentName, null, null, false, false, null, feature);
    }

    public static ParsedElement createPortComponentRefElement(final String portComponentRefName,
            final WebServiceFeature feature) {
        return new ParsedElement(null, portComponentRefName, null, false, false, null, feature);
    }

    public static ParsedElement createPortComponentOperationElement(final String portComponentName,
            final String operationWsdlName, final WebServiceFeature feature) {
        return new ParsedElement(portComponentName, null, operationWsdlName, false, false, null, feature);
    }

    public static ParsedElement createPortComponentRefOperationElement(final String portComponentRefName,
            final String operationWsdlName, final WebServiceFeature feature) {
        return new ParsedElement(null, portComponentRefName, operationWsdlName, false, false, null, feature);
    }

    public static ParsedElement createPortComponentInputElement(final String portComponentName,
            final String operationWsdlName, final WebServiceFeature feature) {
        return new ParsedElement(portComponentName, null, operationWsdlName, true, false, null, feature);
    }

    public static ParsedElement createPortComponentRefInputElement(final String portComponentRefName,
            final String operationWsdlName, final WebServiceFeature feature) {
        return new ParsedElement(null, portComponentRefName, operationWsdlName, true, false, null, feature);
    }

    public static ParsedElement createPortComponentOutputElement(final String portComponentName,
            final String operationWsdlName, final WebServiceFeature feature) {
        return new ParsedElement(portComponentName, null, operationWsdlName, false, true, null, feature);
    }

    public static ParsedElement createPortComponentRefOutputElement(final String portComponentRefName,
            final String operationWsdlName, final WebServiceFeature feature) {
        return new ParsedElement(null, portComponentRefName, operationWsdlName, false, true, null, feature);
    }

    public static ParsedElement createPortComponentFaultElement(final String portComponentName,
            final String operationWsdlName, final String faultWsdlName, final WebServiceFeature feature) {
        return new ParsedElement(portComponentName, null, operationWsdlName, false, false, faultWsdlName, feature);
    }

    public static ParsedElement createPortComponentRefFaultElement(final String portComponentRefName,
            final String operationWsdlName, final String faultWsdlName, final WebServiceFeature feature) {
        return new ParsedElement(null, portComponentRefName, operationWsdlName, false, false, faultWsdlName, feature);
    }

    public String getPortComponentName() {
        return this.portComponentName;
    }

    public String getPortComponentRefName() {
        return this.portComponentRefName;
    }

    public String getOperationWsdlName() {
        return this.operationWsdlName;
    }

    public String getFaultWsdlName() {
        return this.faultWsdlName;
    }

    public WebServiceFeature getWebServiceFeature() {
        return this.webServiceFeature;
    }
    
    public boolean isInputMessage() {
        return this.isInputMessage;
    }

    public boolean isOutputMessage() {
        return this.isOutputMessage;
    }

    public boolean isPortComponent() {
        return (this.portComponentName != null) && (this.operationWsdlName == null);
    }

    public boolean isPortComponentRef() {
        return (this.portComponentRefName != null) && (this.operationWsdlName == null);
    }

    public boolean isPortComponentOperation() {
        return (this.portComponentName != null) && (this.operationWsdlName != null) &&
                (! this.isInputMessage) && (! this.isOutputMessage) && (this.faultWsdlName == null);
    }

    public boolean isPortComponentRefOperation() {
        return (this.portComponentRefName != null) && (this.operationWsdlName != null) &&
                (! this.isInputMessage) && (! this.isOutputMessage) && (this.faultWsdlName == null);
    }

    public boolean isPortComponentInput() {
        return (this.portComponentName != null) && (this.operationWsdlName != null) && this.isInputMessage;
    }

    public boolean isPortComponentOutput() {
        return (this.portComponentName != null) && (this.operationWsdlName != null) && this.isOutputMessage;
    }

    public boolean isPortComponentFault() {
        return (this.portComponentName != null) && (this.operationWsdlName != null) && (this.faultWsdlName != null);
    }

    public boolean isPortComponentRefInput() {
        return (this.portComponentRefName != null) && (this.operationWsdlName != null) && this.isInputMessage;
    }

    public boolean isPortComponentRefOutput() {
        return (this.portComponentRefName != null) && (this.operationWsdlName != null) && this.isOutputMessage;
    }

    public boolean isPortComponentRefFault() {
        return (this.portComponentRefName != null) && (this.operationWsdlName != null) && (this.faultWsdlName != null);
    }

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder("ParsedElement: ");
        if (isPortComponent()) {
            text.append("port component [");
            text.append(this.portComponentName);
            text.append(']');
        }
        else if (isPortComponentRef()) {
            text.append("port component ref [");
            text.append(this.portComponentRefName);
            text.append(']');
        }
        else if (isPortComponentOperation()) {
            text.append("port component operation [");
            text.append(this.portComponentName);
            text.append(", ");
            text.append(this.operationWsdlName);
            text.append(']');
        }
        else if (isPortComponentRefOperation()) {
            text.append("port component ref operation [");
            text.append(this.portComponentRefName);
            text.append(", ");
            text.append(this.operationWsdlName);
            text.append(']');
        }
        else if (isPortComponentInput()) {
            text.append("port component input [");
            text.append(this.portComponentName);
            text.append(", ");
            text.append(this.operationWsdlName);
            text.append(']');
        }
        else if (isPortComponentRefInput()) {
            text.append("port component ref input [");
            text.append(this.portComponentName);
            text.append(", ");
            text.append(this.operationWsdlName);
            text.append(']');
        }
        else if (isPortComponentOutput()) {
            text.append("port component output [");
            text.append(this.portComponentName);
            text.append(", ");
            text.append(this.operationWsdlName);
            text.append(']');
        }
        else if (isPortComponentRefOutput()) {
            text.append("port component ref output [");
            text.append(this.portComponentName);
            text.append(", ");
            text.append(this.operationWsdlName);
            text.append(']');
        }
        else if (isPortComponentFault()) {
            text.append("port component fault [");
            text.append(this.portComponentName);
            text.append(", ");
            text.append(this.operationWsdlName);
            text.append(", ");
            text.append(this.faultWsdlName);
            text.append(']');
        }
        else if (isPortComponentRefFault()) {
            text.append("port component ref fault [");
            text.append(this.portComponentRefName);
            text.append(", ");
            text.append(this.operationWsdlName);
            text.append(", ");
            text.append(this.faultWsdlName);
            text.append(']');
        }
        else {
            text.append("unknown element");
        }
        text.append(" -> [");
        text.append(this.webServiceFeature);
        text.append(']');
        return text.toString();
    }

}