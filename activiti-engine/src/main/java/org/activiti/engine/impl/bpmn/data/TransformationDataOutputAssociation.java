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
package org.activiti.engine.impl.bpmn.data;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;

/**
 * A transformation based data output association
 * 

 */
public class TransformationDataOutputAssociation extends AbstractDataAssociation {

  private static final long serialVersionUID = 1L;

  protected Expression transformation;

  public TransformationDataOutputAssociation(String sourceRef, String targetRef, Expression transformation) {
    super(sourceRef, targetRef);
    this.transformation = transformation;
  }

  public void evaluate(DelegateExecution execution) {
    Object value = this.transformation.getValue(execution);
    execution.setVariable(this.getTarget(), value);
  }
}
