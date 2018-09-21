package gov.va.vinci.leo.exceptions;

public class LeoClientInitializationException extends Exception {

    static final long serialVersionUID = 12312455;

    public LeoClientInitializationException(final String msg) {
        super(msg);
    }

    public LeoClientInitializationException(final String msg, final Throwable reason) {
        super(msg, reason);
    }
}
