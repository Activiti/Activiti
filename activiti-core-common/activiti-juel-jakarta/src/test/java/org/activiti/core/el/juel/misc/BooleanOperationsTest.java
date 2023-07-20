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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import org.activiti.core.el.juel.test.TestCase;
import org.junit.jupiter.api.Test;

public class BooleanOperationsTest extends TestCase {

    /**
     * Test enum type
     */
    static enum Foo {
        BAR,
        BAZ,
    }

    private TypeConverter converter = TypeConverter.DEFAULT;

    /*
     * Test method for 'org.activiti.core.el.juel.BooleanOperations.lt(Object, Object)'
     */
    @Test
    public void testLt() {
        assertFalse(
            BooleanOperations.lt(converter, Boolean.TRUE, Boolean.TRUE)
        );
        assertFalse(BooleanOperations.lt(converter, null, Boolean.TRUE));
        assertFalse(BooleanOperations.lt(converter, Boolean.TRUE, null));
        assertTrue(BooleanOperations.lt(converter, "1", new BigDecimal("2")));
        assertFalse(BooleanOperations.lt(converter, new BigDecimal("1"), "1"));
        assertFalse(BooleanOperations.lt(converter, new BigDecimal("2"), "1"));
        assertTrue(BooleanOperations.lt(converter, "1",  Float.valueOf("2")));
        assertFalse(BooleanOperations.lt(converter, Float.valueOf("1"), "1"));
        assertFalse(BooleanOperations.lt(converter, Float.valueOf("2"), "1"));
        assertTrue(BooleanOperations.lt(converter, "1", Double.valueOf("2")));
        assertFalse(BooleanOperations.lt(converter, Double.valueOf("1"), "1"));
        assertFalse(BooleanOperations.lt(converter, Double.valueOf("2"), "1"));
        assertTrue(BooleanOperations.lt(converter, "1", new BigInteger("2")));
        assertFalse(BooleanOperations.lt(converter, new BigInteger("1"), "1"));
        assertFalse(BooleanOperations.lt(converter, new BigInteger("2"), "1"));
        assertTrue(BooleanOperations.lt(converter, "1", Byte.valueOf("2")));
        assertFalse(BooleanOperations.lt(converter, Byte.valueOf("1"), "1"));
        assertFalse(BooleanOperations.lt(converter, Byte.valueOf("2"), "1"));
        assertTrue(BooleanOperations.lt(converter, "1", Short.valueOf("2")));
        assertFalse(BooleanOperations.lt(converter, Short.valueOf("1"), "1"));
        assertFalse(BooleanOperations.lt(converter, Short.valueOf("2"), "1"));
        assertTrue(
            BooleanOperations.lt(
                converter,
                Character.valueOf('a'),
                Character.valueOf('b')
            )
        );
        assertFalse(
            BooleanOperations.lt(
                converter,
                Character.valueOf('a'),
                Character.valueOf('a')
            )
        );
        assertFalse(
            BooleanOperations.lt(
                converter,
                Character.valueOf('b'),
                Character.valueOf('a')
            )
        );
        assertTrue(BooleanOperations.lt(converter, "1", Integer.valueOf("2")));
        assertFalse(BooleanOperations.lt(converter, Integer.valueOf("1"), "1"));
        assertFalse(BooleanOperations.lt(converter, Integer.valueOf("2"), "1"));
        assertTrue(BooleanOperations.lt(converter, "1", Long.valueOf("2")));
        assertFalse(BooleanOperations.lt(converter, Long.valueOf("1"), "1"));
        assertFalse(BooleanOperations.lt(converter, Long.valueOf("2"), "1"));
        assertTrue(BooleanOperations.lt(converter, "a", "b"));
        assertFalse(BooleanOperations.lt(converter, "a", "a"));
        assertFalse(BooleanOperations.lt(converter, "b", "a"));
        try {
            BooleanOperations.lt(converter, getClass(), Character.valueOf('a'));
            fail();
        } catch (Exception e) {}
        try {
            BooleanOperations.lt(converter, Character.valueOf('a'), getClass());
            fail();
        } catch (Exception e) {}
        try {
            BooleanOperations.lt(converter, getClass(),Long.valueOf(0));
            fail();
        } catch (Exception e) {}
    }

