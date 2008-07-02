package com.sun.xml.ws.messagedump;

import com.sun.xml.ws.assembler.*;
import com.sun.xml.ws.api.pipe.Tube;
import javax.xml.ws.WebServiceException;

public class MessageDumpingTubeAppender implements TubeAppender {

    public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
        MessageDumpingFeature messageDumpingFeature = context.getBinding().getFeature(MessageDumpingFeature.class);
        if (messageDumpingFeature != null) {
            return new MessageDumpingTube(context.getTubelineHead(), messageDumpingFeature);
        }

        return context.getTubelineHead();
    }

    public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
        MessageDumpingFeature messageDumpingFeature = context.getEndpoint().getBinding().getFeature(MessageDumpingFeature.class);
        if (messageDumpingFeature != null) {
            return new MessageDumpingTube(context.getTubelineHead(), messageDumpingFeature);
        }
        
        return context.getTubelineHead();
    }
}
