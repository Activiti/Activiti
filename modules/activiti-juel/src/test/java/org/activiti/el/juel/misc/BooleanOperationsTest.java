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
package org.activiti.el.juel.misc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import org.activiti.el.juel.misc.BooleanOperations;
import org.activiti.el.juel.misc.TypeConverter;

import junit.framework.TestCase;

public class BooleanOperationsTest extends TestCase {

	/**
	 * Test enum type
	 */
	static enum Foo { BAR, BAZ };

	private TypeConverter converter = TypeConverter.DEFAULT;
	
	/*
	 * Test method for 'de.odysseus.el.lang.BooleanOperations.lt(Object, Object)'
	 */
	public void testLt() {
		assertFalse(BooleanOperations.lt(converter, Boolean.TRUE, Boolean.TRUE));
		assertFalse(BooleanOperations.lt(converter, null, Boolean.TRUE));
		assertFalse(BooleanOperations.lt(converter, Boolean.TRUE, null));
		assertTrue(BooleanOperations.lt(converter, "1", new BigDecimal("2")));
		assertFalse(BooleanOperations.lt(converter, new BigDecimal("1"), "1"));
		assertFalse(BooleanOperations.lt(converter, new BigDecimal("2"), "1"));
		assertTrue(BooleanOperations.lt(converter, "1", new Float("2")));
		assertFalse(BooleanOperations.lt(converter, new Float("1"), "1"));
		assertFalse(BooleanOperations.lt(converter, new Float("2"), "1"));
		assertTrue(BooleanOperations.lt(converter, "1", new Double("2")));
		assertFalse(BooleanOperations.lt(converter, new Double("1"), "1"));
		assertFalse(BooleanOperations.lt(converter, new Double("2"), "1"));
		assertTrue(BooleanOperations.lt(converter, "1", new BigInteger("2")));
		assertFalse(BooleanOperations.lt(converter, new BigInteger("1"), "1"));
		assertFalse(BooleanOperations.lt(converter, new BigInteger("2"), "1"));
		assertTrue(BooleanOperations.lt(converter, "1", new Byte("2")));
		assertFalse(BooleanOperations.lt(converter, new Byte("1"), "1"));
		assertFalse(BooleanOperations.lt(converter, new Byte("2"), "1"));
		assertTrue(BooleanOperations.lt(converter, "1", new Short("2")));
		assertFalse(BooleanOperations.lt(converter, new Short("1"), "1"));
		assertFalse(BooleanOperations.lt(converter, new Short("2"), "1"));
		assertTrue(BooleanOperations.lt(converter, new Character('a'), new Character('b')));
		assertFalse(BooleanOperations.lt(converter, new Character('a'), new Character('a')));
		assertFalse(BooleanOperations.lt(converter, new Character('b'), new Character('a')));
		assertTrue(BooleanOperations.lt(converter, "1", new Integer("2")));
		assertFalse(BooleanOperations.lt(converter, new Integer("1"), "1"));
		assertFalse(BooleanOperations.lt(converter, new Integer("2"), "1"));
		assertTrue(BooleanOperations.lt(converter, "1", new Long("2")));
		assertFalse(BooleanOperations.lt(converter, new Long("1"), "1"));
		assertFalse(BooleanOperations.lt(converter, new Long("2"), "1"));
		assertTrue(BooleanOperations.lt(converter, "a", "b"));
		assertFalse(BooleanOperations.lt(converter, "a", "a"));
		assertFalse(BooleanOperations.lt(converter, "b", "a"));
		try {
			BooleanOperations.lt(converter, getClass(), new Character('a'));
			fail();
		} catch (Exception e) {}
		try {
			BooleanOperations.lt(converter, new Character('a'), getClass());
			fail();
		} catch (Exception e) {}
		try {
			BooleanOperations.lt(converter, getClass(), new Long(0));
			fail();
		} catch (Exception e) {}
	}

