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
import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.ActivitiFormObjectNotFoundException;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.FormExpression;
import org.activiti.form.engine.impl.interceptor.Command;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.form.engine.impl.persistence.deploy.FormCacheEntry;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.model.ExpressionFormField;
import org.activiti.form.model.FormDefinition;
import org.activiti.form.model.FormField;
import org.activiti.form.model.FormFieldTypes;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Tijs Rademakers
 */
public class GetRuntimeFormDefinitionCmd implements Command<FormDefinition>, Serializable {

  private static Logger logger = LoggerFactory.getLogger(GetRuntimeFormDefinitionCmd.class);
  
  private static final long serialVersionUID = 1L;

  protected String formDefinitionKey;
  protected String parentDeploymentId;
  protected String formId;
  protected String processInstanceId;
  protected String tenantId;
  protected Map<String, Object> variables;
  
  public GetRuntimeFormDefinitionCmd(String formDefinitionKey, String formId, String processInstanceId, Map<String, Object> variables) {
    initializeValues(formDefinitionKey, formId, null, variables);
    this.processInstanceId = processInstanceId;
  }
  
  public GetRuntimeFormDefinitionCmd(String formDefinitionKey, String parentDeploymentId, String formId, String processInstanceId, Map<String, Object> variables) {
    initializeValues(formDefinitionKey, formId, null, variables);
    this.parentDeploymentId = parentDeploymentId;
    this.processInstanceId = processInstanceId;
  }
  
  public GetRuntimeFormDefinitionCmd(String formDefinitionKey, String parentDeploymentId, String formId, String processInstanceId, String tenantId, Map<String, Object> variables) {
    initializeValues(formDefinitionKey, formId, null, variables);
    this.parentDeploymentId = parentDeploymentId;
    this.processInstanceId = processInstanceId;
    this.tenantId = tenantId;
  }

  public FormDefinition execute(CommandContext commandContext) {
    FormCacheEntry formCacheEntry = resolveForm(commandContext);
    FormDefinition formDefinition = resolveFormDefinition(formCacheEntry, commandContext);
    fillFormFieldValues(formDefinition, commandContext);
    return formDefinition;
  }
  
  protected void initializeValues(String formDefinitionKey, String formId, String tenantId, Map<String, Object> variables) {
    this.formDefinitionKey = formDefinitionKey;
    this.formId = formId;
    this.tenantId = tenantId;
    if (variables != null) {
      this.variables = variables;
    } else {
      this.variables = new HashMap<String, Object>();
    }
  }

  protected void fillFormFieldValues(FormDefinition formDefinition, CommandContext commandContext) {

    FormEngineConfiguration formEngineConfiguration = commandContext.getFormEngineConfiguration();
    List<FormField> allFields = formDefinition.listAllFields();
    if (allFields != null) {

      Map<String, JsonNode> submittedFormFieldMap = fillPreviousFormValues(formEngineConfiguration);
      fillVariablesWithFormValues(submittedFormFieldMap, allFields);
      
      for (FormField field : allFields) {
        if (field instanceof ExpressionFormField) {
          ExpressionFormField expressionField = (ExpressionFormField) field;
          FormExpression formExpression = formEngineConfiguration.getExpressionManager().createExpression(expressionField.getExpression());
          try {
            field.setValue(formExpression.getValue(variables));
          } catch (Exception e) {
            logger.error("Error getting value for expression " + expressionField.getExpression() + " " + e.getMessage(), e);
          }
          
        } else {
          field.setValue(variables.get(field.getId()));
        }
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

    } else if (formDefinitionKey != null && tenantId != null && !FormEngineConfiguration.NO_TENANT_ID.equals(tenantId) && parentDeploymentId == null) {

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
  
  protected Map<String, JsonNode> fillPreviousFormValues(FormEngineConfiguration formEngineConfiguration) {
    Map<String, JsonNode> submittedFormMap = new HashMap<String, JsonNode>();
    if (processInstanceId != null) {
      List<SubmittedForm> submittedForms = formEngineConfiguration.getFormService().createSubmittedFormQuery()
        .processInstanceId(processInstanceId)
        .orderBySubmittedDate()
        .desc()
        .list();

      for (SubmittedForm otherForm : submittedForms) {
        try {
          JsonNode submittedNode = formEngineConfiguration.getObjectMapper().readTree(otherForm.getFormValueBytes());
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
          throw new ActivitiFormException("Error parsing submitted form " + otherForm.getId());
        }
      }
    }
    
    return submittedFormMap;
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
            variables.put(field.getId(), dateValue);
          }
        } catch (Exception e) {
          logger.error("Error parsing form date value for process instance " + processInstanceId + " with value " + fieldValue, e);
        }
  
      } else {
        variables.put(field.getId(), fieldValue);
      }
    }
  }
  
  protected FormDefinition resolveFormDefinition(FormCacheEntry formCacheEntry, CommandContext commandContext) {
    FormEntity formEntity = formCacheEntry.getFormEntity();
    FormJsonConverter formJsonConverter = commandContext.getFormEngineConfiguration().getFormJsonConverter();
    FormDefinition formDefinition = formJsonConverter.convertToForm(formCacheEntry.getFormJson(), formEntity.getId(), formEntity.getVersion());
    formDefinition.setId(formEntity.getId());
    formDefinition.setName(formEntity.getName());
    formDefinition.setKey(formEntity.getKey());
    
    return formDefinition;
  }
}