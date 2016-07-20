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

package org.activiti.form.engine.impl.el;

import java.util.Map;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;

import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.FormExpression;

/**
 * Expression implementation backed by a JUEL {@link ValueExpression}.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class JuelExpression implements FormExpression {

  private static final long serialVersionUID = 1L;
  
  protected String expressionText;
  protected ValueExpression valueExpression;
  protected ExpressionManager expressionManager;

  public JuelExpression(ValueExpression valueExpression, String expressionText, ExpressionManager expressionManager) {
    this.valueExpression = valueExpression;
    this.expressionText = expressionText;
    this.expressionManager = expressionManager;
  }

  public Object getValue(Map<String, Object> variables) {
    ELContext elContext = expressionManager.createElContext(variables);
    try {
      return valueExpression.getValue(elContext);
      
    } catch (PropertyNotFoundException pnfe) {
      throw new ActivitiFormException("Unknown property used in expression: " + expressionText, pnfe);
    } catch (MethodNotFoundException mnfe) {
      throw new ActivitiFormException("Unknown method used in expression: " + expressionText, mnfe);
    } catch (ELException ele) {
      throw new ActivitiFormException("Error while evaluating expression: " + expressionText, ele);
    } catch (Exception e) {
      throw new ActivitiFormException("Error while evaluating expression: " + expressionText, e);
    }
  }

  public void setValue(Object value, Map<String, Object> variables) {
    // set value is not implemented
  }

  @Override
  public String toString() {
    if (valueExpression != null) {
      return valueExpression.getExpressionString();
    }
    return super.toString();
  }

  public String getExpressionText() {
    return expressionText;
  }
}
