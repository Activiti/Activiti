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
package org.activiti.form.engine.impl.cmd;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.editor.form.converter.FormJsonConverter;
import org.activiti.form.api.SubmittedForm;
import org.activiti.form.api.SubmittedFormQuery;
import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.ActivitiFormObjectNotFoundException;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.FormExpression;
import org.activiti.form.engine.impl.interceptor.Command;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.form.engine.impl.persistence.deploy.FormCacheEntry;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.model.CompletedFormDefinition;
import org.activiti.form.model.ExpressionFormField;
import org.activiti.form.model.FormDefinition;
import org.activiti.form.model.FormField;
import org.activiti.form.model.FormFieldTypes;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
public class GetCompletedFormDefinitionCmd implements Command<CompletedFormDefinition>, Serializable {

  private static Logger logger = LoggerFactory.getLogger(GetCompletedFormDefinitionCmd.class);
  
  private static final long serialVersionUID = 1L;

  protected String formDefinitionKey;
  protected String parentDeploymentId;
  protected String formId;
  protected String taskId;
  protected String processInstanceId;
  protected String tenantId;
  protected Map<String, Object> variables;
  
  public GetCompletedFormDefinitionCmd(String formDefinitionKey, String formId, String taskId, 
      String processInstanceId, Map<String, Object> variables) {
    
    initializeValues(formDefinitionKey, null, formId, null, taskId, processInstanceId, variables);
  }
  
  public GetCompletedFormDefinitionCmd(String formDefinitionKey, String parentDeploymentId, String formId, String taskId, 
      String processInstanceId, Map<String, Object> variables) {
    
    initializeValues(formDefinitionKey, parentDeploymentId, formId, null, taskId, processInstanceId, variables);
  }
  
  public GetCompletedFormDefinitionCmd(String formDefinitionKey, String parentDeploymentId, String formId, String taskId, 
      String processInstanceId, String tenantId, Map<String, Object> variables) {
    
    initializeValues(formDefinitionKey, parentDeploymentId, formId, tenantId, taskId, processInstanceId, variables);
  }

  public CompletedFormDefinition execute(CommandContext commandContext) {
    if (taskId == null && processInstanceId == null) {
      throw new ActivitiFormException("A task id or process instance id should be provided");
    }
    
    FormCacheEntry formCacheEntry = resolveForm(commandContext);
    SubmittedForm submittedForm = resolveSubmittedForm(commandContext);
    CompletedFormDefinition formDefinition = resolveRuntimeFormDefinition(formCacheEntry, submittedForm, commandContext);
    fillFormFieldValues(submittedForm, formDefinition, commandContext);
    return formDefinition;
  }
  
  protected void initializeValues(String formDefinitionKey, String parentDeploymentId, String formId, String tenantId, 
      String taskId, String processInstanceId, Map<String, Object> variables) {
    
    this.formDefinitionKey = formDefinitionKey;
    this.parentDeploymentId = parentDeploymentId;
    this.formId = formId;
    this.tenantId = tenantId;
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    if (variables != null) {
      this.variables = variables;
    } else {
      this.variables = new HashMap<String, Object>();
    }
  }

  protected void fillFormFieldValues(SubmittedForm submittedForm, CompletedFormDefinition formDefinition, CommandContext commandContext) {

    FormEngineConfiguration formEngineConfiguration = commandContext.getFormEngineConfiguration();
    List<FormField> allFields = formDefinition.listAllFields();
    if (allFields != null) {

      Map<String, JsonNode> submittedFormFieldMap = fillPreviousFormValues(submittedForm, formEngineConfiguration);
      fillSubmittedFormValues(formDefinition, submittedForm, submittedFormFieldMap, formEngineConfiguration.getObjectMapper());
      fillVariablesWithFormValues(submittedFormFieldMap, allFields);
      
      for (FormField field : allFields) {
        if (field instanceof ExpressionFormField) {
          ExpressionFormField expressionField = (ExpressionFormField) field;
          FormExpression formExpression = formEngineConfiguration.getExpressionManager().createExpression(expressionField.getExpression());
          try {
            field.setValue(formExpression.getValue(variables));
          } catch (Exception e) {
            logger.error("Error getting value for expression " + expressionField.getExpression() + " " + e.getMessage());
          }
          
        } else {
          field.setValue(variables.get(field.getId()));
        }
        
        field.setReadOnly(true);
      }
    }
  }
  
