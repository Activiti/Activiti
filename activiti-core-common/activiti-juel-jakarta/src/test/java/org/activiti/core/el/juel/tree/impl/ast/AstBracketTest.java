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

package org.activiti.core.el.juel.tree.impl.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.el.ELException;
import jakarta.el.MethodInfo;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;
import java.util.Arrays;
import java.util.HashMap;
import org.activiti.core.el.juel.test.TestCase;
import org.activiti.core.el.juel.test.TestClass;
import org.activiti.core.el.juel.tree.Bindings;
import org.activiti.core.el.juel.tree.impl.Builder;
import org.activiti.core.el.juel.util.SimpleContext;
import org.activiti.core.el.juel.util.SimpleResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AstBracketTest extends TestCase {

    AstBracket parseNode(String expression) {
        return (AstBracket) parse(expression).getRoot().getChild(0);
    }

    SimpleContext context;
    Bindings bindings;

    long foo = 1l;

    public long getFoo() {
        return foo;
    }

    public void setFoo(long value) {
        foo = value;
    }

    public long bar() {
        return 1l;
    }

    public long bar(long value) {
        return value;
    }

    public TestClass getTestClass() {
        return new TestClass();
    }

    public Object getNullObject() {
        return null;
    }

    @BeforeEach
    protected void setUp() throws Exception {
        context = new SimpleContext(new SimpleResolver());
        context.getELResolver().setValue(context, null, "base", this);

        HashMap<Object, String> nullmap = new HashMap<Object, String>();
        nullmap.put(null, "foo");
        context.getELResolver().setValue(context, null, "nullmap", nullmap);

        bindings = new Bindings(null, new ValueExpression[2]);
    }

    @Test
    public void testEval() {
        try {
            parseNode("${base[bad]}").eval(bindings, context);
            fail();
        } catch (ELException e) {}
        assertEquals(1l, parseNode("${base['foo']}").eval(bindings, context));
    }

    @Test
    public void testAppendStructure() {
        StringBuilder s = new StringBuilder();
        parseNode("${foo[bar]}").appendStructure(s, new Bindings(null, null));
        assertEquals("foo[bar]", s.toString());
    }

    @Test
    public void testIsLiteralText() {
        assertFalse(parseNode("${foo[bar]}").isLiteralText());
    }

    @Test
    public void testIsLeftValue() {
        assertFalse(parseNode("${'foo'[bar]}").isLeftValue());
        assertTrue(parseNode("${foo[bar]}").isLeftValue());
    }

    @Test
    public void testGetType() {
        try {
            parseNode("${base[bad]}").getType(bindings, context);
            fail();
        } catch (ELException e) {}
        assertEquals(
            long.class,
            parseNode("${base['foo']}").getType(bindings, context)
        );
        assertNull(parseNode("${'base'['foo']}").getType(bindings, context));
        if (BUILDER.isEnabled(Builder.Feature.NULL_PROPERTIES)) {
            assertEquals(
                Object.class,
                parseNode("${nullmap[null]}").getType(bindings, context)
            );
        } else {
            try {
                parseNode("${nullmap[null]}").getType(bindings, context);
                fail();
            } catch (ELException e) {}
        }
    }

    @Test
    public void testIsReadOnly() {
        assertFalse(parseNode("${base['foo']}").isReadOnly(bindings, context));
        assertTrue(parseNode("${'base'['foo']}").isReadOnly(bindings, context));
        if (BUILDER.isEnabled(Builder.Feature.NULL_PROPERTIES)) {
            assertFalse(
                parseNode("${nullmap[null]}").isReadOnly(bindings, context)
            );
        } else {
            try {
                parseNode("${nullmap[null]}").isReadOnly(bindings, context);
                fail();
            } catch (ELException e) {}
        }
    }

    @Test
    public void testSetValue() {
        try {
            parseNode("${base[bad]}").setValue(bindings, context, "good");
            fail();
        } catch (ELException e) {}
        parseNode("${base['foo']}").setValue(bindings, context, 2l);
        assertEquals(2l, getFoo());
        parseNode("${base['foo']}").setValue(bindings, context, "3");
        assertEquals(3l, getFoo());
        if (BUILDER.isEnabled(Builder.Feature.NULL_PROPERTIES)) {
            parseNode("${nullmap[null]}").setValue(bindings, context, "bar");
            assertEquals(
                "bar",
                parseNode("${nullmap[null]}").eval(bindings, context)
            );
            parseNode("${nullmap[null]}").setValue(bindings, context, "foo");
        } else {
            try {
                parseNode("${nullmap[null]}")
                    .setValue(bindings, context, "bar");
                fail();
            } catch (ELException e) {}
        }
    }

    @Test
    public void testGetValue() {
        assertEquals(
            1l,
            parseNode("${base['foo']}").getValue(bindings, context, null)
        );
        assertEquals(
            "1",
            parseNode("${base['foo']}")
                .getValue(bindings, context, String.class)
        );
        assertNull(
            parseNode("${base.nullObject['class']}")
                .getValue(bindings, context, Object.class)
        );
        if (BUILDER.isEnabled(Builder.Feature.NULL_PROPERTIES)) {
            assertEquals(
                "foo",
                parseNode("${nullmap[null]}").getValue(bindings, context, null)
            );
        } else {
            assertNull(
                parseNode("${nullmap[null]}").getValue(bindings, context, null)
            );
        }
    }

    @Test
    public void testGetValueReference() {
        assertEquals(
            this,
            parseNode("${base['foo']}")
                .getValueReference(bindings, context)
                .getBase()
        );
        assertEquals(
            "foo",
            parseNode("${base['foo']}")
                .getValueReference(bindings, context)
                .getProperty()
        );
    }

    @Test
    public void testInvoke() {
        assertEquals(
            1l,
            parseNode("${base['bar']}")
                .invoke(bindings, context, long.class, new Class[0], null)
        );
        assertEquals(
            2l,
            parseNode("${base['bar']}")
                .invoke(
                    bindings,
                    context,
                    null,
                    new Class[] { long.class },
                    new Object[] { 2l }
                )
        );

        assertEquals(
            42,
            parseNode("${base.testClass.anonymousTestInterface['fourtyTwo']}")
                .invoke(bindings, context, null, new Class[0], null)
        );
        assertEquals(
            42,
            parseNode("${base.testClass.nestedTestInterface['fourtyTwo']}")
                .invoke(bindings, context, null, new Class[0], null)
        );

        try {
            parseNode("${base.nullObject['class']}")
                .invoke(bindings, context, null, null, new Object[0]);
            fail();
        } catch (PropertyNotFoundException e) {
            // ok
        }
    }

    @Test
    public void testGetMethodInfo() {
        MethodInfo info = null;

        // long bar()
        info =
            parseNode("${base['bar']}")
                .getMethodInfo(bindings, context, long.class, new Class[0]);
        assertEquals("bar", info.getName());
        assertTrue(Arrays.equals(new Class[0], info.getParamTypes()));
        assertEquals(long.class, info.getReturnType());

        // long bar(long)
        info =
            parseNode("${base['bar']}")
                .getMethodInfo(
                    bindings,
                    context,
                    null,
                    new Class[] { long.class }
                );
        assertEquals("bar", info.getName());
        assertTrue(
            Arrays.equals(new Class[] { long.class }, info.getParamTypes())
        );
        assertEquals(long.class, info.getReturnType());

        // bad arg type
        try {
            info =
                parseNode("${base['bar']}")
                    .getMethodInfo(
                        bindings,
                        context,
                        null,
                        new Class[] { String.class }
                    );
            fail();
        } catch (ELException e) {}
        // bad return type
        try {
            info =
                parseNode("${base['bar']}")
                    .getMethodInfo(
                        bindings,
                        context,
                        String.class,
                        new Class[0]
                    );
            fail();
        } catch (ELException e) {}
    }
}
