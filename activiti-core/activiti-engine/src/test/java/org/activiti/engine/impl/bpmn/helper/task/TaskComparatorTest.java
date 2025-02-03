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