    /*
     * Test method for 'org.activiti.core.el.juel.BooleanOperations.gt(Object, Object)'
     */
    @Test
    public void testGt() {
        assertFalse(
            BooleanOperations.gt(converter, Boolean.TRUE, Boolean.TRUE)
        );
        assertFalse(BooleanOperations.gt(converter, null, Boolean.TRUE));
        assertFalse(BooleanOperations.gt(converter, Boolean.TRUE, null));
        assertFalse(BooleanOperations.gt(converter, "1", new BigDecimal("2")));
        assertFalse(BooleanOperations.gt(converter, new BigDecimal("1"), "1"));
        assertTrue(BooleanOperations.gt(converter, new BigDecimal("2"), "1"));
        assertFalse(BooleanOperations.gt(converter, "1", Float.valueOf("2")));
        assertFalse(BooleanOperations.gt(converter, Float.valueOf("1"), "1"));
        assertTrue(BooleanOperations.gt(converter, Float.valueOf("2"), "1"));
        assertFalse(BooleanOperations.gt(converter, "1", Double.valueOf("2")));
        assertFalse(BooleanOperations.gt(converter, Double.valueOf("1"), "1"));
        assertTrue(BooleanOperations.gt(converter, Double.valueOf("2"), "1"));
        assertFalse(BooleanOperations.gt(converter, "1", new BigInteger("2")));
        assertFalse(BooleanOperations.gt(converter, new BigInteger("1"), "1"));
        assertTrue(BooleanOperations.gt(converter, new BigInteger("2"), "1"));
        assertFalse(BooleanOperations.gt(converter, "1", Byte.valueOf("2")));
        assertFalse(BooleanOperations.gt(converter, Byte.valueOf("1"), "1"));
        assertTrue(BooleanOperations.gt(converter, Byte.valueOf("2"), "1"));
        assertFalse(BooleanOperations.gt(converter, "1",Short.valueOf("2")));
        assertFalse(BooleanOperations.gt(converter, Short.valueOf("1"), "1"));
        assertTrue(BooleanOperations.gt(converter, Short.valueOf("2"), "1"));
        assertFalse(
            BooleanOperations.gt(
                converter,
                Character.valueOf('a'),
                Character.valueOf('b')
            )
        );
        assertFalse(
            BooleanOperations.gt(
                converter,
                Character.valueOf('a'),
                Character.valueOf('a')
            )
        );
        assertTrue(
            BooleanOperations.gt(
                converter,
                Character.valueOf('b'),
                Character.valueOf('a')
            )
        );
        assertFalse(BooleanOperations.gt(converter, "1", Integer.valueOf("2")));
        assertFalse(BooleanOperations.gt(converter, Integer.valueOf("1"), "1"));
        assertTrue(BooleanOperations.gt(converter, Integer.valueOf("2"), "1"));
        assertFalse(BooleanOperations.gt(converter, "1", Long.valueOf("2")));
        assertFalse(BooleanOperations.gt(converter, Long.valueOf("1"), "1"));
        assertTrue(BooleanOperations.gt(converter, Long.valueOf("2"), "1"));
        assertFalse(BooleanOperations.gt(converter, "a", "b"));
        assertFalse(BooleanOperations.gt(converter, "a", "a"));
        assertTrue(BooleanOperations.gt(converter, "b", "a"));
        try {
            BooleanOperations.gt(converter, getClass(), Character.valueOf('a'));
            fail();
        } catch (Exception e) {}
        try {
            BooleanOperations.gt(converter, Character.valueOf('a'), getClass());
            fail();
        } catch (Exception e) {}
        try {
            BooleanOperations.gt(converter, getClass(), Long.valueOf(0));
            fail();
        } catch (Exception e) {}
    }

