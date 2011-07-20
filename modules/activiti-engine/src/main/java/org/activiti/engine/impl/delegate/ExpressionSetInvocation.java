package org.activiti.engine.impl.delegate;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ValueExpression;

/**
 * Class responsible for handling Expression.setValue() invocations.
 * 
 * @author Daniel Meyer
 */
public class ExpressionSetInvocation extends ExpressionInvocation {
    
  protected final Object value;
  protected ELContext elContext;

  public ExpressionSetInvocation(ValueExpression valueExpression, ELContext elContext, Object value) {
    super(valueExpression);
    this.value = value;
    this.elContext = elContext;
    this.invocationParameters = new Object[] {value};
  }

  @Override
  protected void invoke() throws Exception {
    valueExpression.setValue(elContext, value);
  }

}
