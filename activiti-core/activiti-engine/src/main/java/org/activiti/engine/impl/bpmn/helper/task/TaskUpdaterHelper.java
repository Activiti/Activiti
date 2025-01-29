package org.activiti.engine.impl.bpmn.helper.task;

public interface TaskUpdaterHelper<Entity> {

    void updateTask(Entity originalTask, Entity task);
}
