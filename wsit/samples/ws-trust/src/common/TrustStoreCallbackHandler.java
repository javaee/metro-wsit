package common;

import com.sun.xml.wss.impl.callback.KeyStoreCallback;

import java.io.FileInputStream;
import java.io.IOException;
import javax.security.auth.callback.Callback;

import java.security.KeyStore;
import java.util.Map;
import java.util.Set;
import javax.security.auth.callback.UnsupportedCallbackException;

public class TrustStoreCallbackHandler implements javax.security.auth.callback.CallbackHandler {
    private String keyStoreURL;
    private String keyStorePassword;
    private String keyStoreType;

    private KeyStore keyStore;
    
    public void handle(Callback[] callbacks)throws IOException, UnsupportedCallbackException{
        for (Callback callback : callbacks){
            if (callback instanceof KeyStoreCallback){
                ((KeyStoreCallback)callback).setKeystore(keyStore);
                Map map = ((KeyStoreCallback)callback).getRuntimeProperties();
                Set keys = map.keySet();
                System.out.println("TrustStoreCallbackHandler Print out keys");
                for (Object key : keys){
                    System.out.println("key="+key);
                    System.out.println("value="+map.get(key));
                }
            }
        }
    }

    public TrustStoreCallbackHandler() {
        try {
            this.keyStoreURL = "C:/metro/apache-tomcat-5.5.16/xws-security/etc/sts-truststore.jks";
            this.keyStoreType = "JKS";
            this.keyStorePassword = "changeit";

            initKeyStore();
        }catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

     private void initKeyStore() throws IOException {
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(new FileInputStream(keyStoreURL), keyStorePassword.toCharArray());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
