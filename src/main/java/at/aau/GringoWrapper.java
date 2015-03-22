package at.aau;

import at.aau.grounder.Grounder;
import at.aau.grounder.GrounderGringoImpl;
import at.aau.grounder.GroundingException;
import at.aau.postprocessing.PostprocessingException;
import at.aau.postprocessing.Postprocessor;
import at.aau.preprocessing.Preprocessor;

/**
 * The wrapper takes a logic program and returns the grounded logic program
 * without any optimization.
 * 
 * @author Philip Gasteiger
 *
 */
public class GringoWrapper {
	/** The grounder used to ground the program. */
	private Grounder grounder;

	/** The preprocessor that replaces all facts with a rule. */
	private Preprocessor preprocessor;

	/** The postprocessor that replaces the fact-rules with the original facts */
	private Postprocessor postprocessor;

	public GringoWrapper(String grounderCommand) {
		grounder = new GrounderGringoImpl(grounderCommand);
		preprocessor = new Preprocessor();
		postprocessor = new Postprocessor();
	}

	/**
	 * Ground the given logic program without performing any optimizations.
	 * 
	 * @param logicProgram
	 *            The logic program to ground.
	 * @return The grounded logic program.
	 * @throws GroundingException
	 *             If the grounder was not found or the program could not be
	 *             grounded.
	 * @throws PostprocessingException
	 *             If the postprocessing of the grounded program failed.
	 */
	public String ground(String logicProgram) throws GroundingException, PostprocessingException {
		String factLiteral = preprocessor.getFactLiteral(logicProgram);
		String preprocessedLp = preprocessor.addFactLiteral(logicProgram, factLiteral);
		String groundedPreprocessedLp = grounder.ground(preprocessedLp);
		String groundedLp = postprocessor.removeFactLiteral(groundedPreprocessedLp, factLiteral);

		return groundedLp;
	}

}
