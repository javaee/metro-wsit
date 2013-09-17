package com.sun.xml.ws.rx.rm.runtime.transaction;

import com.sun.xml.ws.rx.RxRuntimeException;

public class TransactionException extends RxRuntimeException {
    private static final long serialVersionUID = 1L;

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
