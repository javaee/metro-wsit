package com.sun.xml.ws.dump;

import com.sun.xml.ws.assembler.*;
import com.sun.xml.ws.api.pipe.Tube;
import javax.xml.ws.WebServiceException;

public final class MessageDumpingTubeFactory implements TubeFactory {

    public Tube createTube(ClientTubelineAssemblyContext context) throws WebServiceException {
        MessageDumpingFeature messageDumpingFeature = context.getBinding().getFeature(MessageDumpingFeature.class);
        if (messageDumpingFeature != null) {
            return new MessageDumpingTube(context.getTubelineHead(), messageDumpingFeature);
        }

        return context.getTubelineHead();
    }

    public Tube createTube(ServerTubelineAssemblyContext context) throws WebServiceException {
        MessageDumpingFeature messageDumpingFeature = context.getEndpoint().getBinding().getFeature(MessageDumpingFeature.class);
        if (messageDumpingFeature != null) {
            return new MessageDumpingTube(context.getTubelineHead(), messageDumpingFeature);
        }
        
        return context.getTubelineHead();
    }
}
