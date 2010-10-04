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
package org.activiti.el.juel.tree.impl.ast;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.activiti.el.juel.ObjectValueExpression;
import org.activiti.el.juel.TestCase;
import org.activiti.el.juel.TreeValueExpression;
import org.activiti.el.juel.misc.TypeConverter;
import org.activiti.el.juel.tree.Bindings;
import org.activiti.el.juel.tree.Tree;
import org.activiti.el.juel.tree.TreeStore;
import org.activiti.el.juel.tree.impl.ast.AstIdentifier;
import org.activiti.el.juel.util.SimpleContext;
import org.activiti.el.juel.util.SimpleResolver;
import org.activiti.javax.el.ELException;
import org.activiti.javax.el.MethodInfo;


public class AstIdentifierTest extends TestCase {	
	public static long method_1() {
		return 1l;
	}
	
	AstIdentifier parseNode(String expression) {
		return getNode(parse(expression));
	}

	AstIdentifier getNode(Tree tree) {
		return (AstIdentifier)tree.getRoot().getChild(0);
	}

	SimpleContext context;
	
	@Override
	protected void setUp() throws Exception {
		context = new SimpleContext(new SimpleResolver());
		
		TypeConverter converter = TypeConverter.DEFAULT;
		
		// variables var_long_1, indentifier_string
		context.setVariable("var_long_1", new ObjectValueExpression(converter, 1l, long.class));
		context.setVariable("indentifier_string", new ObjectValueExpression(converter, "foo", String.class));
		context.setVariable("var_method_1", new ObjectValueExpression(converter, getClass().getMethod("method_1"), Method.class));

		// properties property_long_1, indentifier_string
		context.getELResolver().setValue(context, null, "property_long_1", 1l);
		context.getELResolver().setValue(context, null, "indentifier_string", "bar"); // shadowed by variable indentifier_string
		context.getELResolver().setValue(context, null, "property_method_1", getClass().getMethod("method_1"));

		// var_var_long_1 --> var_long_1, var_property_long_1 --> property_long_1
		context.setVariable("var_var_long_1", new TreeValueExpression(new TreeStore(BUILDER, null), null, context.getVariableMapper(), null, "${var_long_1}", long.class));	
		context.setVariable("var_property_long_1", new TreeValueExpression(new TreeStore(BUILDER, null), null, context.getVariableMapper(), null, "${property_long_1}", long.class));	
	}

