package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.Task;
import org.activiti.dmn.api.DmnRuleService;
import org.activiti.dmn.api.RuleEngineExecutionResult;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateHelper;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.repository.ProcessDefinition;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class DmnActivityBehavior extends TaskActivityBehavior {

  private static final long serialVersionUID = 1L;
  
  protected static final String EXPRESSION_DECISION_TABLE_REFERENCE_KEY = "decisionTableReferenceKey";
  
  protected Task task;
  
  public DmnActivityBehavior(Task task) {
    this.task = task;
  }

  public void execute(DelegateExecution execution) {
    FieldExtension fieldExtension = DelegateHelper.getFlowElementField(execution, EXPRESSION_DECISION_TABLE_REFERENCE_KEY);
    if (fieldExtension == null || ((fieldExtension.getStringValue() == null || fieldExtension.getStringValue().length() == 0) && 
        (fieldExtension.getExpression() == null || fieldExtension.getExpression().length() == 0))) {
      
      throw new ActivitiException("decisionTableReferenceKey is a required field extension for the dmn task " + task.getId());
    }
    
    String activeDecisionTableKey = null;
    if (fieldExtension.getExpression() != null && fieldExtension.getExpression().length() > 0) {
      activeDecisionTableKey = fieldExtension.getExpression();
    
    } else {
      activeDecisionTableKey = fieldExtension.getStringValue();
    }
    
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();
    
    if (processEngineConfiguration.isEnableProcessDefinitionInfoCache()) {
      ObjectNode taskElementProperties = Context.getBpmnOverrideElementProperties(task.getId(), execution.getProcessDefinitionId());
      activeDecisionTableKey = getActiveValue(activeDecisionTableKey, DynamicBpmnConstants.DMN_TASK_DECISION_TABLE_KEY, taskElementProperties); 
    }
    
    String finaldecisionTableKeyValue = null;
    Object decisionTableKeyValue = expressionManager.createExpression(activeDecisionTableKey).getValue(execution);
    if (decisionTableKeyValue != null) {
      if (decisionTableKeyValue instanceof String) {
        finaldecisionTableKeyValue = (String) decisionTableKeyValue;
      } else {
        throw new ActivitiIllegalArgumentException("decisionTableReferenceKey expression does not resolve to a string: " + decisionTableKeyValue);
      }
    }
    
    if (finaldecisionTableKeyValue == null || finaldecisionTableKeyValue.length() == 0) {
      throw new ActivitiIllegalArgumentException("decisionTableReferenceKey expression resolves to an empty value: " + decisionTableKeyValue);
    }
    
    ProcessDefinition processDefinition = ProcessDefinitionUtil.getProcessDefinition(execution.getProcessDefinitionId());

    DmnRuleService ruleService = processEngineConfiguration.getDmnEngineRuleService();
    RuleEngineExecutionResult executionResult = ruleService.executeDecisionByKeyParentDeploymentIdAndTenantId(finaldecisionTableKeyValue, 
        processDefinition.getDeploymentId(), execution.getVariables(), execution.getTenantId());
    
    execution.setVariables(executionResult.getResultVariables());
    
    leave(execution);
  }
}
