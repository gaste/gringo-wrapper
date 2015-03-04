package at.aau.postprocessing;

/**
 * Exception that gets thrown when the postprocessing failes.
 * 
 * @author Philip Gasteiger
 *
 */
public class PostprocessingException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public PostprocessingException(String message) {
		super(message);
	}
}
