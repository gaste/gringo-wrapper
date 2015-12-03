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

package at.aau.preprocessing;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import at.aau.Rule;

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
	// getFixedModel tests
	// =========================================================================
	@Test
	public void getFixedModel_noFixModelCommand_returnsNull() {
		String logicProgram =
				"a.\n"
			  + "b :- a.\n"
			  + "assertTrue(b).";
		
		List<String> fixedModel = preprocessor.getFixedModel(logicProgram);
		
		assertNull(fixedModel);
	}
	
	@Test
	public void getFixedModel_fixModelCommandCommentedOut_returnsNull() {
		String logicProgram =
				"a.\n"
			  + "b :- a.\n"
			  + "%fixModel(a,b).";
		
		List<String> fixedModel = preprocessor.getFixedModel(logicProgram);
		
		assertNull(fixedModel);
	}
	
	@Test
	@Ignore(value="Functionality for detecting multi-line comments not yet implemented")
	public void getFixedModel_fixModelCommandCommentedOutMultiline_returnsNull() {
		String logicProgram =
				"a.\n"
			  + "b :- a.\n"
			  + "%*\n"
			  + "fixModel(a,b).\n"
			  + "*%\n"
			  + "assertTrue(a).";
		
		List<String> fixedModel = preprocessor.getFixedModel(logicProgram);
		
		assertNull(fixedModel);
	}
	
	@Test
	public void getFixedModel_constantsOnly_returnsCorrect() {
		String logicProgram = 
				"a.\n"
			  + "b :- a.\n"
			  + "fixModel(a,b).";
		
		List<String> fixedModel = preprocessor.getFixedModel(logicProgram);
		
		assertEquals(Arrays.asList("a", "b"), fixedModel);
	}
	
	@Test
	public void getFixedModel_predicates_returnsCorrect() {
		String logicProgram =
				"a(1..2).\n"
			  + "b(X,Y) :- a(X), a(Y), not c(X,Y).\n"
			  + "c(X,Y) :- a(X), a(Y), not b(X,Y).\n"
			  + "fixModel(a(1),a(2),b(1,1),b(2,1),b(1,2),b(2,2)).";
		
		List<String> fixedModel = preprocessor.getFixedModel(logicProgram);
		
		assertEquals(Arrays.asList("a(1)","a(2)","b(1,1)","b(2,1)","b(1,2)","b(2,2)"), fixedModel);
	}
	
	@Test
	public void getFixedModel_assertions_returnsCorrect() {
		String logicProgram =
				"a.\n"
			  + "b :- a.\n"
			  + "fixModel(a,b).\n"
			  + "assertFalse(b).";
		
		List<String> fixedModel = preprocessor.getFixedModel(logicProgram);
		
		assertEquals(Arrays.asList("a"), fixedModel);
	}

	// =========================================================================
	// rewriteAssertions tests
	// =========================================================================
	@Test
	public void rewriteAssertions_noAssertions_returnsSame() {
		String logicProgram =
				"a. b(1).\n"
			  + "c :- a.\n"
			  + "d(X) :- b(1).\n";
		
		String preprocessed = preprocessor.rewriteAssertions(logicProgram);

		assertEquals(logicProgram, preprocessed);
	}
	
	@Test
	public void rewriteAssertions_assertTrue_returnsRewritten() {
		String logicProgram =
				"assertTrue(a).\n"
			  + "assertTrue(pred(a)).\n"
			  + "assertTrue ( b ).assertTrue(c).\n"
			  + " assertTrue  (   pred2 (a, b) )  .\n";
		
		String expected =
				":- not a.\n"
			  + ":- not pred(a).\n"
			  + ":- not  b .:- not c.\n"
			  + ":- not    pred2 (a, b) .\n";
		
		String preprocessed = preprocessor.rewriteAssertions(logicProgram);

		assertEquals(expected, preprocessed);
	}
	
	@Test
	public void rewriteAssertions_assertFalse_returnsRewritten() {
		String logicProgram =
				"assertFalse(a).\n"
			  + "assertFalse(pred(a)).\n"
			  + "assertFalse ( b ).assertFalse(c).\n"
			  + " assertFalse  (   pred2 (a, b) )  .\n";
		
		String expected =
				":- a.\n"
			  + ":- pred(a).\n"
			  + ":-  b .:- c.\n"
			  + ":-    pred2 (a, b) .\n";
		
		String preprocessed = preprocessor.rewriteAssertions(logicProgram);

		assertEquals(expected, preprocessed);
	}

	// =========================================================================
	// removeComments tests
	// =========================================================================
	@Test
	public void removeComments_noComments_returnsSame() {
		String logicProgram = 
				"a. b(1).\n"
			  + "c :- a.\n"
			  + "d(X) :- b(1).\n";
		
		String preprocessed = preprocessor.removeComments(logicProgram);
		
		assertEquals(logicProgram, preprocessed);
	}
	
	@Test
	public void removeComments_lineComments_returnsCorrect() {
		String logicProgram = 
				"% description of the program.\n"
			  + "a(X) :- b(X, _).\n"
			  + "b(1,2).\n"
			  + "   %   end of program"
			  + "%c :- a(X).";
		
		String expected = 
			    "\na(X) :- b(X, _).\n"
			  + "b(1,2).\n";
		
		String preprocessed = preprocessor.removeComments(logicProgram);
		
		assertEquals(expected, preprocessed);
	}
	
	@Test
	public void removeComments_inlineComments_returnsCorrect() {
		String logicProgram = 
				"a(X) :- b(X, _).%description of this line of code\n"
			  + "d :- a(X).  %  comment";
		
		String expected =
				"a(X) :- b(X, _).\n"
			  + "d :- a(X).";
		
		String preprocessed = preprocessor.removeComments(logicProgram);
		
		assertEquals(expected, preprocessed);
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
		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug", debugRuleMap);
		
		// assert
		assertEquals(lp, preprocessed);
		assertTrue(debugRuleMap.isEmpty());
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
		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug", debugRuleMap);
		
		// assert
		assertEquals(lp, preprocessed);
		assertTrue(debugRuleMap.isEmpty());
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
			  + "_debug1.\n"
			  + "_debug2.\n"
			  + "_debug3.\n"
			  + "_debug4.\n"
			  + "_debug5.\n"
			  + "_debug6.\n"
			  + "_debug7.\n"
			  + "_debug8.\n";
		
		Map<String, Rule> correctDebugRuleMap = new HashMap<String, Rule>();
		correctDebugRuleMap.put("_debug1", new Rule("{a;b;c}."));
		correctDebugRuleMap.put("_debug2", new Rule("{ _d1234_56890__12Asd    ;   e   ;     f   }."));
		correctDebugRuleMap.put("_debug3", new Rule("{ pred(_a);  pred1 (  b , d )  ; pred (    c )   }."));
		correctDebugRuleMap.put("_debug4", new Rule("1 { g; h   ; i } 3."));
		correctDebugRuleMap.put("_debug5", new Rule("1 { pred3  (  a,  b ,    c  )   ; h   ; i } 3."));
		correctDebugRuleMap.put("_debug6", new Rule("1{h;i;pred(d)}2."));
		correctDebugRuleMap.put("_debug7", new Rule("12 { j; kssd12; l; m; pred3 (d, e, f) }."));
		correctDebugRuleMap.put("_debug8", new Rule("{n;pred1(c,d);o;p}3."));
		
		// act
		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug", debugRuleMap);
		
		// assert
		assertEquals(correct, preprocessed);
		assertThat(debugRuleMap, is(correctDebugRuleMap));
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
			  + "{ _pred(X,Y)   ;    _pred (   X ) } :-  pred(X), pred1(  X  , Y  ), _debug9(X, Y).\n"
			  + "_debug1.\n"
			  + "_debug2.\n"
			  + "_debug3.\n"
			  + "_debug4.\n"
			  + "_debug5.\n"
			  + "_debug6.\n"
			  + "_debug7.\n"
			  + "_debug8.\n"
			  + "_debug9(X, Y) :-   pred(X), pred1(  X  , Y  ).\n";
		
		Map<String, Rule> correctDebugRuleMap = new HashMap<String, Rule>();
		correctDebugRuleMap.put("_debug1", new Rule("{a;b;c} :- aa."));
		correctDebugRuleMap.put("_debug2", new Rule("{ _d1234_56890__12Asd    ;   e   ;     f   }    :-   bb."));
		correctDebugRuleMap.put("_debug3", new Rule("{ pred(_a);  pred1 (  b , d )  ; pred (    c )   }    :-  pred (  b   )  ,  pred (e)."));
		correctDebugRuleMap.put("_debug4", new Rule("1 { g; h   ; i } 3 :- ee, ff."));
		correctDebugRuleMap.put("_debug5", new Rule("1 { pred3  (  a,  b ,    c  )   ; h   ; i } 3   :-   gg  ,   pred   (    b,    c )."));
		correctDebugRuleMap.put("_debug6", new Rule("1{h;i;pred(d)}2:-a,b,pred(c)."));
		correctDebugRuleMap.put("_debug7", new Rule("12 { j; kssd12; l; m; pred3 (d, e, f) }  :-  pred(a , b),  dd."));
		correctDebugRuleMap.put("_debug8", new Rule("{n;pred1(c,d);o;p}3 :- aa, bb, pred1(d,c)."));
		correctDebugRuleMap.put("_debug9", new Rule("{ _pred(X,Y)   ;    _pred (   X ) } :-  pred(X), pred1(  X  , Y  ).", Arrays.asList("X", "Y")));
		
		// act
		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug", debugRuleMap);
		
		// assert
		assertEquals(correct, preprocessed);
		assertThat(debugRuleMap, is(correctDebugRuleMap));
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
			  + "_debug1.\n"
			  + "_debug2.\n"
			  + "_debug3.\n"
			  + "_debug4.\n"
			  + "_debug5.\n";

		Map<String, Rule> correctDebugRuleMap = new HashMap<String, Rule>();
		correctDebugRuleMap.put("_debug1", new Rule("a|b."));
		correctDebugRuleMap.put("_debug2", new Rule("c | _d1234_56890__12Asd | e."));
		correctDebugRuleMap.put("_debug3", new Rule("fsdf   | g."));
		correctDebugRuleMap.put("_debug4", new Rule("h | i."));
		correctDebugRuleMap.put("_debug5", new Rule("pred(  _aaax,   _xd   )   |   pred1   (_xd )."));
		
		// act
		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug", debugRuleMap);
		
		// assert
		assertEquals(correct, preprocessed);
		assertThat(debugRuleMap, is(correctDebugRuleMap));
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
			  + "   pred(  _aaax,   _Xd   )   |   pred1   (_Xd )   :-  pred3   ( _Xd   )  , _debug5(_Xd).\n"
			  + "_debug1.\n"
			  + "_debug2.\n"
			  + "_debug3.\n"
			  + "_debug4.\n"
			  + "_debug5(_Xd) :-   pred3   ( _Xd   )  .\n";
		
		Map<String, Rule> correctDebugRuleMap = new HashMap<String, Rule>();
		correctDebugRuleMap.put("_debug1", new Rule("a|b:-aa."));
		correctDebugRuleMap.put("_debug2", new Rule("c | _d1234_56890__12Asd | e :- bb."));
		correctDebugRuleMap.put("_debug3", new Rule("fsdf   | g    :-    dd."));
		correctDebugRuleMap.put("_debug4", new Rule("h | i :-  ee."));
		correctDebugRuleMap.put("_debug5", new Rule("pred(  _aaax,   _Xd   )   |   pred1   (_Xd )   :-  pred3   ( _Xd   ).", Arrays.asList("_Xd")));
		
		// act
		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug", debugRuleMap);
		
		// assert
		assertEquals(correct, preprocessed);
		assertThat(debugRuleMap, is(correctDebugRuleMap));
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
			  + "  pred1  ( X     )     :-     pred2  (   X, a   ), _debug7(X).\n"
			  + "_debug1.\n"
			  + "_debug2.\n"
			  + "_debug3.\n"
			  + "_debug4.\n"
			  + "_debug5.\n"
			  + "_debug6.\n"
			  + "_debug7(X) :-      pred2  (   X, a   ).\n";

		Map<String, Rule> correctDebugRuleMap = new HashMap<String, Rule>();
		correctDebugRuleMap.put("_debug1", new Rule("a :- b."));
		correctDebugRuleMap.put("_debug2", new Rule("z:-x."));
		correctDebugRuleMap.put("_debug3", new Rule("y:-x."));
		correctDebugRuleMap.put("_debug4", new Rule("c   :-   d,   e   ,   f."));
		correctDebugRuleMap.put("_debug5", new Rule("pred1(c) :- d,   e   ,   f."));
		correctDebugRuleMap.put("_debug6", new Rule("pred2    (   _fX01__234567809c   ,   a )   :-   pred1 (  _fX01__234567809c,   a )."));
		correctDebugRuleMap.put("_debug7", new Rule("pred1  ( X     )     :-     pred2  (   X, a   ).", Arrays.asList("X")));
		
		// act
		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug", debugRuleMap);
		
		// assert
		assertEquals(correct, preprocessed);
		assertThat(debugRuleMap, is(correctDebugRuleMap));
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
			  + "_debug1.\n"
			  + "_debug2.\n"
			  + "_debug3.\n"
			  + "_debug4.\n"
			  + "_debug5.\n";

		Map<String, Rule> correctDebugRuleMap = new HashMap<String, Rule>();
		correctDebugRuleMap.put("_debug1", new Rule(":- a."));
		correctDebugRuleMap.put("_debug2", new Rule(":-b."));
		correctDebugRuleMap.put("_debug3", new Rule(":-c,d,e."));
		correctDebugRuleMap.put("_debug4", new Rule(":-       pred(a)."));
		correctDebugRuleMap.put("_debug5", new Rule(":-   pred2   (   _x1290412805798436___124 )."));
		
		// act
		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug", debugRuleMap);
		
		// assert
		assertEquals(correct, preprocessed);
		assertThat(debugRuleMap, is(correctDebugRuleMap));
	}
	
	@Test
	public void addDebugConstants_aggregateRuleNoSpaces_addedCorrect() {
		String lp =
				":-{pred(X,Y)},t(X).\n"
			  + ":-3{pred(X,Y,Z)},t(X).\n"
			  + ":-1<{pred(X,Y):t(Y)},t(X).\n"
			  + ":-{pred(X,Y)}1,t(X).\n"
			  + ":-{pred(X,Y)}<1,t(X).\n"
			  + ":-{pred(X,Y)}.\n"
			  + ":-t(X),1<{pred(X,Y)}.";
		
		String correct =
				":-{pred(X,Y)},t(X), _debug1(X).\n"
			  + ":-3{pred(X,Y,Z)},t(X), _debug2(X).\n"
			  + ":-1<{pred(X,Y):t(Y)},t(X), _debug3(X).\n"
			  + ":-{pred(X,Y)}1,t(X), _debug4(X).\n"
			  + ":-{pred(X,Y)}<1,t(X), _debug5(X).\n"
			  + ":-{pred(X,Y)}, _debug6.\n"
			  + ":-t(X),1<{pred(X,Y)}, _debug7(X).\n"
			  + "_debug1(X) :- t(X).\n"
			  + "_debug2(X) :- t(X).\n"
			  + "_debug3(X) :- t(X).\n"
			  + "_debug4(X) :- t(X).\n"
			  + "_debug5(X) :- t(X).\n"
			  + "_debug6.\n"
			  + "_debug7(X) :- t(X).\n";
		
		Map<String, Rule> correctDebugRuleMap = new HashMap<String, Rule>();
		correctDebugRuleMap.put("_debug1", new Rule(":-{pred(X,Y)},t(X).", Arrays.asList("X")));
		correctDebugRuleMap.put("_debug2", new Rule(":-3{pred(X,Y,Z)},t(X).", Arrays.asList("X")));
		correctDebugRuleMap.put("_debug3", new Rule(":-1<{pred(X,Y):t(Y)},t(X).", Arrays.asList("X")));
		correctDebugRuleMap.put("_debug4", new Rule(":-{pred(X,Y)}1,t(X).", Arrays.asList("X")));
		correctDebugRuleMap.put("_debug5", new Rule(":-{pred(X,Y)}<1,t(X).", Arrays.asList("X")));
		correctDebugRuleMap.put("_debug6", new Rule(":-{pred(X,Y)}."));
		correctDebugRuleMap.put("_debug7", new Rule(":-t(X),1<{pred(X,Y)}.", Arrays.asList("X")));
		
		// act
		Map<String, Rule> debugRuleMap = new HashMap<String, Rule>();
		String preprocessed = preprocessor.addDebugConstants(lp, "_debug", debugRuleMap);
		
		// assert
		assertEquals(correct, preprocessed);
		assertThat(debugRuleMap, is(correctDebugRuleMap));
	}
}
