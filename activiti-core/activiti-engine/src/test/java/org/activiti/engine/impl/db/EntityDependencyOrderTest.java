/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.engine.impl.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;
import org.junit.Test;

public class EntityDependencyOrderTest {

    @Test
    public void updateOrderShouldEqualInsertOrder() {
        assertThat(EntityDependencyOrder.UPDATE_ORDER)
            .isNotEmpty()
            .containsExactlyElementsOf(EntityDependencyOrder.INSERT_ORDER);
    }

    @Test
    public void updateOrderShouldRankBothExecutionAndVariableInstanceForDeterministicSort() {
        assertThat(EntityDependencyOrder.UPDATE_ORDER)
            .contains(ExecutionEntityImpl.class, VariableInstanceEntityImpl.class);
    }
}
