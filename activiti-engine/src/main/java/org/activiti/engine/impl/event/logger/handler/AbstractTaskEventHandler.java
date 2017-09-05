package org.activiti.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

/**

 */
public abstract class AbstractTaskEventHandler extends AbstractDatabaseEventLoggerEventHandler {

  protected Map<String, Object> handleCommonTaskFields(TaskEntity task) {
    Map<String, Object> data = new HashMap<String, Object>();
    putInMapIfNotNull(data, Fields.ID, task.getId());
    putInMapIfNotNull(data, Fields.NAME, task.getName());
    putInMapIfNotNull(data, Fields.TASK_DEFINITION_KEY, task.getTaskDefinitionKey());
    putInMapIfNotNull(data, Fields.DESCRIPTION, task.getDescription());
    putInMapIfNotNull(data, Fields.ASSIGNEE, task.getAssignee());
    putInMapIfNotNull(data, Fields.OWNER, task.getOwner());
    putInMapIfNotNull(data, Fields.CATEGORY, task.getCategory());
    putInMapIfNotNull(data, Fields.CREATE_TIME, task.getCreateTime());
    putInMapIfNotNull(data, Fields.DUE_DATE, task.getDueDate());
    putInMapIfNotNull(data, Fields.FORM_KEY, task.getFormKey());
    putInMapIfNotNull(data, Fields.PRIORITY, task.getPriority());
    putInMapIfNotNull(data, Fields.PROCESS_DEFINITION_ID, task.getProcessDefinitionId());
    putInMapIfNotNull(data, Fields.PROCESS_INSTANCE_ID, task.getProcessInstanceId());
    putInMapIfNotNull(data, Fields.EXECUTION_ID, task.getExecutionId());

    if (task.getTenantId() != null && !ProcessEngineConfigurationImpl.NO_TENANT_ID.equals(task.getTenantId())) {
      putInMapIfNotNull(data, Fields.TENANT_ID, task.getTenantId()); // Important for standalone tasks
    }
    return data;
  }

}
