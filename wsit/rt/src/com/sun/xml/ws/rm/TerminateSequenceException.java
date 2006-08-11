package com.sun.xml.ws.rm;

/**
 * Subclass of <code>RMException</code> thrown from errors resulting
 *  because the endpoint has encountered an unrecoverable condition or
 * detected a violation of the protocol and as a result has chosen to
 * terminate the sequence
 * @author Bhakti Mehta
 *
 */
public class TerminateSequenceException extends RMException {

    private  String sequenceId;
    /**
     */
    public TerminateSequenceException() {
        super();
    }

    public TerminateSequenceException(String message) {
        super(message);
    }

    public TerminateSequenceException(String message,String id){
        super(message);
        this.sequenceId = id;
    }

    public TerminateSequenceException (String s, com.sun.xml.ws.api.message.Message message) {
        super (s,message);
    }

     public TerminateSequenceException(Throwable e) {
         super(e);
    }

    public String getSequenceId() {
        return sequenceId;
    }
}
