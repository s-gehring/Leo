package gov.va.vinci.leo.exceptions;

public class LeoException extends Exception {

    private static final long serialVersionUID = 5461168743271958997L;

    public LeoException(final String msg, final Throwable reason) {
        super(msg, reason);

    }

    public LeoException(final String msg) {
        super(msg);

    }
}
