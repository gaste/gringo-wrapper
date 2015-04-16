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
	
	private final String DEBUG_CONSTANT_PREFIX;
	
	private final boolean rewriteOnly;

	public GringoWrapper(String grounderCommand, String grounderOptions, String debugConstantPrefix, boolean rewriteOnly) {
		grounder = new GrounderGringoImpl(grounderCommand, grounderOptions);
		preprocessor = new Preprocessor();
		postprocessor = new Postprocessor();
		this.DEBUG_CONSTANT_PREFIX = debugConstantPrefix;
		this.rewriteOnly = rewriteOnly;
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
	public String ground(String logicProgram, boolean addDebugConstants)
			throws GroundingException, PostprocessingException {
		String factLiteral = preprocessor.getFactLiteral(logicProgram);
		String preprocessedLp1 = logicProgram;
		
		if (addDebugConstants) {
			preprocessedLp1 = preprocessor.addDebugConstants(logicProgram, DEBUG_CONSTANT_PREFIX);			
		}
		
		if (rewriteOnly) {
			return preprocessedLp1;
		}
		
		String preprocessedLp2 = preprocessor.addFactLiteral(preprocessedLp1, factLiteral);
		String groundedPreprocessedLp = grounder.ground(preprocessedLp2);
		String groundedLpNoFactliteral = postprocessor.removeFactLiteral(groundedPreprocessedLp, factLiteral);
		String groundedLp = postprocessor.removeDebugChoiceRules(groundedLpNoFactliteral, DEBUG_CONSTANT_PREFIX);

		return groundedLp;
	}
}
