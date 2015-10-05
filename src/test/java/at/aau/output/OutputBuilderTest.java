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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.core.StringContains;
import org.hamcrest.core.StringEndsWith;
import org.junit.Test;

import at.aau.Rule;

/**
 * Unit tests for {@link OutputBuilder}.
 * 
 * @author Philip Gasteiger
 *
 */
public class OutputBuilderTest {
	/** The class under test */
	private OutputBuilder outputBuilder = new OutputBuilder();

	// =========================================================================
	// buildRuleTable tests
	// =========================================================================
	@Test
	public void buildRuleTable_emptyMap_returnsCorrect () {
		Map<String, Rule> ruleMap = new HashMap<String, Rule>();
		
		String expected = "0\n";
		
		String ruleTable = outputBuilder.buildRuleTable(ruleMap);
		
		assertEquals(expected, ruleTable);
	}
	
	@Test
	public void buildRuleTable_noVariables_returnsCorrect() {
		Map<String, Rule> ruleMap = new HashMap<String, Rule>();
		ruleMap.put("_debug1", new Rule("a :- b."));
		ruleMap.put("_debug2", new Rule("c :- a, b."));
	
		String ruleTable = outputBuilder.buildRuleTable(ruleMap);
		
		assertThat(ruleTable, StringContains.containsString("10 _debug1 0 a :- b.\n"));
		assertThat(ruleTable, StringContains.containsString("10 _debug2 0 c :- a, b.\n"));
		assertThat(ruleTable, StringEndsWith.endsWith("\n0\n"));
		assertEquals(ruleTable.length(), 47);
	}
	
	@Test
	public void buildRuleTable_variables_returnsCorrect() {
		Map<String, Rule> ruleMap = new HashMap<String, Rule>();
		ruleMap.put("_debug1", new Rule("a(X) :- b(X).", Arrays.asList("X")));
		ruleMap.put("_debug2", new Rule("{pred(X,Y); pred(Y,X)} :- a(X), b(Y).", Arrays.asList("X", "Y")));
		
		String ruleTable = outputBuilder.buildRuleTable(ruleMap);
		
		assertThat(ruleTable, StringContains.containsString("10 _debug1 1 X a(X) :- b(X).\n"));
		assertThat(ruleTable, StringContains.containsString("10 _debug2 2 X Y {pred(X,Y); pred(Y,X)} :- a(X), b(Y).\n"));
		assertThat(ruleTable, StringEndsWith.endsWith("\n0\n"));
		assertEquals(ruleTable.length(), 86);
	}
}
