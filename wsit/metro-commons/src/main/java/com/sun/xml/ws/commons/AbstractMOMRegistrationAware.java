package com.sun.xml.ws.commons;

/**
 * Default implementation of {@link MOMRegistrationAware}.
 */
public abstract class AbstractMOMRegistrationAware implements MOMRegistrationAware {

    private boolean atMOM = false;

    public boolean isRegisteredAtMOM() {
        return this.atMOM;
    }

    public void setRegisteredAtMOM(boolean atMOM) {
        this.atMOM = atMOM;
    }

}
