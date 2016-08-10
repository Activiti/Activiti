package org.activiti.standalone.rules;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.BusinessRuleTaskDelegate;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

public class CustomBusinessRuleTask implements BusinessRuleTaskDelegate {
  
  private static final long serialVersionUID = 1L;
  
  public static List<Expression> ruleInputVariables = new ArrayList<Expression>();
  public static List<Expression> ruleIds = new ArrayList<Expression>();
  public static Boolean exclude;
  public static String resultVariableName;

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    execution.setVariable("test", "test2");
  }

  @Override
  public void addRuleVariableInputIdExpression(Expression inputId) {
    ruleInputVariables.add(inputId);
  }

  @Override
  public void addRuleIdExpression(Expression inputId) {
    ruleIds.add(inputId);
  }

  @Override
  public void setExclude(boolean exclude) {
    this.exclude = exclude;
  }

  @Override
  public void setResultVariable(String resultVariableName) {
    this.resultVariableName = resultVariableName;
  }

}
