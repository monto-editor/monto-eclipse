package de.tudarmstadt.stg.monto.message;

public class ProductMessageParseException extends Exception {

	public ProductMessageParseException(Exception e) {
		super(e);
	}

	public ProductMessageParseException(String reason) {
		super(reason);
	}

	private static final long serialVersionUID = -8652632901411933961L;

}
