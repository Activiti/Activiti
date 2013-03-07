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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.workflow.simple.definition.AbstractNamedStepDefinition;
import org.activiti.workflow.simple.definition.FeedbackStepDefinition;
import org.activiti.workflow.simple.definition.FormDefinition;
import org.activiti.workflow.simple.definition.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.ParallelStepsDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.StepDefinitionContainer;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Joram Barrez
 */
public class JsonConverter {
  
  public static final String WORKFLOW_ID = "id";
  public static final String WORKFLOW_KEY = "key";
  public static final String WORKFLOW_NAME = "name";
  public static final String WORKFLOW_DESCRIPTION = "description";
  public static final String WORKFLOW_STEPS = "steps";
  
  public static final String STEP_TYPE = "type";
  
  public static final String STEP_TYPE_HUMAN_STEP = "human-step";
  
  public static final String HUMAN_STEP_NAME = "name";
  public static final String HUMAN_STEP_DESCRIPTION = "description";
  public static final String HUMAN_STEP_ASSIGNEE = "assignee";
  public static final String HUMAN_STEP_ASSIGNEE_TYPE = "assignee-type";
  public static final String HUMAN_STEP_ASSIGNEE_TYPE_USER = "user";
  public static final String HUMAN_STEP_ASSIGNEE_USER = "user";
  public static final String HUMAN_STEP_ASSIGNEE_TYPE_USERS = "users";
  public static final String HUMAN_STEP_ASSIGNEE_USERS = "users";
  public static final String HUMAN_STEP_ASSIGNEE_TYPE_GROUPS = "groups";
  public static final String HUMAN_STEP_ASSIGNEE_GROUPS = "groups";
  public static final String HUMAN_STEP_ASSIGNEE_TYPE_INITIATOR = "initiator";
  public static final String HUMAN_STEP_GROUPS = "groups";
  
  public static final String STEP_TYPE_FEEDBACK_STEP = "feedback-step";
  
  public static final String FEEDBACK_STEP_INITIATOR = "initiator";
  public static final String FEEDBACK_STEP_FEEDBACK_PROVIDERS = "feedback-providers";
  
  public static final String FORM = "form";
  public static final String FORM_PROPERTY_NAME = "name";
  public static final String FORM_PROPERTY_TYPE = "type";
  public static final String FORM_PROPERTY_MANDATORY = "mandatory";
  public static final String FORM_PROPERTY_VALUES = "values";
  
  public WorkflowDefinition convertFromJson(JsonNode json) {
    WorkflowDefinition workflowDefinition = new WorkflowDefinition();
    
    // Name and description
    workflowDefinition.setName(getStringFieldValue(json, WORKFLOW_NAME, true));
    workflowDefinition.setKey(getStringFieldValue(json, WORKFLOW_KEY, false));
    workflowDefinition.setId(getStringFieldValue(json, WORKFLOW_ID, false));
    
    String description = getStringFieldValue(json, WORKFLOW_DESCRIPTION, false);
    if (description != null) {
      workflowDefinition.setDescription(description);
    }
    
    ArrayNode stepsArray = getArray(json, WORKFLOW_STEPS, true);
    Iterator<JsonNode> stepIterator = stepsArray.iterator();
    while (stepIterator.hasNext()) {
      convertAndAddStepDefinition(workflowDefinition, stepIterator.next());
    }
    
    return workflowDefinition;
  }
  
  protected void convertAndAddStepDefinition(StepDefinitionContainer<?> stepDefinitionContainerToAddTo, JsonNode stepJsonNode) {
    if (stepJsonNode.isArray()) {
      stepDefinitionContainerToAddTo.addStep(convertToParallelStepsDefinition((ArrayNode) stepJsonNode));
    } else {
      String type = getStringFieldValue(stepJsonNode, STEP_TYPE, true);
      if (STEP_TYPE_HUMAN_STEP.equals(type)) {
        stepDefinitionContainerToAddTo.addStep(convertToHumanStepDefinition(stepJsonNode));
      } else if (STEP_TYPE_FEEDBACK_STEP.equals(type)){
        stepDefinitionContainerToAddTo.addStep(convertToFeedbackStepDefinition(stepJsonNode));
      }
    }
  }
  
