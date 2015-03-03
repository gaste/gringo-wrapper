package at.aau.preprocessing;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Preprocessor that replaces each fact by a rule.
 * 
 * @author Philip Gasteiger
 *
 */
public class Preprocessor {
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
		// Pattern that matches a fact. A fact is composed of
		//  (*)  the beginning of line or '.' of the last fact/rule
		//  (*)  an arbitrary sequence of spaces, letters, digits, brackets, 
		//       colons, underscores, dashes, and two dots
		//  (*)  an '.' that delimits the fact.
		Pattern p = Pattern.compile(
				"((?<=((?<!\\.)\\.(?!\\.)))|^)" // positive lookbehind for a single '.' or the beginning of a line
			  + "(([ a-zA-Z0-9(),_\\-]|(\\.\\.))*)" // arbitrary sequence of spaces, letters, digits, brackets, colons, underscores, dashes and two dots
			  + "(?=((?<!\\.)\\.(?!\\.)))", // positive lookbehind for a single '.' that delimits the fact
			  Pattern.MULTILINE); // '^' matches the beginning of each line

		// replace each fact 'f.' with the rule 'f :- factLiteral.'
		logicProgram = p.matcher(logicProgram).replaceAll("$3 :- " + factLiteral);

		// add a choice rule for the fact literal to logic program
		logicProgram += "\n" + factLiteral + " | -" + factLiteral + ".";

		// return the modified logic program
		return logicProgram;
	}
}
