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
package org.activiti.engine.impl.cmd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.repository.ResourceEntity;


/**
 * @author Joram Barrez
 */
public class GetDeploymentResourceCmd implements Command<InputStream> {
  
  protected String deploymentId;
  protected String resourceName;
  
  public GetDeploymentResourceCmd(String deploymentId, String resourceName) {
    this.deploymentId = deploymentId;
    this.resourceName = resourceName;
  }

  public InputStream execute(CommandContext commandContext) {
    if (deploymentId == null) {
      throw new ActivitiException("deploymentId is null");
    }
    if(resourceName == null) {
      throw new ActivitiException("resourceName is null");
    }
    
    ResourceEntity resource = commandContext
      .getResourceManager()
      .findResourceByDeploymentIdAndResourceName(deploymentId, resourceName);
    if(resource == null) {
      throw new ActivitiException("no resource found with name '" + resourceName + "' in deployment '" + deploymentId + "'");
    }
    return new ByteArrayInputStream(resource.getBytes());
  }
  
}