  protected ParallelStepsDefinition convertToParallelStepsDefinition(ArrayNode stepArray) {
    ParallelStepsDefinition parallelStepsDefinition = new ParallelStepsDefinition();
    Iterator<JsonNode> stepIterator = stepArray.iterator();
    while(stepIterator.hasNext()) {
      convertAndAddStepDefinition(parallelStepsDefinition, stepIterator.next());
    }
    
    // Must set startWithPrevious good (a bit hacky ...)
    for (int i=0; i<parallelStepsDefinition.getSteps().size(); i++) {
      StepDefinition stepDefinition = parallelStepsDefinition.getSteps().get(i);
      if (i > 0 && stepDefinition instanceof AbstractNamedStepDefinition) {
        ((AbstractNamedStepDefinition) stepDefinition).setStartsWithPrevious(true);
      }
    }
    
    return parallelStepsDefinition;
  }
  
  protected HumanStepDefinition convertToHumanStepDefinition(JsonNode humanStepJson) {
    HumanStepDefinition humanStepDefinition = new HumanStepDefinition();
    
    String name = getStringFieldValue(humanStepJson, HUMAN_STEP_NAME, false);
    if (name != null) {
      humanStepDefinition.setName(name);
    }
    
    String description = getStringFieldValue(humanStepJson, HUMAN_STEP_DESCRIPTION, false);
    if (description != null) {
      humanStepDefinition.setDescription(description);
    }
    
    JsonNode assigneeNode = getObject(humanStepJson, HUMAN_STEP_ASSIGNEE, false);
    if (assigneeNode != null) {
      
      String type = getStringFieldValue(assigneeNode, HUMAN_STEP_ASSIGNEE_TYPE, true);
      if (type.equals(HUMAN_STEP_ASSIGNEE_TYPE_USER)) {
        humanStepDefinition.setAssignee(getStringFieldValue(assigneeNode, HUMAN_STEP_ASSIGNEE_USER, true));
      } else if (type.equals(HUMAN_STEP_ASSIGNEE_TYPE_USERS)) {
        ArrayNode userArray = getArray(assigneeNode, HUMAN_STEP_ASSIGNEE_USERS, true);
        List<String> users = new ArrayList<String>();
        for (JsonNode userNode : userArray) {
          users.add(userNode.getTextValue());
        }
        humanStepDefinition.setCandidateUsers(users);
      } else if (type.equals(HUMAN_STEP_ASSIGNEE_TYPE_GROUPS)) {
        ArrayNode groupArray = getArray(assigneeNode, HUMAN_STEP_ASSIGNEE_GROUPS, true);
        List<String> groups = new ArrayList<String>();
        for (JsonNode groupNode : groupArray) {
          groups.add(groupNode.getTextValue());
        }
        humanStepDefinition.setCandidateGroups(groups);
      } else if (type.equals(HUMAN_STEP_ASSIGNEE_TYPE_INITIATOR)) {
        humanStepDefinition.setAssigneeIsInitiator(true);
      }
      
    }
    
    // Form
    ArrayNode formPropertyArray = getArray(humanStepJson, FORM, false);
    humanStepDefinition.setForm(convertToFormDefinition(formPropertyArray));
    
    return humanStepDefinition;
  }
  
  protected FeedbackStepDefinition convertToFeedbackStepDefinition(JsonNode feedbackStepNode) {
    FeedbackStepDefinition feedbackStepDefinition = new FeedbackStepDefinition();
    
    // Initiator
    feedbackStepDefinition.setFeedbackInitiator(getStringFieldValue(feedbackStepNode, FEEDBACK_STEP_INITIATOR, true));
    
    // Form
    ArrayNode formPropertyArray = getArray(feedbackStepNode, FORM, false);
    feedbackStepDefinition.setFormDefinitionForFeedbackProviders(convertToFormDefinition(formPropertyArray)); 
    
    return feedbackStepDefinition;
  }
  