	/*
	 * Test method for 'de.odysseus.el.lang.BooleanOperations.gt(Object, Object)'
	 */
	public void testGt() {
		assertFalse(BooleanOperations.gt(converter, Boolean.TRUE, Boolean.TRUE));
		assertFalse(BooleanOperations.gt(converter, null, Boolean.TRUE));
		assertFalse(BooleanOperations.gt(converter, Boolean.TRUE, null));
		assertFalse(BooleanOperations.gt(converter, "1", new BigDecimal("2")));
		assertFalse(BooleanOperations.gt(converter, new BigDecimal("1"), "1"));
		assertTrue(BooleanOperations.gt(converter, new BigDecimal("2"), "1"));
		assertFalse(BooleanOperations.gt(converter, "1", new Float("2")));
		assertFalse(BooleanOperations.gt(converter, new Float("1"), "1"));
		assertTrue(BooleanOperations.gt(converter, new Float("2"), "1"));
		assertFalse(BooleanOperations.gt(converter, "1", new Double("2")));
		assertFalse(BooleanOperations.gt(converter, new Double("1"), "1"));
		assertTrue(BooleanOperations.gt(converter, new Double("2"), "1"));
		assertFalse(BooleanOperations.gt(converter, "1", new BigInteger("2")));
		assertFalse(BooleanOperations.gt(converter, new BigInteger("1"), "1"));
		assertTrue(BooleanOperations.gt(converter, new BigInteger("2"), "1"));
		assertFalse(BooleanOperations.gt(converter, "1", new Byte("2")));
		assertFalse(BooleanOperations.gt(converter, new Byte("1"), "1"));
		assertTrue(BooleanOperations.gt(converter, new Byte("2"), "1"));
		assertFalse(BooleanOperations.gt(converter, "1", new Short("2")));
		assertFalse(BooleanOperations.gt(converter, new Short("1"), "1"));
		assertTrue(BooleanOperations.gt(converter, new Short("2"), "1"));
		assertFalse(BooleanOperations.gt(converter, new Character('a'), new Character('b')));
		assertFalse(BooleanOperations.gt(converter, new Character('a'), new Character('a')));
		assertTrue(BooleanOperations.gt(converter, new Character('b'), new Character('a')));
		assertFalse(BooleanOperations.gt(converter, "1", new Integer("2")));
		assertFalse(BooleanOperations.gt(converter, new Integer("1"), "1"));
		assertTrue(BooleanOperations.gt(converter, new Integer("2"), "1"));
		assertFalse(BooleanOperations.gt(converter, "1", new Long("2")));
		assertFalse(BooleanOperations.gt(converter, new Long("1"), "1"));
		assertTrue(BooleanOperations.gt(converter, new Long("2"), "1"));
		assertFalse(BooleanOperations.gt(converter, "a", "b"));
		assertFalse(BooleanOperations.gt(converter, "a", "a"));
		assertTrue(BooleanOperations.gt(converter, "b", "a"));
		try {
			BooleanOperations.gt(converter, getClass(), new Character('a'));
			fail();
		} catch (Exception e) {}
		try {
			BooleanOperations.gt(converter, new Character('a'), getClass());
			fail();
		} catch (Exception e) {}
		try {
			BooleanOperations.gt(converter, getClass(), new Long(0));
			fail();
		} catch (Exception e) {}
	}

	/*
	 * Test method for 'de.odysseus.el.lang.BooleanOperations.ge(Object, Object)'
	 */
	public void testGe() {
		assertTrue(BooleanOperations.ge(converter, Boolean.TRUE, Boolean.TRUE));
		assertFalse(BooleanOperations.ge(converter, null, Boolean.TRUE));
		assertFalse(BooleanOperations.ge(converter, Boolean.TRUE, null));
		assertFalse(BooleanOperations.ge(converter, "1", new BigDecimal("2")));
		assertTrue(BooleanOperations.ge(converter, new BigDecimal("1"), "1"));
		assertTrue(BooleanOperations.ge(converter, new BigDecimal("2"), "1"));
		assertFalse(BooleanOperations.ge(converter, "1", new Float("2")));
		assertTrue(BooleanOperations.ge(converter, new Float("1"), "1"));
		assertTrue(BooleanOperations.ge(converter, new Float("2"), "1"));
		assertFalse(BooleanOperations.ge(converter, "1", new Double("2")));
		assertTrue(BooleanOperations.ge(converter, new Double("1"), "1"));
		assertTrue(BooleanOperations.ge(converter, new Double("2"), "1"));
		assertFalse(BooleanOperations.ge(converter, "1", new BigInteger("2")));
		assertTrue(BooleanOperations.ge(converter, new BigInteger("1"), "1"));
		assertTrue(BooleanOperations.ge(converter, new BigInteger("2"), "1"));
		assertFalse(BooleanOperations.ge(converter, "1", new Byte("2")));
		assertTrue(BooleanOperations.ge(converter, new Byte("1"), "1"));
		assertTrue(BooleanOperations.ge(converter, new Byte("2"), "1"));
		assertFalse(BooleanOperations.ge(converter, "1", new Short("2")));
		assertTrue(BooleanOperations.ge(converter, new Short("1"), "1"));
		assertTrue(BooleanOperations.ge(converter, new Short("2"), "1"));
		assertFalse(BooleanOperations.ge(converter, new Character('a'), new Character('b')));
		assertTrue(BooleanOperations.ge(converter, new Character('a'), new Character('a')));
		assertTrue(BooleanOperations.ge(converter, new Character('b'), new Character('a')));
		assertFalse(BooleanOperations.ge(converter, "1", new Integer("2")));
		assertTrue(BooleanOperations.ge(converter, new Integer("1"), "1"));
		assertTrue(BooleanOperations.ge(converter, new Integer("2"), "1"));
		assertFalse(BooleanOperations.ge(converter, "1", new Long("2")));
		assertTrue(BooleanOperations.ge(converter, new Long("1"), "1"));
		assertTrue(BooleanOperations.ge(converter, new Long("2"), "1"));
		assertFalse(BooleanOperations.ge(converter, "a", "b"));
		assertTrue(BooleanOperations.ge(converter, "a", "a"));
		assertTrue(BooleanOperations.ge(converter, "b", "a"));
		try {
			BooleanOperations.ge(converter, getClass(), new Character('a'));
			fail();
		} catch (Exception e) {}
		try {
			BooleanOperations.ge(converter, new Character('a'), getClass());
			fail();
		} catch (Exception e) {}
		try {
			BooleanOperations.ge(converter, getClass(), new Long(0));
			fail();
		} catch (Exception e) {}
	}

