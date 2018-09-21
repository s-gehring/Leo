package gov.va.vinci.leo.exceptions;

public class LeoRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 5461168743271958997L;

    public LeoRuntimeException(final String msg, final Throwable reason) {
        super(msg, reason);

    }

    public LeoRuntimeException(final String msg) {
        super(msg);

    }
}
