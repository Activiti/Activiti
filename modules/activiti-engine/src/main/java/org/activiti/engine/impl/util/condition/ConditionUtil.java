package org.activiti.engine.impl.util.condition;

import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Joram Barrez
 */
public class ConditionUtil {

  public static boolean hasTrueCondition(SequenceFlow sequenceFlow, ActivityExecution execution) {
    String conditionExpression = sequenceFlow.getConditionExpression();
    if (StringUtils.isNotEmpty(conditionExpression)) {

      // TODO: should be done at parse time?
      Expression expression = Context.getProcessEngineConfiguration().getExpressionManager().createExpression(sequenceFlow.getConditionExpression());
      Condition condition = new UelExpressionCondition(expression);
      if (condition.evaluate(execution)) {
        return true;
      }

      return false;

    } else {
      return true;
    }

  }

}
