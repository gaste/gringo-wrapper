package at.aau.postprocessing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.aau.Rule;

/**
 * Postprocessor that replaces each artificial fact-rule with the fact and
 * removes the fact literal from the symbol table.
 * 
 * @author Philip Gasteiger
 *
 */
public class Postprocessor {
	private Matcher getDebugConstantSymbolMatcher(String groundedProgram,
			String debugConstantPrefix) {
		Matcher debugConstantSymbolMatcher = Pattern.compile("^(\\d+) " + debugConstantPrefix + "[0-9]*(\\([ _,a-zA-Z0-9]*\\))?\n", Pattern.MULTILINE).matcher(groundedProgram);
		return debugConstantSymbolMatcher;
	}
	
	public String performPostprocessing(String groundedProgram, String debugAtomPrefix, String factAtom) {
		String[] splitted = groundedProgram.split("(?m)^0$", 2);
		BufferedReader rulesReader = new BufferedReader(new StringReader(splitted[0]));
		BufferedReader symbolsReader = new BufferedReader(new StringReader(splitted[1]));
		StringBuilder rules = new StringBuilder(splitted[0].length());
		StringBuilder symbols = new StringBuilder(splitted[1].length());
		StringBuilder debugChoiceRule = new StringBuilder();
		String line = null;
		Set<String> debugSymbols = new HashSet<String>();
		boolean factAtomFound = false;
		boolean factAtomNegFound = false;
		String factAtomSymbol = null;
		String factAtomNegSymbol = null;
		int numDebugAtoms = 0;
		
		int idx = -1;
		
		// process the symbols
		try {
			while ((line = symbolsReader.readLine()) != null) {
				if ((idx = line.indexOf(debugAtomPrefix)) > -1) {
					// n _debug#(...)
					String debugSymbol = line.substring(0, idx - 1);
							
					debugSymbols.add(debugSymbol);
					
					debugChoiceRule.append(' ');
					debugChoiceRule.append(debugSymbol);
					
					numDebugAtoms ++;
				}
				
				if ((!factAtomFound || !factAtomNegFound) && (idx = line.indexOf(factAtom)) > -1) {
					// fact atom symbol entry
					if(line.charAt(idx - 1) == '-') {
						// n -_flXXX
						factAtomNegSymbol = line.substring(0, idx - 2);
						factAtomNegFound = true;
					} else {
						// n _flXXX
						factAtomSymbol = line.substring(0, idx - 1);
						factAtomFound = true;
					}
				} else {
					// other entry, append it
					symbols.append(line);
					symbols.append('\n');
				}
			}
		
			// build the debug choice rule
			if (numDebugAtoms > 0) {
				debugChoiceRule.insert(0, "3 " + numDebugAtoms);
				debugChoiceRule.append(" 0 0\n");
			}
			
			// constraint ':- _fl, -_fl'
			String factAtomConstraint = "1 1 2 0 " + factAtomSymbol + " " + factAtomNegSymbol;
			String factAtomDisjunction1 = "8 2 " + factAtomSymbol + " " + factAtomNegSymbol + " 0 0";
			String factAtomDisjunction2 = "8 2 " + factAtomNegSymbol + " " + factAtomSymbol + " 0 0";
			String factAtomBody = "1 0 " + factAtomSymbol;
			
			boolean factAtomConstraintFound = false;
			boolean factAtomDisjunctionFound = false;
			
			// process the rules
			while ((line = rulesReader.readLine()) != null) {
				if (!factAtomConstraintFound && line.equals(factAtomConstraint)) {
					factAtomConstraintFound = true;
				} else if (!factAtomDisjunctionFound
						&& (line.equals(factAtomDisjunction1) || line
								.equals(factAtomDisjunction2))) {
					factAtomDisjunctionFound = true;
				} else if (line.charAt(0) == '1') {
					// normal rule, check if it has a debug symbol in the head
					idx = line.indexOf(' ', 2);
					String headSymbol = line.substring(2, idx);
					if (!debugSymbols.contains(headSymbol)) {
						// check if it is of the form '1 fact 1 0 _fl'
						if ((idx = line.indexOf(factAtomBody)) > -1 && line.endsWith(factAtomBody)) {
							// replace fact :- _fl with fact.
							rules.append(line.substring(0, idx));
							rules.append("0 0\n");
						} else {
							// regular normal rule
							rules.append(line);
							rules.append('\n');
						}
					}
				} else {
					rules.append(line);
					rules.append('\n');
				}
			}
			
			// append the debug choice rule
			rules.append(debugChoiceRule);
			rules.append('0');
			rules.append(symbols);
			
			return rules.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets a list of non-ground rules that where removed by the grounder.
	 * 
	 * @param groundedProgram
	 *            The grounded program.
	 * @param debugRuleMap
	 *            The mapping of the _debug constants to the rules
	 * @return The list of removed rules.
	 */
	public List<String> getRemovedRules(String groundedProgram, Map<String, Rule> debugRuleMap) {
		List<String> removedRules = new ArrayList<String>();
		String groundedRules = groundedProgram.split("(?m)^0$", 2)[0];
		
		for(String debugConstant : debugRuleMap.keySet()) {
			boolean foundRule = false;
			Matcher debugConstantSymbolMatcher = getDebugConstantSymbolMatcher(groundedProgram, debugConstant);
			
			// search for at least one ground rule
			while (debugConstantSymbolMatcher.find() && !foundRule) {
				String debugSymbol = debugConstantSymbolMatcher.group(1);
				
				if(groundedRules.contains(debugSymbol)) {
					foundRule = true;
				}
			}
			
			if(!foundRule) {
				removedRules.add(debugRuleMap.get(debugConstant).getRule());
			}
		}
		
		return removedRules;
	}
}
