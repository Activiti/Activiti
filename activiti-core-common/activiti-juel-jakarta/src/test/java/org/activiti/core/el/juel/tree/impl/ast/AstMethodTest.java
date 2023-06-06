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

import jakarta.el.BeanELResolver;
import jakarta.el.ELException;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;
import org.activiti.core.el.juel.test.TestCase;
import org.activiti.core.el.juel.tree.Bindings;
import org.activiti.core.el.juel.util.SimpleContext;
import org.activiti.core.el.juel.util.SimpleResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AstMethodTest extends TestCase {

    AstMethod parseNode(String expression) {
        return (AstMethod) parse(expression).getRoot().getChild(0);
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

    public Object getNullObject() {
        return null;
    }

    @BeforeEach
    protected void setUp() throws Exception {
        context = new SimpleContext(new SimpleResolver(new BeanELResolver()));
        context.getELResolver().setValue(context, null, "base", this);

        bindings = new Bindings(null, new ValueExpression[1]);
    }

    @Test
    public void testEval() {
        try {
            parseNode("${base.bad()}").eval(bindings, context);
            fail();
        } catch (MethodNotFoundException e) {}
        assertEquals(1l, parseNode("${base.bar()}").eval(bindings, context));
        assertEquals(3l, parseNode("${base.bar(3)}").eval(bindings, context));
    }

    @Test
    public void testAppendStructure() {
        StringBuilder s = new StringBuilder();
        parseNode("${foo.bar(1)}")
            .appendStructure(s, new Bindings(null, null, null));
        assertEquals("foo.bar(1)", s.toString());
    }

    @Test
    public void testIsLiteralText() {
        assertFalse(parseNode("${foo.bar()}").isLiteralText());
    }

    @Test
    public void testIsLeftValue() {
        assertFalse(parseNode("${foo.bar()}").isLeftValue());
    }

    @Test
    public void testGetType() {
        assertNull(parseNode("${base.foo()}").getType(bindings, context));
    }

    @Test
    public void testIsReadOnly() {
        assertTrue(parseNode("${base.foo()}").isReadOnly(bindings, context));
    }

    @Test
    public void testSetValue() {
        try {
            parseNode("${base.foo()}").setValue(bindings, context, 0);
            fail();
        } catch (ELException e) {}
    }

    @Test
    public void testGetValue() {
        assertEquals(
            "1",
            parseNode("${base.bar()}").getValue(bindings, context, String.class)
        );
        assertEquals(
            "3",
            parseNode("${base.bar(3)}")
                .getValue(bindings, context, String.class)
        );

        assertNull(
            parseNode("${base.nullObject.toString()}")
                .getValue(bindings, context, Object.class)
        );
    }

    @Test
    public void testGetValueReference() {
        assertNull(
            parseNode("${base.bar()}").getValueReference(bindings, context)
        );
    }

    @Test
    public void testInvoke() {
        assertEquals(
            1l,
            parseNode("${base.bar()}")
                .invoke(bindings, context, null, null, new Object[] { 999l })
        );
        assertEquals(
            3l,
            parseNode("${base.bar(3)}")
                .invoke(
                    bindings,
                    context,
                    null,
                    new Class[] { long.class },
                    new Object[] { 999l }
                )
        );

        try {
            parseNode("${base.nullObject.toString()}")
                .invoke(bindings, context, null, null, new Object[0]);
            fail();
        } catch (PropertyNotFoundException e) {
            // ok
        }
    }

    @Test
    public void testGetMethodInfo() {
        assertNull(
            parseNode("${base.bar()}")
                .getMethodInfo(
                    bindings,
                    context,
                    null,
                    new Class[] { long.class }
                )
        );
    }
}
