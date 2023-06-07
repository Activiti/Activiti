/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.core.el.juel.tree.impl.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.el.ELException;
import org.activiti.core.el.juel.test.TestCase;
import org.activiti.core.el.juel.tree.Bindings;
import org.activiti.core.el.juel.tree.Tree;
import org.activiti.core.el.juel.tree.impl.Builder;
import org.activiti.core.el.juel.util.SimpleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AstFunctionTest extends TestCase {
	public static int foo() {
		return 0;
	}

	public static int bar(int op) {
		return op;
	}

	public static int foobar(int op1, int op2) {
		return op1 + op2;
	}

	public static int foovar(int... ops) {
		int sum = 0;
		for (int op : ops) {
			sum += op;
		}
		return sum;
	}

	public static int foovar2(Integer... ops) {
		int sum = 0;
		for (Integer op : ops) {
			if (op != null) {
				sum += op;
			}
		}
		return sum;
	}

	AstFunction parseNode(String expression) {
		return getNode(parse(expression));
	}

	AstFunction getNode(Tree tree) {
		return (AstFunction)tree.getRoot().getChild(0);
	}

	SimpleContext context;

	@BeforeEach
	protected void setUp() throws Exception {
		context = new SimpleContext();

		// functions ns:f0(), ns:f1(int), ns:f2(int)
		context.setFunction("ns", "f0", getClass().getMethod("foo"));
		context.setFunction("ns", "f1", getClass().getMethod("bar", new Class[]{int.class}));
		context.setFunction("ns", "f2", getClass().getMethod("foobar", new Class[]{int.class, int.class}));

		// functions g0(), g1(int), g2(int,int)
		context.setFunction("", "g0", getClass().getMethod("foo"));
		context.setFunction("", "g1", getClass().getMethod("bar", new Class[]{int.class}));
		context.setFunction("", "g2", getClass().getMethod("foobar", new Class[]{int.class, int.class}));

		context.setFunction("vararg", "f", getClass().getMethod("foovar", new Class[]{int[].class}));
		context.getELResolver().setValue(context, null, "var111", new int[]{1,1,1});
		context.getELResolver().setValue(context, null, "var111s", new String[]{"1","1","1"});
	}

	@Test
    public void testVarargs() {
		Builder builder = new Builder(Builder.Feature.VARARGS);
		Tree tree = null;

		tree = builder.build("${vararg:f()}");
		assertEquals(foovar(), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), null));

		tree = builder.build("${vararg:f(1)}");
		assertEquals(foovar(1), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), null));

		tree = builder.build("${vararg:f(1,1)}");
		assertEquals(foovar(1,1), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), null));

		tree = builder.build("${vararg:f(null)}");
		assertEquals(foovar(0), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), null));

		tree = builder.build("${vararg:f(var111)}");
		assertEquals(foovar(1,1,1), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), context));

		tree = builder.build("${vararg:f(var111s)}");
		assertEquals(foovar(1,1,1), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), context));
	}

	@Test
    public void testEval() {
		Tree tree = null;

		tree = parse("${ns:f0()}");
		assertEquals(foo(), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), null));

		tree = parse("${ns:f1(42)}");
		assertEquals(bar(42), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), null));

		tree = parse("${ns:f2(21,21)}");
		assertEquals(foobar(21,21), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), null));

		tree = parse("${g0()}");
		assertEquals(foo(), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), null));

		tree = parse("${g1(42)}");
		assertEquals(bar(42), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), null));

		tree = parse("${g2(21,21)}");
		assertEquals(foobar(21,21), getNode(tree).eval(tree.bind(context.getFunctionMapper(), null), null));
	}

	@Test
    public void testAppendStructure() {
		StringBuilder s = null;

		Bindings bindings = new Bindings(null, null);

		s = new StringBuilder();
		parseNode("${f()}").appendStructure(s, bindings);
		parseNode("${f(x)}").appendStructure(s, bindings);
		parseNode("${f(x,y)}").appendStructure(s, bindings);
		assertEquals("f()f(x)f(x, y)", s.toString());

		s = new StringBuilder();
		parseNode("${p:f()}").appendStructure(s, bindings);
		parseNode("${p:f(x)}").appendStructure(s, bindings);
		parseNode("${p:f(x,y)}").appendStructure(s, bindings);
		assertEquals("p:f()p:f(x)p:f(x, y)", s.toString());
	}

	@Test
    public void testIsLiteralText() {
		assertFalse(parseNode("${f()}").isLiteralText());
	}

	@Test
    public void testIsLeftValue() {
		assertFalse(parseNode("${f()}").isLeftValue());
	}

	@Test
    public void testGetType() {
		assertNull(parseNode("${f()}").getType(null, null));
	}

	@Test
    public void testIsReadOnly() {
		assertTrue(parseNode("${f()}").isReadOnly(null, null));
	}

	@Test
    public void testSetValue() {
		try { parseNode("${f()}").setValue(null, null, null); fail(); } catch (ELException e) {}
	}

	@Test
    public void testGetValue() {
		Tree tree = null;

		tree = parse("${ns:f0()}");

		assertEquals(foo(), getNode(tree).getValue(tree.bind(context.getFunctionMapper(), null), null, null));
		assertEquals("" + foo(), getNode(tree).getValue(tree.bind(context.getFunctionMapper(), null), null, String.class));
	}

	@Test
    public void testGetValueReference() {
		assertNull(parseNode("${ns:f0()}").getValueReference(null, null));
	}
}
