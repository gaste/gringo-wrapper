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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;

import at.aau.Rule;

/**
 * Unit tests for {@link Postprocessor}.
 * 
 * @author Philip Gasteiger
 *
 */
public class PostprocessorTest {
	/** The class under test. */
	private Postprocessor postprocessor;
	
	/**
	 * The grounded logic program to test the postprocessor with. Original logic
	 * program: 
	 * a :- _fl.
	 * b :- _fl.
	 * c :- a, b.
	 * d :- a.
	 * _fl | -_fl.
	 */
	private final String groundedTestCase = 
			"1 2 1 0 3\n"
		  + "1 4 1 0 3\n"
		  + "1 5 2 0 2 4\n"
		  + "1 6 1 0 2\n"
		  + "1 1 2 0 3 7\n"
		  + "8 2 3 7 0 0\n"
		  + "0\n"
		  + "3 _fl\n"
		  + "2 a\n"
		  + "4 b\n"
		  + "5 c\n"
		  + "6 d\n"
		  + "7 -_fl\n"
		  + "0\n"
		  + "B+\n"
		  + "0\n"
		  + "B-\n"
		  + "1\n"
		  + "0\n"
		  + "1";
	
	private final String groundedTestCaseSwitchedDisjunctiveRule = 
			"1 2 1 0 3\n"
		  + "1 4 1 0 3\n"
		  + "1 5 2 0 2 4\n"
		  + "1 6 1 0 2\n"
		  + "1 1 2 0 3 7\n"
		  + "8 2 7 3 0 0\n"
		  + "0\n"
		  + "3 _fl\n"
		  + "2 a\n"
		  + "4 b\n"
		  + "5 c\n"
		  + "6 d\n"
		  + "7 -_fl\n"
		  + "0\n"
		  + "B+\n"
		  + "0\n"
		  + "B-\n"
		  + "1\n"
		  + "0\n"
		  + "1";
	
	
	/** The fact literal used in the test case */
	private final String factLiteral = "_fl";
	
	@Before
	public void setUp() {
		// create a new instance of the class under test
		postprocessor = new Postprocessor();
	}

	// =========================================================================
	// getRemovedRules tests
	// =========================================================================
	@Test
	public void getRemovedRules_noDebugConstants_returnsCorrect() {
		// a.
		// pred(1,2).
		String groundedProgram = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "0\n"
			  + "2 a\n"
			  + "4 pred(1,2)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		
		List<String> removedRules = postprocessor.getRemovedRules(groundedProgram, debugRuleMap);
		
		assertTrue(removedRules.isEmpty());
	}
	
	@Test
	public void getRemovedRules_noneRemoved_returnsCorrect() { 
		// a.
		// n(1).
		// b(X) :- n(X), _debug1(X).
		// c :- a, _debug2.
		//
		// 0{_debug1(X)}1 :- n(X).
		// 0{_debug2}1.		
		String groundedProgram =
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 2 0 4 6\n"
			  + "1 7 2 0 2 8\n"
			  + "0\n"
			  + "2 a\n"
			  + "4 n(1)\n"
			  + "6 _debug1(1)\n"
			  + "5 b(1)\n"
			  + "8 _debug2\n"
			  + "7 c\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";

		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		debugRuleMap.put("_debug1", new Rule("b(X) :- n(X).", Arrays.asList("X")));
		debugRuleMap.put("_debug2", new Rule("c :- a."));
		
		List<String> removedRules = postprocessor.getRemovedRules(groundedProgram, debugRuleMap);
		
		assertTrue(removedRules.isEmpty());
	}
	
	@Test
	public void getRemovedRules_someRemovedDebugConstantsAlsoMissing_returnsCorrect() {
		// n(1).
		// n(2).
		// a(X) :- b(X), n(X), _debug1(X).
		// b(X) :- a(X), n(X), _debug2(X).
		//
		// 0{_debug1(X)}1 :- b(X), n(X).
		// 0{_debug2(X)}1 :- a(X), n(X).		
		String groundedProgram =
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "0\n"
			  + "2 n(1)\n"
			  + "4 n(2)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";

		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		debugRuleMap.put("_debug1", new Rule("a(X) :- b(X), n(X).", Arrays.asList("X")));
		debugRuleMap.put("_debug2", new Rule("b(X) :- a(X), n(X).", Arrays.asList("X")));
		
		List<String> removedRules = postprocessor.getRemovedRules(groundedProgram, debugRuleMap);
		
		assertThat(removedRules, IsIterableContainingInAnyOrder.containsInAnyOrder("a(X) :- b(X), n(X).", "b(X) :- a(X), n(X)."));
	}
	
