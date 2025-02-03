package org.activiti.engine.impl.bpmn.helper.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.persistence.entity.AbstractEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.task.TaskInfo;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class ComparableTaskTest {

    @Test
    public void testCreatingComparableTaskFromDelegateTask() {
        // GIVEN: a DelegateTask
        DelegateTask delegateTask = new TaskEntityImpl();
        ((AbstractEntity)delegateTask).setId("id");
        delegateTask.setName("name");
        delegateTask.setAssignee("assignee");
        delegateTask.setFormKey("formKey");
        delegateTask.setPriority(1);
        delegateTask.setCategory("category");
        delegateTask.setOwner("owner");
        delegateTask.setDescription("description");
        delegateTask.setDueDate(new Date(0));
        ((TaskEntityImpl)delegateTask).setTaskDefinitionKey("taskDefinitionKey");

        // WHEN: creating a ComparableTask based on the given DelegateTask
        ComparableTask comparableTask = new ComparableTask(delegateTask);

        // THEN: the ComparableTask should have the same values as the DelegateTask
        assertThat(comparableTask.getName()).isEqualTo(delegateTask.getName()).isEqualTo("name");
        assertThat(comparableTask.getId()).isEqualTo(delegateTask.getId()).isEqualTo("id");
        assertThat(comparableTask.getDescription()).isEqualTo(delegateTask.getDescription()).isEqualTo("description");
        assertThat(comparableTask.getCategory()).isEqualTo(delegateTask.getCategory()).isEqualTo("category");
        assertThat(comparableTask.getTaskDefinitionKey()).isEqualTo(delegateTask.getTaskDefinitionKey()).isEqualTo("taskDefinitionKey");
        assertThat(comparableTask.getDueDate()).isEqualTo(delegateTask.getDueDate()).isEqualTo(new Date(0));
        assertThat(comparableTask.getPriority()).isEqualTo(delegateTask.getPriority()).isEqualTo(1);
        assertThat(comparableTask.getOwner()).isEqualTo(delegateTask.getOwner()).isEqualTo("owner");

        // THEN: the ComparableTask doesn't have ParentTaskId
        assertThat(comparableTask.getParentTaskId()).isNull();

        // THEN: the ComparableTask is based on the DelegateTask
        assertThat(comparableTask.getOriginalTaskClass()).isEqualTo(DelegateTask.class);
    }

    @Test
    public void testCreatingComparableTaskFromTaskInfo() {
        // GIVEN: a TaskInfo
        TaskEntityImpl taskInfo = new TaskEntityImpl();
        taskInfo.setId("id");
        taskInfo.setName("name");
        taskInfo.setAssignee("assignee");
        taskInfo.setFormKey("formKey");
        taskInfo.setPriority(1);
        taskInfo.setCategory("category");
        taskInfo.setOwner("owner");
        taskInfo.setDescription("description");
        taskInfo.setDueDate(new Date(0));
        taskInfo.setTaskDefinitionKey("taskDefinitionKey");
        taskInfo.setParentTaskId("parentTaskId");

        // WHEN: creating a ComparableTask based on the given TaskInfo
        ComparableTask comparableTask = new ComparableTask((TaskInfo)taskInfo);

        // THEN: the ComparableTask should have the same values as the TaskInfo
        assertThat(comparableTask.getName()).isEqualTo(taskInfo.getName()).isEqualTo("name");
        assertThat(comparableTask.getId()).isEqualTo(taskInfo.getId()).isEqualTo("id");
        assertThat(comparableTask.getDescription()).isEqualTo(taskInfo.getDescription()).isEqualTo("description");
        assertThat(comparableTask.getCategory()).isEqualTo(taskInfo.getCategory()).isEqualTo("category");
        assertThat(comparableTask.getTaskDefinitionKey()).isEqualTo(taskInfo.getTaskDefinitionKey()).isEqualTo("taskDefinitionKey");
        assertThat(comparableTask.getDueDate()).isEqualTo(taskInfo.getDueDate()).isEqualTo(new Date(0));
        assertThat(comparableTask.getPriority()).isEqualTo(taskInfo.getPriority()).isEqualTo(1);
        assertThat(comparableTask.getOwner()).isEqualTo(taskInfo.getOwner()).isEqualTo("owner");
        assertThat(comparableTask.getParentTaskId()).isEqualTo(taskInfo.getParentTaskId()).isEqualTo("parentTaskId");

        // THEN: the ComparableTask is based on the TaskInfo
        assertThat(comparableTask.getOriginalTaskClass()).isEqualTo(TaskInfo.class);
    }
}
