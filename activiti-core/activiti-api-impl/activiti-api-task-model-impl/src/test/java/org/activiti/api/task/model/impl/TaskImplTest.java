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
package org.activiti.api.task.model.impl;

import static org.activiti.api.task.model.Task.TaskStatus.CREATED;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class TaskImplTest {

    @Test
    void shouldDefaultAllowSelfServiceToNull() {
        TaskImpl task = new TaskImpl("task-id", "Task", CREATED);

        assertThat(task.isAllowSelfService()).isNull();
    }

    @Test
    void shouldExposeAllowSelfService() {
        TaskImpl task = new TaskImpl("task-id", "Task", CREATED);

        task.setAllowSelfService(true);

        assertThat(task.isAllowSelfService()).isTrue();
    }

    @Test
    void shouldIncludeAllowSelfServiceInEqualityAndHashCode() {
        TaskImpl task = new TaskImpl("task-id", "Task", CREATED);
        task.setAllowSelfService(true);
        TaskImpl equalTask = new TaskImpl("task-id", "Task", CREATED);
        equalTask.setAllowSelfService(true);
        TaskImpl differentTask = new TaskImpl("task-id", "Task", CREATED);

        assertThat(task).isEqualTo(equalTask).hasSameHashCodeAs(equalTask).isNotEqualTo(differentTask);
    }

    @Test
    void shouldIncludeAllowSelfServiceInToString() {
        TaskImpl task = new TaskImpl("task-id", "Task", CREATED);
        task.setAllowSelfService(true);

        assertThat(task.toString()).contains("allowSelfService=true");
    }

    @Test
    void shouldSerializeAllowSelfService() throws Exception {
        TaskImpl task = new TaskImpl("task-id", "Task", CREATED);
        task.setAllowSelfService(true);

        String json = JsonMapper.builder().build().writeValueAsString(task);

        assertThat(json).contains("\"allowSelfService\":true");
    }

    @Test
    void shouldSerializeAllowSelfServiceAsNull_whenNotSet() throws Exception {
        TaskImpl task = new TaskImpl("task-id", "Task", CREATED);

        String json = JsonMapper.builder().build().writeValueAsString(task);

        assertThat(json).contains("\"allowSelfService\":null");
    }
}
