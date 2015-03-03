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
	 * Tests whether the choice rule for the fact literal is removed
	 */
	@Test
	public void removeFactLiteral_removedChoiceRule() throws PostprocessingException {
		// act
		String postprocessed = postprocessor.removeFactLiteral(groundedTestCase, factLiteral);
		
		// assert
		assertFalse("Choice rule was not removed", postprocessed.contains("8 2 3 7 0 0"));
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
}
