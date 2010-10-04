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
package org.activiti.el.juel;

import org.activiti.el.juel.TreeValueExpression;
import org.activiti.el.juel.tree.TreeStore;
import org.activiti.el.juel.tree.impl.Builder;
import org.activiti.el.juel.util.SimpleContext;
import org.activiti.el.juel.util.SimpleResolver;
import org.activiti.javax.el.BeanELResolver;
import org.activiti.javax.el.PropertyNotFoundException;


public class TreeValueExpressionTest extends TestCase {

	public static int foo() {
		return 0;
	}

	public static int bar() {
		return 0;
	}

	int foobar;
	
	public void setFoobar(int value) {
		foobar = value;
	}
	
	SimpleContext context;
	TreeStore store = new TreeStore(new Builder(), null);
	
	@Override
	protected void setUp() throws Exception {
		context = new SimpleContext(new SimpleResolver(new BeanELResolver()));
		context.getELResolver().setValue(context, null, "base", this);

		// variables var_long_1, var_long_2
		context.setVariable("var_long_1", new TreeValueExpression(store, null, null, null, "${1}", long.class));
		context.setVariable("var_long_2", new TreeValueExpression(store, null, null, null, "${1}", long.class));
		// var_var_long_1 --> var_long_1, var_var_long_2 --> var_long_1
		context.setVariable("var_var_long_1", new TreeValueExpression(store, null, context.getVariableMapper(), null, "${var_long_1}", long.class));	
		context.setVariable("var_var_long_2", new TreeValueExpression(store, null, context.getVariableMapper(), null, "${var_long_2}", long.class));	

		// functions ns:f0(), ns:f1()
		context.setFunction("", "foo", getClass().getMethod("foo"));
		context.setFunction("ns", "foo_1", getClass().getMethod("foo"));
		context.setFunction("ns", "foo_2", getClass().getMethod("foo"));

		context.setVariable("var_foo_1", new TreeValueExpression(store, context.getFunctionMapper(), null, null, "${ns:foo_1()}", long.class));	
		context.setVariable("var_foo_2", new TreeValueExpression(store, context.getFunctionMapper(), null, null, "${ns:foo_2()}", long.class));	

		context.setVariable("var_foobar", new TreeValueExpression(store, null, context.getVariableMapper(), null, "${base.foobar}", int.class));	

		context.getELResolver().setValue(context, null, "property_foo", "foo");
	}

	public void testEqualsAndHashCode() throws NoSuchMethodException {
		TreeValueExpression e1, e2;

		e1 = new TreeValueExpression(store, null, context.getVariableMapper(), null, "${1}", Object.class);
		e2 = new TreeValueExpression(store, null, context.getVariableMapper(), null, "${1}", Object.class);
		assertEquals(e1, e2);
		assertEquals(e1.hashCode(), e2.hashCode());

		e1 = new TreeValueExpression(store, null, context.getVariableMapper(), null, "${var_long_1}", Object.class);
		e2 = new TreeValueExpression(store, null, context.getVariableMapper(), null, "${var_long_2}", Object.class);
		assertEquals(e1, e2);
		assertEquals(e1.hashCode(), e2.hashCode());

		e1 = new TreeValueExpression(store, null, context.getVariableMapper(), null, "${var_var_long_1}", Object.class);
		e2 = new TreeValueExpression(store, null, context.getVariableMapper(), null, "${var_var_long_2}", Object.class);
		assertEquals(e1, e2);
		assertEquals(e1.hashCode(), e2.hashCode());

		e1 = new TreeValueExpression(store, null, context.getVariableMapper(), null, "${var_long_1}", Object.class);
		context.getVariableMapper().setVariable("var_long_1", new TreeValueExpression(store, null, context.getVariableMapper(), null, "${-1}", Object.class));
		e2 = new TreeValueExpression(store, null, context.getVariableMapper(), null, "${var_long_1}", Object.class);
		assertFalse(e1.equals(e2));

		e1 = new TreeValueExpression(store, null, context.getVariableMapper(), null, "${var_foo_1}", Object.class);
		e2 = new TreeValueExpression(store, null, context.getVariableMapper(), null, "${var_foo_2}", Object.class);
		assertEquals(e1, e2);
		assertEquals(e1.hashCode(), e2.hashCode());

		e2 = new TreeValueExpression(store, context.getFunctionMapper(), null, null, "${ns:foo_1()}", Object.class);
		e1 = new TreeValueExpression(store, context.getFunctionMapper(), null, null, "${ns:foo_2()}", Object.class);
		assertEquals(e1, e2);
		assertEquals(e1.hashCode(), e2.hashCode());
	
		e2 = new TreeValueExpression(store, context.getFunctionMapper(), null, null, "${foo()}", Object.class);
		e1 = new TreeValueExpression(store, context.getFunctionMapper(), null, null, "${ns:foo_1()}", Object.class);
		assertEquals(e1, e2);
		assertEquals(e1.hashCode(), e2.hashCode());

		e2 = new TreeValueExpression(store, context.getFunctionMapper(), null, null, "${foo()}", Object.class);
		context.setFunction("", "foo", getClass().getMethod("bar"));
		e1 = new TreeValueExpression(store, context.getFunctionMapper(), null, null, "${foo()}", Object.class);
		assertFalse(e1.equals(e2));
	
		e2 = new TreeValueExpression(store, context.getFunctionMapper(), null, null, "${ns:foo_1()}", Object.class);
		context.setFunction("ns", "foo_1", getClass().getMethod("bar"));
		e1 = new TreeValueExpression(store, context.getFunctionMapper(), null, null, "${ns:foo_1()}", Object.class);
		assertFalse(e1.equals(e2));
	}

