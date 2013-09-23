package com.sun.xml.ws.rx.rm.runtime.transaction;

import com.oracle.webservices.api.message.BasePropertySet;

public class TransactionPropertySet extends BasePropertySet {
    public static final String TX_OWNED_PROPERTY = "com.sun.xml.ws.rx.rm.runtime.transaction.owned";

    //Do we own the TX? This would be set to true when we begin the TX.
    private boolean owned = false;

    @Property(TX_OWNED_PROPERTY)
    public boolean isTransactionOwned() {
        return owned;
    }

    public void setTransactionOwned(boolean flag) {
        owned = flag;
    }

    ////////////////////////////////////////////////////
    //
    // PropertySet boilerplate
    //

    private static final PropertyMap model;

    static {
        model = parse(TransactionPropertySet.class);
    }

    @Override
    protected PropertyMap getPropertyMap() {
        return model;
    }
}
