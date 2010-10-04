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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.activiti.el.juel.misc.TypeConverterImpl;
import org.activiti.javax.el.ELException;

import junit.framework.TestCase;

/**
 * JUnit test case for {@link org.activiti.el.juel.misc.TypeConverterImpl}.
 *
 * @author Christoph Beck
 */
public class TypeConverterImplTest extends TestCase {
	/**
	 * Test property editor for date objects.
	 * Accepts integer strings as text input and uses them as time value in milliseconds.
	 */
	public static class DateEditor implements PropertyEditor {
		private Date value;
		public void addPropertyChangeListener(PropertyChangeListener listener) {}
		public String getAsText() { return value == null ? null : "" + value.getTime(); }
		public Component getCustomEditor() { return null; }
		public String getJavaInitializationString() { return null; }
		public String[] getTags() { return null; }
		public Object getValue() { return value; }
		public boolean isPaintable() { return false; }
		public void paintValue(Graphics gfx, Rectangle box) {}
		public void removePropertyChangeListener(PropertyChangeListener listener) {}
		public void setAsText(String text) throws IllegalArgumentException { value = new Date(Long.parseLong(text)); }
		public void setValue(Object value) { this.value = (Date)value; }
		public boolean supportsCustomEditor() { return false; }
	}

	static {
		PropertyEditorManager.registerEditor(Date.class, DateEditor.class);
	}
	
	/**
	 * Test enum type
	 */
	static enum Foo { BAR };
	
	TypeConverterImpl converter = new TypeConverterImpl();

	public void testToBoolean() {
		assertFalse(converter.coerceToBoolean(null));
		assertFalse(converter.coerceToBoolean(""));
		assertTrue(converter.coerceToBoolean(Boolean.TRUE));
		assertFalse(converter.coerceToBoolean(Boolean.FALSE));
		assertTrue(converter.coerceToBoolean("true"));
		assertFalse(converter.coerceToBoolean("false"));
		assertFalse(converter.coerceToBoolean("yes")); // Boolean.valueOf(String) never throws an exception...
	}

	public void testToCharacter() {
		assertEquals(Character.valueOf((char)0), converter.coerceToCharacter(null));
		assertEquals(Character.valueOf((char)0), converter.coerceToCharacter(""));
		Character c = Character.valueOf((char)99);
		assertSame(c, converter.coerceToCharacter(c));
		try {
			converter.coerceToCharacter(Boolean.TRUE);
			fail();
		} catch (ELException e) {}
		try {
			converter.coerceToCharacter(Boolean.FALSE);
			fail();
		} catch (ELException e) {}
		assertEquals(c, converter.coerceToCharacter(new Byte((byte)99)));
		assertEquals(c, converter.coerceToCharacter(new Short((short)99)));
		assertEquals(c, converter.coerceToCharacter(new Integer(99)));
		assertEquals(c, converter.coerceToCharacter(new Long(99)));
		assertEquals(c, converter.coerceToCharacter(new Float((float)99.5)));
		assertEquals(c, converter.coerceToCharacter(new Double(99.5)));
		assertEquals(c, converter.coerceToCharacter(new BigDecimal("99.5")));
		assertEquals(c, converter.coerceToCharacter(new BigInteger("99")));
		assertEquals(c, converter.coerceToCharacter("c#"));
		try {
			converter.coerceToCharacter(this);
			fail();
		} catch (ELException e) {}
	}

