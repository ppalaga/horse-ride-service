package org.palaga.demo.ride.service;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class SchedulingException extends Exception {

    /**  */
    private static final long serialVersionUID = 7392356100309651180L;

    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(Throwable cause) {
        super(cause);
    }

}
