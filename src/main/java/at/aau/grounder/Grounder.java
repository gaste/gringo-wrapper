package at.aau.grounder;

/**
 * Represents a grounder that takes a logic program and grounds it.
 * 
 * @author Philip Gasteiger
 *
 */
public interface Grounder {
	/**
	 * Ground the given logic program.
	 * 
	 * @param logicProgram
	 *            The logic program to ground in the gringo input format.
	 * @throws GroundingException
	 *             Thrown when the grounding failed because of errors.
	 * @return The grounded logic program in the gringo output format.
	 */
	public String ground(String logicProgram) throws GroundingException;
}
