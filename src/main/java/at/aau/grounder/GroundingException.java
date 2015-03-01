package at.aau.grounder;

public class GroundingException extends Exception {
	private static final long serialVersionUID = 1L;

	public GroundingException(String message, Throwable cause) {
		super(message, cause);
	}

	public GroundingException(String message) {
		super(message);
	}

	public GroundingException(Throwable cause) {
		super(cause);
	}
}