  protected FormDefinition convertToFormDefinition(ArrayNode formPropertyArray) {
    if (formPropertyArray != null) {
      Iterator<JsonNode> formProperyIterator = formPropertyArray.iterator();
      FormDefinition formDefinition = new FormDefinition();
      while (formProperyIterator.hasNext()) {
        JsonNode formPropertyJsonNode = formProperyIterator.next();
        FormPropertyDefinition propertyDefinition = new FormPropertyDefinition();
        propertyDefinition.setPropertyName(getStringFieldValue(formPropertyJsonNode, FORM_PROPERTY_NAME, true));
        propertyDefinition.setType(getStringFieldValue(formPropertyJsonNode, FORM_PROPERTY_TYPE, true));
        propertyDefinition.setRequired(getBooleanValue(formPropertyJsonNode, FORM_PROPERTY_MANDATORY, false, false));
        propertyDefinition.setValues(getStringList(formPropertyJsonNode, FORM_PROPERTY_VALUES, false));
        formDefinition.addFormProperty(propertyDefinition);
      }
      return formDefinition;
    }
    return null;
  }
  
  // Json Helper methods
  
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
  
  protected boolean getBooleanValue(JsonNode json, String field, boolean mandatory, boolean defaultValue) {
    if (json.has(field)) {
      return json.get(field).asBoolean();
    } else {
      if (mandatory) {
        throw new ActivitiException("Could not convert json: " + field + "is mandatory on " + json.toString());
      } else {
        return defaultValue;
      }
    }
  }
  
