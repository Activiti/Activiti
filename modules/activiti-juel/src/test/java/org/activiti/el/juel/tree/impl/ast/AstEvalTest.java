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
import org.activiti.el.juel.tree.impl.ast.AstEval;


public class AstEvalTest extends TestCase {
	AstEval parseNode(String expression) {
		return (AstEval)parse(expression).getRoot();
	}

	public void testIsLeftValue() {
		assertFalse(parseNode("${1}").isLeftValue());
		assertTrue(parseNode("${foo.bar}").isLeftValue());
	}

	public void testIsDeferred() {
		assertTrue(parseNode("#{1}").isDeferred());
		assertFalse(parseNode("${1}").isDeferred());		
	}

	public void testEval() {
		assertEquals(1l, parseNode("${1}").eval(null, null));
	}

	public void testAppendStructure() {
		StringBuilder s = new StringBuilder();
		parseNode("${1}").appendStructure(s, null);
		assertEquals("${1}", s.toString());
	}
}
