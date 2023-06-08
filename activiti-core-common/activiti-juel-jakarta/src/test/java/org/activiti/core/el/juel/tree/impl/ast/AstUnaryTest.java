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
import org.activiti.core.el.juel.test.TestCase;
import org.activiti.core.el.juel.tree.Bindings;
import org.junit.jupiter.api.Test;

public class AstUnaryTest extends TestCase {

    private Bindings bindings = new Bindings(null, null, null);

    AstUnary parseNode(String expression) {
        return (AstUnary) parse(expression).getRoot().getChild(0);
    }

    @Test
    public void testEval() {
        assertEquals(true, parseNode("${!false}").eval(bindings, null));
        assertEquals(false, parseNode("${!true}").eval(bindings, null));
        assertEquals(false, parseNode("${empty 1}").eval(bindings, null));
        assertEquals(true, parseNode("${empty null}").eval(bindings, null));
        assertEquals(-1l, parseNode("${-1}").eval(bindings, null));
    }

    @Test
    public void testAppendStructure() {
        StringBuilder s = new StringBuilder();
        parseNode("${!1}").appendStructure(s, bindings);
        parseNode("${empty 1}").appendStructure(s, bindings);
        parseNode("${-1}").appendStructure(s, bindings);
        assertEquals("! 1empty 1- 1", s.toString());
    }

    @Test
    public void testIsLiteralText() {
        assertFalse(parseNode("${-1}").isLiteralText());
    }

    @Test
    public void testIsLeftValue() {
        assertFalse(parseNode("${-1}").isLeftValue());
    }

    @Test
    public void testGetType() {
        assertNull(parseNode("${-1}").getType(bindings, null));
    }

    @Test
    public void testIsReadOnly() {
        assertTrue(parseNode("${-1}").isReadOnly(bindings, null));
    }

    @Test
    public void testSetValue() {
        try {
            parseNode("${-1}").setValue(bindings, null, null);
            fail();
        } catch (ELException e) {}
    }

    @Test
    public void testGetValue() {
        assertEquals(
            Long.valueOf(-1l),
            parseNode("${-1}").getValue(bindings, null, null)
        );
        assertEquals(
            "-1",
            parseNode("${-1}").getValue(bindings, null, String.class)
        );
    }

    @Test
    public void testGetValueReference() {
        assertNull(parseNode("${-1}").getValueReference(null, null));
    }

    @Test
    public void testOperators() {
        assertFalse(
            (Boolean) parseNode("${not true}")
                .getValue(bindings, null, Boolean.class)
        );
        assertTrue(
            (Boolean) parseNode("${not false}")
                .getValue(bindings, null, Boolean.class)
        );
    }
}
