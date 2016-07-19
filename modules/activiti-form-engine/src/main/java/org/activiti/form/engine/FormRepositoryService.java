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
package org.activiti.form.engine;

import java.io.InputStream;
import java.util.List;

import org.activiti.form.engine.repository.Form;
import org.activiti.form.engine.repository.FormDeploymentBuilder;
import org.activiti.form.engine.repository.FormDeploymentQuery;
import org.activiti.form.engine.repository.FormQuery;
import org.activiti.form.engine.repository.NativeFormDeploymentQuery;
import org.activiti.form.engine.repository.NativeFormQuery;
import org.activiti.form.model.FormDefinition;

/**
 * Service providing access to the repository of process definitions and deployments.
 *
 * @author Tijs Rademakers
 * @author Yvo Swillens
 */
public interface FormRepositoryService {

    FormDeploymentBuilder createDeployment();

    void deleteDeployment(String deploymentId);
    
    FormQuery createFormQuery();
    
    NativeFormQuery createNativeFormQuery();

    void setDeploymentCategory(String deploymentId, String category);
    
    void setDeploymentTenantId(String deploymentId, String newTenantId);

    List<String> getDeploymentResourceNames(String deploymentId);

    InputStream getResourceAsStream(String deploymentId, String resourceName);
    
    FormDeploymentQuery createDeploymentQuery();
    
    NativeFormDeploymentQuery createNativeDeploymentQuery();
    
    Form getForm(String formId);

    FormDefinition getFormDefinitionById(String formId);
    
    FormDefinition getFormDefinitionByKey(String formDefinitionKey);
    
    FormDefinition getFormDefinitionByKey(String formDefinitionKey, String tenantId);
    
    InputStream getFormResource(String formId);
    
    void setFormCategory(String formId, String category);
}
