package gov.va.vinci.leo.exceptions;

import gov.va.vinci.leo.exceptions.LeoClientRuntimeException;

public class LeoListenerException extends LeoClientRuntimeException {

    private static final long serialVersionUID = 5461168743271958997L;

    public LeoListenerException(final String msg, final Throwable reason) {
        super(msg, reason);

    }

    public LeoListenerException(final String msg) {
        super(msg);

    }
}
