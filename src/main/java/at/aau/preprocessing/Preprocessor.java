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

package at.aau.preprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.aau.Rule;

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

	private static final String FIX_MODEL_REGEX
			= "((?<=((?<!\\.)\\.(?!\\.)))|^)" // single '.' or new line
			+ " *fixModel" // ' ', then 'fixModel'
			+ " *\\(" // ' ' followed by a '('
			+ "(?<MODEL>[ a-zA-Z0-9(),_\\-]*)" // content of the model
			+ "\\) *" // closing ')', then ' '
			+ "(?=((?<!\\.)\\.(?!\\.)))"; // single '.' that delimits the fixModel
	
	private static final String ASSERT_TRUE_REGEX
			= "((?<=((?<!\\.)\\.(?!\\.)))|^)" // single '.' or new line
			+ " *assertTrue" // ' ', then 'assertTrue'
			+ " *\\(" // ' ' followed by a '('
			+ "(?<ASSERTION>[ a-zA-Z0-9(),_\\-]*)" // content of the assertion
			+ "\\) *" // closing ')', then ' '
			+ "(?=((?<!\\.)\\.(?!\\.)))"; // single '.' that delimits the assertion
	
	private static final String ASSERT_FALSE_REGEX
			= "((?<=((?<!\\.)\\.(?!\\.)))|^)" // single '.' or new line
			+ " *assertFalse" // ' ', then 'assertFalse'
			+ " *\\(" // ' ' followed by a '('
			+ "(?<ASSERTION>[ a-zA-Z0-9(),_\\-]*)" // content of the assertion
			+ "\\) *" // closing ')', then ' '
			+ "(?=((?<!\\.)\\.(?!\\.)))"; // single '.' that delimits the assertion
	
	/** Group that matches the fact */
	private static final String FACT_REGEX_MATCHING_GROUP = "$3";
	
	/** Regular expression that matches a variable */
	private static final String VARIABLE_REGEX 
			= "(?<=[(,; ])" // positive lookbehind for a starting delimiter of the variable
			+ "(_*[A-Z][A-Za-z0-9]*)" // variable
			+ "(?=[),; ])"; // positive lookahead for a ending delimiter of the variable
	
	private static final Pattern FIX_MODEL_PATTERN = Pattern.compile(FIX_MODEL_REGEX, Pattern.MULTILINE);
	
	private static final Pattern ASSERT_TRUE_PATTERN = Pattern.compile(ASSERT_TRUE_REGEX, Pattern.MULTILINE);
	
	private static final Pattern ASSERT_FALSE_PATTERN = Pattern.compile(ASSERT_FALSE_REGEX, Pattern.MULTILINE);

	private static final Pattern FACT_PATTERN = Pattern.compile(FACT_REGEX, Pattern.MULTILINE);
	
	private static final Pattern VARIABLE_PATTERN = Pattern.compile(VARIABLE_REGEX);
	
	private static final Pattern COMMENT_PATTERN = Pattern.compile(" *%.*$", Pattern.MULTILINE);
	
	private static final Pattern AGGREGATE_PATTERN = Pattern.compile("[^\\{\\},]*\\{[^\\{\\}]*?\\}[^\\{\\},]*");
		
	public String removeComments(String logicProgram) {
		return COMMENT_PATTERN.matcher(logicProgram).replaceAll("");
	}
	
	/**
	 * Takes the given logic program and converts assertions:
	 * <ul>
	 * <li><code>assertTrue(a).</code> to <code>:- not a.</code></li>
	 * <li><code>assertFalse(a).</code> to <code>:- a.</code></li>
	 * </ul>
	 * 
	 * @param logicProgram
	 *            The logic program to rewrite.
	 * @return The rewritten logic program.
	 */
	public String rewriteAssertions(String logicProgram) {
		logicProgram = ASSERT_TRUE_PATTERN.matcher(logicProgram).replaceAll(":- not ${ASSERTION}");
		logicProgram = ASSERT_FALSE_PATTERN.matcher(logicProgram).replaceAll(":- ${ASSERTION}");
				
		return logicProgram;
	}
	
	/**
	 * Takes the logic program and searches for a 'fixModel' command.
	 * @param logicProgram The logic program.
	 * @return A list of atoms inside the fixModel command or <code>null</code>,
	 *         if no fixModel command was found.
	 */
	//TODO implement functionality for multi-line comments detection
	public List<String> getFixedModel(String logicProgram) {
		Matcher matcher = FIX_MODEL_PATTERN.matcher(logicProgram);
		
		if (!matcher.find())
			return null;
		
		String modelTerm = matcher.group("MODEL");

		// parse the atoms inside the modelTerm
		List<String> model = new ArrayList<String>();
		StringBuilder atomBuilder = new StringBuilder();
		int openParens = 0;
		
		for (char c : modelTerm.toCharArray()) {
			if ('(' == c) {
				openParens ++;
				atomBuilder.append('(');
			} else if (')' == c) {
				openParens --;
				atomBuilder.append(')');
			} else if (',' == c && openParens == 0) {
				model.add(atomBuilder.toString());
				atomBuilder.setLength(0);
			} else {
				atomBuilder.append(c);
			}
		}
		
		if (atomBuilder.length() > 0) {
			model.add(atomBuilder.toString());
		}
		
		return model;
	}

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
	 * @param debugAtomRuleMap
	 *            Gets filled with mappings { _debug# -> rule | rule is a non
	 *            fact rule}.
	 * @return The modified logic program.
	 */
	public String addDebugConstants(String logicProgram,
			String debugConstantPrefix, Map<String, Rule> debugAtomRuleMap) {
		StringBuilder preprocessedProgram = new StringBuilder(logicProgram.length());
		StringBuilder debugRules = new StringBuilder();
		int debugConstantNum = 1;
		
		Pattern aggregateTerm1 = Pattern.compile(AGGREGATE_PATTERN.pattern() + ",");
		Pattern aggregateTerm2 = Pattern.compile("," + AGGREGATE_PATTERN.pattern() + "(?!,)");
		
		// split the program into rules. The regex matches only a single '.'
		for (String rule : logicProgram.split("(?<!\\.)\\.(?!\\.)")) {
			if (rule.contains(":-")) {
				// rule, identified by ':-', thus add ', _debug#' to the rule
				StringBuilder debugConstant = new StringBuilder();
				debugConstant.append(debugConstantPrefix);
				debugConstant.append(debugConstantNum);
				
				List<String> variables = getVariables(rule.split(":-")[1]);

				debugAtomRuleMap.put(debugConstantPrefix + debugConstantNum, new Rule(rule.replace("\n", "").trim() + ".", variables));
				
				if (variables.size() > 0) {
					debugConstant.append("(");
					debugConstant.append(variables.get(0));
					for (int i = 1; i < variables.size(); i ++) {
						debugConstant.append(", ");
						debugConstant.append(variables.get(i));
					}
					debugConstant.append(")");
				}
				
				preprocessedProgram.append(rule);
				preprocessedProgram.append(", ");
				preprocessedProgram.append(debugConstant);
				preprocessedProgram.append(".");
				
				debugRules.append(debugConstant);
				
				if (variables.size() > 0) {
					debugRules.append(" :- ");
					String r = rule.split(":-")[1];
					r=aggregateTerm1.matcher(r).replaceAll("");
					r=aggregateTerm2.matcher(r).replaceAll("");
					r=AGGREGATE_PATTERN.matcher(r).replaceAll("");
				
					debugRules.append(r);
				}
				
				debugRules.append(".\n");
				
				debugConstantNum ++;
			} else if (rule.contains("|") || (rule.contains("{") && rule.contains("}"))) {
				// disjunction or choice rule, thus add ' :- _debug#' to the rule
				preprocessedProgram.append(rule);
				preprocessedProgram.append(" :- ");
				preprocessedProgram.append(debugConstantPrefix);
				preprocessedProgram.append(debugConstantNum);
				preprocessedProgram.append(".");
				debugAtomRuleMap.put(debugConstantPrefix + debugConstantNum, new Rule(rule.replace("\n", "").trim() + "."));
				
				debugRules.append(debugConstantPrefix);
				debugRules.append(debugConstantNum);
				debugRules.append(".\n");
				
				debugConstantNum ++;
			} else {
				// fact, thus do not alter it
				preprocessedProgram.append(rule);
				
				// only add delimiting . if the rule is not empty
				if (rule.trim().length() > 0) {
					preprocessedProgram.append(".");
				}
			}
		}
		
		// add choice rule for debug constants
		if (debugConstantNum > 1) {
			preprocessedProgram.append("\n");
			preprocessedProgram.append(debugRules);
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
		// remove any aggregates from the rule body
		ruleBody = AGGREGATE_PATTERN.matcher(ruleBody).replaceAll("");
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
