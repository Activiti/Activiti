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
package org.activiti.form.engine.impl.deployer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.ActivitiFormIllegalArgumentException;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.context.Context;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntity;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.engine.impl.persistence.entity.FormEntityManager;
import org.apache.commons.lang3.StringUtils;

/**
 * Methods for working with deployments.  Much of the actual work of {@link FormDeployer} is
 * done by orchestrating the different pieces of work this class does; by having them here,
 * we allow other deployers to make use of them.   
 */
public class FormDeploymentHelper  {
  
  /**
   * Verifies that no two decision tables share the same key, to prevent database unique index violation.
   * 
   * @throws ActivitiException if any two decision tables have the same key
   */
  public void verifyFormsDoNotShareKeys(Collection<FormEntity> forms) {
    Set<String> keySet = new LinkedHashSet<String>();
    for (FormEntity form : forms) {
      if (keySet.contains(form.getKey())) {
        throw new ActivitiFormException(
            "The deployment contains forms with the same key, this is not allowed");
      }
      keySet.add(form.getKey());
    }
  }

  /**
   * Updates all the decision table entities to match the deployment's values for tenant,
   * engine version, and deployment id.
   */
  public void copyDeploymentValuesToForms(FormDeploymentEntity deployment, List<FormEntity> forms) {
    String tenantId = deployment.getTenantId();
    String deploymentId = deployment.getId();

    for (FormEntity form : forms) {
      
      // decision table inherits the tenant id
      if (tenantId != null) {
        form.setTenantId(tenantId); 
      }

      form.setDeploymentId(deploymentId);
    }
  }

  /**
   * Updates all the decision table entities to have the correct resource names.
   */
  public void setResourceNamesOnForms(ParsedDeployment parsedDeployment) {
    for (FormEntity form : parsedDeployment.getAllForms()) {
      String resourceName = parsedDeployment.getResourceForForm(form).getName();
      form.setResourceName(resourceName);
    }
  }

  /**
   * Gets the most recent persisted decision table that matches this one for tenant and key.
   * If none is found, returns null.  This method assumes that the tenant and key are properly
   * set on the decision table entity.
   */
  public FormEntity getMostRecentVersionOfForm(FormEntity form) {
    String key = form.getKey();
    String tenantId = form.getTenantId();
    FormEntityManager formEntityManager = Context.getCommandContext().getFormEngineConfiguration().getFormEntityManager();

    FormEntity existingDefinition = null;

    if (tenantId != null && !tenantId.equals(FormEngineConfiguration.NO_TENANT_ID)) {
      existingDefinition = formEntityManager.findLatestFormByKeyAndTenantId(key, tenantId);
    } else {
      existingDefinition = formEntityManager.findLatestFormByKey(key);
    }

    return existingDefinition;
  }

  /**
   * Gets the persisted version of the already-deployed form.  Note that this is
   * different from {@link #getMostRecentVersionOfForm} as it looks specifically for
   * a form that is already persisted and attached to a particular deployment,
   * rather than the latest version across all deployments.
   */
  public FormEntity getPersistedInstanceOfForm(FormEntity form) {
    String deploymentId = form.getDeploymentId();
    if (StringUtils.isEmpty(form.getDeploymentId())) {
      throw new ActivitiFormIllegalArgumentException("Provided form must have a deployment id.");
    }

    FormEntityManager formEntityManager = Context.getCommandContext().getFormEngineConfiguration().getFormEntityManager();
    FormEntity persistedForm = null;
    if (form.getTenantId() == null || FormEngineConfiguration.NO_TENANT_ID.equals(form.getTenantId())) {
      persistedForm = formEntityManager.findFormByDeploymentAndKey(deploymentId, form.getKey());
    } else {
      persistedForm = formEntityManager.findFormByDeploymentAndKeyAndTenantId(deploymentId, form.getKey(), form.getTenantId());
    }

    return persistedForm;
  }
}

