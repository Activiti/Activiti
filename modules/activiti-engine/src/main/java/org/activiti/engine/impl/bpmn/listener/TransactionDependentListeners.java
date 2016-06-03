/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.activiti.engine.impl.bpmn.listener;

import org.activiti.bpmn.model.Task;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.TransactionDependentExecutionListener;
import org.activiti.engine.delegate.TransactionDependentTaskListener;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yvo Swillens
 */
public class TransactionDependentListeners implements CommandContextCloseListener {

  protected List<TransactionDependentExecutionListenerExecutionScope> closingExecutionListeners;
  protected List<TransactionDependentExecutionListenerExecutionScope> closedExecutionListeners;
  protected List<TransactionDependentExecutionListenerExecutionScope> closeFailedExecutionListeners;
  protected List<TransactionDependentTaskListenerExecutionScope> closingTaskListeners;
  protected List<TransactionDependentTaskListenerExecutionScope> closedTaskListeners;
  protected List<TransactionDependentTaskListenerExecutionScope> closeFailedTaskListeners;

  @Override
  public void closing(CommandContext commandContext) {
    if (closingExecutionListeners != null) {
      for (TransactionDependentExecutionListenerExecutionScope executionListenerExecutionScope : closingExecutionListeners) {
        executionListenerExecutionScope.getExecutionListener().notify(executionListenerExecutionScope.getProcessInstanceId(), executionListenerExecutionScope.getExecutionId(),
                executionListenerExecutionScope.getFlowElement(), executionListenerExecutionScope.getExecutionVariables(), executionListenerExecutionScope.getCustomPropertiesMap());
      }
    }
    if (closingTaskListeners != null) {
      for (TransactionDependentTaskListenerExecutionScope taskListenerExecutionScope : closingTaskListeners) {
        taskListenerExecutionScope.getTaskListener().notify(taskListenerExecutionScope.getProcessInstanceId(), taskListenerExecutionScope.getExecutionId(),
                taskListenerExecutionScope.getTask(), taskListenerExecutionScope.getExecutionVariables(), taskListenerExecutionScope.getCustomPropertiesMap());
      }
    }
  }

  @Override
  public void closed(CommandContext commandContext) {
    if (closedExecutionListeners != null) {
      for (TransactionDependentExecutionListenerExecutionScope executionListenerExecutionScope : closedExecutionListeners) {
        executionListenerExecutionScope.getExecutionListener().notify(executionListenerExecutionScope.getProcessInstanceId(), executionListenerExecutionScope.getExecutionId(),
                executionListenerExecutionScope.getFlowElement(), executionListenerExecutionScope.getExecutionVariables(), executionListenerExecutionScope.getCustomPropertiesMap());
      }
    }
    if (closedTaskListeners != null) {
      for (TransactionDependentTaskListenerExecutionScope taskListenerExecutionScope : closedTaskListeners) {
        taskListenerExecutionScope.getTaskListener().notify(taskListenerExecutionScope.getProcessInstanceId(), taskListenerExecutionScope.getExecutionId(),
                taskListenerExecutionScope.getTask(), taskListenerExecutionScope.getExecutionVariables(), taskListenerExecutionScope.getCustomPropertiesMap());
      }
    }
  }

  @Override
  public void closeFailure(CommandContext commandContext) {
    if (closeFailedExecutionListeners != null) {
      for (TransactionDependentExecutionListenerExecutionScope executionListenerExecutionScope : closeFailedExecutionListeners) {
        executionListenerExecutionScope.getExecutionListener().notify(executionListenerExecutionScope.getProcessInstanceId(), executionListenerExecutionScope.getExecutionId(),
                executionListenerExecutionScope.getFlowElement(), executionListenerExecutionScope.getExecutionVariables(), executionListenerExecutionScope.getCustomPropertiesMap());
      }
    }
    if (closeFailedTaskListeners != null) {
      for (TransactionDependentTaskListenerExecutionScope taskListenerExecutionScope : closeFailedTaskListeners) {
        taskListenerExecutionScope.getTaskListener().notify(taskListenerExecutionScope.getProcessInstanceId(), taskListenerExecutionScope.getExecutionId(),
                taskListenerExecutionScope.getTask(), taskListenerExecutionScope.getExecutionVariables(), taskListenerExecutionScope.getCustomPropertiesMap());
      }
    }
  }

  @Override
  public void afterSessionsFlush(CommandContext commandContext) {

  }

