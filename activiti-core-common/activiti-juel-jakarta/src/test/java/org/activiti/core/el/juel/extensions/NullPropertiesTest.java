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

package org.activiti.core.el.juel.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import java.util.HashMap;
import java.util.Map;
import org.activiti.core.el.juel.ExpressionFactoryImpl;
import org.activiti.core.el.juel.util.SimpleContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class NullPropertiesTest {

    @AfterEach
    public void afterEach() {
        System.clearProperty("activiti.juel.nullProperties");
    }

    @Test
    public void tesNullProperties() {
        // create our factory which uses our customized builder
        System.setProperty("activiti.juel.nullProperties", "true");
        ExpressionFactory f = new ExpressionFactoryImpl(System.getProperties());

        // create our context
        ELContext context = new SimpleContext();

        // create our expression we want to evaluate
        ValueExpression e = f.createValueExpression(
            context,
            "${map[null]}",
            String.class
        );

        // create a map containing a value for key <code>null</code> and make it available
        Map<String, String> map = new HashMap<String, String>();
        map.put(null, "foo");
        context.getELResolver().setValue(context, null, "map", map);

        // let's go...
        assertEquals(e.getValue(context), "foo"); // --> "foo"
    }

    @Test
    public void tesNullPropertiesDisabled() {
        // create our factory
        System.setProperty("activiti.juel.nullProperties", "false");
        ExpressionFactory f = new ExpressionFactoryImpl(System.getProperties());

        // create our context
        ELContext context = new SimpleContext();

        // create our expression we want to evaluate
        ValueExpression e = f.createValueExpression(
            context,
            "${map[null]}",
            String.class
        );

        // create a map containing a value for key <code>null</code> and make it available
        Map<String, String> map = new HashMap<String, String>();
        map.put(null, "foo");
        context.getELResolver().setValue(context, null, "map", map);

        // let's go...
        assertEquals("", e.getValue(context)); // --> "foo"
    }
}
