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

import org.activiti.el.juel.TreeMethodExpression;
import org.activiti.el.juel.tree.TreeStore;
import org.activiti.el.juel.tree.impl.Builder;
import org.activiti.el.juel.util.SimpleContext;
import org.activiti.el.juel.util.SimpleResolver;
import org.activiti.javax.el.BeanELResolver;
import org.activiti.javax.el.MethodInfo;


public class TreeMethodExpressionTest extends TestCase {

	public static int foo() {
		return 0;
	}

	public static int bar() {
		return 0;
	}

	SimpleContext context;
	TreeStore store = new TreeStore(new Builder(), null);
	
	@Override
	protected void setUp() throws Exception {
		context = new SimpleContext(new SimpleResolver(new BeanELResolver()));
		context.getELResolver().setValue(context, null, "base", this);
	}


	public void testEqualsAndHashCode() {
		TreeMethodExpression e1, e2;
		e1 = new TreeMethodExpression(store, null, null, null, "${base.foo}", null, new Class[0]);
		e2 = new TreeMethodExpression(store, null, null, null, "${base.foo}", null, new Class[0]);
		assertEquals(e1, e2);

		e1 = new TreeMethodExpression(store, null, null, null, "${base.foo}", null, new Class[0]);
		e2 = new TreeMethodExpression(store, null, null, null, "${base.bar}", null, new Class[0]);
		assertFalse(e1.equals(e2));
	}

	public void testGetExpressionString() {
		assertEquals("${base.foo}", new TreeMethodExpression(store, null, null, null, "${base.foo}", null, new Class[0]).getExpressionString());
	}

	public void testIsLiteralText() {
		assertFalse(new TreeMethodExpression(store, null, null, null, "${base.foo}", null, new Class[0]).isLiteralText());
		assertTrue(new TreeMethodExpression(store, null, null, null, "base.foo", null, new Class[0]).isLiteralText());
	}

	public void testIsDeferred() {
		assertFalse(new TreeMethodExpression(store, null, null, null, "foo", null, new Class[0]).isDeferred());
		assertFalse(new TreeMethodExpression(store, null, null, null, "${foo}", null, new Class[0]).isDeferred());
		assertTrue(new TreeMethodExpression(store, null, null, null, "#{foo}", null, new Class[0]).isDeferred());
	}

	public void testGetMethodInfo() {
		TreeMethodExpression e = new TreeMethodExpression(store, null, null, null, "${base.foo}", null, new Class[0]);
		MethodInfo info = e.getMethodInfo(context);
		assertEquals("foo", info.getName());
		assertEquals(0, info.getParamTypes().length);
		assertEquals(int.class, info.getReturnType());
	}

	public void testInvoke() {
		assertEquals(0, new TreeMethodExpression(store, null, null, null, "${base.foo}", null, new Class[0]).invoke(context, null));
	}


	public void testSerialize() throws Exception  {
		TreeMethodExpression expression = new TreeMethodExpression(store, null,  null, null, "${base.foo}", null, new Class[0]);
		assertEquals(expression, deserialize(serialize(expression)));
	}
}
