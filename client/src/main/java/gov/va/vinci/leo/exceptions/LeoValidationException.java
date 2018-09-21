package gov.va.vinci.leo.exceptions;

import gov.va.vinci.leo.exceptions.LeoClientException;

public class LeoValidationException extends LeoClientException {

    static final long serialVersionUID = 12312455;

    public LeoValidationException(final String msg) {
        super(msg);
    }

    public LeoValidationException(final String msg, final Throwable reason) {
        super(msg, reason);
    }
}
