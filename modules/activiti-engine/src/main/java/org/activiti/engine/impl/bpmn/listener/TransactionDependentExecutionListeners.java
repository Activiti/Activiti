/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.activiti.engine.impl.bpmn.listener;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TransactionDependentExecutionListener;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yvo Swillens
 */
public class TransactionDependentExecutionListeners implements CommandContextCloseListener {

  protected List<TransactionDependentExecutionListenerExecutionScope> closedListeners;
  protected List<TransactionDependentExecutionListenerExecutionScope> closeFailedListeners;

  @Override
  public void closing(CommandContext commandContext) {

  }

  @Override
  public void afterSessionsFlush(CommandContext commandContext) {

  }

  @Override
  public void closed(CommandContext commandContext) {
    if (closedListeners != null) {
      for (TransactionDependentExecutionListenerExecutionScope executionListenerExecutionScope : closedListeners) {
        executionListenerExecutionScope.getExecutionListener().notify(executionListenerExecutionScope.getFlowElement(), executionListenerExecutionScope.getVariables());
      }
    }
  }

  @Override
  public void closeFailure(CommandContext commandContext) {
    if (closeFailedListeners != null) {
      for (TransactionDependentExecutionListenerExecutionScope executionListenerExecutionScope : closeFailedListeners) {
        executionListenerExecutionScope.getExecutionListener().notify(executionListenerExecutionScope.getFlowElement(), executionListenerExecutionScope.getVariables());
      }
    }
  }

  public void addClosedListener(TransactionDependentExecutionListener executionListener, FlowElement flowElementToUse, Map<String, Object> variablesToUse) {
    if (executionListener == null) {
      throw new ActivitiIllegalArgumentException("executionListener is null");
    }

    if (flowElementToUse == null) {
      throw new ActivitiIllegalArgumentException("flowElementToUse is null");
    }

    if (closedListeners == null) {
      closedListeners = new ArrayList<>();
    }
    closedListeners.add(new TransactionDependentExecutionListenerExecutionScope(executionListener, flowElementToUse, variablesToUse));
  }

  public void addCloseFailedListener(TransactionDependentExecutionListener executionListener, FlowElement flowElementToUse, Map<String, Object> variablesToUse) {
    if (executionListener == null) {
      throw new ActivitiIllegalArgumentException("executionListener is null");
    }

    if (flowElementToUse == null) {
      throw new ActivitiIllegalArgumentException("flowElementToUse is null");
    }

    if (closeFailedListeners == null) {
      closeFailedListeners = new ArrayList<>();
    }
    closeFailedListeners.add(new TransactionDependentExecutionListenerExecutionScope(executionListener, flowElementToUse, variablesToUse));
  }
}
