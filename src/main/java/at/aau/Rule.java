package at.aau;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A non-ground rule.
 * 
 * @author Philip Gasteiger
 *
 */
public class Rule {
	/** The rule text */
	private final String rule;
	
	/** The variables that occur inside the rule */
	private final List<String> variables;

	public Rule(String rule) {
		this(rule, new ArrayList<String>());
	}

	public Rule(String rule, List<String> variables) {
		this.rule = rule;
		this.variables = variables;
	}

	public String getRule() {
		return rule;
	}

	public List<String> getVariables() {
		return variables;
	}
	
	public Map<String, String> getSubstitution(String term) {
		Map<String, String> substitution = new HashMap<String, String>();
		StringBuilder termBuilder = new StringBuilder();
		int openBrackets = 0;
		
		for (char c : term.toCharArray()) {
			if ('(' == c) {
				openBrackets ++;
			} else if (')' == c) { 
				openBrackets --;
			} else if (',' == c && openBrackets == 0) {
				String var = variables.get(substitution.size());
				substitution.put(var, termBuilder.toString());
				termBuilder.setLength(0);
			} else {
				termBuilder.append(c);
			}
		}
		
		if (termBuilder.length() > 0) {
			String var = variables.get(substitution.size());
			substitution.put(var, termBuilder.toString());
		}
		
		return substitution;
	}

	public String getGroundedRule(String term) {
		return getGroundedRule(getSubstitution(term));
	}
	
	public String getGroundedRule(Map<String, String> substitution) {
		String grounded = rule;
		
		for (String var : substitution.keySet()) {
			grounded = grounded.replaceAll("(?<=[,;( ])" + var + "(?=[,;) ])", substitution.get(var));
		}

		return grounded;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != Rule.class)
			return false;
		
		Rule toCompare = (Rule) obj;
		
		return toCompare.getRule().equals(this.getRule())
			&& this.getVariables().equals(toCompare.getVariables());
	}
}
