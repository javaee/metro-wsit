/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.xml.wss.impl.policy.mls;

/**
 *
 * @author suresh
 */
/**
 * Indicates that a Binding should have its uid re-read when the actual signature is created
 * because the id is not known at startup time, for example because the binding
 * refers to a token which must be generated externally.
 *
 */
public interface LazyKeyBinding {

    public String getRealId();

    public void setRealId(String realId);

    public String getSTRID();
}