	/*
	 * Test method for 'de.odysseus.el.lang.BooleanOperations.le(Object, Object)'
	 */
	public void testLe() {
		assertTrue(BooleanOperations.le(converter, Boolean.TRUE, Boolean.TRUE));
		assertFalse(BooleanOperations.le(converter, null, Boolean.TRUE));
		assertFalse(BooleanOperations.le(converter, Boolean.TRUE, null));
		assertTrue(BooleanOperations.le(converter, "1", new BigDecimal("2")));
		assertTrue(BooleanOperations.le(converter, new BigDecimal("1"), "1"));
		assertFalse(BooleanOperations.le(converter, new BigDecimal("2"), "1"));
		assertTrue(BooleanOperations.le(converter, "1", new Float("2")));
		assertTrue(BooleanOperations.le(converter, new Float("1"), "1"));
		assertFalse(BooleanOperations.le(converter, new Float("2"), "1"));
		assertTrue(BooleanOperations.le(converter, "1", new Double("2")));
		assertTrue(BooleanOperations.le(converter, new Double("1"), "1"));
		assertFalse(BooleanOperations.le(converter, new Double("2"), "1"));
		assertTrue(BooleanOperations.le(converter, "1", new BigInteger("2")));
		assertTrue(BooleanOperations.le(converter, new BigInteger("1"), "1"));
		assertFalse(BooleanOperations.le(converter, new BigInteger("2"), "1"));
		assertTrue(BooleanOperations.le(converter, "1", new Byte("2")));
		assertTrue(BooleanOperations.le(converter, new Byte("1"), "1"));
		assertFalse(BooleanOperations.le(converter, new Byte("2"), "1"));
		assertTrue(BooleanOperations.le(converter, "1", new Short("2")));
		assertTrue(BooleanOperations.le(converter, new Short("1"), "1"));
		assertFalse(BooleanOperations.le(converter, new Short("2"), "1"));
		assertTrue(BooleanOperations.le(converter, new Character('a'), new Character('b')));
		assertTrue(BooleanOperations.le(converter, new Character('a'), new Character('a')));
		assertFalse(BooleanOperations.le(converter, new Character('b'), new Character('a')));
		assertTrue(BooleanOperations.le(converter, "1", new Integer("2")));
		assertTrue(BooleanOperations.le(converter, new Integer("1"), "1"));
		assertFalse(BooleanOperations.le(converter, new Integer("2"), "1"));
		assertTrue(BooleanOperations.le(converter, "1", new Long("2")));
		assertTrue(BooleanOperations.le(converter, new Long("1"), "1"));
		assertFalse(BooleanOperations.le(converter, new Long("2"), "1"));
		assertTrue(BooleanOperations.le(converter, "a", "b"));
		assertTrue(BooleanOperations.le(converter, "a", "a"));
		assertFalse(BooleanOperations.le(converter, "b", "a"));
		try {
			BooleanOperations.le(converter, getClass(), new Character('a'));
			fail();
		} catch (Exception e) {}
		try {
			BooleanOperations.le(converter, new Character('a'), getClass());
			fail();
		} catch (Exception e) {}
		try {
			BooleanOperations.le(converter, getClass(), new Long(0));
			fail();
		} catch (Exception e) {}
	}

