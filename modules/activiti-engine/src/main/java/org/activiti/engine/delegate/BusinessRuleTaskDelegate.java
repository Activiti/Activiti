package org.activiti.engine.delegate;

import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;

public interface BusinessRuleTaskDelegate extends ActivityBehavior {
  
  void addRuleVariableInputIdExpression(Expression inputId);
  
  void addRuleIdExpression(Expression inputId);
  
  void setExclude(boolean exclude);
  
  void setResultVariable(String resultVariableName);
}
