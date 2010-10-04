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

import org.activiti.el.juel.ObjectValueExpression;
import org.activiti.el.juel.misc.TypeConverter;
import org.activiti.javax.el.ELException;


public class ObjectValueExpressionTest extends TestCase {
	private TypeConverter converter = TypeConverter.DEFAULT;

	public void testHashCode() {
		assertEquals("foo".hashCode(), new ObjectValueExpression(converter, "foo", Object.class).hashCode());
	}

	public void testEqualsObject() {
		assertTrue(new ObjectValueExpression(converter, "foo", Object.class).equals(new ObjectValueExpression(converter, "foo", Object.class)));
		assertTrue(new ObjectValueExpression(converter, new String("foo"), Object.class).equals(new ObjectValueExpression(converter, "foo", Object.class)));
		assertFalse(new ObjectValueExpression(converter, "foo", Object.class).equals(new ObjectValueExpression(converter, "bar", Object.class)));
	}

	public void testGetValue() {
		assertEquals("foo", new ObjectValueExpression(converter, "foo", Object.class).getValue(null));
	}

	public void testGetExpressionString() {
		assertNull(new ObjectValueExpression(converter, "foo", Object.class).getExpressionString());
	}

	public void testIsLiteralText() {
		assertFalse(new ObjectValueExpression(converter, "foo", Object.class).isLiteralText());
	}

	public void testGetType() {
		assertNull(new ObjectValueExpression(converter, "foo", Object.class).getType(null));
	}

	public void testIsReadOnly() {
		assertTrue(new ObjectValueExpression(converter, "foo", Object.class).isReadOnly(null));
	}

	public void testSetValue() {
		try {
			new ObjectValueExpression(converter, "foo", Object.class).setValue(null, "bar");
			fail();
		} catch (ELException e) {}
	}

	public void testSerialize() throws Exception {
		ObjectValueExpression expression = new ObjectValueExpression(converter, "foo", Object.class);
		assertEquals(expression, deserialize(serialize(expression)));
	}
}