	/*
	 * Test method for 'de.odysseus.el.lang.BooleanOperations.eq(Object, Object)'
	 */
	public void testEq() {
		assertTrue(BooleanOperations.eq(converter, Boolean.TRUE, Boolean.TRUE));
		assertFalse(BooleanOperations.eq(converter, null, Boolean.TRUE));
		assertFalse(BooleanOperations.eq(converter, Boolean.TRUE, null));
		assertFalse(BooleanOperations.eq(converter, "1", new BigDecimal("2")));
		assertTrue(BooleanOperations.eq(converter, new BigDecimal("1"), "1"));
		assertFalse(BooleanOperations.eq(converter, new BigDecimal("2"), "1"));
		assertFalse(BooleanOperations.eq(converter, "1", new Float("2")));
		assertTrue(BooleanOperations.eq(converter, new Float("1"), "1"));
		assertFalse(BooleanOperations.eq(converter, new Float("2"), "1"));
		assertFalse(BooleanOperations.eq(converter, "1", new Double("2")));
		assertTrue(BooleanOperations.eq(converter, new Double("1"), "1"));
		assertFalse(BooleanOperations.eq(converter, new Double("2"), "1"));
		assertFalse(BooleanOperations.eq(converter, "1", new BigInteger("2")));
		assertTrue(BooleanOperations.eq(converter, new BigInteger("1"), "1"));
		assertFalse(BooleanOperations.eq(converter, new BigInteger("2"), "1"));
		assertFalse(BooleanOperations.eq(converter, "1", new Byte("2")));
		assertTrue(BooleanOperations.eq(converter, new Byte("1"), "1"));
		assertFalse(BooleanOperations.eq(converter, new Byte("2"), "1"));
		assertFalse(BooleanOperations.eq(converter, "1", new Short("2")));
		assertTrue(BooleanOperations.eq(converter, new Short("1"), "1"));
		assertFalse(BooleanOperations.eq(converter, new Short("2"), "1"));
		assertFalse(BooleanOperations.eq(converter, new Character('a'), new Character('b')));
		assertTrue(BooleanOperations.eq(converter, new Character('a'), new Character('a')));
		assertFalse(BooleanOperations.eq(converter, new Character('b'), new Character('a')));
		assertFalse(BooleanOperations.eq(converter, "1", new Integer("2")));
		assertTrue(BooleanOperations.eq(converter, new Integer("1"), "1"));
		assertFalse(BooleanOperations.eq(converter, new Integer("2"), "1"));
		assertFalse(BooleanOperations.eq(converter, "1", new Long("2")));
		assertTrue(BooleanOperations.eq(converter, new Long("1"), "1"));
		assertFalse(BooleanOperations.eq(converter, new Long("2"), "1"));
		assertFalse(BooleanOperations.eq(converter, new Boolean(false), new Boolean(true)));
		assertTrue(BooleanOperations.eq(converter, new Boolean(true), new Boolean(true)));
		assertTrue(BooleanOperations.eq(converter, new Boolean(false), new Boolean(false)));
		assertTrue(BooleanOperations.eq(converter, Foo.BAR, "BAR"));
		assertTrue(BooleanOperations.eq(converter, "BAR", Foo.BAR));
		assertFalse(BooleanOperations.eq(converter, Foo.BAR, "BAZ"));
		try {
			BooleanOperations.eq(converter, Foo.BAR, "FOO"); // coercion fails
			fail();
		} catch (Exception e) {}
		assertFalse(BooleanOperations.eq(converter, "a", "b"));
		assertTrue(BooleanOperations.eq(converter, "a", "a"));
		assertFalse(BooleanOperations.eq(converter, "b", "a"));
		assertFalse(BooleanOperations.eq(converter, getClass(), new Character('a')));
		assertFalse(BooleanOperations.eq(converter, new Character('a'), getClass()));
		try {
			BooleanOperations.eq(converter, getClass(), new Long(0)); // coercion fails
			fail();
		} catch (Exception e) {}
	}

