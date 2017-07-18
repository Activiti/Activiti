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
package org.activiti.engine.impl.bpmn.parser.factory;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.FieldExtension;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.el.FixedValue;
import org.apache.commons.lang3.StringUtils;

/**

 */
public abstract class AbstractBehaviorFactory {

  protected ExpressionManager expressionManager;

  public List<FieldDeclaration> createFieldDeclarations(List<FieldExtension> fieldList) {
    List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>();

    for (FieldExtension fieldExtension : fieldList) {
      FieldDeclaration fieldDeclaration = null;
      if (StringUtils.isNotEmpty(fieldExtension.getExpression())) {
        fieldDeclaration = new FieldDeclaration(fieldExtension.getFieldName(), Expression.class.getName(), expressionManager.createExpression(fieldExtension.getExpression()));
      } else {
        fieldDeclaration = new FieldDeclaration(fieldExtension.getFieldName(), Expression.class.getName(), new FixedValue(fieldExtension.getStringValue()));
      }

      fieldDeclarations.add(fieldDeclaration);
    }
    return fieldDeclarations;
  }

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

}
