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

package org.activiti.core.el.juel.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.el.ValueExpression;
import java.lang.reflect.Method;
import org.activiti.core.el.juel.ObjectValueExpression;
import org.activiti.core.el.juel.misc.TypeConverter;
import org.activiti.core.el.juel.test.TestCase;
import org.activiti.core.el.juel.util.SimpleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BindingsTest extends TestCase {

    public static int foo() {
        return 0;
    }

    public static int bar(int i) {
        return i;
    }

    private SimpleContext context;

    @BeforeEach
    protected void setUp() throws Exception {
        context = new SimpleContext();

        // function ns:f()
        context.setFunction("ns", "f", BindingsTest.class.getMethod("foo"));

        // function g()
        context.setFunction(
            "",
            "g",
            BindingsTest.class.getMethod("bar", new Class[] { int.class })
        );

        // variable v
        context.setVariable(
            "v",
            new ObjectValueExpression(
                TypeConverter.DEFAULT,
               Long.valueOf(0),
                long.class
            )
        );
    }

    @Test
    public void testSerialize() throws Exception {
        Bindings bindings = null;

        bindings = new Bindings(null, null);
        assertEquals(bindings, deserialize(serialize(bindings)));

        bindings =
            parse("${ns:f()+v+g(1)+x}")
                .bind(context.getFunctionMapper(), context.getVariableMapper());
        assertEquals(bindings, deserialize(serialize(bindings)));
    }

    @Test
    public void testEqualsAndHashcode() throws Exception {
        Bindings bindings1 = null;
        Bindings bindings2 = null;

        bindings1 = new Bindings(null, null);
        bindings2 = new Bindings(null, null);
        assertEquals(bindings1, bindings2);
        assertEquals(bindings1.hashCode(), bindings2.hashCode());

        bindings1 = new Bindings(new Method[0], new ValueExpression[0]);
        bindings2 = new Bindings(null, null);
        assertEquals(bindings1, bindings2);
        assertEquals(bindings1.hashCode(), bindings2.hashCode());

        Tree tree = parse("${ns:f()+v+g(1)}+x");
        bindings1 =
            tree.bind(context.getFunctionMapper(), context.getVariableMapper());
        bindings2 =
            tree.bind(context.getFunctionMapper(), context.getVariableMapper());
        assertEquals(bindings1, bindings2);
        assertEquals(bindings1.hashCode(), bindings2.hashCode());
    }
}
