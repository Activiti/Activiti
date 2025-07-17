/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.core.el.juel.misc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.el.ELException;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import org.activiti.core.el.juel.test.TestCase;
import org.junit.jupiter.api.Test;

/**
 * JUnit test case for {@link TypeConverterImpl}.
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

        public void addPropertyChangeListener(
            PropertyChangeListener listener
        ) {}

        public String getAsText() {
            return value == null ? null : "" + value.getTime();
        }

        public Component getCustomEditor() {
            return null;
        }

        public String getJavaInitializationString() {
            return null;
        }

        public String[] getTags() {
            return null;
        }

        public Object getValue() {
            return value;
        }

        public boolean isPaintable() {
            return false;
        }

        public void paintValue(Graphics gfx, Rectangle box) {}

        public void removePropertyChangeListener(
            PropertyChangeListener listener
        ) {}

        public void setAsText(String text) throws IllegalArgumentException {
            value = new Date(Long.parseLong(text));
        }

        public void setValue(Object value) {
            this.value = (Date) value;
        }

        public boolean supportsCustomEditor() {
            return false;
        }
    }

    static {
        PropertyEditorManager.registerEditor(Date.class, DateEditor.class);
    }

    /**
     * Test enum type
     */
    static enum Foo {
        BAR,
        BAZ {
            @Override
            public String toString() {
                return "XXX";
            }
        },
    }

    TypeConverterImpl converter = new TypeConverterImpl();

    @Test
    public void testToBoolean() {
        assertFalse(converter.coerceToBoolean(null));
        assertFalse(converter.coerceToBoolean(""));
        assertTrue(converter.coerceToBoolean(Boolean.TRUE));
        assertFalse(converter.coerceToBoolean(Boolean.FALSE));
        assertTrue(converter.coerceToBoolean("true"));
        assertFalse(converter.coerceToBoolean("false"));
        assertFalse(converter.coerceToBoolean("yes")); // Boolean.valueOf(String) never throws an exception...
    }

    @Test
    public void testToCharacter() {
        assertEquals(
            Character.valueOf((char) 0),
            converter.coerceToCharacter(null)
        );
        assertEquals(
            Character.valueOf((char) 0),
            converter.coerceToCharacter("")
        );
        Character c = Character.valueOf((char) 99);
        assertSame(c, converter.coerceToCharacter(c));
        try {
            converter.coerceToCharacter(Boolean.TRUE);
            fail();
        } catch (ELException e) {}
        try {
            converter.coerceToCharacter(Boolean.FALSE);
            fail();
        } catch (ELException e) {}
        assertEquals(c, converter.coerceToCharacter(Byte.valueOf((byte) 99)));
        assertEquals(c, converter.coerceToCharacter(Short.valueOf((short) 99)));
        assertEquals(c, converter.coerceToCharacter(Integer.valueOf(99)));
        assertEquals(c, converter.coerceToCharacter(Long.valueOf(99)));
        assertEquals(c, converter.coerceToCharacter(Float.valueOf((float) 99.5)));
        assertEquals(c, converter.coerceToCharacter(Double.valueOf(99.5)));
        assertEquals(c, converter.coerceToCharacter(new BigDecimal("99.5")));
        assertEquals(c, converter.coerceToCharacter(new BigInteger("99")));
        assertEquals(c, converter.coerceToCharacter("c#"));
        try {
            converter.coerceToCharacter(this);
            fail();
        } catch (ELException e) {}
    }

    @Test
    public <T extends Number> void testToLong() {
        Number zero = Long.valueOf(0l);
        Number ninetynine = Long.valueOf(99l);
        assertEquals(zero, converter.coerceToLong(null));
        assertEquals(zero, converter.coerceToLong(""));
        assertEquals(
            ninetynine,
            converter.coerceToLong(Character.valueOf('c'))
        );
        assertEquals(ninetynine, converter.coerceToLong(Byte.valueOf((byte) 99)));
        assertEquals(ninetynine, converter.coerceToLong(Short.valueOf((short) 99)));
        assertEquals(ninetynine, converter.coerceToLong(Integer.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToLong(Long.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToLong(Float.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToLong(Double.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToLong(new BigDecimal(99)));
        assertEquals(ninetynine, converter.coerceToLong(new BigInteger("99")));
        assertEquals(ninetynine, converter.coerceToLong(ninetynine.toString()));
        try {
            converter.coerceToLong("foo");
            fail();
        } catch (ELException e) {}
    }

    @Test
    public <T extends Number> void testToInteger() {
        Number zero = Integer.valueOf(0);
        Number ninetynine = Integer.valueOf(99);
        assertEquals(zero, converter.coerceToInteger(null));
        assertEquals(zero, converter.coerceToInteger(""));
        assertEquals(
            ninetynine,
            converter.coerceToInteger(Character.valueOf('c'))
        );
        assertEquals(
            ninetynine,
            converter.coerceToInteger(Byte.valueOf((byte) 99))
        );
        assertEquals(
            ninetynine,
            converter.coerceToInteger(Short.valueOf((short) 99))
        );
        assertEquals(ninetynine, converter.coerceToInteger(Integer.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToInteger(Long.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToInteger(Float.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToInteger(Double.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToInteger(new BigDecimal(99)));
        assertEquals(
            ninetynine,
            converter.coerceToInteger(new BigInteger("99"))
        );
        assertEquals(
            ninetynine,
            converter.coerceToInteger(ninetynine.toString())
        );
        try {
            converter.coerceToInteger("foo");
            fail();
        } catch (ELException e) {}
    }

    @Test
    public <T extends Number> void testToShort() {
        Number zero = Short.valueOf((short) 0);
        Number ninetynine = Short.valueOf((short) 99);
        assertEquals(zero, converter.coerceToShort(null));
        assertEquals(zero, converter.coerceToShort(""));
        assertEquals(
            ninetynine,
            converter.coerceToShort(Character.valueOf('c'))
        );
        assertEquals(ninetynine, converter.coerceToShort(Byte.valueOf((byte) 99)));
        assertEquals(
            ninetynine,
            converter.coerceToShort(Short.valueOf((short) 99))
        );
        assertEquals(ninetynine, converter.coerceToShort(Integer.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToShort(Long.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToShort(Float.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToShort(Double.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToShort(new BigDecimal(99)));
        assertEquals(ninetynine, converter.coerceToShort(new BigInteger("99")));
        assertEquals(
            ninetynine,
            converter.coerceToShort(ninetynine.toString())
        );
        try {
            converter.coerceToShort("foo");
            fail();
        } catch (ELException e) {}
    }

    @Test
    public <T extends Number> void testToByte() {
        Number zero = Byte.valueOf((byte) 0);
        Number ninetynine = Byte.valueOf((byte) 99);
        assertEquals(zero, converter.coerceToByte(null));
        assertEquals(zero, converter.coerceToByte(""));
        assertEquals(
            ninetynine,
            converter.coerceToByte(Character.valueOf('c'))
        );
        assertEquals(ninetynine, converter.coerceToByte(Byte.valueOf((byte) 99)));
        assertEquals(ninetynine, converter.coerceToByte(Short.valueOf((short) 99)));
        assertEquals(ninetynine, converter.coerceToByte(Integer.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToByte(Long.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToByte(Float.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToByte(Double.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToByte(BigDecimal.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToByte(new BigInteger("99")));
        assertEquals(ninetynine, converter.coerceToByte(ninetynine.toString()));
        try {
            converter.coerceToByte("foo");
            fail();
        } catch (ELException e) {}
    }

    @Test
    public <T extends Number> void testToDouble() {
        Number zero = Double.valueOf(0);
        Number ninetynine = Double.valueOf(99);
        assertEquals(zero, converter.coerceToDouble(null));
        assertEquals(zero, converter.coerceToDouble(""));
        assertEquals(
            ninetynine,
            converter.coerceToDouble(Character.valueOf('c'))
        );
        assertEquals(ninetynine, converter.coerceToDouble( Byte.valueOf((byte) 99)));
        assertEquals(
            ninetynine,
            converter.coerceToDouble(Short.valueOf((short) 99))
        );
        assertEquals(ninetynine, converter.coerceToDouble(Integer.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToDouble(Long.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToDouble( Float.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToDouble(Double.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToDouble(new BigDecimal(99)));
        assertEquals(
            ninetynine,
            converter.coerceToDouble(new BigInteger("99"))
        );
        assertEquals(
            ninetynine,
            converter.coerceToDouble(ninetynine.toString())
        );
        try {
            converter.coerceToDouble("foo");
            fail();
        } catch (ELException e) {}
    }

    @Test
    public <T extends Number> void testToFloat() {
        Number zero = Float.valueOf(0);
        Number ninetynine = Float.valueOf(99);
        assertEquals(zero, converter.coerceToFloat(null));
        assertEquals(zero, converter.coerceToFloat(""));
        assertEquals(
            ninetynine,
            converter.coerceToFloat(Character.valueOf('c'))
        );
        assertEquals(ninetynine, converter.coerceToFloat(Byte.valueOf((byte) 99)));
        assertEquals(
            ninetynine,
            converter.coerceToFloat(Short.valueOf((short) 99))
        );
        assertEquals(ninetynine, converter.coerceToFloat(Integer.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToFloat( Long.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToFloat( Float.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToFloat(Double.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToFloat(new BigDecimal(99)));
        assertEquals(ninetynine, converter.coerceToFloat(new BigInteger("99")));
        assertEquals(
            ninetynine,
            converter.coerceToFloat(ninetynine.toString())
        );
        try {
            converter.coerceToFloat("foo");
            fail();
        } catch (ELException e) {}
    }

    @Test
    public <T extends Number> void testToBigDecimal() {
        Number zero = BigDecimal.valueOf(0);
        Number ninetynine = BigDecimal.valueOf(99);
        assertEquals(zero, converter.coerceToBigDecimal(null));
        assertEquals(zero, converter.coerceToBigDecimal(""));
        assertEquals(
            ninetynine,
            converter.coerceToBigDecimal(Character.valueOf('c'))
        );
        assertEquals(
            ninetynine,
            converter.coerceToBigDecimal(Byte.valueOf((byte) 99))
        );
        assertEquals(
            ninetynine,
            converter.coerceToBigDecimal(Short.valueOf((short) 99))
        );
        assertEquals(ninetynine, converter.coerceToBigDecimal(Integer.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToBigDecimal(Long.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToBigDecimal(Float.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToBigDecimal(Double.valueOf(99)));
        assertEquals(
            ninetynine,
            converter.coerceToBigDecimal(new BigDecimal(99))
        );
        assertEquals(
            ninetynine,
            converter.coerceToBigDecimal(new BigInteger("99"))
        );
        assertEquals(
            ninetynine,
            converter.coerceToBigDecimal(ninetynine.toString())
        );
        try {
            converter.coerceToBigDecimal("foo");
            fail();
        } catch (ELException e) {}
    }

    @Test
    public <T extends Number> void testToBigInteger() {
        Number zero = BigInteger.valueOf(0);
        Number ninetynine = BigInteger.valueOf(99);
        assertEquals(zero, converter.coerceToBigInteger(null));
        assertEquals(zero, converter.coerceToBigInteger(""));
        assertEquals(
            ninetynine,
            converter.coerceToBigInteger(Character.valueOf('c'))
        );
        assertEquals(
            ninetynine,
            converter.coerceToBigInteger(Byte.valueOf((byte) 99))
        );
        assertEquals(
            ninetynine,
            converter.coerceToBigInteger(Short.valueOf((short) 99))
        );
        assertEquals(ninetynine, converter.coerceToBigInteger(Integer.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToBigInteger(Long.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToBigInteger(Float.valueOf(99)));
        assertEquals(ninetynine, converter.coerceToBigInteger(Double.valueOf(99)));
        assertEquals(
            ninetynine,
            converter.coerceToBigInteger(new BigDecimal(99))
        );
        assertEquals(
            ninetynine,
            converter.coerceToBigInteger(new BigInteger("99"))
        );
        assertEquals(
            ninetynine,
            converter.coerceToBigInteger(ninetynine.toString())
        );
        try {
            converter.coerceToBigInteger("foo");
            fail();
        } catch (ELException e) {}
    }

    @Test
    public void testToString() {
        assertSame("foo", converter.coerceToString("foo"));
        assertEquals("", converter.coerceToString(null));
        assertEquals(Foo.BAR.name(), converter.coerceToString(Foo.BAR));
        Object value = new BigDecimal("99.345");
        assertEquals(value.toString(), converter.coerceToString(value));
    }

    @Test
    public void testToEnum() {
        assertNull(converter.coerceToEnum(null, Foo.class));
        assertSame(Foo.BAR, converter.coerceToEnum(Foo.BAR, Foo.class));
        assertNull(converter.coerceToEnum("", Foo.class));
        assertSame(Foo.BAR, converter.coerceToEnum("BAR", Foo.class));
        assertSame(Foo.BAZ, converter.coerceToEnum("BAZ", Foo.class));
    }

    @Test
    public void testToType() {
        assertEquals("foo", converter.coerceToType("foo", String.class));
        assertEquals(Long.valueOf(0), converter.coerceToType("0", Long.class));
        assertEquals(
            Character.valueOf('c'),
            converter.coerceToType("c", Character.class)
        );
        assertEquals(
            Boolean.TRUE,
            converter.coerceToType("true", Boolean.class)
        );
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
        assertEquals((byte) 0, converter.coerceToType("0", byte.class));
        assertEquals((short) 0, converter.coerceToType("0", short.class));
        assertEquals(0, converter.coerceToType("0", int.class));
        assertEquals((long) 0, converter.coerceToType("0", long.class));
        assertEquals((float) 0, converter.coerceToType("0", float.class));
        assertEquals((double) 0, converter.coerceToType("0", double.class));
        assertEquals('0', converter.coerceToType("0", char.class));
    }
}
