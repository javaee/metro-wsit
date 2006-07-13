/*
 The contents of this file are subject to the terms
 of the Common Development and Distribution License
 (the "License").  You may not use this file except
 in compliance with the License.

 You can obtain a copy of the license at
 https://jwsdp.dev.java.net/CDDLv1.0.html
 See the License for the specific language governing
 permissions and limitations under the License.

 When distributing Covered Code, include this CDDL
 HEADER in each file and include the License file at
 https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 add the following below this CDDL HEADER, with the
 fields enclosed by brackets "[]" replaced with your
 own identifying information: Portions Copyright [yyyy]
 [name of copyright owner]
*/
/*
 $Id: ActionDumpPipe.java,v 1.2 2006-07-13 18:38:56 arungupta Exp $

 Copyright (c) 2006 Sun Microsystems, Inc.
 All rights reserved.
*/

package com.sun.xml.ws.runtime;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.addressing.AddressingBuilderFactory;
import javax.xml.ws.addressing.AddressingConstants;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterPipeImpl;

/**
 * @author Arun Gupta
 */
public class ActionDumpPipe extends AbstractFilterPipeImpl {
    private final String name;

    private WSDLPort wsdlPort;

    public ActionDumpPipe(WSDLPort wsdlPort, Pipe next) {
        this("ActionDumpPipe", wsdlPort, next);
    }

    public ActionDumpPipe(String name, WSDLPort wsdlPort, Pipe next) {
        super(next);
        this.name = name;
        this.wsdlPort = wsdlPort;
    }

    /**
     * Copy constructor.
     */
    private ActionDumpPipe(ActionDumpPipe that, PipeCloner cloner) {
        super(that, cloner);
        this.name = that.name;
        this.wsdlPort = that.wsdlPort;
    }

    public Packet process(Packet packet) {
        dump(packet);
        Packet reply = next.process(packet);
        dump(reply);
        return reply;
    }

    protected void dump(Packet packet) {
        if (packet.getMessage() != null)
            dumpAction(packet);
    }

    protected void dumpAction(Packet packet) {
        try {
            Message m = packet.getMessage().copy();

            String to = getHeaderValue(m, TO_QNAME);
            String action = getHeaderValue(m, ACTION_QNAME);

            System.out.println("{To, Action}: {" + to + ", " + action + "}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static String getHeaderValue(Message m, QName headerName) throws XMLStreamException {
        Header h = m.getHeaders().get(headerName, false);

        if (h == null)
            return null;

        XMLStreamReader xsr = h.readHeader();
        xsr.next();
        return xsr.getText();
    }

    public Pipe copy(PipeCloner cloner) {
        return new ActionDumpPipe(this, cloner);
    }

    private static final AddressingConstants ac = AddressingBuilderFactory.newInstance().newAddressingBuilder().newAddressingConstants();
    private static final QName ACTION_QNAME = ac.getActionQName();
    private static final QName TO_QNAME = ac.getToQName();
}
