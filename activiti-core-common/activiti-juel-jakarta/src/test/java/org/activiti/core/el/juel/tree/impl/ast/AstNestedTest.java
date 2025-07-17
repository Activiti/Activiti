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

import org.activiti.core.el.juel.test.TestCase;
import org.junit.jupiter.api.Test;

public class AstNestedTest extends TestCase {

    AstNested parseNode(String expression) {
        return (AstNested) parse(expression).getRoot().getChild(0);
    }

    @Test
    public void testIsLeftValue() {
        assertFalse(parseNode("${(a)}").isLeftValue());
    }

    @Test
    public void testEval() {
        assertEquals(1l, parseNode("${(1)}").eval(null, null));
    }

    @Test
    public void testAppendStructure() {
        StringBuilder s = new StringBuilder();
        parseNode("${(1)}").appendStructure(s, null);
        assertEquals("(1)", s.toString());
    }

    @Test
    public void testGetValueReference() {
        assertNull(parseNode("${(1)}").getValueReference(null, null));
    }
}
