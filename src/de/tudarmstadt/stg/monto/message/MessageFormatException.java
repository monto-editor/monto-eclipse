package de.tudarmstadt.stg.monto.message;

public class MessageFormatException extends Exception {

	private static final long serialVersionUID = -5674396133142118737L;

	public MessageFormatException(String message) {
		super(message);
	}
	
	public MessageFormatException(String message, Throwable cause) {
		super(message, cause);
	}
	

	public MessageFormatException(Throwable cause) {
		super(cause);
	}
}
