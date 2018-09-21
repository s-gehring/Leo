package gov.va.vinci.leo.exceptions;

public class LeoClientUserInterruptionException extends RuntimeException {

    static final long serialVersionUID = 12312455;

    public LeoClientUserInterruptionException(final String msg) {
        super(msg);
    }

    public LeoClientUserInterruptionException(final String msg, final Throwable reason) {
        super(msg, reason);
    }
}
