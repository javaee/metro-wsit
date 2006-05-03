package com.sun.xml.ws.rm;

/**
 * Subclass of <code>RMException</code> thrown from errors resulting
 *  when a response to create sequence request cannot be satisfied
 * @author Bhakti Mehta
 *
 */
public class CreateSequenceException extends RMException {

    private  String sequenceId;
    /**
     */
    public CreateSequenceException() {
        super();
    }

    public CreateSequenceException(String message) {
        super(message);
    }

    public CreateSequenceException(String message,String id){
        super(message);
        this.sequenceId = id;
    }

     public CreateSequenceException(Throwable e) {
         super(e);
    }

    public String getSequenceId() {
        return sequenceId;
    }
}
