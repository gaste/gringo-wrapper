/*
 *  Copyright 2015 Philip Gasteiger
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
	
	public String performPostprocessing(String groundedProgram, String debugAtomPrefix, String factAtom, List<String> fixedModel) {
		String[] splitted = groundedProgram.split("(?m)^0$", 2);
		BufferedReader rulesReader = new BufferedReader(new StringReader(splitted[0]));
		BufferedReader symbolsReader = new BufferedReader(new StringReader(splitted[1]));
		StringBuilder rules = new StringBuilder(splitted[0].length());
		StringBuilder symbols = new StringBuilder(splitted[1].length());
		StringBuilder debugChoiceRule = new StringBuilder();
		FixedModelConstraintBuilder fixedModelConstraint = new FixedModelConstraintBuilder();
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
					
					if (null != fixedModel) {
						// add to the fixed model constraint
						int separatorIdx = line.indexOf(' ');
						
						if (-1 != separatorIdx) {
							String symbol = line.substring(0, separatorIdx);
							String atom = line.substring(separatorIdx + 1);
							
							if (!atom.startsWith(debugAtomPrefix) && !atom.startsWith("fixModel")) {
								if (fixedModel.contains(atom))
									fixedModelConstraint.addAtomInModel(symbol);
								else
									fixedModelConstraint.addAtomNotInModel(symbol);
							}
						}
					}
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
			
			// append the fix model constraint (if present)
			if (null != fixedModel) {
				rules.append(fixedModelConstraint.toString());
				rules.append('\n');
			}
			
			// append the end-of-rules-block marker and the symbols table
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
	
	class FixedModelConstraintBuilder {
		private int numPositive;
		private int numNegative;
		private final StringBuilder positiveAtoms;
		private final StringBuilder negativeAtoms;
		
		public FixedModelConstraintBuilder() {
			positiveAtoms = new StringBuilder();
			negativeAtoms = new StringBuilder();
			numPositive = 0;
			numNegative = 0;
		}
		
		public void addAtomInModel(String symbol) {
			// in model, thus use 'not symbol' in the constraint
			if (numNegative > 0) {
				negativeAtoms.append(' ');
			}
			
			numNegative ++;
			negativeAtoms.append(symbol);
		}
		
		public void addAtomNotInModel(String symbol) {
			// not in model, thuse use 'symbol' in the constraint
			if (numPositive > 0) {
				positiveAtoms.append(' ');
			}
			
			numPositive ++;
			positiveAtoms.append(symbol);
		}
		
		@Override
		public String toString() {
			StringBuilder constraint = new StringBuilder();
			constraint.append("1 1 ");
			constraint.append(numNegative + numPositive);
			constraint.append(' ');
			constraint.append(numNegative);
			
			if (numNegative > 0) {
				constraint.append(' ');
				constraint.append(negativeAtoms);
			}
			
			if (numPositive > 0) {
				constraint.append(' ');
				constraint.append(positiveAtoms);
			}
			
			return constraint.toString();
		}
	}
}
