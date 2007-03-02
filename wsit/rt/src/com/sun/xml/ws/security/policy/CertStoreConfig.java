/*
 * CertStoreConfig.java
 *
 * Created on March 1, 2007, 3:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.policy;

/**
 *
 * @author Kumar Jayanti
 */
public interface CertStoreConfig {
    
    public String getCallbackHandlerClassName();
    public String getCertSelectorClassName();
    public String getCRLSelectorClassName();
    
}
