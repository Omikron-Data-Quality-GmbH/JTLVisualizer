package net.omikron.jtl.visualizer.exceptions;

public class JtlMinRequirementsException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;

	public JtlMinRequirementsException() {
		super();
	}

	public JtlMinRequirementsException(final String message) {
		super(message);
	}

	public JtlMinRequirementsException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public JtlMinRequirementsException(final Throwable cause) {
		super(cause);
	}
}
