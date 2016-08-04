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
package org.activiti.form.engine.impl.util;

import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.context.Context;
import org.activiti.form.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.form.engine.impl.persistence.deploy.FormCacheEntry;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.engine.impl.persistence.entity.FormEntityManager;
import org.activiti.form.model.FormDefinition;

/**
 * A utility class that hides the complexity of {@link FormEntity} and {@link Decision} lookup. 
 * Use this class rather than accessing the decision table cache or {@link DeploymentManager} directly.
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class FormUtil {
  
  public static FormEntity getFormEntity(String formId) {
    return getFormEntity(formId, false);
  }

  public static FormEntity getFormEntity(String formId, boolean checkCacheOnly) {
    if (checkCacheOnly) {
      FormCacheEntry cacheEntry = Context.getFormEngineConfiguration().getFormCache().get(formId);
      if (cacheEntry != null) {
        return cacheEntry.getFormEntity();
      }
      return null;
    } else {
      // This will check the cache in the findDeployedProcessDefinitionById method
      return Context.getFormEngineConfiguration().getDeploymentManager().findDeployedFormById(formId);
    }
  }

  public static FormDefinition getFormDefinition(String formId) {
    FormEngineConfiguration formEngineConfiguration = Context.getFormEngineConfiguration();
    DeploymentManager deploymentManager = formEngineConfiguration.getDeploymentManager();
      
    // This will check the cache in the findDeployedProcessDefinitionById and resolveProcessDefinition method
    FormEntity formEntity = deploymentManager.findDeployedFormById(formId);
    FormCacheEntry cacheEntry = deploymentManager.resolveForm(formEntity);
    return formEngineConfiguration.getFormJsonConverter().convertToForm(cacheEntry.getFormJson(), 
        cacheEntry.getFormEntity().getId(), cacheEntry.getFormEntity().getVersion());
  }
  
  public static FormDefinition getFormDefinitionFromCache(String formId) {
    FormEngineConfiguration formEngineConfiguration = Context.getFormEngineConfiguration();
    FormCacheEntry cacheEntry = formEngineConfiguration.getFormCache().get(formId);
    if (cacheEntry != null) {
      return formEngineConfiguration.getFormJsonConverter().convertToForm(cacheEntry.getFormJson(), 
          cacheEntry.getFormEntity().getId(), cacheEntry.getFormEntity().getVersion());
    }
    return null;
  }
  
  public static FormEntity getFormDefinitionFromDatabase(String formId) {
    FormEntityManager formEntityManager = Context.getFormEngineConfiguration().getFormEntityManager();
    FormEntity form = formEntityManager.findById(formId);
    if (form == null) {
      throw new ActivitiFormException("No form found with id " + formId);
    }
    
    return form;
  }
}
