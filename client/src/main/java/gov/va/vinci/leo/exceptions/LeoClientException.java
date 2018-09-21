package gov.va.vinci.leo.exceptions;

public class LeoClientException extends LeoException {

    static final long serialVersionUID = 12312455;

    public LeoClientException(final String msg) {
        super(msg);
    }

    public LeoClientException(final String msg, final Throwable reason) {
        super(msg, reason);
    }
}
