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
	 * <ul>
	 *   <li>the fact literal from the symbol table,</li>
	 *   <li>the negated fact literal from the symbol table,</li>
	 *   <li>the constraint <code>:- factLiteral, -factLiteral.</code>, and</li>
	 *   <li>the disjunctive rule <code>factLiteral | -factLiteral.</code> from
	 *   the grounded program. It also replaces each rule of the form
	 *   <code>f :- factLiteral.</code> with the fact <code>f.</code></li>
	 * </ul>
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
		// try both orders 'factLiteral | -factLiteral' and '-factLiteral | factLiteral'
		groundedProgram = groundedProgram.replace("8 2 " + factLiteralSymbol + " " + factLiteralSymbolNegated + " 0 0\n", ""); 
		groundedProgram = groundedProgram.replace("8 2 " + factLiteralSymbolNegated + " " + factLiteralSymbol + " 0 0\n", "");
		
		// pattern for matching fact literal rules 'f :- factLiteral'
		Pattern factLiteralRules = Pattern.compile("^1 (\\d+) 1 0 " + factLiteralSymbol + "$", Pattern.MULTILINE);
		
		// replace all fact literal rules with the fact
		groundedProgram = factLiteralRules.matcher(groundedProgram).replaceAll("1 $1 0 0");
		
		return groundedProgram;
	}
	
	/**
	 * Removes the choice rules of the _debug constants from the grounded
	 * program.
	 * 
	 * @param groundedProgram
	 *            The program from which the choice rules should be removed.
	 * @param debugConstantPrefix
	 *            The prefix of the debug constants.
	 * @return The postprocessed grounded programm.
	 */
	public String removeDebugChoiceRules(String groundedProgram,
			String debugConstantPrefix) {
		// patterns that match the debug constants in the symbol table
		Matcher debugConstantSymbolMatcher = Pattern.compile("^(\\d+) " + debugConstantPrefix + "[0-9]*(\\([ _,a-zA-Z0-9]*\\))?\n", Pattern.MULTILINE).matcher(groundedProgram);
		
		while(debugConstantSymbolMatcher.find()) {
			String debugConstantSymbol = debugConstantSymbolMatcher.group(1);
			Matcher debugChoiceRuleBodyMatcher = Pattern.compile("^3 1 " + debugConstantSymbol + " 1 0 (\\d+)\n", Pattern.MULTILINE).matcher(groundedProgram);
			
			if (debugChoiceRuleBodyMatcher.find()) {
				// found a body for the debug choice rule, so delete it and the rule itself
				String debugChoiceRuleBodySymbol = debugChoiceRuleBodyMatcher.group(1);
				groundedProgram = debugChoiceRuleBodyMatcher.replaceAll("");
				groundedProgram = groundedProgram.replaceAll("(?m)^1 " + debugChoiceRuleBodySymbol + "( \\d+)*\n", "");
			} else {
				// no body for the debug choice rule, so delete the choice rule itself only
				groundedProgram = groundedProgram.replaceAll("(?m)^3 1 " + debugConstantSymbol + " 0 0\n" , "");
			}
		}
		
		return groundedProgram;
	}
}
