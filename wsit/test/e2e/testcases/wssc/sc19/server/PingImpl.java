/*
 * Copyright (c) 2006 Sun Microsystems, Inc.
 * All rights reserved.
 */
package wssc.sc19.server;
import javax.xml.ws.Holder;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;

@javax.jws.WebService (endpointInterface="wssc.sc19.server.IPingService")
@javax.xml.ws.BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
public class PingImpl implements IPingService {
    
   public void ping( Holder<String> scenario,
         Holder<String> origin,
         Holder<String> text){
        System.out.println("The message is here : " + scenario.value + " " +origin.value + " " + text.value);

    }                    

public static class PlainTextPasswordValidator implements PasswordValidationCallback.PasswordValidator {

        public boolean validate(PasswordValidationCallback.Request request)
            throws PasswordValidationCallback.PasswordValidationException {
            System.out.println("Using configured PlainTextPasswordValidator...");

            PasswordValidationCallback.PlainTextPasswordRequest plainTextRequest =
                (PasswordValidationCallback.PlainTextPasswordRequest) request;
            if ("Alice".equals(plainTextRequest.getUsername()) &&
                "ecilA".equals(plainTextRequest.getPassword())) {
                return true;
            }
            return false;
        }
}
}
