package at.aau.preprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Preprocessor that replaces each fact by a rule.
 * 
 * @author Philip Gasteiger
 *
 */
public class Preprocessor {
	/**
	 * Regular expression that matches a fact. A fact is composed of
	 *  (*)  the beginning of line or '.' of the last fact/rule
	 *  (*)  an arbitrary sequence of spaces, letters, digits, brackets,
	 *       colons, underscores, dashes, and two dots
	 *  (*)  an '.' that delimits the fact.
	 */	
	private static final String FACT_REGEX 
			// positive lookbehind for a single '.' or the beginning of a line
			= "((?<=((?<!\\.)\\.(?!\\.)))|^)"
			// arbitrary sequence of spaces, letters, digits, brackets, colons, underscores, dashes and two dots
			+ "(([ a-zA-Z0-9(),_\\-]|(\\.\\.))*)"
			// positive lookbehind for a single '.' that delimits the fact
			+ "(?=((?<!\\.)\\.(?!\\.)))";
	
	/** Group that matches the fact */
	private static final String FACT_REGEX_MATCHING_GROUP = "$3";
	
	/** Regular expression that matches a variable */
	private static final String VARIABLE_REGEX 
			= "(?<=[(,; ])" // positive lookbehind for a starting delimiter of the variable
			+ "(_*[A-Z][A-Za-z0-9]*)" // variable
			+ "(?=[),; ])"; // positive lookahead for a ending delimiter of the variable
	
	private static final Pattern FACT_PATTERN = Pattern.compile(FACT_REGEX, Pattern.MULTILINE);
	
	private static final Pattern VARIABLE_PATTERN = Pattern.compile(VARIABLE_REGEX);

	/**
	 * Takes the given logic program and returns a new literal that is not
	 * present in the logic program.
	 * 
	 * @param logicProgram
	 *            The logic program for which the fact literal shall be found.
	 * @return A new literal.
	 */
	public String getFactLiteral(String logicProgram) {
		String factLiteral = "";

		do {
			// generate a candidate fact literal
			factLiteral = "_fl" + UUID.randomUUID().toString().replaceAll("-", "");
		} while (logicProgram.contains(factLiteral));

		return factLiteral;
	}

	/**
	 * Take the given logic program and fact literal and return the logic
	 * program, where each fact 'f.' is replaced by the rule 'f :- factLiteral'.
	 * Also the rule 'factLiteral | -factLiteral' is added to the logic program.
	 * 
	 * @param logicProgram
	 *            The logic program to be modified.
	 * @param factLiteral
	 *            The fact literal to be added.
	 * @return The logic program where facts are replaced by rules.
	 */
	public String addFactLiteral(String logicProgram, String factLiteral) {
		// replace each fact 'f.' with the rule 'f :- factLiteral.'
		logicProgram = FACT_PATTERN.matcher(logicProgram).replaceAll(FACT_REGEX_MATCHING_GROUP + " :- " + factLiteral);

		// add a choice rule for the fact literal to logic program
		logicProgram += "\n" + factLiteral + " | -" + factLiteral + ".";

		// return the modified logic program
		return logicProgram;
	}
	
	/**
	 * Takes the given logic program and debug constant prefix as input and
	 * returns the logic program, where the debugConstantPrefix concatenated
	 * with a number is added to the body of each non-fact rule. Furthermore, a
	 * single choice rule containing all debug constants is added at the bottom
	 * of the program to avoid warnings from the grounder.
	 * 
	 * @param logicProgram
	 *            The logic program to be modified.
	 * @param debugConstantPrefix
	 *            The prefix for the debug constants to be added.
	 * @return The modified logic program.
	 */
	public String addDebugConstants(String logicProgram,
			String debugConstantPrefix) {
		StringBuilder preprocessedProgram = new StringBuilder(logicProgram.length());
		StringBuilder debugChoiceRules = new StringBuilder();
		int debugConstantNum = 1;
		
		// split the program into rules. The regex matches only a single '.'
		for (String rule : logicProgram.split("(?=((?<!\\.)\\.(?!\\.)))")) {
			if (rule.contains(":-")) {
				// rule, identified by ':-', thus add ', _debug#' to the rule
				StringBuilder debugConstant = new StringBuilder();
				debugConstant.append(debugConstantPrefix);
				debugConstant.append(debugConstantNum);
				
				List<String> variables = getVariables(rule.split(":-")[1]);
				if (variables.size() > 0) {
					debugConstant.append("(");
					debugConstant.append(variables.stream().collect(Collectors.joining(", ")));
					debugConstant.append(")");
				}
				
				preprocessedProgram.append(rule);
				preprocessedProgram.append(", ");
				preprocessedProgram.append(debugConstant);
				
				debugChoiceRules.append("0{");
				debugChoiceRules.append(debugConstant);
				debugChoiceRules.append("}1");
				
				if (variables.size() > 0) {
					debugChoiceRules.append(" :- ");
					debugChoiceRules.append(rule.split(":-")[1]);
				}
				
				debugChoiceRules.append(".\n");
				
				debugConstantNum ++;
			} else if (rule.contains("|") || (rule.contains("{") && rule.contains("}"))) {
				// disjunction or choice rule, thus add ' :- _debug#' to the rule
				preprocessedProgram.append(rule);
				preprocessedProgram.append(" :- ");
				preprocessedProgram.append(debugConstantPrefix);
				preprocessedProgram.append(debugConstantNum);
				
				debugChoiceRules.append("0{");
				debugChoiceRules.append(debugConstantPrefix);
				debugChoiceRules.append(debugConstantNum);
				debugChoiceRules.append("}1.\n");
				
				debugConstantNum ++;
			} else {
				// fact, thus do not alter it
				preprocessedProgram.append(rule);
			}
		}
		
		// add choice rule for debug constants
		if (debugConstantNum > 1) {
			preprocessedProgram.append("\n");
			preprocessedProgram.append(debugChoiceRules);
		}
		
		return preprocessedProgram.toString();
	}
	
	/**
	 * Get all variables inside the given rule body.
	 * 
	 * @param ruleBody
	 *            The body of the rule.
	 * @return A list that contains all variables without any duplicates
	 */
	private List<String> getVariables(String ruleBody) {
		List<String> variables = new ArrayList<String>();
		Matcher variableMatcher = VARIABLE_PATTERN.matcher(ruleBody);
		
		while (variableMatcher.find()) {
			String currentVariable = variableMatcher.group();
			if (!variables.contains(currentVariable)) {
				variables.add(currentVariable);
			}
		}
		
		return variables;
	}
}
