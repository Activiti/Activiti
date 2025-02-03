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
package org.activiti.engine.impl.bpmn.helper.task;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskComparatorTest {

    @Test
    public void checkingChanges_whenThereNothingToCheck_shouldReturnFalse() {
        TaskComparatorImpl taskComparator = new TaskComparatorImpl();

        assertThat(taskComparator.hasTaskCategoryChanged()).isFalse();
        assertThat(taskComparator.hasTaskAssigneeChanged()).isFalse();
        assertThat(taskComparator.hasTaskDefinitionKeyChanged()).isFalse();
        assertThat(taskComparator.hasTaskFormKeyChanged()).isFalse();
        assertThat(taskComparator.hasTaskNameChanged()).isFalse();
        assertThat(taskComparator.hasTaskOwnerChanged()).isFalse();
        assertThat(taskComparator.hasTaskDueDateChanged()).isFalse();
        assertThat(taskComparator.hasTaskPriorityChanged()).isFalse();
        assertThat(taskComparator.hasTaskParentIdChanged()).isFalse();

        assertThat(taskComparator.hasTaskChanged()).isFalse();

        // DelegateTask = TaskEntityImpl
        // TaskInfo = TaskEntityImpl
    }

    @Test
    public void checkingChanges_whenThereIsOnlyOriginalTask_shouldReturnTrue() {
        TaskComparatorImpl taskComparator = new TaskComparatorImpl();



    }
}
