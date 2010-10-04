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

import org.activiti.el.juel.TestCase;
import org.activiti.el.juel.tree.Tree;
import org.activiti.el.juel.tree.impl.ast.AstBinary;


public class ParserTest extends TestCase {
	static Tree verifyLiteralExpression(String expression) {
		Tree tree = parse(expression);
		assertTrue(tree.getRoot().isLiteralText());
		assertEquals(expression, tree.getRoot().getStructuralId(null));
		return tree;
	}

	static Tree verifyEvalExpression(String canonical) {
		Tree tree = parse(canonical);
		assertFalse(tree.getRoot().isLiteralText());
		assertEquals(canonical, tree.getRoot().getStructuralId(null));
		return tree;
	}

	static Tree verifyEvalExpression(String canonical, String expression) {
		Tree tree = parse(expression);
		assertFalse(tree.getRoot().isLiteralText());
		assertEquals(canonical, tree.getRoot().getStructuralId(null));
		return verifyEvalExpression(canonical);
	}

	static Tree verifyEvalExpression(String canonical, String expression1, String expression2) {
		Tree tree = parse(expression2);
		assertFalse(tree.getRoot().isLiteralText());
		assertEquals(canonical, tree.getRoot().getStructuralId(null));
		return verifyEvalExpression(canonical, expression1);
	}

	static Tree verifyCompositeExpression(String canonical) {
		Tree tree = parse(canonical);
		assertFalse(tree.getRoot().isLiteralText());
		assertEquals(canonical, tree.getRoot().getStructuralId(null));
		return tree;
	}

	public void testLiteral() {
		verifyLiteralExpression("");
		verifyLiteralExpression("$");
		verifyLiteralExpression("#");
		verifyLiteralExpression("1");
		verifyLiteralExpression("{1}");
		verifyLiteralExpression("\\${1}");
		verifyLiteralExpression("\\#{1}");
		verifyLiteralExpression("\\${1}\\#{1}");
		verifyLiteralExpression("\\");
		verifyLiteralExpression("\\\\");
		verifyLiteralExpression("foo");
		verifyLiteralExpression("\\f\\o\\o\\");
		verifyLiteralExpression("\"foo\"");
		verifyLiteralExpression("'foo'");
	}

	Tree verifyBinary(AstBinary.Operator op, String canonical) {
		Tree tree = verifyEvalExpression(canonical);
		assertTrue((tree.getRoot()).getChild(0) instanceof AstBinary);
		assertEquals(op, ((AstBinary)tree.getRoot().getChild(0)).getOperator());
		return tree;
	}

	public void testBinray() {
		verifyEvalExpression("${a * a}");
		verifyEvalExpression("${a / a}", "${a div a}");
		verifyEvalExpression("${a % a}", "${a mod a}");
		verifyEvalExpression("${a + a}");
		verifyEvalExpression("${a - a}");
		verifyEvalExpression("${a < a}", "${a lt a}");
		verifyEvalExpression("${a > a}", "${a gt a}");
		verifyEvalExpression("${a <= a}", "${a le a}");
		verifyEvalExpression("${a >= a}", "${a ge a}");
		verifyEvalExpression("${a == a}", "${a eq a}");
		verifyEvalExpression("${a != a}", "${a ne a}");
		verifyEvalExpression("${a && a}", "${a and a}");
		verifyEvalExpression("${a || a}", "${a or a}");

		verifyBinary(AstBinary.DIV, "${a * a / a}");
		verifyBinary(AstBinary.MUL, "${a / a * a}");
		verifyBinary(AstBinary.MOD, "${a / a % a}");
		verifyBinary(AstBinary.DIV, "${a % a / a}");
		verifyBinary(AstBinary.ADD, "${a % a + a}");
		verifyBinary(AstBinary.ADD, "${a + a % a}");
		verifyBinary(AstBinary.SUB, "${a + a - a}");
		verifyBinary(AstBinary.ADD, "${a - a + a}");
		verifyBinary(AstBinary.LT, "${a - a < a}");
		verifyBinary(AstBinary.LT, "${a < a - a}");
		verifyBinary(AstBinary.GT, "${a < a > a}");
		verifyBinary(AstBinary.LT, "${a > a < a}");
		verifyBinary(AstBinary.LE, "${a > a <= a}");
		verifyBinary(AstBinary.GT, "${a <= a > a}");
		verifyBinary(AstBinary.GE, "${a <= a >= a}");
		verifyBinary(AstBinary.LE, "${a >= a <= a}");
		verifyBinary(AstBinary.EQ, "${a == a >= a}");
		verifyBinary(AstBinary.EQ, "${a >= a == a}");
		verifyBinary(AstBinary.NE, "${a == a != a}");
		verifyBinary(AstBinary.EQ, "${a != a == a}");
		verifyBinary(AstBinary.AND, "${a && a != a}");
		verifyBinary(AstBinary.AND, "${a != a && a}");
		verifyBinary(AstBinary.OR, "${a && a || a}");
		verifyBinary(AstBinary.OR, "${a || a && a}");
		verifyBinary(AstBinary.OR, "${! a || a}");
	}

