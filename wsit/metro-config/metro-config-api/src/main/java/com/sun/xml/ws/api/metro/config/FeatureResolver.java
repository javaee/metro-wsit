/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.xml.ws.api.metro.config;

import java.util.Collection;

/**
 *
 * @author fr159072
 */
public interface FeatureResolver {

    public Collection getServiceFeatureSet(ElementInfo elementInfo);

    public Collection getPortFeatureSet(ElementInfo elementInfo);
    
    public Collection getBindingFeatureSet(ElementInfo elementInfo);

    public Collection getOperationFeatureSet(ElementInfo elementInfo);

    public Collection getInputMessageFeatureSet(ElementInfo elementInfo);

    public Collection getOutputMessageFeatureSet(ElementInfo elementInfo);

    public Collection getFaultMessageFeatureSet(ElementInfo elementInfo);

}
