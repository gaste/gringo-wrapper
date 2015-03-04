package at.aau.grounder;

/**
 * Exception that gets thrown when the grounding of a logic program fails.
 * 
 * @author Philip Gasteiger
 *
 */
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
