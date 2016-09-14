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
package org.activiti.form.engine.impl;

import java.io.InputStream;
import java.util.List;

import org.activiti.form.api.Form;
import org.activiti.form.api.FormDeployment;
import org.activiti.form.api.FormDeploymentBuilder;
import org.activiti.form.api.FormDeploymentQuery;
import org.activiti.form.api.FormQuery;
import org.activiti.form.api.FormRepositoryService;
import org.activiti.form.api.NativeFormDeploymentQuery;
import org.activiti.form.api.NativeFormQuery;
import org.activiti.form.engine.impl.cmd.DeleteDeploymentCmd;
import org.activiti.form.engine.impl.cmd.DeployCmd;
import org.activiti.form.engine.impl.cmd.GetDeploymentFormCmd;
import org.activiti.form.engine.impl.cmd.GetDeploymentFormResourceCmd;
import org.activiti.form.engine.impl.cmd.GetDeploymentResourceCmd;
import org.activiti.form.engine.impl.cmd.GetDeploymentResourceNamesCmd;
import org.activiti.form.engine.impl.cmd.GetFormDefinitionCmd;
import org.activiti.form.engine.impl.cmd.SetDeploymentCategoryCmd;
import org.activiti.form.engine.impl.cmd.SetDeploymentTenantIdCmd;
import org.activiti.form.engine.impl.cmd.SetFormCategoryCmd;
import org.activiti.form.engine.impl.interceptor.Command;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.repository.FormDeploymentBuilderImpl;
import org.activiti.form.model.FormDefinition;

/**
 * @author Tijs Rademakers
 */
public class FormRepositoryServiceImpl extends ServiceImpl implements FormRepositoryService {

    public FormDeploymentBuilder createDeployment() {
      return commandExecutor.execute(new Command<FormDeploymentBuilder>() {
        @Override
        public FormDeploymentBuilder execute(CommandContext commandContext) {
          return new FormDeploymentBuilderImpl();
        }
      });
    }
    
    public FormDeployment deploy(FormDeploymentBuilderImpl deploymentBuilder) {
      return commandExecutor.execute(new DeployCmd<FormDeployment>(deploymentBuilder));
    }
    
    public void deleteDeployment(String deploymentId) {
      commandExecutor.execute(new DeleteDeploymentCmd(deploymentId));
    }
    
    public FormQuery createFormQuery() {
      return new FormQueryImpl(commandExecutor);
    }

    public NativeFormQuery createNativeFormQuery() {
      return new NativeFormQueryImpl(commandExecutor);
    }
    
    public List<String> getDeploymentResourceNames(String deploymentId) {
      return commandExecutor.execute(new GetDeploymentResourceNamesCmd(deploymentId));
    }

    public InputStream getResourceAsStream(String deploymentId, String resourceName) {
      return commandExecutor.execute(new GetDeploymentResourceCmd(deploymentId, resourceName));
    }

    public void setDeploymentCategory(String deploymentId, String category) {
      commandExecutor.execute(new SetDeploymentCategoryCmd(deploymentId, category));
    }
    
    public void setDeploymentTenantId(String deploymentId, String newTenantId) {
      commandExecutor.execute(new SetDeploymentTenantIdCmd(deploymentId, newTenantId));
    }

    public FormDeploymentQuery createDeploymentQuery() {
      return new FormDeploymentQueryImpl(commandExecutor);
    }

    public NativeFormDeploymentQuery createNativeDeploymentQuery() {
      return new NativeFormDeploymentQueryImpl(commandExecutor);
    }
    
    public Form getForm(String formId) {
      return commandExecutor.execute(new GetDeploymentFormCmd(formId));
    }
    
    public FormDefinition getFormDefinitionById(String formId) {
      return commandExecutor.execute(new GetFormDefinitionCmd(null, formId));
    }
    
    public FormDefinition getFormDefinitionByKey(String formDefinitionKey) {
      return commandExecutor.execute(new GetFormDefinitionCmd(formDefinitionKey, null));
    }
    
    public FormDefinition getFormDefinitionByKey(String formDefinitionKey, String tenantId) {
      return commandExecutor.execute(new GetFormDefinitionCmd(formDefinitionKey, null, tenantId));
    }
    
    public FormDefinition getFormDefinitionByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId) {
      return commandExecutor.execute(new GetFormDefinitionCmd(formDefinitionKey, null, null, parentDeploymentId));
    }
    
    public FormDefinition getFormDefinitionByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId, String tenantId) {
      return commandExecutor.execute(new GetFormDefinitionCmd(formDefinitionKey, null, tenantId, parentDeploymentId));
    }
    
    public InputStream getFormResource(String formId) {
      return commandExecutor.execute(new GetDeploymentFormResourceCmd(formId));
    }
    
    public void setFormCategory(String formId, String category) {
      commandExecutor.execute(new SetFormCategoryCmd(formId, category));
    }
}