	public <T extends Number> void testToLong() {
		Number zero = Long.valueOf(0l);
		Number ninetynine = Long.valueOf(99l);
		assertEquals(zero, converter.coerceToLong(null));
		assertEquals(zero, converter.coerceToLong(""));
		assertEquals(ninetynine, converter.coerceToLong(Character.valueOf('c')));
		assertEquals(ninetynine, converter.coerceToLong(new Byte((byte)99)));
		assertEquals(ninetynine, converter.coerceToLong(new Short((short)99)));
		assertEquals(ninetynine, converter.coerceToLong(new Integer(99)));
		assertEquals(ninetynine, converter.coerceToLong(new Long(99)));
		assertEquals(ninetynine, converter.coerceToLong(new Float(99)));
		assertEquals(ninetynine, converter.coerceToLong(new Double(99)));
		assertEquals(ninetynine, converter.coerceToLong(new BigDecimal(99)));
		assertEquals(ninetynine, converter.coerceToLong(new BigInteger("99")));
		assertEquals(ninetynine, converter.coerceToLong(ninetynine.toString()));
		try {
			converter.coerceToLong("foo");
			fail();
		} catch (ELException e) {
		}
	}

	public <T extends Number> void testToInteger() {
		Number zero = Integer.valueOf(0);
		Number ninetynine = Integer.valueOf(99);
		assertEquals(zero, converter.coerceToInteger(null));
		assertEquals(zero, converter.coerceToInteger(""));
		assertEquals(ninetynine, converter.coerceToInteger(Character.valueOf('c')));
		assertEquals(ninetynine, converter.coerceToInteger(new Byte((byte)99)));
		assertEquals(ninetynine, converter.coerceToInteger(new Short((short)99)));
		assertEquals(ninetynine, converter.coerceToInteger(new Integer(99)));
		assertEquals(ninetynine, converter.coerceToInteger(new Long(99)));
		assertEquals(ninetynine, converter.coerceToInteger(new Float(99)));
		assertEquals(ninetynine, converter.coerceToInteger(new Double(99)));
		assertEquals(ninetynine, converter.coerceToInteger(new BigDecimal(99)));
		assertEquals(ninetynine, converter.coerceToInteger(new BigInteger("99")));
		assertEquals(ninetynine, converter.coerceToInteger(ninetynine.toString()));
		try {
			converter.coerceToInteger("foo");
			fail();
		} catch (ELException e) {
		}
	}

	public <T extends Number> void testToShort() {
		Number zero = Short.valueOf((short)0);
		Number ninetynine = Short.valueOf((short)99);
		assertEquals(zero, converter.coerceToShort(null));
		assertEquals(zero, converter.coerceToShort(""));
		assertEquals(ninetynine, converter.coerceToShort(Character.valueOf('c')));
		assertEquals(ninetynine, converter.coerceToShort(new Byte((byte)99)));
		assertEquals(ninetynine, converter.coerceToShort(new Short((short)99)));
		assertEquals(ninetynine, converter.coerceToShort(new Integer(99)));
		assertEquals(ninetynine, converter.coerceToShort(new Long(99)));
		assertEquals(ninetynine, converter.coerceToShort(new Float(99)));
		assertEquals(ninetynine, converter.coerceToShort(new Double(99)));
		assertEquals(ninetynine, converter.coerceToShort(new BigDecimal(99)));
		assertEquals(ninetynine, converter.coerceToShort(new BigInteger("99")));
		assertEquals(ninetynine, converter.coerceToShort(ninetynine.toString()));
		try {
			converter.coerceToShort("foo");
			fail();
		} catch (ELException e) {
		}
	}

	public <T extends Number> void testToByte() {
		Number zero = Byte.valueOf((byte)0);
		Number ninetynine = Byte.valueOf((byte)99);
		assertEquals(zero, converter.coerceToByte(null));
		assertEquals(zero, converter.coerceToByte(""));
		assertEquals(ninetynine, converter.coerceToByte(Character.valueOf('c')));
		assertEquals(ninetynine, converter.coerceToByte(new Byte((byte)99)));
		assertEquals(ninetynine, converter.coerceToByte(new Short((short)99)));
		assertEquals(ninetynine, converter.coerceToByte(new Integer(99)));
		assertEquals(ninetynine, converter.coerceToByte(new Long(99)));
		assertEquals(ninetynine, converter.coerceToByte(new Float(99)));
		assertEquals(ninetynine, converter.coerceToByte(new Double(99)));
		assertEquals(ninetynine, converter.coerceToByte(new BigDecimal(99)));
		assertEquals(ninetynine, converter.coerceToByte(new BigInteger("99")));
		assertEquals(ninetynine, converter.coerceToByte(ninetynine.toString()));
		try {
			converter.coerceToByte("foo");
			fail();
		} catch (ELException e) {
		}
	}

