package at.aau.preprocessing;

//import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link Preprocessor}.
 * 
 * @author Philip Gasteiger
 *
 */
public class PreprocessorTest {
	/** The class under test. */
	private Preprocessor preprocessor;
	
	@Before
	public void setUp() {
		// create a new instance of the class under test
		preprocessor = new Preprocessor();
	}
	

	// =========================================================================
	// getFactLiteral tests
	// =========================================================================
	/**
	 * Tests whether the fact literal returned is not contained in the logic
	 * program.
	 */
	@Test
	public void getFactLiteral_returnsCorrect () {
		// set up
		String logicProgram = "_fl. _factliteral :- _fl";
		
		// act
		String factLiteral = preprocessor.getFactLiteral(logicProgram);
		
		// assert
		assertFalse(logicProgram.contains(factLiteral));
	}

	// =========================================================================
	// addFactLiteral tests
	// =========================================================================
	/**
	 * Tests whether adding the fact literal to a LP with constant-facts only,
	 * where each fact is in a new line and there are no additional spaces,
	 * works correctly.
	 */
	@Test
	public void addFactLiteral_constantsOnlyNoSpacesNewLine_returnCorrect () {
		// set up
		String lp =	
				"a.\n"
			  + "_b.\n"
			  + "aBJi91ed.\n"
			  + "d :- a.";
		String correct = 
				"a :- _fl.\n"
			  + "_b :- _fl.\n" 
			  + "aBJi91ed :- _fl.\n"
			  + "d :- a.\n"
			  + "_fl | -_fl.";
		
		// act
		String preprocessed = preprocessor.addFactLiteral(lp, "_fl");
		
		// assert
		assertEquals(correct, preprocessed);
	}
	
	/**
	 * Tests whether adding the fact literal to a LP with constant-facts only,
	 * where each fact is in a new line and there are additional spaces, works
	 * correctly.
	 */
	@Test
	public void addFactLiteral_constantsOnlySpacesNewLine_returnCorrect () {
		// set up
		String lp =	
				"  a.\n"
			  + "_b      .\n"
			  + "     aBJi91ed    .\n"
			  + "d :- a.";
		String correct = 
				"  a :- _fl.\n"
			  + "_b       :- _fl.\n" 
			  + "     aBJi91ed     :- _fl.\n"
			  + "d :- a.\n"
			  + "_fl | -_fl.";
		
		// act
		String preprocessed = preprocessor.addFactLiteral(lp, "_fl");
		
		// assert
		assertEquals(correct, preprocessed);
	}
	
	/**
	 * Tests whether adding the fact literal to a LP with constant-facts only,
	 * where multiple facts are in the same line and there are additional
	 * spaces, works correctly.
	 */
	@Test
	public void addFactLiteral_constantsOnlySpacesNoNewLine_returnCorrect () {
		// set up
		String lp =	
				"a.     b.   c.   \n"
			  + "   d. e.f.g.\n"
			  + "x :- a";
		String correct = 
				"a :- _fl.     b :- _fl.   c :- _fl.   \n"
			  + "   d :- _fl. e :- _fl.f :- _fl.g :- _fl.\n"
			  + "x :- a\n"
			  + "_fl | -_fl.";
		
		// act
		String preprocessed = preprocessor.addFactLiteral(lp, "_fl");
		
		// assert
		assertEquals(correct, preprocessed);
	}
	
	/**
	 * Tests whether adding the fact literal to a LP with predicate-facts only,
	 * where each predicate fact is in a new line and there are no additional
	 * spaces, works correctly.
	 */
	@Test
	public void addFactLiteral_predicatesOnlyNoSpacesNewLine_returnsCorrect () {
		// set up
		String lp =
				"pred(a).\n"
			  + "pred(b,c).\n"
			  + "_pRed1(b).\n"
			  + "_pRed1(b,c,d).\n"
			  + "a(X) :- pred(X).";
		String correct = 
				"pred(a) :- _fl.\n"
			  + "pred(b,c) :- _fl.\n"
			  + "_pRed1(b) :- _fl.\n"
			  + "_pRed1(b,c,d) :- _fl.\n"
			  + "a(X) :- pred(X).\n"
			  + "_fl | -_fl.";
		
		// act
		String preprocessed = preprocessor.addFactLiteral(lp, "_fl");
		
		// assert
		assertEquals(correct, preprocessed);
	}
	
	/**
	 * Tests whether adding the fact literal to a LP with predicate-facts only,
	 * where each predicate fact is in a new line and there are additional
	 * spaces, works correctly.
	 */
	@Test
	public void addFactLiteral_predicatesOnlySpacesNewLine_returnsCorrect () {
		// set up
		String lp =
				"  pred (a ).\n"
			  + "pred(  b,c    )   .\n"
			  + "   _pRed1   (b)  .\n"
			  + " _pRed1(b ,  c,  d).\n"
			  + "a(X) :- pred(X).";
		String correct = 
				"  pred (a ) :- _fl.\n"
			  + "pred(  b,c    )    :- _fl.\n"
			  + "   _pRed1   (b)   :- _fl.\n"
			  + " _pRed1(b ,  c,  d) :- _fl.\n"
			  + "a(X) :- pred(X).\n"
			  + "_fl | -_fl.";
		
		// act
		String preprocessed = preprocessor.addFactLiteral(lp, "_fl");
		
		// assert
		assertEquals(correct, preprocessed);
	}
	
	/**
	 * Tests whether adding the fact literal to a LP with predicate-facts only,
	 * where each predicate fact is in a new line and there are additional
	 * spaces, works correctly.
	 */
	@Test
	public void addFactLiteral_predicatesOnlySpacesNoNewLine_returnsCorrect () {
		// set up
		String lp =
				"  pred(a). pred(b).    pred(c).\n"
			  + " pred(d).     pred1(e).pred2(f,g).\n"
			  + "    pred2  (a,   b ).pred2(  b, d).   pred3(a,b,   c  ).\n"
			  + "a(X) :- pred(X).";
		String correct =
				"  pred(a) :- _fl. pred(b) :- _fl.    pred(c) :- _fl.\n"
			  + " pred(d) :- _fl.     pred1(e) :- _fl.pred2(f,g) :- _fl.\n"
			  + "    pred2  (a,   b ) :- _fl.pred2(  b, d) :- _fl.   pred3(a,b,   c  ) :- _fl.\n"
			  + "a(X) :- pred(X).\n"
			  + "_fl | -_fl.";
		
		// act
		String preprocessed = preprocessor.addFactLiteral(lp, "_fl");
		
		// assert
		assertEquals(correct, preprocessed);
	}
	
	/**
	 * Tests whether adding the fact literal to a LP with range-predicates works
	 * correctly.
	 */
	@Test
	public void addFactLiteral_rangePredicatesOnly_returnsCorrect () {
		// set up
		String lp =
				"pred(1..10). pred2 ( 1 ..   5).pred3(  5     ..4).\n"
			  + "pred4(0..2).\n"
			  + " pred5 ( 1 ..   9, 10, a).\n"
			  + "a(X) :- pred(X).";
		String correct =
				"pred(1..10) :- _fl. pred2 ( 1 ..   5) :- _fl.pred3(  5     ..4) :- _fl.\n"
			  + "pred4(0..2) :- _fl.\n"
			  + " pred5 ( 1 ..   9, 10, a) :- _fl.\n"
			  + "a(X) :- pred(X).\n"
			  + "_fl | -_fl.";
		
		// act
		String preprocessed = preprocessor.addFactLiteral(lp, "_fl");
		
		// assert
		assertEquals(correct, preprocessed);
	}
}
