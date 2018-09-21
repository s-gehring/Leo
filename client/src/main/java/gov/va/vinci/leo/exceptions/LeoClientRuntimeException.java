package gov.va.vinci.leo.exceptions;

public class LeoClientRuntimeException extends LeoRuntimeException {

    static final long serialVersionUID = 12312455;

    public LeoClientRuntimeException(final String msg) {
        super(msg);
    }

    public LeoClientRuntimeException(final String msg, final Throwable reason) {
        super(msg, reason);
    }
}