	public <T extends Number> void testToDouble() {
		Number zero = Double.valueOf(0);
		Number ninetynine = Double.valueOf(99);
		assertEquals(zero, converter.coerceToDouble(null));
		assertEquals(zero, converter.coerceToDouble(""));
		assertEquals(ninetynine, converter.coerceToDouble(Character.valueOf('c')));
		assertEquals(ninetynine, converter.coerceToDouble(new Byte((byte)99)));
		assertEquals(ninetynine, converter.coerceToDouble(new Short((short)99)));
		assertEquals(ninetynine, converter.coerceToDouble(new Integer(99)));
		assertEquals(ninetynine, converter.coerceToDouble(new Long(99)));
		assertEquals(ninetynine, converter.coerceToDouble(new Float(99)));
		assertEquals(ninetynine, converter.coerceToDouble(new Double(99)));
		assertEquals(ninetynine, converter.coerceToDouble(new BigDecimal(99)));
		assertEquals(ninetynine, converter.coerceToDouble(new BigInteger("99")));
		assertEquals(ninetynine, converter.coerceToDouble(ninetynine.toString()));
		try {
			converter.coerceToDouble("foo");
			fail();
		} catch (ELException e) {
		}
	}

	public <T extends Number> void testToFloat() {
		Number zero = Float.valueOf(0);
		Number ninetynine = Float.valueOf(99);
		assertEquals(zero, converter.coerceToFloat(null));
		assertEquals(zero, converter.coerceToFloat(""));
		assertEquals(ninetynine, converter.coerceToFloat(Character.valueOf('c')));
		assertEquals(ninetynine, converter.coerceToFloat(new Byte((byte)99)));
		assertEquals(ninetynine, converter.coerceToFloat(new Short((short)99)));
		assertEquals(ninetynine, converter.coerceToFloat(new Integer(99)));
		assertEquals(ninetynine, converter.coerceToFloat(new Long(99)));
		assertEquals(ninetynine, converter.coerceToFloat(new Float(99)));
		assertEquals(ninetynine, converter.coerceToFloat(new Double(99)));
		assertEquals(ninetynine, converter.coerceToFloat(new BigDecimal(99)));
		assertEquals(ninetynine, converter.coerceToFloat(new BigInteger("99")));
		assertEquals(ninetynine, converter.coerceToFloat(ninetynine.toString()));
		try {
			converter.coerceToFloat("foo");
			fail();
		} catch (ELException e) {
		}
	}

	public <T extends Number> void testToBigDecimal() {
		Number zero = BigDecimal.valueOf(0);
		Number ninetynine = BigDecimal.valueOf(99);
		assertEquals(zero, converter.coerceToBigDecimal(null));
		assertEquals(zero, converter.coerceToBigDecimal(""));
		assertEquals(ninetynine, converter.coerceToBigDecimal(Character.valueOf('c')));
		assertEquals(ninetynine, converter.coerceToBigDecimal(new Byte((byte)99)));
		assertEquals(ninetynine, converter.coerceToBigDecimal(new Short((short)99)));
		assertEquals(ninetynine, converter.coerceToBigDecimal(new Integer(99)));
		assertEquals(ninetynine, converter.coerceToBigDecimal(new Long(99)));
		assertEquals(ninetynine, converter.coerceToBigDecimal(new Float(99)));
		assertEquals(ninetynine, converter.coerceToBigDecimal(new Double(99)));
		assertEquals(ninetynine, converter.coerceToBigDecimal(new BigDecimal(99)));
		assertEquals(ninetynine, converter.coerceToBigDecimal(new BigInteger("99")));
		assertEquals(ninetynine, converter.coerceToBigDecimal(ninetynine.toString()));
		try {
			converter.coerceToBigDecimal("foo");
			fail();
		} catch (ELException e) {
		}
	}

