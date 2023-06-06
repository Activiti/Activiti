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

import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import java.lang.reflect.Method;
import org.activiti.core.el.juel.util.SimpleContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class VarArgsTest {

    @AfterEach
    public void afterEach() {
        System.clearProperty("activiti.juel.varArgs");
    }

    @Test
    public void testVarArgs() throws NoSuchMethodException {
        // create our factory
        // varargs are enabled by default
        ExpressionFactory f = ExpressionFactory.newInstance();

        // create our context with function "vararg:format"
        Method method =
            String.class.getMethod(
                    "format",
                    new Class[] { String.class, Object[].class }
                );
        SimpleContext context = new SimpleContext();
        context.setFunction("varargs", "format", method);

        // our expression we want to evaluate
        String expression = "${varargs:format('Hey %s','Joe')}";

        // let's go...
        ValueExpression e = f.createValueExpression(
            context,
            expression,
            String.class
        );
        assertEquals(e.getValue(context), "Hey Joe"); // --> Hey Joe
    }

    @Test
    public void testVarArgsDisabled() {
        // create our factory
        System.setProperty("activiti.juel.varArgs", "false");

        try {
            testVarArgs();
            fail("Varargs should be disabled.");
        } catch (ELException expected) {
            assertEquals(
                "Cannot coerce 'Joe' of class java.lang.String to class [Ljava.lang.Object; (incompatible type)",
                expected.getMessage()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