	public void testUnary() {
		verifyEvalExpression("${- a}");
		verifyEvalExpression("${- - a}");
		verifyEvalExpression("${empty a}");
		verifyEvalExpression("${empty empty a}");
		verifyEvalExpression("${! a}", "${not a}");
		verifyEvalExpression("${! ! a}", "${not not a}", "${not ! a}");
	}

	public void testDeferredExpression() {
		verifyEvalExpression("#{a}", "#{ a }");
	}

	
	public void testComposite() {
		verifyCompositeExpression("a${a}a");
		verifyCompositeExpression("a ${a} a");
		verifyCompositeExpression("${a}${a}");
		try { parse("#{a}${a}"); fail(); } catch (Exception e) {}
	}

	public void testInteger() {
		verifyEvalExpression("${0}");
	}

	public void testBoolean() {
		verifyEvalExpression("${true}");
		verifyEvalExpression("${false}");
	}

	public void testNull() {
		verifyEvalExpression("${null}");
	}

	public void testString() {
		verifyEvalExpression("${''}", "${\"\"}");
		verifyEvalExpression("${'\\''}", "${\"'\"}");
		verifyEvalExpression("${'\"'}", "${\"\\\"\"}");
		verifyEvalExpression("${'a'}", "${\"a\"}");
	}

	public void testFloat() {
		verifyEvalExpression("${0.0}", "${0.0e0}");
		verifyEvalExpression("${0.0}", "${0e0}", "${0E0}");
		verifyEvalExpression("${0.0}", "${0.}", "${0.e0}");
		verifyEvalExpression("${0.0}", "${.0}", "${.0e0}");
		verifyEvalExpression("${0.0}", "${0e+0}", "${0e-0}");
	}

	public void testChoice() {
		verifyEvalExpression("${a ? a : a}", "${a?a:a}");
		verifyEvalExpression("${a ? b ? b : b : a}", "${a?b?b:b:a}");
		verifyEvalExpression("${a ? a : b ? b : b}", "${a?a:b?b:b}");
		verifyEvalExpression("${c ? b : (f())}", "${c?b:(f())}");
		verifyEvalExpression("${a ? f() : a}", "${a?f():a}");
		verifyEvalExpression("${a ? a : a:f()}", "${a?a:a:f()}");
		verifyEvalExpression("${a ? a:f() : a}", "${a?a:f():a}");
		try { parse("${a?a:f()}"); fail(); } catch (Exception e) {}
	}

	public void testNested() {
		verifyEvalExpression("${(a)}", "${ ( a ) }");
		verifyEvalExpression("${((a))}");
	}

	public void testIdentifier() {
		verifyEvalExpression("${a}", "${ a}", "${a }");
		assertTrue(parse("${a}").getRoot().isLeftValue());
	}

	public void testFunction() {
		verifyEvalExpression("${a()}");
		verifyEvalExpression("${a(a)}");
		verifyEvalExpression("${a(a, a)}");
		verifyEvalExpression("${a:a()}");
		verifyEvalExpression("${a:a(a)}");
		verifyEvalExpression("${a:a(a, a)}");
	}

	public void testProperty() {
		verifyEvalExpression("${a.a}", "${ a . a }");
		verifyEvalExpression("${a.a.a}");
		verifyEvalExpression("${a[a]}", "${ a [ a ] }");
		verifyEvalExpression("${a[a][a]}");
		verifyEvalExpression("${a[a[a]]}");
		
		assertTrue(parse("${a.a}").getRoot().isLeftValue());
		assertFalse(parse("${1 . a}").getRoot().isLeftValue());
		assertTrue(parse("${(1).a}").getRoot().isLeftValue());

		assertTrue(parse("${a[a]}").getRoot().isLeftValue());
		assertFalse(parse("${1[a]}").getRoot().isLeftValue());
		assertTrue(parse("${(1)[a]}").getRoot().isLeftValue());
	}

	public void testIsDeferred() {
		assertFalse(parse("foo").isDeferred());
		assertFalse(parse("${foo}").isDeferred());
		assertFalse(parse("${foo}bar${foo}").isDeferred());
		assertTrue(parse("#{foo}").isDeferred());
		assertTrue(parse("#{foo}bar#{foo}").isDeferred());
	}
}
