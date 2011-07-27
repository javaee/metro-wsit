/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xml.ws.api.metro.config;

import javax.xml.namespace.QName;

/**
 *
 * @author fr159072
 */
public interface ElementInfo {

    public String getBindingID();

    public QName getPortName();

    public QName getBindingName();

    public QName getServiceName();

    public QName getOperationName();

    public QName getFaultMessageName();

}
