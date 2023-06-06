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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MessagesTest {

    /*
     * Test method for 'org.activiti.core.el.juel.Messages.get(String)'
     */
    @Test
    public void testGetString() {
        assertTrue(LocalMessages.get("foo").matches(".*foo"));
    }

    /*
     * Test method for 'org.activiti.core.el.juel.Messages.get(String, Object)'
     */
    @Test
    public void testGetStringObject() {
        assertTrue(LocalMessages.get("foo", "bar").matches(".*foo\\(bar\\)"));
    }

    /*
     * Test method for 'org.activiti.core.el.juel.Messages.get(String, Object, Object)'
     */
    @Test
    public void testGetStringObjectObject() {
        assertTrue(
            LocalMessages
                .get("foo", "bar", "baz")
                .matches(".*foo\\(bar,\\s*baz\\)")
        );
    }
}
