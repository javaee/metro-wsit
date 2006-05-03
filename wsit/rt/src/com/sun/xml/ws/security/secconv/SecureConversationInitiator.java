package com.sun.xml.ws.security.secconv;

import javax.xml.bind.JAXBElement;
import com.sun.xml.ws.api.message.Packet;

public interface SecureConversationInitiator {
     public JAXBElement startSecureConversation(Packet packet) throws WSSecureConversationException;
}
