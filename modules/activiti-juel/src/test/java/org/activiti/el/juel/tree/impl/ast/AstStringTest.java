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

import org.activiti.el.juel.TestCase;
import org.activiti.el.juel.tree.Bindings;
import org.activiti.el.juel.tree.impl.ast.AstString;
import org.activiti.javax.el.ELException;


public class AstStringTest extends TestCase {
	private Bindings bindings = new Bindings(null, null, null);
	
	AstString parseNode(String expression) {
		return (AstString)parse(expression).getRoot().getChild(0);
	}

	public void testEval() {
		assertEquals("foo", parseNode("${'foo'}").eval(bindings, null));
	}

	public void testAppendStructure() {
		StringBuilder s = new StringBuilder();
		parseNode("${'foo'}").appendStructure(s, bindings);
		assertEquals("'foo'", s.toString());
	}

	public void testIsLiteralText() {
		assertFalse(parseNode("${'foo'}").isLiteralText());
	}

	public void testIsLeftValue() {
		assertFalse(parseNode("${'foo'}").isLeftValue());
	}

	public void testGetType() {
		assertNull(parseNode("${'foo'}").getType(bindings, null));
	}

	public void testIsReadOnly() {
		assertTrue(parseNode("${'foo'}").isReadOnly(bindings, null));
	}

	public void testSetValue() {
		try { parseNode("${'foo'}").setValue(bindings, null, null); fail(); } catch (ELException e) {}
	}

	public void testGetValue() {
		assertEquals("1", parseNode("${'1'}").getValue(bindings, null, null));
		assertEquals(1, parseNode("${'1'}").getValue(bindings, null, Integer.class));
	}

	public void testGetValueReference() {
		assertNull(parseNode("${'foo'}").getValueReference(null, null));
	}
}