	@Test
	public void getRemovedRules_someRemovedDebugConstantsPresent_returnsCorrect() {
		// a:-b, _debug1.
		// b:-c, _debug2.
		// c:-a, _debug3.
		// d:-a, _debug4."
		//
		// 0{_debug1}1.
		// 0{_debug2}1.
		// 0{_debug3}1.
		// 0{_debug4}1.		
		String groundedProgram =
				"0\n"
			  + "6 _debug1\n"
			  + "5 _debug2\n"
			  + "4 _debug3\n"
			  + "7 _debug4\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";

		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		debugRuleMap.put("_debug1", new Rule("a:-b."));
		debugRuleMap.put("_debug2", new Rule("b:-c."));
		debugRuleMap.put("_debug3", new Rule("c:-a."));
		debugRuleMap.put("_debug4", new Rule("d:-a."));
		
		List<String> removedRules = postprocessor.getRemovedRules(groundedProgram, debugRuleMap);
		
		assertThat(removedRules, IsIterableContainingInAnyOrder.containsInAnyOrder("a:-b.", "b:-c.", "c:-a.", "d:-a."));
	}
	
	// =========================================================================
	// postprocessAll tests
	// =========================================================================
	/**
	 * Tests whether the fact literal is removed from the symbol table.
	 */
	@Test
	public void postprocessAll_removeFactLiteral_removesFromSymbolTable () throws PostprocessingException {
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedTestCase, "_debug", factLiteral, null);
		
