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
import org.activiti.el.juel.tree.impl.ast.AstBinary;
import org.activiti.javax.el.ELException;


public class AstBinaryTest extends TestCase {
	private Bindings bindings = new Bindings(null, null, null);
	
	AstBinary parseNode(String expression) {
		return (AstBinary)parse(expression).getRoot().getChild(0);
	}

	public void testEval() {
		assertEquals(6l, parseNode("${4+2}").eval(bindings, null));
		assertEquals(8l, parseNode("${4*2}").eval(bindings, null));
		assertEquals(2d, parseNode("${4/2}").eval(bindings, null));
		assertEquals(0l, parseNode("${4%2}").eval(bindings, null));

		assertEquals(false, parseNode("${true && false}").eval(bindings, null));
		
		assertEquals(true, parseNode("${true || false}").eval(bindings, null));

		assertEquals(true, parseNode("${1 == 1}").eval(bindings, null));
		assertEquals(false, parseNode("${1 == 2}").eval(bindings, null));
		assertEquals(false, parseNode("${2 == 1}").eval(bindings, null));

		assertEquals(false, parseNode("${1 != 1}").eval(bindings, null));
		assertEquals(true, parseNode("${1 != 2}").eval(bindings, null));
		assertEquals(false, parseNode("${2 == 1}").eval(bindings, null));

		assertEquals(false, parseNode("${1 < 1}").eval(bindings, null));
		assertEquals(true, parseNode("${1 < 2}").eval(bindings, null));
		assertEquals(false, parseNode("${2 < 1}").eval(bindings, null));

		assertEquals(false, parseNode("${1 > 1}").eval(bindings, null));
		assertEquals(false, parseNode("${1 > 2}").eval(bindings, null));
		assertEquals(true, parseNode("${2 > 1}").eval(bindings, null));

		assertEquals(true, parseNode("${1 <= 1}").eval(bindings, null));
		assertEquals(true, parseNode("${1 <= 2}").eval(bindings, null));
		assertEquals(false, parseNode("${2 <= 1}").eval(bindings, null));

		assertEquals(true, parseNode("${1 >= 1}").eval(bindings, null));
		assertEquals(false, parseNode("${1 >= 2}").eval(bindings, null));
		assertEquals(true, parseNode("${2 >= 1}").eval(bindings, null));
	}

	public void testAppendStructure() {
		StringBuilder s = null;
		s = new StringBuilder();
		parseNode("${1+1}").appendStructure(s, bindings);
		parseNode("${1*1}").appendStructure(s, bindings);
		parseNode("${1/1}").appendStructure(s, bindings);
		parseNode("${1%1}").appendStructure(s, bindings);
		assertEquals("1 + 11 * 11 / 11 % 1", s.toString());

		s = new StringBuilder();
		parseNode("${1<1}").appendStructure(s, bindings);
		parseNode("${1>1}").appendStructure(s, bindings);
		parseNode("${1<=1}").appendStructure(s, bindings);
		parseNode("${1>=1}").appendStructure(s, bindings);
		assertEquals("1 < 11 > 11 <= 11 >= 1", s.toString());

		s = new StringBuilder();
		parseNode("${1==1}").appendStructure(s, bindings);
		parseNode("${1!=1}").appendStructure(s, bindings);
		assertEquals("1 == 11 != 1", s.toString());

		s = new StringBuilder();
		parseNode("${1&&1}").appendStructure(s, bindings);
		parseNode("${1||1}").appendStructure(s, bindings);
		assertEquals("1 && 11 || 1", s.toString());
	}

	public void testIsLiteralText() {
		assertFalse(parseNode("${1+1}").isLiteralText());
	}

	public void testIsLeftValue() {
		assertFalse(parseNode("${1+1}").isLeftValue());
	}

	public void testGetType() {
		assertNull(parseNode("${1+1}").getType(bindings, null));
	}

	public void testIsReadOnly() {
		assertTrue(parseNode("${1+1}").isReadOnly(bindings, null));
	}

	public void testSetValue() {
		try { parseNode("${1+1}").setValue(bindings, null, null); fail(); } catch (ELException e) {}
	}

	public void testGetValue() {
		assertEquals(Long.valueOf(2l), parseNode("${1+1}").getValue(bindings, null, null));
		assertEquals("2", parseNode("${1+1}").getValue(bindings, null, String.class));
	}

	public void testGetValueReference() {
		assertNull(parseNode("${1+1}").getValueReference(bindings, null));
	}

	public void testOperators() {
		assertTrue((Boolean)parseNode("${true and true}").getValue(bindings, null, Boolean.class));
		assertFalse((Boolean)parseNode("${true and false}").getValue(bindings, null, Boolean.class));
		assertFalse((Boolean)parseNode("${false and true}").getValue(bindings, null, Boolean.class));
		assertFalse((Boolean)parseNode("${false and false}").getValue(bindings, null, Boolean.class));

		assertTrue((Boolean)parseNode("${true or true}").getValue(bindings, null, Boolean.class));
		assertTrue((Boolean)parseNode("${true or false}").getValue(bindings, null, Boolean.class));
		assertTrue((Boolean)parseNode("${false or true}").getValue(bindings, null, Boolean.class));
		assertFalse((Boolean)parseNode("${false or false}").getValue(bindings, null, Boolean.class));
	}
}