	public <T extends Number> void testToBigInteger() {
		Number zero = BigInteger.valueOf(0);
		Number ninetynine = BigInteger.valueOf(99);
		assertEquals(zero, converter.coerceToBigInteger(null));
		assertEquals(zero, converter.coerceToBigInteger(""));
		assertEquals(ninetynine, converter.coerceToBigInteger(Character.valueOf('c')));
		assertEquals(ninetynine, converter.coerceToBigInteger(new Byte((byte)99)));
		assertEquals(ninetynine, converter.coerceToBigInteger(new Short((short)99)));
		assertEquals(ninetynine, converter.coerceToBigInteger(new Integer(99)));
		assertEquals(ninetynine, converter.coerceToBigInteger(new Long(99)));
		assertEquals(ninetynine, converter.coerceToBigInteger(new Float(99)));
		assertEquals(ninetynine, converter.coerceToBigInteger(new Double(99)));
		assertEquals(ninetynine, converter.coerceToBigInteger(new BigDecimal(99)));
		assertEquals(ninetynine, converter.coerceToBigInteger(new BigInteger("99")));
		assertEquals(ninetynine, converter.coerceToBigInteger(ninetynine.toString()));
		try {
			converter.coerceToBigInteger("foo");
			fail();
		} catch (ELException e) {
		}
	}

	public void testToString() {
		assertSame("foo", converter.coerceToString("foo"));
		assertEquals("", converter.coerceToString(null));
		assertEquals(Foo.BAR.name(), converter.coerceToString(Foo.BAR));
		Object value = new BigDecimal("99.345");
		assertEquals(value.toString(), converter.coerceToString(value));
	}

	public void testToEnum() {
		assertNull(converter.coerceToEnum(null, Foo.class));
		assertSame(Foo.BAR, converter.coerceToEnum(Foo.BAR, Foo.class));
		assertNull(converter.coerceToEnum("", Foo.class));
		assertSame(Foo.BAR, converter.coerceToEnum("BAR", Foo.class));
	}

	public void testToType() {
		assertEquals("foo", converter.coerceToType("foo", String.class));
		assertEquals(new Long(0), converter.coerceToType("0", Long.class));
		assertEquals(new Character('c'), converter.coerceToType("c", Character.class));
		assertEquals(Boolean.TRUE, converter.coerceToType("true", Boolean.class));
		assertEquals(Foo.BAR, converter.coerceToType("BAR", Foo.class));
		// other types
		assertNull(converter.coerceToType(null, Object.class));
		Object value = new Date(0);
		assertSame(value, converter.coerceToType(value, Object.class));
		assertEquals(new Date(0), converter.coerceToType("0", Date.class));
		assertNull(converter.coerceToType("", Date.class));
		try {
			converter.coerceToType("foo", Date.class);
			fail();
		} catch (Exception e) {}
		assertNull(converter.coerceToType("", getClass()));
		try {
			converter.coerceToType("bar", getClass());
			fail();
		} catch (Exception e) {}
		assertEquals(false, converter.coerceToType("false", boolean.class));
		assertEquals((byte)0, converter.coerceToType("0", byte.class));
		assertEquals((short)0, converter.coerceToType("0", short.class));
		assertEquals(0, converter.coerceToType("0", int.class));
		assertEquals((long)0, converter.coerceToType("0", long.class));
		assertEquals((float)0, converter.coerceToType("0", float.class));
		assertEquals((double)0, converter.coerceToType("0", double.class));
		assertEquals('0', converter.coerceToType("0", char.class));
	}
}
