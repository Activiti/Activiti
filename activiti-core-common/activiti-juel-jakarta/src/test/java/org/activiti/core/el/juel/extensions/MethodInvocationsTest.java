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
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.el.BeanELResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import org.activiti.core.el.juel.util.SimpleContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class MethodInvocationsTest {

    @AfterEach
    public void afterEach() {
        System.clearProperty("activiti.juel.methodInvocations");
    }

    @Test
    public void testMethodInvocations() {
        // create our factory
        // method invocations are enabled by default
        ExpressionFactory f = ExpressionFactory.newInstance();

        // create our resolver
        CompositeELResolver resolver = new CompositeELResolver();
        resolver.add(new BeanELResolver());

        // create our context
        ELContext context = new SimpleContext(resolver);

        // let's go...
        ValueExpression e = null;

        e =
            f.createValueExpression(
                context,
                "${'foo'.matches('foo|bar')}",
                boolean.class
            );
        assertEquals(e.getValue(context), true); // --> true

        e =
            f.createValueExpression(
                context,
                "${'bar'.toUpperCase()}",
                String.class
            );
        assertEquals(e.getValue(context), "BAR"); // --> BAR

        e =
            f.createValueExpression(
                context,
                "${'foobar '.trim().length()}",
                int.class
            );
        assertEquals((Integer) e.getValue(context), 6); // --> 6
    }

    @Test
    public void testMethodInvocationsDisabled() {
        // create our factory
        System.setProperty("activiti.juel.methodInvocations", "false");

        try {
            testMethodInvocations();
            fail("Method invocations should be disabled.");
        } catch (Exception expected) {}
    }
}
