package gov.va.vinci.leo.exceptions;

public class LeoClientTestException extends LeoClientException {

    /**
     *
     */
    private static final long serialVersionUID = 6645974291691218180L;

    public LeoClientTestException(final String msg, final Throwable reason) {
        super(msg, reason);
    }

    public LeoClientTestException(final String msg) {
        super(msg);
    }
}
