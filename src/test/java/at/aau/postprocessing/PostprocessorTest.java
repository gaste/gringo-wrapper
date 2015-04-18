package at.aau.postprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

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
	// removeFactLiteral tests
	// =========================================================================
	/**
	 * Tests whether the fact literal is removed from the symbol table.
	 */
	@Test
	public void removeFactLiteral_removesFromSymbolTable () throws PostprocessingException {
		// act
		String postprocessed = postprocessor.removeFactLiteral(groundedTestCase, factLiteral);
		
		// assert
		assertFalse("Factliteral was not removed from symbol table", postprocessed.contains(factLiteral));
	}
	
	/**
	 * Tests whether the disjunctive rule for the fact literal is removed
	 */
	@Test
	public void removeFactLiteral_removedDisjunctiveRule() throws PostprocessingException {
		// act
		String postprocessed = postprocessor.removeFactLiteral(groundedTestCase, factLiteral);
		
		// assert
		assertFalse("Disjunctive rule was not removed", postprocessed.contains("8 2 3 7 0 0"));
	}
	
	/**
	 * Tests whether the disjunctive rule for the fact literal is removed
	 */
	@Test
	public void removeFactLiteral_removedOtherOrderDisjunctiveRule() throws PostprocessingException {
		// act
		String postprocessed = postprocessor.removeFactLiteral(groundedTestCaseSwitchedDisjunctiveRule, factLiteral);
		
		// assert
		assertFalse("Disjunctive rule was not removed", postprocessed.contains("8 2 7 3 0 0"));
	}
	
	/**
	 * Tests whether the constraint ':- _fl, -_fl' is removed
	 */
	@Test
	public void removeFactLiteral_removedFactLiteralConstraint() throws PostprocessingException {
		// act
		String postprocessed = postprocessor.removeFactLiteral(groundedTestCase, factLiteral);
		
		// assert
		assertFalse("Fact literal constraint was not removed", postprocessed.contains("1 1 2 0 3 7"));
	}
	
	/**
	 * Tests whether all facts were rewritten
	 */
	@Test
	public void removeFactLiteral_rewrittenFacts() throws PostprocessingException {
		// act
		String postprocessed = postprocessor.removeFactLiteral(groundedTestCase, factLiteral);
		
		// assert
		assertFalse("Rule 'a :- _fl.' was not removed", postprocessed.contains("1 2 1 0 3"));
		assertFalse("Rule 'b :- _fl.' was not removed", postprocessed.contains("1 4 1 0 3"));
		assertTrue("Fact 'a.' was not introduced", postprocessed.contains("1 2 0 0"));
		assertTrue("Fact 'b.' was not introduced", postprocessed.contains("1 4 0 0"));
	}
	
	/**
	 * Tests whether the output of the postprocessor matches the expected output
	 */
	@Test
	public void removeFactLiteral_integrationTest() throws PostprocessingException {
		// act
		String postprocessed = postprocessor.removeFactLiteral(groundedTestCase, factLiteral);
		
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
			  + "1";
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	/**
	 * Tests whether the postprocessor throws an exception if the fact literal
	 * is not found in the grounded program.
	 */
	@Test(expected=PostprocessingException.class)
	public void removeFactLiteral_factLiteralNotPresent_throwsException() throws PostprocessingException {
		// act
		postprocessor.removeFactLiteral(groundedTestCase, "_flnotfound");
	}
	
	// =========================================================================
	// removeDebugChoiceRules tests
	// =========================================================================
	
	@Test
	public void removeDebugChoiceRules_noDebugChoiceRules_returnsSame() {
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
		String postprocessed = postprocessor.removeDebugChoiceRules(groundedProgram, "_debug");
		
		// assert
		assertEquals(groundedProgram, postprocessed);
	}
	
	@Test
	public void removeDebugChoiceRules_simpleRuleNoVariables_returnsCorrect() {
		// a.
		// b:-a,_debug1.
		// 0{_debug1}1.
		String groundedProgram = 
				"1 2 0 0\n"
			  + "1 4 2 0 2 5\n"
			  + "3 1 5 0 0\n"
			  + "0\n"
			  + "2 a\n"
			  + "5 _debug1\n"
			  + "4 b\n"
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
			  + "0\n"
			  + "2 a\n"
			  + "5 _debug1\n"
			  + "4 b\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		// act
		String postprocessed = postprocessor.removeDebugChoiceRules(groundedProgram, "_debug");
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void removeDebugChoiceRules_simpleRuleVariables_returnsCorrect() {
		// a(1).a(xy).a(a1).
		// b(X):-a(X),_debug1(X).
		// c(X,Y):-a(X),b(Y),_debug2(X,Y).
		// 0{_debug1(X)}1 :- a(X).
		// 0{_debug2(X,Y)}1 :- a(X),b(Y).
		String groundedProgram =
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 0 0\n"
			  + "1 6 2 0 2 7\n"
			  + "1 8 2 0 4 9\n"
			  + "1 10 2 0 5 11\n"
			  + "1 12 3 0 2 6 13\n"
			  + "1 14 3 0 4 6 15\n"
			  + "1 16 3 0 5 6 17\n"
			  + "1 18 3 0 2 8 19\n"
			  + "1 20 3 0 4 8 21\n"
			  + "1 22 3 0 5 8 23\n"
			  + "1 24 3 0 2 10 25\n"
			  + "1 26 3 0 4 10 27\n"
			  + "1 28 3 0 5 10 29\n"
			  + "1 31 1 0 2\n"
			  + "3 1 7 1 0 31\n"
			  + "1 32 1 0 4\n"
			  + "3 1 9 1 0 32\n"
			  + "1 33 1 0 5\n"
			  + "3 1 11 1 0 33\n"
			  + "1 34 2 0 2 6\n"
			  + "3 1 13 1 0 34\n"
			  + "1 35 2 0 4 6\n"
			  + "3 1 15 1 0 35\n"
			  + "1 36 2 0 5 6\n"
			  + "3 1 17 1 0 36\n"
			  + "1 37 2 0 2 8\n"
			  + "3 1 19 1 0 37\n"
			  + "1 38 2 0 4 8\n"
			  + "3 1 21 1 0 38\n"
			  + "1 39 2 0 5 8\n"
			  + "3 1 23 1 0 39\n"
			  + "1 40 2 0 2 10\n"
			  + "3 1 25 1 0 40\n"
			  + "1 41 2 0 4 10\n"
			  + "3 1 27 1 0 41\n"
			  + "1 42 2 0 5 10\n"
			  + "3 1 29 1 0 42\n"
			  + "0\n"
			  + "2 a(1)\n"
			  + "4 a(xy)\n"
			  + "5 a(a1)\n"
			  + "7 _debug1(1)\n"
			  + "9 _debug1(xy)\n"
			  + "11 _debug1(a1)\n"
			  + "6 b(1)\n"
			  + "8 b(xy)\n"
			  + "10 b(a1)\n"
			  + "13 _debug2(1,1)\n"
			  + "15 _debug2(xy,1)\n"
			  + "17 _debug2(a1,1)\n"
			  + "19 _debug2(1,xy)\n"
			  + "21 _debug2(xy,xy)\n"
			  + "23 _debug2(a1,xy)\n"
			  + "25 _debug2(1,a1)\n"
			  + "27 _debug2(xy,a1)\n"
			  + "29 _debug2(a1,a1)\n"
			  + "12 c(1,1)\n"
			  + "14 c(xy,1)\n"
			  + "16 c(a1,1)\n"
			  + "18 c(1,xy)\n"
			  + "20 c(xy,xy)\n"
			  + "22 c(a1,xy)\n"
			  + "24 c(1,a1)\n"
			  + "26 c(xy,a1)\n"
			  + "28 c(a1,a1)\n"
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
			  + "1 12 3 0 2 6 13\n"
			  + "1 14 3 0 4 6 15\n"
			  + "1 16 3 0 5 6 17\n"
			  + "1 18 3 0 2 8 19\n"
			  + "1 20 3 0 4 8 21\n"
			  + "1 22 3 0 5 8 23\n"
			  + "1 24 3 0 2 10 25\n"
			  + "1 26 3 0 4 10 27\n"
			  + "1 28 3 0 5 10 29\n"
			  + "0\n"
			  + "2 a(1)\n"
			  + "4 a(xy)\n"
			  + "5 a(a1)\n"
			  + "7 _debug1(1)\n"
			  + "9 _debug1(xy)\n"
			  + "11 _debug1(a1)\n"
			  + "6 b(1)\n"
			  + "8 b(xy)\n"
			  + "10 b(a1)\n"
			  + "13 _debug2(1,1)\n"
			  + "15 _debug2(xy,1)\n"
			  + "17 _debug2(a1,1)\n"
			  + "19 _debug2(1,xy)\n"
			  + "21 _debug2(xy,xy)\n"
			  + "23 _debug2(a1,xy)\n"
			  + "25 _debug2(1,a1)\n"
			  + "27 _debug2(xy,a1)\n"
			  + "29 _debug2(a1,a1)\n"
			  + "12 c(1,1)\n"
			  + "14 c(xy,1)\n"
			  + "16 c(a1,1)\n"
			  + "18 c(1,xy)\n"
			  + "20 c(xy,xy)\n"
			  + "22 c(a1,xy)\n"
			  + "24 c(1,a1)\n"
			  + "26 c(xy,a1)\n"
			  + "28 c(a1,a1)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		// act
		String postprocessed = postprocessor.removeDebugChoiceRules(groundedProgram, "_debug");
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void removeDebugChoiceRules_choiceRuleNoVariables_returnsCorrect() {
		// {a} :- _debug1.
		// 0{_debug1}1.
		String groundedProgram = 
				"3 1 4 0 0\n"
			  + "1 5 1 0 4\n"
			  + "3 1 6 1 0 5\n"
			  + "0\n"
			  + "4 _debug1\n"
			  + "6 a\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		String expected = 
			    "1 5 1 0 4\n"
			  + "3 1 6 1 0 5\n"
			  + "0\n"
			  + "4 _debug1\n"
			  + "6 a\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		// act
		String postprocessed = postprocessor.removeDebugChoiceRules(groundedProgram, "_debug");
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void removeDebugChoiceRules_choiceRuleVariables_returnsCorrect() {
		// a(1). a(xy).
		// {b(X)} :- a(X), _debug1(X).
		// {c(X,Y)} :- a(X), b(Y), _debug2(X,Y).
		// 0{_debug1(X)}1:-a(X).
		// 0{_debug2(X,Y)}1:-a(X),b(Y).
		String groundedProgram = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 6 1 0 2\n"
			  + "3 1 7 1 0 6\n"
			  + "1 8 1 0 4\n"
			  + "3 1 9 1 0 8\n"
			  + "1 10 2 0 2 7\n"
			  + "3 1 11 1 0 10\n"
			  + "1 12 2 0 4 9\n"
			  + "3 1 13 1 0 12\n"
			  + "1 14 2 0 2 11\n"
			  + "3 1 15 1 0 14\n"
			  + "1 16 2 0 4 11\n"
			  + "3 1 17 1 0 16\n"
			  + "1 18 2 0 2 13\n"
			  + "3 1 19 1 0 18\n"
			  + "1 20 2 0 4 13\n"
			  + "3 1 21 1 0 20\n"
			  + "1 22 3 0 2 11 15\n"
			  + "3 1 23 1 0 22\n"
			  + "1 24 3 0 4 11 17\n"
			  + "3 1 25 1 0 24\n"
			  + "1 26 3 0 2 13 19\n"
			  + "3 1 27 1 0 26\n"
			  + "1 28 3 0 4 13 21\n"
			  + "3 1 29 1 0 28\n"
			  + "0\n"
			  + "2 a(1)\n"
			  + "4 a(xy)\n"
			  + "7 _debug1(1)\n"
			  + "9 _debug1(xy)\n"
			  + "11 b(1)\n"
			  + "13 b(xy)\n"
			  + "15 _debug2(1,1)\n"
			  + "17 _debug2(xy,1)\n"
			  + "19 _debug2(1,xy)\n"
			  + "21 _debug2(xy,xy)\n"
			  + "23 c(1,1)\n"
			  + "25 c(xy,1)\n"
			  + "27 c(1,xy)\n"
			  + "29 c(xy,xy)\n"
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
			  + "1 10 2 0 2 7\n"
			  + "3 1 11 1 0 10\n"
			  + "1 12 2 0 4 9\n"
			  + "3 1 13 1 0 12\n"
			  + "1 22 3 0 2 11 15\n"
			  + "3 1 23 1 0 22\n"
			  + "1 24 3 0 4 11 17\n"
			  + "3 1 25 1 0 24\n"
			  + "1 26 3 0 2 13 19\n"
			  + "3 1 27 1 0 26\n"
			  + "1 28 3 0 4 13 21\n"
			  + "3 1 29 1 0 28\n"
			  + "0\n"
			  + "2 a(1)\n"
			  + "4 a(xy)\n"
			  + "7 _debug1(1)\n"
			  + "9 _debug1(xy)\n"
			  + "11 b(1)\n"
			  + "13 b(xy)\n"
			  + "15 _debug2(1,1)\n"
			  + "17 _debug2(xy,1)\n"
			  + "19 _debug2(1,xy)\n"
			  + "21 _debug2(xy,xy)\n"
			  + "23 c(1,1)\n"
			  + "25 c(xy,1)\n"
			  + "27 c(1,xy)\n"
			  + "29 c(xy,xy)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		// act
		String postprocessed = postprocessor.removeDebugChoiceRules(groundedProgram, "_debug");
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void removeDebugChoiceRules_disjunctiveRuleNoVariables_returnsCorrect() {
		// a | b :- _debug1.
		// 0{_debug1}1.
		String groundedProgram = 
				"3 1 4 0 0\n"
			  + "8 2 5 6 1 0 4\n"
			  + "0\n"
			  + "4 _debug1\n"
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
			    "8 2 5 6 1 0 4\n"
			  + "0\n"
			  + "4 _debug1\n"
			  + "5 b\n"
			  + "6 a\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		// act
		String postprocessed = postprocessor.removeDebugChoiceRules(groundedProgram, "_debug");
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void removeDebugChoiceRules_disjunctiveRuleVariables_returnsCorrect() {
		// pred(1).pred(x).trans(1,x).
		// a(X) | b(X) :- pred(X), _debug1(X).
		// c(X) | d(X) :- a(X), b(Y), trans(X,Y), _debug2(X,Y).
		// 0{_debug1(X)}1:-pred(X).
		// 0{_debug2(X,Y)}1:-a(X),b(Y),trans(X,Y).
		String groundedProgram = 
				"1 2 0 0\n"
			  + "1 4 0 0\n"
			  + "1 5 0 0\n"
			  + "1 7 1 0 2\n"
			  + "3 1 8 1 0 7\n"
			  + "1 9 1 0 4\n"
			  + "3 1 10 1 0 9\n"
			  + "8 2 11 12 2 0 2 8\n"
			  + "8 2 13 14 2 0 4 10\n"
			  + "1 15 3 0 12 13 5\n"
			  + "3 1 16 1 0 15\n"
			  + "8 2 17 18 4 0 12 13 5 16\n"
			  + "0\n"
			  + "2 pred(1)\n"
			  + "4 pred(x)\n"
			  + "5 trans(1,x)\n"
			  + "8 _debug1(1)\n"
			  + "10 _debug1(x)\n"
			  + "11 b(1)\n"
			  + "13 b(x)\n"
			  + "12 a(1)\n"
			  + "14 a(x)\n"
			  + "16 _debug2(1,x)\n"
			  + "17 d(1)\n"
			  + "18 c(1)\n"
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
			  + "8 2 11 12 2 0 2 8\n"
			  + "8 2 13 14 2 0 4 10\n"
			  + "8 2 17 18 4 0 12 13 5 16\n"
			  + "0\n"
			  + "2 pred(1)\n"
			  + "4 pred(x)\n"
			  + "5 trans(1,x)\n"
			  + "8 _debug1(1)\n"
			  + "10 _debug1(x)\n"
			  + "11 b(1)\n"
			  + "13 b(x)\n"
			  + "12 a(1)\n"
			  + "14 a(x)\n"
			  + "16 _debug2(1,x)\n"
			  + "17 d(1)\n"
			  + "18 c(1)\n"
			  + "0\n"
			  + "B+\n"
			  + "0\n"
			  + "B-\n"
			  + "1\n"
			  + "0\n"
			  + "1";
		
		// act
		String postprocessed = postprocessor.removeDebugChoiceRules(groundedProgram, "_debug");
		
		// assert
		assertEquals(expected, postprocessed);
	}
	
	// =========================================================================
	// addDebugChoiceRule tests
	// =========================================================================
	
	@Test
	public void addDebugChoiceRule_noDebugAtoms_returnsSame() {
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
			  + "1";
		
		String postprocessed = postprocessor.addDebugChoiceRule(groundedProgram, "_debug");
		
		assertEquals(groundedProgram, postprocessed);
	}
	
	@Test
	public void addDebugChoiceRule_unaryDebugAtoms_returnsCorrect() {
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
			  + "1";
		
		String postprocessed = postprocessor.addDebugChoiceRule(groundedProgram, "_debug");
		
		assertEquals(expected, postprocessed);
	}
	
	@Test
	public void addDebugChoiceRule_naryDebugAtoms_returnsCorrect() {
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
			  + "1";
		
		String postprocessed = postprocessor.addDebugChoiceRule(groundedProgram, "_debug");
		
		assertEquals(expected, postprocessed);
	}
}
