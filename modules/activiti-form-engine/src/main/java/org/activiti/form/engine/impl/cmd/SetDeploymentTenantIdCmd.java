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
import java.util.List;

import org.activiti.form.api.Form;
import org.activiti.form.engine.ActivitiFormIllegalArgumentException;
import org.activiti.form.engine.ActivitiFormObjectNotFoundException;
import org.activiti.form.engine.impl.FormQueryImpl;
import org.activiti.form.engine.impl.interceptor.Command;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntity;

/**
 * @author Joram Barrez
 */
public class SetDeploymentTenantIdCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String deploymentId;
  protected String newTenantId;

  public SetDeploymentTenantIdCmd(String deploymentId, String newTenantId) {
    this.deploymentId = deploymentId;
    this.newTenantId = newTenantId;
  }

  public Void execute(CommandContext commandContext) {
    if (deploymentId == null) {
      throw new ActivitiFormIllegalArgumentException("deploymentId is null");
    }

    // Update all entities

    FormDeploymentEntity deployment = commandContext.getDeploymentEntityManager().findById(deploymentId);
    if (deployment == null) {
      throw new ActivitiFormObjectNotFoundException("Could not find deployment with id " + deploymentId);
    }
    
    deployment.setTenantId(newTenantId);

    // Doing process instances, executions and tasks with direct SQL updates
    // (otherwise would not be performant)
    commandContext.getFormEntityManager().updateFormTenantIdForDeployment(deploymentId, newTenantId);

    // Doing decision tables in memory, cause we need to clear the decision table cache
    List<Form> forms = new FormQueryImpl().deploymentId(deploymentId).list();
    for (Form form : forms) {
      commandContext.getFormEngineConfiguration().getFormCache().remove(form.getId());
    }
    
    commandContext.getDeploymentEntityManager().update(deployment);

    return null;

  }

}
