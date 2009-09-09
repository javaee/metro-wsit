package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.xml.ws.rx.rm.protocol.AcknowledgementData;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessageBase;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.AckRange;
import java.util.List;

class DummyAppMessage extends ApplicationMessageBase {


    public DummyAppMessage(String sequenceId, long messageNumber, String ackSequenceId, List<AckRange> ackRanges, boolean ackReqestedFlag, String correlationId) {
        super(correlationId, sequenceId, messageNumber, null);

        AcknowledgementData.Builder ackDataBuilder = AcknowledgementData.getBuilder();
        if (ackReqestedFlag) {
            ackDataBuilder.ackReqestedSequenceId(sequenceId);
        }
        ackDataBuilder.acknowledgements(ackSequenceId, ackRanges, false);

        setAcknowledgementData(ackDataBuilder.build());
    }

    public DummyAppMessage(String correlationId, String sequenceId, long messageNumber, AcknowledgementData acknowledgementData) {
        super(correlationId, sequenceId, messageNumber, acknowledgementData);
    }

    public DummyAppMessage(String correlationId) {
        super(correlationId);
    }
}
