package org.activiti.engine.impl.delegate;

import org.activiti.engine.impl.javax.el.ValueExpression;

/**
 * Baseclass responsible for handling invocations of Expressions
 * 
 * @author Daniel Meyer
 */
public abstract class ExpressionInvocation extends DelegateInvocation {

  protected final ValueExpression valueExpression;
  
  public ExpressionInvocation(ValueExpression valueExpression) {
    this.valueExpression = valueExpression;
  }

  public Object getTarget() {
    return valueExpression; 
  }
  
}