	public void testGetExpressionString() {
		assertEquals("foo", new TreeValueExpression(store, null, null, null, "foo", Object.class).getExpressionString());
	}

	public void testIsLiteralText() {
		assertTrue(new TreeValueExpression(store, null, null, null, "foo", Object.class).isLiteralText());
		assertFalse(new TreeValueExpression(store, null, null, null, "${foo}", Object.class).isLiteralText());
	}

	public void testIsDeferred() {
		assertFalse(new TreeValueExpression(store, null, null, null, "foo", Object.class).isDeferred());
		assertFalse(new TreeValueExpression(store, null, null, null, "${foo}", Object.class).isDeferred());
		assertTrue(new TreeValueExpression(store, null, null, null, "#{foo}", Object.class).isDeferred());
	}

	public void testGetExpectedType() {
		assertEquals(Object.class, new TreeValueExpression(store, null, null, null, "${foo}", Object.class).getExpectedType());
		assertEquals(String.class, new TreeValueExpression(store, null, null, null, "${foo}", String.class).getExpectedType());
	}

	public void testGetType() {
		assertFalse(new TreeValueExpression(store, null, null, null, "${property_foo}", Object.class).isReadOnly(context));
	}

	public void testIsReadOnly() {
		assertFalse(new TreeValueExpression(store, null, null, null, "${property_foo}", Object.class).isReadOnly(context));
	}

	public void testSetValue() {
		new TreeValueExpression(store, null, null, null, "${property_foo}", Object.class).setValue(context, "bar");
		assertEquals("bar", new TreeValueExpression(store, null, null, null, "${property_foo}", Object.class).getValue(context));

		// Test added for bug #2748538
		new TreeValueExpression(store, null, context.getVariableMapper(), null, "${var_foobar}", Object.class).setValue(context, 123);
		assertEquals(123, foobar);
		try {
			context.getELResolver().getValue(context, null, "var_foobar");
			fail("Bug in AstIdentifierNode.setValue(...)");
		} catch (PropertyNotFoundException e) {
			// fine 
		}
	}

	public void testGetValue() {
		assertEquals("foo", new TreeValueExpression(store, null, null, null, "${property_foo}", Object.class).getValue(context));
	}

	public void testSerialize() throws Exception  {
		TreeValueExpression expression = new TreeValueExpression(store, context.getFunctionMapper(), context.getVariableMapper(), null, "${var_long_1 + foo()}", Object.class);
		assertEquals(expression, deserialize(serialize(expression)));
	}
}