  protected JsonNode getObject(JsonNode json, String field, boolean mandatory) {
    if (json.has(field)) {
      return json.get(field);
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
  
  protected List<String> getStringList(JsonNode json, String arrayName, boolean mandatory) {
    ArrayNode arrayNode = getArray(json, arrayName, mandatory);
    if (arrayNode != null) {
      List<String> list = new ArrayList<String>(arrayNode.size());
      for (JsonNode jsonNode : arrayNode) {
        list.add(jsonNode.getTextValue());
      }
      return list;
    }
    return null;
  }
  
  // Conversion to JSON  ------------------------------------------------------------------------
  
  public ObjectNode convertToJson(WorkflowDefinition workflowDefinition) {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode json = objectMapper.createObjectNode();
    
    json.put(WORKFLOW_NAME, workflowDefinition.getName());
    if(workflowDefinition.getId() != null) {
      json.put(WORKFLOW_ID, workflowDefinition.getId());
    }
    if(workflowDefinition.getKey() != null) {
      json.put(WORKFLOW_KEY, workflowDefinition.getKey());
    }
    
    if (workflowDefinition.getDescription() != null) {
      json.put(WORKFLOW_DESCRIPTION, workflowDefinition.getDescription());
    }
    
    ArrayNode steps = json.putArray(WORKFLOW_STEPS);
    for (JsonNode stepNode : convertToJson(objectMapper, workflowDefinition.getSteps())) {
      steps.add(stepNode);
    }
    
    return json;
  }
  
  protected List<JsonNode> convertToJson(ObjectMapper objectMapper, List<StepDefinition> steps) {
    List<JsonNode> convertedNodes = new ArrayList<JsonNode>();
    for (StepDefinition stepDefinition : steps) {
      convertedNodes.add(convertToJson(objectMapper, stepDefinition));
    }
    return convertedNodes;
  }
  
  protected JsonNode convertToJson(ObjectMapper objectMapper, StepDefinition stepDefinition) {
    if (stepDefinition instanceof ParallelStepsDefinition) {
      return convertToJson(objectMapper, (ParallelStepsDefinition) stepDefinition);
    } else if (stepDefinition instanceof HumanStepDefinition) {
      return convertToJson(objectMapper, (HumanStepDefinition) stepDefinition);
    } else if (stepDefinition instanceof FeedbackStepDefinition) {
      return convertToJson(objectMapper, (FeedbackStepDefinition) stepDefinition);
    } else {
      throw new ActivitiException("Unknown step definition type " + stepDefinition.getClass().getName() + ": cannot complete json conversion");
    }
  }
  
  protected ArrayNode convertToJson(ObjectMapper objectMapper, ParallelStepsDefinition parallelStepsDefinition) {
    ArrayNode parallelSteps = objectMapper.createArrayNode();
    for (JsonNode jsonNode : convertToJson(objectMapper, parallelStepsDefinition.getSteps())) {
      parallelSteps.add(jsonNode);
    }
    return parallelSteps;
  }
  
  protected JsonNode convertToJson(ObjectMapper objectMapper, HumanStepDefinition humanStepDefinition) {
    ObjectNode humanStepNode = objectMapper.createObjectNode();
    humanStepNode.put(HUMAN_STEP_NAME, humanStepDefinition.getName());
    humanStepNode.put(STEP_TYPE, STEP_TYPE_HUMAN_STEP);
    
    // Description
    if (humanStepDefinition.getDescription() != null) {
      humanStepNode.put(HUMAN_STEP_DESCRIPTION, humanStepDefinition.getDescription());
    }
    
    // Assignee
    if (humanStepDefinition.getAssignee() != null) {
      ObjectNode assigneeNode = objectMapper.createObjectNode();
      assigneeNode.put(HUMAN_STEP_ASSIGNEE_USER, humanStepDefinition.getAssignee());
      assigneeNode.put(HUMAN_STEP_ASSIGNEE_TYPE, HUMAN_STEP_ASSIGNEE_TYPE_USER);
      humanStepNode.put(HUMAN_STEP_ASSIGNEE, assigneeNode);
    }
    
    // Candidate groups
    if (humanStepDefinition.getCandidateGroups() != null && humanStepDefinition.getCandidateGroups().size() > 0) {
      ObjectNode assigneeNode = objectMapper.createObjectNode();
      ArrayNode groups = humanStepNode.putArray(HUMAN_STEP_GROUPS);
      for (String group : humanStepDefinition.getCandidateGroups()) {
        groups.add(group);
      }
      assigneeNode.put(HUMAN_STEP_ASSIGNEE_GROUPS, groups);
      assigneeNode.put(HUMAN_STEP_ASSIGNEE_TYPE, HUMAN_STEP_ASSIGNEE_TYPE_GROUPS);
      humanStepNode.put(HUMAN_STEP_ASSIGNEE, assigneeNode);
    }
    
    // Form
    if (humanStepDefinition.getForm() != null) {
      humanStepNode.put(FORM, convertFormToJson(objectMapper, humanStepDefinition.getForm()));
    }
    
    return humanStepNode;
  }
  
  protected JsonNode convertToJson(ObjectMapper objectMapper, FeedbackStepDefinition feedbackStepDefinition) {
    ObjectNode feedbackStepNode = objectMapper.createObjectNode();
    feedbackStepNode.put(FEEDBACK_STEP_INITIATOR, feedbackStepDefinition.getFeedbackInitiator());
    
    // Feedback providers
    if (feedbackStepDefinition.getFeedbackProviders() != null && feedbackStepDefinition.getFeedbackProviders().size() > 0) {
      ArrayNode feedbackProviders = feedbackStepNode.putArray(FEEDBACK_STEP_FEEDBACK_PROVIDERS);
      for (String feedbackProvider : feedbackStepDefinition.getFeedbackProviders()) {
        feedbackProviders.add(feedbackProvider);
      }
    }
    
    // Form
    if (feedbackStepDefinition.getFormDefinitionForFeedbackProviders() != null) {
      feedbackStepNode.put(FORM, convertFormToJson(objectMapper, feedbackStepDefinition.getFormDefinitionForFeedbackProviders()));
    }
    
    return feedbackStepNode;
  }
  
  protected ArrayNode convertFormToJson(ObjectMapper objectMapper, FormDefinition formDefinition) {
    ArrayNode form = objectMapper.createArrayNode();
    if (formDefinition.getFormProperties() != null && formDefinition.getFormProperties().size() > 0) {
      for (FormPropertyDefinition propertyDefinition : formDefinition.getFormProperties()) {
        ObjectNode formPropertyNode = objectMapper.createObjectNode();
        formPropertyNode.put(FORM_PROPERTY_NAME, propertyDefinition.getPropertyName());
        formPropertyNode.put(FORM_PROPERTY_TYPE, propertyDefinition.getType());
        formPropertyNode.put(FORM_PROPERTY_MANDATORY, propertyDefinition.isRequired());
        form.add(formPropertyNode);
      }
    }
    return form;
  }
  
}
