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
	

	// =========================================================================
	// addDebugConstants tests
	// =========================================================================
	
	@Test
	public void addDebugConstants_constantFactsOnly_nothingChanged() {
		// set up
		String lp = 
				"a.\n"
			  + "b.c.d.\n"
			  + "_a.\n"
			  + "_aB.\n"
			  + "a12345_678__9abZIk.\n"
			  + "  bb  .   dd . ";
		
		// act
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug");
		
		// assert
		assertEquals(lp, preprocessed);
	}
	

	@Test
	public void addDebugConstants_predicateFactsOnly_nothingChanged() {
		// set up
		String lp = 
				"pred(a, b, c).\n"
			  + "  pred1   (  a   )  . \n"
			  + "pred(_aAd,_adb124,c12).pred(d1,e81268,f).\n"
			  + " pred3(   _1a  ,  b24)  .  pred3  ( cDik, d) .\n"
			  + "   num( 1 .. 10 ).\n"
			  + "num(15..20).  num    (     25     ..   30 ).";
		
		// act
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug");
		
		// assert
		assertEquals(lp, preprocessed);
	}
	
	@Test
	public void addDebugConstants_choiceRuleNoBody_constantsAdded() {
		// set up
		String lp = 
				"{a;b;c}.\n"
			  + "     { _d1234_56890__12Asd    ;   e   ;     f   }  .  { pred(_a);  pred1 (  b , d )  ; pred (    c )   } .\n"
			  + "  1 { g; h   ; i } 3.\n"
			  + "  1 { pred3  (  a,  b ,    c  )   ; h   ; i } 3.\n"
			  + "1{h;i;pred(d)}2.\n"
			  + "  12 { j; kssd12; l; m; pred3 (d, e, f) }  .\n"
			  + "{n;pred1(c,d);o;p}3.";
		
		String correct = 
				"{a;b;c} :- _debug1.\n"
			  + "     { _d1234_56890__12Asd    ;   e   ;     f   }   :- _debug2.  { pred(_a);  pred1 (  b , d )  ; pred (    c )   }  :- _debug3.\n"
			  + "  1 { g; h   ; i } 3 :- _debug4.\n"
			  + "  1 { pred3  (  a,  b ,    c  )   ; h   ; i } 3 :- _debug5.\n"
			  + "1{h;i;pred(d)}2 :- _debug6.\n"
			  + "  12 { j; kssd12; l; m; pred3 (d, e, f) }   :- _debug7.\n"
			  + "{n;pred1(c,d);o;p}3 :- _debug8.\n"
			  + "{_debug1;_debug2;_debug3;_debug4;_debug5;_debug6;_debug7;_debug8}.";
		
		// act
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug");
		
		// assert
		assertEquals(correct, preprocessed);
	}
	
	@Test
	public void addDebugConstants_choiceRuleWithBody_constantsAdded() {
		// set up
		String lp = 
				"{a;b;c} :- aa.\n"
			  + "     { _d1234_56890__12Asd    ;   e   ;     f   }    :-   bb.  { pred(_a);  pred1 (  b , d )  ; pred (    c )   }    :-  pred (  b   )  ,  pred (e).\n"
			  + "  1 { g; h   ; i } 3 :- ee, ff.\n"
			  + "  1 { pred3  (  a,  b ,    c  )   ; h   ; i } 3   :-   gg  ,   pred   (    b,    c ) .\n"
			  + "1{h;i;pred(d)}2:-a,b,pred(c).\n"
			  + "  12 { j; kssd12; l; m; pred3 (d, e, f) }  :-  pred(a , b),  dd .\n"
			  + "{n;pred1(c,d);o;p}3 :- aa, bb, pred1(d,c).\n"
			  + "{ _pred(X,Y)   ;    _pred (   X ) } :-  pred(X), pred1(  X  , Y  ).";
		
		String correct = 
				"{a;b;c} :- aa, _debug1.\n"
			  + "     { _d1234_56890__12Asd    ;   e   ;     f   }    :-   bb, _debug2.  { pred(_a);  pred1 (  b , d )  ; pred (    c )   }    :-  pred (  b   )  ,  pred (e), _debug3.\n"
			  + "  1 { g; h   ; i } 3 :- ee, ff, _debug4.\n"
			  + "  1 { pred3  (  a,  b ,    c  )   ; h   ; i } 3   :-   gg  ,   pred   (    b,    c ) , _debug5.\n"
			  + "1{h;i;pred(d)}2:-a,b,pred(c), _debug6.\n"
			  + "  12 { j; kssd12; l; m; pred3 (d, e, f) }  :-  pred(a , b),  dd , _debug7.\n"
			  + "{n;pred1(c,d);o;p}3 :- aa, bb, pred1(d,c), _debug8.\n"
			  + "{ _pred(X,Y)   ;    _pred (   X ) } :-  pred(X), pred1(  X  , Y  ), _debug9.\n"
			  + "{_debug1;_debug2;_debug3;_debug4;_debug5;_debug6;_debug7;_debug8;_debug9}.";
		
		// act
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug");
		
		// assert
		assertEquals(correct, preprocessed);
	}
	
	@Test
	public void addDebugConstants_disjunctionNoBody_constantsAdded() {
		String lp =
				"a|b.\n"
			  + "c | _d1234_56890__12Asd | e.\n"
			  + "   fsdf   | g    .  h | i.\n"
			  + "   pred(  _aaax,   _xd   )   |   pred1   (_xd ).";
		
		String correct =
				"a|b :- _debug1.\n"
			  + "c | _d1234_56890__12Asd | e :- _debug2.\n"
			  + "   fsdf   | g     :- _debug3.  h | i :- _debug4.\n"
			  + "   pred(  _aaax,   _xd   )   |   pred1   (_xd ) :- _debug5.\n"
			  + "{_debug1;_debug2;_debug3;_debug4;_debug5}.";

		// act
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug");
		
		// assert
		assertEquals(correct, preprocessed);
	}
	
	@Test
	public void addDebugConstants_disjunctionWithBody_constantsAdded() {
		String lp =
				"a|b:-aa.\n"
			  + "c | _d1234_56890__12Asd | e :- bb.\n"
			  + "   fsdf   | g    :-    dd.  h | i :-  ee.\n"
			  + "   pred(  _aaax,   _Xd   )   |   pred1   (_Xd )   :-  pred3   ( _Xd   )  .";
		
		String correct =
				"a|b:-aa, _debug1.\n"
			  + "c | _d1234_56890__12Asd | e :- bb, _debug2.\n"
			  + "   fsdf   | g    :-    dd, _debug3.  h | i :-  ee, _debug4.\n"
			  + "   pred(  _aaax,   _Xd   )   |   pred1   (_Xd )   :-  pred3   ( _Xd   )  , _debug5.\n"
			  + "{_debug1;_debug2;_debug3;_debug4;_debug5}.";
		  ;

		// act
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug");
		
		// assert
		assertEquals(correct, preprocessed);
		
	}
	
	@Test
	public void addDebugConstants_normalRule_constantsAdded() {
		String lp =
				"a :- b.\n"
			  + "z:-x.y:-x."
			  + "     c   :-   d,   e   ,   f.\n"
			  + "pred1(c) :- d,   e   ,   f.\n"
			  + "     pred2    (   _fX01__234567809c   ,   a )   :-   pred1 (  _fX01__234567809c,   a ).\n"
			  + "  pred1  ( X     )     :-     pred2  (   X, a   ).";
		
		String correct =
				"a :- b, _debug1.\n"
			  + "z:-x, _debug2.y:-x, _debug3."
			  + "     c   :-   d,   e   ,   f, _debug4.\n"
			  + "pred1(c) :- d,   e   ,   f, _debug5.\n"
			  + "     pred2    (   _fX01__234567809c   ,   a )   :-   pred1 (  _fX01__234567809c,   a ), _debug6.\n"
			  + "  pred1  ( X     )     :-     pred2  (   X, a   ), _debug7.\n"
			  + "{_debug1;_debug2;_debug3;_debug4;_debug5;_debug6;_debug7}.";

		// act
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug");
		
		// assert
		assertEquals(correct, preprocessed);
	}
	
	@Test
	public void addDebugConstants_constraintRule_constantsAdded() {
		String lp =
				":- a.\n"
			  + ":-b.:-c,d,e.\n"
			  + "  :-       pred(a).\n"
			  + "     :-   pred2   (   _x1290412805798436___124 ).";
		
		String correct =
				":- a, _debug1.\n"
			  + ":-b, _debug2.:-c,d,e, _debug3.\n"
			  + "  :-       pred(a), _debug4.\n"
			  + "     :-   pred2   (   _x1290412805798436___124 ), _debug5.\n"
			  + "{_debug1;_debug2;_debug3;_debug4;_debug5}.";

		// act
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug");
		
		// assert
		assertEquals(correct, preprocessed);
	}
}