		// assert
		assertFalse("Factliteral was not removed from symbol table", postprocessed.contains(factLiteral));
	}
	
	/**
	 * Tests whether the disjunctive rule for the fact literal is removed
	 */
	@Test
	public void postprocessAll_removeFactLiteral_removedDisjunctiveRule() throws PostprocessingException {
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedTestCase, "_debug", factLiteral, null);
		
		// assert
		assertFalse("Disjunctive rule was not removed", postprocessed.contains("8 2 3 7 0 0"));
	}
	
	/**
	 * Tests whether the disjunctive rule for the fact literal is removed
	 */
	@Test
	public void postprocessAll_removeFactLiteral_removedOtherOrderDisjunctiveRule() throws PostprocessingException {
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedTestCaseSwitchedDisjunctiveRule, "_debug", factLiteral, null);
		
		// assert
		assertFalse("Disjunctive rule was not removed", postprocessed.contains("8 2 7 3 0 0"));
	}
	
	/**
	 * Tests whether the constraint ':- _fl, -_fl' is removed
	 */
	@Test
	public void postprocessAll_removeFactLiteral_removedFactLiteralConstraint() throws PostprocessingException {
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedTestCase, "_debug", factLiteral, null);
		
		// assert
		assertFalse("Fact literal constraint was not removed", postprocessed.contains("1 1 2 0 3 7"));
	}
	
	/**
	 * Tests whether all facts were rewritten
	 */
	@Test
	public void postprocessAll_removeFactLiteral_rewrittenFacts() throws PostprocessingException {
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedTestCase, "_debug", factLiteral, null);
		
		// assert
		assertFalse("Rule 'a :- _fl.' was not removed", postprocessed.contains("1 2 1 0 3"));
		assertFalse("Rule 'b :- _fl.' was not removed", postprocessed.contains("1 4 1 0 3"));
		assertTrue("Fact 'a.' was not introduced", postprocessed.contains("1 2 0 0"));
		assertTrue("Fact 'b.' was not introduced", postprocessed.contains("1 4 0 0"));
	}
	
	/**
	 * Tests whether the postprocessor replaces facts correctly. Consider _fl
	 * with symbol value 3 and the rule '1 1 1 0 30'.
	 * 
	 * In this case, the rule should not be replaced with 1 1 0 0 (which is
	 * 'false'), but could happend due to the fact that the encoding of
	 * 'atom :- _fl.' is equal to '1 0 3' in this case.
	 * @throws PostprocessingException
	 */
	@Test
	public void postprocessAll_removeFactLiteral_factLiteralBodyContained_replacesCorrectly() throws PostprocessingException {
		// a :- _fl.
		// b :- a, _debug1.
		// :- b, _debug2.
		//
		// _debug1 :- _fl.
		// _debug2 :- _fl.
		// :- _debug2.
		//
		// _fl | -_fl.
		String groundedProgram = 
				"1 2 1 0 3\n"
			  + "1 4 1 0 3\n"
			  + "1 5 2 0 4 2\n"
			  + "1 30 1 0 3\n"
			  + "1 1 2 0 30 5\n"
			  + "1 1 1 0 30\n"
			  + "1 1 2 0 3 7\n"
			  + "8 2 7 3 0 0\n"
			  + "0\n"
			  + "3 _fl\n"
			  + "2 a\n"
			  + "4 _debug1\n"
			  + "5 b\n"
			  + "30 _debug2\n"
			  + "7 -_fl\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		String expected = 
				"1 2 0 0\n"
			  + "1 5 2 0 4 2\n"
			  + "1 1 2 0 30 5\n"
			  + "1 1 1 0 30\n"
			  + "3 2 4 30 0 0\n"
			  + "0\n"
			  + "2 a\n"
			  + "4 _debug1\n"
			  + "5 b\n"
			  + "30 _debug2\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", "_fl", null);
		
		assertThat(postprocessed, not(containsString("1 1 0 0")));
		assertEquals(expected, postprocessed);
	}
	
	/**
	 * Tests whether the output of the postprocessor matches the expected output
	 */
	@Test
	public void postprocessAll_removeFactLiteral_integrationTest() throws PostprocessingException {
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedTestCase, "_debug", factLiteral, null);
		
		String expected = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 2 0 2 4\n"
			  + "1 6 1 0 2\n"
			  + "0\n"
			  + "2 a\n"
			  + "4 b\n"
			  + "5 c\n"
			  + "6 d\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void postprocessAll_removeDebugRules_noDebugChoiceRules_returnsSame() {
		// a.
		// b :- a.
		// { c; d; e }.
		// { f; g } :- a,b.
		String groundedProgram = 
				"1 2 0 0\n" 
			  + "1 4 1 0 2\n"
			  + "3 3 6 7 8 0 0\n"
			  + "1 9 2 0 2 4\n" 
			  + "3 2 10 11 1 0 9\n"
			  + "0\n"
			  + "2 a\n" 
			  + "4 b\n" 
			  + "8 c\n"
			  + "7 d\n" 
			  + "6 e\n"
			  + "11 f\n"
			  + "10 g\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", factLiteral, null);
		
		// assert
		assertEquals(groundedProgram + "\n", postprocessed);
	}
	
	@Test
	public void postprocessAll_removeDebugRules_simpleRuleNoVariables_returnsCorrect() {
		// a.
		// b:-a,_debug1.
		// _debug1.
		String groundedProgram = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 2 0 2 4\n"
			  + "0\n"
			  + "2 a\n"
			  + "4 _debug1\n"
			  + "5 b\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		String expected =
				"1 2 0 0\n"
			  + "1 5 2 0 2 4\n"
			  + "3 1 4 0 0\n"
			  + "0\n"
			  + "2 a\n"
			  + "4 _debug1\n"
			  + "5 b\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", factLiteral, null);
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void postprocessAll_removeDebugRules_simpleRuleVariables_returnsCorrect() {
		// a(1).a(xy).a(a1).
		// b(X):-a(X),_debug1(X).
		// c(X,Y):-a(X),b(Y),_debug2(X,Y).
		// _debug1(X) :- a(X).
		// _debug2(X,Y) :- a(X),b(Y).
		String groundedProgram =
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 0 0\n"
			  + "1 6 1 0 2\n"
			  + "1 7 1 0 4\n"
			  + "1 8 1 0 5\n"
			  + "1 9 2 0 2 6\n"
			  + "1 10 2 0 4 7\n"
			  + "1 11 2 0 5 8\n"
			  + "1 12 2 0 2 9\n"
			  + "1 13 2 0 4 9\n"
			  + "1 14 2 0 5 9\n"
			  + "1 15 2 0 2 10\n"
			  + "1 16 2 0 4 10\n"
			  + "1 17 2 0 5 10\n"
			  + "1 18 2 0 2 11\n"
			  + "1 19 2 0 4 11\n"
			  + "1 20 2 0 5 11\n"
			  + "1 21 3 0 2 9 12\n"
			  + "1 22 3 0 4 9 13\n"
			  + "1 23 3 0 5 9 14\n"
			  + "1 24 3 0 2 10 15\n"
			  + "1 25 3 0 4 10 16\n"
			  + "1 26 3 0 5 10 17\n"
			  + "1 27 3 0 2 11 18\n"
			  + "1 28 3 0 4 11 19\n"
			  + "1 29 3 0 5 11 20\n"
			  + "0\n"
			  + "2 a(1)\n"
			  + "4 a(xy)\n"
			  + "5 a(a1)\n"
			  + "6 _debug1(1)\n"
			  + "7 _debug1(xy)\n"
			  + "8 _debug1(a1)\n"
			  + "9 b(1)\n"
			  + "10 b(xy)\n"
			  + "11 b(a1)\n"
			  + "12 _debug2(1,1)\n"
			  + "13 _debug2(xy,1)\n"
			  + "14 _debug2(a1,1)\n"
			  + "15 _debug2(1,xy)\n"
			  + "16 _debug2(xy,xy)\n"
			  + "17 _debug2(a1,xy)\n"
			  + "18 _debug2(1,a1)\n"
			  + "19 _debug2(xy,a1)\n"
			  + "20 _debug2(a1,a1)\n"
			  + "21 c(1,1)\n"
			  + "22 c(xy,1)\n"
			  + "23 c(a1,1)\n"
			  + "24 c(1,xy)\n"
			  + "25 c(xy,xy)\n"
			  + "26 c(a1,xy)\n"
			  + "27 c(1,a1)\n"
			  + "28 c(xy,a1)\n"
			  + "29 c(a1,a1)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		String expected = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 0 0\n"
			  + "1 9 2 0 2 6\n"
			  + "1 10 2 0 4 7\n"
			  + "1 11 2 0 5 8\n"
			  + "1 21 3 0 2 9 12\n"
			  + "1 22 3 0 4 9 13\n"
			  + "1 23 3 0 5 9 14\n"
			  + "1 24 3 0 2 10 15\n"
			  + "1 25 3 0 4 10 16\n"
			  + "1 26 3 0 5 10 17\n"
			  + "1 27 3 0 2 11 18\n"
			  + "1 28 3 0 4 11 19\n"
			  + "1 29 3 0 5 11 20\n"
			  + "3 12 6 7 8 12 13 14 15 16 17 18 19 20 0 0\n"
			  + "0\n"
			  + "2 a(1)\n"
			  + "4 a(xy)\n"
			  + "5 a(a1)\n"
			  + "6 _debug1(1)\n"
			  + "7 _debug1(xy)\n"
			  + "8 _debug1(a1)\n"
			  + "9 b(1)\n"
			  + "10 b(xy)\n"
			  + "11 b(a1)\n"
			  + "12 _debug2(1,1)\n"
			  + "13 _debug2(xy,1)\n"
			  + "14 _debug2(a1,1)\n"
			  + "15 _debug2(1,xy)\n"
			  + "16 _debug2(xy,xy)\n"
			  + "17 _debug2(a1,xy)\n"
			  + "18 _debug2(1,a1)\n"
			  + "19 _debug2(xy,a1)\n"
			  + "20 _debug2(a1,a1)\n"
			  + "21 c(1,1)\n"
			  + "22 c(xy,1)\n"
			  + "23 c(a1,1)\n"
			  + "24 c(1,xy)\n"
			  + "25 c(xy,xy)\n"
			  + "26 c(a1,xy)\n"
			  + "27 c(1,a1)\n"
			  + "28 c(xy,a1)\n"
			  + "29 c(a1,a1)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", factLiteral, null);
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void postprocessAll_removeDebugRules_choiceRuleNoVariables_returnsCorrect() {
		// {a} :- _debug1.
		// _debug1.
		String groundedProgram = 
				"1 2 0 0\n"
			  + "1 5 1 0 2\n"
			  + "3 1 6 1 0 5\n"
			  + "0\n"
			  + "2 _debug1\n"
			  + "6 a\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		String expected = 
			    "1 5 1 0 2\n"
			  + "3 1 6 1 0 5\n"
			  + "3 1 2 0 0\n"
			  + "0\n"
			  + "2 _debug1\n"
			  + "6 a\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", factLiteral, null);
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void postprocessAll_removeDebugRules_choiceRuleVariables_returnsCorrect() {
		// a(1). a(xy).
		// {b(X)} :- a(X), _debug1(X).
		// {c(X,Y)} :- a(X), b(Y), _debug2(X,Y).
		// _debug1(X):-a(X).
		// _debug2(X,Y):-a(X),b(Y).
		String groundedProgram = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 1 0 2\n"
			  + "1 6 1 0 4\n"
			  + "1 7 2 0 2 8\n"
			  + "1 9 2 0 4 8\n"
			  + "1 10 2 0 2 11\n"
			  + "1 12 2 0 4 11\n"
			  + "1 14 2 0 2 5\n"
			  + "3 1 8 1 0 14\n"
			  + "1 15 2 0 4 6\n"
			  + "3 1 11 1 0 15\n"
			  + "1 16 3 0 2 8 7\n"
			  + "3 1 17 1 0 16\n"
			  + "1 18 3 0 4 8 9\n"
			  + "3 1 19 1 0 18\n"
			  + "1 20 3 0 2 11 10\n"
			  + "3 1 21 1 0 20\n"
			  + "1 22 3 0 4 11 12\n"
			  + "3 1 23 1 0 22\n"
			  + "0\n"
			  + "2 a(1)\n"
			  + "4 a(xy)\n"
			  + "5 _debug1(1)\n"
			  + "6 _debug1(xy)\n"
			  + "8 b(1)\n"
			  + "11 b(xy)\n"
			  + "7 _debug2(1,1)\n"
			  + "9 _debug2(xy,1)\n"
			  + "10 _debug2(1,xy)\n"
			  + "12 _debug2(xy,xy)\n"
			  + "17 c(1,1)\n"
			  + "19 c(xy,1)\n"
			  + "21 c(1,xy)\n"
			  + "23 c(xy,xy)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		String expected = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 14 2 0 2 5\n"
			  + "3 1 8 1 0 14\n"
			  + "1 15 2 0 4 6\n"
			  + "3 1 11 1 0 15\n"
			  + "1 16 3 0 2 8 7\n"
			  + "3 1 17 1 0 16\n"
			  + "1 18 3 0 4 8 9\n"
			  + "3 1 19 1 0 18\n"
			  + "1 20 3 0 2 11 10\n"
			  + "3 1 21 1 0 20\n"
			  + "1 22 3 0 4 11 12\n"
			  + "3 1 23 1 0 22\n"
			  + "3 6 5 6 7 9 10 12 0 0\n"
			  + "0\n"
			  + "2 a(1)\n"
			  + "4 a(xy)\n"
			  + "5 _debug1(1)\n"
			  + "6 _debug1(xy)\n"
			  + "8 b(1)\n"
			  + "11 b(xy)\n"
			  + "7 _debug2(1,1)\n"
			  + "9 _debug2(xy,1)\n"
			  + "10 _debug2(1,xy)\n"
			  + "12 _debug2(xy,xy)\n"
			  + "17 c(1,1)\n"
			  + "19 c(xy,1)\n"
			  + "21 c(1,xy)\n"
			  + "23 c(xy,xy)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", factLiteral, null);
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void postprocessAll_removeDebugRules_disjunctiveRuleNoVariables_returnsCorrect() {
		// a | b :- _debug1.
		// _debug1.
		String groundedProgram = 
				"1 2 0 0\n"
			  + "8 2 5 6 1 0 2\n"
			  + "0\n"
			  + "2 _debug1\n"
			  + "5 b\n"
			  + "6 a\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		String expected = 
				"8 2 5 6 1 0 2\n"
			  + "3 1 2 0 0\n"
			  + "0\n"
			  + "2 _debug1\n"
			  + "5 b\n"
			  + "6 a\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", factLiteral, null);
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void postprocessAll_removeDebugRules_disjunctiveRuleVariables_returnsCorrect() {
		// pred(1).pred(x).trans(1,x).
		// a(X) | b(X) :- pred(X), _debug1(X).
		// c(X) | d(X) :- a(X), b(Y), trans(X,Y), _debug2(X,Y).
		// _debug1(X):-pred(X).
		// _debug2(X,Y):-a(X),b(Y),trans(X,Y). 
		String groundedProgram = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 0 0\n"
			  + "1 6 1 0 2\n"
			  + "1 7 1 0 4\n"
			  + "1 8 3 0 9 10 5\n"
			  + "8 2 12 9 2 0 2 6\n"
			  + "8 2 10 13 2 0 4 7\n"
			  + "8 2 14 15 4 0 9 10 5 8\n"
			  + "0\n"
			  + "2 pred(1)\n"
			  + "4 pred(x)\n"
			  + "5 trans(1,x)\n"
			  + "6 _debug1(1)\n"
			  + "7 _debug1(x)\n"
			  + "12 b(1)\n"
			  + "10 b(x)\n"
			  + "9 a(1)\n"
			  + "13 a(x)\n"
			  + "8 _debug2(1,x)\n"
			  + "14 d(1)\n"
			  + "15 c(1)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		String expected = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 0 0\n"
			  + "8 2 12 9 2 0 2 6\n"
			  + "8 2 10 13 2 0 4 7\n"
			  + "8 2 14 15 4 0 9 10 5 8\n"
			  + "3 3 6 7 8 0 0\n"
			  + "0\n"
			  + "2 pred(1)\n"
			  + "4 pred(x)\n"
			  + "5 trans(1,x)\n"
			  + "6 _debug1(1)\n"
			  + "7 _debug1(x)\n"
			  + "12 b(1)\n"
			  + "10 b(x)\n"
			  + "9 a(1)\n"
			  + "13 a(x)\n"
			  + "8 _debug2(1,x)\n"
			  + "14 d(1)\n"
			  + "15 c(1)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		// act
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", factLiteral, null);
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void postprocessAll_addDebugChoiceRule_noDebugAtoms_returnsSame() {
		// a.
		// b:-a.
		// c | d.
		// {e;f}:-b,c.
		// :- not e.
		String groundedProgram =
				"1 2 0 0\n"
			  + "1 4 1 0 2\n"
			  + "1 1 1 1 5\n"
			  + "8 2 7 8 0 0\n"
			  + "1 9 2 0 4 8\n"
			  + "3 2 10 5 1 0 9\n"
			  + "0\n"
			  + "2 a\n"
			  + "4 b\n"
			  + "7 d\n"
			  + "8 c\n"
			  + "5 e\n"
			  + "10 f\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", factLiteral, null);
		
		assertEquals(groundedProgram, postprocessed);
	}
	
	@Test
	public void postprocessAll_addDebugChoiceRule_unaryDebugAtoms_returnsCorrect() {
		// a.
		// b:-a, _debug1.
		// c | d :- _debug2.
		// {e;f}:-b,c, _debug3.
		// :- not e, _debug4.
		String groundedProgram = 
				"1 2 0 0\n"
			  + "1 4 2 0 2 5\n"
			  + "1 1 2 1 6 7\n"
			  + "8 2 10 11 1 0 9\n"
			  + "1 13 3 0 4 11 12\n"
			  + "3 2 14 6 1 0 13\n"
			  + "0\n"
			  + "2 a\n"
			  + "5 _debug1\n"
			  + "4 b\n"
			  + "9 _debug2\n"
			  + "10 d\n"
			  + "11 c\n"
			  + "12 _debug3\n"
			  + "6 e\n"
			  + "14 f\n"
			  + "7 _debug4\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		String expected =
				"1 2 0 0\n"
			  + "1 4 2 0 2 5\n"
			  + "1 1 2 1 6 7\n"
			  + "8 2 10 11 1 0 9\n"
			  + "1 13 3 0 4 11 12\n"
			  + "3 2 14 6 1 0 13\n"
			  + "3 4 5 9 12 7 0 0\n"
			  + "0\n"
			  + "2 a\n"
			  + "5 _debug1\n"
			  + "4 b\n"
			  + "9 _debug2\n"
			  + "10 d\n"
			  + "11 c\n"
			  + "12 _debug3\n"
			  + "6 e\n"
			  + "14 f\n"
			  + "7 _debug4\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", factLiteral, null);
		
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void postprocessAll_addFixedModel_returnsCorrect() {
		// a(1) :- _fl.
		// a(2) :- _fl.
		// b(X,Y) :- a(X), a(Y), not c(X,Y), _debug1(X, Y).
		// c(X,Y) :- a(X), a(Y), not b(X,Y), _debug2(X, Y).
		// _debug1(X, Y) :-  a(X), a(Y), not c(X,Y).
		// _debug2(X, Y) :-  a(X), a(Y), not b(X,Y).
		// _fl | -_fl.
		String groundedProgram =
				"1 2 1 0 3\n" 
			  + "1 4 1 0 3\n" 
			  + "1 5 3 1 6 2 2\n" 
			  + "1 7 3 1 8 2 4\n" 
			  + "1 9 3 1 10 4 2\n" 
			  + "1 11 3 1 12 4 4\n" 
			  + "1 13 4 1 6 5 2 2\n" 
			  + "1 14 4 1 10 9 4 2\n" 
			  + "1 15 4 1 8 7 2 4\n" 
			  + "1 16 4 1 12 11 4 4\n" 
			  + "1 17 3 1 13 2 2\n" 
			  + "1 18 3 1 15 2 4\n" 
			  + "1 19 3 1 14 4 2\n" 
			  + "1 20 3 1 16 4 4\n" 
			  + "1 6 4 1 13 17 2 2\n" 
			  + "1 10 4 1 14 19 4 2\n" 
			  + "1 8 4 1 15 18 2 4\n" 
			  + "1 12 4 1 16 20 4 4\n" 
			  + "1 1 2 0 3 21\n" 
			  + "8 2 21 3 0 0\n" 
			  + "0\n" 
			  + "3 _fl\n" 
			  + "2 a(1)\n" 
			  + "4 a(2)\n" 
			  + "5 _debug1(1,1)\n" 
			  + "7 _debug1(2,1)\n" 
			  + "9 _debug1(1,2)\n" 
			  + "11 _debug1(2,2)\n" 
			  + "6 c(1,1)\n" 
			  + "10 c(1,2)\n" 
			  + "8 c(2,1)\n" 
			  + "12 c(2,2)\n" 
			  + "13 b(1,1)\n" 
			  + "14 b(1,2)\n" 
			  + "15 b(2,1)\n" 
			  + "16 b(2,2)\n" 
			  + "17 _debug2(1,1)\n" 
			  + "18 _debug2(2,1)\n" 
			  + "19 _debug2(1,2)\n" 
			  + "20 _debug2(2,2)\n" 
			  + "21 -_fl\n" 
			  + "0\n" 
			  + "B+\n" 
			  + "0\n" 
			  + "B-\n" 
			  + "1\n" 
			  + "0\n" 
			  + "1";
		
		List<String> model = Arrays.asList("a(1)", "b(1,1)", "b(2,1)", "b(1,2)", "b(2,2)");
		
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", factLiteral, model);
		
		assertThat(postprocessed, containsString("1 1 1 1 2"));
		assertThat(postprocessed, containsString("1 1 1 1 13"));
		assertThat(postprocessed, containsString("1 1 1 1 14"));
		assertThat(postprocessed, containsString("1 1 1 1 15"));
		assertThat(postprocessed, containsString("1 1 1 1 16"));
	}
	
	@Test
	public void postprocessAll_addDebugChoiceRule_naryDebugAtoms_returnsCorrect() {
		// a(1).a(abc9).a(_lD9e).
		// b(X,X) :- a(X), _debug1(X).
		// c(X) :- a(X), _debug2(X).
		// d(X,Y) :- a(X), c(Y), d(X,Y), _debug3(X, Y).
		String groundedProgram = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 0 0\n"
			  + "1 6 2 0 2 7\n"
			  + "1 8 2 0 4 9\n"
			  + "1 10 2 0 5 11\n"
			  + "1 12 2 0 2 13\n"
			  + "1 14 2 0 4 15\n"
			  + "1 16 2 0 5 17\n"
			  + "0\n"
			  + "2 a(1)\n"
			  + "4 a(abc9)\n"
			  + "5 a(_lD9e)\n"
			  + "7 _debug1(1)\n"
			  + "9 _debug1(abc9)\n"
			  + "11 _debug1(_lD9e)\n"
			  + "6 b(1,1)\n"
			  + "8 b(abc9,abc9)\n"
			  + "10 b(_lD9e,_lD9e)\n"
			  + "13 _debug2(1)\n"
			  + "15 _debug2(abc9)\n"
			  + "17 _debug2(_lD9e)\n"
			  + "12 c(1)\n"
			  + "14 c(abc9)\n"
			  + "16 c(_lD9e)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		String expected = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 0 0\n"
			  + "1 6 2 0 2 7\n"
			  + "1 8 2 0 4 9\n"
			  + "1 10 2 0 5 11\n"
			  + "1 12 2 0 2 13\n"
			  + "1 14 2 0 4 15\n"
			  + "1 16 2 0 5 17\n"
			  + "3 6 7 9 11 13 15 17 0 0\n"
			  + "0\n"
			  + "2 a(1)\n"
			  + "4 a(abc9)\n"
			  + "5 a(_lD9e)\n"
			  + "7 _debug1(1)\n"
			  + "9 _debug1(abc9)\n"
			  + "11 _debug1(_lD9e)\n"
			  + "6 b(1,1)\n"
			  + "8 b(abc9,abc9)\n"
			  + "10 b(_lD9e,_lD9e)\n"
			  + "13 _debug2(1)\n"
			  + "15 _debug2(abc9)\n"
			  + "17 _debug2(_lD9e)\n"
			  + "12 c(1)\n"
			  + "14 c(abc9)\n"
			  + "16 c(_lD9e)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", factLiteral, null);
		
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void postprocessAll_normalRulesVariables_integrationTest() {
		// a :- _fl.
		// b :- a, _debug1.
		// n(1..3) :- _fl.
		// pred(X) :- n(X), _debug2(X).
		// _debug1 :- _fl.
		// _debug2(X) :- n(X).
		// _fl | -_fl.
		String groundedProgram = 
				"1 2 1 0 3\n"
			  + "1 4 1 0 3\n"
			  + "1 5 2 0 2 4\n"
			  + "1 6 1 0 3\n"
			  + "1 7 1 0 3\n"
			  + "1 8 1 0 3\n"
			  + "1 9 1 0 6\n"
			  + "1 10 1 0 7\n"
			  + "1 11 1 0 8\n"
			  + "1 12 2 0 6 9\n"
			  + "1 13 2 0 7 10\n"
			  + "1 14 2 0 8 11\n"
			  + "1 1 2 0 3 15\n"
			  + "8 2 3 15 0 0\n"
			  + "0\n"
			  + "3 _fl\n"
			  + "2 a\n"
			  + "4 _debug1\n"
			  + "5 b\n"
			  + "6 n(1)\n"
			  + "7 n(2)\n"
			  + "8 n(3)\n"
			  + "9 _debug2(1)\n"
			  + "10 _debug2(2)\n"
			  + "11 _debug2(3)\n"
			  + "12 pred(1)\n"
			  + "13 pred(2)\n"
			  + "14 pred(3)\n"
			  + "15 -_fl\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		String expected = 
				"1 2 0 0\n"
			  + "1 5 2 0 2 4\n"
			  + "1 6 0 0\n"
			  + "1 7 0 0\n"
			  + "1 8 0 0\n"
			  + "1 12 2 0 6 9\n"
			  + "1 13 2 0 7 10\n"
			  + "1 14 2 0 8 11\n"
			  + "3 4 4 9 10 11 0 0\n"
			  + "0\n"
			  + "2 a\n"
			  + "4 _debug1\n"
			  + "5 b\n"
			  + "6 n(1)\n"
			  + "7 n(2)\n"
			  + "8 n(3)\n"
			  + "9 _debug2(1)\n"
			  + "10 _debug2(2)\n"
			  + "11 _debug2(3)\n"
			  + "12 pred(1)\n"
			  + "13 pred(2)\n"
			  + "14 pred(3)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1\n";
		
		String postprocessed = postprocessor.performPostprocessing(groundedProgram, "_debug", "_fl", null);
		
		assertEquals(expected, postprocessed);
	}
}
