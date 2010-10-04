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
package org.activiti.el.juel.tree;

import org.activiti.el.juel.ObjectValueExpression;
import org.activiti.el.juel.TestCase;
import org.activiti.el.juel.misc.TypeConverter;
import org.activiti.el.juel.tree.Bindings;
import org.activiti.el.juel.util.SimpleContext;


public class TreeTest extends TestCase {
	
	public static int foo() {
		return 0;
	}

	public static int bar(int op) {
		return op;
	}

	private SimpleContext context;
	
	@Override
	protected void setUp() throws Exception {
		context = new SimpleContext();
		
		// functions ns:f0(), ns:f1(int)
		context.setFunction("ns", "f0", TreeTest.class.getMethod("foo"));
		context.setFunction("ns", "f1", TreeTest.class.getMethod("bar", new Class[]{int.class}));

		// functions g0(), g1(int)
		context.setFunction("", "g0", TreeTest.class.getMethod("foo"));
		context.setFunction("", "g1", TreeTest.class.getMethod("bar", new Class[]{int.class}));

		// variables v0, v1
		context.setVariable("v0", new ObjectValueExpression(TypeConverter.DEFAULT, 0, long.class));
		context.setVariable("v1", new ObjectValueExpression(TypeConverter.DEFAULT, 1, long.class));
	}

	public void testBindFunctions() throws Exception {
		Bindings bindings = null;
		
		bindings = parse("${ns:f0()}").bind(context.getFunctionMapper(), null);
		assertSame(context.getFunctionMapper().resolveFunction("ns", "f0"), bindings.getFunction(0));
		try { bindings.getFunction(1); fail(); } catch (Exception e) {}
		
		bindings = parse("${ns:f1(1)}").bind(context.getFunctionMapper(), null);
		assertSame(context.getFunctionMapper().resolveFunction("ns", "f1"), bindings.getFunction(0));
		try { bindings.getFunction(1); fail(); } catch (Exception e) {}

		bindings = parse("${ns:f0()+ns:f1(1)}").bind(context.getFunctionMapper(), null);
		assertSame(context.getFunctionMapper().resolveFunction("ns", "f0"), bindings.getFunction(0));
		assertSame(context.getFunctionMapper().resolveFunction("ns", "f1"), bindings.getFunction(1));
		try { bindings.getFunction(2); fail(); } catch (Exception e) {}

		// the same for default namespace functions g0(), g1()
		bindings = parse("${g0()}").bind(context.getFunctionMapper(), null);
		assertSame(context.getFunctionMapper().resolveFunction("", "g0"), bindings.getFunction(0));
		try { bindings.getFunction(1); fail(); } catch (Exception e) {}
		
		bindings = parse("${g1(1)}").bind(context.getFunctionMapper(), null);
		assertSame(context.getFunctionMapper().resolveFunction("", "g1"), bindings.getFunction(0));
		try { bindings.getFunction(1); fail(); } catch (Exception e) {}

		bindings = parse("${g0()+g1(1)}").bind(context.getFunctionMapper(), null);
		assertSame(context.getFunctionMapper().resolveFunction("", "g0"), bindings.getFunction(0));
		assertSame(context.getFunctionMapper().resolveFunction("", "g1"), bindings.getFunction(1));
		try { bindings.getFunction(2); fail(); } catch (Exception e) {}

		try { parse("${foo()}").bind(context.getFunctionMapper(), null); fail(); } catch (Exception e) {}
		try { parse("${g1()}").bind(context.getFunctionMapper(), null); fail(); } catch (Exception e) {}
		try { parse("${g1(1,2)}").bind(context.getFunctionMapper(), null); fail(); } catch (Exception e) {}
	}

	public void testBindVariables() throws Exception {
		Bindings bindings = null;
				
		bindings = parse("${v0}").bind(null, context.getVariableMapper());
		assertSame(context.getVariableMapper().resolveVariable("v0"), bindings.getVariable(0));
		try { bindings.getVariable(1); fail(); } catch (Exception e) {}

		bindings = parse("${v1}").bind(null, context.getVariableMapper());
		assertSame(context.getVariableMapper().resolveVariable("v1"), bindings.getVariable(0));
		try { bindings.getVariable(1); fail(); } catch (Exception e) {}

		bindings = parse("${v0+v1}").bind(null, context.getVariableMapper());
		assertSame(context.getVariableMapper().resolveVariable("v0"), bindings.getVariable(0));
		assertSame(context.getVariableMapper().resolveVariable("v1"), bindings.getVariable(1));
		try { bindings.getVariable(2); fail(); } catch (Exception e) {}

		bindings = parse("${foo}").bind(null, context.getVariableMapper());
		assertNull(bindings.getVariable(0));
		try { bindings.getVariable(1); fail(); } catch (Exception e) {}
	}

	public void testBindFunctionsAndVariables() throws Exception {
		Bindings bindings = parse("${ns:f0()+v0+g1(1)+v1+foo}").bind(context.getFunctionMapper(), context.getVariableMapper());
		assertSame(context.getFunctionMapper().resolveFunction("ns", "f0"), bindings.getFunction(0));
		assertSame(context.getFunctionMapper().resolveFunction("", "g1"), bindings.getFunction(1));
		try { bindings.getFunction(2); fail(); } catch (Exception e) {}
		assertSame(context.getVariableMapper().resolveVariable("v0"), bindings.getVariable(0));
		assertSame(context.getVariableMapper().resolveVariable("v1"), bindings.getVariable(1));
		assertNull(bindings.getVariable(2));
		try { bindings.getVariable(3); fail(); } catch (Exception e) {}
	}
}
