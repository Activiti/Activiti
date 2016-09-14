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
package org.activiti.form.api;

import java.util.Map;

import org.activiti.form.model.CompletedFormDefinition;
import org.activiti.form.model.FormDefinition;

/**
 * @author Tijs Rademakers
 */
public interface FormService {

    void completeForm(FormDefinition formDefinition);
    
    /**
     * @param definition form definition to use for type-conversion and validation
     * @param values values submitted by the user
     * @param outcome outcome selected by the user. If null, no outcome is used and any outcome definitions are ignored.
     * 
     * @return raw variables that can be used in the activiti engine, based on the filled in values and selected outcome.
     * @throws FormValidationException when a submitted value is not valid or a required value is missing.
     */
    Map<String, Object> getVariablesFromFormSubmission(FormDefinition formDefinition, Map<String, Object> values, String outcome);
    
    /**
     * Store the submitted form values.
     * 
       * @param form form instance of the submitted form
       * @param taskId task instance id of the completed task
       * @param processInstanceId process instance id of the completed task
       * @param valuesNode json node with the values of the 
       */
    SubmittedForm storeSubmittedForm(Map<String, Object> values, FormDefinition formDefinition, String taskId, String processInstanceId);
    
    FormDefinition getTaskFormDefinitionById(String formId, String processInstanceId, Map<String, Object> variables);
    
    FormDefinition getTaskFormDefinitionById(String formId, String processInstanceId, Map<String, Object> variables, String tenantId);
    
    FormDefinition getTaskFormDefinitionByKey(String formDefinitionKey, String processInstanceId, Map<String, Object> variables);
    
    FormDefinition getTaskFormDefinitionByKey(String formDefinitionKey, String processInstanceId, 
        Map<String, Object> variables, String tenantId);
    
    FormDefinition getTaskFormDefinitionByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId, 
        String processInstanceId, Map<String, Object> variables);
    
    FormDefinition getTaskFormDefinitionByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId, String processInstanceId, 
        Map<String, Object> variables, String tenantId);
    
    CompletedFormDefinition getCompletedTaskFormDefinitionById(String formId, String taskId, String processInstanceId, Map<String, Object> variables);
    
    CompletedFormDefinition getCompletedTaskFormDefinitionById(String formId, String taskId, String processInstanceId, 
        Map<String, Object> variables, String tenantId);
    
    CompletedFormDefinition getCompletedTaskFormDefinitionByKey(String formDefinitionKey, String taskId, String processInstanceId, Map<String, Object> variables);
    
    CompletedFormDefinition getCompletedTaskFormDefinitionByKey(String formDefinitionKey, String taskId, String processInstanceId, 
        Map<String, Object> variables, String tenantId);
    
    CompletedFormDefinition getCompletedTaskFormDefinitionByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId,
        String taskId, String processInstanceId, Map<String, Object> variables);
    
    CompletedFormDefinition getCompletedTaskFormDefinitionByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId,
        String taskId, String processInstanceId, Map<String, Object> variables, String tenantId);
    
    SubmittedFormQuery createSubmittedFormQuery();
}
