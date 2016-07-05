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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

import org.activiti.form.engine.ActivitiFormIllegalArgumentException;
import org.activiti.form.engine.ActivitiFormObjectNotFoundException;
import org.activiti.form.engine.impl.interceptor.Command;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.persistence.entity.ResourceEntity;

/**
 * @author Joram Barrez
 */
public class GetDeploymentResourceCmd implements Command<InputStream>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String deploymentId;
  protected String resourceName;

  public GetDeploymentResourceCmd(String deploymentId, String resourceName) {
    this.deploymentId = deploymentId;
    this.resourceName = resourceName;
  }

  public InputStream execute(CommandContext commandContext) {
    if (deploymentId == null) {
      throw new ActivitiFormIllegalArgumentException("deploymentId is null");
    }
    if (resourceName == null) {
      throw new ActivitiFormIllegalArgumentException("resourceName is null");
    }

    ResourceEntity resource = commandContext.getResourceEntityManager().findResourceByDeploymentIdAndResourceName(deploymentId, resourceName);
    if (resource == null) {
      if (commandContext.getDeploymentEntityManager().findById(deploymentId) == null) {
        throw new ActivitiFormObjectNotFoundException("deployment does not exist: " + deploymentId);
      } else {
        throw new ActivitiFormObjectNotFoundException("no resource found with name '" + resourceName + "' in deployment '" + deploymentId + "'");
      }
    }
    return new ByteArrayInputStream(resource.getBytes());
  }

}
