package gov.va.vinci.leo.exceptions;

public class LeoUnintializedUimaAsException extends LeoValidationException {

    private static final long serialVersionUID = 5461168743271958997L;

    public LeoUnintializedUimaAsException(final String msg, final Throwable reason) {
        super(msg, reason);

    }

    public LeoUnintializedUimaAsException(final String msg) {
        super(msg);

    }
}
