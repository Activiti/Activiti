/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.el;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.Condition;
import org.activiti.pvm.activity.ActivityContext;


/**
 * Condition that resolves a UEL value expression at runtime.
 * 
 * @author Joram Barrez
 */
public class UelValueExpressionCondition implements Condition {
  
  protected ActivitiValueExpression valueExpression;
  
  public UelValueExpressionCondition(ActivitiValueExpression valueExpression) {
    this.valueExpression = valueExpression;
  }
  
  public boolean evaluate(ActivityContext activityContext) {
    Object result = valueExpression.getValue(activityContext);
    
    if (result==null) {
      throw new ActivitiException("condition expression '"+valueExpression+"' returns null");
    }
    if (! (result instanceof Boolean)) {
      throw new ActivitiException("condition expression '"+valueExpression+"' returns non-Boolean: "+result+" ("+result.getClass().getName()+")");
    }
    return (Boolean) result;
  }

}
