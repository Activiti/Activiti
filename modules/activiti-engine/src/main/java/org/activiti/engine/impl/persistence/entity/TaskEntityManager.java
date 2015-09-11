package org.activiti.engine.impl.persistence.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.task.Task;

public interface TaskEntityManager extends EntityManager<TaskEntity> {

  /**
   * Creates a new task. This task still will have to be persisted. See {@link #insert(ExecutionEntity))}.
   */
  TaskEntity create(Date createTime);

  /** creates and initializes a new persistent task. */
  TaskEntity createAndInsert(DelegateExecution execution);

  void insert(TaskEntity taskEntity, ExecutionEntity execution);

  List<TaskEntity> findTasksByExecutionId(String executionId);

  List<TaskEntity> findTasksByProcessInstanceId(String processInstanceId);

  List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery);

  List<Task> findTasksAndVariablesByQueryCriteria(TaskQueryImpl taskQuery);

  long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery);

  List<Task> findTasksByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults);

  long findTaskCountByNativeQuery(Map<String, Object> parameterMap);

  List<Task> findTasksByParentTaskId(String parentTaskId);

  TaskEntity update(TaskEntity taskEntity);
  
  void updateTaskTenantIdForDeployment(String deploymentId, String newTenantId);

  void deleteTask(String taskId, String deleteReason, boolean cascade);
  
  void deleteTasksByProcessInstanceId(String processInstanceId, String deleteReason, boolean cascade);

  void deleteTask(TaskEntity task, String deleteReason, boolean cascade, boolean cancel);

  void fireTaskListenerEvent(TaskEntity taskEntity, String taskEventName);

}