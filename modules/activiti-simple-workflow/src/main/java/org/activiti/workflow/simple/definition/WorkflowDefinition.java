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

import java.util.ArrayList;
import java.util.List;


/**
 * @author Joram Barrez
 */
public class WorkflowDefinition implements StepDefinitionContainer {

  protected String name;
  protected String description;
  protected List<StepDefinition> steps = new ArrayList<StepDefinition>();
  protected ParallelBlock currentParallelBlock;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public WorkflowDefinition name(String name) {
    setName(name);
    return this;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
  
  public WorkflowDefinition description(String description) {
    setDescription(description);
    return this;
  }
  
  public void addStep(StepDefinition stepDefinition) {
    steps.add(stepDefinition);
  }

  public List<StepDefinition> getSteps() {
    return steps;
  }

  public WorkflowDefinition addHumanStep(String name, String assignee) {
    return addHumanStep(name, assignee, false);
  }

  public WorkflowDefinition addHumanStepForWorkflowInitiator(String name) {
    return addHumanStep(name, null, true);
  }

  protected WorkflowDefinition addHumanStep(String name, String assignee, boolean initiator) {
    HumanStepDefinition humanStepDefinition = new HumanStepDefinition();

    if (name != null) {
      humanStepDefinition.setName(name);
    }

    if (assignee != null) {
      humanStepDefinition.setAssignee(assignee);
    }

    humanStepDefinition.setAssigneeInitiator(initiator);
    humanStepDefinition.setStartWithPrevious(currentParallelBlock != null);

    addStep(humanStepDefinition);
    return this;
  }
  
  public ParallelBlock inParallel() {
    currentParallelBlock = new ParallelBlock(this);
    return currentParallelBlock;
  }
  
  public WorkflowDefinition endParallel() {
    currentParallelBlock = null;
    return this;
  }

  // Helper classes
  
  public static class ParallelBlock implements StepDefinitionContainer {
    
    protected WorkflowDefinition workflowDefinition;
    
    public ParallelBlock(WorkflowDefinition workflowDefinition) {
      this.workflowDefinition = workflowDefinition;
    }
    
    public void addStep(StepDefinition stepDefinition) {
      workflowDefinition.addStep(stepDefinition);
    }

    public ParallelBlock addHumanStep(String name, String assignee) {
      workflowDefinition.addHumanStep(name, assignee);
      return this;
    }

    public ParallelBlock addHumanStepForWorkflowInitiator(String name) {
      workflowDefinition.addHumanStepForWorkflowInitiator(name);
      return this;
    }
    
    public WorkflowDefinition endParallel() {
      workflowDefinition.endParallel();
      return workflowDefinition;
    }

    
  }

}