  public void addClosingExecutionListener(TransactionDependentExecutionListener executionListener, DelegateExecution execution, Map<String, Object> executionVariablesToUse, Map<String, Object> customPropertiesMapToUse) {
    if (executionListener == null) {
      throw new ActivitiIllegalArgumentException("executionListener is null");
    }
    if (execution == null) {
      throw new ActivitiIllegalArgumentException("execution is null");
    }

    if (closingExecutionListeners == null) {
      closingExecutionListeners = new ArrayList<>();
    }

    closingExecutionListeners.add(new TransactionDependentExecutionListenerExecutionScope(executionListener, execution.getProcessInstanceId(), execution.getId(),
            execution.getCurrentFlowElement(), executionVariablesToUse, customPropertiesMapToUse));
  }

  public void addClosedExecutionListener(TransactionDependentExecutionListener executionListener, DelegateExecution execution, Map<String, Object> executionVariablesToUse, Map<String, Object> customPropertiesMapToUse) {
    if (executionListener == null) {
      throw new ActivitiIllegalArgumentException("executionListener is null");
    }
    if (execution == null) {
      throw new ActivitiIllegalArgumentException("execution is null");
    }

    if (closedExecutionListeners == null) {
      closedExecutionListeners = new ArrayList<>();
    }

    closedExecutionListeners.add(new TransactionDependentExecutionListenerExecutionScope(executionListener, execution.getProcessInstanceId(), execution.getId(),
            execution.getCurrentFlowElement(), executionVariablesToUse, customPropertiesMapToUse));
  }

  public void addCloseFailedExecutionListener(TransactionDependentExecutionListener executionListener, DelegateExecution execution, Map<String, Object> executionVariablesToUse, Map<String, Object> customPropertiesMapToUse) {
    if (executionListener == null) {
      throw new ActivitiIllegalArgumentException("executionListener is null");
    }
    if (execution == null) {
      throw new ActivitiIllegalArgumentException("execution is null");
    }

    if (closeFailedExecutionListeners == null) {
      closeFailedExecutionListeners = new ArrayList<>();
    }

    closeFailedExecutionListeners.add(new TransactionDependentExecutionListenerExecutionScope(executionListener, execution.getProcessInstanceId(), execution.getId(),
            execution.getCurrentFlowElement(), executionVariablesToUse, customPropertiesMapToUse));
  }

  public void addClosingTaskListener(TransactionDependentTaskListener taskListener, DelegateExecution execution, Map<String, Object> executionVariablesToUse, Map<String, Object> customPropertiesMapToUse) {
    if (taskListener == null) {
      throw new ActivitiIllegalArgumentException("taskListener is null");
    }
    if (execution == null) {
      throw new ActivitiIllegalArgumentException("execution is null");
    }

    if (closingTaskListeners == null) {
      closingTaskListeners = new ArrayList<>();
    }

    closingTaskListeners.add(new TransactionDependentTaskListenerExecutionScope(taskListener, execution.getProcessInstanceId(), execution.getId(),
            (Task) execution.getCurrentFlowElement(), executionVariablesToUse, customPropertiesMapToUse));
  }

  public void addClosedTaskListener(TransactionDependentTaskListener taskListener, DelegateExecution execution, Map<String, Object> executionVariablesToUse, Map<String, Object> customPropertiesMapToUse) {
    if (taskListener == null) {
      throw new ActivitiIllegalArgumentException("taskListener is null");
    }
    if (execution == null) {
      throw new ActivitiIllegalArgumentException("execution is null");
    }

    if (closedTaskListeners == null) {
      closedTaskListeners = new ArrayList<>();
    }

    closedTaskListeners.add(new TransactionDependentTaskListenerExecutionScope(taskListener, execution.getProcessInstanceId(), execution.getId(),
            (Task) execution.getCurrentFlowElement(), executionVariablesToUse, customPropertiesMapToUse));
  }

  public void addCloseFailedTaskListener(TransactionDependentTaskListener taskListener, DelegateExecution execution, Map<String, Object> executionVariablesToUse, Map<String, Object> customPropertiesMapToUse) {
    if (taskListener == null) {
      throw new ActivitiIllegalArgumentException("taskListener is null");
    }
    if (execution == null) {
      throw new ActivitiIllegalArgumentException("execution is null");
    }

    if (closeFailedTaskListeners == null) {
      closeFailedTaskListeners = new ArrayList<>();
    }

    closeFailedTaskListeners.add(new TransactionDependentTaskListenerExecutionScope(taskListener, execution.getProcessInstanceId(), execution.getId(),
            (Task) execution.getCurrentFlowElement(), executionVariablesToUse, customPropertiesMapToUse));
  }
}
