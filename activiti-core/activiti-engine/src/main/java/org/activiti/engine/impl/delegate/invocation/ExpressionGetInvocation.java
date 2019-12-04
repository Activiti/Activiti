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
package org.activiti.engine.impl.delegate.invocation;

import javax.el.ELContext;
import javax.el.ValueExpression;

/**
 * Class responsible for handling Expression.getValue invocations
 * 

 */
public class ExpressionGetInvocation extends ExpressionInvocation {

  protected final ELContext elContext;

  public ExpressionGetInvocation(ValueExpression valueExpression, ELContext elContext) {
    super(valueExpression);
    this.elContext = elContext;
  }

  protected void invoke() {
    invocationResult = valueExpression.getValue(elContext);
  }

}
