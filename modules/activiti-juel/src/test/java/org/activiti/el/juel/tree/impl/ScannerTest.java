/*
 * Based on JUEL 2.2.1 code, 2006-2009 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.activiti.el.juel.tree.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.activiti.el.juel.tree.impl.Scanner;
import org.activiti.el.juel.tree.impl.Scanner.*;

import junit.framework.TestCase;


import static org.activiti.el.juel.tree.impl.Scanner.Symbol.*;

public class ScannerTest extends TestCase {
	void assertEquals(Object[] a1, Object[] a2) {
		assertTrue(Arrays.equals(a1, a2));
	}
	
	Symbol[] symbols(String expression) throws ScanException {
		ArrayList<Symbol> list = new ArrayList<Symbol>();
		Scanner scanner = new Scanner(expression);
		Token token = scanner.next();
		while (token.getSymbol() != EOF) {
			list.add(token.getSymbol());
			token = scanner.next();
		}
		return list.toArray(new Symbol[list.size()]);
	}
	
	public void testInteger() throws ScanException {
		Symbol[] expected = { START_EVAL_DYNAMIC, INTEGER, END_EVAL };

		assertEquals(expected, symbols("${0}"));
		assertEquals(expected, symbols("${1}"));
		assertEquals(expected, symbols("${01234567890}"));
	}

	public void testFloat() throws ScanException {
		Symbol[] expected = { START_EVAL_DYNAMIC, FLOAT, END_EVAL };

		assertEquals(expected, symbols("${0.}"));
		assertEquals(expected, symbols("${023456789.}"));
		assertEquals(expected, symbols("${.0}"));
		assertEquals(expected, symbols("${.023456789}"));
		assertEquals(expected, symbols("${0.0}"));

		assertEquals(expected, symbols("${0e0}"));
		assertEquals(expected, symbols("${0E0}"));
		assertEquals(expected, symbols("${0e+0}"));
		assertEquals(expected, symbols("${0E+0}"));
		assertEquals(expected, symbols("${0e+0}"));
		assertEquals(expected, symbols("${0E+0}"));
		
		assertEquals(expected, symbols("${.0e0}"));
		assertEquals(expected, symbols("${.0E0}"));
		assertEquals(expected, symbols("${.0e+0}"));
		assertEquals(expected, symbols("${.0E+0}"));
		assertEquals(expected, symbols("${.0e-0}"));
		assertEquals(expected, symbols("${.0E-0}"));
		
		assertEquals(expected, symbols("${0.e0}"));
		assertEquals(expected, symbols("${0.E0}"));
		assertEquals(expected, symbols("${0.e+0}"));
		assertEquals(expected, symbols("${0.E+0}"));
		assertEquals(expected, symbols("${0.e-0}"));
		assertEquals(expected, symbols("${0.E-0}"));
		
		assertEquals(expected, symbols("${0.0e0}"));
		assertEquals(expected, symbols("${0.0E0}"));
		assertEquals(expected, symbols("${0.0e+0}"));
		assertEquals(expected, symbols("${0.0E+0}"));
		assertEquals(expected, symbols("${0.0e-0}"));
		assertEquals(expected, symbols("${0.0E-0}"));
	}

	public void testString() throws ScanException {
		Symbol[] expected = { START_EVAL_DYNAMIC, STRING, END_EVAL };

		assertEquals(expected, symbols("${'foo'}"));
		assertEquals(expected, symbols("${'f\"o'}"));
		assertEquals(expected, symbols("${'f\\'o'}"));
		try { symbols("${'f\\oo'}"); fail(); } catch (ScanException e) {}
		try { symbols("${'f\\\"o'}"); fail(); } catch (ScanException e) {}
		try { symbols("${'foo"); fail(); } catch (ScanException e) {}

		assertEquals(expected, symbols("${\"foo\"}"));
		assertEquals(expected, symbols("${\"f\\\"o\"}"));
		assertEquals(expected, symbols("${\"f'o\"}"));
		try { symbols("${\"f\\oo\"}"); fail(); } catch (ScanException e) {}
		try { symbols("${\"f\\'o\"}"); fail(); } catch (ScanException e) {}
		try { symbols("${\"foo"); fail(); } catch (ScanException e) {}
	}

	public void testKeywords() throws ScanException {
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, NULL, END_EVAL }, symbols("${null}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, TRUE, END_EVAL }, symbols("${true}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, FALSE, END_EVAL }, symbols("${false}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, EMPTY, END_EVAL }, symbols("${empty}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, DIV, END_EVAL }, symbols("${div}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, MOD, END_EVAL }, symbols("${mod}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, NOT, END_EVAL }, symbols("${not}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, AND, END_EVAL }, symbols("${and}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, OR, END_EVAL }, symbols("${or}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, LE, END_EVAL }, symbols("${le}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, LT, END_EVAL }, symbols("${lt}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, EQ, END_EVAL }, symbols("${eq}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, NE, END_EVAL }, symbols("${ne}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, GE, END_EVAL }, symbols("${ge}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, GT, END_EVAL }, symbols("${gt}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, INSTANCEOF, END_EVAL }, symbols("${instanceof}"));

		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, IDENTIFIER, END_EVAL }, symbols("${xnull}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, IDENTIFIER, END_EVAL }, symbols("${nullx}"));
	}

	public void testOperators() throws ScanException {
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, MUL, END_EVAL }, symbols("${*}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, DIV, END_EVAL }, symbols("${/}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, MOD, END_EVAL }, symbols("${%}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, PLUS, END_EVAL }, symbols("${+}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, MINUS, END_EVAL }, symbols("${-}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, QUESTION, END_EVAL }, symbols("${?}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, COLON, END_EVAL }, symbols("${:}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, LBRACK, END_EVAL }, symbols("${[}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, RBRACK, END_EVAL }, symbols("${]}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, LPAREN, END_EVAL }, symbols("${(}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, RPAREN, END_EVAL }, symbols("${)}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, COMMA, END_EVAL }, symbols("${,}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, DOT, END_EVAL }, symbols("${.}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, AND, END_EVAL }, symbols("${&&}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, OR, END_EVAL }, symbols("${||}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, NOT, END_EVAL }, symbols("${!}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, LE, END_EVAL }, symbols("${<=}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, LT, END_EVAL }, symbols("${<}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, EQ, END_EVAL }, symbols("${==}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, NE, END_EVAL }, symbols("${!=}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, GE, END_EVAL }, symbols("${>=}"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, GT, END_EVAL }, symbols("${>}"));
		
		try { symbols("${&)"); fail(); } catch (ScanException e) {}
		try { symbols("${|)"); fail(); } catch (ScanException e) {}
		try { symbols("${=)"); fail(); } catch (ScanException e) {}
	}

	public void testWhitespace() throws ScanException {
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, END_EVAL }, symbols("${\t\n\r }"));		
	}

	public void testIdentifier() throws ScanException {
		Symbol[] expected = { START_EVAL_DYNAMIC, IDENTIFIER, END_EVAL };

		assertEquals(expected, symbols("${foo}"));
		assertEquals(expected, symbols("${foo_1}"));
	}

	public void testText() throws ScanException {
		Symbol[] expected = { TEXT };

		assertEquals(expected, symbols("foo"));
		assertEquals(expected, symbols("foo\\"));
		assertEquals(expected, symbols("foo\\$"));
		assertEquals(expected, symbols("foo\\#"));
		assertEquals(expected, symbols("foo\\${"));
		assertEquals(expected, symbols("foo\\#{"));
		assertEquals(expected, symbols("\\${foo}"));
		assertEquals(expected, symbols("\\${foo}"));
	}

	public void testMixed() throws ScanException {
		assertEquals(new Symbol[]{ TEXT, START_EVAL_DYNAMIC }, symbols("foo${"));	
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, IDENTIFIER }, symbols("${bar"));
		assertEquals(new Symbol[]{ START_EVAL_DYNAMIC, END_EVAL, TEXT }, symbols("${}bar"));
		assertEquals(new Symbol[]{ TEXT, START_EVAL_DYNAMIC, END_EVAL, TEXT }, symbols("foo${}bar"));
	}

	public void testDeferred() throws ScanException {
		assertEquals(new Symbol[]{ START_EVAL_DEFERRED }, symbols("#{"));	
	}
}