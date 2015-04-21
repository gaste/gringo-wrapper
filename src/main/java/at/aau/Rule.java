package at.aau;

import java.util.ArrayList;
import java.util.List;

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
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != Rule.class)
			return false;
		
		Rule toCompare = (Rule) obj;
		
		return toCompare.getRule().equals(this.getRule())
			&& this.getVariables().equals(toCompare.getVariables());
	}
}
