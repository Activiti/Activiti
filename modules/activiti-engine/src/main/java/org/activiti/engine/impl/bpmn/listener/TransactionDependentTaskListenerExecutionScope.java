/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.activiti.engine.impl.bpmn.listener;

import org.activiti.bpmn.model.Task;
import org.activiti.engine.delegate.TransactionDependentExecutionListener;
import org.activiti.engine.delegate.TransactionDependentTaskListener;

import java.util.Map;

/**
 * @author Yvo Swillens
 */
public class TransactionDependentTaskListenerExecutionScope {

  protected final TransactionDependentTaskListener taskListener;
  protected final String processInstanceId;
  protected final String executionId;
  protected final Task task;
  protected final Map<String, Object> executionVariables;
  protected final Map<String, Object> customPropertiesMap;

  public TransactionDependentTaskListenerExecutionScope(TransactionDependentTaskListener taskListener, String processInstanceId, String executionId,
                                                             Task task, Map<String, Object> executionVariables, Map<String, Object> customPropertiesMap) {
    this.processInstanceId = processInstanceId;
    this.executionId = executionId;
    this.taskListener = taskListener;
    this.task = task;
    this.executionVariables = executionVariables;
    this.customPropertiesMap = customPropertiesMap;
  }

  public TransactionDependentTaskListener getTaskListener() {
    return taskListener;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public Task getTask() {
    return task;
  }

  public Map<String, Object> getExecutionVariables() {
    return executionVariables;
  }

  public Map<String, Object> getCustomPropertiesMap() {
    return customPropertiesMap;
  }
}