	/*
	 * Test method for 'de.odysseus.el.lang.BooleanOperations.ne(Object, Object)'
	 */
	public void testNe() {
		assertFalse(BooleanOperations.ne(converter, Boolean.TRUE, Boolean.TRUE));
		assertTrue(BooleanOperations.ne(converter, null, Boolean.TRUE));
		assertTrue(BooleanOperations.ne(converter, Boolean.TRUE, null));
		assertTrue(BooleanOperations.ne(converter, "1", new BigDecimal("2")));
		assertFalse(BooleanOperations.ne(converter, new BigDecimal("1"), "1"));
		assertTrue(BooleanOperations.ne(converter, new BigDecimal("2"), "1"));
		assertTrue(BooleanOperations.ne(converter, "1", new Float("2")));
		assertFalse(BooleanOperations.ne(converter, new Float("1"), "1"));
		assertTrue(BooleanOperations.ne(converter, new Float("2"), "1"));
		assertTrue(BooleanOperations.ne(converter, "1", new Double("2")));
		assertFalse(BooleanOperations.ne(converter, new Double("1"), "1"));
		assertTrue(BooleanOperations.ne(converter, new Double("2"), "1"));
		assertTrue(BooleanOperations.ne(converter, "1", new BigInteger("2")));
		assertFalse(BooleanOperations.ne(converter, new BigInteger("1"), "1"));
		assertTrue(BooleanOperations.ne(converter, new BigInteger("2"), "1"));
		assertTrue(BooleanOperations.ne(converter, "1", new Byte("2")));
		assertFalse(BooleanOperations.ne(converter, new Byte("1"), "1"));
		assertTrue(BooleanOperations.ne(converter, new Byte("2"), "1"));
		assertTrue(BooleanOperations.ne(converter, "1", new Short("2")));
		assertFalse(BooleanOperations.ne(converter, new Short("1"), "1"));
		assertTrue(BooleanOperations.ne(converter, new Short("2"), "1"));
		assertTrue(BooleanOperations.ne(converter, new Character('a'), new Character('b')));
		assertFalse(BooleanOperations.ne(converter, new Character('a'), new Character('a')));
		assertTrue(BooleanOperations.ne(converter, new Character('b'), new Character('a')));
		assertTrue(BooleanOperations.ne(converter, "1", new Integer("2")));
		assertFalse(BooleanOperations.ne(converter, new Integer("1"), "1"));
		assertTrue(BooleanOperations.ne(converter, new Integer("2"), "1"));
		assertTrue(BooleanOperations.ne(converter, "1", new Long("2")));
		assertFalse(BooleanOperations.ne(converter, new Long("1"), "1"));
		assertTrue(BooleanOperations.ne(converter, new Long("2"), "1"));
		assertTrue(BooleanOperations.ne(converter, new Boolean(false), new Boolean(true)));
		assertFalse(BooleanOperations.ne(converter, new Boolean(true), new Boolean(true)));
		assertFalse(BooleanOperations.ne(converter, new Boolean(false), new Boolean(false)));
		assertFalse(BooleanOperations.ne(converter, Foo.BAR, "BAR"));
		assertFalse(BooleanOperations.ne(converter, "BAR", Foo.BAR));
		assertTrue(BooleanOperations.ne(converter, Foo.BAR, "BAZ"));
		try {
			BooleanOperations.ne(converter, Foo.BAR, "FOO"); // coercion fails
			fail();
		} catch (Exception e) {}
		assertTrue(BooleanOperations.ne(converter, "a", "b"));
		assertFalse(BooleanOperations.ne(converter, "a", "a"));
		assertTrue(BooleanOperations.ne(converter, "b", "a"));
		assertTrue(BooleanOperations.ne(converter, getClass(), new Character('a')));
		assertTrue(BooleanOperations.ne(converter, new Character('a'), getClass()));
		try {
			BooleanOperations.ne(converter, getClass(), new Long(0)); // coercion fails
			fail();
		} catch (Exception e) {}
	}

	/*
	 * Test method for 'de.odysseus.el.lang.BooleanOperations.empty(Object)'
	 */
	public void testEmpty() {
		assertTrue(BooleanOperations.empty(converter, null));
		assertTrue(BooleanOperations.empty(converter, ""));
		assertTrue(BooleanOperations.empty(converter, new Object[0]));
		assertTrue(BooleanOperations.empty(converter, new HashMap<Object,Object>()));
		assertTrue(BooleanOperations.empty(converter, new ArrayList<Object>()));
		assertFalse(BooleanOperations.empty(converter, "foo"));
	}
}