  protected FormCacheEntry resolveForm(CommandContext commandContext) {
    DeploymentManager deploymentManager = commandContext.getFormEngineConfiguration().getDeploymentManager();

    // Find the form definition
    FormEntity formEntity = null;
    if (formId != null) {

      formEntity = deploymentManager.findDeployedFormById(formId);
      if (formEntity == null) {
        throw new ActivitiFormObjectNotFoundException("No form found for id = '" + formId + "'", FormEntity.class);
      }

    } else if (formDefinitionKey != null && (tenantId == null || FormEngineConfiguration.NO_TENANT_ID.equals(tenantId)) && parentDeploymentId == null) {

      formEntity = deploymentManager.findDeployedLatestFormByKey(formDefinitionKey);
      if (formEntity == null) {
        throw new ActivitiFormObjectNotFoundException("No form found for key '" + formDefinitionKey + "'", FormEntity.class);
      }

    } else if (formDefinitionKey != null && tenantId != null && !FormEngineConfiguration.NO_TENANT_ID.equals(tenantId)  && parentDeploymentId == null) {

      formEntity = deploymentManager.findDeployedLatestFormByKeyAndTenantId(formDefinitionKey, tenantId);
      if (formEntity == null) {
        throw new ActivitiFormObjectNotFoundException("No form found for key '" + formDefinitionKey + "' for tenant identifier " + tenantId, FormEntity.class);
      }
      
    } else if (formDefinitionKey != null && (tenantId == null || FormEngineConfiguration.NO_TENANT_ID.equals(tenantId)) && parentDeploymentId != null) {

      formEntity = deploymentManager.findDeployedLatestFormByKeyAndParentDeploymentId(formDefinitionKey, parentDeploymentId);
      if (formEntity == null) {
        throw new ActivitiFormObjectNotFoundException("No form found for key '" + formDefinitionKey + 
            "' for parent deployment id " + parentDeploymentId, FormEntity.class);
      }
      
    } else if (formDefinitionKey != null && tenantId != null && !FormEngineConfiguration.NO_TENANT_ID.equals(tenantId)  && parentDeploymentId != null) {

      formEntity = deploymentManager.findDeployedLatestFormByKeyParentDeploymentIdAndTenantId(formDefinitionKey, parentDeploymentId, tenantId);
      if (formEntity == null) {
        throw new ActivitiFormObjectNotFoundException("No form found for key '" + formDefinitionKey + 
            "' for parent deployment id '" + parentDeploymentId + "' and for tenant identifier " + tenantId, FormEntity.class);
      }

    } else {
      throw new ActivitiFormObjectNotFoundException("formDefinitionKey and formDefinitionId are null");
    }

    FormCacheEntry formCacheEntry = deploymentManager.resolveForm(formEntity);
    
    return formCacheEntry;
  }
  
  protected SubmittedForm resolveSubmittedForm(CommandContext commandContext) {
    FormEngineConfiguration formEngineConfiguration = commandContext.getFormEngineConfiguration();
    SubmittedFormQuery submittedFormQuery = formEngineConfiguration.getFormService().createSubmittedFormQuery().formId(formId);
    if (taskId != null) {
      submittedFormQuery.taskId(taskId);
    } else {
      submittedFormQuery.processInstanceId(processInstanceId);
    }
    
    List<SubmittedForm> submittedForms = submittedFormQuery.list();
    
    if (submittedForms.size() == 0) {
      throw new ActivitiFormException("No submitted form could be found");
    }
    
    SubmittedForm submittedForm = null;
    if (taskId != null) {
      if (submittedForms.size() > 1) {
        throw new ActivitiFormException("Multiple submitted forms are found for the same task");
      }
      
      submittedForm = submittedForms.get(0);
    
    } else {
      for (SubmittedForm form : submittedForms) {
        if (form.getTaskId() == null) {
          submittedForm = form;
          break;
        }
      }
    }
    
    if (submittedForm == null) {
      throw new ActivitiFormException("No submitted form could be found");
    }
    
    return submittedForm;
  }
  
