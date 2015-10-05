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

package at.aau.output;

import java.util.Map;

import at.aau.Rule;

/**
 * Builds the output.
 * 
 * @author Philip Gasteiger
 *
 */
public class OutputBuilder {
	private final String DEBUG_RULE_MAP_TYPE = "10";

	private final String DEBUG_RULE_MAP_END = "0";

	/**
	 * Build the rule table containing the debug constants, variables and the
	 * ungrounded rule.
	 * 
	 * @param debugRuleMap
	 *            The map of the debug constants to the ungrounded rules.
	 * @return The rule table.
	 */
	public String buildRuleTable(Map<String, Rule> debugRuleMap) {
		StringBuilder ruleTable = new StringBuilder();

		for (String debugConstant : debugRuleMap.keySet()) {
			Rule rule = debugRuleMap.get(debugConstant);
			ruleTable.append(DEBUG_RULE_MAP_TYPE);
			ruleTable.append(' ');
			ruleTable.append(debugConstant);
			ruleTable.append(' ');
			ruleTable.append(rule.getVariables().size());
			ruleTable.append(' ');
			for (String variable : rule.getVariables()) {
				ruleTable.append(variable);
				ruleTable.append(' ');
			}
			ruleTable.append(rule.getRule());
			ruleTable.append('\n');
		}

		ruleTable.append(DEBUG_RULE_MAP_END);
		ruleTable.append('\n');

		return ruleTable.toString();
	}
}
