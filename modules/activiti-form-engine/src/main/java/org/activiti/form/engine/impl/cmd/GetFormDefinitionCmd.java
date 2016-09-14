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

import org.activiti.editor.form.converter.FormJsonConverter;
import org.activiti.form.engine.ActivitiFormObjectNotFoundException;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.interceptor.Command;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.form.engine.impl.persistence.deploy.FormCacheEntry;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.model.FormDefinition;

/**
 * @author Tijs Rademakers
 */
public class GetFormDefinitionCmd implements Command<FormDefinition>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String formDefinitionKey;
  protected String formId;
  protected String tenantId;
  protected String parentDeploymentId;

  public GetFormDefinitionCmd(String formDefinitionKey, String formId) {
    this.formDefinitionKey = formDefinitionKey;
    this.formId = formId;
  }
  
  public GetFormDefinitionCmd(String formDefinitionKey, String formId, String tenantId) {
    this(formDefinitionKey, formId);
    this.tenantId = tenantId;
  }
  
  public GetFormDefinitionCmd(String formDefinitionKey, String formId, String tenantId, String parentDeploymentId) {
    this(formDefinitionKey, formId, tenantId);
    this.parentDeploymentId = parentDeploymentId;
  }

  public FormDefinition execute(CommandContext commandContext) {
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
      
    } else if (formDefinitionKey != null && tenantId != null && !FormEngineConfiguration.NO_TENANT_ID.equals(tenantId) && parentDeploymentId != null) {

      formEntity = deploymentManager.findDeployedLatestFormByKeyParentDeploymentIdAndTenantId(formDefinitionKey, parentDeploymentId, tenantId);
      if (formEntity == null) {
        throw new ActivitiFormObjectNotFoundException("No form found for key '" + formDefinitionKey + 
            "for parent deployment id '" + parentDeploymentId + "' and for tenant identifier " + tenantId, FormEntity.class);
      }

    } else {
      throw new ActivitiFormObjectNotFoundException("formDefinitionKey and formDefinitionId are null");
    }
    
    FormCacheEntry formCacheEntry = deploymentManager.resolveForm(formEntity);
    FormJsonConverter formJsonConverter = commandContext.getFormEngineConfiguration().getFormJsonConverter();
    return formJsonConverter.convertToForm(formCacheEntry.getFormJson(), formEntity.getId(), formEntity.getVersion());
  }
}