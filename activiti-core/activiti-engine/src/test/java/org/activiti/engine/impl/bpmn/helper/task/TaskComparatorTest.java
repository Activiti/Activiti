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

import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.task.TaskInfo;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TaskComparatorTest {

    @Test
    public void checkingChanges_whenThereNothingToCheck_shouldReturnFalse() {
        TaskComparatorImpl taskComparator = new TaskComparatorImpl();

        assertThatAllChangesAreFalse(taskComparator);
    }

    @Test
    public void checkingChanges_whenThereIsOnlyOriginalTask_shouldReturnFalse() {
        TaskComparatorImpl taskComparator = new TaskComparatorImpl();

        TaskInfo originalTask = createTaskInfo();
        taskComparator.setOriginalTask(originalTask);

        assertThatAllChangesAreFalse(taskComparator);
    }

    @Test
    public void checkingChanges_whenThereIsOnlyUpdatedTask_shouldReturnException() {
        TaskComparatorImpl taskComparator = new TaskComparatorImpl();

        TaskInfo originalTask = createTaskInfo();
        try {
            taskComparator.setUpdatedTask(originalTask);
            fail("An Exception should be raised instead");
        } catch (IllegalArgumentException exception) {
            assertThat(exception.getMessage()).isEqualTo("an originalTask is needed before setting an updatedTask");
        }
    }

    @Test
    public void checkingChanges_whenThereAreNoChanges_shouldReturnFalse() {
        TaskComparatorImpl taskComparator = new TaskComparatorImpl();

        TaskInfo task = createTaskInfo();
        taskComparator.setOriginalTask(task);
        taskComparator.setUpdatedTask(task);

        assertThatAllChangesAreFalse(taskComparator);
    }

    @Test
    public void checkingChanges_whenThereAreChanges_shouldReturnFalse() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TaskComparatorImpl taskComparator = new TaskComparatorImpl();

        // GIVEN: a TaskComparator with a task to be compared to
        TaskEntityImpl task = createTaskInfo();
        taskComparator.setOriginalTask(task);

        // WHEN: we change the assignee of the updated-task
        task.setAssignee("new assignee");
        taskComparator.setUpdatedTask(task);
        // THEN: only "hasTaskAssigneeChanged" and "hasTaskChanged" method indicates that task was changed
        assertThatAllChangesExceptOneAreFalse(taskComparator, TaskComparator.class.getMethod("hasTaskAssigneeChanged",null));
        assertThat(taskComparator.hasTaskChanged()).isTrue();

        // WHEN: we change the category of the updated-task
        task = createTaskInfo();
        task.setCategory("new category");
        taskComparator.setUpdatedTask(task);
        // THEN: only "hasTaskCategoryChanged" and "hasTaskChanged" method indicates that task was changed
        assertThatAllChangesExceptOneAreFalse(taskComparator, TaskComparator.class.getMethod("hasTaskCategoryChanged",null));
        assertThat(taskComparator.hasTaskChanged()).isTrue();

        // WHEN: we change the description of the updated-task
        task = createTaskInfo();
        task.setDescription("new description");
        taskComparator.setUpdatedTask(task);
        // THEN: only "hasTaskDescriptionChanged" and "hasTaskChanged" method indicates that task was changed
        assertThatAllChangesExceptOneAreFalse(taskComparator, TaskComparator.class.getMethod("hasTaskDescriptionChanged",null));
        assertThat(taskComparator.hasTaskChanged()).isTrue();

        // WHEN: we change the dueDate of the updated-task
        task = createTaskInfo();
        task.setDueDate(new Date(100));
        taskComparator.setUpdatedTask(task);
        // THEN: only "hasTaskDueDateChanged" and "hasTaskChanged" method indicates that task was changed
        assertThatAllChangesExceptOneAreFalse(taskComparator, TaskComparator.class.getMethod("hasTaskDueDateChanged",null));
        assertThat(taskComparator.hasTaskChanged()).isTrue();

        // WHEN: we change the formKey of the updated-task
        task = createTaskInfo();
        task.setFormKey("new formKey");
        taskComparator.setUpdatedTask(task);
        // THEN: only "hasTaskFormKeyChanged" and "hasTaskChanged" method indicates that task was changed
        assertThatAllChangesExceptOneAreFalse(taskComparator, TaskComparator.class.getMethod("hasTaskFormKeyChanged",null));
        assertThat(taskComparator.hasTaskChanged()).isTrue();

        // WHEN: we change the name of the updated-task
        task = createTaskInfo();
        task.setName("new name");
        taskComparator.setUpdatedTask(task);
        // THEN: only "hasTaskNameChanged" and "hasTaskChanged" method indicates that task was changed
        assertThatAllChangesExceptOneAreFalse(taskComparator, TaskComparator.class.getMethod("hasTaskNameChanged",null));
        assertThat(taskComparator.hasTaskChanged()).isTrue();

        // WHEN: we change the owner of the updated-task
        task = createTaskInfo();
        task.setOwner("new owner");
        taskComparator.setUpdatedTask(task);
        // THEN: only "hasTaskOwnerChanged" and "hasTaskChanged" method indicates that task was changed
        assertThatAllChangesExceptOneAreFalse(taskComparator, TaskComparator.class.getMethod("hasTaskOwnerChanged",null));
        assertThat(taskComparator.hasTaskChanged()).isTrue();

        // WHEN: we change the parentTaskId of the updated-task
        task = createTaskInfo();
        task.setParentTaskId("new parentTaskId");
        taskComparator.setUpdatedTask(task);
        // THEN: only "hasTaskParentIdChanged" and "hasTaskChanged" method indicates that task was changed
        assertThatAllChangesExceptOneAreFalse(taskComparator, TaskComparator.class.getMethod("hasTaskParentIdChanged",null));
        assertThat(taskComparator.hasTaskChanged()).isTrue();

        // WHEN: we change the priority of the updated-task
        task = createTaskInfo();
        task.setPriority(100);
        taskComparator.setUpdatedTask(task);
        // THEN: only "hasTaskPriorityChanged" and "hasTaskChanged" method indicates that task was changed
        assertThatAllChangesExceptOneAreFalse(taskComparator, TaskComparator.class.getMethod("hasTaskPriorityChanged",null));
        assertThat(taskComparator.hasTaskChanged()).isTrue();

        // WHEN: we change the definitionKey of the updated-task
        task = createTaskInfo();
        task.setTaskDefinitionKey("new taskDefinitionKey");
        taskComparator.setUpdatedTask(task);
        // THEN: only "hasTaskDefinitionKeyChanged" and "hasTaskChanged" method indicates that task was changed
        assertThatAllChangesExceptOneAreFalse(taskComparator, TaskComparator.class.getMethod("hasTaskDefinitionKeyChanged",null));
        assertThat(taskComparator.hasTaskChanged()).isTrue();
    }

    private TaskEntityImpl createTaskInfo() {
        TaskEntityImpl taskInfo = new TaskEntityImpl();

        taskInfo.setAssignee("assignee");
        taskInfo.setCategory("category");
        taskInfo.setDescription("description");
        taskInfo.setDueDate(new Date(0));
        taskInfo.setFormKey("formKey");
        taskInfo.setName("name");
        taskInfo.setOwner("owner");
        taskInfo.setParentTaskId("parentTaskId");
        taskInfo.setPriority(1);
        taskInfo.setTaskDefinitionKey("taskDefinitionKey");

        return taskInfo;
    }

    private void assertThatAllChangesAreFalse(TaskComparator taskComparator) {
        assertThat(taskComparator.hasTaskAssigneeChanged()).isFalse();
        assertThat(taskComparator.hasTaskCategoryChanged()).isFalse();
        assertThat(taskComparator.hasTaskDescriptionChanged()).isFalse();
        assertThat(taskComparator.hasTaskDueDateChanged()).isFalse();
        assertThat(taskComparator.hasTaskFormKeyChanged()).isFalse();
        assertThat(taskComparator.hasTaskNameChanged()).isFalse();
        assertThat(taskComparator.hasTaskOwnerChanged()).isFalse();
        assertThat(taskComparator.hasTaskParentIdChanged()).isFalse();
        assertThat(taskComparator.hasTaskPriorityChanged()).isFalse();
        assertThat(taskComparator.hasTaskDefinitionKeyChanged()).isFalse();
        assertThat(taskComparator.hasTaskChanged()).isFalse();
    }

    private void assertThatAllChangesExceptOneAreFalse(TaskComparator taskComparator, Method hasChangedMethod) throws InvocationTargetException, IllegalAccessException {
        assertThat((Boolean)hasChangedMethod.invoke(taskComparator, null)).isTrue();

        final List<Method> remainingMethods = Arrays.stream(TaskComparator.class.getDeclaredMethods())
            .filter(method -> method.getReturnType().isAssignableFrom(Boolean.class))
            .filter(method -> !method.getName().equals("hasTaskChanged"))
            .filter( method -> !method.equals(hasChangedMethod))
            .collect(Collectors.toList());

        for (Method method : remainingMethods) {
            assertThat((Boolean)method.invoke(taskComparator, null)).isFalse();
        }
    }
}
