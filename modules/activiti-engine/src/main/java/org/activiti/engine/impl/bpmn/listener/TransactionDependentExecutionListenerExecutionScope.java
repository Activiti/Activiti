/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.activiti.engine.impl.bpmn.listener;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TransactionDependentExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import java.util.Map;

/**
 * @author Yvo Swillens
 */
public class TransactionDependentExecutionListenerExecutionScope {

  protected TransactionDependentExecutionListener executionListener;
  protected FlowElement flowElement;
  protected Map<String, Object> variables;

  private TransactionDependentExecutionListenerExecutionScope() {
  }

  public TransactionDependentExecutionListenerExecutionScope(TransactionDependentExecutionListener executionListener, FlowElement flowElement) {
    this.executionListener = executionListener;
    this.flowElement = flowElement;
  }

  public TransactionDependentExecutionListenerExecutionScope(TransactionDependentExecutionListener executionListener, FlowElement flowElement, Map<String, Object> variables) {
    this.executionListener = executionListener;
    this.flowElement = flowElement;
    this.variables = variables;
  }

  public TransactionDependentExecutionListener getExecutionListener() {
    return executionListener;
  }

  public FlowElement getFlowElement() {
    return flowElement;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }
}
