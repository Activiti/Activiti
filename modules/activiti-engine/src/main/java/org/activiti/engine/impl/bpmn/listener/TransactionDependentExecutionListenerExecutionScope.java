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

  protected final TransactionDependentExecutionListener executionListener;
  protected final String processInstanceId;
  protected final String executionId;
  protected final FlowElement flowElement;
  protected final Map<String, Object> executionVariables;
  protected final Map<String, Object> customPropertiesMap;

  public TransactionDependentExecutionListenerExecutionScope(TransactionDependentExecutionListener executionListener, String processInstanceId, String executionId,
                                                             FlowElement flowElement, Map<String, Object> executionVariables, Map<String, Object> customPropertiesMap) {
    this.processInstanceId = processInstanceId;
    this.executionId = executionId;
    this.executionListener = executionListener;
    this.flowElement = flowElement;
    this.executionVariables = executionVariables;
    this.customPropertiesMap = customPropertiesMap;
  }

  public TransactionDependentExecutionListener getExecutionListener() {
    return executionListener;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public FlowElement getFlowElement() {
    return flowElement;
  }

  public Map<String, Object> getExecutionVariables() {
    return executionVariables;
  }

  public Map<String, Object> getCustomPropertiesMap() {
    return customPropertiesMap;
  }
}
