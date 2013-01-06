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
package org.activiti.workflow.simple.converter.json;

import java.util.Iterator;

import org.activiti.engine.ActivitiException;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.ParallelStepsDefinition;
import org.activiti.workflow.simple.definition.StepDefinitionContainer;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Joram Barrez
 */
@Deprecated // Dunno if I actually need this in Activiti?
public class JsonConverter {
  
  private static final String WORKFLOW_NAME = "name";
  private static final String WORKFLOW_DESCRIPTION = "description";
  private static final String WORKFLOW_STEPS = "steps";
  
  private static final String STEP_TYPE = "type";
  
  private static final String STEP_TYPE_HUMAN_STEP = "human-step";
  
  private static final String HUMAN_STEP_NAME = "name";
  private static final String HUMAN_STEP_DESCRIPTION = "description";
  private static final String HUMAN_STEP_ASSIGNEE = "assignee";
  
  public WorkflowDefinition convertJson(ObjectNode json) {
    WorkflowDefinition workflowDefinition = new WorkflowDefinition();
    
    // Name and description
    workflowDefinition.setName(getStringFieldValue(json, WORKFLOW_NAME, true));
    workflowDefinition.setDescription(getStringFieldValue(json, WORKFLOW_DESCRIPTION, false));
    
    ArrayNode stepsArray = getArray(json, WORKFLOW_STEPS, true);
    Iterator<JsonNode> stepIterator = stepsArray.iterator();
    while (stepIterator.hasNext()) {
      convertStep(workflowDefinition, stepIterator.next());
    }
    
    return workflowDefinition;
  }
  
  protected void convertStep(StepDefinitionContainer<?> stepDefinitionContainer, JsonNode stepJsonNode) {
    if (stepJsonNode.isArray()) {
      stepDefinitionContainer.addStep(convertParallelSteps((ArrayNode) stepJsonNode));
    } else {
      String type = getStringFieldValue(stepJsonNode, STEP_TYPE, true);
      if (STEP_TYPE_HUMAN_STEP.equals(type)) {
        stepDefinitionContainer.addStep(convertHumanStepJson(stepJsonNode));
      }
    }
  }
  
  protected ParallelStepsDefinition convertParallelSteps(ArrayNode stepArray) {
    ParallelStepsDefinition parallelStepsDefinition = new ParallelStepsDefinition();
    Iterator<JsonNode> stepIterator = stepArray.iterator();
    while(stepIterator.hasNext()) {
      convertStep(parallelStepsDefinition, stepIterator.next());
    }
    return parallelStepsDefinition;
  }
  
  protected HumanStepDefinition convertHumanStepJson(JsonNode humanStepJson) {
    HumanStepDefinition humanStepDefinition = new HumanStepDefinition();
    humanStepDefinition.setName(getStringFieldValue(humanStepJson, HUMAN_STEP_NAME, false));
    humanStepDefinition.setDescription(getStringFieldValue(humanStepJson, HUMAN_STEP_DESCRIPTION, false));
    humanStepDefinition.setAssignee(getStringFieldValue(humanStepJson, HUMAN_STEP_ASSIGNEE, false));
    return humanStepDefinition;
  }
  
  protected String getStringFieldValue(JsonNode json, String field, boolean mandatory) {
    if (json.has(field)) {
      JsonNode fieldNode = json.get(field);
      return fieldNode.getTextValue();
    } else {
      if (mandatory) {
        throw new ActivitiException("Could not convert json: " + field + " is mandatory on " + json.toString());
      } else {
        return null;
      }
    }
  }
  
  protected ArrayNode getArray(JsonNode json, String arrayName, boolean mandatory) {
    if (json.has(arrayName)) {
      JsonNode arrayJsonNode = json.get(arrayName);
      if (arrayJsonNode.isArray()) {
        return (ArrayNode) arrayJsonNode;
      } else {
        throw new ActivitiException("Could not convert json: " + arrayName + " should be an array, but it isn't");
      }
    } else {
      if (mandatory) {
        throw new ActivitiException("Could not convert json: " + arrayName + " is mandatory on " + json.toString());
      } else {
        return null;
      }
    }
  }
  
}
