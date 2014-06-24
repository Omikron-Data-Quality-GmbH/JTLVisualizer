package net.omikron.jtl.visualizer.exceptions;

public class JtlReaderException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;

	public JtlReaderException() {
		super();
	}

	public JtlReaderException(final String message) {
		super(message);
	}

	public JtlReaderException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public JtlReaderException(final Throwable cause) {
		super(cause);
	}
}
