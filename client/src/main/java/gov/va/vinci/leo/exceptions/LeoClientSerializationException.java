package gov.va.vinci.leo.exceptions;

public class LeoClientSerializationException extends Exception {

    static final long serialVersionUID = 12312455;

    public LeoClientSerializationException(final String msg) {
        super(msg);
    }

    public LeoClientSerializationException(final String msg, final Throwable reason) {
        super(msg, reason);
    }
}
