package gov.va.vinci.leo.exceptions;

import gov.va.vinci.leo.exceptions.LeoClientRuntimeException;

public class LeoBrokerConfigurationException extends LeoClientRuntimeException {

    private static final long serialVersionUID = 5461168743271958997L;

    public LeoBrokerConfigurationException(final String msg, final Throwable reason) {
        super(msg, reason);

    }

    public LeoBrokerConfigurationException(final String msg) {
        super(msg);

    }
}
