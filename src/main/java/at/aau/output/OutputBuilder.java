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