  protected Map<String, JsonNode> fillPreviousFormValues(SubmittedForm submittedForm, FormEngineConfiguration formEngineConfiguration) {
    Map<String, JsonNode> submittedFormMap = new HashMap<String, JsonNode>();
    if (taskId != null && processInstanceId != null) {
      List<SubmittedForm> submittedForms = formEngineConfiguration.getFormService().createSubmittedFormQuery()
        .processInstanceId(processInstanceId)
        .submittedDateBefore(submittedForm.getSubmittedDate())
        .orderBySubmittedDate()
        .desc()
        .list();

      for (SubmittedForm otherForm : submittedForms) {
        if (otherForm.getId().equals(submittedForm.getId())) {
          continue;
        }
        
        try {
          JsonNode submittedNode = formEngineConfiguration.getObjectMapper().readTree(submittedForm.getFormValueBytes());
          if (submittedNode == null || submittedNode.get("values") != null) {
            continue;
          }
         
          JsonNode valuesNode = submittedNode.get("values");
          Iterator<String> fieldIdIterator = valuesNode.fieldNames();
          while (fieldIdIterator.hasNext()) {
            String fieldId = fieldIdIterator.next();
            if (submittedFormMap.containsKey(fieldId) == false) {
  
              JsonNode valueNode = valuesNode.get(fieldId);
              submittedFormMap.put(fieldId, valueNode);
            }
          }

        } catch (Exception e) {
          throw new ActivitiFormException("Error parsing submitted form " + submittedForm.getId());
        }
      }
    }
    
    return submittedFormMap;
  }
  
  protected void fillSubmittedFormValues(CompletedFormDefinition runtimeFormDefinition, SubmittedForm submittedForm, 
      Map<String, JsonNode> submittedFormFieldMap, ObjectMapper objectMapper) {
    
    try {
      JsonNode submittedNode = objectMapper.readTree(submittedForm.getFormValueBytes());
      if (submittedNode == null) {
        return;
      }
     
      if (submittedNode.get("values") != null) {
        JsonNode valuesNode = submittedNode.get("values");
        Iterator<String> fieldIdIterator = valuesNode.fieldNames();
        while (fieldIdIterator.hasNext()) {
          String fieldId = fieldIdIterator.next();
          JsonNode valueNode = valuesNode.get(fieldId);
          submittedFormFieldMap.put(fieldId, valueNode);
        }
      }
      
      if (submittedNode.get("outcome") != null) {
        JsonNode outcomeNode = submittedNode.get("outcome");
        if (outcomeNode.isNull() == false && StringUtils.isNotEmpty(outcomeNode.asText())) {
          runtimeFormDefinition.setSelectedOutcome(outcomeNode.asText());
        }
      }

    } catch (Exception e) {
      throw new ActivitiFormException("Error parsing submitted form " + submittedForm.getId(), e);
    }
  }
  
  public void fillVariablesWithFormValues(Map<String, JsonNode> submittedFormFieldMap, List<FormField> allFields) {
    for (FormField field : allFields) {
      
      JsonNode fieldValueNode = submittedFormFieldMap.get(field.getId());
  
      if (fieldValueNode == null || fieldValueNode.isNull()) {
        continue;
      }
  
      String fieldType = field.getType();
      String fieldValue = fieldValueNode.asText();
  
      if (FormFieldTypes.DATE.equals(fieldType)) {
        try {
          if (StringUtils.isNotEmpty(fieldValue)) {
            LocalDate dateValue = LocalDate.parse(fieldValue);
            variables.put(field.getId(), dateValue.toString("d-M-yyyy"));
          }
        } catch (Exception e) {
          logger.error("Error parsing form date value for process instance " + processInstanceId + " and task " + taskId + " with value " + fieldValue, e);
        }
  
      } else {
        variables.put(field.getId(), fieldValue);
      }
    }
  }
  
  protected CompletedFormDefinition resolveRuntimeFormDefinition(FormCacheEntry formCacheEntry, 
      SubmittedForm submittedForm, CommandContext commandContext) {
    
    FormEntity formEntity = formCacheEntry.getFormEntity();
    FormJsonConverter formJsonConverter = commandContext.getFormEngineConfiguration().getFormJsonConverter();
    FormDefinition formDefinition = formJsonConverter.convertToForm(formCacheEntry.getFormJson(), formEntity.getId(), formEntity.getVersion());
    CompletedFormDefinition runtimeFormDefinition = new CompletedFormDefinition(formDefinition);
    runtimeFormDefinition.setId(formEntity.getId());
    runtimeFormDefinition.setName(formEntity.getName());
    runtimeFormDefinition.setKey(formEntity.getKey());
    runtimeFormDefinition.setTenantId(formEntity.getTenantId());
    
    runtimeFormDefinition.setSubmittedFormId(submittedForm.getId());
    runtimeFormDefinition.setTaskId(submittedForm.getTaskId());
    runtimeFormDefinition.setProcessInstanceId(submittedForm.getProcessInstanceId());
    runtimeFormDefinition.setProcessDefinitionId(submittedForm.getProcessDefinitionId());
    runtimeFormDefinition.setSubmittedBy(submittedForm.getSubmittedBy());
    runtimeFormDefinition.setSubmittedDate(submittedForm.getSubmittedDate());
    
    return runtimeFormDefinition;
  }
}