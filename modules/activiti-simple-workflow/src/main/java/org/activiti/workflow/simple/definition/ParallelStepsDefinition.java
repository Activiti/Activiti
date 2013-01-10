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
package org.activiti.workflow.simple.definition;

import org.activiti.engine.ActivitiException;

/**
 * Defines a block of steps that all must be executed in parallel.
 * 
 * @author Joram Barrez
 */
public class ParallelStepsDefinition extends AbstractStepDefinitionContainer<ParallelStepsDefinition> implements StepDefinition {

  protected WorkflowDefinition workflowDefinition;
  
  public ParallelStepsDefinition() {
    
  }
  
  public ParallelStepsDefinition(WorkflowDefinition workflowDefinition) {
    this.workflowDefinition = workflowDefinition;
  }
  
  public WorkflowDefinition endParallel() {
    if (workflowDefinition == null) {
      throw new ActivitiException("Can only call endParallel when inParallel was called on a workflow definition first");
    }
    return workflowDefinition;
  }
  
}
