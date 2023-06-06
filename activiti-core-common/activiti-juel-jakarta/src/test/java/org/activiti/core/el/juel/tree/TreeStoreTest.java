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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.activiti.core.el.juel.test.TestCase;
import org.activiti.core.el.juel.tree.impl.Cache;
import org.junit.jupiter.api.Test;

public class TreeStoreTest extends TestCase {

    @Test
    public void test() {
        TreeStore store = new TreeStore(BUILDER, new Cache(1));
        assertSame(BUILDER, store.getBuilder());

        Tree tree = store.get("1");
        assertNotNull(tree);
        assertSame(tree, store.get("1"));
    }
}