    /*
     * Test method for 'org.activiti.core.el.juel.BooleanOperations.ge(Object, Object)'
     */
    @Test
    public void testGe() {
        assertTrue(BooleanOperations.ge(converter, Boolean.TRUE, Boolean.TRUE));
        assertFalse(BooleanOperations.ge(converter, null, Boolean.TRUE));
        assertFalse(BooleanOperations.ge(converter, Boolean.TRUE, null));
        assertFalse(BooleanOperations.ge(converter, "1", new BigDecimal("2")));
        assertTrue(BooleanOperations.ge(converter, new BigDecimal("1"), "1"));
        assertTrue(BooleanOperations.ge(converter, new BigDecimal("2"), "1"));
        assertFalse(BooleanOperations.ge(converter, "1", Float.valueOf("2")));
        assertTrue(BooleanOperations.ge(converter, Float.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ge(converter, Float.valueOf("2"), "1"));
        assertFalse(BooleanOperations.ge(converter, "1",  Double.valueOf("2")));
        assertTrue(BooleanOperations.ge(converter, Double.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ge(converter, Double.valueOf("2"), "1"));
        assertFalse(BooleanOperations.ge(converter, "1", new BigInteger("2")));
        assertTrue(BooleanOperations.ge(converter, new BigInteger("1"), "1"));
        assertTrue(BooleanOperations.ge(converter, new BigInteger("2"), "1"));
        assertFalse(BooleanOperations.ge(converter, "1", Byte.valueOf("2")));
        assertTrue(BooleanOperations.ge(converter, Byte.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ge(converter, Byte.valueOf("2"), "1"));
        assertFalse(BooleanOperations.ge(converter, "1", Short.valueOf("2")));
        assertTrue(BooleanOperations.ge(converter, Short.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ge(converter, Short.valueOf("2"), "1"));
        assertFalse(
            BooleanOperations.ge(
                converter,
                Character.valueOf('a'),
                Character.valueOf('b')
            )
        );
        assertTrue(
            BooleanOperations.ge(
                converter,
                Character.valueOf('a'),
                Character.valueOf('a')
            )
        );
        assertTrue(
            BooleanOperations.ge(
                converter,
                Character.valueOf('b'),
                Character.valueOf('a')
            )
        );
        assertFalse(BooleanOperations.ge(converter, "1", Integer.valueOf("2")));
        assertTrue(BooleanOperations.ge(converter, Integer.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ge(converter, Integer.valueOf("2"), "1"));
        assertFalse(BooleanOperations.ge(converter, "1",Long.valueOf("2")));
        assertTrue(BooleanOperations.ge(converter, Long.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ge(converter, Long.valueOf("2"), "1"));
        assertFalse(BooleanOperations.ge(converter, "a", "b"));
        assertTrue(BooleanOperations.ge(converter, "a", "a"));
        assertTrue(BooleanOperations.ge(converter, "b", "a"));
        try {
            BooleanOperations.ge(converter, getClass(), Character.valueOf('a'));
            fail();
        } catch (Exception e) {}
        try {
            BooleanOperations.ge(converter, Character.valueOf('a'), getClass());
            fail();
        } catch (Exception e) {}
        try {
            BooleanOperations.ge(converter, getClass(), Long.valueOf(0));
            fail();
        } catch (Exception e) {}
    }

    /*
     * Test method for 'org.activiti.core.el.juel.BooleanOperations.le(Object, Object)'
     */
    @Test
    public void testLe() {
        assertTrue(BooleanOperations.le(converter, Boolean.TRUE, Boolean.TRUE));
        assertFalse(BooleanOperations.le(converter, null, Boolean.TRUE));
        assertFalse(BooleanOperations.le(converter, Boolean.TRUE, null));
        assertTrue(BooleanOperations.le(converter, "1", new BigDecimal("2")));
        assertTrue(BooleanOperations.le(converter, new BigDecimal("1"), "1"));
        assertFalse(BooleanOperations.le(converter, new BigDecimal("2"), "1"));
        assertTrue(BooleanOperations.le(converter, "1", Float.valueOf("2")));
        assertTrue(BooleanOperations.le(converter, Float.valueOf("1"), "1"));
        assertFalse(BooleanOperations.le(converter, Float.valueOf("2"), "1"));
        assertTrue(BooleanOperations.le(converter, "1", Double.valueOf("2")));
        assertTrue(BooleanOperations.le(converter, Double.valueOf("1"), "1"));
        assertFalse(BooleanOperations.le(converter, Double.valueOf("2"), "1"));
        assertTrue(BooleanOperations.le(converter, "1", new BigInteger("2")));
        assertTrue(BooleanOperations.le(converter, new BigInteger("1"), "1"));
        assertFalse(BooleanOperations.le(converter, new BigInteger("2"), "1"));
        assertTrue(BooleanOperations.le(converter, "1", Byte.valueOf("2")));
        assertTrue(BooleanOperations.le(converter, Byte.valueOf("1"), "1"));
        assertFalse(BooleanOperations.le(converter, Byte.valueOf("2"), "1"));
        assertTrue(BooleanOperations.le(converter, "1",  Short.valueOf("2")));
        assertTrue(BooleanOperations.le(converter, Short.valueOf("1"), "1"));
        assertFalse(BooleanOperations.le(converter, Short.valueOf("2"), "1"));
        assertTrue(
            BooleanOperations.le(
                converter,
               Character.valueOf('a'),
                 Character.valueOf('b')
            )
        );
        assertTrue(
            BooleanOperations.le(
                converter,
               Character.valueOf('a'),
                 Character.valueOf('a')
            )
        );
        assertFalse(
            BooleanOperations.le(
                converter,
                Character.valueOf('b'),
                 Character.valueOf('a')
            )
        );
        assertTrue(BooleanOperations.le(converter, "1", Integer.valueOf("2")));
        assertTrue(BooleanOperations.le(converter, Integer.valueOf("1"), "1"));
        assertFalse(BooleanOperations.le(converter, Integer.valueOf("2"), "1"));
        assertTrue(BooleanOperations.le(converter, "1", Long.valueOf("2")));
        assertTrue(BooleanOperations.le(converter, Long.valueOf("1"), "1"));
        assertFalse(BooleanOperations.le(converter,Long.valueOf("2"), "1"));
        assertTrue(BooleanOperations.le(converter, "a", "b"));
        assertTrue(BooleanOperations.le(converter, "a", "a"));
        assertFalse(BooleanOperations.le(converter, "b", "a"));
        try {
            BooleanOperations.le(converter, getClass(), Character.valueOf('a'));
            fail();
        } catch (Exception e) {}
        try {
            BooleanOperations.le(converter, Character.valueOf('a'), getClass());
            fail();
        } catch (Exception e) {}
        try {
            BooleanOperations.le(converter, getClass(), Long.valueOf(0));
            fail();
        } catch (Exception e) {}
    }

    /*
     * Test method for 'org.activiti.core.el.juel.BooleanOperations.eq(Object, Object)'
     */
    @Test
    public void testEq() {
        assertTrue(BooleanOperations.eq(converter, Boolean.TRUE, Boolean.TRUE));
        assertFalse(BooleanOperations.eq(converter, null, Boolean.TRUE));
        assertFalse(BooleanOperations.eq(converter, Boolean.TRUE, null));
        assertFalse(BooleanOperations.eq(converter, "1", new BigDecimal("2")));
        assertTrue(BooleanOperations.eq(converter, new BigDecimal("1"), "1"));
        assertFalse(BooleanOperations.eq(converter, new BigDecimal("2"), "1"));
        assertFalse(BooleanOperations.eq(converter, "1",  Float.valueOf("2")));
        assertTrue(BooleanOperations.eq(converter, Float.valueOf("1"), "1"));
        assertFalse(BooleanOperations.eq(converter, Float.valueOf("2"), "1"));
        assertFalse(BooleanOperations.eq(converter, "1", Double.valueOf("2")));
        assertTrue(BooleanOperations.eq(converter, Double.valueOf("1"), "1"));
        assertFalse(BooleanOperations.eq(converter,  Double.valueOf("2"), "1"));
        assertFalse(BooleanOperations.eq(converter, "1", new BigInteger("2")));
        assertTrue(BooleanOperations.eq(converter, new BigInteger("1"), "1"));
        assertFalse(BooleanOperations.eq(converter, new BigInteger("2"), "1"));
        assertFalse(BooleanOperations.eq(converter, "1", Byte.valueOf("2")));
        assertTrue(BooleanOperations.eq(converter, Byte.valueOf("1"), "1"));
        assertFalse(BooleanOperations.eq(converter,  Byte.valueOf("2"), "1"));
        assertFalse(BooleanOperations.eq(converter, "1",  Short.valueOf("2")));
        assertTrue(BooleanOperations.eq(converter,  Short.valueOf("1"), "1"));
        assertFalse(BooleanOperations.eq(converter,  Short.valueOf("2"), "1"));
        assertFalse(
            BooleanOperations.eq(
                converter,
                Character.valueOf('a'),
                 Character.valueOf('b')
            )
        );
        assertTrue(
            BooleanOperations.eq(
                converter,
                 Character.valueOf('a'),
                Character.valueOf('a')
            )
        );
        assertFalse(
            BooleanOperations.eq(
                converter,
                 Character.valueOf('b'),
                 Character.valueOf('a')
            )
        );
        assertFalse(BooleanOperations.eq(converter, "1", Integer.valueOf("2")));
        assertTrue(BooleanOperations.eq(converter, Integer.valueOf("1"), "1"));
        assertFalse(BooleanOperations.eq(converter, Integer.valueOf("2"), "1"));
        assertFalse(BooleanOperations.eq(converter, "1",Long.valueOf("2")));
        assertTrue(BooleanOperations.eq(converter,  Long.valueOf("1"), "1"));
        assertFalse(BooleanOperations.eq(converter, Long.valueOf("2"), "1"));
        assertFalse(
            BooleanOperations.eq(
                converter,
                Boolean.FALSE,
                Boolean.TRUE
            )
        );
        assertTrue(
            BooleanOperations.eq(
                converter,
                Boolean.TRUE,
                Boolean.TRUE
            )
        );
        assertTrue(
            BooleanOperations.eq(
                converter,
                Boolean.FALSE,
                Boolean.FALSE
            )
        );
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
        assertFalse(
            BooleanOperations.eq(converter, getClass(), Character.valueOf('a'))
        );
        assertFalse(
            BooleanOperations.eq(converter, Character.valueOf('a'), getClass())
        );
        try {
            BooleanOperations.eq(converter, getClass(), Long.valueOf(0)); // coercion fails
            fail();
        } catch (Exception e) {}
    }

    /*
     * Test method for 'org.activiti.core.el.juel.BooleanOperations.ne(Object, Object)'
     */
    @Test
    public void testNe() {
        assertFalse(
            BooleanOperations.ne(converter, Boolean.TRUE, Boolean.TRUE)
        );
        assertTrue(BooleanOperations.ne(converter, null, Boolean.TRUE));
        assertTrue(BooleanOperations.ne(converter, Boolean.TRUE, null));
        assertTrue(BooleanOperations.ne(converter, "1", new BigDecimal("2")));
        assertFalse(BooleanOperations.ne(converter, new BigDecimal("1"), "1"));
        assertTrue(BooleanOperations.ne(converter, new BigDecimal("2"), "1"));
        assertTrue(BooleanOperations.ne(converter, "1", Float.valueOf("2")));
        assertFalse(BooleanOperations.ne(converter, Float.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ne(converter, Float.valueOf("2"), "1"));
        assertTrue(BooleanOperations.ne(converter, "1", Double.valueOf("2")));
        assertFalse(BooleanOperations.ne(converter, Double.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ne(converter, Double.valueOf("2"), "1"));
        assertTrue(BooleanOperations.ne(converter, "1", new BigInteger("2")));
        assertFalse(BooleanOperations.ne(converter, new BigInteger("1"), "1"));
        assertTrue(BooleanOperations.ne(converter, new BigInteger("2"), "1"));
        assertTrue(BooleanOperations.ne(converter, "1", Byte.valueOf("2")));
        assertFalse(BooleanOperations.ne(converter, Byte.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ne(converter, Byte.valueOf("2"), "1"));
        assertTrue(BooleanOperations.ne(converter, "1", Short.valueOf("2")));
        assertFalse(BooleanOperations.ne(converter, Short.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ne(converter, Short.valueOf("2"), "1"));
        assertTrue(
            BooleanOperations.ne(
                converter,
                Character.valueOf('a'),
                Character.valueOf('b')
            )
        );
        assertFalse(
            BooleanOperations.ne(
                converter,
                Character.valueOf('a'),
                Character.valueOf('a')
            )
        );
        assertTrue(
            BooleanOperations.ne(
                converter,
                Character.valueOf('b'),
                 Character.valueOf('a')
            )
        );
        assertTrue(BooleanOperations.ne(converter, "1", Integer.valueOf("2")));
        assertFalse(BooleanOperations.ne(converter, Integer.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ne(converter, Integer.valueOf("2"), "1"));
        assertTrue(BooleanOperations.ne(converter, "1", Long.valueOf("2")));
        assertFalse(BooleanOperations.ne(converter, Long.valueOf("1"), "1"));
        assertTrue(BooleanOperations.ne(converter, Long.valueOf("2"), "1"));
        assertTrue(
            BooleanOperations.ne(
                converter,
                Boolean.FALSE,
                Boolean.TRUE
            )
        );
        assertFalse(
            BooleanOperations.ne(
                converter,
                Boolean.TRUE,
                Boolean.TRUE
            )
        );
        assertFalse(
            BooleanOperations.ne(
                converter,
                Boolean.FALSE,
                Boolean.FALSE
            )
        );
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
        assertTrue(
            BooleanOperations.ne(converter, getClass(), Character.valueOf('a'))
        );
        assertTrue(
            BooleanOperations.ne(converter, Character.valueOf('a'), getClass())
        );
        try {
            BooleanOperations.ne(converter, getClass(), Long.valueOf(0)); // coercion fails
            fail();
        } catch (Exception e) {}
    }

    /*
     * Test method for 'org.activiti.core.el.juel.BooleanOperations.empty(Object)'
     */
    @Test
    public void testEmpty() {
        assertTrue(BooleanOperations.empty(converter, null));
        assertTrue(BooleanOperations.empty(converter, ""));
        assertTrue(BooleanOperations.empty(converter, new Object[0]));
        assertTrue(
            BooleanOperations.empty(converter, new HashMap<Object, Object>())
        );
        assertTrue(BooleanOperations.empty(converter, new ArrayList<Object>()));
        assertFalse(BooleanOperations.empty(converter, "foo"));
    }
}
