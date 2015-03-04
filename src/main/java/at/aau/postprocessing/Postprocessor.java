package at.aau.postprocessing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Postprocessor that replaces each artificial fact-rule with the fact and
 * removes the fact literal from the symbol table.
 * 
 * @author Philip Gasteiger
 *
 */
public class Postprocessor {
	/**
	 * Remove the fact literal from the grounded program. This method removes
	 * (*) the fact literal from the symbol table, (*) the negated fact literal
	 * from the symbol table, (*) the constraint ':- factLiteral,
	 * -factLiteral.', and (*) the disjunctive rule 'factLiteral |
	 * -factLiteral.' from the grounded program. It also replaces each rule of
	 * the form 'f :- factLiteral.' with the fact 'f.'
	 * 
	 * @param groundedProgram
	 *            The grounded program to be postprocessed.
	 * @param factLiteral
	 *            The fact literal to be removed.
	 * @return The postprocessed grounded program.
	 * @throws PostprocessingException
	 *             If the fact literal is not found in the symbol table.
	 */
	public String removeFactLiteral(String groundedProgram, String factLiteral)
			throws PostprocessingException {
		// patterns that match the fact literal in the symbol table
		Pattern pFL = Pattern.compile("^(\\d+) " + factLiteral + "\n", Pattern.MULTILINE);
		Pattern pFLNegated = Pattern.compile("^(\\d+) -" + factLiteral + "\n", Pattern.MULTILINE);
		
		// matchers for the fact literal and the negated fact literal
		Matcher mFL = pFL.matcher(groundedProgram);
		Matcher mFLNegated = pFLNegated.matcher(groundedProgram);
		
		// find the fact literal in the symbol table
		if (!mFL.find()) {
			throw new PostprocessingException("The fact literal was not found in the symbol table");
		}
		
		// parse the fact literal from the symbol table
		String factLiteralSymbol = mFL.group(1);
		
		// find the negated fact literal in the symbol table
		if (!mFLNegated.find()) {
			throw new PostprocessingException("The negated fact literal was not found in the symbol table");
		}
		
		// parse the negated fact literal from the symbol table
		String factLiteralSymbolNegated = mFLNegated.group(1);
		
		// remove the fact literal from the symbol table
		groundedProgram = pFL.matcher(groundedProgram).replaceAll("");
		
		// remove the negated fact literal from the symbol table
		groundedProgram = pFLNegated.matcher(groundedProgram).replaceAll("");
		
		// remove the fact literal constraint ':- factLiteral, -factLiteral' from the set of rules
		groundedProgram = groundedProgram.replace("1 1 2 0 " + factLiteralSymbol + " " + factLiteralSymbolNegated + "\n", "");
		
		// remove the fact literal disjunction rule 'factLiteral | -factLiteral' from the set of rules
		groundedProgram = groundedProgram.replace("8 2 " + factLiteralSymbol + " " + factLiteralSymbolNegated + " 0 0\n", "");
		
		// pattern for matching fact literal rules 'f :- factLiteral'
		Pattern factLiteralRules = Pattern.compile("^1 (\\d+) 1 0 " + factLiteralSymbol + "$", Pattern.MULTILINE);
		
		// replace all fact literal rules with the fact
		groundedProgram = factLiteralRules.matcher(groundedProgram).replaceAll("1 $1 0 0");
		
		return groundedProgram;
	}
}
