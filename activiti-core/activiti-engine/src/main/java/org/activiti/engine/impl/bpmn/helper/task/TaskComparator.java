package org.activiti.engine.impl.bpmn.helper.task;

public interface TaskComparator<Entity> {

    void setOriginalTask(Entity originalTask);
    Entity getOriginalTask();

    boolean hasTaskNameChanged(Entity newTask);
    boolean hasTaskDescriptionChanged(Entity newTask);
    boolean hasTaskOwnerChanged(Entity newTask);
    boolean hasTaskPriorityChanged(Entity newTask);
    boolean hasTaskCategoryChanged(Entity newTask);
    boolean hasTaskFormKeyChanged(Entity newTask);
    boolean hasTaskParentIdChanged(Entity newTask);
    boolean hasTaskDefinitionKeyChanged(Entity newTask);
    boolean hasTaskAssigneeChanged(Entity newTask);
    boolean hasTaskDueDateChanged(Entity newTask);

    default boolean hasTaskChanged(Entity newTask) {
        return hasTaskNameChanged(newTask)
            || hasTaskDescriptionChanged(newTask)
            || hasTaskOwnerChanged(newTask)
            || hasTaskPriorityChanged(newTask)
            || hasTaskCategoryChanged(newTask)
            || hasTaskFormKeyChanged(newTask)
            || hasTaskParentIdChanged(newTask)
            || hasTaskDefinitionKeyChanged(newTask)
            || hasTaskAssigneeChanged(newTask)
            || hasTaskDueDateChanged(newTask);
    }

}
