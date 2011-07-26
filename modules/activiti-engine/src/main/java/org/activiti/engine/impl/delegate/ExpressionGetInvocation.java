package org.activiti.engine.impl.delegate;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ValueExpression;

/**
 * Class responsible for handling Expression.getValue invocations
 * 
 * @author Daniel Meyer
 */
public class ExpressionGetInvocation extends ExpressionInvocation {
  
  protected final ELContext elContext;
  
  public ExpressionGetInvocation(ValueExpression valueExpression, ELContext elContext) {
    super(valueExpression);
    this.elContext = elContext;
  }
  
  protected void invoke() throws Exception {    
    invocationResult = valueExpression.getValue(elContext);
  }

}