	public void testEval() {
		Tree tree = null;
		Bindings bindings = null;

		tree = parse("${bad}");
		bindings = tree.bind(null, context.getVariableMapper());
		try { getNode(tree).eval(bindings, context); fail(); } catch (ELException e) {}
		
		tree = parse("${var_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(1l, getNode(tree).eval(bindings, context));

		tree = parse("${property_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(1l, getNode(tree).eval(bindings, context));

		tree = parse("${indentifier_string}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals("foo", getNode(tree).eval(bindings, context));

		tree = parse("${var_var_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(1l, getNode(tree).eval(bindings, context));

		tree = parse("${var_property_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(1l, getNode(tree).eval(bindings, context));
	}

	public void testAppendStructure() {
		StringBuilder s = new StringBuilder();
		parseNode("${foo}").appendStructure(s, null);
		assertEquals("foo", s.toString());
	}

	public void testIsLiteralText() {
		assertFalse(parseNode("${foo}").isLiteralText());
	}

	public void testIsLeftValue() {
		assertTrue(parseNode("${foo}").isLeftValue());
	}

	public void testGetType() {
		Tree tree = null;
		Bindings bindings = null;

		tree = parse("${var_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(null, getNode(tree).getType(bindings, context));

		tree = parse("${property_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(Object.class, getNode(tree).getType(bindings, context));

		tree = parse("${var_var_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(null, getNode(tree).getType(bindings, context));

		tree = parse("${var_property_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(Object.class, getNode(tree).getType(bindings, context));

		tree = parse("${indentifier_string}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(null, getNode(tree).getType(bindings, context));
	}

	public void testIsReadOnly() {
		Tree tree = null;
		Bindings bindings = null;
		
		tree = parse("${var_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertTrue(getNode(tree).isReadOnly(bindings, context));

		tree = parse("${property_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertFalse(getNode(tree).isReadOnly(bindings, context));

		tree = parse("${var_var_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertTrue(getNode(tree).isReadOnly(bindings, context));

		tree = parse("${var_property_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertFalse(getNode(tree).isReadOnly(bindings, context));

		tree = parse("${indentifier_string}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertTrue(getNode(tree).isReadOnly(bindings, context));
	}

	public void testSetValue() {
		Tree tree = null;
		Bindings bindings = null;

		tree = parse("${bad}");
		bindings = tree.bind(null, context.getVariableMapper());
		getNode(tree).setValue(bindings, context, "good");
		assertEquals("good", getNode(tree).getValue(bindings, context, null));

		tree = parse("${var_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		try { getNode(tree).setValue(bindings, context, 2l); fail(); } catch (ELException e) {}

		tree = parse("${property_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(1l, getNode(tree).getValue(bindings, context, null));
		getNode(tree).setValue(bindings, context, 2l);
		assertEquals(2l, getNode(tree).getValue(bindings, context, null));

		tree = parse("${var_var_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		try { getNode(tree).setValue(bindings, context, 2l); fail(); } catch (ELException e) {}

		tree = parse("${var_property_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(2l, getNode(tree).getValue(bindings, context, null));
		getNode(tree).setValue(bindings, context, 1l);
		assertEquals(1l, getNode(tree).getValue(bindings, context, null));

		tree = parse("${indentifier_string}");
		bindings = tree.bind(null, context.getVariableMapper());
		try { getNode(tree).setValue(bindings, context, "bar"); fail(); } catch (ELException e) {}
	}

	public void testGetValue() {
		Tree tree = null;
		Bindings bindings = null;

		tree = parse("${bad}");
		bindings = tree.bind(null, context.getVariableMapper());
		try { getNode(tree).getValue(bindings, context, null); fail(); } catch (ELException e) {}
		
		tree = parse("${var_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(1l, getNode(tree).getValue(bindings, context, null));
		assertEquals("1", getNode(tree).getValue(bindings, context, String.class));
	}
	
	public void testGetValueReference() {
		Tree tree = null;
		Bindings bindings = null;

		tree = parse("${var_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertNull(getNode(tree).getValueReference(bindings, context));

		tree = parse("${property_long_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertNotNull(getNode(tree).getValueReference(bindings, context));
	}

	public void testInvoke() {
		Tree tree = null;
		Bindings bindings = null;

		tree = parse("${bad}");
		bindings = tree.bind(null, context.getVariableMapper());
		try { getNode(tree).invoke(bindings, context, long.class, new Class[0], null); fail(); } catch (ELException e) {}
		
		tree = parse("${var_method_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(1l, getNode(tree).invoke(bindings, context, long.class, new Class[0], null));
		
		tree = parse("${property_method_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		assertEquals(1l, getNode(tree).invoke(bindings, context, null, new Class[0], null));

		// no return type - ok
		assertEquals(1l, getNode(tree).invoke(bindings, context, long.class, new Class[0], null));
		// bad return type
		try { getNode(tree).invoke(bindings, context, int.class, new Class[0], null); fail(); } catch (ELException e) {}
		// bad arg types
		try { getNode(tree).invoke(bindings, context, long.class, new Class[]{String.class}, null); fail(); } catch (ELException e) {}
		// bad args
		try { getNode(tree).invoke(bindings, context, long.class, new Class[0], new Object[]{""}); fail(); } catch (ELException e) {}
	}

	public void testGetMethodInfo() {
		Tree tree = null;
		Bindings bindings = null;
		MethodInfo info = null;

		tree = parse("${bad}");
		bindings = tree.bind(null, context.getVariableMapper());
		try { getNode(tree).getMethodInfo(bindings, context, long.class, new Class[0]); fail(); } catch (ELException e) {}
		
		tree = parse("${var_method_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		info = getNode(tree).getMethodInfo(bindings, context, long.class, new Class[0]);
		assertEquals("method_1", info.getName());
		assertTrue(Arrays.equals(new Class[0], info.getParamTypes()));
		assertEquals(long.class, info.getReturnType());
		
		tree = parse("${property_method_1}");
		bindings = tree.bind(null, context.getVariableMapper());
		info = getNode(tree).getMethodInfo(bindings, context, long.class, new Class[0]);
		assertEquals("method_1", info.getName());
		assertTrue(Arrays.equals(new Class[0], info.getParamTypes()));
		assertEquals(long.class, info.getReturnType());

		// no return type - ok
		info = getNode(tree).getMethodInfo(bindings, context, null, new Class[0]);
		assertEquals("method_1", info.getName());
		assertTrue(Arrays.equals(new Class[0], info.getParamTypes()));
		assertEquals(long.class, info.getReturnType());
		// bad return type
		try { getNode(tree).getMethodInfo(bindings, context, int.class, new Class[0]); fail(); } catch (ELException e) {}
		// bad arg types
		try { getNode(tree).getMethodInfo(bindings, context, long.class, new Class[]{String.class}); fail(); } catch (ELException e) {}
	}
}
