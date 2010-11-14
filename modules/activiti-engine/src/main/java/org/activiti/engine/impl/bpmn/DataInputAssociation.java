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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * A data input association between a source and a target
 * 
 * @author Esteban Robles Luna
 */
public class DataInputAssociation {

  protected String source;
  
  protected String target;
  
  protected List<Assignment> assignments;
  
  public DataInputAssociation(String source, String target) {
    this.source = source;
    this.target = target;
    this.assignments = new ArrayList<Assignment>();
  }
  
  public void addAssignment(Assignment assignment) {
    this.assignments.add(assignment);
  }

  public void evaluate(ActivityExecution execution) {
    for (Assignment assignment : this.assignments) {
      assignment.evaluate(execution);
    }
  }
  
  public String getSource() {
    return source;
  }
  
  public String getTarget() {
    return target;
  }
}
