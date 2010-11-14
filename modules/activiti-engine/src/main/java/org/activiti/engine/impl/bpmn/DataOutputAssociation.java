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
package org.activiti.engine.impl.bpmn;

import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * A data output association between a source and a target
 * 
 * @author Esteban Robles Luna
 */
public class DataOutputAssociation {

  protected String targetRef;
  
  protected Expression transformation;
  
  public DataOutputAssociation(String targetRef, Expression transformation) {
    this.targetRef = targetRef;
    this.transformation = transformation;
  }

  public void evaluate(ActivityExecution execution) {
    //TODO
  }
}
