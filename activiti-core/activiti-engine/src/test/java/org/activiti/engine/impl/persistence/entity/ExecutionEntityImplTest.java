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

package org.activiti.engine.impl.persistence.entity;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ExecutionEntityImplTest {

    public static final String ROOT_ID = "rootId";

    @Test
    public void isRootExecution_should_returnTrue_whenIdIsSameAsRoot() {
        final ExecutionEntityImpl executionEntity = new ExecutionEntityImpl();
        executionEntity.setId(ROOT_ID);
        executionEntity.setRootProcessInstanceId(ROOT_ID);

        assertThat(executionEntity.isRootExecution()).isTrue();

    }

    @Test
    public void isRootExecution_should_returnFalse_whenIdIsNotSameAsRoot() {
        final ExecutionEntityImpl executionEntity = new ExecutionEntityImpl();
        executionEntity.setId("anotherId");
        executionEntity.setRootProcessInstanceId(ROOT_ID);

        assertThat(executionEntity.isRootExecution()).isFalse();

    }

    @Test
    public void isRootExecution_should_returnFalse_whenIdIsNull() {
        final ExecutionEntityImpl executionEntity = new ExecutionEntityImpl();
        executionEntity.setId(null);

        assertThat(executionEntity.isRootExecution()).isFalse();
    }
}
