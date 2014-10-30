package de.tudarmstadt.stg.monto.message;

public class VersionMessageParseException extends Exception {


	public VersionMessageParseException(Exception e) {
		super(e);
	}

	public VersionMessageParseException(String reason) {
		super(reason);
	}

	private static final long serialVersionUID = -9091435240771124544L;
}
