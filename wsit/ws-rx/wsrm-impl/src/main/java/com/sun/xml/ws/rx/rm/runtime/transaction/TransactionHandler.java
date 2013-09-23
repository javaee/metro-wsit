package com.sun.xml.ws.rx.rm.runtime.transaction;

public interface TransactionHandler {
    void begin(int txTimeout) throws TransactionException;
    void commit() throws TransactionException;
    void rollback() throws TransactionException;
    void setRollbackOnly() throws TransactionException;
    boolean userTransactionAvailable() throws TransactionException;
    boolean isActive() throws TransactionException;
    boolean isMarkedForRollback() throws TransactionException;
    boolean canBegin() throws TransactionException;
    int getStatus() throws TransactionException;
    String getStatusAsString() throws TransactionException;
}
