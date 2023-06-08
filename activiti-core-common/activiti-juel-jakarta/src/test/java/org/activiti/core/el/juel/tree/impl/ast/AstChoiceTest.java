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

public class AstChoiceTest extends TestCase {

    private Bindings bindings = new Bindings(null, null, null);

    AstChoice parseNode(String expression) {
        return (AstChoice) parse(expression).getRoot().getChild(0);
    }

    @Test
    public void testEval() {
        assertEquals(1l, parseNode("${true?1:2}").eval(bindings, null));
        assertEquals(2l, parseNode("${false?1:2}").eval(bindings, null));
    }

    @Test
    public void testAppendStructure() {
        StringBuilder s = new StringBuilder();
        parseNode("${a?b:c}").appendStructure(s, bindings);
        assertEquals("a ? b : c", s.toString());
    }

    @Test
    public void testIsLiteralText() {
        assertFalse(parseNode("${a?b:c}").isLiteralText());
    }

    @Test
    public void testIsLeftValue() {
        assertFalse(parseNode("${a?b:c}").isLeftValue());
    }

    @Test
    public void testGetType() {
        assertNull(parseNode("${a?b:c}").getType(bindings, null));
    }

    @Test
    public void testIsReadOnly() {
        assertTrue(parseNode("${a?b:c}").isReadOnly(bindings, null));
    }

    @Test
    public void testSetValue() {
        try {
            parseNode("${a?b:c}").setValue(bindings, null, null);
            fail();
        } catch (ELException e) {}
    }

    @Test
    public void testGetValue() {
        assertEquals(
            1l,
            parseNode("${true?1:2}").getValue(bindings, null, null)
        );
        assertEquals(
            "1",
            parseNode("${true?1:2}").getValue(bindings, null, String.class)
        );
    }

    @Test
    public void testGetValueReference() {
        assertNull(parseNode("${true?1:2}").getValueReference(bindings, null));
    }
}
